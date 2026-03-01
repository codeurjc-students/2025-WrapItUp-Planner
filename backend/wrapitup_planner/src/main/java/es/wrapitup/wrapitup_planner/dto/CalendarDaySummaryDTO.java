package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarDaySummaryDTO {
    private LocalDate date;
    private List<String> eventColors = new ArrayList<>();
    private int totalEvents;
    private int pendingTasks;
    private boolean hasEvents;
    private boolean hasTasks;
    
    public CalendarDaySummaryDTO(LocalDate date) {
        this.date = date;
    }
    
    // Custom setters to maintain logic
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
        this.hasEvents = totalEvents > 0;
    }
    
    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
        this.hasTasks = pendingTasks > 0;
    }
}
