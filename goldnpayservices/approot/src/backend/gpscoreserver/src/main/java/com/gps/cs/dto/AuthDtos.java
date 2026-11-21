package com.gps.cs.dto;

import com.gps.cs.entity.OtpPurpose;
import com.gps.cs.entity.Role;
import com.gps.cs.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public static final String PASSWORD_RULE = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    public static final String PASSWORD_MSG = "must be 8-72 characters with at least one letter and one digit";

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MSG) String password,
            @NotBlank @Size(max = 150) String fullName,
            @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits") String phone) {}

    public record RegisterResponse(Long id, String email, UserStatus status) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds,
                                Role role) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record LogoutRequest(String refreshToken) {}

    public record OtpRequest(@NotBlank @Email String email, @NotNull OtpPurpose purpose) {}

    public record OtpVerifyRequest(@NotBlank @Email String email, @NotNull OtpPurpose purpose,
                                   @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must be 6 digits") String code) {}

    /** resetToken is only present for PASSWORD_RESET. */
    public record OtpVerifyResponse(boolean verified, String resetToken) {}

    public record PasswordResetRequest(@NotBlank @Email String email, @NotBlank String resetToken,
                                       @NotBlank @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MSG) String newPassword) {}

    public record TokenCheckRequest(@NotBlank String token) {}

    public record TokenCheckResponse(boolean valid, Long userId, String email, Role role) {}

    public record MessageResponse(String message) {}
}
