package es.wrapitup.wrapitup_planner.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.wrapitup.wrapitup_planner.dto.CalendarTaskDTO;
import es.wrapitup.wrapitup_planner.service.CalendarTaskService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/calendar/tasks")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class CalendarTaskRestController {
    
    private final CalendarTaskService taskService;
    
    public CalendarTaskRestController(CalendarTaskService taskService) {
        this.taskService = taskService;
    }
    
    @GetMapping("")
    public ResponseEntity<?> getTasks(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "false") boolean pendingOnly,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view tasks"));
        }
        
        try {
            List<CalendarTaskDTO> tasks;
            
            if (date != null) {
                
                LocalDate taskDate = LocalDate.parse(date);
                tasks = taskService.getTasksByDay(username, taskDate);
            } else if (start != null && end != null) {
                LocalDate startDate = LocalDate.parse(start);
                LocalDate endDate = LocalDate.parse(end);
                tasks = taskService.getTasksByDateRange(username, startDate, endDate);
            } else if (pendingOnly) {
                tasks = taskService.getPendingTasks(username);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Please provide date"));
            }
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid date format."));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CalendarTaskDTO taskDTO, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to create tasks"));
        }
        
        try {
            CalendarTaskDTO created = taskService.createTask(taskDTO, username);
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
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody CalendarTaskDTO taskDTO, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to update tasks"));
        }
        
        try {
            CalendarTaskDTO updated = taskService.updateTask(id, taskDTO, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTaskComplete(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to update tasks"));
        }
        
        try {
            CalendarTaskDTO updated = taskService.toggleTaskComplete(id, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to delete tasks"));
        }
        
        try {
            taskService.deleteTask(id, username);
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
