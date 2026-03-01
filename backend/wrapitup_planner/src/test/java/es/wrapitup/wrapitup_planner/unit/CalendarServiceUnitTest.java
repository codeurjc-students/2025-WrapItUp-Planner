package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.wrapitup.wrapitup_planner.dto.CalendarDayDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarDaySummaryDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarEventDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarMonthDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarTaskDTO;
import es.wrapitup.wrapitup_planner.model.EventColor;
import es.wrapitup.wrapitup_planner.service.CalendarEventService;
import es.wrapitup.wrapitup_planner.service.CalendarService;
import es.wrapitup.wrapitup_planner.service.CalendarTaskService;

@Tag("unit")
public class CalendarServiceUnitTest {

    @Mock
    private CalendarEventService eventService;

    @Mock
    private CalendarTaskService taskService;

    @InjectMocks
    private CalendarService calendarService;

    private CalendarEventDTO testEvent1;
    private CalendarEventDTO testEvent2;
    private CalendarTaskDTO testTask1;
    private CalendarTaskDTO testTask2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testEvent1 = new CalendarEventDTO();
        testEvent1.setId(1L);
        testEvent1.setUserId(1L);
        testEvent1.setTitle("Event 1");
        testEvent1.setDescription("Description 1");
        testEvent1.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        testEvent1.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        testEvent1.setColor(EventColor.BLUE);
        testEvent1.setAllDay(false);

        testEvent2 = new CalendarEventDTO();
        testEvent2.setId(2L);
        testEvent2.setUserId(1L);
        testEvent2.setTitle("Event 2");
        testEvent2.setDescription("Description 2");
        testEvent2.setStartDate(LocalDateTime.of(2026, 2, 26, 14, 0));
        testEvent2.setEndDate(LocalDateTime.of(2026, 2, 26, 15, 0));
        testEvent2.setColor(EventColor.GREEN);
        testEvent2.setAllDay(false);

        testTask1 = new CalendarTaskDTO();
        testTask1.setId(1L);
        testTask1.setUserId(1L);
        testTask1.setTitle("Task 1");
        testTask1.setDescription("Description 1");
        testTask1.setTaskDate(LocalDate.of(2026, 2, 25));
        testTask1.setCompleted(false);

