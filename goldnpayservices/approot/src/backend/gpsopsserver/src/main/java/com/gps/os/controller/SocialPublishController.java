package com.gps.os.controller;

import com.gps.os.dto.MessagingDtos.SocialPostRequest;
import com.gps.os.dto.MessagingDtos.SocialPostResult;
import com.gps.os.service.SocialPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Social publishing")
@RestController
@RequestMapping("/cs/v1/social")
@RequiredArgsConstructor
public class SocialPublishController {
    private final SocialPublishService social;

    @Operation(summary = "Publish text and an optional image to the channels enabled in the social-media settings")
    @PostMapping("/post")
    public SocialPostResult post(@Valid @RequestBody SocialPostRequest r) {
        return social.publish(r);
    }
}
