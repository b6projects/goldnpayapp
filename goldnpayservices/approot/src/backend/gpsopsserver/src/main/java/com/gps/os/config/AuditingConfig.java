package com.gps.os.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Ops rows are always attributed to "ops-server". */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
public class AuditingConfig {
    @Bean
    public AuditorAware<String> applicationAuditAware() {
        return () -> Optional.of("ops-server");
    }
}
