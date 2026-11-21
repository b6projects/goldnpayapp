package com.gps.os.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One row per send attempt sequence. request_id is the idempotency key. */
@Getter
@Setter
@Entity
@Table(name = "notification_logs")
public class NotificationLog extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Channel channel;

    @Column(length = 40)
    private String category;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 500)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private DeliveryStatus status = DeliveryStatus.QUEUED;

    @Column(length = 1000)
    private String error;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "sent_at")
    private Instant sentAt;
}
