package com.gps.cs.dto;

import com.gps.cs.entity.KycDocType;
import com.gps.cs.entity.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/** Client self-service DTOs: KYC, bank accounts, Telegram. */
public final class ClientDtos {
    private ClientDtos() {}

    public record KycDocumentResponse(Long id, KycDocType docType, String contentType, long sizeBytes,
                                      KycStatus status, String rejectReason, Instant uploadedAt) {}

    public record KycStatusResponse(KycStatus status, List<KycDocumentResponse> documents) {}

    public record KycReviewRequest(boolean approve, @Size(max = 500) String reason) {}

    public record BankAccountRequest(
            @NotBlank @Size(max = 150) String accountHolder,
            @NotBlank @Pattern(regexp = "^[0-9]{9,18}$", message = "must be 9-18 digits") String accountNumber,
            @NotBlank @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "invalid IFSC") String ifsc,
            @NotBlank @Size(max = 150) String bankName) {}

    /** accountNumberMasked shows the last 4 digits only. */
    public record BankAccountResponse(Long id, String accountHolder, String accountNumberMasked, String ifsc,
                                      String bankName, boolean primary) {}

    public record LinkCodeResponse(String code, Instant expiresAt) {}

    public record TelegramStatusResponse(boolean linked, String username, Instant linkedAt) {}

    public record TelegramUserResponse(Long userId, String fullName, String email, String username, Instant linkedAt) {}

    // internal (Ops -> Core)
    public record LinkCodeVerifyRequest(@NotBlank String code) {}

    public record LinkCodeVerifyResponse(boolean valid, Long userId) {}
}
