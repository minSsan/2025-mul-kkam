package backend.mulkkam.common.infrastructure.fcm.service.handler;

import backend.mulkkam.common.exception.AlarmException;
import backend.mulkkam.common.infrastructure.fcm.domain.FcmTargetType;
import backend.mulkkam.common.infrastructure.fcm.domain.MulticastTarget;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MulticastFcmHandler implements FcmHandler {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public FcmTargetType getType() {
        return FcmTargetType.MULTICAST;
    }

    @Override
    public void send(SendMessageRequest request) {
        MulticastTarget target = (MulticastTarget) request.target();
        try {
            BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(MulticastMessage.builder()
                    .addAllTokens(target.tokens())
                    .putData("title", request.title())
                    .putData("body", request.body())
                    .putData("action", request.action().name())
                    .build());
            log.info("[FCM MULTICAST] successCount={}, failureCount={}, totalCount={}, action={}", batchResponse.getSuccessCount(), batchResponse.getFailureCount(), target.tokens().size(), request.action());
        } catch (FirebaseMessagingException e) {
            // TODO: 실패 여부 기록 필요 - target.tokens() 순회하면서 각각 따로 기록 (TOKEN 타입으로)
            log.error("[FCM MULTICAST FAILED] tokenCount={}, errorCode={}, errorMessage={}, action={}", target.tokens().size(), e.getMessagingErrorCode(), e.getMessage(), request.action());
            throw new AlarmException(e);
        }
    }
}
