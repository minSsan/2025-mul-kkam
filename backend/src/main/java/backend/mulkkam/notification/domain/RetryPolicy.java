package backend.mulkkam.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RetryPolicy {

    @Column(nullable = false)
    private int attemptCount = 0;

    @Column(nullable = false)
    private int maxAttempts = 5;

    private LocalDateTime nextAttemptAt;

    private RetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.nextAttemptAt = LocalDateTime.now();
    }

    public static RetryPolicy withMaxAttempts(int maxAttempts) {
        return new RetryPolicy(maxAttempts);
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(5);
    }

    public void scheduleNextAttempt(Duration backoff) {
        this.attemptCount++;
        this.nextAttemptAt = LocalDateTime.now().plus(backoff);
    }

    public void recordFailure() {
        this.attemptCount++;
        this.nextAttemptAt = null;
    }

    public boolean isRemainAttempt() {
        return attemptCount < maxAttempts;
    }
}
