package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import u2g.codylab.dschang_signal.dto.CreateReportRequestApiDTO;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Report;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "reviewedAt", target = "reviewedAt", qualifiedByName = "timestampToOffsetDateTime")
    ReportApiDTO toDTO(Report report);

    // DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "moderationStatus", ignore = true)
    @Mapping(target = "reportStatus", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    Report toEntity(CreateReportRequestApiDTO dto);

    // Timestamp -> OffsetDateTime
    @Named("timestampToOffsetDateTime")
    static OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}