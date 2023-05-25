package pro.sky.jpatelegram.bot;

import jakarta.annotation.PostConstruct;
import org.aspectj.bridge.Message;
import org.hibernate.sql.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.jpatelegram.entity.NotificationTask;
import pro.sky.jpatelegram.listener.TelegramBotUpdatesListener;
import pro.sky.jpatelegram.service.NotificationTaskService;

import javax.management.Notification;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdateListener implements UpdatesListener {

    //
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern pattern = Pattern.compile(
            "^(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2})\\s+([А-яA-z\\d\\s.,!?:;]+)$"
    );
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd.MM.yyyy HH:mm"
    );
    private final TelegramBot telegramBot;
    private final Notification notification;

    @Autowired
    public TelegramUpdateListener(
            TelegramBot telegramBot,
            Notification notification) {
        this.telegramBot = telegramBot;
        this.notification = notification;
    }
    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        logger.info("Processing update: {}", update);

                        Message message = update.message();
                        Long chatId = message.chat().id();
                        String text = message.text();
                        if ("/start".equals(text)) {
                            sendMessage(chatId, """
                  Привет!
                  Чтобы запланировать задачу, отправь сообщение в следующем формате:
                      _01.01.2022 20:00 Сделать домашнюю работу """
                            );
                        } else if (text != null) {
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                Optional<LocalDateTime> dateTime = parseDateTime(matcher.group(1));
                                if (dateTime.isEmpty()) {
                                    sendMessage(chatId, "Некорректный формат даты/времени!");
                                } else {
                                    String txt = matcher.group(2);
                                    NotificationTask notificationTask = new NotificationTask();
                                    notificationTask.setChatId(chatId);
                                    notificationTask.setMessage(txt);
                                    notificationTask.setNotificationDateTime(dateTime.get());
                                    notificationTaskService.save(notificationTask);
                                    sendMessage(chatId, "Задача запланирована!");
                                }
                            } else {
                                sendMessage(chatId, "Некорректный формат сообщения!");
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private Optional<LocalDateTime> parseDateTime(String dateTime) {
        try {
            return Optional.of(LocalDateTime.parse(dateTime, dateTimeFormatter));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.parseMode(ParseMode.Markdown);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error during sending message: {}", sendResponse.description());
        }
    }
    //
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }
    private static final Pattern NOTIFICATION_PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\s=]=)");

    private static final DateTimeFormatter NOTIFICATION_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;


    public void TelegramUpdateListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationBot = notificationTaskService;
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