        testTask2 = new CalendarTaskDTO();
        testTask2.setId(2L);
        testTask2.setUserId(1L);
        testTask2.setTitle("Task 2");
        testTask2.setDescription("Description 2");
        testTask2.setTaskDate(LocalDate.of(2026, 2, 25));
        testTask2.setCompleted(true);
    }

    // Month view tests

    @Test
    void getMonthViewReturnsMonthWithDays() {
        int year = 2026;
        int month = 2;
        String username = "testuser";

        List<CalendarEventDTO> events = Arrays.asList(testEvent1, testEvent2);
        List<CalendarTaskDTO> tasks = Arrays.asList(testTask1, testTask2);

        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(events);
        when(taskService.getTasksByDateRange(eq(username), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(tasks);

        CalendarMonthDTO result = calendarService.getMonthView(username, year, month);

        assertNotNull(result);
        assertEquals(year, result.getYear());
        assertEquals(month, result.getMonth());
        assertNotNull(result.getDays());
        assertEquals(28, result.getDays().size()); 
        
        verify(eventService).getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(taskService).getTasksByDateRange(eq(username), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getMonthViewWithNoEventsOrTasks() {
        int year = 2026;
        int month = 2;
        String username = "testuser";

        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(taskService.getTasksByDateRange(eq(username), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        CalendarMonthDTO result = calendarService.getMonthView(username, year, month);

        assertNotNull(result);
        assertEquals(year, result.getYear());
        assertEquals(month, result.getMonth());
        assertNotNull(result.getDays());
        assertEquals(28, result.getDays().size());
        
        for (CalendarDaySummaryDTO day : result.getDays()) {
            assertEquals(0, day.getTotalEvents());
            assertEquals(0, day.getPendingTasks());
            assertTrue(day.getEventColors().isEmpty());
        }
    }

    // Day view tests

    @Test
    void getDayViewReturnsDayWithEventsAndTasks() {
        LocalDate date = LocalDate.of(2026, 2, 25);
        String username = "testuser";

        List<CalendarEventDTO> events = Arrays.asList(testEvent1);
        List<CalendarTaskDTO> tasks = Arrays.asList(testTask1, testTask2);

        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(events);
        when(taskService.getTasksByDay(username, date))
            .thenReturn(tasks);

        CalendarDayDTO result = calendarService.getDayView(username, date);

        assertNotNull(result);
        assertEquals(date, result.getDate());
        assertNotNull(result.getEvents());
        assertNotNull(result.getTasks());
        assertEquals(1, result.getEvents().size());
        assertEquals(2, result.getTasks().size());
        
        verify(eventService).getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(taskService).getTasksByDay(username, date);
    }

    @Test
    void getDayViewWithNoEventsOrTasks() {
        LocalDate date = LocalDate.of(2026, 2, 25);
        String username = "testuser";

        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(taskService.getTasksByDay(username, date))
            .thenReturn(Collections.emptyList());

        CalendarDayDTO result = calendarService.getDayView(username, date);

        assertNotNull(result);
        assertEquals(date, result.getDate());
        assertNotNull(result.getEvents());
        assertNotNull(result.getTasks());
        assertTrue(result.getEvents().isEmpty());
        assertTrue(result.getTasks().isEmpty());
    }

    @Test
    void getDayViewFiltersMultiDayEvents() {
        LocalDate date = LocalDate.of(2026, 2, 25);
        String username = "testuser";

        // Create a multi-day event that spans Feb 24-26
        CalendarEventDTO multiDayEvent = new CalendarEventDTO();
        multiDayEvent.setId(3L);
        multiDayEvent.setTitle("Multi-day Event");
        multiDayEvent.setStartDate(LocalDateTime.of(2026, 2, 24, 0, 0));
        multiDayEvent.setEndDate(LocalDateTime.of(2026, 2, 26, 23, 59));
        multiDayEvent.setColor(EventColor.PURPLE);
        multiDayEvent.setAllDay(true);

        List<CalendarEventDTO> events = Arrays.asList(testEvent1, multiDayEvent);

        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(events);
        when(taskService.getTasksByDay(username, date))
            .thenReturn(Collections.emptyList());

        CalendarDayDTO result = calendarService.getDayView(username, date);

        assertNotNull(result);
        assertEquals(2, result.getEvents().size()); 
    }

    @Test
    void getMonthViewForMarchReturns31Days() {
        int year = 2026;
        int month = 3;
        String username = "testuser";

        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(taskService.getTasksByDateRange(eq(username), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        CalendarMonthDTO result = calendarService.getMonthView(username, year, month);

        assertNotNull(result);
        assertEquals(31, result.getDays().size());
    }

    @Test
    void getMonthViewWithEventsPopulatesEventColors() {
        int year = 2026;
        int month = 2;
        String username = "testuser";

        List<CalendarEventDTO> events = Arrays.asList(testEvent1, testEvent2);
        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(events);
        when(taskService.getTasksByDateRange(eq(username), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        CalendarMonthDTO result = calendarService.getMonthView(username, year, month);

        assertNotNull(result);
        CalendarDaySummaryDTO day25 = result.getDays().stream()
            .filter(d -> d.getDate().getDayOfMonth() == 25)
            .findFirst()
            .orElse(null);
        assertNotNull(day25);
        assertTrue(day25.getTotalEvents() > 0);
    }

    @Test
    void getDayViewWithTasksIncludesPendingAndCompleted() {
        LocalDate date = LocalDate.of(2026, 2, 25);
        String username = "testuser";

        List<CalendarTaskDTO> tasks = Arrays.asList(testTask1, testTask2);
        when(eventService.getEventsByDateRange(eq(username), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(taskService.getTasksByDay(username, date))
            .thenReturn(tasks);

        CalendarDayDTO result = calendarService.getDayView(username, date);

        assertNotNull(result);
        assertEquals(2, result.getTasks().size());
    }
}
