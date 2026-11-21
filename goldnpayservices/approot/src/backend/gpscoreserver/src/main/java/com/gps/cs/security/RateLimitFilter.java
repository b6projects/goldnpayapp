package com.gps.cs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/** In-memory fixed-window limiter for /tc/v1/auth/**, per client IP. */
public class RateLimitFilter extends OncePerRequestFilter {
    private record Window(long minute, int count) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limitPerMinute;
    private final ErrorResponseWriter writer;

    public RateLimitFilter(int limitPerMinute, ErrorResponseWriter writer) {
        this.limitPerMinute = limitPerMinute;
        this.writer = writer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !req.getRequestURI().startsWith("/tc/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        long minute = System.currentTimeMillis() / 60_000;
        Window w = windows.merge(req.getRemoteAddr(), new Window(minute, 1),
                (old, fresh) -> old.minute() == minute ? new Window(minute, old.count() + 1) : fresh);
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> e.getValue().minute() < minute);
        }
        if (w.count() > limitPerMinute) {
            res.setHeader("Retry-After", "60");
            writer.write(req, res, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests");
            return;
        }
        chain.doFilter(req, res);
    }
}
