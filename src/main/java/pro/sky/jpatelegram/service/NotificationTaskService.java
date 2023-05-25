package pro.sky.jpatelegram.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.jpatelegram.entity.NotificationTask;
import pro.sky.jpatelegram.repository.NotificationTaskRepository;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    @Autowired
    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void save(NotificationTask notificationTask) {
        notificationTaskRepository.save(notificationTask);
    }
}


