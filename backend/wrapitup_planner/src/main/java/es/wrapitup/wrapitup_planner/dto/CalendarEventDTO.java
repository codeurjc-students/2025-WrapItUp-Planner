package es.wrapitup.wrapitup_planner.dto;

import es.wrapitup.wrapitup_planner.model.EventColor;
import java.time.LocalDateTime;

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
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public EventColor getColor() {
        return color;
    }
    
    public void setColor(EventColor color) {
        this.color = color;
        if (color != null) {
            this.colorHex = color.getHexCode();
        }
    }
    
    public String getColorHex() {
        return colorHex;
    }
    
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    
    public Boolean getAllDay() {
        return allDay;
    }
    
    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
