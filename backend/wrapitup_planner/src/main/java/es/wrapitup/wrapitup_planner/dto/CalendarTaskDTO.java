package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CalendarTaskDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate taskDate;
    private Boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
}
