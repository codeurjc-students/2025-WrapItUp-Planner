package es.wrapitup.wrapitup_planner.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarMonthDTO {
    private int year;
    private int month;
    private List<CalendarDaySummaryDTO> days = new ArrayList<>();
    
    public CalendarMonthDTO(int year, int month) {
        this.year = year;
        this.month = month;
    }
}
