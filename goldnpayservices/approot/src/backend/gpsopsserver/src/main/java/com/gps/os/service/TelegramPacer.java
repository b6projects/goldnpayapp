package com.gps.os.service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.LongSupplier;

/**
 * Hands out send times so Telegram's limits are respected: at most {@code globalPerSecond} messages per
 * second overall and one message per {@code perChatIntervalMs} per chat.
 * <p>
 * Any two reserved times are at least 1000/globalPerSecond ms apart, which caps every one-second window at
 * {@code globalPerSecond} messages. A busy chat only delays its own messages: other chats slot in between.
 */
public class TelegramPacer {
    private final long gapMs;
    private final long perChatMs;
    private final LongSupplier clock;
    private final Map<String, Long> lastPerChat = new HashMap<>();
    private final TreeSet<Long> reserved = new TreeSet<>();

    public TelegramPacer(int globalPerSecond, long perChatIntervalMs, LongSupplier clockMillis) {
        this.gapMs = (long) Math.ceil(1000.0 / globalPerSecond);
        this.perChatMs = perChatIntervalMs;
        this.clock = clockMillis;
    }

    /** Reserves the next send time for the chat; returns how many ms from now the message may be sent. */
    public synchronized long reserve(String chatKey) {
        long now = clock.getAsLong();
        long t = now;
        Long last = lastPerChat.get(chatKey);
        if (last != null) t = Math.max(t, last + perChatMs);

        boolean moved = true;
        while (moved) { // push t forward until it is far enough from every reserved time
            moved = false;
            Long before = reserved.floor(t);
            if (before != null && t - before < gapMs) {
                t = before + gapMs;
                moved = true;
            }
            Long after = reserved.ceiling(t);
            if (after != null && after - t < gapMs) {
                t = after + gapMs;
                moved = true;
            }
        }
        reserved.add(t);
        lastPerChat.put(chatKey, t);

        reserved.headSet(now - gapMs).clear();
        if (lastPerChat.size() > 5000) lastPerChat.values().removeIf(v -> v < now - 60_000);
        return t - now;
    }
}
