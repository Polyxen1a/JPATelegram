package pro.sky.jpatelegram.handlers;

public abstract class AbstractMessagingHandler implements TelegramHander {
    protected TelegramBot telegramBot;

    public AbstractMessagingHandler(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
}
