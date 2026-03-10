package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import u2g.codylab.dschang_signal.dto.ReportResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Report;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "reviewedAt", target = "reviewedAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(target = "photoUrl", ignore = true)
    ReportResponseApiDTO toReportDTO(Report report);

    @Named("timestampToOffsetDateTime")
    default OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().atOffset(OffsetDateTime.now().getOffset());
    }
}