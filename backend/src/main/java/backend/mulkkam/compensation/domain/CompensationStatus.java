package backend.mulkkam.compensation.domain;

public enum CompensationStatus {
    READY,
    LEASED,
    SUCCESS,
    RETRY_WAITING,
    DEAD,
    CANCELLED
}
