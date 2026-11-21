package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gps.os.integration.BotApiService;
import com.gps.os.integration.BotApiService.VerifyResult;
import com.gps.os.integration.TelegramApiClient;
import com.gps.os.repository.TelegramLinkRepository;
import com.gps.os.service.BotAuthorizationService;
import com.gps.os.service.TgmSupportBotService;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

class TelegramFlowTest extends IntegrationTestBase {
    @MockitoBean(name = "supportBotApi") TelegramApiClient support;
    @MockitoBean(name = "prBotApi") TelegramApiClient pr;
    @MockitoBean BotApiService core;

    @Autowired TelegramLinkRepository links;
    @Autowired TgmSupportBotService bot;
    @Autowired BotAuthorizationService authorization;
    @Autowired com.gps.os.service.AppPublicCache cache;

    @BeforeEach
    void resetMocks() {
        reset(support, pr, core);
    }

    private ResultActions call(String path, Object body) throws Exception {
        return mvc.perform(post(path).header("X-API-KEY", KEY).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)));
    }

    private long chatId() {
        return 100_000 + (long) (Math.random() * 1_000_000);
    }

    private long linkedUser(long chat) throws Exception {
        long uid = insertUser("tg-" + UUID.randomUUID() + "@example.com", "ACTIVE", Instant.now());
        when(core.verifyLinkCode("ABCD2345")).thenReturn(new VerifyResult(true, uid));
        call("/cs/v1/bauth/tgmauthenticate", Map.of("code", "ABCD2345", "chatId", chat, "username", "priya")).andExpect(status().isOk());
        return uid;
    }

    @Test
    void a_link_code_from_core_links_the_chat_and_disconnect_removes_it() throws Exception {
        long chat = chatId();
        long uid = insertUser("link@example.com", "ACTIVE", Instant.now());
        when(core.verifyLinkCode("ABCD2345")).thenReturn(new VerifyResult(true, uid));

        call("/cs/v1/bauth/tgmauthenticate", Map.of("code", "ABCD2345", "chatId", chat, "username", "priya"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.linked").value(true))
                .andExpect(jsonPath("$.userId").value(uid));
        var link = links.findByUserId(uid).orElseThrow();
        assertThat(link.getChatId()).isEqualTo(chat);
        assertThat(link.getUsername()).isEqualTo("priya");
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat)), contains("linked"));

        call("/cs/v1/bauth/tgmdisconnect", Map.of("userId", uid))
                .andExpect(status().isOk()).andExpect(jsonPath("$.disconnected").value(true));
        assertThat(links.findByUserId(uid)).isEmpty();
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat)), contains("disconnected"));

        call("/cs/v1/bauth/tgmdisconnect", Map.of("userId", uid)).andExpect(jsonPath("$.disconnected").value(false));
    }

    @Test
    void invalid_code_and_core_outage_are_reported() throws Exception {
        when(core.verifyLinkCode("BADCODE1")).thenReturn(new VerifyResult(true, null));
        call("/cs/v1/bauth/tgmauthenticate", Map.of("code", "BADCODE1", "chatId", chatId()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("LINK_CODE_INVALID"));
        when(core.verifyLinkCode("DOWNDOWN")).thenReturn(new VerifyResult(false, null));
        call("/cs/v1/bauth/tgmauthenticate", Map.of("code", "DOWNDOWN", "chatId", chatId()))
                .andExpect(status().isServiceUnavailable());
        assertThat(links.count()).isZero();
    }

    @Test
    void relinking_moves_the_user_to_the_new_chat() throws Exception {
        long uid = insertUser("move@example.com", "ACTIVE", Instant.now());
        when(core.verifyLinkCode("ABCD2345")).thenReturn(new VerifyResult(true, uid));
        long first = chatId();
        long second = first + 7;
        call("/cs/v1/bauth/tgmauthenticate", Map.of("code", "ABCD2345", "chatId", first)).andExpect(status().isOk());
        call("/cs/v1/bauth/tgmauthenticate", Map.of("code", "ABCD2345", "chatId", second)).andExpect(status().isOk());
        assertThat(links.count()).isEqualTo(1);
        assertThat(links.findByUserId(uid).orElseThrow().getChatId()).isEqualTo(second);
    }

    @Test
    void app_message_reaches_the_linked_user_once_even_if_the_request_repeats() throws Exception {
        long chat = chatId();
        long uid = linkedUser(chat);
        String rid = UUID.randomUUID().toString();
        Map<String, Object> body = Map.of("requestId", rid, "userId", uid, "text", "Your KYC has been approved.");
        call("/cs/v1/tgm/tgmappmessage", body).andExpect(status().isAccepted()).andExpect(jsonPath("$.status").value("QUEUED"));
        call("/cs/v1/tgm/tgmappmessage", body).andExpect(status().isAccepted());
        verify(support, timeout(5000)).sendMessage(String.valueOf(chat), "Your KYC has been approved.");
        Thread.sleep(300);
        verify(support, times(1)).sendMessage(String.valueOf(chat), "Your KYC has been approved.");
    }

    @Test
    void app_message_for_an_unlinked_user_is_404() throws Exception {
        long uid = insertUser("nolink@example.com", "ACTIVE", Instant.now());
        call("/cs/v1/tgm/tgmappmessage", Map.of("requestId", UUID.randomUUID().toString(), "userId", uid, "text", "hi"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("TELEGRAM_NOT_LINKED"));
    }

    @Test
    void pr_message_goes_to_the_pr_channel_with_or_without_an_image() throws Exception {
        call("/cs/v1/tgm/tgmprmessage", Map.of("requestId", UUID.randomUUID().toString(), "text", "Diwali offer"))
                .andExpect(status().isAccepted());
        verify(pr, timeout(5000)).sendMessage("@gnp_channel", "Diwali offer");

        byte[] img = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        call("/cs/v1/tgm/tgmprmessage", Map.of("requestId", UUID.randomUUID().toString(), "text", "With picture",
                "imageBase64", Base64.getEncoder().encodeToString(img))).andExpect(status().isAccepted());
        verify(pr, timeout(5000)).sendPhoto(eq("@gnp_channel"), eq(img), eq("With picture"));
        verify(support, never()).sendMessage(anyString(), anyString()); // PR goes through the PR bot only

        call("/cs/v1/tgm/tgmprmessage", Map.of("requestId", UUID.randomUUID().toString(), "text", "x", "imageBase64", "***"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void telegram_endpoints_need_the_api_key() throws Exception {
        mvc.perform(post("/cs/v1/tgm/tgmprmessage").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/cs/v1/bauth/tgmdisconnect").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ---- the support bot's own commands ----

    private com.fasterxml.jackson.databind.JsonNode update(long chat, String text) throws Exception {
        return json.readTree(json.writeValueAsString(Map.of("update_id", 1, "message",
                Map.of("text", text, "chat", Map.of("id", chat, "type", "private"), "from", Map.of("username", "priya")))));
    }

    @Test
    void start_command_links_the_chat() throws Exception {
        long chat = chatId();
        long uid = insertUser("startcmd@example.com", "ACTIVE", Instant.now());
        when(core.verifyLinkCode("ABCD2345")).thenReturn(new VerifyResult(true, uid));
        bot.handleUpdate(update(chat, "/start ABCD2345"));
        assertThat(links.findByUserId(uid)).isPresent();
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat)), contains("linked"));
    }

    @Test
    void start_with_a_bad_code_or_no_code_explains_what_to_do() throws Exception {
        long chat = chatId();
        when(core.verifyLinkCode("WRONG123")).thenReturn(new VerifyResult(true, null));
        bot.handleUpdate(update(chat, "/start@gnp_support_bot WRONG123"));
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat)), contains("invalid or has expired"));
        long other = chat + 1;
        bot.handleUpdate(update(other, "/start"));
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(other)), contains("link code"));
    }

    @Test
    void rate_command_shows_the_cached_live_rate_and_respects_a_pause() throws Exception {
        insertRate("7500.50", "6875.00", "5625.00", false);
        cache.refresh();
        long chat = chatId();
        bot.handleUpdate(update(chat, "/rate"));
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat)), contains("7,500.50"));

        jdbc.update("update gold_rates set paused = true");
        cache.refresh();
        long chat2 = chat + 1;
        bot.handleUpdate(update(chat2, "/rate"));
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat2)), contains("paused"));
    }

    @Test
    void disconnect_command_needs_a_linked_active_account() throws Exception {
        long chat = chatId();
        bot.handleUpdate(update(chat, "/disconnect"));
        verify(support, timeout(5000)).sendMessage(eq(String.valueOf(chat)), contains("not linked"));

        long linkedChat = chat + 10;
        long uid = linkedUser(linkedChat);
        bot.handleUpdate(update(linkedChat, "/disconnect"));
        assertThat(links.findByUserId(uid)).isEmpty();
    }

    @Test
    void group_chats_and_non_text_updates_are_ignored() throws Exception {
        bot.handleUpdate(json.readTree("{\"update_id\":1,\"message\":{\"text\":\"/help\",\"chat\":{\"id\":5,\"type\":\"group\"}}}"));
        bot.handleUpdate(json.readTree("{\"update_id\":2,\"message\":{\"chat\":{\"id\":5,\"type\":\"private\"}}}"));
        bot.handleUpdate(json.readTree("{\"update_id\":3}"));
        Thread.sleep(200);
        verify(support, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void authorization_requires_a_link_to_an_active_account() throws Exception {
        long chat = chatId();
        long uid = linkedUser(chat);
        assertThat(authorization.authorize(chat)).contains(uid);
        assertThat(authorization.authorize(chat + 1)).isEmpty(); // unknown chat
        jdbc.update("update users set status = 'SUSPENDED' where id = ?", uid);
        assertThat(authorization.authorize(chat)).isEmpty();      // suspended account
    }
}
