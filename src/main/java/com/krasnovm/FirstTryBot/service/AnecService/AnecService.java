package com.krasnovm.FirstTryBot.service.AnecService;

import com.krasnovm.FirstTryBot.exception.ServiceException;

public interface AnecService {
    String getAnec() throws ServiceException;
}
