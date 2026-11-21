package com.gps.cs.controller.admin;

import com.gps.cs.dto.AdminDtos.PaymentReportRow;
import com.gps.cs.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - reports")
@RestController
@RequestMapping("/tc/v1/admin/reports")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@RequiredArgsConstructor
public class ReportAdminController {
    private final ReportService reports;

    @Operation(summary = "All-payments report (deferred: empty page until the business module adds payments)")
    @GetMapping("/payments")
    public Page<PaymentReportRow> payments(Pageable pageable) {
        return reports.paymentsReport(pageable);
    }
}
