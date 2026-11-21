package com.gps.cs.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails startup, before any connection is attempted, when a required setting or secret is missing.
 * Spring leaves an unresolved ${VAR} as literal text, which would otherwise let the server start with
 * a placeholder as its JWT key or database password. Only the names are reported, never values.
 */
public class RequiredConfigCheck implements EnvironmentPostProcessor, Ordered {

    static final List<String> REQUIRED = List.of(
            "app.env", "spring.datasource.url", "spring.datasource.username", "spring.datasource.password",
            "app.jwt.secret", "app.security.core-to-ops-api-key", "app.security.ops-to-core-api-key",
            "app.security.field-encryption-key", "app.cors.allowed-origins", "app.ops.base-url",
            "app.storage.volume-home");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        List<String> problems = new ArrayList<>();
        for (String key : REQUIRED) {
            try {
                if (env.getProperty(key) == null) problems.add(key + " (not set)");
            } catch (IllegalArgumentException e) {
                problems.add(key + " (environment variable missing: " + placeholderName(e.getMessage()) + ")");
            }
        }
        if (!problems.isEmpty()) {
            throw new MissingConfigException(problems);
        }
    }

    private static String placeholderName(String message) {
        int a = message == null ? -1 : message.indexOf('\'');
        int b = a < 0 ? -1 : message.indexOf('\'', a + 1);
        return a >= 0 && b > a ? message.substring(a + 1, b) : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
