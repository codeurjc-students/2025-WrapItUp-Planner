package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.wrapitup.wrapitup_planner.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    @Mapping(source = "note.id", target = "noteId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.image", target = "userProfilePicUrl")
    CommentDTO toDto(Comment comment);
    
    @Mapping(target = "note", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userProfilePicUrl", ignore = true)
    Comment toEntity(CommentDTO commentDTO);
}
