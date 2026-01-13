package backend.mulkkam.compensation.service.dto;

public record RestoreFriendReminderQuotaPayload(
        Long reminderHistoryId,
        short restoreCount
) {
    public RestoreFriendReminderQuotaPayload {
        if (reminderHistoryId == null || reminderHistoryId <= 0) {
            throw new IllegalArgumentException("reminder history 식별자가 올바르지 않습니다. id = " + reminderHistoryId);
        }
        if (restoreCount <= 0) {
            throw new IllegalArgumentException("복구 쿼터 수는 양수여야 합니다. restoreCount = " + restoreCount);
        }
    }
}
