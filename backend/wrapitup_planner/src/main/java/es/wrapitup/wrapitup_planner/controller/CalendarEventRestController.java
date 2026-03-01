package es.wrapitup.wrapitup_planner.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.wrapitup.wrapitup_planner.dto.CalendarEventDTO;
import es.wrapitup.wrapitup_planner.service.CalendarEventService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/calendar/events")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class CalendarEventRestController {
    
    private final CalendarEventService eventService;
    
    public CalendarEventRestController(CalendarEventService eventService) {
        this.eventService = eventService;
    }
    
    @GetMapping("")
    public ResponseEntity<?> getEventsByDateRange(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view events"));
        }
        
        try {
            if (start != null && end != null) {
                LocalDateTime startDate = LocalDateTime.parse(start);
                LocalDateTime endDate = LocalDateTime.parse(end);
                List<CalendarEventDTO> events = eventService.getEventsByDateRange(username, startDate, endDate);
                return ResponseEntity.ok(events);
            } else {
                List<CalendarEventDTO> events = eventService.getAllUserEvents(username);
                return ResponseEntity.ok(events);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid date format. Use ISO format (YYYY-MM-DDTHH:mm:ss)"));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody CalendarEventDTO eventDTO, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to create events"));
        }
        
        try {
            CalendarEventDTO created = eventService.createEvent(eventDTO, username);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.getId())
                    .toUri();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(location)
                    .body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody CalendarEventDTO eventDTO, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to update events"));
        }
        
        try {
            CalendarEventDTO updated = eventService.updateEvent(id, eventDTO, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to delete events"));
        }
        
        try {
            eventService.deleteEvent(id, username);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
