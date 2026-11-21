package com.gps.cs.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;

/** Written by Ops. Core only reads it (link status, admin list). */
@Getter
@Entity
@Table(name = "telegram_links")
public class TelegramLink {

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false, insertable = false)
    private Long userId;

    @Column(name = "chat_id", nullable = false, updatable = false, insertable = false)
    private Long chatId;

    @Column(length = 100, updatable = false, insertable = false)
    private String username;

    @Column(name = "linked_at", nullable = false, updatable = false, insertable = false)
    private Instant linkedAt;
}
