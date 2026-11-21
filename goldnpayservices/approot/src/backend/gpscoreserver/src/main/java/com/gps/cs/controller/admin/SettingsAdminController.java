package com.gps.cs.controller.admin;

import com.gps.cs.dto.AdminDtos.SettingsRequest;
import com.gps.cs.entity.SettingScope;
import com.gps.cs.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - settings")
@RestController
@RequestMapping("/tc/v1/admin/settings")
@RequiredArgsConstructor
public class SettingsAdminController {
    private final SettingsService settings;

    @Operation(summary = "System settings (key/value)")
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> system() {
        return settings.get(SettingScope.SYSTEM);
    }

    @PutMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateSystem(@Valid @RequestBody SettingsRequest r) {
        return settings.put(SettingScope.SYSTEM, r.settings());
    }

    @Operation(summary = "App settings shown to clients (contact details, feature flags)")
    @GetMapping("/app")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> app() {
        return settings.get(SettingScope.APP);
    }

    @PutMapping("/app")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateApp(@Valid @RequestBody SettingsRequest r) {
        return settings.put(SettingScope.APP, r.settings());
    }

    @Operation(summary = "Social-media toggles. Keys: telegram_pr.enabled, discord.enabled, twitter.enabled, post_on_rate_change. Credentials stay in Ops")
    @GetMapping("/social-media")
    @PreAuthorize("hasAnyRole('ADMIN','MARKETING')")
    public Map<String, String> social() {
        return settings.get(SettingScope.SOCIAL);
    }

    @PutMapping("/social-media")
    @PreAuthorize("hasAnyRole('ADMIN','MARKETING')")
    public Map<String, String> updateSocial(@Valid @RequestBody SettingsRequest r) {
        return settings.put(SettingScope.SOCIAL, r.settings());
    }
}
