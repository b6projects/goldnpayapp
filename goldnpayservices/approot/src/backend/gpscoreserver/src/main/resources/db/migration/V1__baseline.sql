-- GoldNPay baseline schema (spec section 8). Core owns the schema; Ops only validates it.

CREATE TABLE users (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                 VARCHAR(190) NOT NULL,
    password_hash         VARCHAR(100) NOT NULL,
    full_name             VARCHAR(150) NOT NULL,
    phone                 VARCHAR(20),
    address               VARCHAR(500),
    role                  VARCHAR(20)  NOT NULL,
    status                VARCHAR(30)  NOT NULL,
    kyc_status            VARCHAR(20)  NOT NULL,
    failed_login_count    INT          NOT NULL DEFAULT 0,
    last_failed_login_at  DATETIME(6),
    locked_until          DATETIME(6),
    created_at            DATETIME(6)  NOT NULL,
    created_by            VARCHAR(100),
    updated_at            DATETIME(6),
    updated_by            VARCHAR(100),
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE INDEX idx_users_role_status ON users (role, status);

CREATE TABLE tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,
    type        VARCHAR(10) NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    expired     BOOLEAN     NOT NULL DEFAULT FALSE,
    expires_at  DATETIME(6) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    created_by  VARCHAR(100),
    updated_at  DATETIME(6),
    updated_by  VARCHAR(100),
    CONSTRAINT uk_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_tokens_user ON tokens (user_id);

CREATE TABLE otps (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    purpose     VARCHAR(30) NOT NULL,
    code_hash   VARCHAR(64) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    used_at     DATETIME(6),
    attempts    INT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6) NOT NULL,
    created_by  VARCHAR(100),
    updated_at  DATETIME(6),
    updated_by  VARCHAR(100),
    CONSTRAINT fk_otps_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_otps_user_purpose ON otps (user_id, purpose);

CREATE TABLE kyc_documents (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    doc_type      VARCHAR(30)  NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    reviewed_by   BIGINT,
    reviewed_at   DATETIME(6),
    reject_reason VARCHAR(500),
    created_at    DATETIME(6)  NOT NULL,
    created_by    VARCHAR(100),
    updated_at    DATETIME(6),
    updated_by    VARCHAR(100),
    CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_kyc_user ON kyc_documents (user_id);

CREATE TABLE bank_accounts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    account_holder  VARCHAR(150) NOT NULL,
    account_number  VARCHAR(512) NOT NULL,
    account_last4   VARCHAR(4)   NOT NULL,
    ifsc            VARCHAR(11)  NOT NULL,
    bank_name       VARCHAR(150) NOT NULL,
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(100),
    updated_at      DATETIME(6),
    updated_by      VARCHAR(100),
    CONSTRAINT fk_bank_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_bank_user ON bank_accounts (user_id);

CREATE TABLE telegram_link_codes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    code_hash   VARCHAR(64) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    used_at     DATETIME(6),
    created_at  DATETIME(6) NOT NULL,
    created_by  VARCHAR(100),
    updated_at  DATETIME(6),
    updated_by  VARCHAR(100),
    CONSTRAINT uk_tglc_hash UNIQUE (code_hash),
    CONSTRAINT fk_tglc_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE telegram_links (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    chat_id     BIGINT       NOT NULL,
    username    VARCHAR(100),
    linked_at   DATETIME(6)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100),
    updated_at  DATETIME(6),
    updated_by  VARCHAR(100),
    CONSTRAINT uk_tgl_user UNIQUE (user_id),
    CONSTRAINT uk_tgl_chat UNIQUE (chat_id),
    CONSTRAINT fk_tgl_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE gold_rates (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    rate_24k      DECIMAL(19,2) NOT NULL,
    rate_22k      DECIMAL(19,2) NOT NULL,
    rate_18k      DECIMAL(19,2) NOT NULL,
    currency      VARCHAR(3)    NOT NULL,
    effective_at  DATETIME(6)   NOT NULL,
    paused        BOOLEAN       NOT NULL DEFAULT FALSE,
    source        VARCHAR(50),
    created_at    DATETIME(6)   NOT NULL,
    created_by    VARCHAR(100),
    updated_at    DATETIME(6),
    updated_by    VARCHAR(100)
);
CREATE INDEX idx_gold_rates_effective ON gold_rates (effective_at);

CREATE TABLE app_settings (
    setting_key    VARCHAR(100)  NOT NULL PRIMARY KEY,
    setting_value  VARCHAR(2000),
    scope          VARCHAR(10)   NOT NULL,
    is_public      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6)   NOT NULL,
    created_by     VARCHAR(100),
    updated_at     DATETIME(6),
    updated_by     VARCHAR(100)
);

CREATE TABLE media_images (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    kind          VARCHAR(10)  NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    title         VARCHAR(200),
    sort_order    INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    created_by    VARCHAR(100),
    updated_at    DATETIME(6),
    updated_by    VARCHAR(100)
);
CREATE INDEX idx_media_kind ON media_images (kind, active);

CREATE TABLE notification_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id  VARCHAR(64)   NOT NULL,
    channel     VARCHAR(10)   NOT NULL,
    category    VARCHAR(40),
    user_id     BIGINT,
    recipient   VARCHAR(500),
    status      VARCHAR(10)   NOT NULL,
    error       VARCHAR(1000),
    attempts    INT           NOT NULL DEFAULT 0,
    sent_at     DATETIME(6),
    created_at  DATETIME(6)   NOT NULL,
    created_by  VARCHAR(100),
    updated_at  DATETIME(6),
    updated_by  VARCHAR(100),
    CONSTRAINT uk_notif_request UNIQUE (request_id)
);
CREATE INDEX idx_notif_user_cat ON notification_logs (user_id, category);

CREATE TABLE audit_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id     BIGINT,
    action       VARCHAR(80)  NOT NULL,
    target_type  VARCHAR(50),
    target_id    VARCHAR(64),
    detail_json  VARCHAR(4000),
    occurred_at  DATETIME(6)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    created_by   VARCHAR(100),
    updated_at   DATETIME(6),
    updated_by   VARCHAR(100)
);

-- ShedLock (used by Ops schedulers); fixed layout required by the library.
CREATE TABLE shedlock (
    name        VARCHAR(64)  NOT NULL PRIMARY KEY,
    lock_until  TIMESTAMP(3) NOT NULL,
    locked_at   TIMESTAMP(3) NOT NULL,
    locked_by   VARCHAR(255) NOT NULL
);
