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
        testNote = noteRepository.save(testNote);

        publicNote = new Note();
        publicNote.setUser(testUser);
        publicNote.setTitle("Public Note for Comments");
        publicNote.setOverview("Public Overview");
        publicNote.setSummary("Public Summary");
        publicNote.setVisibility(NoteVisibility.PUBLIC);
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
            .statusCode(UNAUTHORIZED.value())
            .body("message", containsString("log in"));
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
}
