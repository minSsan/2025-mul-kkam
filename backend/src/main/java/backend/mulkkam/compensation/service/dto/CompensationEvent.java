package backend.mulkkam.compensation.service.dto;

import backend.mulkkam.compensation.domain.CompensationType;

public record CompensationEvent(
        Long outboxId,
        CompensationType compensationType
) {
}
