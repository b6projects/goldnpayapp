package com.gps.cs.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.gps.cs.config.AppProperties;
import com.gps.cs.dto.OpsDtos.*;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Core -> Ops client. Core never sends email or messages itself.
 * Timeouts are set on the RestClient; 5xx responses and timeouts are retried with back-off.
 * Request bodies are never logged (they can contain OTPs).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsApiService {
    private final RestClient opsRestClient;
    private final AppProperties props;

    /** Synchronous send; returns false on failure instead of throwing. */
    public boolean sendEmail(EmailRequest request) {
        return post("/cs/v1/email/send", request, "email " + request.category()).isPresent();
    }

    /** Fire-and-forget variant for emails that must not slow down or fail the user's action. */
    @Async
    public void sendEmailAsync(EmailRequest request) {
        sendEmail(request);
    }

    public boolean sendTelegramAppMessage(TgmAppMessage message) {
        return post("/cs/v1/tgm/tgmappmessage", message, "telegram app message").isPresent();
    }

    public boolean sendTelegramPrMessage(TgmPrMessage message) {
        return post("/cs/v1/tgm/tgmprmessage", message, "telegram PR message").isPresent();
    }

    /** Returns Ops' per-channel result, or empty on failure. */
    public Optional<JsonNode> publishSocialPost(SocialPostRequest request) {
        return post("/cs/v1/social/post", request, "social post");
    }

    @Async
    public void publishSocialPostAsync(SocialPostRequest request) {
        publishSocialPost(request);
    }

    public boolean unlinkTelegram(Long userId) {
        return post("/cs/v1/bauth/tgmdisconnect", java.util.Map.of("userId", userId), "telegram unlink").isPresent();
    }

    private Optional<JsonNode> post(String path, Object body, String what) {
        int attempts = props.ops().retries() + 1;
        for (int i = 1; i <= attempts; i++) {
            try {
                JsonNode res = opsRestClient.post().uri(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Trace-Id", String.valueOf(MDC.get("traceId")))
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
                return Optional.ofNullable(res).or(() -> Optional.of(com.fasterxml.jackson.databind.node.NullNode.getInstance()));
            } catch (HttpServerErrorException | ResourceAccessException e) {
                log.warn("Ops call failed ({}), attempt {}/{}: {}", what, i, attempts, e.getClass().getSimpleName());
                if (i < attempts) sleep(300L * i);
            } catch (Exception e) {
                log.warn("Ops call rejected ({}): {}", what, e.getClass().getSimpleName());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
