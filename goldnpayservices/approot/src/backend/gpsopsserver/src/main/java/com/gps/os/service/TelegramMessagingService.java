package com.gps.os.service;

import com.gps.os.dto.MessagingDtos.MessageAccepted;
import com.gps.os.dto.MessagingDtos.TgmAppMessage;
import com.gps.os.dto.MessagingDtos.TgmPrMessage;
import com.gps.os.entity.Channel;
import com.gps.os.entity.DeliveryStatus;
import com.gps.os.entity.NotificationLog;
import com.gps.os.entity.TelegramLink;
import com.gps.os.exception.ApiException;
import com.gps.os.integration.TelegramApiClient;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Core -> Telegram messages: one linked user (support bot) or the PR channel (PR bot). Idempotent on requestId. */
@Service
public class TelegramMessagingService {
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final TelegramApiClient supportApi;
    private final TelegramSender sender;
    private final TelegramLinkService links;
    private final TgmPRBotService prBot;
    private final NotificationLogService logs;

    public TelegramMessagingService(@Qualifier("supportBotApi") TelegramApiClient supportApi, TelegramSender sender,
                                    TelegramLinkService links, TgmPRBotService prBot, NotificationLogService logs) {
        this.supportApi = supportApi;
        this.sender = sender;
        this.links = links;
        this.prBot = prBot;
        this.logs = logs;
    }

    public MessageAccepted sendAppMessage(TgmAppMessage m) {
        TelegramLink link = links.byUser(m.userId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "TELEGRAM_NOT_LINKED", "The user has not linked Telegram"));
        var created = logs.createIfAbsent(m.requestId(), Channel.TELEGRAM, "APP_MESSAGE", m.userId(), "user:" + m.userId());
        if (!created.isNew()) return new MessageAccepted(m.requestId(), created.log().getStatus());
        track(created.log(), sender.sendText(supportApi, String.valueOf(link.getChatId()), m.text()));
        return new MessageAccepted(m.requestId(), DeliveryStatus.QUEUED);
    }

    public MessageAccepted sendPrMessage(TgmPrMessage m) {
        byte[] image = decodeImage(m.imageBase64());
        var created = logs.createIfAbsent(m.requestId(), Channel.TELEGRAM, "PR_MESSAGE", null, "pr-channel");
        if (!created.isNew()) return new MessageAccepted(m.requestId(), created.log().getStatus());
        track(created.log(), prBot.post(m.text(), image));
        return new MessageAccepted(m.requestId(), DeliveryStatus.QUEUED);
    }

    /** Decodes an optional Base64 image; null when absent. */
    public static byte[] decodeImage(String base64) {
        if (base64 == null || base64.isBlank()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64.replaceAll("\\s", "")); // strict: no silent skipping
            if (bytes.length == 0) throw new IllegalArgumentException("empty");
            if (bytes.length > MAX_IMAGE_BYTES) {
                throw ApiException.badRequest("IMAGE_TOO_LARGE", "Image exceeds 5 MB");
            }
            return bytes;
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("IMAGE_INVALID", "imageBase64 is not valid Base64");
        }
    }

    /** Updates the log row when the queued send finishes. */
    void track(NotificationLog log, CompletableFuture<Boolean> result) {
        result.whenComplete((ok, ex) -> {
            if (ex == null && Boolean.TRUE.equals(ok)) logs.markSent(log.getId(), 1);
            else logs.markFailed(log.getId(), 1, ex == null ? "Telegram did not accept the message" : ex.getClass().getSimpleName());
        });
    }
}
