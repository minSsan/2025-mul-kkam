package backend.mulkkam.notification.repository;

import backend.mulkkam.notification.domain.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Modifying
    @Query("""
            UPDATE NotificationOutbox o
            SET o.status = backend.mulkkam.compensation.domain.CompensationStatus.LEASED,
                o.lease.leasedAt = CURRENT_TIMESTAMP,
                o.lease.leaseExpiresAt = :leaseExpiresAt
            WHERE o.id = :id AND (
                o.status = backend.mulkkam.compensation.domain.CompensationStatus.READY
                OR (
                    o.status = backend.mulkkam.compensation.domain.CompensationStatus.RETRY_WAITING
                    AND (o.retryPolicy.nextAttemptAt IS NULL OR o.retryPolicy.nextAttemptAt <= CURRENT_TIMESTAMP)
                )
                OR (o.status = backend.mulkkam.compensation.domain.CompensationStatus.LEASED AND o.lease.leaseExpiresAt <= CURRENT_TIMESTAMP)
            )
    """)
    int tryLease(@Param("id") Long id, @Param("leaseExpiresAt") LocalDateTime leaseExpiresAt);
}
