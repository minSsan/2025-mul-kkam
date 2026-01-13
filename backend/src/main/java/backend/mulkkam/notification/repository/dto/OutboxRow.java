package backend.mulkkam.notification.repository.dto;

public record OutboxRow(
        Long outboxId,
        String token
) {
}
