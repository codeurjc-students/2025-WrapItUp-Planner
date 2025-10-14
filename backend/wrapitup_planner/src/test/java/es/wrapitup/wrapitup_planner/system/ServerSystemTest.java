package es.wrapitup.wrapitup_planner.system;

import es.wrapitup.wrapitup_planner.dto.AINoteDTO;
import es.wrapitup.wrapitup_planner.model.AINote;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.repository.AINoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.AINoteService;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("system")
@Transactional
@SpringBootTest
public class ServerSystemTest {

    @Autowired
    private AINoteService aiNoteService;

    @Autowired
    private AINoteRepository aiNoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByIdWithSqlDatabase() {

        UserModel user = new UserModel();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole("USER");
        user.setStatus("ACTIVE");


        userRepository.save(user);

        AINote note = new AINote();
        note.setOverview("Resumen general de la sesión de IA");
        note.setSummary("Este es el contenido detallado del resumen");
        note.setJsonQuestions("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}");
        note.setVisibility(true);
        note.setUser(user);
        aiNoteRepository.save(note);
        Optional<AINoteDTO> result = aiNoteService.findById(note.getId());

        assertEquals(true, result.isPresent());
        assertEquals("Resumen general de la sesión de IA", result.get().getOverview());
        assertEquals("Este es el contenido detallado del resumen", result.get().getSummary());
        assertEquals("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}", result.get().getJsonQuestions());
        assertEquals(true, result.get().getVisibility());
    }
}
