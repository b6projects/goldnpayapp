package com.gps.os.controller;

import com.gps.os.dto.MessagingDtos.TokenCheckRequest;
import com.gps.os.dto.MessagingDtos.TokenCheckResponse;
import com.gps.os.security.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token check")
@RestController
@RequestMapping("/cs/v1")
@RequiredArgsConstructor
public class ApiAuthController {
    private final JwtService jwt;

    /**
     * Validates signature, expiry and type locally with the shared signing key.
     * Revocation lives in Core; use BotApiService.tokenCheck when it matters.
     */
    @Operation(summary = "Validate an access token locally (signature and expiry only)")
    @PostMapping("/tokencheck")
    public TokenCheckResponse tokenCheck(@Valid @RequestBody TokenCheckRequest r) {
        try {
            Claims c = jwt.parse(r.token());
            if (!"ACCESS".equals(c.get("typ", String.class))) return new TokenCheckResponse(false, null, null, null);
            return new TokenCheckResponse(true, c.get("uid", Long.class), c.getSubject(), c.get("role", String.class));
        } catch (Exception e) {
            return new TokenCheckResponse(false, null, null, null);
        }
    }
}
