package com.gps.cs.repository;

import com.gps.cs.entity.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRole(Role role);

    @Query("select u.role, u.status, count(u) from User u group by u.role, u.status")
    List<Object[]> countByRoleAndStatus();

    long countByRoleAndStatus(Role role, UserStatus status);
    long countByRoleAndKycStatus(Role role, KycStatus status);
    long countByRoleAndCreatedAtAfter(Role role, Instant after);
    List<User> findByStatusOrderByCreatedAtAsc(UserStatus status);
}
