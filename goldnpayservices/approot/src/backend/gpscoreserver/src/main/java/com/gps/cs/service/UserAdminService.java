package com.gps.cs.service;

import com.gps.cs.dto.UserDtos.*;
import com.gps.cs.entity.*;
import com.gps.cs.exception.ApiException;
import com.gps.cs.mapper.UserMapper;
import com.gps.cs.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository users;
    private final TokenService tokenService;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Role role, UserStatus status, KycStatus kyc, Instant from, Instant to, String q,
                                   Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (role != null) p.add(cb.equal(root.get("role"), role));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (kyc != null) p.add(cb.equal(root.get("kycStatus"), kyc));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) p.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT).replace("%", "").replace("_", "") + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("phone")), like)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return users.findAll(spec, pageable).map(UserMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserMapper.toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> pendingActivation() {
        return users.findByStatusOrderByCreatedAtAsc(UserStatus.PENDING_ACTIVATION).stream()
                .map(UserMapper::toResponse).toList();
    }

    @Transactional
    public UserResponse activate(Long id) {
        User u = load(id);
        if (u.getStatus() != UserStatus.PENDING_ACTIVATION) {
            throw ApiException.conflict("NOT_PENDING", "Account is not waiting for activation");
        }
        u.setStatus(UserStatus.ACTIVE);
        audit.log("USER_ACTIVATED", "User", id, null);
        return UserMapper.toResponse(users.save(u));
    }

    @Transactional
    public UserResponse changeRole(Long actorId, Long id, Role newRole) {
        User u = load(id);
        if (u.getId().equals(actorId)) {
            throw ApiException.forbidden("You cannot change your own role");
        }
        Role old = u.getRole();
        u.setRole(newRole);
        users.save(u);
        tokenService.revokeAll(id); // old tokens carry the old role
        audit.log("ROLE_CHANGED", "User", id, old + "->" + newRole);
        return UserMapper.toResponse(u);
    }

    @Transactional(readOnly = true)
    public List<RoleSummary> roleSummary() {
        return users.countByRoleAndStatus().stream()
                .map(r -> new RoleSummary((Role) r[0], (UserStatus) r[1], (Long) r[2])).toList();
    }

    @Transactional(readOnly = true)
    public ClientSummary clientSummary() {
        long total = 0;
        for (UserStatus s : UserStatus.values()) total += users.countByRoleAndStatus(Role.CLIENT, s);
        return new ClientSummary(total,
                users.countByRoleAndCreatedAtAfter(Role.CLIENT, Instant.now().minus(Duration.ofDays(7))),
                users.countByRoleAndStatus(Role.CLIENT, UserStatus.ACTIVE),
                users.countByRoleAndStatus(Role.CLIENT, UserStatus.PENDING_ACTIVATION),
                users.countByRoleAndKycStatus(Role.CLIENT, KycStatus.SUBMITTED));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> pendingKyc(Pageable pageable) {
        return list(Role.CLIENT, null, KycStatus.SUBMITTED, null, null, null, pageable);
    }

    private User load(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
    }
}
