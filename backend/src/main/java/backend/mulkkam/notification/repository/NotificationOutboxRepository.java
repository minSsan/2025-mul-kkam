package backend.mulkkam.notification.repository;

import backend.mulkkam.notification.domain.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update NotificationOutbox o
           set o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.SUCCESS,
               o.lease.leasedAt = null, o.lease.leaseExpiresAt = null
         where o.id in :ids
           and o.status <> :success
    """)
    int bulkMarkSuccess(@Param("ids") List<Long> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update NotificationOutbox o
           set o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.RETRY_WAITING,
               o.retryPolicy.attemptCount = o.retryPolicy.attemptCount + 1,
               o.retryPolicy.nextAttemptAt = :nextAttemptAt,
               o.lease.leasedAt = null, o.lease.leaseExpiresAt = null
         where o.id in :ids
           and o.status not in (backend.mulkkam.notification.domain.NotificationOutboxStatus.SUCCESS, backend.mulkkam.notification.domain.NotificationOutboxStatus.FAILED)
           and o.retryPolicy.attemptCount + 1 < o.retryPolicy.maxAttempts
    """)
    int bulkMarkRetryWaiting(
            @Param("ids") List<Long> ids,
            @Param("nextAttemptAt") LocalDateTime nextAttemptAt
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update NotificationOutbox o
           set o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.FAILED,
               o.retryPolicy.attemptCount = o.retryPolicy.attemptCount + 1,
               o.lease.leasedAt = null, o.lease.leaseExpiresAt = null
         where o.id in :ids
           and o.status not in (backend.mulkkam.notification.domain.NotificationOutboxStatus.SUCCESS, backend.mulkkam.notification.domain.NotificationOutboxStatus.FAILED)
           and o.retryPolicy.attemptCount + 1 >= o.retryPolicy.maxAttempts
    """)
    int bulkMarkFailed(@Param("ids") List<Long> ids);

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
