package com.gps.cs.service;

import com.gps.cs.dto.AdminDtos.*;
import com.gps.cs.dto.OpsDtos;
import com.gps.cs.entity.GoldRate;
import com.gps.cs.entity.SettingScope;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.repository.GoldRateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The live rate is the newest rate whose effective time has passed. Pausing applies to the live rate. */
@Service
@RequiredArgsConstructor
public class GoldRateService {
    private final GoldRateRepository repo;
    private final SettingsService settings;
    private final OpsApiService ops;
    private final AuditService audit;

    @Transactional
    public GoldRateResponse add(GoldRateRequest r) {
        if (r.rate24k().compareTo(r.rate22k()) < 0 || r.rate22k().compareTo(r.rate18k()) < 0) {
            throw ApiException.badRequest("RATE_ORDER", "Expected 24K >= 22K >= 18K");
        }
        Instant eff = r.effectiveAt() == null ? Instant.now() : r.effectiveAt();
        GoldRate g = new GoldRate();
        g.setRate24k(r.rate24k());
        g.setRate22k(r.rate22k());
        g.setRate18k(r.rate18k());
        g.setCurrency("INR");
        g.setEffectiveAt(eff);
        g.setPaused(false);
        g.setSource("MANUAL");
        repo.save(g);
        audit.log("GOLD_RATE_ADDED", "GoldRate", g.getId(), "24k=" + g.getRate24k());
        if (!eff.isAfter(Instant.now())) announce(g);
        return toResponse(g);
    }

    @Transactional(readOnly = true)
    public LiveRateResponse live() {
        return repo.findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDescIdDesc(Instant.now())
                .map(g -> g.isPaused()
                        ? new LiveRateResponse("PAUSED", null, null, null, g.getCurrency(), g.getEffectiveAt())
                        : new LiveRateResponse("LIVE", g.getRate24k(), g.getRate22k(), g.getRate18k(),
                        g.getCurrency(), g.getEffectiveAt()))
                .orElse(new LiveRateResponse("NO_RATE", null, null, null, "INR", null));
    }

    @Transactional
    public GoldRateResponse pause() {
        return setPaused(true);
    }

    @Transactional
    public GoldRateResponse resume() {
        return setPaused(false);
    }

    @Transactional(readOnly = true)
    public Page<GoldRateResponse> history(Pageable pageable) {
        return repo.findAll(pageable).map(GoldRateService::toResponse);
    }

    private GoldRateResponse setPaused(boolean paused) {
        GoldRate g = repo.findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDescIdDesc(Instant.now())
                .orElseThrow(() -> ApiException.notFound("Live gold rate"));
        g.setPaused(paused);
        repo.save(g);
        audit.log(paused ? "GOLD_RATE_PAUSED" : "GOLD_RATE_RESUMED", "GoldRate", g.getId(), null);
        return toResponse(g);
    }

    /** Asks Ops to post the new rate to the channels switched on in the social-media settings. */
    private void announce(GoldRate g) {
        if (!settings.flag(SettingScope.SOCIAL, "post_on_rate_change")) return;
        List<String> channels = new ArrayList<>();
        if (settings.flag(SettingScope.SOCIAL, "telegram_pr.enabled")) channels.add("TELEGRAM_PR");
        if (settings.flag(SettingScope.SOCIAL, "discord.enabled")) channels.add("DISCORD");
        if (settings.flag(SettingScope.SOCIAL, "twitter.enabled")) channels.add("TWITTER");
        if (channels.isEmpty()) return;
        String text = "Gold rate update - 24K: " + money(g.getRate24k()) + "/g, 22K: " + money(g.getRate22k())
                + "/g, 18K: " + money(g.getRate18k()) + "/g";
        ops.publishSocialPostAsync(new OpsDtos.SocialPostRequest(UUID.randomUUID().toString(), text, null, channels, true));
    }

    private static String money(BigDecimal v) {
        return "INR " + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    static GoldRateResponse toResponse(GoldRate g) {
        return new GoldRateResponse(g.getId(), g.getRate24k(), g.getRate22k(), g.getRate18k(), g.getCurrency(),
                g.getEffectiveAt(), g.isPaused(), g.getSource());
    }
}
