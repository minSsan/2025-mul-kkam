package backend.mulkkam.notification.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor
public class ProcessingLease {

    private LocalDateTime leasedAt;

    private LocalDateTime leaseExpiresAt;

    public void release() {
        this.leasedAt = null;
        this.leaseExpiresAt = null;
    }
}
