package backend.mulkkam.common.infrastructure.fcm.service.handler;

import backend.mulkkam.common.exception.AlarmException;
import backend.mulkkam.common.infrastructure.fcm.domain.FcmTargetType;
import backend.mulkkam.common.infrastructure.fcm.domain.MulticastTarget;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import backend.mulkkam.notification.service.command.NotificationOutboxCommandService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MulticastFcmHandler implements FcmHandler {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationOutboxCommandService outboxCommandService;

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

            List<SendResponse> responses = batchResponse.getResponses();
            List<Long> successIds = new ArrayList<>();
            List<Long> failedIds = new ArrayList<>();

            for (int i = 0; i < responses.size(); i++) {
                MulticastTarget.OutboxToken pair = target.pairs().get(i);
                SendResponse response = responses.get(i);

                if (response.isSuccessful()) {
                    successIds.add(pair.outboxId());
                } else {
                    failedIds.add(pair.outboxId());
                }
            }

            outboxCommandService.markSuccess(successIds);
            outboxCommandService.markFailure(failedIds);

            log.info("[FCM MULTICAST] successCount={}, failureCount={}, totalCount={}, action={}", batchResponse.getSuccessCount(), batchResponse.getFailureCount(), target.tokens().size(), request.action());
        } catch (FirebaseMessagingException e) {
            List<Long> ids = target.pairs()
                    .stream()
                    .map(MulticastTarget.OutboxToken::outboxId)
                    .toList();
            outboxCommandService.markFailure(ids);

            log.error("[FCM MULTICAST FAILED] tokenCount={}, errorCode={}, errorMessage={}, action={}", target.tokens().size(), e.getMessagingErrorCode(), e.getMessage(), request.action());
            throw new AlarmException(e);
        }
    }
}
