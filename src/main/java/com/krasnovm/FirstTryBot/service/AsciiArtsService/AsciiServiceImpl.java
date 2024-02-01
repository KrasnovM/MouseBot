package com.krasnovm.FirstTryBot.service.AsciiArtsService;

import org.springframework.stereotype.Service;

@Service
public class AsciiServiceImpl implements AsciiService {
    @Override
    public String getAciiArt() {
        String asciiArt;
        int random = (int) (Math.random() * (AsciiArts.values().length)) + 1;
        switch (random) {
            case 1 -> asciiArt = AsciiArts.EMOTICONS.getArt();
            case 2 -> asciiArt = AsciiArts.MOUSE2.getArt();
            case 3 -> asciiArt = AsciiArts.TELELOGO.getArt();
            case 4 -> asciiArt = AsciiArts.MOUSE4.getArt();
            case 5 -> asciiArt = AsciiArts.PAPERCLIP.getArt();
            case 6 -> asciiArt = AsciiArts.CAT2.getArt();
            case 7 -> asciiArt = AsciiArts.CAT3.getArt();
            case 8 -> asciiArt = AsciiArts.CAT4.getArt();
            case 9 -> asciiArt = AsciiArts.WEB.getArt();
            case 10 -> asciiArt = AsciiArts.WELCOME.getArt();
            case 11 -> asciiArt = AsciiArts.HOMER.getArt();
            case 12 -> asciiArt = AsciiArts.CAMEL.getArt();
            case 13 -> asciiArt = AsciiArts.MONKEY.getArt();
            case 14 -> asciiArt = AsciiArts.DONKEYKONG.getArt();
            case 15 -> asciiArt = AsciiArts.SAW.getArt();
            case 16 -> asciiArt = AsciiArts.ANON.getArt();
            case 17 -> asciiArt = AsciiArts.SKULL.getArt();
            case 18 -> asciiArt = AsciiArts.GRAB.getArt();
            case 19 -> asciiArt = AsciiArts.SCREAM.getArt();
            case 20 -> asciiArt = AsciiArts.LETSGO.getArt();
            default -> asciiArt = "Арты сломалисб";
        }
        return asciiArt;
    }
}
