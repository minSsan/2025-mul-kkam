package backend.mulkkam.notification.repository.dto;

public record DeviceTokenRow(
        Long deviceId,
        Long memberId,
        String token
) {
}
