package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import u2g.codylab.dschang_signal.dto.CategoryRequestApiDTO;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Category;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true) // To delete
    Category toEntity(CategoryRequestApiDTO categoryRequestApiDTO);

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToOffsetDateTime")
    CategoryResponseApiDTO toCategoryDto(Category category);

    @Named("timestampToOffsetDateTime")
    default OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().atOffset(OffsetDateTime.now().getOffset());
    }
}
