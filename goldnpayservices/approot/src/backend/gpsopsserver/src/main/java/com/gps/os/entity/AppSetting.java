package com.gps.os.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/** Read-only view of Core's app_settings (also carries the SOCIAL scope toggles). */
@Getter
@Immutable
@Entity
@Table(name = "app_settings")
public class AppSetting {
    @Id
    @Column(name = "setting_key", length = 100)
    private String key;

    @Column(name = "setting_value", length = 2000)
    private String value;

    /** SYSTEM, APP or SOCIAL. */
    @Column(nullable = false, length = 10)
    private String scope;

    @Column(name = "is_public", nullable = false)
    private boolean publicSetting;
}
