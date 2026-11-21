package com.gps.cs.repository;

import com.gps.cs.entity.TelegramLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramLinkRepository extends JpaRepository<TelegramLink, Long> {
    Optional<TelegramLink> findByUserId(Long userId);
}
