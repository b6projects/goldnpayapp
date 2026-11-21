package com.gps.os.controller;

import com.gps.os.dto.EmailDtos.EmailAccepted;
import com.gps.os.dto.EmailDtos.EmailRequest;
import com.gps.os.dto.EmailDtos.EmailStatus;
import com.gps.os.entity.Channel;
import com.gps.os.exception.ApiException;
import com.gps.os.service.EmailService;
import com.gps.os.service.NotificationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email")
@RestController
@RequestMapping("/cs/v1/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emails;
    private final NotificationLogService logs;

    @Operation(summary = "Wrap the body in the selected template, apply the mail rules and send asynchronously")
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EmailAccepted send(@Valid @RequestBody EmailRequest r) {
        return emails.accept(r);
    }

    @Operation(summary = "Delivery status: QUEUED, SENT or FAILED")
    @GetMapping("/{notificationId}")
    public EmailStatus status(@PathVariable String notificationId) {
        long id;
        try {
            id = Long.parseLong(notificationId);
        } catch (NumberFormatException e) {
            throw ApiException.notFound("Notification");
        }
        return logs.findById(id).filter(n -> n.getChannel() == Channel.EMAIL)
                .map(n -> new EmailStatus(String.valueOf(n.getId()), n.getStatus(), n.getAttempts(), n.getError()))
                .orElseThrow(() -> ApiException.notFound("Notification"));
    }
}
