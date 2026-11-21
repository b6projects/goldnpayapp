package com.gps.cs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bank_accounts")
public class BankAccount extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_holder", nullable = false, length = 150)
    private String accountHolder;

    /** AES-GCM encrypted, Base64. */
    @Column(name = "account_number", nullable = false, length = 512)
    private String accountNumber;

    @Column(name = "account_last4", nullable = false, length = 4)
    private String accountLast4;

    @Column(nullable = false, length = 11)
    private String ifsc;

    @Column(name = "bank_name", nullable = false, length = 150)
    private String bankName;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryAccount;
}
