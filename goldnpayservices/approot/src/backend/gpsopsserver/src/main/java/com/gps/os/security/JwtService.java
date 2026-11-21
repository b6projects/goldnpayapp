package com.gps.os.security;

import com.gps.os.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import org.springframework.stereotype.Service;

/** Validates Core-issued JWTs locally with the shared signing key. Ops never issues tokens. */
@Service
public class JwtService {
    private final Key key;

    public JwtService(AppProperties props) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(props.jwt().secret()));
    }

    /** Verifies signature and expiry. Throws a JwtException when invalid. */
    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
