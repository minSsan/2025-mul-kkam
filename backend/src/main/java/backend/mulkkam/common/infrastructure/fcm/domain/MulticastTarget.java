package backend.mulkkam.common.infrastructure.fcm.domain;

import java.util.List;

public record MulticastTarget(List<String> tokens) implements FcmTarget {

    @Override
    public FcmTargetType type() {
        return FcmTargetType.MULTICAST;
    }
}
