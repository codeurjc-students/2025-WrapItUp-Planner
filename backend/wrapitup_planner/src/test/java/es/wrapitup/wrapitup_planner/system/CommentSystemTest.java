package es.wrapitup.wrapitup_planner.system;

import es.wrapitup.wrapitup_planner.dto.CommentDTO;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@Transactional
@SpringBootTest
public class CommentSystemTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel testUser;
    private UserModel otherUser;
    private UserModel adminUser;
    private Note testNote;

    @BeforeEach
    void setUp() {

        testUser = new UserModel();
        testUser.setUsername("commentsystemuser");
        testUser.setEmail("commentsystem@test.com");
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        otherUser = new UserModel();
        otherUser.setUsername("othercommentsystemuser");
        otherUser.setEmail("othercommentsystem@test.com");
        otherUser.setRoles(Arrays.asList("USER"));
        otherUser.setStatus(UserStatus.ACTIVE);
        otherUser = userRepository.save(otherUser);

        adminUser = new UserModel();
        adminUser.setUsername("admincommentsystemuser");
        adminUser.setEmail("admincommentsystem@test.com");
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);

        testNote = new Note();
        testNote.setUser(testUser);
        testNote.setTitle("Test Note for Comments");
        testNote.setOverview("Overview");
        testNote.setSummary("Summary");
        testNote.setVisibility(NoteVisibility.PRIVATE);
        testNote = noteRepository.save(testNote);
    }

    // comment creation tests

    @Test
    void createCommentPersistsInDatabase() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("This is a system test comment");
        commentDTO.setNoteId(testNote.getId());

        CommentDTO created = commentService.createComment(commentDTO, "commentsystemuser");

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("This is a system test comment", created.getContent());
        assertEquals(testNote.getId(), created.getNoteId());
        assertEquals("commentsystemuser", created.getUsername());

        Optional<Comment> savedComment = commentRepository.findById(created.getId());
        assertTrue(savedComment.isPresent());
        assertEquals("This is a system test comment", savedComment.get().getContent());
    }

    @Test
    void createCommentWithInvalidNoteThrowsException() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Comment on non-existent note");
        commentDTO.setNoteId(999L);

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(commentDTO, "commentsystemuser");
        });
    }

    @Test
    void createCommentWithEmptyContentThrowsException() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("   ");
        commentDTO.setNoteId(testNote.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(commentDTO, "commentsystemuser");
        });
    }

    // get comments tests

    @Test
    void getCommentsByNoteIdReturnsAllComments() {
        CommentDTO comment1 = new CommentDTO();
        comment1.setContent("First comment");
        comment1.setNoteId(testNote.getId());
        commentService.createComment(comment1, "commentsystemuser");

        CommentDTO comment2 = new CommentDTO();
        comment2.setContent("Second comment");
        comment2.setNoteId(testNote.getId());
        commentService.createComment(comment2, "commentsystemuser");

        List<CommentDTO> comments = commentService.getCommentsByNoteId(testNote.getId());

        assertNotNull(comments);
        assertEquals(2, comments.size());
    }

    @Test
    void getCommentsByNoteIdPaginatedReturnsCorrectPage() {
        for (int i = 1; i <= 15; i++) {
            CommentDTO comment = new CommentDTO();
            comment.setContent("Comment " + i);
            comment.setNoteId(testNote.getId());
            commentService.createComment(comment, "commentsystemuser");
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentDTO> page = commentService.getCommentsByNoteIdPaginated(testNote.getId(), pageable);

        assertNotNull(page);
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    // delete comment tests

    @Test
    void deleteCommentByOwnerSuccess() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Comment to delete");
        commentDTO.setNoteId(testNote.getId());
        CommentDTO created = commentService.createComment(commentDTO, "commentsystemuser");

        assertDoesNotThrow(() -> {
            commentService.deleteComment(created.getId(), "commentsystemuser");
        });

        Optional<Comment> deleted = commentRepository.findById(created.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    void deleteCommentByNonOwnerThrowsException() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Comment owned by testuser");
        commentDTO.setNoteId(testNote.getId());
        CommentDTO created = commentService.createComment(commentDTO, "commentsystemuser");

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(created.getId(), "othercommentsystemuser");
        });

        Optional<Comment> stillExists = commentRepository.findById(created.getId());
        assertTrue(stillExists.isPresent());
    }

    @Test
    void deleteNonExistentCommentThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(999L, "commentsystemuser");
        });
    }

    // access permission tests

    @Test
    void canUserAccessPublicNoteComments() {
        testNote.setVisibility(NoteVisibility.PUBLIC);
        noteRepository.save(testNote);

        boolean result = commentService.canUserAccessComments(testNote.getId(), null);

        assertTrue(result);
    }

    @Test
    void canOwnerAccessPrivateNoteComments() {
        boolean result = commentService.canUserAccessComments(testNote.getId(), "commentsystemuser");

        assertTrue(result);
    }

    @Test
    void cannotAccessPrivateNoteCommentsWithoutAuth() {
        boolean result = commentService.canUserAccessComments(testNote.getId(), null);

        assertFalse(result);
    }

    @Test
    void cannotAccessPrivateNoteCommentsAsNonOwner() {
        boolean result = commentService.canUserAccessComments(testNote.getId(), "othercommentsystemuser");

        assertFalse(result);
    }

    @Test
    void canSharedUserAccessPrivateNoteComments() {
        testNote.getSharedWith().add(otherUser);
        noteRepository.save(testNote);

        boolean result = commentService.canUserAccessComments(testNote.getId(), "othercommentsystemuser");

        assertTrue(result);
    }

    @Test
    void cannotAccessNonExistentNoteComments() {
        boolean result = commentService.canUserAccessComments(999L, "commentsystemuser");

        assertFalse(result);
    }

    // admin tests

    @Test
    void adminCanDeleteAnyComment() {
        Comment comment = new Comment("User's comment", testNote, testUser);
        comment = commentRepository.save(comment);

        Long commentId = comment.getId();

        commentService.deleteComment(commentId, "admincommentsystemuser");

        Optional<Comment> deletedComment = commentRepository.findById(commentId);
        assertFalse(deletedComment.isPresent());
    }

    @Test
    void adminCanAccessPrivateNoteComments() {
        Comment comment = new Comment("Private comment", testNote, testUser);
        comment = commentRepository.save(comment);

        boolean result = commentService.canUserAccessComments(testNote.getId(), "admincommentsystemuser");

        assertTrue(result);
    }
}
