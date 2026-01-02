package backend.mulkkam.common.infrastructure.fcm.service.handler;

import backend.mulkkam.common.infrastructure.fcm.domain.FcmTargetType;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;

public interface FcmHandler {
    FcmTargetType getType();
    void send(SendMessageRequest request);
}
