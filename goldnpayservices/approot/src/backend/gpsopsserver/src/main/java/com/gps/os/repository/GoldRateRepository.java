package com.gps.os.repository;

import com.gps.os.entity.GoldRate;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoldRateRepository extends JpaRepository<GoldRate, Long> {
    Optional<GoldRate> findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDescIdDesc(Instant now);
}
