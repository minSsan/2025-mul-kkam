package backend.mulkkam.notification.domain;

public enum NotificationOutboxStatus {
    READY,
    LEASED,
    RETRY_WAITING,
    SUCCESS,
    FAILED,
    CANCELLED
}
