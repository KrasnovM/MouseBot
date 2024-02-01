package com.krasnovm.FirstTryBot.service.FusionBrainService;

import com.krasnovm.FirstTryBot.exception.ServiceException;

import java.util.Map;

public interface FusionBrainService {

    String getHelp(Long chatId);

    Map<String, String> availableStyles() throws ServiceException;

    String getImage(Long chatId, String style, String prompt, String negPrompt, String width, String height) throws ServiceException;

    String parseMessage(String message, int position);

    String parsePrompt(String message) throws ServiceException;

    String parseNegPrompt(String message) throws ServiceException;

    String parseModel(String message);

    String parseWidth(String message) throws ServiceException;

    String parseHeight(String message) throws ServiceException;

}
