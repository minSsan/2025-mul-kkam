CREATE TABLE outbox_compensation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbox_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    executed_at DATETIME(6) NULL,
    attempt_count SMALLINT NOT NULL DEFAULT 0,
    max_attempts SMALLINT NOT NULL DEFAULT 5,
    next_attempt_at DATETIME(6) NOT NULL,
    leased_at DATETIME(6) NULL,
    lease_expires_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,

    CONSTRAINT uq_outbox_compensation_outbox_id UNIQUE (outbox_id),
    CONSTRAINT fk_compensation_outbox FOREIGN KEY (outbox_id) REFERENCES notification_outbox(id),
    INDEX idx_compensation_poll (status, next_attempt_at),
    INDEX idx_compensation_lease_recovery (status, lease_expires_at)
);
