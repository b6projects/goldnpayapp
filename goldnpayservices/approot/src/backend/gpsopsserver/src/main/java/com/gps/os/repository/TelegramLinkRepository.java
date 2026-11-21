package com.gps.os.repository;

import com.gps.os.entity.TelegramLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramLinkRepository extends JpaRepository<TelegramLink, Long> {
    Optional<TelegramLink> findByUserId(Long userId);
    Optional<TelegramLink> findByChatId(Long chatId);
}
