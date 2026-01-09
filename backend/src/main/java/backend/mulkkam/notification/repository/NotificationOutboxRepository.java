package backend.mulkkam.notification.repository;

import backend.mulkkam.notification.domain.entity.NotificationOutbox;
import backend.mulkkam.notification.repository.dto.OutboxDispatchRow;
import org.springframework.data.domain.Pageable;
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
           and o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.LEASED
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
           and o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.LEASED
           and o.retryPolicy.attemptCount + 1 >= o.retryPolicy.maxAttempts
    """)
    int bulkMarkFailed(@Param("ids") List<Long> ids);

    @Query("""
        SELECT o.id
        FROM NotificationOutbox o
        WHERE
            o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.READY
            OR (
                o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.RETRY_WAITING
                AND o.retryPolicy.nextAttemptAt <= :now
            )
        ORDER BY o.id ASC
    """)
    List<Long> findReadyOrWaitingIds(Pageable pageable, @Param("now") LocalDateTime now);

    @Query("""
        SELECT o.id AS outboxId, o.targetValue AS token, o.payload AS payload
        FROM NotificationOutbox o
        WHERE o.id in :ids
    """)
    List<OutboxDispatchRow> findDispatchRowsByIds(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE NotificationOutbox o
        SET o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.LEASED,
            o.lease.leasedAt = :now,
            o.lease.leaseExpiresAt = :leaseExpiresAt
        WHERE o.id IN :ids
          AND (
              o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.READY
              OR (
                  o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.RETRY_WAITING
                  AND o.retryPolicy.nextAttemptAt <= :now
              )
              OR (
                  o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.LEASED
                  AND o.lease.leaseExpiresAt <= :now
              )
          )
    """)
    int tryLeaseBatch(
            @Param("ids") List<Long> ids,
            @Param("now") LocalDateTime now,
            @Param("leaseExpiresAt") LocalDateTime leaseExpiresAt
    );

    @Query("""
        SELECT o.id
        FROM NotificationOutbox o
        WHERE o.id IN :ids
          AND o.status = backend.mulkkam.notification.domain.NotificationOutboxStatus.LEASED
          AND o.lease.leaseExpiresAt = :leaseExpiresAt
    """)
    List<Long> findLeasedIdsByIdsAndLeaseExpiresAt(
            @Param("ids") List<Long> ids,
            @Param("leaseExpiresAt") LocalDateTime leaseExpiresAt
    );
}
