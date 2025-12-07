package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.NoteMapper;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.NoteService;

@Tag("unit")
public class NoteServiceUnitTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private UserModel testUser;
    private UserModel otherUser;
    private Note testNote;
    private NoteDTO testNoteDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(java.util.List.of("USER"));

        otherUser = new UserModel();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setRoles(java.util.List.of("USER"));

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setOverview("Test Overview");
        testNote.setSummary("Test Summary");
        testNote.setJsonQuestions("{}");
        testNote.setVisibility(NoteVisibility.PRIVATE);
        testNote.setCategory(NoteCategory.OTHERS);
        testNote.setLastModified(LocalDateTime.now());
        testNote.setUser(testUser);
        testNote.setSharedWith(new HashSet<>());

        testNoteDTO = new NoteDTO();
        testNoteDTO.setId(1L);
        testNoteDTO.setTitle("Test Note");
        testNoteDTO.setOverview("Test Overview");
        testNoteDTO.setSummary("Test Summary");
        testNoteDTO.setJsonQuestions("{}");
        testNoteDTO.setVisibility(NoteVisibility.PRIVATE);
        testNoteDTO.setCategory(NoteCategory.OTHERS);
        testNoteDTO.setLastModified(LocalDateTime.now());
        testNoteDTO.setUserId(1L);
    }

    // create notes tests

    @Test
    void createNoteSuccessful() {
        NoteDTO inputDTO = new NoteDTO();
        inputDTO.setTitle("New Note");
        inputDTO.setOverview("Overview");
        inputDTO.setSummary("Summary");
        inputDTO.setVisibility(NoteVisibility.PUBLIC);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDTO);

        NoteDTO result = noteService.createNote(inputDTO, "testuser");

        assertNotNull(result);
        verify(userRepository).findByUsername("testuser");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void CreateNoteWithNoTitleThrowsException() {
        NoteDTO inputDTO = new NoteDTO();
        inputDTO.setTitle("");
        inputDTO.setOverview("Overview");

        assertThrows(IllegalArgumentException.class, () -> {
            noteService.createNote(inputDTO, "testuser");
        });
    }

    @Test
    void createNoteWithUserNotFoundThrowsException() {
        NoteDTO inputDTO = new NoteDTO();
        inputDTO.setTitle("Title");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            noteService.createNote(inputDTO, "nonexistent");
        });
    }

    // get note tests

    @Test
    void findNoteByIdAndItsFound() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testNoteDTO.getId(), result.get().getId());
    }

    @Test
    void FindByIdNotFound() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<NoteDTO> result = noteService.findById(999L);

        assertFalse(result.isPresent());
    }


    @Test
    void findByIdPublicNoteWithoutLoginAndNoteIsAvailable() {
        testNote.setVisibility(NoteVisibility.PUBLIC);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(1L, null);

        assertTrue(result.isPresent());
    }

    @Test
    void findByIdPrivateNoteNotAvailable() {
        testNote.setVisibility(NoteVisibility.PRIVATE);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(1L, null);

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdByOwnerAndGetsAccess() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(1L, "testuser");

        assertTrue(result.isPresent());
    }

    @Test
    void findByIdPrivateNoteAndUserHasNoAccess() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(1L, "otheruser");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdPrivateNoteAndUserHasAccess() {
        Set<UserModel> sharedWith = new HashSet<>();
        sharedWith.add(otherUser);
        testNote.setSharedWith(sharedWith);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(1L, "otheruser");

        assertTrue(result.isPresent());
    }


    // update notes tests

    @Test
    void UpdateNoteOwnerWithSuccess() {
        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setOverview("Updated Overview");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.updateNote(1L, updateDTO, "testuser");

        assertTrue(result.isPresent());
        verify(noteRepository).save(testNote);
    }

    @Test
    void UpdateNoteNotOwnerWithSuccess() {
        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("Updated Title");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        Optional<NoteDTO> result = noteService.updateNote(1L, updateDTO, "otheruser");

        assertFalse(result.isPresent());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void UpdateNonExistentNoteReturnsFalse() {
        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("Title");

        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<NoteDTO> result = noteService.updateNote(999L, updateDTO, "testuser");

        assertFalse(result.isPresent());
    }

    @Test
    void UpdateNoteWithEmptyTitleReturnsFalse() {
        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> {
            noteService.updateNote(1L, updateDTO, "testuser");
        });
    }

    // share notes tests

    @Test
    void shareNoteWithSuccess() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.shareNoteWithUsername(1L, "otheruser", "testuser");

        assertTrue(result.isPresent());
        verify(noteRepository).save(testNote);
    }


    @Test
    void shareNoteWithNonExistentUserReturnsEmpty() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<NoteDTO> result = noteService.shareNoteWithUsername(1L, "nonexistent", "testuser");

        assertFalse(result.isPresent());
    }

    @Test
    void shareNonExistentNoteReturnsEmpty() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<NoteDTO> result = noteService.shareNoteWithUsername(999L, "otheruser", "testuser");

        assertFalse(result.isPresent());
    }

    // delete notes tests

    @Test
    void deleteNoteOwnerSucess() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = noteService.deleteNote(1L, "testuser");

        assertTrue(result);
        verify(noteRepository).delete(testNote);
    }

    @Test
    void deleteNoteNonOwnerReturnsFalse() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        boolean result = noteService.deleteNote(1L, "otheruser");

        assertFalse(result);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void deleteNonExistentNoteReturnsFalse() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = noteService.deleteNote(999L, "testuser");

        assertFalse(result);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    // admin tests

    @Test
    void adminCanDeleteAnyNote() {
        UserModel admin = new UserModel();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRoles(java.util.List.of("USER", "ADMIN"));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        boolean result = noteService.deleteNote(1L, "admin");

        assertTrue(result);
        verify(noteRepository).delete(testNote);
    }

    @Test
    void adminCannotEditNote() {
        UserModel admin = new UserModel();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRoles(java.util.List.of("USER", "ADMIN"));

        NoteDTO updateDTO = new NoteDTO();
        updateDTO.setTitle("Updated Title");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        Optional<NoteDTO> result = noteService.updateNote(1L, updateDTO, "admin");

        assertTrue(result.isEmpty());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void adminCanAccessPrivateNote() {
        UserModel admin = new UserModel();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRoles(java.util.List.of("USER", "ADMIN"));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(noteMapper.toDto(testNote)).thenReturn(testNoteDTO);

        Optional<NoteDTO> result = noteService.findByIdWithPermissions(1L, "admin");

        assertTrue(result.isPresent());
        assertEquals(testNoteDTO, result.get());
    }

    // Recent notes filtering tests

    @Test
    @SuppressWarnings("unchecked")
    void findRecentNotesByUserWithNoFilters() {
        Page<Note> page = mock(Page.class);
        when(page.map(any())).thenReturn(mock(Page.class));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByUserIdOrderByLastModifiedDesc(eq(1L), any())).thenReturn(page);

        noteService.findRecentNotesByUser("testuser", PageRequest.of(0, 10), null, null);

        verify(noteRepository).findByUserIdOrderByLastModifiedDesc(eq(1L), any());
        verify(noteRepository, never()).findByUserIdAndCategoryOrderByLastModifiedDesc(any(), any(), any());
        verify(noteRepository, never()).findByUserIdAndTitleContainingOrderByLastModifiedDesc(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findRecentNotesByUserWithCategoryFilter() {
        Page<Note> page = mock(Page.class);
        when(page.map(any())).thenReturn(mock(Page.class));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByUserIdAndCategoryOrderByLastModifiedDesc(eq(1L), eq(NoteCategory.MATHS), any())).thenReturn(page);

        noteService.findRecentNotesByUser("testuser", PageRequest.of(0, 10), "MATHS", null);

        verify(noteRepository).findByUserIdAndCategoryOrderByLastModifiedDesc(eq(1L), eq(NoteCategory.MATHS), any());
        verify(noteRepository, never()).findByUserIdOrderByLastModifiedDesc(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findRecentNotesByUserWithSearchFilter() {
        Page<Note> page = mock(Page.class);
        when(page.map(any())).thenReturn(mock(Page.class));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByUserIdAndTitleContainingOrderByLastModifiedDesc(eq(1L), eq("test"), any())).thenReturn(page);

        noteService.findRecentNotesByUser("testuser", PageRequest.of(0, 10), null, "test");

        verify(noteRepository).findByUserIdAndTitleContainingOrderByLastModifiedDesc(eq(1L), eq("test"), any());
        verify(noteRepository, never()).findByUserIdOrderByLastModifiedDesc(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findRecentNotesByUserWithCategoryAndSearchFilter() {
        Page<Note> page = mock(Page.class);
        when(page.map(any())).thenReturn(mock(Page.class));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByUserIdAndCategoryAndTitleContainingOrderByLastModifiedDesc(eq(1L), eq(NoteCategory.SCIENCE), eq("biology"), any())).thenReturn(page);

        noteService.findRecentNotesByUser("testuser", PageRequest.of(0, 10), "SCIENCE", "biology");

        verify(noteRepository).findByUserIdAndCategoryAndTitleContainingOrderByLastModifiedDesc(eq(1L), eq(NoteCategory.SCIENCE), eq("biology"), any());
        verify(noteRepository, never()).findByUserIdOrderByLastModifiedDesc(any(), any());
    }

    // Shared notes tests

    @Test
    @SuppressWarnings("unchecked")
    void findNotesSharedWithUserSuccess() {
        Page<Note> page = mock(Page.class);
        when(page.map(any())).thenReturn(mock(Page.class));
        when(noteRepository.findNotesSharedWithUser(eq("testuser"), any())).thenReturn(page);

        noteService.findNotesSharedWithUser("testuser", PageRequest.of(0, 10), null);

        verify(noteRepository).findNotesSharedWithUser(eq("testuser"), any());
        verify(noteRepository, never()).findNotesSharedWithUserAndTitleContaining(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findNotesSharedWithUserWithSearchFilter() {
        Page<Note> page = mock(Page.class);
        when(page.map(any())).thenReturn(mock(Page.class));
        when(noteRepository.findNotesSharedWithUserAndTitleContaining(eq("testuser"), eq("biology"), any())).thenReturn(page);

        noteService.findNotesSharedWithUser("testuser", PageRequest.of(0, 10), "biology");

        verify(noteRepository).findNotesSharedWithUserAndTitleContaining(eq("testuser"), eq("biology"), any());
        verify(noteRepository, never()).findNotesSharedWithUser(any(), any());
    }
}
