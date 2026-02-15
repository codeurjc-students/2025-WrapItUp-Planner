package es.wrapitup.wrapitup_planner.unit;

import es.wrapitup.wrapitup_planner.dto.CommentDTO;
import es.wrapitup.wrapitup_planner.dto.CommentMapper;
import es.wrapitup.wrapitup_planner.model.Comment;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.CommentService;

import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
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
    private UserModel adminUser;
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

        adminUser = new UserModel();
        adminUser.setId(3L);
        adminUser.setUsername("admin");
        adminUser.setRoles(java.util.List.of("USER", "ADMIN"));

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setUser(testUser);
        testNote.setVisibility(NoteVisibility.PRIVATE);
        testNote.setCategory(NoteCategory.OTHERS);
        testNote.setLastModified(LocalDateTime.now());
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

    @Test
    void createCommentWithBannedUserThrowsSecurityException() {
        UserModel bannedUser = new UserModel();
        bannedUser.setId(3L);
        bannedUser.setUsername("banneduser");
        bannedUser.setStatus(UserStatus.BANNED);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("banneduser")).thenReturn(Optional.of(bannedUser));

        assertThrows(SecurityException.class, () -> {
            commentService.createComment(testCommentDTO, "banneduser");
        });

        verify(commentRepository, never()).save(any(Comment.class));
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
        
        when(commentRepository.findByNoteIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(commentPage);
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
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertDoesNotThrow(() -> commentService.deleteComment(1L, "admin"));
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void adminCanAccessPrivateNoteComments() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        boolean result = commentService.canUserAccessComments(1L, "admin");

        assertTrue(result);
    }

    // Report/Unreport tests
    @Test
    void reportCommentMarksAsReported() {
        testComment.setReported(false);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(testCommentDTO);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        CommentDTO result = commentService.reportComment(1L, "testuser");

        assertNotNull(result);
        verify(commentRepository).save(argThat(comment -> comment.isReported()));
    }

    @Test
    void reportCommentReturnsNullWhenCommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.reportComment(99L, "testuser");
        });

        verify(commentRepository, never()).save(any());
    }

    @Test
    void unreportCommentMarksAsNotReported() {
        testComment.setReported(true);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(testCommentDTO);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        CommentDTO result = commentService.unreportComment(1L, "admin");

        assertNotNull(result);
        verify(commentRepository).save(argThat(comment -> !comment.isReported()));
    }

    @Test
    void unreportCommentReturnsNullWhenCommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.unreportComment(99L, "admin");
        });

        verify(commentRepository, never()).save(any());
    }

    @Test
    void unreportCommentByNonAdminThrowsException() {
        testComment.setReported(true);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.unreportComment(1L, "testuser");
        });

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getReportedCommentsReturnsPaginatedResults() {
        Comment reportedComment1 = new Comment();
        reportedComment1.setId(1L);
        reportedComment1.setContent("Reported 1");
        reportedComment1.setReported(true);
        reportedComment1.setNote(testNote);
        reportedComment1.setUser(testUser);

        Comment reportedComment2 = new Comment();
        reportedComment2.setId(2L);
        reportedComment2.setContent("Reported 2");
        reportedComment2.setReported(true);
        reportedComment2.setNote(testNote);
        reportedComment2.setUser(testUser);

        List<Comment> reportedComments = Arrays.asList(reportedComment1, reportedComment2);
        Page<Comment> commentPage = new PageImpl<>(reportedComments, PageRequest.of(0, 10), 2);

        CommentDTO dto1 = new CommentDTO();
        dto1.setId(1L);
        dto1.setContent("Reported 1");

        CommentDTO dto2 = new CommentDTO();
        dto2.setId(2L);
        dto2.setContent("Reported 2");

        when(commentRepository.findByIsReportedTrueOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(commentPage);
        when(commentMapper.toDto(reportedComment1)).thenReturn(dto1);
        when(commentMapper.toDto(reportedComment2)).thenReturn(dto2);

        Page<CommentDTO> result = commentService.getReportedComments(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(commentRepository).findByIsReportedTrueOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void getReportedCommentsReturnsEmptyPageWhenNoReportedComments() {
        Page<Comment> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(commentRepository.findByIsReportedTrueOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(emptyPage);

        Page<CommentDTO> result = commentService.getReportedComments(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
