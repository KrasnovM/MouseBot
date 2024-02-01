package com.krasnovm.FirstTryBot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.krasnovm.FirstTryBot.client.FusionBrainClient;
import com.krasnovm.FirstTryBot.exception.ServiceException;
import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SpringBootTest
public class FusionBrainClientTests {
    @Autowired
    private OkHttpClient client;

    @Autowired
    FusionBrainClient fusionBrainClient;

    @Value("${fusionbrain.url}")
    private String url;
    @Value("${fusionbrain.api.key}")
    private String publicKey;
    @Value("${fusionbrain.api.secret.key}")
    private String secretKey;

    public static final Logger LOG = LoggerFactory.getLogger(FusionBrainClientTests.class);

    /*@Test
    public void checkStatus() throws ServiceException {
        var request = new Request.Builder()
                .url(url+"/text2image/availability")
                .build();
        LOG.info(request.toString());
        try (var response = client.newCall(request).execute()) {
            String json = response.body().string();
            LOG.info(json);
            json = json.substring(1, json.length()-1);
            LOG.info(json);
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            LOG.info(jsonNode.toString());
            String modelStatus =
                    jsonNode.at("/model_status").toString();
            LOG.info(modelStatus);
            if (modelStatus.equals("DISABLED_BY_QUEUE")) {
                throw new ServiceException("Model is not available",
                        new Exception());
            }
        } catch (IOException e) {
            throw new ServiceException("Failed to check model status", e);
        }
    }*/

    @Test
    public void getModel() throws ServiceException {
        LOG.info(url);
        Request request = new Request.Builder()
                .url(url+"/models")
                .addHeader("X-Key", "Key "+publicKey)
                .addHeader("X-Secret", "Secret "+secretKey)
                .build();
        LOG.info(request.toString());
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            LOG.info(json);
            json = json.substring(1, json.length()-1);
            LOG.info(json);
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            LOG.info(jsonNode.toString());
            String modelId = jsonNode.at("/id").toString();
            LOG.info(modelId);
            if(modelId.equals("DISABLED_BY_QUEUE")) {
                throw new ServiceException("Service is not available", new Exception());
            }
        } catch (IOException e) {
            throw new ServiceException("Failed to acquire model", e);
        }
    }

    /*@Test
    public void generateImage() throws ServiceException {
        String prompt = "cat"; //<1000 characters
        String width = "1024";
        String height = "1024";

        //hard-coded, no variations
        String type = "GENERATE";
        String numImages = "1";
        String modelId = fusionBrainClient.getModel();

        MediaType JSON = MediaType.parse("application/json");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNodeParams = mapper.createObjectNode();
        rootNodeParams.put("type", type);
        rootNodeParams.put("numImages", numImages);
        rootNodeParams.put("width", width);
        rootNodeParams.put("height", height);
        ObjectNode childNode = mapper.createObjectNode();
        childNode.put("query", prompt);
        rootNodeParams.set("generateParams", childNode);

        String jsonString;
        try {
            jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNodeParams);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        LOG.info(jsonString);

        RequestBody requestBody;
        try {
            requestBody = MultipartBody.create(mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(rootNodeParams), JSON);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model_id", modelId)
                .addFormDataPart("params", "params.json", requestBody);
        Request request = new Request.Builder()
                .url(url+"/text2image/run")
                .addHeader("X-Key", "Key "+publicKey)
                .addHeader("X-Secret", "Secret "+secretKey)
                .post(builder.build())
                .build();

        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info(buffer.readUtf8());
        LOG.info(request.toString());
        LOG.info(request.body().contentType().toString());

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            LOG.info(json);
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            LOG.info(jsonNode.toString());
            String uuid = jsonNode.at("/uuid").toString();
            LOG.info(uuid);
            String status = jsonNode.at("/status").toString();
            LOG.info(status);
        } catch (IOException e) {
            throw new ServiceException("Failed to send generation request", e);
        }
    }*/

    /*@Test
    public void checkImage() throws ServiceException{
        String requestId = "6832e17a-26bc-42a0-8c3b-d61273cbad7f";
        int attempts = 10;
        int delay = 30_000;

        Request request = new Request.Builder()
                .url(url+"/text2image/status/"+requestId)
                .addHeader("X-Key", "Key "+publicKey)
                .addHeader("X-Secret", "Secret "+secretKey)
                .build();

        while (attempts > 0) {
            try (Response response = client.newCall(request).execute()) {
                String json = response.body().string();
                //LOG.info(json);
                JsonNode jsonNode = new ObjectMapper().readTree(json);
                //LOG.info(jsonNode.toString());
                String status = jsonNode.at("/status").toString();
                LOG.info(status);
                if (status.equals("\"DONE\"")) {
                    StringBuilder stringBuilder = new StringBuilder(jsonNode.at("/images").toString());
                    stringBuilder
                            .delete(stringBuilder.length()-2, stringBuilder.length())
                            .delete(0, 2);
                    String images = stringBuilder.toString();
                    LOG.info(images);
                    return;
                } else if (status.equals("\"FAIL\"")) {
                    String errorDescriptions = jsonNode.at("/errorDescription").toString();
                    throw new ServiceException("Service failed to generate image: "+errorDescriptions, new Exception());
                }
            } catch (IOException e) {
                throw new ServiceException("Failed to check generation request", e);
            }
            attempts--;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    /*@Test
    public void decodeImage() throws ServiceException{
        String requestId = "1231241";
        String base64img;

        try (BufferedReader br = new BufferedReader(new FileReader("src/img.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            base64img = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        base64img = base64img.substring(0, base64img.length()-2);
        *//*if (base64img.length() % 4 > 0) {
            base64img += "=".repeat(4 - base64img.length() % 4);
        }*//*
        LOG.info(String.valueOf(base64img.length()));

        String path = "src/images/"+requestId+".jpg";
        BufferedImage image;

        byte[] imageByte = Base64Coder.decode(base64img);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageByte)) {
            image = ImageIO.read(bis);
        } catch (IOException e) {
            throw new ServiceException("Failed to read image from byte array", e);
        }

        if (image != null) {
            File file = new File(path);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    throw new ServiceException("Img file already exists", new Exception());
                }
                ImageIO.write(image, "jpg", file);
                LOG.info(path);
                return;
            } catch (IOException e) {
                throw new ServiceException("Failed to write image from buffer", e);
            }
        }
        throw new ServiceException("Failed to save image", new Exception());
    }*/

    @Test
    public void getStyles() throws ServiceException{
        String urlStyles = "https://cdn.fusionbrain.ai/static/styles/api";
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
            LOG.info(models.toString());
        } catch (IOException e) {
            throw new ServiceException("Failed to get styles", e);
        }
    }
}
