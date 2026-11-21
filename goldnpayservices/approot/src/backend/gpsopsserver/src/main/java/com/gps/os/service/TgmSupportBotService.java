package com.gps.os.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gps.os.config.AppProperties;
import com.gps.os.dto.MessagingDtos.LiveRate;
import com.gps.os.exception.ApiException;
import com.gps.os.integration.BotApiService;
import com.gps.os.integration.TelegramApiClient;
import com.gps.os.integration.TelegramApiException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * The support bot. Commands: /start CODE, /help, /rate, /disconnect.
 * It long-polls Telegram on its own thread (no inbound webhook is needed) and never logs message text,
 * because a /start message carries a link code.
 */
@Slf4j
@Service
public class TgmSupportBotService implements SmartLifecycle {
    public enum LinkOutcome { LINKED, INVALID_CODE, CORE_UNAVAILABLE }

    public record LinkResult(LinkOutcome outcome, Long userId) {}

    private final TelegramApiClient api;
    private final TelegramSender sender;
    private final BotApiService core;
    private final TelegramLinkService links;
    private final BotAuthorizationService authorization;
    private final AppPublicCache cache;
    private final boolean pollingEnabled;
    private final boolean enabled;

    private volatile boolean running;
    private Thread poller;

    public TgmSupportBotService(@Qualifier("supportBotApi") TelegramApiClient api, TelegramSender sender,
                                BotApiService core, TelegramLinkService links,
                                BotAuthorizationService authorization, AppPublicCache cache, AppProperties props) {
        this.api = api;
        this.sender = sender;
        this.core = core;
        this.links = links;
        this.authorization = authorization;
        this.cache = cache;
        this.pollingEnabled = props.telegram().pollingEnabled();
        this.enabled = props.telegram().enabled();
    }

    // ---- linking (used by /start and by POST /cs/v1/bauth/tgmauthenticate) ----

    public LinkResult linkChat(String code, long chatId, String username) {
        if (!enabled) {
            log.warn("Telegram link skipped: the Telegram integration is disabled (app.telegram.enabled=false)");
            throw new ApiException(HttpStatus.CONFLICT, "TELEGRAM_DISABLED", "The Telegram integration is disabled");
        }
        BotApiService.VerifyResult v = core.verifyLinkCode(code.trim());
        if (!v.reachable()) return new LinkResult(LinkOutcome.CORE_UNAVAILABLE, null);
        if (!v.valid()) return new LinkResult(LinkOutcome.INVALID_CODE, null);
        links.link(v.userId(), chatId, username);
        say(chatId, "Your Telegram is now linked to your GoldNPay account. Send /help to see what I can do.");
        return new LinkResult(LinkOutcome.LINKED, v.userId());
    }

    /** Removes the link for a user and tells the chat. Returns false when there was no link. */
    public boolean unlinkUser(Long userId) {
        Optional<Long> chat = links.unlinkUser(userId);
        if (!enabled) { // the link is still removed, only the notice to the chat is skipped
            if (chat.isPresent()) {
                log.warn("Telegram disconnect notice skipped: the Telegram integration is disabled (app.telegram.enabled=false)");
            }
            return chat.isPresent();
        }
        chat.ifPresent(c -> say(c, "You have been disconnected from GoldNPay. Send /start CODE to link again."));
        return chat.isPresent();
    }

    // ---- update handling ----

    public void handleUpdate(JsonNode update) {
        JsonNode msg = update.path("message");
        if (msg.isMissingNode() || !"private".equals(msg.path("chat").path("type").asText())) return;
        if (!msg.hasNonNull("text") || !msg.path("chat").has("id")) return;
        long chatId = msg.path("chat").path("id").asLong();
        String username = msg.path("from").path("username").asText(null);
        String[] parts = msg.path("text").asText().trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        int at = command.indexOf('@');
        if (at > 0) command = command.substring(0, at);
        String arg = parts.length > 1 ? parts[1].trim() : "";

        switch (command) {
            case "/start" -> start(chatId, username, arg);
            case "/help" -> say(chatId, HELP);
            case "/rate" -> say(chatId, rateText());
            case "/disconnect" -> disconnect(chatId);
            default -> say(chatId, "I did not understand that. Send /help to see the commands.");
        }
    }

    private static final String HELP = """
            GoldNPay support bot

            /start CODE - link this chat to your GoldNPay account (get the code in the app)
            /rate - today's gold rate
            /disconnect - unlink this chat
            /help - show this message""";

    private void start(long chatId, String username, String code) {
        if (code.isEmpty()) {
            say(chatId, "Welcome to GoldNPay. To link your account, open the GoldNPay app, create a Telegram link "
                    + "code, then send: /start YOURCODE");
            return;
        }
        switch (linkChat(code, chatId, username).outcome()) {
            case LINKED -> { /* confirmation already sent */ }
            case INVALID_CODE -> say(chatId, "That code is invalid or has expired. Create a new one in the GoldNPay app.");
            case CORE_UNAVAILABLE -> say(chatId, "I could not check the code right now. Please try again shortly.");
        }
    }

    private void disconnect(long chatId) {
        Optional<Long> userId = authorization.authorize(chatId);
        if (userId.isEmpty()) {
            say(chatId, "This chat is not linked to an active GoldNPay account.");
            return;
        }
        links.unlinkChat(chatId);
        say(chatId, "You have been disconnected from GoldNPay.");
    }

    private String rateText() {
        LiveRate r = cache.snapshot().goldRate();
        if (r == null || "NO_RATE".equals(r.status())) return "No gold rate has been published yet.";
        if ("PAUSED".equals(r.status())) return "Gold rates are paused right now. Please check back later.";
        return String.format(Locale.ENGLISH, "Gold rate (per gram, %s)%n24K: %,.2f%n22K: %,.2f%n18K: %,.2f",
                r.currency(), r.rate24k(), r.rate22k(), r.rate18k());
    }

    private CompletableFuture<Boolean> say(long chatId, String text) {
        return sender.sendText(api, String.valueOf(chatId), text);
    }

    // ---- long polling ----

    @Override
    public void start() {
        if (!enabled) {
            log.warn("Telegram support bot not started: the Telegram integration is disabled (app.telegram.enabled=false)");
            return;
        }
        if (!pollingEnabled || running) return;
        running = true;
        poller = new Thread(this::pollLoop, "telegram-support-poller");
        poller.setDaemon(true);
        poller.start();
    }

    private void pollLoop() {
        long offset = 0;
        long backoffMs = 2000;
        while (running) {
            try {
                JsonNode updates = api.getUpdates(offset, 25);
                backoffMs = 2000;
                for (JsonNode u : updates) {
                    offset = Math.max(offset, u.path("update_id").asLong() + 1);
                    try {
                        handleUpdate(u);
                    } catch (RuntimeException e) {
                        log.warn("Support bot could not handle an update: {}", e.getClass().getSimpleName());
                    }
                }
            } catch (TelegramApiException e) {
                log.warn("Support bot polling failed: {}", e.getMessage());
                sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 60_000);
            } catch (RuntimeException e) {
                log.warn("Support bot polling error: {}", e.getClass().getSimpleName());
                sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 60_000);
            }
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        running = false;
        if (poller != null) poller.interrupt();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
