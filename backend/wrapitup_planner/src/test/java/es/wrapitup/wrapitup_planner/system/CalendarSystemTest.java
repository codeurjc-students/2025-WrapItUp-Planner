package es.wrapitup.wrapitup_planner.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.wrapitup.wrapitup_planner.dto.CalendarDayDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarEventDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarMonthDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarTaskDTO;
import es.wrapitup.wrapitup_planner.model.CalendarEvent;
import es.wrapitup.wrapitup_planner.model.CalendarTask;
import es.wrapitup.wrapitup_planner.model.EventColor;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import es.wrapitup.wrapitup_planner.repository.CalendarEventRepository;
import es.wrapitup.wrapitup_planner.repository.CalendarTaskRepository;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.CalendarEventService;
import es.wrapitup.wrapitup_planner.service.CalendarService;
import es.wrapitup.wrapitup_planner.service.CalendarTaskService;

@Tag("system")
@Transactional
@SpringBootTest
public class CalendarSystemTest {

    @Autowired
    private CalendarEventService eventService;

    @Autowired
    private CalendarTaskService taskService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarEventRepository eventRepository;

    @Autowired
    private CalendarTaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel testUser;
    private UserModel bannedUser;
    private UserModel adminUser;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        noteRepository.deleteAll();
        eventRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserModel();
        testUser.setUsername("calsystemuser");
        testUser.setEmail("calsystem@test.com");
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        bannedUser = new UserModel();
        bannedUser.setUsername("bannedcaluser");
        bannedUser.setEmail("bannedcal@test.com");
        bannedUser.setRoles(Arrays.asList("USER"));
        bannedUser.setStatus(UserStatus.BANNED);
        bannedUser = userRepository.save(bannedUser);

