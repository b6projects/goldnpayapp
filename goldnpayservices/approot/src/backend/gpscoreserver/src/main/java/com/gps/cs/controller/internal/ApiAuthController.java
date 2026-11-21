package com.gps.cs.controller.internal;

import com.gps.cs.dto.AuthDtos.TokenCheckRequest;
import com.gps.cs.dto.AuthDtos.TokenCheckResponse;
import com.gps.cs.dto.ClientDtos.LinkCodeVerifyRequest;
import com.gps.cs.dto.ClientDtos.LinkCodeVerifyResponse;
import com.gps.cs.service.AuthService;
import com.gps.cs.service.TelegramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints for Ops. Authenticated with the Ops->Core API key (X-API-KEY), never a JWT. */
@Tag(name = "Internal (Ops)")
@RestController
@RequestMapping("/tc/v1/internal")
@RequiredArgsConstructor
public class ApiAuthController {
    private final AuthService auth;
    private final TelegramService telegram;

    @Operation(summary = "Validate a JWT for Ops")
    @PostMapping("/tokencheck")
    public TokenCheckResponse tokenCheck(@Valid @RequestBody TokenCheckRequest r) {
        return auth.tokenCheck(r.token());
    }

    @Operation(summary = "Verify a Telegram link code; returns the userId when valid")
    @PostMapping("/telegram/link-code/verify")
    public LinkCodeVerifyResponse verifyLinkCode(@Valid @RequestBody LinkCodeVerifyRequest r) {
        return telegram.verify(r.code());
    }
}
