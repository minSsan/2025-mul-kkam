package backend.mulkkam.common.interceptor;

import backend.mulkkam.auth.domain.IdempotencyRecord;
import backend.mulkkam.auth.domain.ProgressStatus;
import backend.mulkkam.auth.repository.IdempotencyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private final IdempotencyRepository repository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String key = request.getHeader("Idempotency-Key");

        if (key == null) {
            return true;
        }

        Optional<IdempotencyRecord> existing = repository.findByKey(key);

        if (existing.isEmpty()) {
            /* 첫 요청 - PROCESSING 상태로 저장 */
            IdempotencyRecord newRecord = new IdempotencyRecord(key, ProgressStatus.PROCESSING);
            repository.save(newRecord);

            return true;
        }

        IdempotencyRecord record = existing.get();

        switch (record.getProgressStatus()) {
            /* 이미 처리 완료된 요청인 경우 */
            case COMPLETED -> {
                response.setStatus(record.getStatusCode().value());
                response.getWriter().write(record.getResponseBody() != null ? record.getResponseBody() : "");
                return false;
            }
            /* 처리중인 요청인 경우 */
            case PROCESSING -> {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return false;
            }
        }

        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String key = request.getHeader("Idempotency-Key");

        if (key == null) {
            return;
        }

        IdempotencyRecord record = repository.findByKey(key).orElse(null);
        if (record == null) {
            return;
        }

        if (record.getProgressStatus() == ProgressStatus.PROCESSING) {
            byte[] body = new byte[0];
            if (response instanceof ContentCachingResponseWrapper wrapper) {
                body = wrapper.getContentAsByteArray();
            }
            String bodyToSave = new String(body, StandardCharsets.UTF_8);

            /* HttpServletResponse body 추출 불가능 - 응답 본문 캡처 */
            record.setResponseBody(bodyToSave);
            record.setProgressStatus(ProgressStatus.COMPLETED);
            record.setStatusCode(HttpStatus.valueOf(response.getStatus()));
            repository.save(record);
        }
    }
}
