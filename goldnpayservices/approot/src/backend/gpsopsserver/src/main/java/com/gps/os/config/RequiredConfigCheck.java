package com.gps.os.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails startup, before any connection is attempted, when a required setting or secret is missing.
 * Spring leaves an unresolved ${VAR} as literal text, which would otherwise let the server start with
 * a placeholder as a password or key. Only the names are reported, never values.
 */
public class RequiredConfigCheck implements EnvironmentPostProcessor, Ordered {

    static final List<String> REQUIRED = List.of(
            "app.env", "spring.datasource.url", "spring.datasource.username", "spring.datasource.password",
            "app.jwt.secret", "app.security.core-to-ops-api-key", "app.security.ops-to-core-api-key",
            "app.core.base-url",
            "spring.mail.host", "spring.mail.port", "spring.mail.username", "spring.mail.password",
            "app.mail.from", "app.mail.reply-to",
            "app.telegram.support-bot-username", "app.telegram.support-bot-token",
            "app.telegram.pr-bot-username", "app.telegram.pr-bot-token", "app.telegram.pr-channel-id",
            "app.discord.webhook-url",
            "app.twitter.api-key", "app.twitter.api-secret", "app.twitter.access-token", "app.twitter.access-secret");

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
