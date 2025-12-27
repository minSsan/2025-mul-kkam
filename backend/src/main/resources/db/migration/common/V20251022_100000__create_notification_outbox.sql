CREATE TABLE notification_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NULL,
    member_id BIGINT NOT NULL,
    device_id BIGINT NULL,
    idempotency_key VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count SMALLINT NOT NULL DEFAULT 0,
    max_attempts SMALLINT NOT NULL DEFAULT 5,
    next_attempt_at DATETIME(6) NOT NULL,
    leased_at DATETIME(6) NULL,
    lease_expires_at DATETIME(6) NULL,
    payload TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,

    CONSTRAINT uq_notification_outbox_idemp UNIQUE (idempotency_key),
    INDEX idx_outbox_poll (status, next_attempt_at),
    INDEX idx_outbox_lease_recovery (status, lease_expires_at)
);
