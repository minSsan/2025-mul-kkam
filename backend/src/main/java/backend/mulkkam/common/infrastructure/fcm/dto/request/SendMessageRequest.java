package backend.mulkkam.common.infrastructure.fcm.dto.request;

import backend.mulkkam.common.infrastructure.fcm.domain.Action;
import backend.mulkkam.common.infrastructure.fcm.domain.FcmTarget;
import backend.mulkkam.notification.dto.NotificationMessageTemplate;

public record SendMessageRequest(
        String title,
        String body,
        Action action,
        FcmTarget target,
        Long outboxId
) {

    public SendMessageRequest(NotificationMessageTemplate template, FcmTarget target, Long outboxId) {
        this(template.title(), template.body(), template.action(), target, outboxId);
    }
}
