package backend.mulkkam.common.infrastructure.fcm.dto.request;

import backend.mulkkam.common.infrastructure.fcm.domain.Action;
import backend.mulkkam.common.infrastructure.fcm.domain.FcmTarget;

public record SendMessageRequest(
        String title,
        String body,
        Action action,
        FcmTarget target,
        Long outboxId
) {
}
