package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.wrapitup.wrapitup_planner.dto.CalendarEventDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarEventMapper;
import es.wrapitup.wrapitup_planner.model.CalendarEvent;
import es.wrapitup.wrapitup_planner.model.EventColor;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CalendarEventRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.CalendarEventService;

@Tag("unit")
public class CalendarEventServiceUnitTest {

    @Mock
    private CalendarEventRepository eventRepository;

    @Mock
    private CalendarEventMapper eventMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CalendarEventService eventService;

    private UserModel testUser;
    private UserModel otherUser;
    private UserModel adminUser;
    private UserModel bannedUser;
    private CalendarEvent testEvent;
    private CalendarEventDTO testEventDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(java.util.List.of("USER"));
        testUser.setStatus(UserStatus.ACTIVE);

        otherUser = new UserModel();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setRoles(java.util.List.of("USER"));
        otherUser.setStatus(UserStatus.ACTIVE);

        adminUser = new UserModel();
        adminUser.setId(3L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(java.util.List.of("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);

        bannedUser = new UserModel();
        bannedUser.setId(4L);
        bannedUser.setUsername("banneduser");
        bannedUser.setEmail("banned@example.com");
        bannedUser.setRoles(java.util.List.of("USER"));
        bannedUser.setStatus(UserStatus.BANNED);

        testEvent = new CalendarEvent();
        testEvent.setId(1L);
        testEvent.setUser(testUser);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        testEvent.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        testEvent.setColor(EventColor.BLUE);
        testEvent.setAllDay(false);

        testEventDTO = new CalendarEventDTO();
        testEventDTO.setId(1L);
        testEventDTO.setUserId(1L);
        testEventDTO.setTitle("Test Event");
        testEventDTO.setDescription("Test Description");
        testEventDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 10, 0));
        testEventDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        testEventDTO.setColor(EventColor.BLUE);
        testEventDTO.setAllDay(false);
    }

    // Create event tests

    @Test
    void createEventSuccessful() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(testEvent);
        when(eventMapper.toDto(testEvent)).thenReturn(testEventDTO);

        CalendarEventDTO result = eventService.createEvent(testEventDTO, "testuser");

        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        verify(eventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void createEventWithEmptyTitleThrowsException() {
        testEventDTO.setTitle("");

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void createEventWithNullTitleThrowsException() {
        testEventDTO.setTitle(null);

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });
    }

    @Test
    void createEventWithNullStartDateThrowsException() {
        testEventDTO.setStartDate(null);

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });
    }

    @Test
    void createEventWithNullEndDateThrowsException() {
        testEventDTO.setEndDate(null);

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });
    }

    @Test
    void createEventWithEndDateBeforeStartDateThrowsException() {
        testEventDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 11, 0));
        testEventDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 10, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });
    }

    @Test
    void createEventWithNullColorThrowsException() {
        testEventDTO.setColor(null);

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });
    }

    @Test
    void createEventWithBannedUserThrowsSecurityException() {
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            eventService.createEvent(testEventDTO, "banneduser");
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void createEventWithAdminUserThrowsSecurityException() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertThrows(SecurityException.class, () -> {
            eventService.createEvent(testEventDTO, "admin");
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    // Update event tests

    @Test
    void updateEventSuccessful() {
        CalendarEventDTO updateDTO = new CalendarEventDTO();
        updateDTO.setTitle("Updated Event");
        updateDTO.setDescription("Updated Description");
        updateDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 14, 0));
        updateDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 15, 0));
        updateDTO.setColor(EventColor.GREEN);
        updateDTO.setAllDay(true);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(testEvent);
        when(eventMapper.toDto(testEvent)).thenReturn(testEventDTO);

        CalendarEventDTO result = eventService.updateEvent(1L, updateDTO, "testuser");

        assertNotNull(result);
        verify(eventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void updateEventWithNonExistentEventThrowsException() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        CalendarEventDTO updateDTO = new CalendarEventDTO();
        updateDTO.setTitle("Updated");

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(999L, updateDTO, "testuser");
        });
    }

    @Test
    void updateEventByNonOwnerThrowsSecurityException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        CalendarEventDTO updateDTO = new CalendarEventDTO();
        updateDTO.setTitle("Updated");

        assertThrows(SecurityException.class, () -> {
            eventService.updateEvent(1L, updateDTO, "otheruser");
        });
    }

    @Test
    void updateEventWithInvalidDateRangeThrowsException() {
        CalendarEventDTO updateDTO = new CalendarEventDTO();
        updateDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 15, 0));
        updateDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 10, 0));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(1L, updateDTO, "testuser");
        });
    }

    // Delete event tests

    @Test
    void deleteEventSuccessful() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        eventService.deleteEvent(1L, "testuser");

        verify(eventRepository).delete(testEvent);
    }

    @Test
    void deleteEventByAdminSuccessful() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        eventService.deleteEvent(1L, "admin");

        verify(eventRepository).delete(testEvent);
    }

    @Test
    void deleteEventWithNonExistentEventThrowsException() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(999L, "testuser");
        });
    }

    @Test
    void deleteEventByNonOwnerThrowsSecurityException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        assertThrows(SecurityException.class, () -> {
            eventService.deleteEvent(1L, "otheruser");
        });

        verify(eventRepository, never()).delete(any(CalendarEvent.class));
    }

    // Get events tests

    @Test
    void getEventsByDateRangeReturnsEvents() {
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 25, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 25, 23, 59);

        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventRepository.findEventsByUserAndDateRange(1L, startDate, endDate)).thenReturn(events);
        when(eventMapper.toDto(testEvent)).thenReturn(testEventDTO);

        List<CalendarEventDTO> result = eventService.getEventsByDateRange("testuser", startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }

    @Test
    void getEventsByDateRangeWithNonExistentUserThrowsException() {
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 25, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 25, 23, 59);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEventsByDateRange("nonexistent", startDate, endDate);
        });
    }

    @Test
    void getAllUserEventsReturnsEvents() {
        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventRepository.findByUserOrderByStartDateAsc(testUser)).thenReturn(events);
        when(eventMapper.toDto(testEvent)).thenReturn(testEventDTO);

        List<CalendarEventDTO> result = eventService.getAllUserEvents("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }

    @Test
    void getAllUserEventsWithNonExistentUserThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.getAllUserEvents("nonexistent");
        });
    }

    @Test
    void createEventWithStartDateAfterEndDateThrowsException() {
        testEventDTO.setStartDate(LocalDateTime.of(2026, 2, 25, 15, 0));
        testEventDTO.setEndDate(LocalDateTime.of(2026, 2, 25, 10, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, "testuser");
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void createEventByBannedUserThrowsSecurityException() {
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            eventService.createEvent(testEventDTO, "banneduser");
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void updateEventByBannedUserThrowsSecurityException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            eventService.updateEvent(1L, testEventDTO, "banneduser");
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void deleteEventByBannedUserThrowsSecurityException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            eventService.deleteEvent(1L, "banneduser");
        });

        verify(eventRepository, never()).delete(any(CalendarEvent.class));
    }

    @Test
    void getEventsByDateRangeWithInvalidUserThrowsException() {
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 25, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 25, 23, 59);

        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEventsByDateRange("invaliduser", startDate, endDate);
        });
    }

    @Test
    void createEventWithNullUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO, null);
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void updateEventWithNullUsernameThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(1L, testEventDTO, null);
        });

        verify(eventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    void deleteEventWithNullUsernameThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(1L, null);
        });

        verify(eventRepository, never()).delete(any(CalendarEvent.class));
    }

    @Test
    void getAllUserEventsWithNullUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.getAllUserEvents(null);
        });
    }
}
