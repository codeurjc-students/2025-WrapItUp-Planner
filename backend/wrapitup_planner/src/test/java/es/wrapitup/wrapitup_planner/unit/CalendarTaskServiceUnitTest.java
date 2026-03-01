package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.wrapitup.wrapitup_planner.dto.CalendarTaskDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarTaskMapper;
import es.wrapitup.wrapitup_planner.model.CalendarTask;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CalendarTaskRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.CalendarTaskService;

@Tag("unit")
public class CalendarTaskServiceUnitTest {

    @Mock
    private CalendarTaskRepository taskRepository;

    @Mock
    private CalendarTaskMapper taskMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CalendarTaskService taskService;

    private UserModel testUser;
    private UserModel otherUser;
    private UserModel adminUser;
    private UserModel bannedUser;
    private CalendarTask testTask;
    private CalendarTaskDTO testTaskDTO;

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

        testTask = new CalendarTask();
        testTask.setId(1L);
        testTask.setUser(testUser);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskDate(LocalDate.of(2026, 2, 25));
        testTask.setCompleted(false);

        testTaskDTO = new CalendarTaskDTO();
        testTaskDTO.setId(1L);
        testTaskDTO.setUserId(1L);
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setTaskDate(LocalDate.of(2026, 2, 25));
        testTaskDTO.setCompleted(false);
    }

    // Create task tests

    @Test
    void createTaskSuccessful() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(CalendarTask.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);

        CalendarTaskDTO result = taskService.createTask(testTaskDTO, "testuser");

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(any(CalendarTask.class));
    }

    @Test
    void createTaskWithEmptyTitleThrowsException() {
        testTaskDTO.setTitle("");

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testTaskDTO, "testuser");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void createTaskWithNullTitleThrowsException() {
        testTaskDTO.setTitle(null);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testTaskDTO, "testuser");
        });
    }

    @Test
    void createTaskWithNullTaskDateThrowsException() {
        testTaskDTO.setTaskDate(null);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testTaskDTO, "testuser");
        });
    }

    @Test
    void createTaskWithNonExistentUserThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testTaskDTO, "nonexistent");
        });
    }

    @Test
    void createTaskWithBannedUserThrowsSecurityException() {
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            taskService.createTask(testTaskDTO, "banneduser");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void createTaskWithAdminUserThrowsSecurityException() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertThrows(SecurityException.class, () -> {
            taskService.createTask(testTaskDTO, "admin");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    // Update task tests

    @Test
    void updateTaskSuccessful() {
        CalendarTaskDTO updateDTO = new CalendarTaskDTO();
        updateDTO.setTitle("Updated Task");
        updateDTO.setDescription("Updated Description");
        updateDTO.setTaskDate(LocalDate.of(2026, 2, 26));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(CalendarTask.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);

        CalendarTaskDTO result = taskService.updateTask(1L, updateDTO, "testuser");

        assertNotNull(result);
        verify(taskRepository).save(any(CalendarTask.class));
    }

    @Test
    void updateTaskWithNonExistentTaskThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        CalendarTaskDTO updateDTO = new CalendarTaskDTO();
        updateDTO.setTitle("Updated");

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(999L, updateDTO, "testuser");
        });
    }

    @Test
    void updateTaskByNonOwnerThrowsSecurityException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        CalendarTaskDTO updateDTO = new CalendarTaskDTO();
        updateDTO.setTitle("Updated");

        assertThrows(SecurityException.class, () -> {
            taskService.updateTask(1L, updateDTO, "otheruser");
        });
    }

    // Toggle task completion tests

    @Test
    void toggleTaskCompleteSuccessful() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(CalendarTask.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);

        CalendarTaskDTO result = taskService.toggleTaskComplete(1L, "testuser");

        assertNotNull(result);
        verify(taskRepository).save(any(CalendarTask.class));
    }

    @Test
    void toggleTaskCompleteWithNonExistentTaskThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.toggleTaskComplete(999L, "testuser");
        });
    }

    @Test
    void toggleTaskCompleteByNonOwnerThrowsSecurityException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        assertThrows(SecurityException.class, () -> {
            taskService.toggleTaskComplete(1L, "otheruser");
        });
    }

    // Delete task tests

    @Test
    void deleteTaskSuccessful() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        taskService.deleteTask(1L, "testuser");

        verify(taskRepository).delete(testTask);
    }

    @Test
    void deleteTaskByAdminSuccessful() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        taskService.deleteTask(1L, "admin");

        verify(taskRepository).delete(testTask);
    }

    @Test
    void deleteTaskWithNonExistentTaskThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(999L, "testuser");
        });
    }

    @Test
    void deleteTaskByNonOwnerThrowsSecurityException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        assertThrows(SecurityException.class, () -> {
            taskService.deleteTask(1L, "otheruser");
        });

        verify(taskRepository, never()).delete(any(CalendarTask.class));
    }

    // Get tasks tests

    @Test
    void getTasksByDayReturnsTasks() {
        LocalDate date = LocalDate.of(2026, 2, 25);

        List<CalendarTask> tasks = Arrays.asList(testTask);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByUserAndTaskDateOrderByCompletedAndCreated(1L, date)).thenReturn(tasks);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);

        List<CalendarTaskDTO> result = taskService.getTasksByDay("testuser", date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
    }

    @Test
    void getTasksByDateRangeReturnsTasks() {
        LocalDate startDate = LocalDate.of(2026, 2, 25);
        LocalDate endDate = LocalDate.of(2026, 2, 28);

        List<CalendarTask> tasks = Arrays.asList(testTask);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByUserAndDateRangeOrderByDate(1L, startDate, endDate)).thenReturn(tasks);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);

        List<CalendarTaskDTO> result = taskService.getTasksByDateRange("testuser", startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
    }

    @Test
    void getTasksByDateRangeWithNonExistentUserThrowsException() {
        LocalDate startDate = LocalDate.of(2026, 2, 25);
        LocalDate endDate = LocalDate.of(2026, 2, 28);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksByDateRange("nonexistent", startDate, endDate);
        });
    }

    @Test
    void getPendingTasksReturnsTasks() {
        List<CalendarTask> tasks = Arrays.asList(testTask);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByUserAndCompletedFalseOrderByTaskDateAsc(testUser)).thenReturn(tasks);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);

        List<CalendarTaskDTO> result = taskService.getPendingTasks("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
        assertFalse(result.get(0).getCompleted());
    }

    @Test
    void getPendingTasksWithNonExistentUserThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getPendingTasks("nonexistent");
        });
    }

    @Test
    void createTaskByBannedUserThrowsSecurityException() {
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            taskService.createTask(testTaskDTO, "banneduser");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void toggleTaskCompleteChangesStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(CalendarTask.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDTO);
        testTask.setCompleted(false);

        CalendarTaskDTO result = taskService.toggleTaskComplete(1L, "testuser");

        assertNotNull(result);
        verify(taskRepository).save(any(CalendarTask.class));
    }

    @Test
    void updateTaskByBannedUserThrowsSecurityException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            taskService.updateTask(1L, testTaskDTO, "banneduser");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void deleteTaskByBannedUserThrowsSecurityException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            taskService.deleteTask(1L, "banneduser");
        });

        verify(taskRepository, never()).delete(any(CalendarTask.class));
    }

    @Test
    void toggleTaskCompleteByBannedUserThrowsSecurityException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            taskService.toggleTaskComplete(1L, "banneduser");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void getTasksByDayWithInvalidUserThrowsException() {
        LocalDate date = LocalDate.of(2026, 2, 25);

        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksByDay("invaliduser", date);
        });
    }

    @Test
    void updateTaskWithEmptyTitleThrowsException() {
        CalendarTaskDTO updateDTO = new CalendarTaskDTO();
        updateDTO.setTitle("");
        updateDTO.setTaskDate(LocalDate.of(2026, 2, 25));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(1L, updateDTO, "testuser");
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void createTaskWithNullUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testTaskDTO, null);
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void updateTaskWithNullUsernameThrowsException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(1L, testTaskDTO, null);
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void toggleTaskCompleteWithNullUsernameThrowsException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.toggleTaskComplete(1L, null);
        });

        verify(taskRepository, never()).save(any(CalendarTask.class));
    }

    @Test
    void getTasksByDayWithNullUsernameThrowsException() {
        LocalDate date = LocalDate.of(2026, 2, 25);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksByDay(null, date);
        });
    }

    @Test
    void getTasksByDateRangeWithNullUsernameThrowsException() {
        LocalDate startDate = LocalDate.of(2026, 2, 25);
        LocalDate endDate = LocalDate.of(2026, 2, 28);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksByDateRange(null, startDate, endDate);
        });
    }

    @Test
    void getPendingTasksWithNullUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getPendingTasks(null);
        });
    }
}
