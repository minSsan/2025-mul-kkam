package backend.mulkkam.notification.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import backend.mulkkam.common.infrastructure.fcm.domain.MulticastTarget;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import backend.mulkkam.notification.domain.vo.NotificationPayload;
import backend.mulkkam.notification.repository.NotificationOutboxRepository;
import backend.mulkkam.notification.repository.dto.OutboxDispatchRow;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationOutboxScheduler {

    private static final int CLAIM_SIZE = 500;
    private static final int MULTICAST_BATCH_SIZE = 500;

    private final NotificationOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;

    @Scheduled(fixedDelay = 10000)
    public void run() {
        List<Long> candidateIds = outboxRepository.findReadyOrWaitingIds(
                PageRequest.of(0, CLAIM_SIZE),
                LocalDateTime.now()
        );
        if (candidateIds.isEmpty()) {
            return;
        }

        List<OutboxDispatchRow> rows = outboxRepository.findDispatchRowsByIds(candidateIds);
        if (rows.isEmpty()) {
            return;
        }

        Map<NotificationPayload, List<MulticastTarget.OutboxToken>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        OutboxDispatchRow::payload,
                        Collectors.mapping(
                                r -> new MulticastTarget.OutboxToken(r.outboxId(), r.token()),
                                Collectors.toList()
                        )
                ));

        for (Map.Entry<NotificationPayload, List<MulticastTarget.OutboxToken>> entry : grouped.entrySet()) {
            NotificationPayload key = entry.getKey();
            List<MulticastTarget.OutboxToken> pairs = entry.getValue();

            for (int i = 0; i < pairs.size(); i += MULTICAST_BATCH_SIZE) {
                List<MulticastTarget.OutboxToken> batchPairs =
                        pairs.subList(i, Math.min(i + MULTICAST_BATCH_SIZE, pairs.size()));

                SendMessageRequest request = new SendMessageRequest(
                        key.title(),
                        key.body(),
                        key.action(),
                        new MulticastTarget(batchPairs),
                        null
                );

                publisher.publishEvent(request);
            }
        }
    }
}
