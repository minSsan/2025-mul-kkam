package backend.mulkkam.common.infrastructure.fcm.domain;

import java.util.List;

public record MulticastTarget(List<OutboxToken> pairs) implements FcmTarget {

    @Override
    public FcmTargetType type() {
        return FcmTargetType.MULTICAST;
    }

    public List<String> tokens() {
        return pairs.stream().map(OutboxToken::token).toList();
    }

    public record OutboxToken(Long outboxId, String token) {}
}
