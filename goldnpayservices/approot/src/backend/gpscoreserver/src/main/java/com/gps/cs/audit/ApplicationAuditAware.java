package com.gps.cs.audit;

import com.gps.cs.security.AppPrincipal;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Fills createdBy / updatedBy with the caller's email, or "system" outside a request. */
public class ApplicationAuditAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated()) {
            if (a.getPrincipal() instanceof AppPrincipal p) {
                return Optional.of(p.email());
            }
            if ("ops-server".equals(String.valueOf(a.getPrincipal()))) {
                return Optional.of("ops-server");
            }
        }
        return Optional.of("system");
    }
}
