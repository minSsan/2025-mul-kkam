package backend.mulkkam.common.infrastructure.fcm.domain;

public record TokenTarget(String token) implements FcmTarget {

    @Override
    public FcmTargetType type() {
        return FcmTargetType.TOKEN;
    }
}
