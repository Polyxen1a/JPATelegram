package pro.sky.jpatelegram.timer;

        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.api.extension.ExtendWith;
        import org.mockito.ArgumentCaptor;
        import org.mockito.InjectMocks;
        import org.mockito.Mock;
        import org.mockito.Mockito;
        import org.mockito.junit.jupiter.MockitoExtension;
        import pro.sky.jpatelegram.entity.NotificationTask;
        import pro.sky.jpatelegram.repository.NotificationTaskRepository;

        import java.time.LocalDateTime;
        import java.util.Collections;

        import static org.mockito.ArgumentMatchers.any;
        import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationTaskTaskTimerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @InjectMocks
    private NotificationTaskTimer notificationTaskTimer;

    @Test
    public void sendingNotificationTest() {
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setMessage("Do smth.");
        notificationTask.setChatId(456);
        notificationTask.setNotificationDateTime(LocalDateTime.now());

        when(notificationTaskRepository.findAllByNotificationDateTime(any()))
                .thenReturn(Collections.singletonList(notificationTask));

        notificationTaskTimer.task();

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertEquals(actual.getParameters().get("chat_id"),
                notificationTask.getChatId());
        Assertions.assertEquals(actual.getParameters().get("text"),
                "Вы просили напомнить о задаче:\n" + notificationTask.getMessage());
    }
}