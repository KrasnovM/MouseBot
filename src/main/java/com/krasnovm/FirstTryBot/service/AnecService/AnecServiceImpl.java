package com.krasnovm.FirstTryBot.service.AnecService;

import com.krasnovm.FirstTryBot.client.AnecClient;
import com.krasnovm.FirstTryBot.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnecServiceImpl implements AnecService {
    @Autowired
    private AnecClient client;

    private String fetchAnec() throws ServiceException {
        return client.getAnec();
    }

    @Override
    public String getAnec() throws ServiceException {
        return extractAnec(fetchAnec());
    }

    private static  String extractAnec(String anec) {
        Pattern pattern = Pattern.compile("<p>(.*?)</p>");
        Matcher matcher = pattern.matcher(anec);
        while (matcher.find()) {
            anec = anec.substring(matcher.start(), matcher.end());
        }
        anec = anec
                .replaceAll("</p>","")
                .replaceAll("<p>","")
                .replaceAll("<br />", "\n")
                .replaceAll("<br>", "")
                .replaceAll("\n ", "\n");
        return anec;
    }
}
