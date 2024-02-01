package com.krasnovm.FirstTryBot;

import com.krasnovm.FirstTryBot.service.AsciiArtsService.AsciiArts;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FirstTryBotAsciiArtsTests {

    @Test
    void getArts() {
        String asciiArt;
        for (int i=0; i<90; i++) {
            int random = (int) (Math.random() * (10 - 1)) + 1;
            switch (random) {
                case 1 -> {
                    asciiArt = AsciiArts.EMOTICONS.getArt();
                }
                case 2 -> {
                    asciiArt = AsciiArts.MOUSE2.getArt();
                }
                case 3 -> {
                    asciiArt = AsciiArts.TELELOGO.getArt();
                }
                case 4 -> {
                    asciiArt = AsciiArts.MOUSE4.getArt();
                }
                case 5 -> {
                    asciiArt = AsciiArts.PAPERCLIP.getArt();
                }
                case 6 -> {
                    asciiArt = AsciiArts.CAT2.getArt();
                }
                case 7 -> {
                    asciiArt = AsciiArts.CAT3.getArt();
                }
                case 8 -> {
                    asciiArt = AsciiArts.CAT4.getArt();
                }
                case 9 -> {
                    asciiArt = AsciiArts.WEB.getArt();
                }
                default -> {
                    asciiArt = "Арты сломалисб";
                }
            }
            System.out.println(random);
            System.out.println(asciiArt);
        }
    }
}
