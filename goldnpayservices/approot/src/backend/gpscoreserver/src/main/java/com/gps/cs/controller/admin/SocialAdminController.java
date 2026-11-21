package com.gps.cs.controller.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.gps.cs.dto.AdminDtos.SocialPostRequest;
import com.gps.cs.dto.OpsDtos;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - social")
@RestController
@RequestMapping("/tc/v1/admin/social")
@PreAuthorize("hasAnyRole('ADMIN','MARKETING')")
@RequiredArgsConstructor
public class SocialAdminController {
    private static final Set<String> CHANNELS = Set.of("TELEGRAM_PR", "DISCORD", "TWITTER");
    private static final int MAX_IMAGE_BASE64 = 7_000_000; // ~5 MB of image data

    private final OpsApiService ops;
    private final AuditService audit;

    @Operation(summary = "Ask Ops to publish an announcement; returns one result per channel")
    @PostMapping("/post")
    public JsonNode post(@Valid @RequestBody SocialPostRequest r) {
        List<String> channels = r.channels().stream().map(String::toUpperCase).distinct().toList();
        if (!CHANNELS.containsAll(channels)) {
            throw ApiException.badRequest("INVALID_CHANNEL", "Channels must be among " + CHANNELS);
        }
        if (r.imageBase64() != null && r.imageBase64().length() > MAX_IMAGE_BASE64) {
            throw ApiException.badRequest("IMAGE_TOO_LARGE", "Image exceeds 5 MB");
        }
        JsonNode result = ops.publishSocialPost(new OpsDtos.SocialPostRequest(UUID.randomUUID().toString(), r.text(),
                        r.imageBase64(), channels, false))
                .orElseThrow(() -> ApiException.unavailable("OPS_UNAVAILABLE", "Could not publish right now. Try again shortly."));
        audit.log("SOCIAL_POST", "Social", null, String.join(",", channels));
        return result;
    }
}
