package com.gps.cs.controller.admin;

import com.gps.cs.dto.ClientDtos.KycReviewRequest;
import com.gps.cs.dto.ClientDtos.TelegramUserResponse;
import com.gps.cs.dto.UserDtos.*;
import com.gps.cs.entity.KycDocument;
import com.gps.cs.entity.KycStatus;
import com.gps.cs.entity.Role;
import com.gps.cs.entity.UserStatus;
import com.gps.cs.security.AppPrincipal;
import com.gps.cs.service.KycService;
import com.gps.cs.service.TelegramService;
import com.gps.cs.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - users and KYC")
@RestController
@RequestMapping("/tc/v1/admin")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService users;
    private final KycService kyc;
    private final TelegramService telegram;

    @Operation(summary = "Paged user list with filters")
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Page<UserResponse> list(@RequestParam(required = false) Role role,
                                   @RequestParam(required = false) UserStatus status,
                                   @RequestParam(required = false) KycStatus kycStatus,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
                                   @RequestParam(required = false) String q,
                                   Pageable pageable) {
        return users.list(role, status, kycStatus, createdFrom, createdTo, q, pageable);
    }

    @Operation(summary = "Accounts waiting for activation")
    @GetMapping("/users/pending-activation")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<UserResponse> pendingActivation() {
        return users.pendingActivation();
    }

    @Operation(summary = "User detail")
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserResponse get(@PathVariable Long id) {
        return users.get(id);
    }

    @Operation(summary = "Activate an account")
    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserResponse activate(@PathVariable Long id) {
        return users.activate(id);
    }

    @Operation(summary = "Change a user's role (ADMIN only)")
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id,
                                   @Valid @RequestBody ChangeRoleRequest r) {
        return users.changeRole(me.id(), id, r.role());
    }

    @Operation(summary = "User counts by role and status")
    @GetMapping("/summary/roles")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<RoleSummary> roleSummary() {
        return users.roleSummary();
    }

    @Operation(summary = "Client summary: new, active, KYC pending")
    @GetMapping("/summary/clients")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ClientSummary clientSummary() {
        return users.clientSummary();
    }

    @Operation(summary = "KYC submissions waiting for review")
    @GetMapping("/kyc/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Page<UserResponse> pendingKyc(Pageable pageable) {
        return users.pendingKyc(pageable);
    }

    @Operation(summary = "Download a KYC document")
    @GetMapping("/kyc/{userId}/documents/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Resource> downloadKyc(@PathVariable Long userId, @PathVariable Long docId) {
        KycDocument d = kyc.document(userId, docId);
        Resource body = kyc.content(d);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("kyc-" + d.getId() + "." + d.getContentType().substring(d.getContentType().indexOf('/') + 1)
                                .replace("jpeg", "jpg")).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    @Operation(summary = "Approve or reject KYC; the client is emailed through Ops")
    @PutMapping("/kyc/{userId}/review")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> review(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long userId,
                                       @Valid @RequestBody KycReviewRequest r) {
        kyc.review(me.id(), userId, r);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Linked Telegram users")
    @GetMapping("/telegram/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TelegramUserResponse> telegramUsers(Pageable pageable) {
        return telegram.linkedUsers(pageable);
    }
}
