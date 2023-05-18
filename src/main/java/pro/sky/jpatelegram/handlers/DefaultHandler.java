package pro.sky.jpatelegram.handlers;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.sky.jpatelegram.bot.TelegramUpdateListener;

@Component

public class DefaultHandler extends AbstractMessagingHandler {

    public DefaultHandler(TelegramBot telegramBot) {
        super(telegramBot);
    }
    @Override
    public  int getWeight (Update update) {
        return 1;
    }

    @Override
    public void handleUpdate(Update update) {
        telegramBot.execute(
                new SendMessage(update.message().chat.id(), "Я не понимаю чего вы хотите"));
    }
}
