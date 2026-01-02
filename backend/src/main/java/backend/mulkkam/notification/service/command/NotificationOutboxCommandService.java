package backend.mulkkam.notification.service.command;

import backend.mulkkam.notification.domain.NotificationTargetType;
import backend.mulkkam.notification.domain.entity.NotificationOutbox;
import backend.mulkkam.notification.domain.vo.NotificationPayload;
import backend.mulkkam.notification.repository.NotificationOutboxRepository;
import backend.mulkkam.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class NotificationOutboxCommandService {

    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(10);

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;

    @Transactional
    public NotificationOutbox create(
            Long notificationId,
            NotificationTargetType targetType,
            String targetValue,
            NotificationPayload payload
    ) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalArgumentException("Notification does not exist: id = " + notificationId);
        }
        NotificationOutbox notificationOutbox = NotificationOutbox.builder()
                .notificationId(notificationId)
                .targetType(targetType)
                .targetValue(targetValue)
                .payload(payload)
                .build();
        return outboxRepository.save(notificationOutbox);
    }

    @Transactional
    public void markFailure(Long outboxId) {
        NotificationOutbox notificationOutbox = outboxRepository.findById(outboxId)
                .orElseThrow();
        if (notificationOutbox.canRetry()) {
            notificationOutbox.markAsRetryWaiting(RETRY_BACKOFF);
        } else {
            notificationOutbox.markAsFailed();
        }
    }

    @Transactional
    public void markSuccess(Long outboxId) {
        NotificationOutbox notificationOutbox = outboxRepository.findById(outboxId)
                .orElseThrow();
        notificationOutbox.markAsSuccess();
    }
}
