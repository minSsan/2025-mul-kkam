package backend.mulkkam.compensation.service.query;

import backend.mulkkam.compensation.domain.entity.OutboxCompensation;
import backend.mulkkam.compensation.repository.OutboxCompensationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class OutboxCompensationQueryService {

    private final OutboxCompensationRepository compensationRepository;

    public OutboxCompensation get(Long id) {
        return compensationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compensation 존재하지 않음: id = " + id));
    }
}
