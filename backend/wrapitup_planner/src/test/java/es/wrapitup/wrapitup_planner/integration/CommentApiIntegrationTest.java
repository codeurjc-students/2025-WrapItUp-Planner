package es.wrapitup.wrapitup_planner.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.wrapitup.wrapitup_planner.model.Comment;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import java.time.LocalDateTime;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser;
    private UserModel otherUser;
    private UserModel adminUser;
    private Note testNote;
    private Note publicNote;
    private String authToken;
    private String otherAuthToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String ownerUsername = "commentowner_" + timestamp;
        String otherUsername = "commentother_" + timestamp;
        String adminUsername = "commentadmin_" + timestamp;

        testUser = new UserModel();
        testUser.setUsername(ownerUsername);
        testUser.setEmail(ownerUsername + "@test.com");
        testUser.setDisplayName(ownerUsername);
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        otherUser = new UserModel();
        otherUser.setUsername(otherUsername);
        otherUser.setEmail(otherUsername + "@test.com");
        otherUser.setDisplayName(otherUsername);
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setRoles(Arrays.asList("USER"));
        otherUser.setStatus(UserStatus.ACTIVE);
        otherUser = userRepository.save(otherUser);

        adminUser = new UserModel();
        adminUser.setUsername(adminUsername);
        adminUser.setEmail(adminUsername + "@test.com");
        adminUser.setDisplayName(adminUsername);
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);

        testNote = new Note();
        testNote.setUser(testUser);
        testNote.setTitle("Private Note for Comments");
        testNote.setOverview("Private Overview");
        testNote.setSummary("Private Summary");
        testNote.setVisibility(NoteVisibility.PRIVATE);
        testNote.setCategory(NoteCategory.OTHERS);
        testNote.setLastModified(LocalDateTime.now());
        testNote = noteRepository.save(testNote);

        publicNote = new Note();
        publicNote.setUser(testUser);
        publicNote.setTitle("Public Note for Comments");
        publicNote.setOverview("Public Overview");
        publicNote.setSummary("Public Summary");
        publicNote.setVisibility(NoteVisibility.PUBLIC);
        publicNote.setCategory(NoteCategory.OTHERS);
        publicNote.setLastModified(LocalDateTime.now());
        publicNote = noteRepository.save(publicNote);

        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + ownerUsername + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");
        authToken = loginResponse.getCookie("AuthToken");

        Response otherLoginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + otherUsername + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");
        otherAuthToken = otherLoginResponse.getCookie("AuthToken");

        Response adminLoginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + adminUsername + "\", \"password\":\"admin123\"}")
        .when()
            .post("/api/v1/auth/login");
        adminToken = adminLoginResponse.getCookie("AuthToken");
    }

    // create comment tests

    @Test
    void createCommentOnPrivateNoteAuthSuccess() {
        String commentJson = """
            {
                "content": "This is a test comment"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(commentJson)
        .when()
            .post("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(CREATED.value())
            .body("content", equalTo("This is a test comment"))
            .body("username", equalTo(testUser.getUsername()))
            .body("noteId", equalTo(testNote.getId().intValue()));
    }

    @Test
    void createCommentOnPublicNoteAuthSuccess() {
        String commentJson = """
            {
                "content": "Comment on public note"
            }
            """;

        given()
            .cookie("AuthToken", otherAuthToken)
            .contentType(ContentType.JSON)
            .body(commentJson)
        .when()
            .post("/api/v1/notes/" + publicNote.getId() + "/comments")
        .then()
            .statusCode(CREATED.value())
            .body("content", equalTo("Comment on public note"));
    }

    @Test
    void createCommentNotAuthReturns401() {
        String commentJson = """
            {
                "content": "Comment without auth"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(commentJson)
        .when()
            .post("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void createCommentOnPrivateNoteByNonOwnerReturns403() {
        String commentJson = """
            {
                "content": "Trying to comment on private note"
            }
            """;

        given()
            .cookie("AuthToken", otherAuthToken)
            .contentType(ContentType.JSON)
            .body(commentJson)
        .when()
            .post("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("permission"));
    }

    @Test
    void createCommentWithEmptyContentReturns400() {
        String commentJson = """
            {
                "content": ""
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(commentJson)
        .when()
            .post("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(BAD_REQUEST.value());
    }

    // get comments tests

    @Test
    void getCommentsOnPrivateNoteByOwnerSuccess() {
        Comment comment1 = new Comment("First comment", testNote, testUser);
        Comment comment2 = new Comment("Second comment", testNote, testUser);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(OK.value())
            .body("content", hasSize(2))
            .body("content.content", hasItems("First comment", "Second comment"));
    }

    @Test
    void getCommentsOnPublicNoteWithoutAuthSuccess() {
        Comment comment = new Comment("Public comment", publicNote, testUser);
        commentRepository.save(comment);

        given()
        .when()
            .get("/api/v1/notes/" + publicNote.getId() + "/comments")
        .then()
            .statusCode(OK.value())
            .body("content", hasSize(1))
            .body("content[0].content", equalTo("Public comment"));
    }

    @Test
    void getCommentsOnPrivateNoteWithoutAuthReturns401() {
        given()
        .when()
            .get("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void getCommentsOnPrivateNoteByNonOwnerReturns403() {
        given()
            .cookie("AuthToken", otherAuthToken)
        .when()
            .get("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(FORBIDDEN.value());
    }

    @Test
    void getCommentsPaginatedReturnsCorrectPage() {
        for (int i = 1; i <= 15; i++) {
            Comment comment = new Comment("Comment " + i, testNote, testUser);
            commentRepository.save(comment);
        }

        given()
            .cookie("AuthToken", authToken)
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(OK.value())
            .body("content", hasSize(10))
            .body("totalElements", equalTo(15))
            .body("totalPages", equalTo(2));
    }

    // delete comment tests

    @Test
    void deleteCommentByOwnerSuccess() {
        Comment comment = new Comment("Comment to delete", testNote, testUser);
        comment = commentRepository.save(comment);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/notes/" + testNote.getId() + "/comments/" + comment.getId())
        .then()
            .statusCode(NO_CONTENT.value());
    }

    @Test
    void deleteCommentByNonOwnerReturns403() {
        Comment comment = new Comment("Comment owned by testUser", publicNote, testUser);
        comment = commentRepository.save(comment);

        given()
            .cookie("AuthToken", otherAuthToken)
        .when()
            .delete("/api/v1/notes/" + publicNote.getId() + "/comments/" + comment.getId())
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("owner"));
    }

    @Test
    void deleteCommentWithoutAuthReturns401() {
        Comment comment = new Comment("Some comment", testNote, testUser);
        comment = commentRepository.save(comment);

        given()
        .when()
            .delete("/api/v1/notes/" + testNote.getId() + "/comments/" + comment.getId())
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void deleteNonExistentCommentReturns403() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/notes/" + testNote.getId() + "/comments/999")
        .then()
            .statusCode(FORBIDDEN.value());
    }

    // admin tests

    @Test
    void adminCanDeleteAnyComment() {

        Comment comment = new Comment("User's comment", testNote, testUser);
        comment = commentRepository.save(comment);

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .delete("/api/v1/notes/" + testNote.getId() + "/comments/" + comment.getId())
        .then()
            .statusCode(NO_CONTENT.value());
    }

    @Test
    void adminCanAccessPrivateNoteComments() {

        Comment comment = new Comment("Private comment", testNote, testUser);
        comment = commentRepository.save(comment);
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .get("/api/v1/notes/" + testNote.getId() + "/comments")
        .then()
            .statusCode(OK.value())
            .body("content[0].content", equalTo("Private comment"));
    }

    // Report and banned user tests

    @Test
    void userCanReportComment() {
        Comment comment = new Comment("Comment to report", publicNote, testUser);
        comment = commentRepository.save(comment);

        given()
            .cookie("AuthToken", otherAuthToken)
        .when()
            .post("/api/v1/notes/" + publicNote.getId() + "/comments/" + comment.getId() + "/report")
        .then()
            .statusCode(OK.value())
            .body("reported", equalTo(true));
    }

    @Test
    void reportNonExistentCommentReturns400() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .post("/api/v1/notes/" + testNote.getId() + "/comments/999/report")
        .then()
            .statusCode(BAD_REQUEST.value());
    }

    @Test
    void reportCommentNotAuthReturns401() {
        Comment comment = new Comment("Comment to report", publicNote, testUser);
        comment = commentRepository.save(comment);

        given()
        .when()
            .post("/api/v1/notes/" + publicNote.getId() + "/comments/" + comment.getId() + "/report")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void reportCommentTwiceStillWorks() {
        Comment comment = new Comment("Comment to report twice", publicNote, testUser);
        comment = commentRepository.save(comment);

        
        given()
            .cookie("AuthToken", otherAuthToken)
        .when()
            .post("/api/v1/notes/" + publicNote.getId() + "/comments/" + comment.getId() + "/report")
        .then()
            .statusCode(OK.value())
            .body("reported", equalTo(true));

        
        given()
            .cookie("AuthToken", authToken)
        .when()
            .post("/api/v1/notes/" + publicNote.getId() + "/comments/" + comment.getId() + "/report")
        .then()
            .statusCode(OK.value())
            .body("reported", equalTo(true));
    }

    @Test
    void bannedUserCannotCreateComment() {
        // Ban the other user
        otherUser.setStatus(UserStatus.BANNED);
        userRepository.save(otherUser);

        String commentJson = """
            {
                "content": "This should fail"
            }
            """;

        given()
            .cookie("AuthToken", otherAuthToken)
            .contentType(ContentType.JSON)
            .body(commentJson)
        .when()
            .post("/api/v1/notes/" + publicNote.getId() + "/comments")
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("Banned users cannot create comments"));
    }

    // Admin reported comments endpoints

    @Test
    void adminCanGetReportedComments() {
        Comment reportedComment1 = new Comment("Reported content 1", publicNote, testUser);
        reportedComment1.setReported(true);
        commentRepository.save(reportedComment1);

        Comment reportedComment2 = new Comment("Reported content 2", publicNote, otherUser);
        reportedComment2.setReported(true);
        commentRepository.save(reportedComment2);

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .get("/api/v1/admin/reported-comments?page=0&size=10")
        .then()
            .statusCode(OK.value())
            .body("content", hasSize(greaterThanOrEqualTo(2)))
            .body("content[0].reported", equalTo(true));
    }

    @Test
    void nonAdminCannotGetReportedComments() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/admin/reported-comments")
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("Only admins can view reported comments"));
    }

    @Test
    void getReportedCommentsWithoutAuthReturns401() {
        given()
        .when()
            .get("/api/v1/admin/reported-comments")
        .then()
            .statusCode(UNAUTHORIZED.value())
            .body("message", containsString("You must log in to view reported comments"));
    }

    @Test
    void adminCanUnreportComment() {
        Comment reportedComment = new Comment("Reported content", publicNote, testUser);
        reportedComment.setReported(true);
        reportedComment = commentRepository.save(reportedComment);

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/admin/reported-comments/" + reportedComment.getId() + "/unreport")
        .then()
            .statusCode(OK.value())
            .body("reported", equalTo(false));
    }

    @Test
    void nonAdminCannotUnreportComment() {
        Comment reportedComment = new Comment("Reported content", publicNote, testUser);
        reportedComment.setReported(true);
        reportedComment = commentRepository.save(reportedComment);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .post("/api/v1/admin/reported-comments/" + reportedComment.getId() + "/unreport")
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("Only admins can unreport comments"));
    }

    @Test
    void unreportCommentWithoutAuthReturns401() {
        Comment reportedComment = new Comment("Reported content", publicNote, testUser);
        reportedComment.setReported(true);
        reportedComment = commentRepository.save(reportedComment);

        given()
        .when()
            .post("/api/v1/admin/reported-comments/" + reportedComment.getId() + "/unreport")
        .then()
            .statusCode(UNAUTHORIZED.value())
            .body("message", containsString("You must log in to unreport comments"));
    }

    @Test
    void adminCanDeleteReportedComment() {
        Comment reportedComment = new Comment("Reported content to delete", publicNote, testUser);
        reportedComment.setReported(true);
        reportedComment = commentRepository.save(reportedComment);

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .delete("/api/v1/admin/reported-comments/" + reportedComment.getId())
        .then()
            .statusCode(NO_CONTENT.value());
    }

    @Test
    void nonAdminCannotDeleteReportedComment() {
        Comment reportedComment = new Comment("Reported content", publicNote, testUser);
        reportedComment.setReported(true);
        reportedComment = commentRepository.save(reportedComment);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/admin/reported-comments/" + reportedComment.getId())
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("Only admins can delete comments from this view"));
    }

    @Test
    void deleteReportedCommentWithoutAuthReturns401() {
        Comment reportedComment = new Comment("Reported content", publicNote, testUser);
        reportedComment.setReported(true);
        reportedComment = commentRepository.save(reportedComment);

        given()
        .when()
            .delete("/api/v1/admin/reported-comments/" + reportedComment.getId())
        .then()
            .statusCode(UNAUTHORIZED.value())
            .body("message", containsString("You must log in to delete comments"));
    }

    @Test
    void unreportNonExistentCommentReturns400() {
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/admin/reported-comments/999999/unreport")
        .then()
            .statusCode(BAD_REQUEST.value());
    }

    @Test
    void deleteNonExistentReportedCommentReturns400() {
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .delete("/api/v1/admin/reported-comments/999999")
        .then()
            .statusCode(BAD_REQUEST.value());
    }
}
