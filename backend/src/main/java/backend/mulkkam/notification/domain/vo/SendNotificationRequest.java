package backend.mulkkam.notification.domain.vo;

import backend.mulkkam.common.infrastructure.fcm.domain.Action;
import backend.mulkkam.notification.domain.NotificationTargetType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SendNotificationRequest {

    private String title;
    private String body;
    private Action action;
    private NotificationTargetType targetType;
    private String targetValue;
}
