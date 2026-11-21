package com.gps.cs.service;

import com.gps.cs.dto.ClientDtos.BankAccountRequest;
import com.gps.cs.dto.ClientDtos.BankAccountResponse;
import com.gps.cs.entity.BankAccount;
import com.gps.cs.exception.ApiException;
import com.gps.cs.mapper.UserMapper;
import com.gps.cs.repository.BankAccountRepository;
import com.gps.cs.util.FieldEncryptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Account numbers are AES-GCM encrypted at rest; responses show the last 4 digits only. */
@Service
@RequiredArgsConstructor
public class BankAccountService {
    private static final int MAX_ACCOUNTS = 5;
    private final BankAccountRepository repo;
    private final FieldEncryptor encryptor;

    @Transactional(readOnly = true)
    public List<BankAccountResponse> list(Long userId) {
        return repo.findByUserIdOrderByIdAsc(userId).stream().map(UserMapper::toResponse).toList();
    }

    @Transactional
    public BankAccountResponse add(Long userId, BankAccountRequest r) {
        List<BankAccount> existing = repo.findByUserIdOrderByIdAsc(userId);
        if (existing.size() >= MAX_ACCOUNTS) {
            throw ApiException.conflict("BANK_ACCOUNT_LIMIT", "At most " + MAX_ACCOUNTS + " bank accounts are allowed");
        }
        BankAccount b = new BankAccount();
        b.setUserId(userId);
        apply(b, r);
        b.setPrimaryAccount(existing.isEmpty());
        return UserMapper.toResponse(repo.save(b));
    }

    @Transactional
    public BankAccountResponse update(Long userId, Long id, BankAccountRequest r) {
        BankAccount b = owned(userId, id);
        apply(b, r);
        return UserMapper.toResponse(repo.save(b));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        BankAccount b = owned(userId, id);
        boolean wasPrimary = b.isPrimaryAccount();
        repo.delete(b);
        repo.flush();
        if (wasPrimary) {
            repo.findByUserIdOrderByIdAsc(userId).stream().findFirst().ifPresent(next -> {
                next.setPrimaryAccount(true);
                repo.save(next);
            });
        }
    }

    @Transactional
    public BankAccountResponse setPrimary(Long userId, Long id) {
        BankAccount target = owned(userId, id);
        repo.findByUserIdOrderByIdAsc(userId).forEach(b -> {
            boolean primary = b.getId().equals(target.getId());
            if (b.isPrimaryAccount() != primary) {
                b.setPrimaryAccount(primary);
                repo.save(b);
            }
        });
        target.setPrimaryAccount(true);
        return UserMapper.toResponse(target);
    }

    private void apply(BankAccount b, BankAccountRequest r) {
        b.setAccountHolder(r.accountHolder().trim());
        b.setAccountNumber(encryptor.encrypt(r.accountNumber()));
        b.setAccountLast4(r.accountNumber().substring(r.accountNumber().length() - 4));
        b.setIfsc(r.ifsc());
        b.setBankName(r.bankName().trim());
    }

    private BankAccount owned(Long userId, Long id) {
        return repo.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Bank account"));
    }
}
