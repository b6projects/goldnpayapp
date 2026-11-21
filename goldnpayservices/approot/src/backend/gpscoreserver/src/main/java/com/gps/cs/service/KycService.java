package com.gps.cs.service;

import com.gps.cs.dto.ClientDtos.KycStatusResponse;
import com.gps.cs.dto.ClientDtos.KycReviewRequest;
import com.gps.cs.entity.*;
import com.gps.cs.exception.ApiException;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.mapper.UserMapper;
import com.gps.cs.repository.KycDocumentRepository;
import com.gps.cs.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class KycService {
    private static final int MAX_FILES_PER_REQUEST = 5;

    private final KycDocumentRepository docs;
    private final UserRepository users;
    private final FileStorageService storage;
    private final OpsApiService ops;
    private final EmailContentFactory emails;
    private final AuditService audit;

    @Transactional
    public KycStatusResponse submit(Long userId, List<KycDocType> types, List<MultipartFile> files) {
        if (files == null || files.isEmpty() || types == null || types.size() != files.size()) {
            throw ApiException.badRequest("KYC_INVALID_REQUEST", "Provide one docType per file");
        }
        if (files.size() > MAX_FILES_PER_REQUEST) {
            throw ApiException.badRequest("KYC_TOO_MANY_FILES", "At most " + MAX_FILES_PER_REQUEST + " files per upload");
        }
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (u.getKycStatus() == KycStatus.APPROVED) {
            throw ApiException.conflict("KYC_ALREADY_APPROVED", "KYC is already approved");
        }
        List<String> saved = new ArrayList<>();
        try {
            for (int i = 0; i < files.size(); i++) {
                FileStorageService.Stored s = storage.save(files.get(i), storage.kycDir(userId), false);
                saved.add(s.relativePath());
                KycDocument d = new KycDocument();
                d.setUserId(userId);
                d.setDocType(types.get(i));
                d.setFilePath(s.relativePath());
                d.setContentType(s.contentType());
                d.setSizeBytes(s.size());
                d.setStatus(KycStatus.SUBMITTED);
                docs.save(d);
            }
        } catch (RuntimeException e) {
            saved.forEach(storage::deleteQuietly); // do not leave orphan files behind
            throw e;
        }
        u.setKycStatus(KycStatus.SUBMITTED);
        users.save(u);
        return status(userId);
    }

    @Transactional(readOnly = true)
    public KycStatusResponse status(Long userId) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        return new KycStatusResponse(u.getKycStatus(),
                docs.findByUserIdOrderByIdDesc(userId).stream().map(UserMapper::toResponse).toList());
    }

    @Transactional
    public void review(Long reviewerId, Long userId, KycReviewRequest r) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (u.getKycStatus() != KycStatus.SUBMITTED) {
            throw ApiException.conflict("KYC_NOT_PENDING", "No KYC submission is waiting for review");
        }
        if (!r.approve() && (r.reason() == null || r.reason().isBlank())) {
            throw ApiException.badRequest("KYC_REASON_REQUIRED", "A reason is required when rejecting");
        }
        KycStatus outcome = r.approve() ? KycStatus.APPROVED : KycStatus.REJECTED;
        Instant now = Instant.now();
        for (KycDocument d : docs.findByUserIdAndStatus(userId, KycStatus.SUBMITTED)) {
            d.setStatus(outcome);
            d.setReviewedBy(reviewerId);
            d.setReviewedAt(now);
            d.setRejectReason(r.approve() ? null : r.reason());
            docs.save(d);
        }
        u.setKycStatus(outcome);
        users.save(u);
        audit.log(r.approve() ? "KYC_APPROVED" : "KYC_REJECTED", "User", userId, null);
        ops.sendEmailAsync(emails.kycResult(u, r.approve(), r.reason()));
    }

    /** Admin/manager download. The file is only reachable through this authenticated path. */
    @Transactional(readOnly = true)
    public KycDocument document(Long userId, Long docId) {
        return docs.findByIdAndUserId(docId, userId).orElseThrow(() -> ApiException.notFound("Document"));
    }

    public Resource content(KycDocument d) {
        return storage.load(d.getFilePath());
    }
}
