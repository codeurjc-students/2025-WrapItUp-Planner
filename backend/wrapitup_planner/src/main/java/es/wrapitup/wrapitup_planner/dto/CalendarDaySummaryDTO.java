package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDaySummaryDTO {
    
    private LocalDate date;
    private List<String> eventColors = new ArrayList<>();
    private int totalEvents;
    private int pendingTasks;
    private boolean hasEvents;
    private boolean hasTasks;
    
    public CalendarDaySummaryDTO() {
    }
    
    public CalendarDaySummaryDTO(LocalDate date) {
        this.date = date;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public List<String> getEventColors() {
        return eventColors;
    }
    
    public void setEventColors(List<String> eventColors) {
        this.eventColors = eventColors;
    }
    
    public int getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
        this.hasEvents = totalEvents > 0;
    }
    
    public int getPendingTasks() {
        return pendingTasks;
    }
    
    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
        this.hasTasks = pendingTasks > 0;
    }
    
    public boolean isHasEvents() {
        return hasEvents;
    }
    
    public void setHasEvents(boolean hasEvents) {
        this.hasEvents = hasEvents;
    }
    
    public boolean isHasTasks() {
        return hasTasks;
    }
    
    public void setHasTasks(boolean hasTasks) {
        this.hasTasks = hasTasks;
    }
}
