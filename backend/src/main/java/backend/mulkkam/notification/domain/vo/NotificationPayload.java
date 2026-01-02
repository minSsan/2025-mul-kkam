package backend.mulkkam.notification.domain.vo;

import backend.mulkkam.common.infrastructure.fcm.domain.Action;
import lombok.Builder;

public record NotificationPayload(
        String title,
        String body,
        Action action
) {

    @Builder
    public NotificationPayload {}
}
