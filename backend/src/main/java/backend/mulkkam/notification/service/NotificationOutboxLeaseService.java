package backend.mulkkam.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import backend.mulkkam.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NotificationOutboxLeaseService {

    public static final int LEASE_EXPIRE_LIMIT_SECONDS = 30;

    private final NotificationOutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> claim(List<Long> outboxIds) {
        if (outboxIds == null || outboxIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime leaseExpiresAt = now.plusSeconds(LEASE_EXPIRE_LIMIT_SECONDS);

        int updated = outboxRepository.tryLeaseBatch(outboxIds, now, leaseExpiresAt);
        if (updated == 0) {
            return List.of();
        }

        return outboxRepository.findLeasedIdsByIdsAndLeaseExpiresAt(outboxIds, leaseExpiresAt);
    }
}
