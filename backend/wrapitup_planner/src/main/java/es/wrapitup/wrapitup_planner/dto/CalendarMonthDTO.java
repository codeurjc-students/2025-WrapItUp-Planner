package es.wrapitup.wrapitup_planner.dto;

import java.util.ArrayList;
import java.util.List;

public class CalendarMonthDTO {
    
    private int year;
    private int month;
    private List<CalendarDaySummaryDTO> days = new ArrayList<>();
    
    public CalendarMonthDTO() {
    }
    
    public CalendarMonthDTO(int year, int month) {
        this.year = year;
        this.month = month;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public List<CalendarDaySummaryDTO> getDays() {
        return days;
    }
    
    public void setDays(List<CalendarDaySummaryDTO> days) {
        this.days = days;
    }
}
