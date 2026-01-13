package backend.mulkkam.notification.service.dto;

public record ChunkResult(
        Long nextLastScheduleId,
        boolean isLastChunk
) {
}
