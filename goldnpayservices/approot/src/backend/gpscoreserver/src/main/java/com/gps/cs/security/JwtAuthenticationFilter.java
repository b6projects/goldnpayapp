package com.gps.cs.security;

import com.gps.cs.entity.Role;
import com.gps.cs.entity.Token;
import com.gps.cs.entity.TokenType;
import com.gps.cs.entity.UserStatus;
import com.gps.cs.repository.TokenRepository;
import com.gps.cs.repository.UserRepository;
import com.gps.cs.util.Hashing;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the Bearer access token: signature, expiry, type, and that the stored token is not revoked
 * (checked on every request). Unauthenticated requests continue and are rejected by the security chain.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TokenRepository tokens;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwtService, TokenRepository tokens, UserRepository users) {
        this.jwtService = jwtService;
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return req.getRequestURI().startsWith("/tc/v1/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(header.substring(7).trim());
        }
        chain.doFilter(req, res);
    }

    private void authenticate(String jwt) {
        try {
            Claims claims = jwtService.parse(jwt);
            if (!TokenType.ACCESS.name().equals(claims.get("typ", String.class))) return;
            Optional<Token> stored = tokens.findByTokenHash(Hashing.sha256Hex(jwt));
            if (stored.isEmpty() || stored.get().isRevoked() || stored.get().isExpired()
                    || stored.get().getExpiresAt().isBefore(Instant.now())) {
                return;
            }
            users.findById(stored.get().getUserId())
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                    .ifPresent(u -> {
                        AppPrincipal p = new AppPrincipal(u.getId(), u.getEmail(), Role.valueOf(u.getRole().name()));
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                                p, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))));
                    });
        } catch (Exception e) {
            log.debug("Rejected bearer token: {}", e.getClass().getSimpleName());
        }
    }
}
