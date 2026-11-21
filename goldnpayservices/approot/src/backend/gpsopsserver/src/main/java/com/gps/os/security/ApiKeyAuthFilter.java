package com.gps.os.security;

import com.gps.os.util.Hashing;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Every /cs/v1/** call must carry the Core->Ops API key; compared in constant time. */
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-API-KEY";
    private final String expectedKey;
    private final ErrorResponseWriter writer;

    public ApiKeyAuthFilter(String expectedKey, ErrorResponseWriter writer) {
        this.expectedKey = expectedKey;
        this.writer = writer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !req.getRequestURI().startsWith("/cs/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (!Hashing.constantTimeEquals(expectedKey, req.getHeader(HEADER))) {
            writer.write(req, res, HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", "Invalid or missing API key");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "core-server", null, List.of(new SimpleGrantedAuthority("ROLE_CORE"))));
        chain.doFilter(req, res);
    }
}
