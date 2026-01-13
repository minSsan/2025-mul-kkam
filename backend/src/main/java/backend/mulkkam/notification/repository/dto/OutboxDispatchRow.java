package backend.mulkkam.notification.repository.dto;

import backend.mulkkam.notification.domain.vo.NotificationPayload;

public record OutboxDispatchRow(
        Long outboxId,
        String token,
        NotificationPayload payload
) {
}
