package com.gps.cs.service;

import com.gps.cs.entity.AuditLog;
import com.gps.cs.repository.AuditLogRepository;
import com.gps.cs.security.AppPrincipal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repo;

    /** Records a privileged action. detail must not contain secrets or personal documents. */
    public void log(String action, String targetType, Object targetId, String detail) {
        AuditLog a = new AuditLog();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppPrincipal p) a.setActorId(p.id());
        a.setAction(action);
        a.setTargetType(targetType);
        a.setTargetId(targetId == null ? null : String.valueOf(targetId));
        a.setDetailJson(detail == null ? null : (detail.length() > 4000 ? detail.substring(0, 4000) : detail));
        a.setOccurredAt(Instant.now());
        repo.save(a);
    }
}
