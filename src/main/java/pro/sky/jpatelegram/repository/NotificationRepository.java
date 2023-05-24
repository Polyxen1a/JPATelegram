package pro.sky.jpatelegram.repository;

import org.springframework.stereotype.Repository;
import pro.sky.jpatelegram.entity.Notification;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByDateEquals(LocalDateTime date);

}
