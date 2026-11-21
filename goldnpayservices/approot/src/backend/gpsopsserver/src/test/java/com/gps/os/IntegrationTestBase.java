package com.gps.os;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Full app on H2 (Core's real schema script) with a GreenMail SMTP server. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    protected static final String KEY = "test-core-to-ops-key";
    private static final int SMTP_PORT = freePort();

    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL =
            new GreenMailExtension(new ServerSetup(SMTP_PORT, "127.0.0.1", ServerSetup.PROTOCOL_SMTP))
                    .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @DynamicPropertySource
    static void mail(DynamicPropertyRegistry r) {
        r.add("spring.mail.port", () -> SMTP_PORT);
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected JdbcTemplate jdbc;

    /** Core owns these tables; Ops tests seed them directly. Start each test from a clean slate. */
    @BeforeEach
    void cleanDatabase() {
        jdbc.update("delete from telegram_links");
        jdbc.update("delete from notification_logs");
        jdbc.update("delete from gold_rates");
        jdbc.update("delete from app_settings");
        jdbc.update("delete from users");
    }

    protected long insertUser(String email, String status, Instant createdAt) {
        jdbc.update("insert into users (email, password_hash, full_name, role, status, kyc_status, failed_login_count, created_at)"
                + " values (?, 'x', ?, 'CLIENT', ?, 'NOT_SUBMITTED', 0, ?)", email, "Priya <S>", status, Timestamp.from(createdAt));
        return jdbc.queryForObject("select id from users where email = ?", Long.class, email);
    }

    protected void insertRate(String r24, String r22, String r18, boolean paused) {
        jdbc.update("insert into gold_rates (rate_24k, rate_22k, rate_18k, currency, effective_at, paused, created_at)"
                        + " values (?, ?, ?, 'INR', ?, ?, ?)", new BigDecimal(r24), new BigDecimal(r22), new BigDecimal(r18),
                Timestamp.from(Instant.now().minusSeconds(60)), paused, Timestamp.from(Instant.now()));
    }

    protected void setting(String key, String value, String scope, boolean isPublic) {
        jdbc.update("insert into app_settings (setting_key, setting_value, scope, is_public, created_at) values (?, ?, ?, ?, ?)",
                key, value, scope, isPublic, Timestamp.from(Instant.now()));
    }

    private static int freePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
