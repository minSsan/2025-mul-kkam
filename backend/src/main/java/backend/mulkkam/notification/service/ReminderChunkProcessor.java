package backend.mulkkam.notification.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import backend.mulkkam.common.util.IdempotencyKeys;
import backend.mulkkam.device.repository.DeviceRepository;
import backend.mulkkam.notification.domain.NotificationTargetType;
import backend.mulkkam.notification.domain.NotificationType;
import backend.mulkkam.notification.domain.RetryPolicy;
import backend.mulkkam.notification.domain.entity.NotificationOutbox;
import backend.mulkkam.notification.domain.entity.ReminderSchedule;
import backend.mulkkam.notification.domain.vo.NotificationPayload;
import backend.mulkkam.notification.dto.NotificationMessageTemplate;
import backend.mulkkam.notification.repository.NotificationBatchRepository;
import backend.mulkkam.notification.repository.NotificationOutboxBatchRepository;
import backend.mulkkam.notification.repository.ReminderScheduleRepository;
import backend.mulkkam.notification.repository.dto.DeviceTokenRow;
import backend.mulkkam.notification.service.dto.ChunkResult;
import backend.mulkkam.notification.service.dto.NotificationRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReminderChunkProcessor {

    private static final int CHUNK_SIZE = 500;

    private final ReminderScheduleRepository reminderScheduleRepository;
    private final DeviceRepository deviceRepository;

    private final NotificationBatchRepository notificationBatchRepository;
    private final NotificationOutboxBatchRepository outboxBatchRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChunkResult processChunk(LocalDateTime nowBucket, Long lastScheduleId) {
        LocalTime schedule = nowBucket.toLocalTime();

        List<ReminderSchedule> schedules = reminderScheduleRepository.findChunkBySchedule(
                schedule, lastScheduleId, PageRequest.of(0, CHUNK_SIZE)
        );

        if (schedules.isEmpty()) {
            return new ChunkResult(null, true);
        }

        Long nextLastScheduleId = schedules.getLast().getId();
        boolean isLastChunk = schedules.size() < CHUNK_SIZE;

        // 멤버 중복 제거
        List<Long> memberIds = schedules.stream()
                .map(rs -> rs.getMember().getId())
                .distinct()
                .toList();

        // 1) Notification(멤버당 1건) 배치 insert + 생성 notificationId 매핑 확보
        NotificationMessageTemplate template = RemindNotificationMessageTemplateProvider.getRandomMessageTemplate();
        List<NotificationRow> insertedNotifications = notificationBatchRepository.bulkInsert(
                memberIds, NotificationType.REMIND, template, nowBucket
        );

        // 2) Device 조회(memberId -> devices)
        Map<Long, List<DeviceTokenRow>> devicesByMemberId = getDevicesByMemberId(memberIds);

        // 3) Outbox 생성(디바이스별) + READY 상태로 배치 insert
        List<NotificationOutbox> outboxes = getOutboxes(nowBucket, insertedNotifications, devicesByMemberId, template);

        if (!outboxes.isEmpty()) {
            outboxBatchRepository.bulkInsert(outboxes);
        }

        return new ChunkResult(nextLastScheduleId, isLastChunk);
    }

    private Map<Long, List<DeviceTokenRow>> getDevicesByMemberId(List<Long> memberIds) {
        List<DeviceTokenRow> deviceRows = deviceRepository.findDeviceTokensByMemberIds(memberIds);

        return deviceRows.stream()
                .filter(r -> r.token() != null && !r.token().isBlank())
                .collect(Collectors.groupingBy(DeviceTokenRow::memberId));
    }

    private List<NotificationOutbox> getOutboxes(
            LocalDateTime nowBucket,
            List<NotificationRow> insertedNotifications,
            Map<Long, List<DeviceTokenRow>> devicesByMemberId,
            NotificationMessageTemplate template
    ) {
        List<NotificationOutbox> outboxes = new ArrayList<>();
        for (NotificationRow n : insertedNotifications) {
            List<DeviceTokenRow> devices = devicesByMemberId.getOrDefault(n.memberId(), List.of());
            for (DeviceTokenRow d : devices) {
                String idempotencyKey = IdempotencyKeys.remind(n.memberId(), d.deviceId(), nowBucket);

                NotificationOutbox outbox = NotificationOutbox.builder()
                        .notificationId(n.notificationId())
                        .targetType(NotificationTargetType.TOKEN)
                        .targetValue(d.token())
                        .idempotencyKey(idempotencyKey)
                        .payload(new NotificationPayload(template.title(), template.body(), template.action()))
                        .retryPolicy(RetryPolicy.withMaxAttempts(3))
                        .build();

                outboxes.add(outbox);
            }
        }
        return outboxes;
    }
}
