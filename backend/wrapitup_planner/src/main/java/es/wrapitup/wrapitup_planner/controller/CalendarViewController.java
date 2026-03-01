package es.wrapitup.wrapitup_planner.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.wrapitup.wrapitup_planner.dto.CalendarDayDTO;
import es.wrapitup.wrapitup_planner.dto.CalendarMonthDTO;
import es.wrapitup.wrapitup_planner.service.CalendarService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/calendar")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class CalendarViewController {
    
    private final CalendarService calendarService;
    
    public CalendarViewController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<?> getMonthView(
            @PathVariable int year,
            @PathVariable int month,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view calendar"));
        }
        
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Month must be between 1 and 12"));
            }
            
            CalendarMonthDTO monthView = calendarService.getMonthView(username, year, month);
            return ResponseEntity.ok(monthView);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid date parameters"));
        }
    }
    
    @GetMapping("/day/{year}/{month}/{day}")
    public ResponseEntity<?> getDayView(
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view calendar"));
        }
        
        try {
            LocalDate date = LocalDate.of(year, month, day);
            CalendarDayDTO dayView = calendarService.getDayView(username, date);
            return ResponseEntity.ok(dayView);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid date parameters"));
        }
    }
    
    @GetMapping("/day")
    public ResponseEntity<?> getDayViewByDate(
            @RequestParam String date,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view calendar"));
        }
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            CalendarDayDTO dayView = calendarService.getDayView(username, localDate);
            return ResponseEntity.ok(dayView);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid date format. "));
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
