package es.wrapitup.wrapitup_planner.system;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.NoteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@Transactional
@SpringBootTest
public class NoteFilteringSystemTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel testUser;
    private UserModel otherUser;

    @BeforeEach
    void setUp() {
        testUser = new UserModel();
        testUser.setUsername("user");
        testUser.setEmail("user@test.com");
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        otherUser = new UserModel();
        otherUser.setUsername("otherUser");
        otherUser.setEmail("otherUserm@test.com");
        otherUser.setRoles(Arrays.asList("USER"));
        otherUser.setStatus(UserStatus.ACTIVE);
        otherUser = userRepository.save(otherUser);
    }

    // Category filtering tests

    @Test
    void findRecentNotesByUserWithSearchFilterReturnsOnlyMatchingNotes() {
        Note note1 = new Note();
        note1.setUser(testUser);
        note1.setTitle("Pythagorean Theorem");
        note1.setOverview("Overview");
        note1.setSummary("Summary");
        note1.setJsonQuestions("{}");
        note1.setVisibility(NoteVisibility.PRIVATE);
        note1.setCategory(NoteCategory.MATHS);
        note1.setLastModified(LocalDateTime.now());
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setUser(testUser);
        note2.setTitle("Algebra Equations");
        note2.setOverview("Overview");
        note2.setSummary("Summary");
        note2.setJsonQuestions("{}");
        note2.setVisibility(NoteVisibility.PRIVATE);
        note2.setCategory(NoteCategory.MATHS);
        note2.setLastModified(LocalDateTime.now().minusDays(1));
        noteRepository.save(note2);

        Note note3 = new Note();
        note3.setUser(testUser);
        note3.setTitle("Biology Basics");
        note3.setOverview("Overview");
        note3.setSummary("Summary");
        note3.setJsonQuestions("{}");
        note3.setVisibility(NoteVisibility.PRIVATE);
        note3.setCategory(NoteCategory.SCIENCE);
        note3.setLastModified(LocalDateTime.now().minusDays(2));
        noteRepository.save(note3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<NoteDTO> result = noteService.findRecentNotesByUser("user", pageable, null, "Pythagorean");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Pythagorean Theorem", result.getContent().get(0).getTitle());
    }

    @Test
    void findRecentNotesByUserWithCategoryAndSearchFilterReturnsCombinedResults() {
        Note mathsNote1 = new Note();
        mathsNote1.setUser(testUser);
        mathsNote1.setTitle("Algebra Advanced");
        mathsNote1.setOverview("Overview");
        mathsNote1.setSummary("Summary");
        mathsNote1.setJsonQuestions("{}");
        mathsNote1.setVisibility(NoteVisibility.PRIVATE);
        mathsNote1.setCategory(NoteCategory.MATHS);
        mathsNote1.setLastModified(LocalDateTime.now());
        noteRepository.save(mathsNote1);

        Note mathsNote2 = new Note();
        mathsNote2.setUser(testUser);
        mathsNote2.setTitle("Geometry Basics");
        mathsNote2.setOverview("Overview");
        mathsNote2.setSummary("Summary");
        mathsNote2.setJsonQuestions("{}");
        mathsNote2.setVisibility(NoteVisibility.PRIVATE);
        mathsNote2.setCategory(NoteCategory.MATHS);
        mathsNote2.setLastModified(LocalDateTime.now().minusDays(1));
        noteRepository.save(mathsNote2);

        Note scienceNote = new Note();
        scienceNote.setUser(testUser);
        scienceNote.setTitle("Algebra in Chemistry");
        scienceNote.setOverview("Overview");
        scienceNote.setSummary("Summary");
        scienceNote.setJsonQuestions("{}");
        scienceNote.setVisibility(NoteVisibility.PRIVATE);
        scienceNote.setCategory(NoteCategory.SCIENCE);
        scienceNote.setLastModified(LocalDateTime.now().minusDays(2));
        noteRepository.save(scienceNote);

        Pageable pageable = PageRequest.of(0, 10);
        Page<NoteDTO> result = noteService.findRecentNotesByUser("user", pageable, "MATHS", "Algebra");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Algebra Advanced", result.getContent().get(0).getTitle());
        assertEquals(NoteCategory.MATHS, result.getContent().get(0).getCategory());
    }

    // Shared notes tests

    @Test
    void findNotesSharedWithUserWithSearchFilterReturnsFilteredResults() {
        // Create multiple shared notes
        Note sharedNote1 = new Note();
        sharedNote1.setUser(otherUser);
        sharedNote1.setTitle("Pythagorean Theorem");
        sharedNote1.setOverview("Overview");
        sharedNote1.setSummary("Summary");
        sharedNote1.setJsonQuestions("{}");
        sharedNote1.setVisibility(NoteVisibility.PRIVATE);
        sharedNote1.setCategory(NoteCategory.MATHS);
        sharedNote1.setLastModified(LocalDateTime.now());
        Set<UserModel> sharedWith1 = new HashSet<>();
        sharedWith1.add(testUser);
        sharedNote1.setSharedWith(sharedWith1);
        noteRepository.save(sharedNote1);

        Note sharedNote2 = new Note();
        sharedNote2.setUser(otherUser);
        sharedNote2.setTitle("Biology Basics");
        sharedNote2.setOverview("Overview");
        sharedNote2.setSummary("Summary");
        sharedNote2.setJsonQuestions("{}");
        sharedNote2.setVisibility(NoteVisibility.PRIVATE);
        sharedNote2.setCategory(NoteCategory.SCIENCE);
        sharedNote2.setLastModified(LocalDateTime.now().minusDays(1));
        Set<UserModel> sharedWith2 = new HashSet<>();
        sharedWith2.add(testUser);
        sharedNote2.setSharedWith(sharedWith2);
        noteRepository.save(sharedNote2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<NoteDTO> result = noteService.findNotesSharedWithUser("user", pageable, "Pythagorean");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Pythagorean Theorem", result.getContent().get(0).getTitle());
    }


    @Test
    void findNotesSharedWithUserReturnsEmptyForEmptyUsername() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NoteDTO> result = noteService.findNotesSharedWithUser("", pageable, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
