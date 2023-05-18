package pro.sky.jpatelegram.handlers;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class EchoHandler extends AbstractMessagingHandler {

    public EchoHandler(TelegramBot telegramBot) {
        super(telegramBot);

    }

    @Override
    public int getWeight(Update update) {
        int weight = 0;
        if (update.message().text() != null) {
            weight += 1;
        }
        if (!update.message().text() != null && !update.message().text().isBlank()) {
            weight += 1;
        }
        return weight;
    }

    @Override
    public void handleUpdate(Update update) {
        telegramBot.execute(new SendMessage(update.message().chat().id(), update.message().text()));
    }
}
