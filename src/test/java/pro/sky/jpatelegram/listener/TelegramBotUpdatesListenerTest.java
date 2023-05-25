package pro.sky.jpatelegram.listener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import pro.sky.jpatelegram.service.NotificationTaskService;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskService notificationTaskService;

    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdateListener;

    @Test
    public void initTest() {
        telegramBotUpdateListener.init();
    }

    @Test
    public void handleStartTest() throws URISyntaxException, IOException {
        checkResponse("/start", """
                  Привет!
                                
                  Чтобы запланировать задачу, отправь сообщение в следующем формате:
                      _01.01.2022 20:00 Сделать домашнюю работу_
                  """);
    }

    @Test
    public void handleIncorrectInputDateTimeFormatTest() throws URISyntaxException, IOException {
        checkResponse("18.05.2023 25:44 Do smth.", "Некорректный формат даты/времени!");
    }

    @Test
    public void handleIncorrectInputFormatTest() throws URISyntaxException, IOException {
        checkResponse("18.05.2023-12:44 Do smth.", "Некорректный формат сообщения!");
    }

    @Test
    public void handleCorrectInputFormatTest() throws URISyntaxException, IOException {
        checkResponse("18.05.2023 12:44 Do smth.", "Задача запланирована!");
    }

    private void checkResponse(String input, String expectedOutput) throws URISyntaxException, IOException {
        String json = Files.readString(
                Path.of(TelegramBotUpdatesListenerTest.class.getResource("update.json").toURI()));
        Update updateWithIncorrectDateTime = BotUtils.fromJson(
                json.replace("%text%", input), Update.class);
        SendResponse sendResponse = BotUtils.fromJson("""
        {
          "ok": true
        }
        """, SendResponse.class);

        when(telegramBot.execute(any())).thenReturn(sendResponse);

        telegramBotUpdateListener.process(Collections.singletonList(updateWithIncorrectDateTime));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertEquals(actual.getParameters().get("chat_id"),
                updateWithIncorrectDateTime.message().chat().id());
        Assertions.assertEquals(actual.getParameters().get("text"),
                expectedOutput);
    }

}