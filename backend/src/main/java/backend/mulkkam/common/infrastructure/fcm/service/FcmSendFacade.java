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
        List<String> tokens = ((MulticastTarget) request.target()).tokens();
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        for (int start = 0; start < tokens.size(); start += FCM_BATCH_SIZE) {
            int end = Math.min(start + FCM_BATCH_SIZE, tokens.size());
            List<String> batch = tokens.subList(start, end);

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
