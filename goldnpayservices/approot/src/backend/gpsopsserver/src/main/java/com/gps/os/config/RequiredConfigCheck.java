package com.gps.os.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
            "app.mail.from", "app.mail.reply-to");

    /** Social integrations: the credentials are required only while the integration's enabled flag is on. */
    static final Map<String, Map<String, String>> SOCIAL = new TreeMap<>(Map.of(
            "app.telegram", Map.of("support-bot-username", "TGM_SUPPORT_BOT_USERNAME",
                    "support-bot-token", "TGM_SUPPORT_BOT_TOKEN", "pr-bot-username", "TGM_PR_BOT_USERNAME",
                    "pr-bot-token", "TGM_PR_BOT_TOKEN", "pr-channel-id", "TGM_PR_CHANNEL_ID"),
            "app.discord", Map.of("webhook-url", "DISCORD_WEBHOOK_URL"),
            "app.twitter", Map.of("api-key", "TWITTER_API_KEY", "api-secret", "TWITTER_API_SECRET",
                    "access-token", "TWITTER_ACCESS_TOKEN", "access-secret", "TWITTER_ACCESS_SECRET")));

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
        SOCIAL.forEach((prefix, keys) -> {
            if (!env.getProperty(prefix + ".enabled", Boolean.class, true)) return; // disabled: nothing needed
            keys.forEach((key, envVar) -> {
                String value = env.getProperty(prefix + "." + key);
                if (value == null || value.isBlank()) {
                    problems.add(prefix + "." + key + " (environment variable " + envVar + " is empty; set it, or set "
                            + prefix + ".enabled=false)");
                }
            });
        });
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
