package com.gps.cs.service;

import com.gps.cs.config.AppProperties;
import com.gps.cs.dto.ClientDtos.*;
import com.gps.cs.entity.TelegramLink;
import com.gps.cs.entity.TelegramLinkCode;
import com.gps.cs.entity.User;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.repository.TelegramLinkCodeRepository;
import com.gps.cs.repository.TelegramLinkRepository;
import com.gps.cs.repository.UserRepository;
import com.gps.cs.util.Hashing;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Telegram linking: 8-character code, valid 10 minutes, single use, stored hashed. */
@Service
@RequiredArgsConstructor
public class TelegramService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private final TelegramLinkCodeRepository codes;
    private final TelegramLinkRepository links;
    private final UserRepository users;
    private final OpsApiService ops;
    private final AppProperties props;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public LinkCodeResponse issueCode(Long userId) {
        Instant now = Instant.now();
        codes.findByUserIdAndUsedAtIsNull(userId).forEach(c -> {
            c.setUsedAt(now);
            codes.save(c);
        });
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        String code = sb.toString();
        TelegramLinkCode c = new TelegramLinkCode();
        c.setUserId(userId);
        c.setCodeHash(Hashing.sha256Hex(code));
        c.setExpiresAt(now.plus(Duration.ofMinutes(props.telegram().linkCodeTtlMinutes())));
        codes.save(c);
        return new LinkCodeResponse(code, c.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public TelegramStatusResponse status(Long userId) {
        return links.findByUserId(userId)
                .map(l -> new TelegramStatusResponse(true, l.getUsername(), l.getLinkedAt()))
                .orElse(new TelegramStatusResponse(false, null, null));
    }

    public void disconnect(Long userId) {
        if (links.findByUserId(userId).isEmpty()) return;
        if (!ops.unlinkTelegram(userId)) {
            throw ApiException.unavailable("OPS_UNAVAILABLE", "Could not disconnect Telegram right now. Try again shortly.");
        }
    }

    /** Called by Ops when a user sends /start CODE to the bot. */
    @Transactional
    public LinkCodeVerifyResponse verify(String code) {
        TelegramLinkCode c = codes.findByCodeHash(Hashing.sha256Hex(code.trim().toUpperCase(Locale.ROOT))).orElse(null);
        if (c == null || c.getUsedAt() != null || c.getExpiresAt().isBefore(Instant.now())) {
            return new LinkCodeVerifyResponse(false, null);
        }
        c.setUsedAt(Instant.now());
        codes.save(c);
        return new LinkCodeVerifyResponse(true, c.getUserId());
    }

    @Transactional(readOnly = true)
    public Page<TelegramUserResponse> linkedUsers(Pageable pageable) {
        Page<TelegramLink> page = links.findAll(pageable);
        List<Long> ids = page.getContent().stream().map(TelegramLink::getUserId).toList();
        var byId = new java.util.HashMap<Long, User>();
        users.findAllById(ids).forEach(u -> byId.put(u.getId(), u));
        return page.map(l -> {
            User u = byId.get(l.getUserId());
            return new TelegramUserResponse(l.getUserId(), u == null ? null : u.getFullName(),
                    u == null ? null : u.getEmail(), l.getUsername(), l.getLinkedAt());
        });
    }
}
