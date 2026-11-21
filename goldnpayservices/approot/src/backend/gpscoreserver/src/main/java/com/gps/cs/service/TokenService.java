package com.gps.cs.service;

import com.gps.cs.dto.AuthDtos.TokenResponse;
import com.gps.cs.entity.Token;
import com.gps.cs.entity.TokenType;
import com.gps.cs.entity.User;
import com.gps.cs.repository.TokenRepository;
import com.gps.cs.security.JwtService;
import com.gps.cs.util.Hashing;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Issues and revokes JWTs. Only SHA-256 hashes of tokens are stored. */
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtService jwt;
    private final TokenRepository tokens;

    @Transactional
    public TokenResponse issuePair(User user) {
        String access = persist(user, TokenType.ACCESS);
        String refresh = persist(user, TokenType.REFRESH);
        return response(user, access, refresh);
    }

    @Transactional
    public TokenResponse issueAccessOnly(User user, String existingRefresh) {
        return response(user, persist(user, TokenType.ACCESS), existingRefresh);
    }

    @Transactional
    public void revokeAll(Long userId) {
        tokens.revokeAllForUser(userId);
    }

    private String persist(User user, TokenType type) {
        String value = jwt.generate(user, type);
        Token t = new Token();
        t.setUserId(user.getId());
        t.setTokenHash(Hashing.sha256Hex(value));
        t.setType(type);
        t.setExpiresAt(Instant.now().plusMillis(jwt.ttlMs(type)));
        tokens.save(t);
        return value;
    }

    private TokenResponse response(User user, String access, String refresh) {
        return new TokenResponse(access, refresh, "Bearer", jwt.ttlMs(TokenType.ACCESS) / 1000, user.getRole());
    }
}
