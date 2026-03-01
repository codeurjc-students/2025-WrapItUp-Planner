package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarDayDTO {
    private LocalDate date;
    private List<CalendarEventDTO> events = new ArrayList<>();
    private List<CalendarTaskDTO> tasks = new ArrayList<>();
    
    public CalendarDayDTO(LocalDate date) {
        this.date = date;
    }
}
