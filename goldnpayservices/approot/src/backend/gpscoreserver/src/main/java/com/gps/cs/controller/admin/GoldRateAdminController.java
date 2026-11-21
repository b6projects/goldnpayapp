package com.gps.cs.controller.admin;

import com.gps.cs.dto.AdminDtos.GoldRateRequest;
import com.gps.cs.dto.AdminDtos.GoldRateResponse;
import com.gps.cs.service.GoldRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - gold rate")
@RestController
@RequestMapping("/tc/v1/admin/gold-rate")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@RequiredArgsConstructor
public class GoldRateAdminController {
    private final GoldRateService rates;

    @Operation(summary = "Add a new gold rate; it becomes the live rate")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoldRateResponse add(@Valid @RequestBody GoldRateRequest r) {
        return rates.add(r);
    }

    @Operation(summary = "Pause the live rate")
    @PutMapping("/pause")
    public GoldRateResponse pause() {
        return rates.pause();
    }

    @Operation(summary = "Resume the live rate")
    @PutMapping("/resume")
    public GoldRateResponse resume() {
        return rates.resume();
    }

    @Operation(summary = "Paged rate history (newest first by default)")
    @GetMapping("/history")
    public Page<GoldRateResponse> history(
            @org.springframework.data.web.PageableDefault(sort = "effectiveAt",
                    direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return rates.history(pageable);
    }
}
