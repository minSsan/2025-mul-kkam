package backend.mulkkam.compensation.repository;

import backend.mulkkam.compensation.domain.entity.OutboxCompensation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OutboxCompensationRepository extends JpaRepository<OutboxCompensation, Long> {

    Optional<OutboxCompensation> findByOutboxId(Long outboxId);

    @Modifying
    @Query("""
            UPDATE OutboxCompensation oc
            SET oc.status = backend.mulkkam.compensation.domain.CompensationStatus.LEASED,
                oc.lease.leasedAt = :now,
                oc.lease.leaseExpiresAt = :leaseExpiresAt
            WHERE oc.id = :id AND (
                oc.status = backend.mulkkam.compensation.domain.CompensationStatus.READY
                OR (
                    oc.status = backend.mulkkam.compensation.domain.CompensationStatus.RETRY_WAITING
                    AND oc.retryPolicy.nextAttemptAt <= :now
                )
                OR (oc.status = backend.mulkkam.compensation.domain.CompensationStatus.LEASED AND oc.lease.leaseExpiresAt <= :now)
            )
    """)
    int tryLease(
            @Param("id") Long id,
            @Param("now") LocalDateTime now,
            @Param("leaseExpiresAt") LocalDateTime leaseExpiresAt
    );
}
