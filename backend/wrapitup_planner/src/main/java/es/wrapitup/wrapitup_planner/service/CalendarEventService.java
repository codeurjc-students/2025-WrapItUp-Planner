package es.wrapitup.wrapitup_planner.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.wrapitup.wrapitup_planner.dto.CalendarEventDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarEventMapper;
import es.wrapitup.wrapitup_planner.model.CalendarEvent;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CalendarEventRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;

@Service
public class CalendarEventService {

    private static final int TITLE_MAX_LENGTH = 50;
    
    private final CalendarEventRepository eventRepository;
    private final CalendarEventMapper eventMapper;
    private final UserRepository userRepository;
    
    public CalendarEventService(CalendarEventRepository eventRepository, CalendarEventMapper eventMapper, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.userRepository = userRepository;
    }

    private static String normalizeTitle(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= TITLE_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, TITLE_MAX_LENGTH);
    }
    
    private boolean isAdmin(UserModel user) {
        return user != null && user.getRoles() != null && user.getRoles().contains("ADMIN");
    }
    
    @Transactional
    public CalendarEventDTO createEvent(CalendarEventDTO eventDTO, String username) {
        String normalizedTitle = normalizeTitle(eventDTO.getTitle());
        if (normalizedTitle == null || normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("Event title is required");
        }
        
        if (eventDTO.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        
        if (eventDTO.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
        
        if (eventDTO.getEndDate().isBefore(eventDTO.getStartDate())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }
        
        if (eventDTO.getColor() == null) {
            throw new IllegalArgumentException("Event color is required");
        }
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (user.getStatus() == UserStatus.BANNED) {
            throw new SecurityException("Banned users cannot create events");
        }
        
        if (isAdmin(user)) {
            throw new SecurityException("Admins cannot create events");
        }
        
        CalendarEvent event = new CalendarEvent(
            user,
            normalizedTitle,
            eventDTO.getDescription(),
            eventDTO.getStartDate(),
            eventDTO.getEndDate(),
            eventDTO.getColor(),
            eventDTO.getAllDay() != null ? eventDTO.getAllDay() : false
        );
        
        CalendarEvent saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }
    
    @Transactional
    public CalendarEventDTO updateEvent(Long id, CalendarEventDTO eventDTO, String username) {
        Optional<CalendarEvent> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }
        
        CalendarEvent event = eventOpt.get();
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (!event.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only update your own events");
        }
        
        if (eventDTO.getTitle() != null) {
            String normalizedTitle = normalizeTitle(eventDTO.getTitle());
            if (normalizedTitle != null && !normalizedTitle.isEmpty()) {
                event.setTitle(normalizedTitle);
            }
        }
        
        if (eventDTO.getDescription() != null) {
            event.setDescription(eventDTO.getDescription());
        }
        
        if (eventDTO.getStartDate() != null) {
            event.setStartDate(eventDTO.getStartDate());
        }
        
        if (eventDTO.getEndDate() != null) {
            event.setEndDate(eventDTO.getEndDate());
        }
        
        if (event.getEndDate().isBefore(event.getStartDate())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }
        
        if (eventDTO.getColor() != null) {
            event.setColor(eventDTO.getColor());
        }
        
        if (eventDTO.getAllDay() != null) {
            event.setAllDay(eventDTO.getAllDay());
        }
        
        event.setLastModified(LocalDateTime.now());
        
        CalendarEvent saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }
    
    @Transactional
    public void deleteEvent(Long id, String username) {
        Optional<CalendarEvent> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }
        
        CalendarEvent event = eventOpt.get();
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (!event.getUser().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new SecurityException("You can only delete your own events");
        }
        
        eventRepository.delete(event);
    }
    
    public List<CalendarEventDTO> getEventsByDateRange(String username, LocalDateTime startDate, LocalDateTime endDate) {
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        return eventRepository.findEventsByUserAndDateRange(user.getId(), startDate, endDate)
                             .stream()
                             .map(eventMapper::toDto)
                             .collect(Collectors.toList());
    }
    
    public List<CalendarEventDTO> getAllUserEvents(String username) {
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        return eventRepository.findByUserOrderByStartDateAsc(user)
                             .stream()
                             .map(eventMapper::toDto)
                             .collect(Collectors.toList());
    }
}
