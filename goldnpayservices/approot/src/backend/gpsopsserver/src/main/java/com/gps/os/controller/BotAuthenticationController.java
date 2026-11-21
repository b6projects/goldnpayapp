package com.gps.os.controller;

import com.gps.os.dto.MessagingDtos.*;
import com.gps.os.exception.ApiException;
import com.gps.os.service.TgmSupportBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Telegram linking")
@RestController
@RequestMapping("/cs/v1/bauth")
@RequiredArgsConstructor
public class BotAuthenticationController {
    private final TgmSupportBotService bot;

    @Operation(summary = "Complete a link: link code + Telegram chat id + username")
    @PostMapping("/tgmauthenticate")
    public TgmAuthenticateResponse authenticate(@Valid @RequestBody TgmAuthenticateRequest r) {
        var result = bot.linkChat(r.code(), r.chatId(), r.username());
        return switch (result.outcome()) {
            case LINKED -> new TgmAuthenticateResponse(true, result.userId());
            case INVALID_CODE -> throw ApiException.badRequest("LINK_CODE_INVALID", "Invalid or expired link code");
            case CORE_UNAVAILABLE -> throw ApiException.unavailable("CORE_UNAVAILABLE", "Could not verify the code right now");
        };
    }

    @Operation(summary = "Remove the Telegram link for a user and tell the chat")
    @PostMapping("/tgmdisconnect")
    public TgmDisconnectResponse disconnect(@Valid @RequestBody TgmDisconnectRequest r) {
        return new TgmDisconnectResponse(bot.unlinkUser(r.userId()));
    }
}
