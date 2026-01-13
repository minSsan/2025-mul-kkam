package backend.mulkkam.notification.service.command;

import backend.mulkkam.notification.repository.NotificationOutboxRepository;
import backend.mulkkam.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;


}
