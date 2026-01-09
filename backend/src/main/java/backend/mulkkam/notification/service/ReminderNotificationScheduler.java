package backend.mulkkam.notification.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import backend.mulkkam.notification.service.dto.ChunkResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReminderNotificationScheduler {

    private static final String MINUTELY_CRON = "0 * * * * *";

    private final ReminderChunkProcessor chunkProcessor;

    @Scheduled(cron = MINUTELY_CRON)
    public void run() {
        LocalDateTime raw = LocalDateTime.now();
        LocalDateTime nowBucket = computeNowBucket(Clock.systemDefaultZone(), ZoneId.systemDefault());
        log.warn("[SCHED] now={}, epochMs={}", raw, nowBucket);

        Long lastScheduleId = null;

        while (true) {
            ChunkResult result = chunkProcessor.processChunk(nowBucket, lastScheduleId);

            if (result.nextLastScheduleId() == null) {
                break;
            }
            lastScheduleId = result.nextLastScheduleId();

            if (result.isLastChunk()) {
                break;
            }
        }
    }

    private static final long LAST_SECOND_THRESHOLD_MS = 59_000;

    /**
     * 스케줄링 시간 보정 (ex. 11:59:XX => 12:00:XX)
     * @param clock
     * @param zoneId
     * @return
     */
    private LocalDateTime computeNowBucket(Clock clock, ZoneId zoneId) {
        Instant nowInstant = Instant.now(clock);
        long epochMs = nowInstant.toEpochMilli();
        long msInMinute = Math.floorMod(epochMs, 60_000);

        ZonedDateTime zdt = ZonedDateTime.ofInstant(nowInstant, zoneId);
        ZonedDateTime bucket = zdt.truncatedTo(ChronoUnit.MINUTES);

        if (msInMinute >= LAST_SECOND_THRESHOLD_MS) {
            bucket = bucket.plusMinutes(1);
        }
        return bucket.toLocalDateTime();
    }
}
