package com.gps.os.repository;

import com.gps.os.entity.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Read-only use: Ops never saves users. */
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatusAndCreatedAtBeforeOrderByIdAsc(String status, Instant before, Pageable pageable);
}
