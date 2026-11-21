package com.gps.cs.controller.publicapi;

import com.gps.cs.dto.AdminDtos.LiveRateResponse;
import com.gps.cs.dto.AdminDtos.MediaImageResponse;
import com.gps.cs.dto.AdminDtos.PublicReportResponse;
import com.gps.cs.entity.MediaImage;
import com.gps.cs.entity.MediaKind;
import com.gps.cs.service.GoldRateService;
import com.gps.cs.service.MediaService;
import com.gps.cs.service.ReportService;
import com.gps.cs.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Unauthenticated endpoints. */
@Tag(name = "Public")
@RestController
@RequestMapping("/tc/v1/public")
@RequiredArgsConstructor
public class PublicController {
    private final GoldRateService rates;
    private final MediaService media;
    private final SettingsService settings;
    private final ReportService reports;

    @Operation(summary = "The live gold rate, or a paused status")
    @GetMapping("/gold-rate/live")
    public LiveRateResponse liveRate() {
        return rates.live();
    }

    @Operation(summary = "Active homepage images")
    @GetMapping("/homepage-images")
    public List<MediaImageResponse> homepageImages() {
        return media.list(MediaKind.HOMEPAGE, true);
    }

    @Operation(summary = "Active event images")
    @GetMapping("/event-images")
    public List<MediaImageResponse> eventImages() {
        return media.list(MediaKind.EVENT, true);
    }

    @Operation(summary = "Image bytes for an active homepage/event image (the url in the list responses)")
    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> image(@PathVariable Long id) {
        MediaImage m = media.activeImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(m.getContentType()))
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(media.content(m));
    }

    @Operation(summary = "Public app settings")
    @GetMapping("/app-settings")
    public Map<String, String> appSettings() {
        return settings.publicApp();
    }

    @Operation(summary = "Public reports: aggregate counts only, no personal data")
    @GetMapping("/reports")
    public PublicReportResponse reports() {
        return reports.publicReport();
    }
}
