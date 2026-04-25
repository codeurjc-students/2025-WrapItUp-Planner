package es.wrapitup.wrapitup_planner.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import es.wrapitup.wrapitup_planner.dto.AiNoteResult;
import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.DocumentTextExtractorService;
import es.wrapitup.wrapitup_planner.service.NoteService;
import es.wrapitup.wrapitup_planner.service.OpenAiService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Tag("system")
@Transactional
@SpringBootTest
public class NoteAiSystemTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OpenAiService openAiService;

    @MockBean
    private DocumentTextExtractorService documentTextExtractorService;

    private UserModel testUser;
    private UserModel bannedUser;
    private UserModel adminUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new UserModel();
        testUser.setUsername("aisystemuser");
        testUser.setEmail("aisystem@test.com");
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        bannedUser = new UserModel();
        bannedUser.setUsername("aisystembanned");
        bannedUser.setEmail("aisystembanned@test.com");
        bannedUser.setRoles(Arrays.asList("USER"));
        bannedUser.setStatus(UserStatus.BANNED);
        bannedUser = userRepository.save(bannedUser);

        adminUser = new UserModel();
        adminUser.setUsername("aisystemadmin");
        adminUser.setEmail("aisystemadmin@test.com");
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    void createAiNotePersistsData() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "ai text".getBytes());
        AiNoteResult aiNoteResult = new AiNoteResult();
        aiNoteResult.setTitle("AI Title");
        aiNoteResult.setOverview("AI Overview");
        aiNoteResult.setCompleteSummary("AI Summary");

        when(documentTextExtractorService.extractText(any())).thenReturn("ai text");
        when(openAiService.generateNoteFromText("ai text")).thenReturn(aiNoteResult);

        NoteDTO created = noteService.createNoteFromAi(file, "aisystemuser", NoteVisibility.PUBLIC, NoteCategory.ART);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("AI Title", created.getTitle());
        assertEquals(NoteVisibility.PUBLIC, created.getVisibility());
        assertEquals(NoteCategory.ART, created.getCategory());
    }

    @Test
    void createAiNoteDefaultsVisibilityAndCategory() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "ai text".getBytes());
        AiNoteResult aiNoteResult = new AiNoteResult();
        aiNoteResult.setTitle("AI Title");
        aiNoteResult.setOverview("AI Overview");
        aiNoteResult.setCompleteSummary("AI Summary");

        when(documentTextExtractorService.extractText(any())).thenReturn("ai text");
        when(openAiService.generateNoteFromText("ai text")).thenReturn(aiNoteResult);

        NoteDTO created = noteService.createNoteFromAi(file, "aisystemuser", null, null);

        assertEquals(NoteVisibility.PRIVATE, created.getVisibility());
        assertEquals(NoteCategory.OTHERS, created.getCategory());
    }

    @Test
    void createAiNoteBannedUserThrowsSecurityException() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "ai text".getBytes());

        assertThrows(SecurityException.class, () -> {
            noteService.createNoteFromAi(file, "aisystembanned", NoteVisibility.PRIVATE, NoteCategory.OTHERS);
        });
    }

    @Test
    void createAiNoteAdminThrowsSecurityException() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "ai text".getBytes());

        assertThrows(SecurityException.class, () -> {
            noteService.createNoteFromAi(file, "aisystemadmin", NoteVisibility.PRIVATE, NoteCategory.OTHERS);
        });
    }
}
