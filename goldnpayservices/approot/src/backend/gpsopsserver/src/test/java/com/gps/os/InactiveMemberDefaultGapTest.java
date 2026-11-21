package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;

import com.gps.os.scheduler.InactiveMemberNotificationScheduler;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** With the default settings, reminders are spaced apart: a second run on the same day sends nothing. */
class InactiveMemberDefaultGapTest extends IntegrationTestBase {
    @Autowired InactiveMemberNotificationScheduler job;

    @Test
    void a_second_run_the_same_day_does_not_remind_again() {
        insertUser("gap@example.com", "PENDING_ACTIVATION", Instant.now().minus(Duration.ofDays(4)));
        assertThat(job.sendReminders()).isEqualTo(1);
        assertThat(job.sendReminders()).isZero();
    }
}
