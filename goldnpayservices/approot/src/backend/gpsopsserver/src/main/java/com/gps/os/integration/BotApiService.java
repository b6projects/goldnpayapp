package com.gps.os.integration;

import com.gps.os.dto.MessagingDtos.CoreLinkCodeVerifyResponse;
import com.gps.os.dto.MessagingDtos.TokenCheckResponse;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Ops -> Core client for /tc/v1/internal/**, authenticated with the Ops->Core API key. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotApiService {
    private final RestClient coreRestClient;

    /** reachable=false means Core could not be asked; userId is set only for a valid code. */
    public record VerifyResult(boolean reachable, Long userId) {
        public boolean valid() { return reachable && userId != null; }
    }

    public VerifyResult verifyLinkCode(String code) {
        try {
            CoreLinkCodeVerifyResponse r = post("/tc/v1/internal/telegram/link-code/verify", Map.of("code", code),
                    CoreLinkCodeVerifyResponse.class);
            return new VerifyResult(true, r != null && r.valid() ? r.userId() : null);
        } catch (RuntimeException e) {
            log.warn("Core link-code verification failed: {}", e.getClass().getSimpleName());
            return new VerifyResult(false, null);
        }
    }

    public Optional<TokenCheckResponse> tokenCheck(String token) {
        try {
            return Optional.ofNullable(post("/tc/v1/internal/tokencheck", Map.of("token", token), TokenCheckResponse.class));
        } catch (RuntimeException e) {
            log.warn("Core token check failed: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private <T> T post(String path, Object body, Class<T> type) {
        try {
            return coreRestClient.post().uri(path).contentType(MediaType.APPLICATION_JSON)
                    .header("X-Trace-Id", String.valueOf(MDC.get("traceId")))
                    .body(body).retrieve().body(type);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Core returned HTTP " + e.getStatusCode().value()); // body may echo secrets
        }
    }
}
