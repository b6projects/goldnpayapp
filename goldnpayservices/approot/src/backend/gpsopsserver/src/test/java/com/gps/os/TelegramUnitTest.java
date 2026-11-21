package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.gps.os.integration.TelegramApiClient;
import com.gps.os.integration.TelegramApiException;
import com.gps.os.service.TelegramPacer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TelegramUnitTest {

    // ---- pacing: 30 messages/s overall, 1 message/s per chat ----

    @Test
    void no_more_than_30_messages_start_within_any_second() {
        AtomicLong now = new AtomicLong(1_000_000);
        TelegramPacer pacer = new TelegramPacer(30, 1000, now::get);
        List<Long> slots = new ArrayList<>();
        for (int i = 0; i < 100; i++) slots.add(now.get() + pacer.reserve("chat-" + i)); // 100 different chats at once
        for (int i = 0; i + 30 < slots.size(); i++) {
            assertThat(slots.get(i + 30) - slots.get(i)).as("31 messages must span at least 1s").isGreaterThanOrEqualTo(1000);
        }
    }

    @Test
    void one_chat_gets_at_most_one_message_per_second() {
        AtomicLong now = new AtomicLong(5_000);
        TelegramPacer pacer = new TelegramPacer(30, 1000, now::get);
        assertThat(pacer.reserve("a")).isZero();
        long second = pacer.reserve("a");
        long third = pacer.reserve("a");
        assertThat(second).isGreaterThanOrEqualTo(1000);
        assertThat(third - second).isGreaterThanOrEqualTo(1000);
        assertThat(pacer.reserve("b")).isLessThan(1000); // other chats are not held back by "a"
    }

    @Test
    void an_idle_pacer_does_not_delay() {
        AtomicLong now = new AtomicLong(0);
        TelegramPacer pacer = new TelegramPacer(30, 1000, now::get);
        pacer.reserve("a");
        now.addAndGet(5000);
        assertThat(pacer.reserve("a")).isZero();
    }

    // ---- API client ----

    private record Rig(TelegramApiClient api, MockRestServiceServer server) {}

    private Rig rig() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        return new Rig(new TelegramApiClient(b, "https://api.telegram.test", "SECRET-TOKEN"), server);
    }

    @Test
    void sendMessage_posts_json_to_the_bot_endpoint() {
        Rig r = rig();
        r.server.expect(requestTo("https://api.telegram.test/botSECRET-TOKEN/sendMessage"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.chat_id").value("42"))
                .andExpect(jsonPath("$.text").value("hello"))
                .andRespond(withSuccess("{\"ok\":true,\"result\":{}}", MediaType.APPLICATION_JSON));
        r.api.sendMessage("42", "hello");
        r.server.verify();
    }

    @Test
    void sendPhoto_uses_multipart() {
        Rig r = rig();
        r.server.expect(requestTo("https://api.telegram.test/botSECRET-TOKEN/sendPhoto"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess("{\"ok\":true,\"result\":{}}", MediaType.APPLICATION_JSON));
        r.api.sendPhoto("@chan", new byte[]{1, 2, 3}, "caption");
        r.server.verify();
    }

    @Test
    void getUpdates_returns_the_result_array() {
        Rig r = rig();
        r.server.expect(requestTo("https://api.telegram.test/botSECRET-TOKEN/getUpdates"))
                .andRespond(withSuccess("{\"ok\":true,\"result\":[{\"update_id\":7}]}", MediaType.APPLICATION_JSON));
        JsonNode res = r.api.getUpdates(0, 25);
        assertThat(res.get(0).get("update_id").asInt()).isEqualTo(7);
    }

    @Test
    void errors_expose_status_and_retry_after_but_never_the_token() {
        Rig r = rig();
        r.server.expect(requestTo("https://api.telegram.test/botSECRET-TOKEN/sendMessage"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":false,\"error_code\":429,\"parameters\":{\"retry_after\":7}}"));
        assertThatThrownBy(() -> r.api.sendMessage("1", "x")).isInstanceOfSatisfying(TelegramApiException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(429);
            assertThat(e.getRetryAfterSeconds()).isEqualTo(7);
            assertThat(e.isRetryable()).isTrue();
            assertThat(e.getMessage()).doesNotContain("SECRET-TOKEN");
        });
    }

    @Test
    void a_forbidden_response_is_permanent() {
        Rig r = rig();
        r.server.expect(requestTo("https://api.telegram.test/botSECRET-TOKEN/sendMessage"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":false,\"description\":\"bot was blocked by the user\"}"));
        assertThatThrownBy(() -> r.api.sendMessage("1", "x")).isInstanceOfSatisfying(TelegramApiException.class,
                e -> assertThat(e.isRetryable()).isFalse());
    }
}
