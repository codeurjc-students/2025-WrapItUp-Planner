package es.wrapitup.wrapitup_planner.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.wrapitup.wrapitup_planner.dto.AINoteDTO;
import es.wrapitup.wrapitup_planner.service.AINoteService;

@RestController
@RequestMapping("/api/v1/notes")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:9876"})
public class AINoteRestController {
    @Autowired
    AINoteService aiNoteService;

    @GetMapping("/{id}")
    public ResponseEntity<AINoteDTO> getNote(@PathVariable Long id) {
        Optional<AINoteDTO> note = aiNoteService.findById(id);
        if (note.isPresent()) {
            return ResponseEntity.ok(note.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
