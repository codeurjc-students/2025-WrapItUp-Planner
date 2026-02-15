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
import java.time.LocalDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@Transactional
@SpringBootTest
public class NoteSystemTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel testUser;
    private UserModel otherUser;
    private UserModel adminUser;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserModel();
        testUser.setUsername("systemuser");
        testUser.setEmail("system@test.com");
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        otherUser = new UserModel();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@test.com");
        otherUser.setRoles(Arrays.asList("USER"));
        otherUser.setStatus(UserStatus.ACTIVE);
        otherUser = userRepository.save(otherUser);

        adminUser = new UserModel();
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@test.com");
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }

    // note creation tests

    @Test
    void createNoteAndPersistsInDDBB() {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle("System Test Note");
        noteDTO.setOverview("System Overview");
        noteDTO.setSummary("System Summary");
        noteDTO.setJsonQuestions("{\"q\":\"test\"}");
        noteDTO.setVisibility(NoteVisibility.PRIVATE);
        noteDTO.setCategory(NoteCategory.SCIENCE);

        NoteDTO created = noteService.createNote(noteDTO, "systemuser");

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("System Test Note", created.getTitle());
        assertNotNull(created.getLastModified());
        assertEquals(NoteCategory.SCIENCE, created.getCategory());

        
        Optional<Note> savedNote = noteRepository.findById(created.getId());
        assertTrue(savedNote.isPresent());
        assertEquals("System Test Note", savedNote.get().getTitle());
        assertEquals("System Overview", savedNote.get().getOverview());
        assertEquals(testUser.getId(), savedNote.get().getUser().getId());
        assertEquals(NoteCategory.SCIENCE, savedNote.get().getCategory());
        assertNotNull(savedNote.get().getLastModified());
    }


    @Test
    void createNoteWithInvalidUserThrowsException() {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle("Note");

        assertThrows(IllegalArgumentException.class, () -> {
            noteService.createNote(noteDTO, "nonexistent");
        });
    }

    // get notes tests

    @Test
    void findByIdPublicNoteAndItsFound() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PUBLIC);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        Optional<NoteDTO> result = noteService.findById(note.getId());

        assertTrue(result.isPresent());
        assertEquals("Note", result.get().getTitle());
        assertEquals("Overview", result.get().getOverview());
    }

    @Test
    void findByIdPublicNoAuth() {
        Note publicNote = new Note();
        publicNote.setUser(testUser);
        publicNote.setTitle("Public Note");
        publicNote.setOverview("Public");
        publicNote.setSummary("Summary");
        publicNote.setJsonQuestions("{}");
        publicNote.setVisibility(NoteVisibility.PUBLIC);
        publicNote.setCategory(NoteCategory.OTHERS);
        publicNote.setLastModified(LocalDateTime.now());
        publicNote = noteRepository.save(publicNote);

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(publicNote.getId(), null);

        assertTrue(result.isPresent());
        assertEquals("Public Note", result.get().getTitle());
    }

    @Test
    void findByIdPrivateNoteOnlyOwnerHasAccess() {
        Note privateNote = new Note();
        privateNote.setUser(testUser);
        privateNote.setTitle("Private Note");
        privateNote.setOverview("Private");
        privateNote.setSummary("Summary");
        privateNote.setJsonQuestions("{}");
        privateNote.setVisibility(NoteVisibility.PRIVATE);
        privateNote.setCategory(NoteCategory.OTHERS);
        privateNote.setLastModified(LocalDateTime.now());
        privateNote = noteRepository.save(privateNote);

        // Owner has access
        Optional<NoteDTO> ownerResult = noteService.findByIdWithPermissions(privateNote.getId(), "systemuser");
        assertTrue(ownerResult.isPresent());

        // Second user has no access
        Optional<NoteDTO> otherResult = noteService.findByIdWithPermissions(privateNote.getId(), "otheruser");
        assertFalse(otherResult.isPresent());

        // Not auth user has no access
        Optional<NoteDTO> unauthResult = noteService.findByIdWithPermissions(privateNote.getId(), null);
        assertFalse(unauthResult.isPresent());
    }

    @Test
    void findByIdNoteSharedUserHassAccess() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Shared Note");
        note.setOverview("Shared");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        
        Set<UserModel> sharedWith = new HashSet<>();
        sharedWith.add(otherUser);
        note.setSharedWith(sharedWith);
        note = noteRepository.save(note);

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(note.getId(), "otheruser");

        assertTrue(result.isPresent());
        assertEquals("Shared Note", result.get().getTitle());
    }

    // update notes tests

    @Test
    void updateNoteSuccessfully() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Original Title");
        note.setOverview("Original Overview");
        note.setSummary("Original Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setOverview("Updated Overview");
        updateDTO.setVisibility(NoteVisibility.PUBLIC);

        Optional<NoteDTO> result = noteService.updateNote(note.getId(), updateDTO, "systemuser");

        assertTrue(result.isPresent());

        Note updatedNote = noteRepository.findById(note.getId()).orElse(null);
        assertNotNull(updatedNote);
        assertEquals("Updated Title", updatedNote.getTitle());
        assertEquals("Updated Overview", updatedNote.getOverview());
        assertEquals(NoteVisibility.PUBLIC, updatedNote.getVisibility());
        assertEquals("Original Summary", updatedNote.getSummary()); 
    }


    // share note funcionality tests

    @Test
    void shareNoteToUser() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note to Share");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note.setSharedWith(new HashSet<>());
        note = noteRepository.save(note);

        Optional<NoteDTO> result = noteService.shareNoteWithUsername(note.getId(), "otheruser", "systemuser");

        assertTrue(result.isPresent());

        Note sharedNote = noteRepository.findById(note.getId()).orElse(null);
        assertNotNull(sharedNote);
        assertEquals(1, sharedNote.getSharedWith().size());
        assertTrue(sharedNote.getSharedWith().stream()
            .anyMatch(u -> u.getUsername().equals("otheruser")));
    }

    @Test
    void userCannotShareNoteWithThemselves() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note.setSharedWith(new HashSet<>());
        note = noteRepository.save(note);

        Optional<NoteDTO> result = noteService.shareNoteWithUsername(note.getId(), "systemuser", "systemuser");

        assertFalse(result.isPresent());

    }


    // delete notes tests

    @Test
    void deleteNoteRemovesItFromDDBB() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note to Delete");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        Long noteId = note.getId();

        boolean result = noteService.deleteNote(noteId, "systemuser");

        assertTrue(result);

        Optional<Note> deletedNote = noteRepository.findById(noteId);
        assertFalse(deletedNote.isPresent());
    }

    @Test
    void NonOwnerDeletesNoteNotAllowed() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Protected Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        Long noteId = note.getId();

        boolean result = noteService.deleteNote(noteId, "otheruser");

        assertFalse(result);

        Optional<Note> stillExists = noteRepository.findById(noteId);
        assertTrue(stillExists.isPresent());
    }

    @Test
    void deleteNonExistentNoteReturnsFalse() {
        boolean result = noteService.deleteNote(999999L, "systemuser");

        assertFalse(result);
    }

    // admin tests

    @Test
    void adminCanDeleteAnyNote() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("User's Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        Long noteId = note.getId();


        boolean result = noteService.deleteNote(noteId, "adminuser");

        assertTrue(result);
        Optional<Note> deletedNote = noteRepository.findById(noteId);
        assertFalse(deletedNote.isPresent());
    }

    @Test
    void adminCanAccessPrivateNote() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Private Note");
        note.setOverview("Private Overview");
        note.setSummary("Private Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        Long noteId = note.getId();

       
        Optional<NoteDTO> retrieved = noteService.findByIdWithPermissions(noteId, "adminuser");

        assertTrue(retrieved.isPresent());
        assertEquals("Private Note", retrieved.get().getTitle());
    }

    // Banned user tests

    @Test
    void bannedUserCannotCreateNoteSystemTest() {
        // Ban the user
        testUser.setStatus(UserStatus.BANNED);
        userRepository.save(testUser);

        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle("Banned user note");
        noteDTO.setOverview("This should fail");
        noteDTO.setSummary("Summary");
        noteDTO.setJsonQuestions("{}");
        noteDTO.setVisibility(NoteVisibility.PRIVATE);
        noteDTO.setCategory(NoteCategory.OTHERS);

        assertThrows(SecurityException.class, () -> {
            noteService.createNote(noteDTO, "systemuser");
        });
    }

    @Test
    void bannedUserCannotUpdateNoteSystemTest() {
        // Create note first
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Original Title");
        note.setOverview("Original Overview");
        note.setSummary("Original Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        // Ban the user
        testUser.setStatus(UserStatus.BANNED);
        userRepository.save(testUser);

        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setOverview("Updated Overview");
        updateDTO.setSummary("Updated Summary");
        updateDTO.setJsonQuestions("{}");
        updateDTO.setVisibility(NoteVisibility.PRIVATE);
        updateDTO.setCategory(NoteCategory.OTHERS);

        Long noteId = note.getId();

        assertThrows(SecurityException.class, () -> {
            noteService.updateNote(noteId, updateDTO, "systemuser");
        });
    }

    

}
