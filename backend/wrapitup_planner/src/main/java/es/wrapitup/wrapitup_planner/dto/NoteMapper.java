package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.wrapitup.wrapitup_planner.model.Note;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(source = "user.id", target = "userId")
    NoteDTO toDto(Note note);

    @Mapping(source = "userId", target = "user.id")
    Note toEntity(NoteDTO dto);
}
