package backend.mulkkam.common.infrastructure.fcm.service;

import backend.mulkkam.common.infrastructure.fcm.domain.MulticastTarget;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import backend.mulkkam.notification.service.NotificationOutboxLeaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FcmSendFacade {

    private static final int FCM_BATCH_SIZE = 500;

    private final FcmDispatcher dispatcher;
    private final NotificationOutboxLeaseService leaseService;

    public void send(SendMessageRequest request) {
        switch (request.target().type()) {
            case TOKEN, TOPIC -> sendSingleWithClaim(request);
            case MULTICAST -> sendInBatchesWithClaim(request);
        }
    }

    private void sendSingleWithClaim(SendMessageRequest request) {
        Long outboxId = request.outboxId();
        if (outboxId == null) {
            throw new IllegalArgumentException("outbox id가 존재하지 않습니다. ");
        }

        boolean claimed = !leaseService.claim(List.of(outboxId)).isEmpty();
        if (!claimed) {
            return;
        }

        dispatcher.send(request);
    }

    private void sendInBatchesWithClaim(SendMessageRequest request) {
        List<MulticastTarget.OutboxToken> pairs = ((MulticastTarget) request.target()).pairs();
        if (pairs == null) {
            return;
        }

        for (int start = 0; start < pairs.size(); start += FCM_BATCH_SIZE) {
            int end = Math.min(start + FCM_BATCH_SIZE, pairs.size());
            List<MulticastTarget.OutboxToken> batch = pairs.subList(start, end);

            List<Long> ids = batch.stream().map(MulticastTarget.OutboxToken::outboxId).toList();
            Set<Long> leasedIds = new HashSet<>(leaseService.claim(ids));
            if (leasedIds.isEmpty()) continue;

            List<MulticastTarget.OutboxToken> leasedBatch = batch.stream()
                    .filter(p -> leasedIds.contains(p.outboxId()))
                    .toList();

            if (leasedBatch.isEmpty()) continue;

            SendMessageRequest batchedRequest = new SendMessageRequest(
                    request.title(),
                    request.body(),
                    request.action(),
                    new MulticastTarget(leasedBatch),
                    null
            );

            dispatcher.send(batchedRequest);
        }
    }
}
