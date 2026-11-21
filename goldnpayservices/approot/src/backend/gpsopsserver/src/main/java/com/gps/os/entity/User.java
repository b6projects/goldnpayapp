package com.gps.os.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/** Read-only view of Core's users table (name, email, status). Ops never writes it. */
@Getter
@Immutable
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** PENDING_ACTIVATION, ACTIVE, SUSPENDED or DELETED. */
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
