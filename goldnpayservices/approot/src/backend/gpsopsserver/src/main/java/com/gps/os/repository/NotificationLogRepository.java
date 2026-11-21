package com.gps.os.repository;

import com.gps.os.entity.DeliveryStatus;
import com.gps.os.entity.NotificationLog;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByRequestId(String requestId);
    long countByUserIdAndCategoryAndStatusIn(Long userId, String category, Collection<DeliveryStatus> statuses);
    long countByUserIdAndCategory(Long userId, String category);
    Optional<NotificationLog> findFirstByUserIdAndCategoryOrderByIdDesc(Long userId, String category);
}
