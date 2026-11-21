package com.gps.os.integration;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Minimal Telegram Bot API client (HTTPS). The spring-boot starter named in the spec registers itself
 * through the Spring Boot 2 spring.factories mechanism, which Spring Boot 3 ignores, so Ops talks to the
 * Bot API directly.
 */
public class TelegramApiClient {
    private final RestClient client;
    private final String token;

    public TelegramApiClient(RestClient.Builder builder, String apiBase, String token) {
        this.client = builder.clone().baseUrl(apiBase).build();
        this.token = token;
    }

    public void sendMessage(String chatId, String text) {
        call("sendMessage", Map.of("chat_id", chatId, "text", text, "disable_web_page_preview", true));
    }

    public void sendPhoto(String chatId, byte[] image, String caption) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("chat_id", chatId);
        if (caption != null && !caption.isBlank()) form.add("caption", caption);
        form.add("photo", new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return "image.png";
            }
        });
        exchange("sendPhoto", () -> client.post().uri("/bot{token}/sendPhoto", token)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().body(JsonNode.class));
    }

    /** Long-polls for updates; returns the "result" array. */
    public JsonNode getUpdates(long offset, int timeoutSeconds) {
        return call("getUpdates", Map.of("offset", offset, "timeout", timeoutSeconds, "allowed_updates", List.of("message")));
    }

    public void setMyCommands(List<Map<String, String>> commands) {
        call("setMyCommands", Map.of("commands", commands));
    }

    private JsonNode call(String method, Object body) {
        return exchange(method, () -> client.post().uri("/bot{token}/{method}", token, method)
                .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class));
    }

    private JsonNode exchange(String method, java.util.function.Supplier<JsonNode> call) {
        try {
            JsonNode res = call.get();
            if (res == null || !res.path("ok").asBoolean(false)) {
                throw new TelegramApiException(200, 0, method + " was not accepted by Telegram");
            }
            return res.path("result");
        } catch (RestClientResponseException e) {
            int retryAfter = 0;
            try {
                retryAfter = e.getResponseBodyAs(JsonNode.class).path("parameters").path("retry_after").asInt(0);
            } catch (Exception ignored) {
                // body was not JSON
            }
            throw new TelegramApiException(e.getStatusCode().value(), retryAfter,
                    method + " failed with HTTP " + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            throw new TelegramApiException(0, 0, method + " failed: Telegram unreachable");
        }
    }
}
