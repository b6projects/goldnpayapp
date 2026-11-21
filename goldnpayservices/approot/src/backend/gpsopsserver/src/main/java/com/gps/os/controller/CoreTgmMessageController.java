package com.gps.os.controller;

import com.gps.os.dto.MessagingDtos.MessageAccepted;
import com.gps.os.dto.MessagingDtos.TgmAppMessage;
import com.gps.os.dto.MessagingDtos.TgmPrMessage;
import com.gps.os.service.TelegramMessagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Telegram messaging")
@RestController
@RequestMapping("/cs/v1/tgm")
@RequiredArgsConstructor
public class CoreTgmMessageController {
    private final TelegramMessagingService messaging;

    @Operation(summary = "Send an app message to one linked user through the support bot")
    @PostMapping("/tgmappmessage")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageAccepted appMessage(@Valid @RequestBody TgmAppMessage m) {
        return messaging.sendAppMessage(m);
    }

    @Operation(summary = "Send a PR or announcement message, with an optional image, to the PR channel")
    @PostMapping("/tgmprmessage")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageAccepted prMessage(@Valid @RequestBody TgmPrMessage m) {
        return messaging.sendPrMessage(m);
    }
}
