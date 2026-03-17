package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Category;
import u2g.codylab.dschang_signal.entity.Report;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CategoryMapper.class, UserMapper.class})
public abstract class ReportMapper {

    @Autowired
    protected CategoryMapper categoryMapper;

    @Mapping(target = "categories", expression = "java(mapCategoriesToDTO(report.getCategories()))")
    @Mapping(target = "createdBy", source = "createdBy")
    public abstract ReportApiDTO toReportDTO(Report report);

    @Mapping(target = "moderationStatus", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    public abstract Report toEntity(ReportApiDTO reportApiDTO);

    protected List<CategoryResponseApiDTO> mapCategoriesToDTO(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }
}