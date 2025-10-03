package backend.mulkkam.auth.repository;

import backend.mulkkam.auth.domain.IdempotencyRecord;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyRepository {

    private final Map<String, IdempotencyRecord> idempotencyRecords = new ConcurrentHashMap<>();

    public Optional<IdempotencyRecord> findByKey(String key) {
        if (idempotencyRecords.containsKey(key)) {
            return Optional.of(idempotencyRecords.get(key));
        }
        return Optional.empty();
    }

    public boolean existsByKey(String key) {
        return idempotencyRecords.containsKey(key);
    }

    public void save(IdempotencyRecord record) {
        idempotencyRecords.putIfAbsent(record.getKey(), record);
    }
}
