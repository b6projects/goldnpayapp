package com.gps.cs.service;

import com.gps.cs.config.AppProperties;
import com.gps.cs.dto.AuthDtos.OtpVerifyResponse;
import com.gps.cs.entity.*;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.repository.OtpRepository;
import com.gps.cs.repository.UserRepository;
import com.gps.cs.util.Hashing;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 6-digit OTPs: valid 5 minutes, single use, stored hashed, at most 5 requests per user per hour.
 * Responses never reveal whether an email is registered.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private static final int RESET_GRANT_MINUTES = 10;

    private final OtpRepository otps;
    private final UserRepository users;
    private final OpsApiService ops;
    private final EmailContentFactory emails;
    private final AppProperties props;
    private final SecureRandom random = new SecureRandom();

    @Transactional(noRollbackFor = ApiException.class)
    public void request(String email, OtpPurpose purpose) {
        requireClientPurpose(purpose);
        User user = users.findByEmailIgnoreCase(norm(email)).orElse(null);
        if (user == null || user.getStatus() == UserStatus.DELETED || user.getStatus() == UserStatus.SUSPENDED) {
            return; // silent: do not reveal account state
        }
        if (purpose == OtpPurpose.ACTIVATION && user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            return;
        }
        Instant now = Instant.now();
        long recent = otps.countByUserIdAndPurposeInAndCreatedAtAfter(user.getId(),
                List.of(OtpPurpose.ACTIVATION, OtpPurpose.PASSWORD_RESET), now.minus(Duration.ofHours(1)));
        if (recent >= props.otp().maxPerHour()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_RATE_LIMITED",
                    "Too many code requests. Try again later.");
        }
        otps.findByUserIdAndPurposeAndUsedAtIsNull(user.getId(), purpose).forEach(o -> {
            o.setUsedAt(now);
            otps.save(o);
        });
        String code = String.format("%06d", random.nextInt(1_000_000));
        Otp otp = new Otp();
        otp.setUserId(user.getId());
        otp.setPurpose(purpose);
        otp.setCodeHash(hash(user.getId(), purpose, code));
        otp.setExpiresAt(now.plus(Duration.ofMinutes(props.otp().ttlMinutes())));
        otps.save(otp);

        String label = purpose == OtpPurpose.ACTIVATION ? "account activation" : "password reset";
        if (!ops.sendEmail(emails.otp(user, label, code, props.otp().ttlMinutes()))) {
            otp.setUsedAt(now); // the code never reached the user
            otps.save(otp);
            throw ApiException.unavailable("OTP_DELIVERY_FAILED", "Could not send the verification code. Try again shortly.");
        }
    }

    @Transactional(noRollbackFor = ApiException.class)
    public OtpVerifyResponse verify(String email, OtpPurpose purpose, String code) {
        requireClientPurpose(purpose);
        User user = users.findByEmailIgnoreCase(norm(email)).orElseThrow(OtpService::invalid);
        Otp otp = otps.findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByIdDesc(user.getId(), purpose)
                .orElseThrow(OtpService::invalid);
        Instant now = Instant.now();
        if (otp.getExpiresAt().isBefore(now) || otp.getAttempts() >= props.otp().maxAttempts()) {
            throw invalid();
        }
        if (!Hashing.constantTimeEquals(otp.getCodeHash(), hash(user.getId(), purpose, code))) {
            otp.setAttempts(otp.getAttempts() + 1);
            otps.save(otp);
            throw invalid();
        }
        otp.setUsedAt(now);
        otps.save(otp);

        if (purpose == OtpPurpose.ACTIVATION) {
            if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
                user.setStatus(UserStatus.ACTIVE);
                users.save(user);
            }
            return new OtpVerifyResponse(true, null);
        }
        // PASSWORD_RESET: hand back a short-lived grant that /password/reset consumes
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String grant = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Otp g = new Otp();
        g.setUserId(user.getId());
        g.setPurpose(OtpPurpose.PASSWORD_RESET_GRANT);
        g.setCodeHash(hash(user.getId(), OtpPurpose.PASSWORD_RESET_GRANT, grant));
        g.setExpiresAt(now.plus(Duration.ofMinutes(RESET_GRANT_MINUTES)));
        otps.save(g);
        return new OtpVerifyResponse(true, grant);
    }

    /** Consumes a reset grant. Throws if it is wrong, used or expired. */
    @Transactional(noRollbackFor = ApiException.class)
    public User consumeResetGrant(String email, String grant) {
        User user = users.findByEmailIgnoreCase(norm(email)).orElseThrow(OtpService::invalid);
        Otp g = otps.findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByIdDesc(user.getId(), OtpPurpose.PASSWORD_RESET_GRANT)
                .orElseThrow(OtpService::invalid);
        if (g.getExpiresAt().isBefore(Instant.now())
                || !Hashing.constantTimeEquals(g.getCodeHash(), hash(user.getId(), OtpPurpose.PASSWORD_RESET_GRANT, grant))) {
            throw invalid();
        }
        g.setUsedAt(Instant.now());
        otps.save(g);
        return user;
    }

    private static void requireClientPurpose(OtpPurpose p) {
        if (p == OtpPurpose.PASSWORD_RESET_GRANT) {
            throw ApiException.badRequest("INVALID_PURPOSE", "Unsupported purpose");
        }
    }

    private static ApiException invalid() {
        return ApiException.badRequest("OTP_INVALID", "Invalid or expired code");
    }

    private static String norm(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hash(Long userId, OtpPurpose purpose, String secret) {
        return Hashing.sha256Hex(userId + ":" + purpose.name() + ":" + secret);
    }
}
