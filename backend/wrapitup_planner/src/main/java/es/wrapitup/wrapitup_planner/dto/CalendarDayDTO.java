package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDayDTO {
    
    private LocalDate date;
    private List<CalendarEventDTO> events = new ArrayList<>();
    private List<CalendarTaskDTO> tasks = new ArrayList<>();
    
    public CalendarDayDTO() {
    }
    
    public CalendarDayDTO(LocalDate date) {
        this.date = date;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public List<CalendarEventDTO> getEvents() {
        return events;
    }
    
    public void setEvents(List<CalendarEventDTO> events) {
        this.events = events;
    }
    
    public List<CalendarTaskDTO> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<CalendarTaskDTO> tasks) {
        this.tasks = tasks;
    }
}
