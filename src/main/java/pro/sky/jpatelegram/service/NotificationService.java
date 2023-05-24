package pro.sky.jpatelegram.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.jpatelegram.entity.Notification;
import pro.sky.jpatelegram.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void createNotification(
            Long chatId, LocalDateTime notificationDate, String notificationMessage) {
        Notification notification = new Notification();
        notification.setChatId(chatId);
        notification.setDate(notificationDate.truncatedTo(ChronoUnit.MINUTES));
        notification.setMessage(notificationMessage);
        repository.save(notification);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Notification repository content:");
            repository.findAll().forEach(not -> LOG.trace("Notification={}", not));
            };
        }

    public Lisr<Notification> getAllNotificationsForDate(LocalDateTime date) {
        return repository.findAllByDateEquals(date.truncatedTo(ChronoUnit.MINUTES));
    }
    }


