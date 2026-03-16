package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.Media;
import u2g.codylab.dschang_signal.entity.User;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = ReportMapper.class)
public interface MediaMapper {
    @Mapping(source = "report.id", target = "reportId")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToOffsetDateTime")
    MediaResponseApiDTO toMediaDTO(Media media);

    @Mapping(target = "report", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "offsetDateTimeToTimestamp")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "offsetDateTimeToTimestamp")
    Media toEntity(MediaResponseApiDTO media);

    @Named("timestampToOffsetDateTime")
    default OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().atOffset(OffsetDateTime.now().getOffset());
    }
    @Named("offsetDateTimeToTimestamp")
    default Timestamp offsetDateTimeToTimestamp(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return Timestamp.valueOf(offsetDateTime.toLocalDateTime());
    }
}
