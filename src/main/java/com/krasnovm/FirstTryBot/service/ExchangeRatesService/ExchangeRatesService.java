package com.krasnovm.FirstTryBot.service.ExchangeRatesService;

import com.krasnovm.FirstTryBot.exception.ServiceException;

public interface ExchangeRatesService {
    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;

    String getCNYExchangeRate() throws ServiceException;

    String getGBPExchangeRate() throws ServiceException;

    String getTRYExchangeRate() throws ServiceException;

    String getUAHExchangeRate() throws ServiceException;

    String getJPYExchangeRate() throws ServiceException;

    String getKRWExchangeRate() throws ServiceException;

    void clearCache();
}
