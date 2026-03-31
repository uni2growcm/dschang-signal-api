package u2g.codylab.dschang_signal.mapper;

import org.springframework.stereotype.Component;
import u2g.codylab.dschang_signal.dto.NotificationApiDTO;
import u2g.codylab.dschang_signal.entity.Notification;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class NotificationMapper {

    public NotificationApiDTO toApiDTO(Notification notification) {
        if (notification == null) return null;

        NotificationApiDTO dto = new NotificationApiDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setReportId(notification.getReportId());
        dto.setOldStatus(notification.getOldStatus());
        dto.setNewStatus(notification.getNewStatus());

        if (notification.getCreatedAt() != null) {
            dto.setCreatedAt(OffsetDateTime.ofInstant(
                    notification.getCreatedAt().toInstant(),
                    ZoneOffset.UTC));
        }

        return dto;
    }
}