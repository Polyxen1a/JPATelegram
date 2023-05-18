package pro.sky.jpatelegram.handlers;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ImageHandler extends AbstractMessagingHandler {

    public ImageHandler(TelegramBot telegramBot) {
        super(telegramBot);

    }

    @Override
    public int getWeight(Update update) {
        int weight = 0;
        if (update.message().photo() != null) {
            weight += 1;
        }
        if (update.message().photo() != null && update.message().photo().length > 0) {
            weight += 2;
        }
        return weight;

    }

    @Override
    public void handleUpdate(Update update) {
        telegramBot.execute(new SendMessage(update.message().chat().id(), "вы прислали картинку"));
    }
}
