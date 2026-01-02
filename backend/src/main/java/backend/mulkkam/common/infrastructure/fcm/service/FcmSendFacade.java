package backend.mulkkam.common.infrastructure.fcm.service;

import backend.mulkkam.common.infrastructure.fcm.domain.MulticastTarget;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FcmSendFacade {

    private static final int FCM_BATCH_SIZE = 500;

    private final FcmDispatcher dispatcher;

    public void send(SendMessageRequest request) {
        switch (request.target().type()) {
            case TOKEN, TOPIC -> dispatcher.send(request);
            case MULTICAST -> sendInBatches(request);
        }
    }

    private void sendInBatches(SendMessageRequest request) {
        List<MulticastTarget.OutboxToken> pairs = ((MulticastTarget) request.target()).pairs();
        if (pairs == null || pairs.isEmpty()) {
            return;
        }

        for (int start = 0; start < pairs.size(); start += FCM_BATCH_SIZE) {
            int end = Math.min(start + FCM_BATCH_SIZE, pairs.size());
            List<MulticastTarget.OutboxToken> batch = pairs.subList(start, end);

            SendMessageRequest batchedRequest = new SendMessageRequest(
                    request.title(),
                    request.body(),
                    request.action(),
                    new MulticastTarget(batch),
                    request.outboxId()
            );

            dispatcher.send(batchedRequest);
        }
    }
}
