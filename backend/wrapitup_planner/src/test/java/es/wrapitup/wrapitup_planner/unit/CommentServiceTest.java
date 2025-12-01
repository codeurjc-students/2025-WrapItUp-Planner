package es.wrapitup.wrapitup_planner.unit;

import es.wrapitup.wrapitup_planner.dto.CommentDTO;
import es.wrapitup.wrapitup_planner.dto.CommentMapper;
import es.wrapitup.wrapitup_planner.model.Comment;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.CommentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private UserModel testUser;
    private UserModel otherUser;
    private Note testNote;
    private Comment testComment;
    private CommentDTO testCommentDTO;

    @BeforeEach
    void setUp() {
        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRoles(java.util.List.of("USER"));

        otherUser = new UserModel();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setRoles(java.util.List.of("USER"));

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setUser(testUser);
        testNote.setVisibility(NoteVisibility.PRIVATE);
        testNote.setSharedWith(new HashSet<>());

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test comment");
        testComment.setNote(testNote);
        testComment.setUser(testUser);

        testCommentDTO = new CommentDTO();
        testCommentDTO.setId(1L);
        testCommentDTO.setContent("Test comment");
        testCommentDTO.setNoteId(1L);
        testCommentDTO.setUsername("testuser");
    }

    // comment creation tests

    @Test
    void createCommentSuccess() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentMapper.toDto(testComment)).thenReturn(testCommentDTO);

        CommentDTO result = commentService.createComment(testCommentDTO, "testuser");

        assertNotNull(result);
        assertEquals("Test comment", result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createCommentWithEmptyContentThrowsException() {
        testCommentDTO.setContent("");

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(testCommentDTO, "testuser");
        });

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createCommentWithNullContentThrowsException() {
        testCommentDTO.setContent(null);

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(testCommentDTO, "testuser");
        });
    }

    @Test
    void createCommentWithInvalidNoteThrowsException() {
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(testCommentDTO, "testuser");
        });
    }

    @Test
    void createCommentWithInvalidUserThrowsException() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(testCommentDTO, "invaliduser");
        });
    }

    // get comments tests

    @Test
    void getCommentsByNoteIdReturnsComments() {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findByNoteIdOrderByCreatedAtDesc(1L)).thenReturn(comments);
        when(commentMapper.toDto(testComment)).thenReturn(testCommentDTO);

        List<CommentDTO> result = commentService.getCommentsByNoteId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).getContent());
    }

    @Test
    void getCommentsByNoteIdPaginatedReturnsPage() {
        Page<Comment> commentPage = new PageImpl<>(Arrays.asList(testComment));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(commentRepository.findByNoteId(1L, pageable)).thenReturn(commentPage);
        when(commentMapper.toDto(testComment)).thenReturn(testCommentDTO);

        Page<CommentDTO> result = commentService.getCommentsByNoteIdPaginated(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test comment", result.getContent().get(0).getContent());
    }

    // delete comment tests

    @Test
    void deleteCommentByOwnerSuccess() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> {
            commentService.deleteComment(1L, "testuser");
        });

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteCommentByNonOwnerThrowsException() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(1L, "otheruser");
        });

        verify(commentRepository, never()).deleteById(1L);
    }

    @Test
    void deleteNonExistentCommentThrowsException() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(999L, "testuser");
        });
    }

    // access permission tests

    @Test
    void canUserAccessPublicNoteComments() {
        testNote.setVisibility(NoteVisibility.PUBLIC);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        boolean result = commentService.canUserAccessComments(1L, null);

        assertTrue(result);
    }

    @Test
    void canOwnerAccessPrivateNoteComments() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = commentService.canUserAccessComments(1L, "testuser");

        assertTrue(result);
    }

    @Test
    void cannotAccessPrivateNoteCommentsWithoutAuth() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        boolean result = commentService.canUserAccessComments(1L, null);

        assertFalse(result);
    }

    @Test
    void canSharedUserAccessPrivateNoteComments() {
        testNote.getSharedWith().add(otherUser);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        boolean result = commentService.canUserAccessComments(1L, "otheruser");

        assertTrue(result);
    }

    @Test
    void cannotAccessNonExistentNoteComments() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = commentService.canUserAccessComments(999L, "testuser");

        assertFalse(result);
    }

    // admin tests
    @Test
    void adminCanDeleteAnyComment() {
        UserModel admin = new UserModel();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setRoles(java.util.List.of("USER", "ADMIN"));

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertDoesNotThrow(() -> commentService.deleteComment(1L, "admin"));
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void adminCanAccessPrivateNoteComments() {
        UserModel admin = new UserModel();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setRoles(java.util.List.of("USER", "ADMIN"));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        boolean result = commentService.canUserAccessComments(1L, "admin");

        assertTrue(result);
    }
}
