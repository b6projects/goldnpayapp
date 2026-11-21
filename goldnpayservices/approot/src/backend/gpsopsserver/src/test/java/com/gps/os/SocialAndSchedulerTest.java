package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gps.os.dto.MessagingDtos.SocialPostResult;
import com.gps.os.integration.TelegramApiClient;
import com.gps.os.scheduler.CacheRefreshScheduler;
import com.gps.os.scheduler.GoldRatePublishScheduler;
import com.gps.os.service.DiscordPublisher;
import com.gps.os.service.TwitterPublisher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

class SocialAndSchedulerTest extends IntegrationTestBase {
    @MockitoBean DiscordPublisher discord;
    @MockitoBean TwitterPublisher twitter;
    @MockitoBean(name = "supportBotApi") TelegramApiClient support;
    @MockitoBean(name = "prBotApi") TelegramApiClient pr;

    @Autowired GoldRatePublishScheduler goldRateJob;
    @Autowired CacheRefreshScheduler cacheJob;

    @BeforeEach
    void mocks() {
        reset(discord, twitter, support, pr);
        when(discord.channel()).thenReturn("DISCORD");
        when(twitter.channel()).thenReturn("TWITTER");
    }

    private ResultActions social(Map<String, Object> body) throws Exception {
        return mvc.perform(post("/cs/v1/social/post").header("X-API-KEY", KEY).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)));
    }

    private Map<String, Object> req(String... channels) {
        return new java.util.LinkedHashMap<>(Map.of("requestId", UUID.randomUUID().toString(), "text", "Gold rate update",
                "channels", List.of(channels)));
    }

    private void enable(String... keys) {
        for (String k : keys) setting(k, "true", "SOCIAL", false);
    }

    // ---- /cs/v1/social/post ----

    @Test
    void publishes_only_to_enabled_channels_and_reports_each_one() throws Exception {
        enable("discord.enabled", "telegram_pr.enabled"); // twitter stays off
        social(req("TELEGRAM_PR", "DISCORD", "TWITTER")).andExpect(status().isOk())
                .andExpect(jsonPath("$.results.DISCORD").value("SENT"))
                .andExpect(jsonPath("$.results.TELEGRAM_PR").value("QUEUED"))
                .andExpect(jsonPath("$.results.TWITTER").value("SKIPPED_DISABLED"));
        verify(discord).publish("Gold rate update", null);
        verify(twitter, never()).publish(anyString(), any());
        verify(pr, timeout(5000)).sendMessage("@gnp_channel", "Gold rate update");
    }

    @Test
    void one_failing_channel_does_not_block_the_others() throws Exception {
        enable("discord.enabled", "twitter.enabled");
        doThrow(new IllegalStateException("Twitter rejected the post: HTTP 403")).when(twitter).publish(anyString(), any());
        social(req("DISCORD", "TWITTER")).andExpect(status().isOk())
                .andExpect(jsonPath("$.results.DISCORD").value("SENT"))
                .andExpect(jsonPath("$.results.TWITTER").value("FAILED"));
        assertThat(jdbc.queryForObject("select status from notification_logs where channel = 'TWITTER'", String.class))
                .isEqualTo("FAILED");
        assertThat(jdbc.queryForObject("select error from notification_logs where channel = 'TWITTER'", String.class))
                .contains("403");
    }

    @Test
    void a_repeated_request_does_not_post_twice() throws Exception {
        enable("discord.enabled");
        Map<String, Object> body = req("DISCORD");
        social(body).andExpect(jsonPath("$.results.DISCORD").value("SENT"));
        social(body).andExpect(status().isOk()).andExpect(jsonPath("$.results.DISCORD").value("SENT"));
        verify(discord, times(1)).publish(anyString(), any());
    }

    @Test
    void unknown_channel_is_rejected() throws Exception {
        social(req("FACEBOOK")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_CHANNEL"));
        social(new java.util.LinkedHashMap<>(Map.of("requestId", "r", "text", "t", "channels", List.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void overlay_draws_the_live_rate_onto_a_png() throws Exception {
        enable("discord.enabled");
        insertRate("7500.50", "6875.00", "5625.00", false);
        Map<String, Object> body = req("DISCORD");
        body.put("overlayRate", true);
        social(body).andExpect(jsonPath("$.results.DISCORD").value("SENT"));
        ArgumentCaptor<byte[]> png = ArgumentCaptor.forClass(byte[].class);
        verify(discord).publish(eq("Gold rate update"), png.capture());
        assertThat(png.getValue()).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');
    }

    @Test
    void overlay_is_skipped_while_the_rate_is_paused() throws Exception {
        enable("discord.enabled");
        insertRate("7500.50", "6875.00", "5625.00", true);
        Map<String, Object> body = req("DISCORD");
        body.put("overlayRate", true);
        social(body).andExpect(jsonPath("$.results.DISCORD").value("SKIPPED_RATE_PAUSED"));
        verify(discord, never()).publish(anyString(), any());
    }

    // ---- GoldRatePublishScheduler ----

    @Test
    void the_gold_rate_job_posts_the_live_rate_with_an_image_to_enabled_channels() {
        enable("discord.enabled", "twitter.enabled");
        insertRate("7500.50", "6875.00", "5625.00", false);
        SocialPostResult res = goldRateJob.publishNow();
        assertThat(res.results()).containsEntry("DISCORD", "SENT").containsEntry("TWITTER", "SENT")
                .containsEntry("TELEGRAM_PR", "SKIPPED_DISABLED");
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> img = ArgumentCaptor.forClass(byte[].class);
        verify(discord).publish(text.capture(), img.capture());
        assertThat(text.getValue()).contains("7,500.50").contains("24K");
        assertThat(img.getValue()).isNotEmpty();
    }

    @Test
    void the_gold_rate_job_is_skipped_while_paused_or_without_a_rate() {
        enable("discord.enabled");
        assertThat(goldRateJob.publishNow()).isNull(); // no rate yet
        insertRate("7500.50", "6875.00", "5625.00", true);
        assertThat(goldRateJob.publishNow()).isNull(); // paused
        verifyNoInteractions(discord);
    }

    @Test
    void running_the_gold_rate_job_twice_in_one_slot_posts_once() {
        enable("discord.enabled");
        insertRate("7500.50", "6875.00", "5625.00", false);
        goldRateJob.publishNow();
        goldRateJob.publishNow();
        verify(discord, times(1)).publish(anyString(), any());
    }

    // ---- CacheRefreshScheduler / public-cache ----

    @Test
    void the_cache_job_refreshes_rate_settings_and_the_public_cache_endpoint() throws Exception {
        mvc.perform(get("/cs/v1/public-cache").header("X-API-KEY", KEY)).andExpect(status().isOk());
        insertRate("7500.50", "6875.00", "5625.00", false);
        setting("support.phone", "+911234567890", "APP", true);
        setting("internal.flag", "x", "SYSTEM", false);
        cacheJob.run();
        mvc.perform(get("/cs/v1/public-cache").header("X-API-KEY", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goldRate.status").value("LIVE"))
                .andExpect(jsonPath("$.goldRate.rate24k").value(7500.50))
                .andExpect(jsonPath("$.appSettings['support.phone']").value("+911234567890"))
                .andExpect(jsonPath("$.appSettings['internal.flag']").doesNotExist())
                .andExpect(jsonPath("$.refreshedAt").isNotEmpty());
        mvc.perform(get("/cs/v1/public-cache")).andExpect(status().isUnauthorized());
    }
}
