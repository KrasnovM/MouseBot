package com.krasnovm.FirstTryBot;

import com.krasnovm.FirstTryBot.client.AnecClient;
import com.krasnovm.FirstTryBot.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class AnecServiceImplTests {

    @Autowired
    AnecClient anecClient;

    private String anec = null;

    @Test
    void getAnecTest() {
        try {
            this.anec = anecClient.getAnec();
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        Pattern pattern = Pattern.compile("<p>(.*?)</p>");
        Matcher matcher = pattern.matcher(anec);
        while (matcher.find()) {
            anec = anec.substring(matcher.start(), matcher.end());
        }
        anec = anec
                .replaceAll("<p>"," ")
                .replaceAll("</p>","")
                .replaceAll("<br />?", "\n");
        System.out.println(anec);
    }

}
