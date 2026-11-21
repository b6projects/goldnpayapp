package com.gps.cs.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gold_rates")
public class GoldRate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_24k", nullable = false, precision = 19, scale = 2)
    private BigDecimal rate24k;

    @Column(name = "rate_22k", nullable = false, precision = 19, scale = 2)
    private BigDecimal rate22k;

    @Column(name = "rate_18k", nullable = false, precision = 19, scale = 2)
    private BigDecimal rate18k;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(nullable = false)
    private boolean paused;

    @Column(length = 50)
    private String source;
}
