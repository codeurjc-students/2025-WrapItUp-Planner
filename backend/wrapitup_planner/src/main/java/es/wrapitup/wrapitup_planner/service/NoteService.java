package es.wrapitup.wrapitup_planner.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.NoteMapper;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public NoteService(NoteRepository noteRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }

    public Optional<NoteDTO> findById(Long id) {
        return noteRepository.findById(id)
                               .map(noteMapper::toDto);
    }
}
