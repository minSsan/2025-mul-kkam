package backend.mulkkam.common.infrastructure.fcm.service.handler;

import backend.mulkkam.common.exception.AlarmException;
import backend.mulkkam.common.infrastructure.fcm.domain.FcmTargetType;
import backend.mulkkam.common.infrastructure.fcm.domain.TokenTarget;
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
public class TokenFcmHandler implements FcmHandler {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationOutboxCommandService outboxCommandService;

    @Override
    public FcmTargetType getType() {
        return FcmTargetType.TOKEN;
    }

    @Override
    public void send(SendMessageRequest request) {
        TokenTarget target = (TokenTarget) request.target();
        try {
            String messageId = firebaseMessaging.send(Message.builder()
                    .setNotification(Notification.builder().build())
                    .setToken(target.token())
                    .putData("title", request.title())
                    .putData("body", request.body())
                    .putData("action", request.action().name())
                    .build());
            outboxCommandService.markSuccess(request.outboxId());
            log.info("[FCM SUCCESS] token={}, messageId={}, action={}", target.token(), messageId, request.action());
        } catch (FirebaseMessagingException e) {
            outboxCommandService.markFailure(request.outboxId());
            log.error("[FCM FAILED] token={}, errorCode={}, errorMessage={}, action={}", target.token(), e.getMessagingErrorCode(), e.getMessage(), request.action());
            throw new AlarmException(e);
        }
    }
}
