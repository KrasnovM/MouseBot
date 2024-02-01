package com.krasnovm.FirstTryBot.client;

import com.krasnovm.FirstTryBot.exception.ServiceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AnecClient {
    @Autowired
    private OkHttpClient client;

    @Value("${anec.url}")
    private String url;

    public String getAnec() throws ServiceException {
        var request = new Request.Builder()
                .url(url)
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? null : body.string();
        } catch (IOException e) {
            throw new ServiceException("Failed to acquire anec", e);
        }
    }
}
