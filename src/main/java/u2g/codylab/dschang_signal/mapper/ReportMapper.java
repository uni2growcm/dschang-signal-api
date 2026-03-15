package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Category;
import u2g.codylab.dschang_signal.entity.Report;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CategoryMapper.class, UserMapper.class})
public interface ReportMapper {

    @Mapping(target = "category", expression = "java(mapFirstCategoryName(report.getCategories()))")
    ReportApiDTO toReportDTO(Report report);

    @Mapping(target = "moderationStatus", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Report toEntity(ReportApiDTO reportApiDTO);

    default String mapFirstCategoryName(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.iterator().next().getName();
    }
}