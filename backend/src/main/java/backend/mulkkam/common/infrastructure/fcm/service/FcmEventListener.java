package backend.mulkkam.common.infrastructure.fcm.service;

import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageByFcmTokenRequest;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageByFcmTokensRequest;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageByFcmTopicRequest;
import backend.mulkkam.common.infrastructure.fcm.dto.request.SendMessageRequest;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Async
@RequiredArgsConstructor
@Component
public class FcmEventListener {

    private static final int FCM_BATCH_SIZE = 500;
    private final FcmClient fcmClient;
    private final FcmSendFacade fcmSendFacade;

    @Deprecated
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTopic(SendMessageByFcmTopicRequest sendMessageByFcmTopicRequest) {
        fcmClient.sendMessageByTopic(sendMessageByFcmTopicRequest);
    }

    @Deprecated
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onToken(SendMessageByFcmTokenRequest sendMessageByFcmTokenRequest) {
        fcmClient.sendMessageByToken(sendMessageByFcmTokenRequest);
    }

    @Deprecated
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTokens(SendMessageByFcmTokensRequest sendMessageByFcmTokensRequest) {
        Lists.partition(sendMessageByFcmTokensRequest.allTokens(), FCM_BATCH_SIZE)
                .forEach(tokens -> {
                    fcmClient.sendMulticast(sendMessageByFcmTokensRequest.withTokens(tokens));
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFcmEvent(SendMessageRequest request) {
        fcmSendFacade.send(request);
    }
}
