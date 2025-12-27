package backend.mulkkam.compensation.service;

import backend.mulkkam.compensation.repository.OutboxCompensationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CompensationLeaseService {

    public static final int LEASE_EXPIRE_LIMIT = 30;

    private final OutboxCompensationRepository compensationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(Long compensationId) {
        LocalDateTime leaseExpiresAt = LocalDateTime.now().plusSeconds(LEASE_EXPIRE_LIMIT);
        return compensationRepository.tryLease(compensationId, leaseExpiresAt) > 0;
    }
}
