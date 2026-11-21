package com.gps.os;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gps.os.integration.TelegramApiClient;
import com.gps.os.service.DiscordPublisher;
import com.gps.os.service.TwitterPublisher;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

/** app.telegram.enabled=false and app.twitter.enabled=false: those channels are skipped, Discord still works. */
@TestPropertySource(properties = {"app.telegram.enabled=false", "app.twitter.enabled=false"})
class SocialDisabledFlagTest extends IntegrationTestBase {
    @MockitoBean DiscordPublisher discord;
    @MockitoBean TwitterPublisher twitter;
    @MockitoBean(name = "supportBotApi") TelegramApiClient support;
    @MockitoBean(name = "prBotApi") TelegramApiClient pr;

    @BeforeEach
    void mocks() {
        reset(discord, twitter, support, pr);
        when(discord.channel()).thenReturn("DISCORD");
        when(twitter.channel()).thenReturn("TWITTER");
    }

    private ResultActions social(String... channels) throws Exception {
        Map<String, Object> body = Map.of("requestId", UUID.randomUUID().toString(), "text", "Gold rate update",
                "channels", List.of(channels));
        return mvc.perform(post("/cs/v1/social/post").header("X-API-KEY", KEY).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)));
    }

    @Test
    void a_disabled_flag_skips_the_channel_even_when_the_database_setting_is_on() throws Exception {
        setting("discord.enabled", "true", "SOCIAL", false);
        setting("twitter.enabled", "true", "SOCIAL", false);
        setting("telegram_pr.enabled", "true", "SOCIAL", false);
        social("TELEGRAM_PR", "DISCORD", "TWITTER").andExpect(status().isOk())
                .andExpect(jsonPath("$.results.DISCORD").value("SENT"))
                .andExpect(jsonPath("$.results.TWITTER").value("SKIPPED_DISABLED"))
                .andExpect(jsonPath("$.results.TELEGRAM_PR").value("SKIPPED_DISABLED"));
        verify(discord).publish("Gold rate update", null);
        verify(twitter, never()).publish(anyString(), any());
        verifyNoInteractions(pr);
        // skipped requests leave no delivery log row
        org.assertj.core.api.Assertions.assertThat(
                jdbc.queryForObject("select count(*) from notification_logs where channel in ('TWITTER','TELEGRAM')",
                        Integer.class)).isZero();
    }

    @Test
    void telegram_requests_from_core_are_refused_without_a_retryable_error() throws Exception {
        mvc.perform(post("/cs/v1/tgm/tgmprmessage").header("X-API-KEY", KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"r-1\",\"text\":\"hello\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.errorCode").value("TELEGRAM_DISABLED"));
        mvc.perform(post("/cs/v1/tgm/tgmappmessage").header("X-API-KEY", KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"r-2\",\"userId\":1,\"text\":\"hello\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.errorCode").value("TELEGRAM_DISABLED"));
        verifyNoInteractions(support, pr);
    }
}
