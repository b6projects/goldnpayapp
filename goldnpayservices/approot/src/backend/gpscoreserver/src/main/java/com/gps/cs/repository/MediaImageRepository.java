package com.gps.cs.repository;

import com.gps.cs.entity.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaImageRepository extends JpaRepository<MediaImage, Long> {
    List<MediaImage> findByKindOrderBySortOrderAscIdAsc(MediaKind kind);
    List<MediaImage> findByKindAndActiveTrueOrderBySortOrderAscIdAsc(MediaKind kind);
}
