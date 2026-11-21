package com.gps.os.scheduler;

import com.gps.os.config.AppProperties;
import com.gps.os.entity.DeliveryStatus;
import com.gps.os.entity.User;
import com.gps.os.repository.NotificationLogRepository;
import com.gps.os.repository.UserRepository;
import com.gps.os.service.EmailService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Daily account-activation reminders by email for users who stayed PENDING_ACTIVATION for more than
 * 2 days: at most 3 per user, at least {@code min-gap-days} apart, each one logged in NotificationLog.
 * Activation reminders only: there are no wallet reminders (D8).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveMemberNotificationScheduler {
    static final String CATEGORY = "ACTIVATION_REMINDER";

    private final UserRepository users;
    private final NotificationLogRepository logs;
    private final EmailService emails;
    private final AppProperties props;

    @Scheduled(cron = "${app.schedule.inactive-cron}", zone = "${app.schedule.zone}")
    @SchedulerLock(name = "inactiveMemberReminders", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void run() {
        int sent = sendReminders();
        log.info("Activation reminders queued: {}", sent);
    }

    /** Returns how many reminders were queued. */
    public int sendReminders() {
        AppProperties.Reminders cfg = props.reminders();
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofDays(cfg.afterDays()));
        int queued = 0;
        for (int page = 0; ; page++) {
            List<User> batch = users.findByStatusAndCreatedAtBeforeOrderByIdAsc("PENDING_ACTIVATION", cutoff,
                    PageRequest.of(page, cfg.batchSize()));
            if (batch.isEmpty()) break;
            for (User u : batch) {
                try {
                    if (remind(u, now, cfg)) queued++;
                } catch (RuntimeException e) {
                    log.warn("Reminder for user {} failed: {}", u.getId(), e.getClass().getSimpleName());
                }
            }
        }
        return queued;
    }

    private boolean remind(User u, Instant now, AppProperties.Reminders cfg) {
        long active = logs.countByUserIdAndCategoryAndStatusIn(u.getId(), CATEGORY,
                List.of(DeliveryStatus.QUEUED, DeliveryStatus.SENT));
        if (active >= cfg.maxPerUser()) return false;
        var last = logs.findFirstByUserIdAndCategoryOrderByIdDesc(u.getId(), CATEGORY);
        if (last.isPresent() && last.get().getStatus() != DeliveryStatus.FAILED
                && last.get().getCreatedAt().isAfter(now.minus(Duration.ofDays(cfg.minGapDays())))) {
            return false;
        }
        long attemptNo = logs.countByUserIdAndCategory(u.getId(), CATEGORY) + 1; // unique request id, even after failures
        String body = "<p>Hi " + HtmlUtils.htmlEscape(u.getFullName()) + ",</p>"
                + "<p>Your GoldNPay account is not activated yet. Open GoldNPay, choose <strong>Verify email</strong> "
                + "and enter the code we send you to finish setting up.</p>"
                + "<p>If you did not create this account, you can ignore this email.</p>";
        emails.sendInternal("activation-reminder-" + u.getId() + "-" + attemptNo, "STANDARD", u.getId(), u.getEmail(),
                "Finish activating your GoldNPay account", body, CATEGORY);
        return true;
    }
}
