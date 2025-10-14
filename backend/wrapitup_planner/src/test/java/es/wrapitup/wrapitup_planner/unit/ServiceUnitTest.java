package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import es.wrapitup.wrapitup_planner.dto.AINoteDTO;
import es.wrapitup.wrapitup_planner.dto.AINoteMapper;
import es.wrapitup.wrapitup_planner.model.AINote;
import es.wrapitup.wrapitup_planner.repository.AINoteRepository;
import es.wrapitup.wrapitup_planner.service.AINoteService;

@Tag("unit") 
public class ServiceUnitTest {

    @Mock
    private AINoteRepository aiNoteRepository;

    @Mock
    private AINoteMapper aiNoteMapper;

    @InjectMocks
    private AINoteService aiNoteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByIdReturnsDTO() {
        AINote note = new AINote();
        note.setId(1L);
        note.setOverview("Resumen general de la sesión de IA");
        note.setSummary("Este es el contenido detallado del resumen");
        note.setJsonQuestions("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}");
        note.setVisibility(true);

        AINoteDTO dto = new AINoteDTO();
        dto.setId(1L);
        dto.setOverview("Resumen general de la sesión de IA");
        dto.setSummary("Este es el contenido detallado del resumen");
        dto.setJsonQuestions("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}");
        dto.setVisibility(true);

        when(aiNoteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(aiNoteMapper.toDto(note)).thenReturn(dto);

        Optional<AINoteDTO> result = aiNoteService.findById(1L);

        assertEquals(true, result.isPresent());
        assertEquals(dto.getId(), result.get().getId());
        assertEquals(dto.getOverview(), result.get().getOverview());
    }

}
