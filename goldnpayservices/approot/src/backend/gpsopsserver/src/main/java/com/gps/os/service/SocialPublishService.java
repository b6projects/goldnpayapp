package com.gps.os.service;

import com.gps.os.dto.MessagingDtos.LiveRate;
import com.gps.os.dto.MessagingDtos.SocialPostRequest;
import com.gps.os.dto.MessagingDtos.SocialPostResult;
import com.gps.os.entity.Channel;
import com.gps.os.entity.NotificationLog;
import com.gps.os.exception.ApiException;
import com.gps.os.repository.AppSettingRepository;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Publishes to the channels that are switched on in the SOCIAL settings: Telegram PR, Discord, Twitter/X.
 * Returns one result per channel. A failure on one channel never blocks the others.
 */
@Slf4j
@Service
public class SocialPublishService {
    public static final String TELEGRAM_PR = "TELEGRAM_PR";
    private static final Set<String> CHANNELS = Set.of(TELEGRAM_PR, "DISCORD", "TWITTER");

    private final AppSettingRepository settings;
    private final NotificationLogService logs;
    private final TgmPRBotService prBot;
    private final TelegramMessagingService messaging;
    private final ImageOverlayService overlay;
    private final AppPublicCache cache;
    private final List<ChannelPublisher> publishers;

    public SocialPublishService(AppSettingRepository settings, NotificationLogService logs, TgmPRBotService prBot,
                                TelegramMessagingService messaging, ImageOverlayService overlay, AppPublicCache cache,
                                List<ChannelPublisher> publishers) {
        this.settings = settings;
        this.logs = logs;
        this.prBot = prBot;
        this.messaging = messaging;
        this.overlay = overlay;
        this.cache = cache;
        this.publishers = publishers;
    }

    private ChannelPublisher publisherFor(String channel) {
        return publishers.stream().filter(p -> channel.equals(p.channel())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No publisher for " + channel));
    }

    public SocialPostResult publish(SocialPostRequest r) {
        List<String> channels = r.channels().stream().map(c -> c.trim().toUpperCase(Locale.ROOT)).distinct().toList();
        if (!CHANNELS.containsAll(channels)) {
            throw ApiException.badRequest("INVALID_CHANNEL", "Channels must be among " + CHANNELS);
        }
        byte[] image = TelegramMessagingService.decodeImage(r.imageBase64());
        Map<String, String> results = new LinkedHashMap<>();

        if (r.overlayRate()) {
            LiveRate rate = cache.liveRateFromDb();
            if (!"LIVE".equals(rate.status())) { // nothing truthful to show while paused or unset
                channels.forEach(c -> results.put(c, "SKIPPED_RATE_" + rate.status()));
                return new SocialPostResult(r.requestId(), results);
            }
            image = overlay.render(image, rate, ZonedDateTime.now());
        }

        for (String channel : channels) {
            if (!enabled(channel)) {
                results.put(channel, "SKIPPED_DISABLED");
                continue;
            }
            var created = logs.createIfAbsent(r.requestId() + ":" + channel, logChannel(channel), "SOCIAL_POST", null, channel);
            if (!created.isNew()) {
                results.put(channel, created.log().getStatus().name()); // repeat of an accepted request
                continue;
            }
            results.put(channel, deliver(channel, created.log(), r.text(), image));
        }
        return new SocialPostResult(r.requestId(), results);
    }

    private String deliver(String channel, NotificationLog entry, String text, byte[] image) {
        if (TELEGRAM_PR.equals(channel)) {
            messaging.track(entry, prBot.post(text, image));
            return "QUEUED";
        }
        try {
            publisherFor(channel).publish(text, image);
            logs.markSent(entry.getId(), 1);
            return "SENT";
        } catch (Exception e) {
            log.warn("{} post failed: {}", channel, e.getMessage());
            logs.markFailed(entry.getId(), 1, e.getMessage());
            return "FAILED";
        }
    }

    private boolean enabled(String channel) {
        String key = channel.toLowerCase(Locale.ROOT) + ".enabled";
        return settings.findById(key).filter(s -> "SOCIAL".equals(s.getScope()))
                .map(s -> "true".equalsIgnoreCase(s.getValue())).orElse(false);
    }

    private static Channel logChannel(String channel) {
        return switch (channel) {
            case "DISCORD" -> Channel.DISCORD;
            case "TWITTER" -> Channel.TWITTER;
            default -> Channel.TELEGRAM;
        };
    }
}
