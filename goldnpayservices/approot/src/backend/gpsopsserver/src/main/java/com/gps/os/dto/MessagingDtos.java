package com.gps.os.dto;

import com.gps.os.entity.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Telegram, social, token-check and cache DTOs. */
public final class MessagingDtos {
    private MessagingDtos() {}

    public record TgmAppMessage(@NotBlank @Size(max = 50) String requestId, @NotNull Long userId,
                                @NotBlank @Size(max = 4096) String text) {}

    public record TgmPrMessage(@NotBlank @Size(max = 50) String requestId, @NotBlank @Size(max = 4096) String text,
                               String imageBase64) {}

    public record MessageAccepted(String requestId, DeliveryStatus status) {}

    public record TgmAuthenticateRequest(@NotBlank String code, @NotNull Long chatId, @Size(max = 100) String username) {}

    public record TgmAuthenticateResponse(boolean linked, Long userId) {}

    public record TgmDisconnectRequest(@NotNull Long userId) {}

    public record TgmDisconnectResponse(boolean disconnected) {}

    public record SocialPostRequest(@NotBlank @Size(max = 50) String requestId, @NotBlank @Size(max = 4000) String text,
                                    String imageBase64, @NotEmpty List<String> channels, boolean overlayRate) {}

    /** results: channel -> QUEUED | SENT | FAILED | SKIPPED_DISABLED. */
    public record SocialPostResult(String requestId, Map<String, String> results) {}

    public record TokenCheckRequest(@NotBlank String token) {}

    public record TokenCheckResponse(boolean valid, Long userId, String email, String role) {}

    // Core's contract for /tc/v1/internal/**
    public record CoreLinkCodeVerifyResponse(boolean valid, Long userId) {}

    public record LiveRate(String status, BigDecimal rate24k, BigDecimal rate22k, BigDecimal rate18k, String currency,
                           Instant effectiveAt) {}

    public record PublicCacheResponse(LiveRate goldRate, Map<String, String> appSettings, Instant refreshedAt) {}
}
