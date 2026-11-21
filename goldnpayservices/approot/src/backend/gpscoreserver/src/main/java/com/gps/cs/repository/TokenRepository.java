package com.gps.cs.repository;

import com.gps.cs.entity.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update Token t set t.revoked = true, t.expired = true where t.userId = :userId and t.revoked = false")
    int revokeAllForUser(Long userId);
}
