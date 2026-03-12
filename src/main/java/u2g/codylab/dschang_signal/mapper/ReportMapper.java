package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Report;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {

    ReportApiDTO toReportDTO(Report report);

    @Mapping(target = "moderationStatus", ignore = true)
    Report toEntity(ReportApiDTO reportApiDTO);
}