package com.gps.cs.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** All settings under "app.*". Secrets arrive as ${ENV_VAR} with no fallback (fail fast). */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String env,
        @Valid Jwt jwt,
        @Valid Security security,
        @Valid Cors cors,
        @Valid Ops ops,
        @Valid Storage storage,
        Otp otp,
        Lockout lockout,
        Telegram telegram) {

    public record Jwt(@NotBlank String secret, long accessExpirationMs, long refreshExpirationMs) {}

    public record Security(@NotBlank String coreToOpsApiKey, @NotBlank String opsToCoreApiKey,
                           @NotBlank String fieldEncryptionKey, int authRateLimitPerMinute) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Ops(@NotBlank String baseUrl, int connectTimeoutMs, int readTimeoutMs, int retries) {}

    public record Storage(@NotBlank String volumeHome, String kycFolder, String imagesVolume, String hmImagesVolume) {}

    public record Otp(int ttlMinutes, int maxPerHour, int maxAttempts) {}

    public record Lockout(int maxFailures, int windowMinutes, int lockMinutes) {}

    public record Telegram(int linkCodeTtlMinutes) {}
}
