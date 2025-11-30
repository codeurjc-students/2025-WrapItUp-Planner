package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.UserModel;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface NoteMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "sharedWithUserIds", expression = "java(mapSharedUsers(note.getSharedWith()))")
    @Mapping(source = "comments", target = "comments")
    NoteDTO toDto(Note note);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "sharedWith", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Note toEntity(NoteDTO dto);
    
    default List<Long> mapSharedUsers(Set<UserModel> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(UserModel::getId)
                .collect(Collectors.toList());
    }
}
