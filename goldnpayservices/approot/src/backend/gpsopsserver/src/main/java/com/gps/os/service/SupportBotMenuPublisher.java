package com.gps.os.service;

import com.gps.os.config.AppProperties;
import com.gps.os.integration.TelegramApiClient;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Sets the support bot's command menu on startup. A failure is logged and never blocks startup. */
@Slf4j
@Service
public class SupportBotMenuPublisher {
    static final List<Map<String, String>> COMMANDS = List.of(
            Map.of("command", "start", "description", "Link your GoldNPay account"),
            Map.of("command", "rate", "description", "Today's gold rate"),
            Map.of("command", "disconnect", "description", "Unlink this chat"),
            Map.of("command", "help", "description", "Show help"));

    private final TelegramApiClient api;
    private final boolean enabled;
    private final boolean integrationEnabled;

    public SupportBotMenuPublisher(@Qualifier("supportBotApi") TelegramApiClient api, AppProperties props) {
        this.api = api;
        this.integrationEnabled = props.telegram().enabled();
        this.enabled = props.telegram().pollingEnabled();
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void publish() {
        if (!integrationEnabled) {
            log.warn("Support bot menu not published: the Telegram integration is disabled (app.telegram.enabled=false)");
            return;
        }
        if (!enabled) return;
        try {
            api.setMyCommands(COMMANDS);
        } catch (RuntimeException e) {
            log.warn("Could not publish the support bot menu: {}", e.getMessage());
        }
    }
}
