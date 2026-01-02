package backend.mulkkam.notification.domain.converter;

import backend.mulkkam.notification.domain.vo.NotificationPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SendNotificationRequestConverter implements AttributeConverter<NotificationPayload, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NotificationPayload attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting SendNotificationRequest to JSON", e);
        }
    }

    @Override
    public NotificationPayload convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, NotificationPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to SendNotificationRequest", e);
        }
    }
}
