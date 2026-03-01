package es.wrapitup.wrapitup_planner.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.CalendarDayDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarDaySummaryDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarEventDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarMonthDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarTaskDTO;

@Service
public class CalendarService {
    
    private final CalendarEventService eventService;
    private final CalendarTaskService taskService;
    
    public CalendarService(CalendarEventService eventService, CalendarTaskService taskService) {
        this.eventService = eventService;
        this.taskService = taskService;
    }
    
    public CalendarMonthDTO getMonthView(String username, int year, int month) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        
        LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
        LocalDateTime endDateTime = lastDayOfMonth.atTime(LocalTime.MAX);
        
        List<CalendarEventDTO> monthEvents = eventService.getEventsByDateRange(username, startDateTime, endDateTime);
        List<CalendarTaskDTO> monthTasks = taskService.getTasksByDateRange(username, firstDayOfMonth, lastDayOfMonth);
        
        CalendarMonthDTO monthDTO = new CalendarMonthDTO(year, month);
        List<CalendarDaySummaryDTO> daySummaries = new ArrayList<>();
        
        for (LocalDate date = firstDayOfMonth; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
            CalendarDaySummaryDTO daySummary = createDaySummary(date, monthEvents, monthTasks);
            daySummaries.add(daySummary);
        }
        
        monthDTO.setDays(daySummaries);
        return monthDTO;
    }
    
    public CalendarDayDTO getDayView(String username, LocalDate date) {
        CalendarDayDTO dayDTO = new CalendarDayDTO(date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<CalendarEventDTO> allEvents = eventService.getEventsByDateRange(username, startOfDay, endOfDay);
        
        List<CalendarEventDTO> dayEvents = allEvents.stream()
            .filter(event -> isEventOnDay(event, date))
            .collect(Collectors.toList());
        
        List<CalendarTaskDTO> dayTasks = taskService.getTasksByDay(username, date);
        
        dayDTO.setEvents(dayEvents);
        dayDTO.setTasks(dayTasks);
        
        return dayDTO;
    }
    
    private CalendarDaySummaryDTO createDaySummary(LocalDate date, List<CalendarEventDTO> allEvents, List<CalendarTaskDTO> allTasks) {
        CalendarDaySummaryDTO summary = new CalendarDaySummaryDTO(date);
        
        List<CalendarEventDTO> dayEvents = allEvents.stream()
            .filter(event -> isEventOnDay(event, date))
            .collect(Collectors.toList());
        
        List<String> eventColors = dayEvents.stream()
            .map(CalendarEventDTO::getColorHex)
            .limit(3)
            .collect(Collectors.toList());
        
        long pendingTasksCount = allTasks.stream()
            .filter(task -> task.getTaskDate().equals(date) && !task.getCompleted())
            .count();
        
        summary.setEventColors(eventColors);
        summary.setTotalEvents(dayEvents.size());
        summary.setPendingTasks((int) pendingTasksCount);
        
        return summary;
    }
    
    private boolean isEventOnDay(CalendarEventDTO event, LocalDate date) {
        LocalDate eventStartDate = event.getStartDate().toLocalDate();
        LocalDate eventEndDate = event.getEndDate().toLocalDate();
        
        return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate);
    }
}
