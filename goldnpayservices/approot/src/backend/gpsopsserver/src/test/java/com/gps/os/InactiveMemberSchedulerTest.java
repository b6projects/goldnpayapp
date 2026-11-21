package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.gps.os.scheduler.InactiveMemberNotificationScheduler;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/** Min gap of 0 so a daily job can be exercised repeatedly; the 3-per-user cap must still hold. */
@TestPropertySource(properties = "app.reminders.min-gap-days=0")
class InactiveMemberSchedulerTest extends IntegrationTestBase {
    @Autowired InactiveMemberNotificationScheduler job;

    @BeforeEach
    void emptyMailbox() throws Exception {
        GREEN_MAIL.purgeEmailFromAllMailboxes();
    }

    private int mailsTo(String address) throws Exception {
        return (int) Arrays.stream(GREEN_MAIL.getReceivedMessages()).filter(m -> {
            try {
                return m.getAllRecipients()[0].toString().equals(address);
            } catch (Exception e) {
                return false;
            }
        }).count();
    }

    @Test
    void sends_activation_reminders_only_to_stale_pending_users() throws Exception {
        Instant old = Instant.now().minus(Duration.ofDays(3));
        insertUser("stale@example.com", "PENDING_ACTIVATION", old);
        insertUser("fresh@example.com", "PENDING_ACTIVATION", Instant.now().minus(Duration.ofHours(5)));
        insertUser("active@example.com", "ACTIVE", old);
        insertUser("suspended@example.com", "SUSPENDED", old);

        assertThat(job.sendReminders()).isEqualTo(1);
        await().atMost(Duration.ofSeconds(10)).until(() -> mailsTo("stale@example.com") == 1);
        assertThat(mailsTo("fresh@example.com")).isZero();
        assertThat(mailsTo("active@example.com")).isZero();
        assertThat(mailsTo("suspended@example.com")).isZero();

        MimeMessage m = GREEN_MAIL.getReceivedMessages()[0];
        assertThat(m.getSubject()).contains("activating");
        assertThat(GREEN_MAIL.getReceivedMessages()[0].getContent().toString()).doesNotContainIgnoringCase("wallet");

        // the reminder is logged
        assertThat(jdbc.queryForObject(
                "select count(*) from notification_logs where category = 'ACTIVATION_REMINDER' and channel = 'EMAIL'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void never_more_than_three_reminders_per_user() throws Exception {
        long id = insertUser("cap@example.com", "PENDING_ACTIVATION", Instant.now().minus(Duration.ofDays(10)));
        for (int i = 0; i < 6; i++) job.sendReminders();
        await().atMost(Duration.ofSeconds(10)).until(() -> mailsTo("cap@example.com") >= 3);
        Thread.sleep(500);
        assertThat(mailsTo("cap@example.com")).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from notification_logs where user_id = ? and category = 'ACTIVATION_REMINDER'",
                Integer.class, id)).isEqualTo(3);
    }

    @Test
    void html_in_a_name_is_escaped_in_the_email() throws Exception {
        insertUser("escape@example.com", "PENDING_ACTIVATION", Instant.now().minus(Duration.ofDays(5))); // name is "Priya <S>"
        job.sendReminders();
        await().atMost(Duration.ofSeconds(10)).until(() -> mailsTo("escape@example.com") == 1);
        String raw = com.icegreen.greenmail.util.GreenMailUtil.getWholeMessage(GREEN_MAIL.getReceivedMessages()[0]);
        // the HTML part carries the escaped form; the raw tag must never appear as markup in it
        assertThat(raw).contains("&lt;S&gt;");
    }
}
