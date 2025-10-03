package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.wrapitup.wrapitup_planner.model.AINote;

@Mapper(componentModel = "spring")
public interface AINoteMapper {

    @Mapping(source = "user.idUser", target = "userId")
    AINoteDTO toDto(AINote aiNote);

    @Mapping(source = "userId", target = "user.idUser")
    AINote toEntity(AINoteDTO dto);
}
