package backend.mulkkam.compensation.domain.entity;

import backend.mulkkam.common.domain.BaseEntity;
import backend.mulkkam.compensation.domain.CompensationStatus;
import backend.mulkkam.compensation.domain.CompensationType;
import backend.mulkkam.notification.domain.ProcessingLease;
import backend.mulkkam.notification.domain.RetryPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxCompensation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long outboxId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompensationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompensationStatus status;

    private LocalDateTime executedAt;

    @Embedded
    private ProcessingLease lease;

    @Embedded
    private RetryPolicy retryPolicy;

    @Builder
    public OutboxCompensation(
            Long outboxId,
            CompensationType type,
            String payload,
            RetryPolicy retryPolicy
    ) {
        this.outboxId = outboxId;
        this.type = type;
        this.payload = payload;
        this.status = CompensationStatus.READY;
        this.lease = new ProcessingLease();
        this.retryPolicy = Objects.requireNonNullElseGet(retryPolicy, RetryPolicy::defaultPolicy);
    }

    public void markAsSuccess() {
        this.status = CompensationStatus.SUCCESS;
        this.executedAt = LocalDateTime.now();
        this.lease.release();
    }

    public void markAsFailed() {
        this.status = CompensationStatus.DEAD;
        this.executedAt = LocalDateTime.now();
        this.retryPolicy.recordFailure();
        this.lease.release();
    }

    public void markAsRetryWaiting(Duration backoff) {
        this.status = CompensationStatus.RETRY_WAITING;
        this.executedAt = LocalDateTime.now();
        this.retryPolicy.scheduleNextAttempt(backoff);
        this.lease.release();
    }

    public boolean canRetry() {
        return retryPolicy.isRemainAttempt();
    }
}
