package backend.mulkkam.compensation.service;

import backend.mulkkam.compensation.domain.OutboxCompensation;
import backend.mulkkam.compensation.repository.OutboxCompensationRepository;
import backend.mulkkam.compensation.service.dto.CompensationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class CompensationEventListener {

    private final OutboxCompensationRepository compensationRepository;
    private final CompensationLeaseService compensationLeaseService;
    private final CompensationService compensationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxDead(CompensationEvent event) {
        OutboxCompensation compensation = compensationRepository.findByOutboxId(event.outboxId())
                .orElse(null);

        if (compensation == null) {
            log.info("No compensation needed for outbox {}", event.outboxId());
            return;
        }

        if (compensationLeaseService.claim(compensation.getId())) {
            compensationService.execute(compensation.getId());
        }
    }
}
