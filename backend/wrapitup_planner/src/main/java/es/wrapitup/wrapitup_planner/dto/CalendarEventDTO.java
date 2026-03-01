package es.wrapitup.wrapitup_planner.dto;

import es.wrapitup.wrapitup_planner.model.EventColor;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CalendarEventDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventColor color;
    private String colorHex;
    private Boolean allDay;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    
    // Custom setter to maintain logic
    public void setColor(EventColor color) {
        this.color = color;
        if (color != null) {
            this.colorHex = color.getHexCode();
        }
    }
}
