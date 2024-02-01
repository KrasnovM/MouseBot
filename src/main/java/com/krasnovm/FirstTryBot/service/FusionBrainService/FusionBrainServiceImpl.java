package com.krasnovm.FirstTryBot.service.FusionBrainService;

import com.krasnovm.FirstTryBot.client.FusionBrainClient;
import com.krasnovm.FirstTryBot.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FusionBrainServiceImpl implements FusionBrainService {

    @Autowired
    FusionBrainClient client;

    public static final String HELP = """
                                        Пример запроса 1:
                                        gen/DEFAULT/1024/1024/pixel art кот/красные глаза и серая шерсть/
                                        
                                        Пример запроса 2:
                                        gen/ANIME/1024/512/кот в темных очках//
                                        
                                        Пример запроса 3:
                                        gen/KANDINSKY/900/900/steampunk engineer/vehicle, monocle/

                                        Пример запроса 3:
                                        gen/UHD/1024/1024/инженер/очки/
                                        
                                        Параметры запроса:
                                        - Разделитель "/" (между тегами и в конце запроса)
                                        - Тег генерации (всегда "gen")
                                        - Название модели из /styles
                                        - Горизонтальный размер изображения (от 128 до 1024 пискселей)
                                        - Вертикальный размер изображения (от 128 до 1024 пискселей)
                                        - Запрос для генерации (до 1000 символов на русском и английском)
                                        - Исключения из генерации
                                        """;

    //public Logger LOG = LoggerFactory.getLogger(FusionBrainService.class);

    @Override
    public String getHelp(Long chatId) {
        return HELP;
    }

    @Override
    public Map<String, String> availableStyles() throws ServiceException {
        return client.getStyles();
    }

    @Override
    public String getImage(Long chatId, String style, String prompt, String negPrompt, String width, String height) throws ServiceException {
        //LOG.info("FusionBrainService getImage");
        String modelId;
        String requestId;
        String base64img;
        String path;

        try {
            modelId = client.getModel();
            requestId = client.generateImage(modelId, style, prompt, negPrompt, width, height);
            try {
                requestId = requestId.substring(1, requestId.length()-1);
            } catch (StringIndexOutOfBoundsException e) {
                throw new ServiceException("Didn't get requestId to get image", e);
            }
            base64img = client.checkImage(requestId);
            path = client.decodeImage(requestId, base64img);
            return path;
        } catch (ServiceException e) {
            throw new ServiceException("Image not acquired", e);
        }
    }

    @Override
    public String parseMessage(String message, int position) {
        String copyMesage = message;
        int pos = position;
        int nextDelimeter = copyMesage.indexOf("/");
        int prevDelimeter = 0;
        while (pos>0) {
            prevDelimeter = nextDelimeter;
            copyMesage = copyMesage.replaceFirst("/", "@");
            nextDelimeter = copyMesage.indexOf("/");
            pos--;
        }
        return copyMesage.substring(prevDelimeter+1, nextDelimeter);
    }

    @Override
    public String parseModel(String message){
        String model = parseMessage(message, 1).toUpperCase();
        return model;
    }

    @Override
    public String parseWidth(String message) throws ServiceException{
        String width = parseMessage(message, 2);

        Integer intWidth;
        try {
            intWidth = Integer.parseInt(width);
            if (intWidth > 1024 || intWidth < 128) {
                throw new ServiceException("Width out of bounds 128 to 1024", new Exception());
            }
        } catch (NumberFormatException e) {
            throw new ServiceException("Width is not a number", e);
        }
        return width;
    }

    @Override
    public String parseHeight(String message) throws ServiceException{
        String height = parseMessage(message, 3);
        Integer intHeight;
        try {
            intHeight = Integer.parseInt(height);
            if (intHeight > 1024 || intHeight < 128) {
                throw new ServiceException("Height out of bounds 128 to 1024", new Exception());
            }
        } catch (NumberFormatException e) {
            throw new ServiceException("Height is not a number", e);
        }
        return height;
    }

    @Override
    public String parsePrompt(String message) throws ServiceException{
        String prompt = parseMessage(message, 4);
        if (prompt.length() > 1024) {
            throw new ServiceException("Описание больше 1000 символов", new Exception());
        }
        return prompt;
    }

    @Override
    public String parseNegPrompt(String message) throws ServiceException{
        String negPrompt = parseMessage(message, 5);
        if (negPrompt.length() > 1024) {
            throw new ServiceException("Описание negative prompt больше 1000 символов", new Exception());
        }
        return negPrompt;
    }

}
