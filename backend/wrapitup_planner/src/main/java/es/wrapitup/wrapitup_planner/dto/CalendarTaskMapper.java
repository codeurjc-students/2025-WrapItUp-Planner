package es.wrapitup.wrapitup_planner.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.wrapitup.wrapitup_planner.model.CalendarTask;

@Mapper(componentModel = "spring")
public interface CalendarTaskMapper {
    
    @Mapping(source = "user.id", target = "userId")
    CalendarTaskDTO toDto(CalendarTask task);
    
    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    CalendarTask toEntity(CalendarTaskDTO dto);
}
