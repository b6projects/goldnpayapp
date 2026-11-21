package com.gps.cs.security;

import com.gps.cs.config.AppProperties;
import com.gps.cs.entity.TokenType;
import com.gps.cs.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final Key key;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(AppProperties props) {
        this.key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(props.jwt().secret()));
        this.accessMs = props.jwt().accessExpirationMs();
        this.refreshMs = props.jwt().refreshExpirationMs();
    }

    public long ttlMs(TokenType type) {
        return type == TokenType.ACCESS ? accessMs : refreshMs;
    }

    public String generate(User user, TokenType type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .claim("typ", type.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(ttlMs(type))))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Verifies signature and expiry. Throws {@link JwtException} when invalid. */
    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Instant expiryOf(String token) {
        return parse(token).getExpiration().toInstant();
    }
}
