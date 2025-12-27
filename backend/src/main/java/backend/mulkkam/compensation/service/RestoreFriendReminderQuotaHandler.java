package backend.mulkkam.compensation.service;

import backend.mulkkam.compensation.domain.CompensationType;
import backend.mulkkam.compensation.domain.OutboxCompensation;
import backend.mulkkam.compensation.service.dto.RestoreFriendReminderQuotaPayload;
import backend.mulkkam.friend.service.command.FriendReminderHistoryCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RestoreFriendReminderQuotaHandler implements CompensationHandler {

    private final ObjectMapper objectMapper;
    private final FriendReminderHistoryCommandService friendReminderHistoryCommandService;

    @Override
    public CompensationType getType() {
        return CompensationType.RESTORE_QUOTA;
    }

    @Override
    @Transactional
    public void handle(OutboxCompensation compensation) {
        RestoreFriendReminderQuotaPayload payload = deserialize(compensation.getPayload());
        short restoreCount = payload.restoreCount();
        friendReminderHistoryCommandService.restoreRemainingCount(payload.reminderHistoryId(), restoreCount);
    }

    private RestoreFriendReminderQuotaPayload deserialize(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, RestoreFriendReminderQuotaPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid friend reminder restore payload: {}", rawPayload, e);
            throw new IllegalArgumentException("올바르지 않은 친구 리마인더 복구 페이로드", e);
        }
    }
}
