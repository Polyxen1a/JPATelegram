package pro.sky.jpatelegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.jpatelegram.handlers.TelegramHandler;
import pro.sky.jpatelegram.service.NotificationService;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramUpdateListener implements UpdatesListener {

    private static final String HELP_TEXT =
            "Могу напомнить Вам о чём-то. Для этого отправьте сообщение в формате <code>01.01.2020 20:00 Приготовить суп</code>";


    private static final Logger LOG = LoggerFactory.getLogger(TelegramUpdateListener.class);

    private static final Pattern NOTIFICATION_PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\s=]=)");

    private static final DateTimeFormatter NOTIFICATION_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;


    public TelegramUpdateListener(TelegramBot telegramBot, NotificationService notificationService) {
        this.telegramBot = telegramBot;
        this.notificationBot = notificationService;
        this.telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.stream().filter(update -> update.message() != null).forEach(this::handleUpdate);
        return CONFIRMED_UPDATES_ALL;
    }

    private void handleUpdate(Update update) {
        if (update.message().text() != null) {
            processText(update);
        } else {
            this.sendMessage(update.message().chat().id());
        }
    }

    private void processText(Update update) {
        String text = update.message().text();
        Long chatId = update.message().chat().id();
        switch (text) {
            case "/start" -> sendMessage(
                    chatId, String.forman("Привет, %s, %s", update.message().from().firstName(), HELP_TEXT));
            case "/help" -> sendMessage(chatId, HELP_TEXT);
            default -> {
                Matcher matcher = NOTIFICATION_PATTERN.matcher(text);
                if (matcher.matches()) {
                    LOG.info(matcher.group());
                } else {
                    this.defaultMessage(chatId);
                }
            }
        }
    }
        private void handleNotification(Long chatId, String dataString, String notificationMessage) {
            LOG.info(
                    "Got notification message, chat_id={}, date={}, notification={}",
                    chatId,
                    dateString,
                    notificationMessage);
            try {
                LocalDateTime notificationDate = LocalDateTime.parse(dateString, NOTIFICATION_DATE_FORMATTER);
                notificationService.createNotification(chatId, notificationDate, notificationMesage);
                this.sendMessage(chatId, "Нотификация создана");
            } catch (DateTimeException e) {
                LOG.error("Got notification with wrong date, chat_id={}, date={}", chatId, dataString, e);
                this.sendMessage(chatId, "Неверная дата, используйте /help");
            }
        }

        private void defaultMessage (Long chatId) {
            this.sendMessage(chatId, "Неизвестный формат сообщения - попробуйте /help");
        }
    
    private void sendMessage(Long chatId, String text) {
        this.telegramBot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML));
    }
}
