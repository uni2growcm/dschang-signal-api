package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import u2g.codylab.dschang_signal.dto.CategoryRequestApiDTO;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Category;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapper.class)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reports", ignore = true)
    Category toEntity(CategoryRequestApiDTO categoryRequestApiDTO);

    @Mapping(target = "createdAt", expression = "java(timestampToOffsetDateTime(category.getCreatedAt()))")
    CategoryResponseApiDTO toCategoryDto(Category category);

    default OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}