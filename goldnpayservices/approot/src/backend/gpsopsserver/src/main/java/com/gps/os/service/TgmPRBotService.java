package com.gps.os.service;

import com.gps.os.config.AppProperties;
import com.gps.os.integration.TelegramApiClient;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** The PR bot: posts announcements to the PR channel. */
@Service
public class TgmPRBotService {
    private final TelegramApiClient api;
    private final TelegramSender sender;
    private final String channelId;

    public TgmPRBotService(@Qualifier("prBotApi") TelegramApiClient api, TelegramSender sender, AppProperties props) {
        this.api = api;
        this.sender = sender;
        this.channelId = props.telegram().prChannelId();
    }

    /** Queues the post; the future completes with true once Telegram accepted it. */
    public CompletableFuture<Boolean> post(String text, byte[] image) {
        return image == null || image.length == 0
                ? sender.sendText(api, channelId, text)
                : sender.sendPhoto(api, channelId, image, text);
    }
}
