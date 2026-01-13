package backend.mulkkam.common.infrastructure.fcm.service.handler;

import backend.mulkkam.common.exception.AlarmException;
import backend.mulkkam.common.infrastructure.fcm.domain.FcmTargetType;
import backend.mulkkam.common.infrastructure.fcm.domain.TopicTarget;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import backend.mulkkam.notification.service.command.NotificationOutboxCommandService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TopicFcmHandler implements FcmHandler {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationOutboxCommandService outboxCommandService;

    @Override
    public FcmTargetType getType() {
        return FcmTargetType.TOPIC;
    }

    @Override
    public void send(SendMessageRequest request) {
        TopicTarget target = (TopicTarget) request.target();
        try {
            String messageId = firebaseMessaging.send(Message.builder()
                    .setNotification(Notification.builder().build())
                    .setTopic(target.topic())
                    .putData("title", request.title())
                    .putData("body", request.body())
                    .putData("action", request.action().name())
                    .build());
            outboxCommandService.markSuccess(request.outboxId());
            log.info("[FCM SUCCESS] topic={}, messageId={}, action={}", target.topic(), messageId, request.action());
        } catch (FirebaseMessagingException e) {
            outboxCommandService.markFailure(request.outboxId());
            log.error("[FCM FAILED] topic={}, errorCode={}, errorMessage={}, action={}", target.topic(), e.getMessagingErrorCode(), e.getMessage(), request.action());
            throw new AlarmException(e);
        }
    }
}
