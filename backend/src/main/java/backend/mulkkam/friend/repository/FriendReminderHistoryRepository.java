package backend.mulkkam.friend.repository;

import backend.mulkkam.friend.domain.FriendReminderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface FriendReminderHistoryRepository extends JpaRepository<FriendReminderHistory, Long> {

    Optional<FriendReminderHistory> findBySenderIdAndRecipientIdAndQuotaDate(Long senderId, Long recipientId, LocalDate quotaDate);

    @Modifying
    @Query("""
           UPDATE FriendReminderHistory h
           SET h.remaining = h.remaining - 1
           WHERE h.id = :id AND h.remaining > 0
           """)
    int tryReduceRemaining(@Param("id") Long id);

    @Modifying
    @Query("""
           UPDATE FriendReminderHistory h
           SET h.remaining = CASE
                WHEN h.remaining + :restore >= backend.mulkkam.friend.domain.FriendReminderHistory.INIT_REMAINING_VALUE
                    THEN backend.mulkkam.friend.domain.FriendReminderHistory.INIT_REMAINING_VALUE
                ELSE h.remaining + :restore
            END
           WHERE h.id = :id
           """)
    int restoreRemaining(@Param("id") Long id, @Param("restore") short restore);
}
