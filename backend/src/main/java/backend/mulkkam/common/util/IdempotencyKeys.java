package backend.mulkkam.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class IdempotencyKeys {

    private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final String TYPE_REMIND = "REMIND";

    private IdempotencyKeys() {}

    public static String remind(Long memberId, Long deviceId, LocalDateTime now) {
        requirePositive(memberId, "memberId");
        requirePositive(deviceId, "deviceId");
        Objects.requireNonNull(now, "now must not be null");

        LocalDateTime bucket = normalizeToMinute(now);
        String timePart = bucket.format(YYYYMMDDHHMM);

        // "REMIND:{memberId}:{deviceId}:{yyyyMMddHHmm}"
        return TYPE_REMIND + ":" + memberId + ":" + deviceId + ":" + timePart;
    }

    public static LocalDateTime normalizeToMinute(LocalDateTime dt) {
        return dt.withSecond(0).withNano(0);
    }

    private static void requirePositive(Long value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive. value=" + value);
        }
    }
}
