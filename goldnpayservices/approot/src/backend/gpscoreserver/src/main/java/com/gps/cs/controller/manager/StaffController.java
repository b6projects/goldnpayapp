package com.gps.cs.controller.manager;

import com.gps.cs.dto.UserDtos.CreateStaffRequest;
import com.gps.cs.dto.UserDtos.UserResponse;
import com.gps.cs.security.AppPrincipal;
import com.gps.cs.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Staff management")
@RestController
@RequestMapping("/tc/v1/manager/staff")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staff;

    @Operation(summary = "Create a staff user; a welcome email is sent through Ops")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal AppPrincipal me, @Valid @RequestBody CreateStaffRequest r) {
        return staff.create(me, r);
    }

    @Operation(summary = "Suspend a staff user and revoke their tokens")
    @PutMapping("/{id}/suspend")
    public UserResponse suspend(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id) {
        return staff.suspend(me, id);
    }

    @Operation(summary = "Reverse a suspension")
    @PutMapping("/{id}/reinstate")
    public UserResponse reinstate(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id) {
        return staff.reinstate(me, id);
    }

    @Operation(summary = "Soft-delete a staff user (status DELETED; the row is kept for audit)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AppPrincipal me, @PathVariable Long id) {
        staff.delete(me, id);
        return ResponseEntity.noContent().build();
    }
}