        adminUser = new UserModel();
        adminUser.setUsername("admincaluser");
        adminUser.setEmail("admincal@test.com");
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }

    // Event system tests

    @Test
    void createEventPersistsInDatabase() {
        CalendarEventDTO eventDTO = new CalendarEventDTO();
        eventDTO.setTitle("System Event");
        eventDTO.setDescription("System Description");
        eventDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        eventDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        eventDTO.setColor(EventColor.BLUE);
        eventDTO.setAllDay(false);

        CalendarEventDTO created = eventService.createEvent(eventDTO, "calsystemuser");

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("System Event", created.getTitle());

        CalendarEvent savedEvent = eventRepository.findById(created.getId()).orElse(null);
        assertNotNull(savedEvent);
        assertEquals("System Event", savedEvent.getTitle());
        assertEquals(EventColor.BLUE, savedEvent.getColor());
        assertEquals(testUser.getId(), savedEvent.getUser().getId());
    }

    @Test
    void createEventByBannedUserThrowsException() {
        CalendarEventDTO eventDTO = new CalendarEventDTO();
        eventDTO.setTitle("Event");
        eventDTO.setStartDate(LocalDateTime.now());
        eventDTO.setEndDate(LocalDateTime.now().plusHours(1));
        eventDTO.setColor(EventColor.BLUE);

        assertThrows(SecurityException.class, () -> {
            eventService.createEvent(eventDTO, "bannedcaluser");
        });
    }

    @Test
    void createEventByAdminThrowsException() {
        CalendarEventDTO eventDTO = new CalendarEventDTO();
        eventDTO.setTitle("Event");
        eventDTO.setStartDate(LocalDateTime.now());
        eventDTO.setEndDate(LocalDateTime.now().plusHours(1));
        eventDTO.setColor(EventColor.BLUE);

        assertThrows(SecurityException.class, () -> {
            eventService.createEvent(eventDTO, "admincaluser");
        });
    }

    @Test
    void updateEventModifiesDatabase() {
        CalendarEventDTO eventDTO = new CalendarEventDTO();
        eventDTO.setTitle("Original");
        eventDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        eventDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        eventDTO.setColor(EventColor.BLUE);

        CalendarEventDTO created = eventService.createEvent(eventDTO, "calsystemuser");

        CalendarEventDTO updateDTO = new CalendarEventDTO();
        updateDTO.setTitle("Updated");
        updateDTO.setColor(EventColor.GREEN);
        updateDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 14, 0));
        updateDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 15, 0));

        eventService.updateEvent(created.getId(), updateDTO, "calsystemuser");

        CalendarEvent updatedEvent = eventRepository.findById(created.getId()).orElse(null);
        assertNotNull(updatedEvent);
        assertEquals("Updated", updatedEvent.getTitle());
        assertEquals(EventColor.GREEN, updatedEvent.getColor());
    }

    @Test
    void deleteEventRemovesFromDatabase() {
        CalendarEventDTO eventDTO = new CalendarEventDTO();
        eventDTO.setTitle("To Delete");
        eventDTO.setStartDate(LocalDateTime.now());
        eventDTO.setEndDate(LocalDateTime.now().plusHours(1));
        eventDTO.setColor(EventColor.RED);

        CalendarEventDTO created = eventService.createEvent(eventDTO, "calsystemuser");

        eventService.deleteEvent(created.getId(), "calsystemuser");

        assertFalse(eventRepository.findById(created.getId()).isPresent());
    }

    // Task system tests

    @Test
    void createTaskPersistsInDatabase() {
        CalendarTaskDTO taskDTO = new CalendarTaskDTO();
        taskDTO.setTitle("System Task");
        taskDTO.setDescription("System Task Description");
        taskDTO.setTaskDate(LocalDate.of(2026, 2, 25));

        CalendarTaskDTO created = taskService.createTask(taskDTO, "calsystemuser");

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("System Task", created.getTitle());
        assertFalse(created.getCompleted());

        CalendarTask savedTask = taskRepository.findById(created.getId()).orElse(null);
        assertNotNull(savedTask);
        assertEquals("System Task", savedTask.getTitle());
        assertFalse(savedTask.getCompleted());
        assertEquals(testUser.getId(), savedTask.getUser().getId());
    }

    @Test
    void createTaskByBannedUserThrowsException() {
        CalendarTaskDTO taskDTO = new CalendarTaskDTO();
        taskDTO.setTitle("Task");
        taskDTO.setTaskDate(LocalDate.now());

        assertThrows(SecurityException.class, () -> {
            taskService.createTask(taskDTO, "bannedcaluser");
        });
    }

    @Test
    void createTaskByAdminThrowsException() {
        CalendarTaskDTO taskDTO = new CalendarTaskDTO();
        taskDTO.setTitle("Task");
        taskDTO.setTaskDate(LocalDate.now());

        assertThrows(SecurityException.class, () -> {
            taskService.createTask(taskDTO, "admincaluser");
        });
    }

    @Test
    void toggleTaskCompletePersistsInDatabase() {
        CalendarTaskDTO taskDTO = new CalendarTaskDTO();
        taskDTO.setTitle("Task to Toggle");
        taskDTO.setTaskDate(LocalDate.now());

        CalendarTaskDTO created = taskService.createTask(taskDTO, "calsystemuser");
        assertFalse(created.getCompleted());

        taskService.toggleTaskComplete(created.getId(), "calsystemuser");

        CalendarTask toggledTask = taskRepository.findById(created.getId()).orElse(null);
        assertNotNull(toggledTask);
        assertTrue(toggledTask.getCompleted());

        taskService.toggleTaskComplete(created.getId(), "calsystemuser");

        CalendarTask toggledAgain = taskRepository.findById(created.getId()).orElse(null);
        assertNotNull(toggledAgain);
        assertFalse(toggledAgain.getCompleted());
    }

    @Test
    void deleteTaskRemovesFromDatabase() {
        CalendarTaskDTO taskDTO = new CalendarTaskDTO();
        taskDTO.setTitle("Task to Delete");
        taskDTO.setTaskDate(LocalDate.now());

        CalendarTaskDTO created = taskService.createTask(taskDTO, "calsystemuser");

        taskService.deleteTask(created.getId(), "calsystemuser");

        assertFalse(taskRepository.findById(created.getId()).isPresent());
    }

    // Calendar view system tests

    @Test
    void getMonthViewReturnsCompleteMonthData() {
        CalendarEventDTO event = new CalendarEventDTO();
        event.setTitle("Event");
        event.setStartDate(LocalDateTime.of(2026, 2, 15, 10, 0));
        event.setEndDate(LocalDateTime.of(2026, 2, 15, 11, 0));
        event.setColor(EventColor.BLUE);
        eventService.createEvent(event, "calsystemuser");

        CalendarTaskDTO task = new CalendarTaskDTO();
        task.setTitle("Task");
        task.setTaskDate(LocalDate.of(2026, 2, 15));
        taskService.createTask(task, "calsystemuser");

        CalendarMonthDTO monthView = calendarService.getMonthView("calsystemuser", 2026, 2);

        assertNotNull(monthView);
        assertEquals(2026, monthView.getYear());
        assertEquals(2, monthView.getMonth());
        assertNotNull(monthView.getDays());
        assertEquals(28, monthView.getDays().size()); 
    }

    @Test
    void getDayViewReturnsEventsAndTasks() {
        CalendarEventDTO event = new CalendarEventDTO();
        event.setTitle("Day Event");
        event.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        event.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        event.setColor(EventColor.GREEN);
        eventService.createEvent(event, "calsystemuser");

        CalendarTaskDTO task = new CalendarTaskDTO();
        task.setTitle("Day Task");
        task.setTaskDate(LocalDate.of(2026, 2, 25));
        taskService.createTask(task, "calsystemuser");

        CalendarDayDTO dayView = calendarService.getDayView("calsystemuser", LocalDate.of(2026, 2, 25));

        assertNotNull(dayView);
        assertEquals(LocalDate.of(2026, 2, 25), dayView.getDate());
        assertNotNull(dayView.getEvents());
        assertNotNull(dayView.getTasks());
        assertEquals(1, dayView.getEvents().size());
        assertEquals(1, dayView.getTasks().size());
        assertEquals("Day Event", dayView.getEvents().get(0).getTitle());
        assertEquals("Day Task", dayView.getTasks().get(0).getTitle());
    }

    @Test
    void getEventsByDateRangeFiltersCorrectly() {
        CalendarEventDTO event1 = new CalendarEventDTO();
        event1.setTitle("Event Feb 20");
        event1.setStartDate(LocalDateTime.of(2026, 2, 20, 10, 0));
        event1.setEndDate(LocalDateTime.of(2026, 2, 20, 11, 0));
        event1.setColor(EventColor.BLUE);
        eventService.createEvent(event1, "calsystemuser");

        CalendarEventDTO event2 = new CalendarEventDTO();
        event2.setTitle("Event Feb 25");
        event2.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        event2.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        event2.setColor(EventColor.GREEN);
        eventService.createEvent(event2, "calsystemuser");

        List<CalendarEventDTO> events = eventService.getEventsByDateRange(
            "calsystemuser",
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 2, 28, 23, 59)
        );

        assertNotNull(events);
        assertEquals(2, events.size());
    }

    @Test
    void getPendingTasksReturnsOnlyIncomplete() {
        CalendarTaskDTO task1 = new CalendarTaskDTO();
        task1.setTitle("Pending Task");
        task1.setTaskDate(LocalDate.of(2026, 3, 1));
        taskService.createTask(task1, "calsystemuser");

        CalendarTaskDTO task2 = new CalendarTaskDTO();
        task2.setTitle("Another Task");
        task2.setTaskDate(LocalDate.of(2026, 3, 2));
        CalendarTaskDTO created2 = taskService.createTask(task2, "calsystemuser");

        taskService.toggleTaskComplete(created2.getId(), "calsystemuser");

        List<CalendarTaskDTO> pendingTasks = taskService.getPendingTasks("calsystemuser");

        assertNotNull(pendingTasks);
        assertEquals(1, pendingTasks.size());
        assertEquals("Pending Task", pendingTasks.get(0).getTitle());
        assertFalse(pendingTasks.get(0).getCompleted());
    }
}
