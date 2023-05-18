package pro.sky.jpatelegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.jpatelegram.handlers.TelegramHandler;

import java.util.List;

@Component
public class TelegramUpdateListener implements UpdatesListener {
    private static final Logger LOG = LoggerFactory.getLogger(TelegramUpdateListener.class);

    private final TelegramBot telegramBot;

    private final List<TelegramHandler> handlers;

    public TelegramUpdateListener(TelegramBot telegramBot, List<TelegramHandler> handlers) {
        this.telegramBot = telegramBot;
        this.handlers = handlers;
        this.telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.stream().filter(update -> update.message() != null).forEach(this::handleUpdate);
        return CONFIRMED_UPDATES_ALL;
    }

    private void handleUpdate(Update update) {
        TelegramHandler matchedHandler = null;
        int maxWeight = Integer.MIN_VALUE;
        for (TelegramHandler handler : handlers) {
            int weight = handler.getWeight((update));
            if (maxWeight < weight) {
                maxWeight = weight;
                matchedHandler = handler;
            }
        }
        matchedHandler.handleUpdate(update);
    }
}
