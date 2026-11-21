package com.gps.os.dto;

import com.gps.os.entity.DeliveryStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class EmailDtos {
    private EmailDtos() {}

    public record Attachment(@NotBlank @Size(max = 200) @Pattern(regexp = "^[^\\r\\n/\\\\]+$", message = "invalid file name") String fileName,
                             @NotBlank @Size(max = 100) String contentType,
                             @NotBlank String base64) {}

    /** content is the body HTML only. Layout, branding, header and footer belong to Ops (D1). */
    public record EmailRequest(
            @NotBlank @Size(max = 50) String requestId,
            @Size(max = 30) String templateCode,
            @NotEmpty(message = "must contain at least one address") List<@NotBlank @Email String> to,
            List<@NotBlank @Email String> cc,
            List<@NotBlank @Email String> bcc,
            @NotBlank @Size(max = 200) @Pattern(regexp = "^[^\\r\\n]*$", message = "must be a single line") String subject,
            @NotBlank @Size(max = 1_000_000) String content,
            @Valid List<Attachment> attachments,
            @Size(max = 40) String category,
            Long userId) {}

    public record EmailAccepted(String notificationId, DeliveryStatus status) {}

    public record EmailStatus(String notificationId, DeliveryStatus status, int attempts, String error) {}
}
