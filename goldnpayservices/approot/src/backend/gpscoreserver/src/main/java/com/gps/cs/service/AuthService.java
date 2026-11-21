package com.gps.cs.service;

import com.gps.cs.config.AppProperties;
import com.gps.cs.dto.AuthDtos.*;
import com.gps.cs.entity.*;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.repository.TokenRepository;
import com.gps.cs.repository.UserRepository;
import com.gps.cs.security.JwtService;
import com.gps.cs.util.Hashing;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final TokenRepository tokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final OpsApiService ops;
    private final EmailContentFactory emails;
    private final AppProperties props;

    @Transactional
    public RegisterResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_TAKEN", "An account with this email already exists");
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone());
        u.setRole(Role.CLIENT);
        u.setStatus(UserStatus.PENDING_ACTIVATION);
        u.setKycStatus(KycStatus.NOT_SUBMITTED);
        users.save(u);
        ops.sendEmailAsync(emails.welcome(u)); // failure must not fail registration
        return new RegisterResponse(u.getId(), u.getEmail(), u.getStatus());
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse login(LoginRequest r) {
        User u = users.findByEmailIgnoreCase(r.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        if (u == null || u.getStatus() == UserStatus.DELETED) {
            encoder.matches(r.password(), "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BWfF1LzYKZ0f6qk0cYV5GkVj3rXe"); // timing
            throw badCredentials();
        }
        Instant now = Instant.now();
        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(now)) {
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Account temporarily locked. Try again later.");
        }
        if (!encoder.matches(r.password(), u.getPasswordHash())) {
            registerFailure(u, now);
            throw badCredentials();
        }
        if (u.getStatus() == UserStatus.SUSPENDED) {
            throw ApiException.forbidden("Account is suspended");
        }
        if (u.getStatus() == UserStatus.PENDING_ACTIVATION) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVATED", "Account is not activated yet");
        }
        u.setFailedLoginCount(0);
        u.setLastFailedLoginAt(null);
        u.setLockedUntil(null);
        users.save(u);
        return tokenService.issuePair(u);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        try {
            Claims c = jwt.parse(refreshToken);
            if (!TokenType.REFRESH.name().equals(c.get("typ", String.class))) throw badRefresh();
            Token stored = tokens.findByTokenHash(Hashing.sha256Hex(refreshToken)).orElseThrow(AuthService::badRefresh);
            if (stored.isRevoked() || stored.isExpired() || stored.getExpiresAt().isBefore(Instant.now())) {
                throw badRefresh();
            }
            User u = users.findById(stored.getUserId()).orElseThrow(AuthService::badRefresh);
            if (u.getStatus() != UserStatus.ACTIVE) throw badRefresh();
            return tokenService.issueAccessOnly(u, refreshToken);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw badRefresh();
        }
    }

    /** Revokes every token of the caller. The user is identified from the session or from a refresh token. */
    @Transactional
    public void logout(Long authenticatedUserId, String refreshToken) {
        Long userId = authenticatedUserId;
        if (userId == null && refreshToken != null && !refreshToken.isBlank()) {
            try {
                userId = jwt.parse(refreshToken).get("uid", Long.class);
            } catch (Exception e) {
                throw ApiException.unauthorized("UNAUTHORIZED", "Authentication required");
            }
        }
        if (userId == null) throw ApiException.unauthorized("UNAUTHORIZED", "Authentication required");
        tokenService.revokeAll(userId);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void resetPassword(PasswordResetRequest r) {
        User u = otpService.consumeResetGrant(r.email(), r.resetToken());
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setFailedLoginCount(0);
        u.setLockedUntil(null);
        users.save(u);
        tokenService.revokeAll(u.getId());
        ops.sendEmailAsync(emails.passwordChanged(u));
    }

    public TokenCheckResponse tokenCheck(String token) {
        try {
            Claims c = jwt.parse(token);
            if (!TokenType.ACCESS.name().equals(c.get("typ", String.class))) return invalidCheck();
            Token stored = tokens.findByTokenHash(Hashing.sha256Hex(token)).orElse(null);
            if (stored == null || stored.isRevoked() || stored.isExpired()) return invalidCheck();
            return users.findById(stored.getUserId())
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                    .map(u -> new TokenCheckResponse(true, u.getId(), u.getEmail(), u.getRole()))
                    .orElseGet(AuthService::invalidCheck);
        } catch (Exception e) {
            return invalidCheck();
        }
    }

    private void registerFailure(User u, Instant now) {
        AppProperties.Lockout l = props.lockout();
        boolean windowExpired = u.getLastFailedLoginAt() == null
                || u.getLastFailedLoginAt().isBefore(now.minus(Duration.ofMinutes(l.windowMinutes())));
        u.setFailedLoginCount(windowExpired ? 1 : u.getFailedLoginCount() + 1);
        u.setLastFailedLoginAt(now);
        if (u.getFailedLoginCount() >= l.maxFailures()) {
            u.setLockedUntil(now.plus(Duration.ofMinutes(l.lockMinutes())));
            u.setFailedLoginCount(0);
            log.warn("Account locked after repeated failures, userId={}", u.getId());
        }
        users.save(u);
    }

    private static TokenCheckResponse invalidCheck() {
        return new TokenCheckResponse(false, null, null, null);
    }

    private static ApiException badCredentials() {
        return ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password");
    }

    private static ApiException badRefresh() {
        return ApiException.unauthorized("INVALID_REFRESH_TOKEN", "Invalid or expired refresh token");
    }
}
