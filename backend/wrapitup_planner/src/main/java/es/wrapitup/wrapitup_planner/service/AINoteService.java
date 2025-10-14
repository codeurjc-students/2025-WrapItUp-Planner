package es.wrapitup.wrapitup_planner.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.AINoteDTO;
import es.wrapitup.wrapitup_planner.dto.AINoteMapper;
import es.wrapitup.wrapitup_planner.repository.AINoteRepository;

@Service
public class AINoteService {
    private final AINoteRepository aiNoteRepository;
    private final AINoteMapper aiNoteMapper;

    public AINoteService(AINoteRepository aiNoteRepository, AINoteMapper aiNoteMapper) {
        this.aiNoteRepository = aiNoteRepository;
        this.aiNoteMapper = aiNoteMapper;
    }

    public Optional<AINoteDTO> findById(Long id) {
        return aiNoteRepository.findById(id)
                               .map(aiNoteMapper::toDto);
    }
}
