package com.gps.cs.repository;

import com.gps.cs.entity.BankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUserIdOrderByIdAsc(Long userId);
    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);
}
