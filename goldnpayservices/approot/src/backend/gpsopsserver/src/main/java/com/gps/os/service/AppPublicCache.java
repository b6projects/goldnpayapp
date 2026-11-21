package com.gps.os.service;

import com.gps.os.dto.MessagingDtos.LiveRate;
import com.gps.os.dto.MessagingDtos.PublicCacheResponse;
import com.gps.os.entity.TelegramLink;
import com.gps.os.repository.AppSettingRepository;
import com.gps.os.repository.GoldRateRepository;
import com.gps.os.repository.TelegramLinkRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * In-memory copy of the public data the bots need (live rate, public app settings) and of the
 * Telegram chat -> user mapping (the "Telegram user cache"). Refreshed every 15 minutes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppPublicCache {
    private final GoldRateRepository rates;
    private final AppSettingRepository settings;
    private final TelegramLinkRepository links;

    private volatile PublicCacheResponse snapshot =
            new PublicCacheResponse(new LiveRate("NO_RATE", null, null, null, "INR", null), Map.of(), null);
    private final ConcurrentHashMap<Long, Long> chatToUser = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        try {
            refresh();
        } catch (RuntimeException e) {
            log.warn("Initial cache load failed: {}", e.getClass().getSimpleName());
        }
    }

    @Transactional(readOnly = true)
    public void refresh() {
        LiveRate rate = liveRateFromDb();
        Map<String, String> app = new TreeMap<>();
        settings.findByScopeAndPublicSettingTrueOrderByKeyAsc("APP").forEach(s -> app.put(s.getKey(), s.getValue()));
        snapshot = new PublicCacheResponse(rate, app, Instant.now());

        chatToUser.clear();
        for (TelegramLink l : links.findAll()) chatToUser.put(l.getChatId(), l.getUserId());
    }

    /** Reads the live rate straight from the database (used by schedulers that must not act on stale data). */
    @Transactional(readOnly = true)
    public LiveRate liveRateFromDb() {
        return rates.findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDescIdDesc(Instant.now())
                .map(g -> g.isPaused()
                        ? new LiveRate("PAUSED", null, null, null, g.getCurrency(), g.getEffectiveAt())
                        : new LiveRate("LIVE", g.getRate24k(), g.getRate22k(), g.getRate18k(), g.getCurrency(), g.getEffectiveAt()))
                .orElse(new LiveRate("NO_RATE", null, null, null, "INR", null));
    }

    public PublicCacheResponse snapshot() {
        return snapshot;
    }

    public Optional<Long> userForChat(long chatId) {
        return Optional.ofNullable(chatToUser.get(chatId));
    }

    public void putLink(long chatId, long userId) {
        chatToUser.put(chatId, userId);
    }

    public void removeChat(long chatId) {
        chatToUser.remove(chatId);
    }
}
