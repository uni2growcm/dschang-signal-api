package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Report;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "reviewedAt", source = "reviewedAt")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "categories", ignore = true)
    ReportApiDTO toReportDTO(Report report);

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "reviewedAt", source = "reviewedAt")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "media", ignore = true)
    Report toEntity(ReportApiDTO reportApiDTO);

    default OffsetDateTime map(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    default Timestamp map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return Timestamp.from(offsetDateTime.toInstant());
    }
}