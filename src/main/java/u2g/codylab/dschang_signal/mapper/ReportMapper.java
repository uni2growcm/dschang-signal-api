package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Category;
import u2g.codylab.dschang_signal.entity.Report;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, CategoryMapper.class})
public interface ReportMapper {

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(source = "reviewedAt", target = "reviewedAt", qualifiedByName = "timestampToOffsetDateTime")
    @Mapping(target = "categories", source = "categories", qualifiedByName = "categoriesToDtoList")
    ReportApiDTO toReportDTO(Report report);

    @Named("categoriesToDtoList")
    default List<CategoryResponseApiDTO> categoriesToDtoList(Set<Category> categories) {
        if (categories == null) return Collections.emptyList();

        return categories.stream()
                .map(this::categoryToDto)
                .toList();
    }

    default CategoryResponseApiDTO categoryToDto(Category category) {
        if (category == null) return null;

        CategoryResponseApiDTO dto = new CategoryResponseApiDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        return dto;
    }
}