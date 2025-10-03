package backend.mulkkam.auth.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class IdempotencyRecord {

    @Id
    private String key;
    private ProgressStatus progressStatus;
    private HttpStatus statusCode;
    private String responseBody;

    public IdempotencyRecord(String key, ProgressStatus progressStatus) {
        this.key = key;
        this.progressStatus = progressStatus;
    }
}
