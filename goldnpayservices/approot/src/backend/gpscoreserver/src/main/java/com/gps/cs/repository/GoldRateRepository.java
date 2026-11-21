package com.gps.cs.repository;

import com.gps.cs.entity.GoldRate;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoldRateRepository extends JpaRepository<GoldRate, Long> {
    Optional<GoldRate> findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDescIdDesc(Instant now);
}
