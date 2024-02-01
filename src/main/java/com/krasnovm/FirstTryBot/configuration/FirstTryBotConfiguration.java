package com.krasnovm.FirstTryBot.configuration;

import com.krasnovm.FirstTryBot.bot.FirstTryBot;
import okhttp3.OkHttpClient;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@EnableCaching
@EnableScheduling
public class FirstTryBotConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(FirstTryBot firstTryBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(firstTryBot);
        return api;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

}
