package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.NoteMapper;
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
public class NoteServiceGenerateQuizUnitTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

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

    private UserModel owner;
    private UserModel other;
    private Note note;
    private NoteDTO noteDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        owner = new UserModel();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setRoles(java.util.List.of("USER"));

        other = new UserModel();
        other.setId(2L);
        other.setUsername("other");
        other.setRoles(java.util.List.of("USER"));

        note = new Note();
        note.setId(10L);
        note.setUser(owner);
        note.setJsonQuestions("");
        note.setLastModified(LocalDateTime.now());

        noteDTO = new NoteDTO();
        noteDTO.setId(10L);
    }

    @Test
    void generateQuizForNote_success() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());

        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(documentTextExtractorService.extractText(file)).thenReturn("content");
        when(openAiService.generateQuizFromText("content")).thenReturn("{\"questions\":[{\"question\":\"q1\",\"options\":[\"a\",\"b\",\"c\",\"d\"],\"correctOptionIndex\":0},{\"question\":\"q2\",\"options\":[\"a\",\"b\",\"c\",\"d\"],\"correctOptionIndex\":1},{\"question\":\"q3\",\"options\":[\"a\",\"b\",\"c\",\"d\"],\"correctOptionIndex\":2},{\"question\":\"q4\",\"options\":[\"a\",\"b\",\"c\",\"d\"],\"correctOptionIndex\":3},{\"question\":\"q5\",\"options\":[\"a\",\"b\",\"c\",\"d\"],\"correctOptionIndex\":0}]}\n");
        when(noteRepository.save(any())).thenReturn(note);
        when(noteMapper.toDto(note)).thenReturn(noteDTO);

        var res = noteService.generateQuizForNoteFromAi(10L, file, "owner");

        assertTrue(res.isPresent());
        verify(noteRepository).save(any());
    }

    @Test
    void generateQuizForNote_noteNotFound_returnsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());

        when(noteRepository.findById(10L)).thenReturn(Optional.empty());

        var res = noteService.generateQuizForNoteFromAi(10L, file, "owner");

        assertTrue(res.isEmpty());
    }

    @Test
    void generateQuizForNote_userNotFound_returnsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());

        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.empty());

        var res = noteService.generateQuizForNoteFromAi(10L, file, "owner");

        assertTrue(res.isEmpty());
    }

    @Test
    void generateQuizForNote_nonOwner_throwsSecurityException() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());

        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

        assertThrows(SecurityException.class, () -> {
            noteService.generateQuizForNoteFromAi(10L, file, "other");
        });
    }

    @Test
    void generateQuizForNote_bannedUser_throwsSecurityException() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", "content".getBytes());
        UserModel banned = new UserModel();
        banned.setId(3L);
        banned.setUsername("b");
        banned.setStatus(UserStatus.BANNED);

        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        when(userRepository.findByUsername("b")).thenReturn(Optional.of(banned));

        assertThrows(SecurityException.class, () -> {
            noteService.generateQuizForNoteFromAi(10L, file, "b");
        });
    }
}
