package backend.mulkkam.notification.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ProcessingLease {

    private LocalDateTime leasedAt;

    private LocalDateTime leaseExpiresAt;

    public boolean isExpired() {
        return leaseExpiresAt != null && LocalDateTime.now().isAfter(leaseExpiresAt);
    }
}
