package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.wrapitup.wrapitup_planner.model.CalendarEvent;

@Mapper(componentModel = "spring")
public interface CalendarEventMapper {
    
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "colorHex", expression = "java(event.getColor() != null ? event.getColor().getHexCode() : null)")
    CalendarEventDTO toDto(CalendarEvent event);
    
    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    CalendarEvent toEntity(CalendarEventDTO dto);
}
