package u2g.codylab.dschang_signal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.User;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserApiDTO toUserDTO(User user);

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "reports", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "media", ignore = true)
    User toEntity(UserApiDTO userApiDTO);

    default OffsetDateTime map(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    default Timestamp map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return Timestamp.from(offsetDateTime.toInstant());
    }
}