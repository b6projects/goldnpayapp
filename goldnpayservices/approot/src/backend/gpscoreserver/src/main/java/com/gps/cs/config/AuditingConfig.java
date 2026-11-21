package com.gps.cs.config;

import com.gps.cs.audit.ApplicationAuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
public class AuditingConfig {
    @Bean
    public AuditorAware<String> applicationAuditAware() {
        return new ApplicationAuditAware();
    }
}
