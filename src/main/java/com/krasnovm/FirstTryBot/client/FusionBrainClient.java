package com.krasnovm.FirstTryBot.client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.krasnovm.FirstTryBot.exception.ServiceException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class FusionBrainClient {

    @Autowired
    private OkHttpClient client;

    @Value("${fusionbrain.url}")
    private String url;
    @Value("${fusionbrain.api.key}")
    private String publicKey;
    @Value("${fusionbrain.api.secret.key}")
    private String secretKey;

    @Value("${fusionbrain.styles}")
    private String urlStylesProperty;

    //public Logger LOG = LoggerFactory.getLogger(FusionBrainClient.class);

    public Map<String, String> getStyles() throws ServiceException{
        String urlStyles = urlStylesProperty;
        Request request = new Request.Builder()
                .url(urlStyles)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            Map<String, String> models = new HashMap<>();
            Iterator<JsonNode> iterator = jsonNode.elements();
            while (iterator.hasNext()) {
                JsonNode modelNode = iterator.next();
                models.put(
                        modelNode.at("/name").toString(),
                        modelNode.at("/title").toString()
                );
            }
            return models;
        } catch (IOException e) {
            throw new ServiceException("Failed to get styles", e);
        }
    }

    public String getModel() throws ServiceException {
        //LOG.info("FusionBrainClient getModel");
        String modelId;
        var request = new Request.Builder()
                .url(url + "/models")
                .addHeader("X-Key", "Key " + publicKey)
                .addHeader("X-Secret", "Secret " + secretKey)
                .build();
        try (var response = client.newCall(request).execute()) {
            String json = response.body().string();
            json = json.substring(1, json.length() - 1);
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            modelId = jsonNode.at("/id").toString();
            if (modelId.equals("DISABLED_BY_QUEUE")) {
                throw new ServiceException("Service is not available", new Exception());
            }
        } catch (IOException e) {
            throw new ServiceException("Failed to acquire model", e);
        }
        return modelId;
    }

    public String generateImage(String modelId, String style, String prompt, String negativePromptUnclip,  String width, String height) throws ServiceException {
        //LOG.info("FusionBrainClient generateImage");
        String type = "GENERATE";
        String numImages = "1";

        MediaType JSON = MediaType.parse("application/json");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNodeParams = mapper.createObjectNode();
        rootNodeParams.put("type", type);
        rootNodeParams.put("style", style);
        rootNodeParams.put("width", width);
        rootNodeParams.put("height", height);
        rootNodeParams.put("numImages", numImages);
        rootNodeParams.put("negativePromptUnclip", negativePromptUnclip);
        ObjectNode childNode = mapper.createObjectNode();
        //LOG.info(prompt);
        childNode.put("query", prompt);
        rootNodeParams.set("generateParams", childNode);
        RequestBody requestBody;
        try {
            requestBody = MultipartBody.create(mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(rootNodeParams), JSON);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Failed to turn json into requestBody", e);
        }
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model_id", modelId)
                .addFormDataPart("params", "params.json", requestBody);
        Request request = new Request.Builder()
                .url(url + "/text2image/run")
                .addHeader("X-Key", "Key " + publicKey)
                .addHeader("X-Secret", "Secret " + secretKey)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            String uuid = jsonNode.at("/uuid").toString();
            return uuid;
        } catch (IOException e) {
            throw new ServiceException("Failed to send generation request", e);
        }
    }

    public String checkImage(String requestId) throws ServiceException {
        //LOG.info("FusionBrainClient checkImage");
        int attempts = 60;
        int delay = 5_000;

        Request request = new Request.Builder()
                .url(url + "/text2image/status/" + requestId)
                .addHeader("X-Key", "Key " + publicKey)
                .addHeader("X-Secret", "Secret " + secretKey)
                .build();

        while (attempts > 0) {
            try (Response response = client.newCall(request).execute()) {
                String json = response.body().string();
                JsonNode jsonNode = new ObjectMapper().readTree(json);
                String status = jsonNode.at("/status").toString();
                if (status.equals("\"DONE\"")) {
                    StringBuilder stringBuilder = new StringBuilder(jsonNode.at("/images").toString());
                    stringBuilder
                            .delete(stringBuilder.length()-2, stringBuilder.length())
                            .delete(0, 2);
                    return stringBuilder.toString();
                } else if (status.equals("\"FAIL\"")) {
                    String errorDescriptions = jsonNode.at("/errorDescription").toString();
                    throw new ServiceException("Service failed to generate image: " + errorDescriptions, new Exception());
                }
            } catch (IOException e) {
                throw new ServiceException("Failed to check generation request", e);
            }
            attempts--;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new ServiceException("checkImage thread interrupted", e);
            }
        }
        throw new ServiceException("Image generation >5 min", new Exception());
    }

    public String decodeImage(String requestId, String base64img) throws ServiceException{
        //LOG.info("decodeImage");
        String path = "/src/images";
        File folder = new File(path);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String pathToImg = path+requestId+".jpg";

        StringBuilder base64imgFix = new StringBuilder(base64img);
        int numFixes = base64imgFix.length() % 4;
        if (numFixes > 0) {
            for (int i = 0; i < numFixes; i++) {
                base64imgFix.append("=");
            }
        }
        base64img = base64imgFix.toString();

        BufferedImage image;
        byte[] imageByte = Base64Coder.decode(base64img);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageByte)) {
            image = ImageIO.read(bis);
        } catch (IOException e) {
            throw new ServiceException("Failed to read image from byte array", e);
        }

        if (image != null) {
            File file = new File(pathToImg);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    throw new ServiceException("Img file already exists", new Exception());
                }
                ImageIO.write(image, "jpg", file);
                return pathToImg;
            } catch (IOException e) {
                throw new ServiceException("Failed to write image from buffer", e);
            }
        }
        throw new ServiceException("Failed to save image", new Exception());
    }

}
