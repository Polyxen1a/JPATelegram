package pro.sky.jpatelegram.timer;

import org.springframework.stereotype.Component;
import pro.sky.jpatelegram.service.NotificationService;

import java.time.LocalDateTime;

@Component
public class NotificationTimer {
    private final NotificationService notificationService;

    private final TelegramBot telegramBot;

    public NotificationTimer(NotificationService notificationService, TelegramBot telegramBot) {
        this.notificationService = notificationService;
        this.telegramBot = telegramBot;
    }
    @Sheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        notificationService
                .getAllNotificationsForDate(now)
                .forEach(
                        n -> telegramBot.execute(
                                new SendMessage(
                                        n.getChatId(), String.format("Вы просили напомнить о %s", n.getMessage()))));

                }

}
