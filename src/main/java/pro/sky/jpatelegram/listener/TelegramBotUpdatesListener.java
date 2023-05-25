package pro.sky.jpatelegram.listener;

import jakarta.annotation.PostConstruct;
import org.aspectj.bridge.Message;
import org.hibernate.sql.Update;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pro.sky.jpatelegram.entity.NotificationTask;
import pro.sky.jpatelegram.service.NotificationTaskService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
    public class TelegramBotUpdatesListener implements UpdatesListener {

        private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
        private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
        private final Pattern pattern = Pattern.compile(
                "^(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2})\\s+([А-яA-z\\d\\s.,!?:;]+)$"
        );
        private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                "dd.MM.yyyy HH:mm"
        );
        private final TelegramBot telegramBot;
        private final NotificationTaskService notificationTaskService;
        @Autowired
        public TelegramBotUpdatesListener(
                TelegramBot telegramBot,
                NotificationTaskService notificationTaskService) {
            this.telegramBot = telegramBot;
            this.notificationTaskService = notificationTaskService;
        }

        @PostConstruct
        public void init() {
            telegramBot.setUpdatesListener(this);
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
                      _01.01.2022 20:00 Сделать домашнюю работу_
                  """);
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


}
