package es.wrapitup.wrapitup_planner.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.wrapitup.wrapitup_planner.dto.CalendarTaskDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarTaskMapper;
import es.wrapitup.wrapitup_planner.model.CalendarTask;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CalendarTaskRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;

@Service
public class CalendarTaskService {
    
    private final CalendarTaskRepository taskRepository;
    private final CalendarTaskMapper taskMapper;
    private final UserRepository userRepository;
    
    public CalendarTaskService(CalendarTaskRepository taskRepository, CalendarTaskMapper taskMapper, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
    }
    
    private boolean isAdmin(UserModel user) {
        return user != null && user.getRoles() != null && user.getRoles().contains("ADMIN");
    }
    
    @Transactional
    public CalendarTaskDTO createTask(CalendarTaskDTO taskDTO, String username) {
        if (taskDTO.getTitle() == null || taskDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }
        
        if (taskDTO.getTaskDate() == null) {
            throw new IllegalArgumentException("Task date is required");
        }
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (user.getStatus() == UserStatus.BANNED) {
            throw new SecurityException("Banned users cannot create tasks");
        }
        
        if (isAdmin(user)) {
            throw new SecurityException("Admins cannot create tasks");
        }
        
        CalendarTask task = new CalendarTask(
            user,
            taskDTO.getTitle(),
            taskDTO.getDescription(),
            taskDTO.getTaskDate()
        );
        
        CalendarTask saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }
    
    @Transactional
    public CalendarTaskDTO updateTask(Long id, CalendarTaskDTO taskDTO, String username) {
        Optional<CalendarTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        
        CalendarTask task = taskOpt.get();
        

        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        

        if (!task.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only update your own tasks");
        }
        
        if (taskDTO.getTitle() != null && !taskDTO.getTitle().trim().isEmpty()) {
            task.setTitle(taskDTO.getTitle());
        }
        
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }
        
        if (taskDTO.getTaskDate() != null) {
            task.setTaskDate(taskDTO.getTaskDate());
        }
        
        task.setLastModified(LocalDateTime.now());
        
        CalendarTask saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }
    
    @Transactional
    public CalendarTaskDTO toggleTaskComplete(Long id, String username) {
        Optional<CalendarTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        
        CalendarTask task = taskOpt.get();
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (!task.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only update your own tasks");
        }
        
        task.setCompleted(!task.getCompleted());
        task.setLastModified(LocalDateTime.now());
        
        CalendarTask saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }
    
    @Transactional
    public void deleteTask(Long id, String username) {
        Optional<CalendarTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        
        CalendarTask task = taskOpt.get();
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (!task.getUser().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new SecurityException("You can only delete your own tasks");
        }
        
        taskRepository.delete(task);
    }
    
    public List<CalendarTaskDTO> getTasksByDay(String username, LocalDate date) {
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        return taskRepository.findByUserAndTaskDateOrderByCompletedAndCreated(user.getId(), date)
                            .stream()
                            .map(taskMapper::toDto)
                            .collect(Collectors.toList());
    }
    
    public List<CalendarTaskDTO> getTasksByDateRange(String username, LocalDate startDate, LocalDate endDate) {
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        return taskRepository.findByUserAndDateRangeOrderByDate(user.getId(), startDate, endDate)
                            .stream()
                            .map(taskMapper::toDto)
                            .collect(Collectors.toList());
    }
    
    public List<CalendarTaskDTO> getPendingTasks(String username) {
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        return taskRepository.findByUserAndCompletedFalseOrderByTaskDateAsc(user)
                            .stream()
                            .map(taskMapper::toDto)
                            .collect(Collectors.toList());
    }
}
