package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.QuizScoreRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.DocumentTextExtractorService;
import es.wrapitup.wrapitup_planner.service.NoteService;
import es.wrapitup.wrapitup_planner.service.OpenAiService;

@Tag("unit")
public class NoteServiceGenerateQuizAdminTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private DocumentTextExtractorService documentTextExtractorService;

    @Mock
    private QuizScoreRepository quizScoreRepository;

    @InjectMocks
    private NoteService noteService;

    private Note note;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        note = new Note();
        note.setId(1L);
    }

    @Test
    void generateQuizForNote_admin_throwsSecurityException() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());

        UserModel admin = new UserModel();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRoles(java.util.List.of("ADMIN", "USER"));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThrows(SecurityException.class, () -> noteService.generateQuizForNoteFromAi(1L, file, "admin"));
    }

    @Test
    void generateQuizForNote_openAiFailure_propagatesIllegalStateException() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());

        UserModel owner = new UserModel();
        owner.setId(3L);
        owner.setUsername("owner");
        owner.setRoles(java.util.List.of("USER"));

        note.setUser(owner);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(documentTextExtractorService.extractText(file)).thenReturn("content");
        when(openAiService.generateQuizFromText("content")).thenThrow(new IllegalStateException("AI error"));

        assertThrows(IllegalStateException.class, () -> noteService.generateQuizForNoteFromAi(1L, file, "owner"));
    }
}
