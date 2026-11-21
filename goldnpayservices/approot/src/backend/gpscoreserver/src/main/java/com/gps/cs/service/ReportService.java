package com.gps.cs.service;

import com.gps.cs.dto.AdminDtos.PaymentReportRow;
import com.gps.cs.dto.AdminDtos.PublicReportResponse;
import com.gps.cs.entity.KycStatus;
import com.gps.cs.entity.Role;
import com.gps.cs.entity.UserStatus;
import com.gps.cs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserRepository users;

    /** Aggregate counts only, no personal data. */
    @Transactional(readOnly = true)
    public PublicReportResponse publicReport() {
        long registered = 0;
        for (UserStatus s : UserStatus.values()) {
            if (s != UserStatus.DELETED) registered += users.countByRoleAndStatus(Role.CLIENT, s);
        }
        return new PublicReportResponse(registered, users.countByRoleAndStatus(Role.CLIENT, UserStatus.ACTIVE),
                users.countByRoleAndKycStatus(Role.CLIENT, KycStatus.APPROVED));
    }

    /** Deferred (D3): returns an empty page until the business module adds payments. */
    public Page<PaymentReportRow> paymentsReport(Pageable pageable) {
        return new PageImpl<>(java.util.List.of(), pageable, 0);
    }
}
