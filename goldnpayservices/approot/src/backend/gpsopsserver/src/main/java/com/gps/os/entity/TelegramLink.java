package com.gps.os.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** A Telegram chat linked to a GoldNPay user. Written by Ops. */
@Getter
@Setter
@Entity
@Table(name = "telegram_links")
public class TelegramLink extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(length = 100)
    private String username;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;
}
