package backend.mulkkam.compensation.service;

import backend.mulkkam.compensation.domain.entity.OutboxCompensation;
import backend.mulkkam.compensation.service.query.OutboxCompensationQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompensationService {

    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(30);

    private final List<CompensationHandler> handlers;
    private final OutboxCompensationQueryService compensationQueryService;

    @Transactional
    public void execute(Long compensationId) {
        OutboxCompensation compensation = compensationQueryService.get(compensationId);
        CompensationHandler handler = handlers.stream()
                .filter(h -> h.getType() == compensation.getType())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("해당 타입의 보상 로직을 처리할 핸들러가 존재하지 않음 : " + compensation.getType()));
        try {
            handler.handle(compensation);
            compensation.markAsSuccess();
        } catch (Exception e) {
            log.warn("Compensation execution failed for outboxId={}, type={}", compensation.getOutboxId(), compensation.getType(), e);
            handleFailure(compensation);
        }
    }

    private void handleFailure(OutboxCompensation compensation) {
        if (compensation.canRetry()) {
            compensation.markAsRetryWaiting(RETRY_BACKOFF);
        } else {
            compensation.markAsFailed();
        }
    }
}
