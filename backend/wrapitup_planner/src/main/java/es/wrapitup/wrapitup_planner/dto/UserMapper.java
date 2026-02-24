package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.wrapitup.wrapitup_planner.model.UserModel;


@Mapper(componentModel = "spring")
public interface UserMapper {
    UserModelDTO toDto(UserModel userModel);
    
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "profilePic", ignore = true)
    @Mapping(target = "calendarEvents", ignore = true)
    @Mapping(target = "calendarTasks", ignore = true)
    UserModel toEntity(UserModelDTO dto);
}
