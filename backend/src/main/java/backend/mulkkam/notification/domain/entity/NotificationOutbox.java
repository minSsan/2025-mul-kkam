package backend.mulkkam.notification.domain.entity;

import backend.mulkkam.common.domain.BaseEntity;
import backend.mulkkam.notification.domain.NotificationOutboxStatus;
import backend.mulkkam.notification.domain.NotificationTargetType;
import backend.mulkkam.notification.domain.ProcessingLease;
import backend.mulkkam.notification.domain.RetryPolicy;
import backend.mulkkam.notification.domain.converter.SendNotificationRequestConverter;
import backend.mulkkam.notification.domain.vo.NotificationPayload;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uq_notification_outbox_idemp",
        columnNames = {"idempotencyKey"}))
@Entity
public class NotificationOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType targetType;

    @Column(nullable = false)
    private String targetValue;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationOutboxStatus status;

    @Embedded
    private ProcessingLease lease;

    @Embedded
    private RetryPolicy retryPolicy;

    @Convert(converter = SendNotificationRequestConverter.class)
    @Column(columnDefinition = "TEXT")
    private NotificationPayload payload;

    @Builder
    public NotificationOutbox(
            Long notificationId,
            NotificationTargetType targetType,
            String targetValue,
            String idempotencyKey,
            NotificationPayload payload,
            RetryPolicy retryPolicy
    ) {
        this.notificationId = notificationId;
        this.targetType = targetType;
        this.targetValue = targetValue;
        this.idempotencyKey = idempotencyKey;
        this.payload = payload;
        this.retryPolicy = Objects.requireNonNullElseGet(retryPolicy, RetryPolicy::defaultPolicy);
        this.status = NotificationOutboxStatus.READY;
        this.lease = new ProcessingLease();
    }

    public void markAsSuccess() {
        this.status = NotificationOutboxStatus.SUCCESS;
        this.lease.release();
    }

    public void markAsFailed() {
        this.status = NotificationOutboxStatus.FAILED;
        this.retryPolicy.recordFailure();
        this.lease.release();
    }

    public void markAsRetryWaiting(Duration backoff) {
        this.status = NotificationOutboxStatus.RETRY_WAITING;
        this.retryPolicy.scheduleNextAttempt(backoff);
        this.lease.release();
    }

    public boolean canRetry() {
        return retryPolicy.isRemainAttempt();
    }
}
