package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;

import es.wrapitup.wrapitup_planner.model.UserModel;


@Mapper(componentModel = "spring")
public interface UserMapper {
    UserModelDTO toDto(UserModel userModel);
    UserModel toEntity(UserModelDTO dto);
}
