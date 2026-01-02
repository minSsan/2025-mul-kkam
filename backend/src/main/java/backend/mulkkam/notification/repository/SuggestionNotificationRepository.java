package backend.mulkkam.notification.repository;

import backend.mulkkam.notification.domain.entity.Notification;
import backend.mulkkam.notification.domain.entity.SuggestionNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionNotificationRepository extends JpaRepository<SuggestionNotification, Long> {

    SuggestionNotification getSuggestionNotificationByNotification(Notification notification);

    Optional<SuggestionNotification> findByIdAndNotificationMemberId(Long id, Long notification_member_id);

    void deleteByIdIn(List<Long> id);
}
