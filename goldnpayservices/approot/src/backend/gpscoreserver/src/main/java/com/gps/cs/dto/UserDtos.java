package com.gps.cs.dto;

import com.gps.cs.entity.KycStatus;
import com.gps.cs.entity.Role;
import com.gps.cs.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class UserDtos {
    private UserDtos() {}

    public record UserResponse(Long id, String email, String fullName, String phone, String address, Role role,
                               UserStatus status, KycStatus kycStatus, Instant createdAt) {}

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 150) String fullName,
            @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits") String phone,
            @Size(max = 500) String address) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Pattern(regexp = AuthDtos.PASSWORD_RULE, message = AuthDtos.PASSWORD_MSG) String newPassword) {}

    public record ChangeRoleRequest(@NotNull Role role) {}

    public record CreateStaffRequest(
            @NotBlank @jakarta.validation.constraints.Email @Size(max = 190) String email,
            @NotBlank @Size(max = 150) String fullName,
            @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits") String phone,
            @NotNull Role role) {}

    public record RoleSummary(Role role, UserStatus status, long count) {}

    public record ClientSummary(long totalClients, long newClientsLast7Days, long activeClients,
                                long pendingActivation, long kycPending) {}
}
