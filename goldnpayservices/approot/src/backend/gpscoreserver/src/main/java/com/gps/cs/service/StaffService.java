package com.gps.cs.service;

import com.gps.cs.dto.UserDtos.CreateStaffRequest;
import com.gps.cs.dto.UserDtos.UserResponse;
import com.gps.cs.entity.*;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.mapper.UserMapper;
import com.gps.cs.repository.UserRepository;
import com.gps.cs.security.AppPrincipal;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Staff management. ADMIN may manage MANAGER, PARTNER, SALES, MARKETING.
 * MANAGER may manage PARTNER, SALES, MARKETING.
 */
@Service
@RequiredArgsConstructor
public class StaffService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final OpsApiService ops;
    private final EmailContentFactory emails;
    private final AuditService audit;
    private final SecureRandom random = new SecureRandom();

    public static Set<Role> manageableBy(Role actor) {
        return switch (actor) {
            case ADMIN -> Set.of(Role.MANAGER, Role.PARTNER, Role.SALES, Role.MARKETING);
            case MANAGER -> Set.of(Role.PARTNER, Role.SALES, Role.MARKETING);
            default -> Set.of();
        };
    }

    @Transactional
    public UserResponse create(AppPrincipal actor, CreateStaffRequest r) {
        requireCanManage(actor, r.role());
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_TAKEN", "An account with this email already exists");
        }
        byte[] raw = new byte[24];
        random.nextBytes(raw);
        User u = new User();
        u.setEmail(email);
        // Random, never shown: the staff member sets their own password through the OTP reset flow.
        u.setPasswordHash(encoder.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(raw)));
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone());
        u.setRole(r.role());
        u.setStatus(UserStatus.ACTIVE);
        u.setKycStatus(KycStatus.NOT_SUBMITTED);
        users.save(u);
        audit.log("STAFF_CREATED", "User", u.getId(), r.role().name());
        ops.sendEmailAsync(emails.staffWelcome(u, r.role()));
        return UserMapper.toResponse(u);
    }

    @Transactional
    public UserResponse suspend(AppPrincipal actor, Long id) {
        User u = managed(actor, id);
        if (u.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.conflict("INVALID_STATE", "Only active users can be suspended");
        }
        u.setStatus(UserStatus.SUSPENDED);
        users.save(u);
        tokenService.revokeAll(id);
        audit.log("STAFF_SUSPENDED", "User", id, null);
        return UserMapper.toResponse(u);
    }

    @Transactional
    public UserResponse reinstate(AppPrincipal actor, Long id) {
        User u = managed(actor, id);
        if (u.getStatus() != UserStatus.SUSPENDED) {
            throw ApiException.conflict("INVALID_STATE", "Only suspended users can be reinstated");
        }
        u.setStatus(UserStatus.ACTIVE);
        audit.log("STAFF_REINSTATED", "User", id, null);
        return UserMapper.toResponse(users.save(u));
    }

    /** Soft delete: the row stays for audit. */
    @Transactional
    public void delete(AppPrincipal actor, Long id) {
        User u = managed(actor, id);
        u.setStatus(UserStatus.DELETED);
        users.save(u);
        tokenService.revokeAll(id);
        audit.log("STAFF_DELETED", "User", id, null);
    }

    private User managed(AppPrincipal actor, Long id) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
        if (u.getId().equals(actor.id())) throw ApiException.forbidden("You cannot do this to your own account");
        requireCanManage(actor, u.getRole());
        return u;
    }

    private static void requireCanManage(AppPrincipal actor, Role target) {
        if (!manageableBy(actor.role()).contains(target)) {
            throw ApiException.forbidden("Your role cannot manage " + target + " users");
        }
    }
}
