package com.gps.cs.controller.auth;

import com.gps.cs.dto.AuthDtos.*;
import com.gps.cs.security.AppPrincipal;
import com.gps.cs.service.AuthService;
import com.gps.cs.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/tc/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthService auth;
    private final OtpService otp;

    @Operation(summary = "Client self-registration; the account starts PENDING_ACTIVATION")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    @Operation(summary = "Log in; returns an access token and a refresh token")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @Operation(summary = "Issue a new access token from a valid refresh token")
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest r) {
        return auth.refresh(r.refreshToken());
    }

    @Operation(summary = "Revoke all of the caller's tokens")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AppPrincipal caller,
                                       @RequestBody(required = false) LogoutRequest body) {
        auth.logout(caller == null ? null : caller.id(), body == null ? null : body.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate an OTP and email it through Ops")
    @PostMapping("/otp/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageResponse requestOtp(@Valid @RequestBody OtpRequest r) {
        otp.request(r.email(), r.purpose());
        return new MessageResponse("If the account exists, a code has been sent");
    }

    @Operation(summary = "Verify an OTP (activation or password reset)")
    @PostMapping("/otp/verify")
    public OtpVerifyResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest r) {
        return otp.verify(r.email(), r.purpose(), r.code());
    }

    @Operation(summary = "Reset the password after OTP verification")
    @PostMapping("/password/reset")
    public MessageResponse resetPassword(@Valid @RequestBody PasswordResetRequest r) {
        auth.resetPassword(r);
        return new MessageResponse("Password updated");
    }
}
