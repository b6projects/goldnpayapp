package com.gps.os.service;

import com.gps.os.config.AppProperties;
import com.gps.os.integration.TelegramApiClient;
import com.gps.os.integration.TelegramApiException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

/**
 * Queues outgoing Telegram messages and sends them at the allowed pace (30/s overall, 1/s per chat).
 * Rate-limited (429) and transient failures are retried; permanent failures complete the future with false.
 */
@Slf4j
@Service
public class TelegramSender implements DisposableBean {
    private static final int MAX_ATTEMPTS = 3;
    private static final int CAPTION_LIMIT = 1024;

    private final TelegramPacer pacer;
    private final int maxPending;
    private final AtomicInteger pending = new AtomicInteger();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, r -> {
        Thread t = new Thread(r, "telegram-sender");
        t.setDaemon(true);
        return t;
    });

    @org.springframework.beans.factory.annotation.Autowired
    public TelegramSender(AppProperties props) {
        this(new TelegramPacer(props.telegram().globalPerSecond(), props.telegram().perChatIntervalMs(),
                System::currentTimeMillis), props.telegram().maxPending());
    }

    TelegramSender(TelegramPacer pacer, int maxPending) {
        this.pacer = pacer;
        this.maxPending = maxPending;
    }

    @FunctionalInterface
    private interface Action {
        void run() throws TelegramApiException;
    }

    public CompletableFuture<Boolean> sendText(TelegramApiClient bot, String chatId, String text) {
        return enqueue(chatId, () -> bot.sendMessage(chatId, text));
    }

    /** Sends an image with the text as caption; a long text goes as a second message. */
    public CompletableFuture<Boolean> sendPhoto(TelegramApiClient bot, String chatId, byte[] image, String text) {
        if (text == null || text.length() <= CAPTION_LIMIT) {
            return enqueue(chatId, () -> bot.sendPhoto(chatId, image, text));
        }
        return enqueue(chatId, () -> bot.sendPhoto(chatId, image, null))
                .thenCompose(ok -> ok ? sendText(bot, chatId, text) : CompletableFuture.completedFuture(false));
    }

    private CompletableFuture<Boolean> enqueue(String chatId, Action action) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        if (pending.incrementAndGet() > maxPending) {
            pending.decrementAndGet();
            log.warn("Telegram queue is full; message dropped");
            result.complete(false);
            return result;
        }
        schedule(chatId, action, result, 1, 0);
        return result;
    }

    private void schedule(String chatId, Action action, CompletableFuture<Boolean> result, int attempt, long extraDelayMs) {
        long delay = Math.max(pacer.reserve(chatId), extraDelayMs);
        executor.schedule(() -> {
            try {
                action.run();
                finish(result, true);
            } catch (TelegramApiException e) {
                if (e.isRetryable() && attempt < MAX_ATTEMPTS) {
                    long wait = e.getStatus() == 429 && e.getRetryAfterSeconds() > 0
                            ? e.getRetryAfterSeconds() * 1000L : 1000L * attempt;
                    log.warn("Telegram send retry {}/{}: {}", attempt, MAX_ATTEMPTS, e.getMessage());
                    schedule(chatId, action, result, attempt + 1, wait);
                } else {
                    log.warn("Telegram send failed: {}", e.getMessage());
                    finish(result, false);
                }
            } catch (RuntimeException e) {
                log.warn("Telegram send failed: {}", e.getClass().getSimpleName());
                finish(result, false);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void finish(CompletableFuture<Boolean> result, boolean ok) {
        pending.decrementAndGet();
        result.complete(ok);
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
