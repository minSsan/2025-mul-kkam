package backend.mulkkam.common.infrastructure.fcm.domain;

public record TopicTarget(String topic) implements FcmTarget {

    @Override
    public FcmTargetType type() {
        return FcmTargetType.TOPIC;
    }
}
