package es.wrapitup.wrapitup_planner.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/notes")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class NoteRestController {
    @Autowired
    NoteService noteService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getNote(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        
        Optional<NoteDTO> noteCheck = noteService.findById(id);
        if (noteCheck.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Note not found"));
        }
        
        
        Optional<NoteDTO> note = noteService.findByIdWithPermissions(id, username);
        
        if (note.isPresent()) {
            return ResponseEntity.ok(note.get());
        }
        
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to view this note"));
        }
        
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("You do not have permission to view this note"));
    }

    @PostMapping
    public ResponseEntity<?> createNote(@RequestBody NoteDTO noteDTO, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to create a note"));
        }
        
        try {
            NoteDTO created = noteService.createNote(noteDTO, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody NoteDTO noteDTO, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        String username = principal != null ? principal.getName() : null;
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to edit this note"));
        }
        
        try {
            Optional<NoteDTO> updated = noteService.updateNote(id, noteDTO, username);
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("You do not have permission to edit this note"));
            }
            return ResponseEntity.ok(updated.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/share")
    public ResponseEntity<NoteDTO> shareNote(@PathVariable Long id, @RequestBody Long[] userIds) {
        Optional<NoteDTO> updated = noteService.shareNoteWithUsers(id, userIds);
        return updated.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/share-username")
    public ResponseEntity<?> shareNoteByUsername(@PathVariable Long id, @RequestBody ShareRequest request, HttpServletRequest httpRequest) {
        Principal principal = httpRequest.getUserPrincipal();
        String ownerUsername = principal != null ? principal.getName() : null;
        
        if (ownerUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("You must log in to share notes"));
        }
        
        Optional<NoteDTO> updated = noteService.shareNoteWithUsername(id, request.getUsername(), ownerUsername);
        if (updated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }
        return ResponseEntity.ok(updated.get());
    }

    static class ShareRequest {
        private String username;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
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

