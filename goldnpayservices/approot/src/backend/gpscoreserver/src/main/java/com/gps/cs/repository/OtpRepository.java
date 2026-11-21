package com.gps.cs.repository;

import com.gps.cs.entity.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    long countByUserIdAndPurposeInAndCreatedAtAfter(Long userId, List<OtpPurpose> purposes, Instant after);
    Optional<Otp> findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByIdDesc(Long userId, OtpPurpose purpose);
    List<Otp> findByUserIdAndPurposeAndUsedAtIsNull(Long userId, OtpPurpose purpose);
}
