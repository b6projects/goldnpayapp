package com.gps.os.service;

import com.gps.os.entity.TelegramLink;
import com.gps.os.repository.TelegramLinkRepository;
import com.gps.os.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks that a Telegram chat is linked to an ACTIVE GoldNPay account. The bot must call this before it
 * shows anything account-specific. It reads the database, not the cache, so a suspension takes effect at once.
 */
@Service
@RequiredArgsConstructor
public class BotAuthorizationService {
    private final TelegramLinkRepository links;
    private final UserRepository users;

    /** The userId when the chat is linked to an active account, otherwise empty. */
    @Transactional(readOnly = true)
    public Optional<Long> authorize(long chatId) {
        return links.findByChatId(chatId)
                .map(TelegramLink::getUserId)
                .filter(id -> users.findById(id).map(u -> "ACTIVE".equals(u.getStatus())).orElse(false));
    }
}
