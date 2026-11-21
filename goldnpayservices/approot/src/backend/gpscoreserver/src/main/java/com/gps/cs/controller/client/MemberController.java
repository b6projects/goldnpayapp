package com.gps.cs.controller.client;

import com.gps.cs.dto.ClientDtos.*;
import com.gps.cs.dto.UserDtos.ChangePasswordRequest;
import com.gps.cs.dto.UserDtos.UpdateProfileRequest;
import com.gps.cs.dto.UserDtos.UserResponse;
import com.gps.cs.entity.KycDocType;
import com.gps.cs.security.AppPrincipal;
import com.gps.cs.service.BankAccountService;
import com.gps.cs.service.KycService;
import com.gps.cs.service.TelegramService;
import com.gps.cs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Client self-service")
@RestController
@RequestMapping("/tc/v1/client")
@PreAuthorize("hasRole('CLIENT')")
@RequiredArgsConstructor
public class MemberController {
    private final UserService users;
    private final KycService kyc;
    private final BankAccountService banks;
    private final TelegramService telegram;

    @Operation(summary = "Read my profile")
    @GetMapping("/profile")
    public UserResponse profile(@AuthenticationPrincipal AppPrincipal me) {
        return users.profile(me.id());
    }

    @Operation(summary = "Update my profile (the email cannot be changed here)")
    @PutMapping("/profile")
    public UserResponse updateProfile(@AuthenticationPrincipal AppPrincipal me, @Valid @RequestBody UpdateProfileRequest r) {
        return users.updateProfile(me.id(), r);
    }

    @Operation(summary = "Change my password; signs out all sessions")
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AppPrincipal me,
                                               @Valid @RequestBody ChangePasswordRequest r) {
        users.changePassword(me.id(), r);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload KYC documents (PDF/JPG/PNG, max 5 MB each); one docType per file")
    @PostMapping(value = "/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KycStatusResponse uploadKyc(@AuthenticationPrincipal AppPrincipal me,
                                       @RequestParam("docTypes") List<KycDocType> docTypes,
                                       @RequestPart("files") List<MultipartFile> files) {
        return kyc.submit(me.id(), docTypes, files);
    }

    @Operation(summary = "KYC status and document list (no file contents)")
    @GetMapping("/kyc")
    public KycStatusResponse kycStatus(@AuthenticationPrincipal AppPrincipal me) {
        return kyc.status(me.id());
    }

    @Operation(summary = "List my bank accounts (masked)")
    @GetMapping("/bank-accounts")
    public List<BankAccountResponse> bankAccounts(@AuthenticationPrincipal AppPrincipal me) {
        return banks.list(me.id());
    }

    @Operation(summary = "Add a bank account")
    @PostMapping("/bank-accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountResponse addBank(@AuthenticationPrincipal AppPrincipal me, @Valid @RequestBody BankAccountRequest r) {
        return banks.add(me.id(), r);
    }

    @Operation(summary = "Update a bank account")
    @PutMapping("/bank-accounts/{id}")
    public BankAccountResponse updateBank(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id,
                                          @Valid @RequestBody BankAccountRequest r) {
        return banks.update(me.id(), id, r);
    }

    @Operation(summary = "Remove a bank account")
    @DeleteMapping("/bank-accounts/{id}")
    public ResponseEntity<Void> deleteBank(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id) {
        banks.delete(me.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set the primary bank account")
    @PutMapping("/bank-accounts/{id}/primary")
    public BankAccountResponse primaryBank(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id) {
        return banks.setPrimary(me.id(), id);
    }

    @Operation(summary = "Issue a one-time code for linking Telegram")
    @PostMapping("/telegram/link-code")
    @ResponseStatus(HttpStatus.CREATED)
    public LinkCodeResponse linkCode(@AuthenticationPrincipal AppPrincipal me) {
        return telegram.issueCode(me.id());
    }

    @Operation(summary = "Telegram link status")
    @GetMapping("/telegram")
    public TelegramStatusResponse telegramStatus(@AuthenticationPrincipal AppPrincipal me) {
        return telegram.status(me.id());
    }

    @Operation(summary = "Disconnect Telegram (Core asks Ops to unlink)")
    @DeleteMapping("/telegram")
    public ResponseEntity<Void> disconnectTelegram(@AuthenticationPrincipal AppPrincipal me) {
        telegram.disconnect(me.id());
        return ResponseEntity.noContent().build();
    }
}
