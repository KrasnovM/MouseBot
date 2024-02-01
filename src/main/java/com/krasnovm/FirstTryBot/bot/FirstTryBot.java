package com.krasnovm.FirstTryBot.bot;

import com.krasnovm.FirstTryBot.exception.ServiceException;
import com.krasnovm.FirstTryBot.service.AnecService.AnecService;
import com.krasnovm.FirstTryBot.service.AsciiArtsService.AsciiService;
import com.krasnovm.FirstTryBot.service.ExchangeRatesService.ExchangeRatesService;
import com.krasnovm.FirstTryBot.service.FusionBrainService.FusionBrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.ForkJoinPool;

@Component
public class FirstTryBot extends TelegramLongPollingBot {

    @Autowired
    private ExchangeRatesService exchangeRatesService;

    @Autowired
    private AnecService anecService;

    @Autowired
    private AsciiService asciiService;

    @Autowired
    private FusionBrainService fusionBrainService;

    //public static final Logger LOG = LoggerFactory.getLogger(FirstTryBot.class);

    //private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    public static final String START = "/start";
    public static final String HELP = "/help";
    public static final String CURR_RATES = "/currency";
    public static final String ANEC = "/anec";
    public static final String ASCII = "/asciiart";
    public static final String GEN = "/gen";
    public static final String STYLE = "/styles";

    public static final String HELPMES = """
                                            Список команд:
                                            /help - список команд
                                            /currency - курсы валют на сегодня
                                            /asciiart - ascii арты
                                            /anec - анекдот
                                            /gen - сгенерировать изображение
                                            /styles - доступные стили для генерации изображений
                                            """;

    public static final String GREETINGS = """
                                            Мышебот приветствует тебя, %s!
                                            ..........()-().........
                                            ...........\\"/..........
                                            ............`...........
                                            """ + HELPMES;

    public static final String CURR_MES = """
                                            Курсы на %s составляют:
                                            Доллар: %s рублей
                                            Евро: %s рублей
                                            Юань: %s рублей
                                            Фунт стерлингов: %s рублей
                                            Лира: %s рублей
                                            Гривна: %s рублей
                                            Йена: %s рублей
                                            Вона: %s рублей
                                            """;

    public static final String HELP_STYLES = """
                                        На момент последнего теста были доступны следующие модели:
                                        
                                        "KANDINSKY": рисунок художника, иногда переходит в абстракционизм;
                                        "UHD": хорош для фотореализма;
                                        "ANIME": мультяшная рисовка;
                                        "DEFAULT": в среднем хорошо справляется со всем;
                                        
                                        Впрочем, модели способны вести себя очень по-разному в завсимости от запросов.
                                        """;

    public FirstTryBot(@Value("${bot.token}")String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        forkJoinPool.submit(() -> {
            //LOG.info(String.valueOf(ForkJoinPool.getCommonPoolParallelism()));
            //LOG.info("onUpdateReceived");
            if (!update.hasMessage() || !update.getMessage().hasText()) {
                return;
            }
            var message = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();
            switch (message) {
                case START -> startCommand(chatId, update.getMessage().getChat().getUserName());
                case HELP -> helpCommand(chatId);
                case CURR_RATES -> currCommand(chatId);
                case ANEC -> anecCommand(chatId);
                case ASCII -> asciiCommand(chatId);
                case GEN -> sendMessage(chatId, fusionBrainService.getHelp(chatId));
                case STYLE -> stylesCommand(chatId);
                default -> {
                    if (message.contains("gen/")) {
                        genCommand(chatId, message);
                    }
                }
            }
        });
    }

    private void startCommand(Long chatId, String userName) {
        var text = GREETINGS;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void helpCommand(Long chatId) {
        var text = HELPMES;
        sendMessage(chatId, text);
    }

    private void currCommand(Long chatId) {
        String formattedText;
        try {
            String usd = exchangeRatesService.getUSDExchangeRate();
            String eur = exchangeRatesService.getEURExchangeRate();
            String cny = exchangeRatesService.getCNYExchangeRate();
            String gbp = exchangeRatesService.getGBPExchangeRate();
            String TRY = exchangeRatesService.getTRYExchangeRate();
            String uah = exchangeRatesService.getUAHExchangeRate();
            String jpy = exchangeRatesService.getJPYExchangeRate();
            String krw = exchangeRatesService.getKRWExchangeRate();
            String text = CURR_MES;

            formattedText = String.format(text, LocalDate.now(),
                    usd, eur, cny, gbp, TRY, uah, jpy, krw);
        } catch (ServiceException e) {
            //LOG.error("Failed to get currencies", e);
            formattedText = "Не удалось получить текущее значение курсов валют. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void asciiCommand(Long chatId) {
        String asciiArt = asciiService.getAciiArt();
        sendMessage(chatId, asciiArt);
    }

    private void anecCommand(Long chatId) {
        String text;
        try {
            text = anecService.getAnec();
        } catch (ServiceException e) {
            text = "Не удалось получить анек, попробуйте позже";
        }
        if (text.contains("<")) {
            text = "Серверу не удалось прочитать анек, попробуйте снова";
        }
        sendMessage(chatId, text);
    }

    private void stylesCommand(long chatId) {
        try {
            sendMessage(chatId, fusionBrainService.availableStyles().toString()
                + "\n\n" + HELP_STYLES);
        } catch (ServiceException e) {
            sendMessage(chatId, "Couldn't get styles");
        }
    }

    private void genCommand(Long chatId, String message) {
        //LOG.info("genCommand");
        //validation of syntax
        int tags = StringUtils.countOccurrencesOf(message, "/");
        if (tags < 6) {
            sendMessage(chatId, "Missing \"/\" in query");
            return;
        } else if (tags > 6) {
            sendMessage(chatId, "Too many \"/\" in query");
            return;
        }

        String width;
        String height;
        String prompt;
        String negPrompt;
        try {
            width = fusionBrainService.parseWidth(message);
            height = fusionBrainService.parseHeight(message);
            prompt = fusionBrainService.parsePrompt(message);
            negPrompt = fusionBrainService.parseNegPrompt(message);
        } catch (ServiceException e) {
            sendMessage(chatId, e.getMessage());
            return;
        }
        String model = fusionBrainService.parseModel(message);

        String path;
        try {
            path = fusionBrainService.getImage(chatId, model, prompt, negPrompt, width, height);
        } catch (ServiceException e) {
            sendMessage(chatId, "Request failed, couldn't get image from service");
            return;
        }

        if (path != null) {
            File file = new File(path);
            InputFile image = new InputFile(file);
            String chatIdStr = String.valueOf(chatId);
            SendPhoto sendPhoto = new SendPhoto(chatIdStr, image);
            sendPhoto(sendPhoto);
            if (file.exists()) {
                file.delete();
            }
        } else {
            sendMessage(chatId, "Request failed, couldn't get file from host");
        }
    }

    @Override
    public String getBotUsername() {
        return "Мышебот";
    }

    private void sendMessage(Long chatId, String text) {
        String chatIdStr = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            //LOG.error("sendMessage failed", e);
        }
    }

    private void sendPhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            //LOG.error("sendPhoto failed", e);
        }
    }
}
