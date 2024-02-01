package com.krasnovm.FirstTryBot.scheduler;

import com.krasnovm.FirstTryBot.service.ExchangeRatesService.ExchangeRatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class InvalidationScheduler {

    @Autowired
    private ExchangeRatesService service;

    @Scheduled(cron = "* 0 0 * * ?")
    public void invalidateCache() {
        service.clearCache();
    }
}
