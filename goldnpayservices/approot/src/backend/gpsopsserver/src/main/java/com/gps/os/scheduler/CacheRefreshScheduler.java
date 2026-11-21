package com.gps.os.scheduler;

import com.gps.os.service.AppPublicCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Refreshes the app-public data cache and the Telegram user cache every 15 minutes.
 * Not guarded by ShedLock on purpose: the cache lives in each instance's memory, so every instance must refresh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshScheduler {
    private final AppPublicCache cache;

    @Scheduled(cron = "${app.schedule.cache-cron}", zone = "${app.schedule.zone}")
    public void run() {
        try {
            cache.refresh();
        } catch (RuntimeException e) {
            log.warn("Cache refresh failed: {}", e.getClass().getSimpleName());
        }
    }
}
