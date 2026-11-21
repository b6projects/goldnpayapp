package com.gps.os.controller;

import com.gps.os.dto.MessagingDtos.PublicCacheResponse;
import com.gps.os.service.AppPublicCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cached public data for the bots only. API key required; the thin client does not use it. */
@Tag(name = "Public cache")
@RestController
@RequestMapping("/cs/v1")
@RequiredArgsConstructor
public class AppPublicController {
    private final AppPublicCache cache;

    @Operation(summary = "Cached live gold rate and public app settings")
    @GetMapping("/public-cache")
    public PublicCacheResponse publicCache() {
        return cache.snapshot();
    }
}
