package backend.mulkkam.compensation.service;

import backend.mulkkam.compensation.domain.CompensationType;
import backend.mulkkam.compensation.domain.OutboxCompensation;

public interface CompensationHandler {
    CompensationType getType();
    void handle(OutboxCompensation compensation);
}
