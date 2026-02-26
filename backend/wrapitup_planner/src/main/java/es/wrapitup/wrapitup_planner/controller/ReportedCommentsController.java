package es.wrapitup.wrapitup_planner.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.wrapitup.wrapitup_planner.dto.CommentDTO;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/admin/reported-comments")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class ReportedCommentsController {
    
    private final CommentService commentService;
    private final UserRepository userRepository;
    
    public ReportedCommentsController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }
    
    private boolean isAdmin(String username) {
        if (username == null) {
            return false;
        }
        
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles() != null && user.getRoles().contains("ADMIN"))
                .orElse(false);
    }
    
    @GetMapping
    public ResponseEntity<?> getReportedComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view reported comments"));
        }
        
        if (!isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Only admins can view reported comments"));
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> reportedComments = commentService.getReportedComments(pageable);
        return ResponseEntity.ok(reportedComments);
    }
    
    @PostMapping("/{commentId}/unreport")
    public ResponseEntity<?> unreportComment(@PathVariable Long commentId, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to unreport comments"));
        }
        
        if (!isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Only admins can unreport comments"));
        }
        
        try {
            CommentDTO unreported = commentService.unreportComment(commentId, username);
            return ResponseEntity.ok(unreported);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteReportedComment(@PathVariable Long commentId, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to delete comments"));
        }
        
        if (!isAdmin(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Only admins can delete comments from this view"));
        }
        
        try {
            commentService.deleteComment(commentId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
