package com.gps.os.scheduler;

import com.gps.os.dto.MessagingDtos.LiveRate;
import com.gps.os.dto.MessagingDtos.SocialPostRequest;
import com.gps.os.dto.MessagingDtos.SocialPostResult;
import com.gps.os.service.AppPublicCache;
import com.gps.os.service.SocialPublishService;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Publishes the live rate, with an overlay image, to the enabled social channels at 09:30 and 15:30 IST.
 * Skipped while the rate is paused. The request id is per hour, so a second instance or a manual re-run in
 * the same hour cannot post twice even without the lock.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoldRatePublishScheduler {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter SLOT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final AppPublicCache cache;
    private final SocialPublishService social;

    @Scheduled(cron = "${app.schedule.goldrate-cron}", zone = "${app.schedule.zone}")
    @SchedulerLock(name = "goldRatePublish", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void run() {
        publishNow();
    }

    /** Returns the per-channel results, or null when nothing was published (paused or no rate). */
    public SocialPostResult publishNow() {
        LiveRate rate = cache.liveRateFromDb();
        if (!"LIVE".equals(rate.status())) {
            log.info("Gold rate post skipped: rate is {}", rate.status());
            return null;
        }
        ZonedDateTime now = ZonedDateTime.now(IST).truncatedTo(ChronoUnit.HOURS);
        String text = "Gold rate update - " + WHEN.format(ZonedDateTime.now(IST)) + " IST\n"
                + "24K: " + money(rate.rate24k()) + "/g\n22K: " + money(rate.rate22k()) + "/g\n18K: "
                + money(rate.rate18k()) + "/g";
        SocialPostResult result = social.publish(new SocialPostRequest("goldrate-" + SLOT.format(now), text, null,
                List.of(SocialPublishService.TELEGRAM_PR, "DISCORD", "TWITTER"), true));
        log.info("Gold rate post results: {}", result.results());
        return result;
    }

    private static String money(BigDecimal v) {
        return String.format(Locale.ENGLISH, "INR %,.2f", v);
    }
}
