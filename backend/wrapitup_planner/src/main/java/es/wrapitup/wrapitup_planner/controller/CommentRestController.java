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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.wrapitup.wrapitup_planner.dto.CommentDTO;
import es.wrapitup.wrapitup_planner.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/notes/{noteId}/comments")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class CommentRestController {
    
    private final CommentService commentService;
    
    public CommentRestController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    @GetMapping
    public ResponseEntity<?> getCommentsByNote(
            @PathVariable Long noteId, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (!commentService.canUserAccessComments(noteId, username)) {
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("You must log in to view comments"));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You do not have permission to view comments"));
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> comments = commentService.getCommentsByNoteIdPaginated(noteId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long noteId, @RequestBody CommentDTO commentDTO, 
                                          HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to create a comment"));
        }
        
        if (!commentService.canUserAccessComments(noteId, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You do not have permission to comment on this note"));
        }
        
        try {
            commentDTO.setNoteId(noteId);
            CommentDTO created = commentService.createComment(commentDTO, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long noteId, @PathVariable Long commentId, 
                                          HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to delete a comment"));
        }
        
        try {
            commentService.deleteComment(commentId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
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
