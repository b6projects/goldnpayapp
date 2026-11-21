package com.gps.os.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** All settings under "app.*". Secrets arrive as ${ENV_VAR} with no fallback (fail fast). */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String env,
        @Valid Jwt jwt,
        @Valid Security security,
        @Valid Core core,
        @Valid Mail mail,
        @Valid Telegram telegram,
        @Valid Discord discord,
        @Valid Twitter twitter,
        Schedule schedule,
        Reminders reminders) {

    public record Jwt(@NotBlank String secret) {}

    public record Security(@NotBlank String coreToOpsApiKey, @NotBlank String opsToCoreApiKey) {}

    public record Core(@NotBlank String baseUrl, int connectTimeoutMs, int readTimeoutMs) {}

    public record Mail(@NotBlank String from, String replyTo, int maxRecipients, long maxAttachmentBytes,
                       int maxSubjectLength, int retries, long backoffMs, Brand brand) {}

    public record Brand(String name, String siteUrl, String logoUrl, String address, String contact,
                        String unsubscribeUrl) {}

    public record Telegram(@NotBlank String supportBotUsername, @NotBlank String supportBotToken,
                           @NotBlank String prBotUsername, @NotBlank String prBotToken, @NotBlank String prChannelId,
                           String apiBase, boolean pollingEnabled, int globalPerSecond, long perChatIntervalMs,
                           int maxPending) {}

    public record Discord(@NotBlank String webhookUrl) {}

    public record Twitter(@NotBlank String apiKey, @NotBlank String apiSecret, @NotBlank String accessToken,
                          @NotBlank String accessSecret) {}

    public record Schedule(boolean enabled, String zone, String goldrateCron, String inactiveCron, String cacheCron) {}

    public record Reminders(int afterDays, int maxPerUser, int minGapDays, int batchSize) {}
}
