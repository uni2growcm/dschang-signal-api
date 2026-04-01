package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import u2g.codylab.dschang_signal.dto.NotificationApiDTO;
import u2g.codylab.dschang_signal.entity.Notification;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ReportMapper.class, UserMapper.class}
)
public interface NotificationMapper {

    @Mapping(
            source = "createdAt",
            target = "createdAt",
            qualifiedByName = "timestampToOffsetDateTime"
    )
    NotificationApiDTO toNotificationDTO(Notification notification);
}