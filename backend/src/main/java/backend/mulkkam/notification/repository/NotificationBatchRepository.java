package backend.mulkkam.notification.repository;

import backend.mulkkam.notification.domain.NotificationType;
import backend.mulkkam.notification.dto.NotificationInsertDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import backend.mulkkam.notification.dto.NotificationMessageTemplate;
import backend.mulkkam.notification.service.dto.NotificationRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NotificationBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<NotificationInsertDto> notificationInsertDtos, int batchSize) {
        String sql = "INSERT INTO notification (notification_type, is_read, created_at, member_id, content, deleted_at) values (?, ?, ?, ?, ?, ?)";
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

        for (int i = 0; i < notificationInsertDtos.size(); i += batchSize) {
            List<NotificationInsertDto> batchNotificationInsertDtos = notificationInsertDtos.subList(i,
                    Math.min(i + batchSize, notificationInsertDtos.size()));

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    NotificationInsertDto notificationInsertDto = batchNotificationInsertDtos.get(i);
                    ps.setString(1, notificationInsertDto.notificationType().name());
                    ps.setBoolean(2, Boolean.FALSE);
                    ps.setTimestamp(3, currentTimestamp);
                    ps.setLong(4, notificationInsertDto.memberId());
                    ps.setString(5, notificationInsertDto.content());
                    ps.setTimestamp(6, null);
                }

                @Override
                public int getBatchSize() {
                    return batchNotificationInsertDtos.size();
                }
            });
        }
    }

    public List<NotificationRow> bulkInsert(
            List<Long> memberIds,
            NotificationType type,
            NotificationMessageTemplate template,
            LocalDateTime createdAt
    ) {
        if (memberIds.isEmpty()) return List.of();

        return jdbcTemplate.execute((ConnectionCallback<List<NotificationRow>>) con -> {
            int step = queryAutoIncrementStep(con);

            String sql = buildMultiValuesInsertSql(
                    "notification",
                    List.of("notification_type", "is_read", "created_at", "member_id", "content", "deleted_at"),
                    memberIds.size()
            );

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int idx = 1;
                Timestamp ts = Timestamp.valueOf(createdAt);

                for (Long memberId : memberIds) {
                    ps.setString(idx++, type.name());
                    ps.setBoolean(idx++, false);
                    ps.setTimestamp(idx++, ts);
                    ps.setLong(idx++, memberId);
                    ps.setString(idx++, template.body()); // content 생성 규칙을 template에 둔다고 가정
                    ps.setTimestamp(idx++, null);
                }
                ps.executeUpdate();
            }

            long firstId = queryLastInsertId(con);
            List<NotificationRow> rows = new ArrayList<>(memberIds.size());
            for (int i = 0; i < memberIds.size(); i++) {
                long notificationId = firstId + (long) i * step;
                rows.add(new NotificationRow(notificationId, memberIds.get(i)));
            }
            return rows;
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
