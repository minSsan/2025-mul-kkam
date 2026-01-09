package backend.mulkkam.notification.repository;

import backend.mulkkam.notification.domain.converter.SendNotificationRequestConverter;
import backend.mulkkam.notification.domain.entity.NotificationOutbox;
import backend.mulkkam.notification.repository.dto.OutboxRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Repository
public class NotificationOutboxBatchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SendNotificationRequestConverter payloadConverter = new SendNotificationRequestConverter(); // TEXT 컬럼 저장용

    public List<OutboxRow> bulkInsert(List<NotificationOutbox> rows) {
        if (rows.isEmpty()) return List.of();

        return jdbcTemplate.execute((ConnectionCallback<List<OutboxRow>>) con -> {
            int step = queryAutoIncrementStep(con);

            // 컬럼명은 실제 DDL과 BaseEntity 컬럼에 맞게 조정 필요
            String sql = buildMultiValuesInsertSql(
                    "notification_outbox",
                    List.of(
                            "notification_id",
                            "target_type",
                            "target_value",
                            "idempotency_key",
                            "status",
                            "leased_at",
                            "lease_expires_at",
                            "attempt_count",
                            "max_attempts",
                            "next_attempt_at",
                            "payload",
                            "created_at",
                            "deleted_at"
                    ),
                    rows.size()
            );

            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int idx = 1;
                for (NotificationOutbox r : rows) {
                    ps.setLong(idx++, r.getNotificationId());
                    ps.setString(idx++, r.getTargetType().name());
                    ps.setString(idx++, r.getTargetValue());
                    ps.setString(idx++, r.getIdempotencyKey());
                    ps.setString(idx++, r.getStatus().name());

                    LocalDateTime leasedAt = (r.getLease() == null) ? null : r.getLease().getLeasedAt();
                    LocalDateTime leaseExpiresAt = (r.getLease() == null) ? null : r.getLease().getLeaseExpiresAt();

                    if (leasedAt == null) ps.setNull(idx++, Types.TIMESTAMP);
                    else ps.setTimestamp(idx++, Timestamp.valueOf(leasedAt));

                    if (leaseExpiresAt == null) ps.setNull(idx++, Types.TIMESTAMP);
                    else ps.setTimestamp(idx++, Timestamp.valueOf(leaseExpiresAt));

                    ps.setInt(idx++, r.getRetryPolicy().getAttemptCount());
                    ps.setInt(idx++, r.getRetryPolicy().getMaxAttempts());
                    ps.setTimestamp(idx++, Timestamp.valueOf(r.getRetryPolicy().getNextAttemptAt()));

                    ps.setString(idx++, payloadConverter.convertToDatabaseColumn(r.getPayload()));
                    ps.setTimestamp(idx++, Timestamp.valueOf(now));
                    ps.setTimestamp(idx++, null);
                }
                ps.executeUpdate();
            }

            long firstId = queryLastInsertId(con);
            List<OutboxRow> out = new ArrayList<>(rows.size());
            for (int i = 0; i < rows.size(); i++) {
                long outboxId = firstId + (long) i * step;
                out.add(new OutboxRow(outboxId, rows.get(i).getTargetValue()));
            }
            return out;
        });
    }

    private static int queryAutoIncrementStep(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT @@auto_increment_increment");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static long queryLastInsertId(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT LAST_INSERT_ID()");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static String buildMultiValuesInsertSql(String table, List<String> columns, int rows) {
        String cols = String.join(", ", columns);
        String one = "(" + columns.stream().map(c -> "?").collect(Collectors.joining(", ")) + ")";
        String values = IntStream.range(0, rows).mapToObj(i -> one).collect(Collectors.joining(", "));
        return "INSERT INTO " + table + " (" + cols + ") VALUES " + values;
    }
}
