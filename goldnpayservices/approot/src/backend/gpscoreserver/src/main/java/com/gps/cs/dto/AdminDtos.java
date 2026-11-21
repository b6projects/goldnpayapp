package com.gps.cs.dto;

import com.gps.cs.entity.MediaKind;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Gold rate, settings, media, social and report DTOs. */
public final class AdminDtos {
    private AdminDtos() {}

    public record GoldRateRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal rate24k,
            @NotNull @DecimalMin(value = "0.01") BigDecimal rate22k,
            @NotNull @DecimalMin(value = "0.01") BigDecimal rate18k,
            Instant effectiveAt) {}

    public record GoldRateResponse(Long id, BigDecimal rate24k, BigDecimal rate22k, BigDecimal rate18k,
                                   String currency, Instant effectiveAt, boolean paused, String source) {}

    /** status is LIVE, PAUSED or NO_RATE. Rates are omitted while paused. */
    public record LiveRateResponse(String status, BigDecimal rate24k, BigDecimal rate22k, BigDecimal rate18k,
                                   String currency, Instant effectiveAt) {}

    public record SettingsRequest(@NotNull @Size(max = 100) Map<String, String> settings) {}

    public record MediaImageResponse(Long id, MediaKind kind, String title, int sortOrder, boolean active, String url) {}

    public record SocialPostRequest(@NotNull @Size(min = 1, max = 4000) String text, String imageBase64,
                                    @NotEmpty List<String> channels) {}

    public record PublicReportResponse(long registeredClients, long activeClients, long kycApprovedClients) {}

    /** Placeholder until the business module adds payments (D3). */
    public record PaymentReportRow(Long id) {}
}
