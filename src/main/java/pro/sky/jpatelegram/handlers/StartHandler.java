package pro.sky.jpatelegram.handlers;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)

public class StartHandler extends AbstractMessagingHandler {

    public StartHandler(TelegramBot telegramBot) {
        super(telegramBot);
    }

    @Override
    public int getWeight(Update update) {
        int weight = 0;
        if (update.message().text() != null) {
            weight += 1;
        }
        if (update.message().text() != null && update.message().text().equals("/start")) {
            weight += 2;
        }

        @Override
        public void handleUpdate(Update update) {
            telegramBot.execute(
                    new SendMessage(
                            update.message().chat().id(), "Привет " + update.message().from().firstName()));
        }
    }
}
