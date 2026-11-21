package com.gps.os.service;

import com.gps.os.entity.TelegramLink;
import com.gps.os.repository.TelegramLinkRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Writes telegram_links (Ops owns this table). One chat per user and one user per chat. */
@Service
@RequiredArgsConstructor
public class TelegramLinkService {
    private final TelegramLinkRepository links;
    private final AppPublicCache cache;

    @Transactional
    public TelegramLink link(Long userId, Long chatId, String username) {
        links.findByUserId(userId).ifPresent(links::delete);
        links.findByChatId(chatId).ifPresent(links::delete);
        links.flush();
        TelegramLink l = new TelegramLink();
        l.setUserId(userId);
        l.setChatId(chatId);
        l.setUsername(username == null ? null : (username.length() > 100 ? username.substring(0, 100) : username));
        l.setLinkedAt(Instant.now());
        links.save(l);
        cache.putLink(chatId, userId);
        return l;
    }

    /** Returns the chat id that was unlinked, or empty when the user had no link. */
    @Transactional
    public Optional<Long> unlinkUser(Long userId) {
        return links.findByUserId(userId).map(l -> {
            links.delete(l);
            cache.removeChat(l.getChatId());
            return l.getChatId();
        });
    }

    @Transactional
    public Optional<Long> unlinkChat(Long chatId) {
        return links.findByChatId(chatId).map(l -> {
            links.delete(l);
            cache.removeChat(chatId);
            return l.getUserId();
        });
    }

    @Transactional(readOnly = true)
    public Optional<TelegramLink> byUser(Long userId) {
        return links.findByUserId(userId);
    }
}
