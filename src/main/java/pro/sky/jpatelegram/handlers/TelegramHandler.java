package pro.sky.jpatelegram.handlers;

public interface TelegramHandler {

    int getWeight(Update update);

    void handleUpdate(Update update);
}
