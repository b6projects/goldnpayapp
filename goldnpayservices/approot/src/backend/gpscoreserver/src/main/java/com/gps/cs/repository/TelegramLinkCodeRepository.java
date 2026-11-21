package com.gps.cs.repository;

import com.gps.cs.entity.TelegramLinkCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramLinkCodeRepository extends JpaRepository<TelegramLinkCode, Long> {
    Optional<TelegramLinkCode> findByCodeHash(String codeHash);
    List<TelegramLinkCode> findByUserIdAndUsedAtIsNull(Long userId);
}
