package backend.mulkkam.compensation.service;

import backend.mulkkam.compensation.domain.OutboxCompensation;
import backend.mulkkam.compensation.repository.OutboxCompensationRepository;
import backend.mulkkam.compensation.service.dto.CompensationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class CompensationEventListener {

    public static final int LEASE_EXPIRE_LIMIT = 30;
    private final OutboxCompensationRepository compensationRepository;
    private final CompensationService compensationService;

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxDead(CompensationEvent event) {
        OutboxCompensation compensation = compensationRepository.findByOutboxId(event.outboxId())
                .orElse(null);

        if (compensation == null) {
            log.info("No compensation needed for outbox {}", event.outboxId());
            return;
        }

        boolean claimed = claim(compensation.getId());
        if (claimed) {
            compensationService.execute(compensation.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(Long compensationId) {
        LocalDateTime nextLeaseExpireAt = LocalDateTime.now().plusSeconds(LEASE_EXPIRE_LIMIT);
        int updated = compensationRepository.tryLease(compensationId, nextLeaseExpireAt);
        return updated > 0;
    }
}
