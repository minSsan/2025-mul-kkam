package backend.mulkkam.notification.domain;

import backend.mulkkam.common.domain.BaseEntity;
import backend.mulkkam.notification.domain.converter.SendNotificationRequestConverter;
import backend.mulkkam.notification.domain.vo.SendNotificationRequest;
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

    @Column(nullable = false)
    private Long memberId;

    private Long deviceId;

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
    private SendNotificationRequest payload;

    @Builder
    public NotificationOutbox(
            Long notificationId,
            Long memberId,
            Long deviceId,
            String idempotencyKey,
            SendNotificationRequest payload
    ) {
        this.notificationId = notificationId;
        this.memberId = memberId;
        this.deviceId = deviceId;
        this.idempotencyKey = idempotencyKey;
        this.status = NotificationOutboxStatus.READY;
        this.payload = payload;
        this.lease = new ProcessingLease();
        this.retryPolicy = RetryPolicy.defaultPolicy();
    }
}
