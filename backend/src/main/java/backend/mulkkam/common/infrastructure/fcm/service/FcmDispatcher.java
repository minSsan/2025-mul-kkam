package backend.mulkkam.common.infrastructure.fcm.service;

import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import backend.mulkkam.common.infrastructure.fcm.service.handler.FcmHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class FcmDispatcher {

    private final List<FcmHandler> handlers;

    public void send(SendMessageRequest request) {
        FcmHandler handler = handlers.stream()
                .filter(h -> h.getType() == request.target().type())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 FCM 타겟 타입입니다: " + request.target().type()));
        handler.send(request);
    }
}
