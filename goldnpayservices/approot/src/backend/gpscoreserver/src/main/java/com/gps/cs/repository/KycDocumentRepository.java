package com.gps.cs.repository;

import com.gps.cs.entity.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByUserIdOrderByIdDesc(Long userId);
    Optional<KycDocument> findByIdAndUserId(Long id, Long userId);
    List<KycDocument> findByUserIdAndStatus(Long userId, KycStatus status);
}
