package es.wrapitup.wrapitup_planner.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NoteApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser;
    private UserModel otherUser;
    private UserModel adminUser;
    private String authToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String ownerUsername = "noteowner_" + timestamp;
        String otherUsername = "otheruser_" + timestamp;

        // create test users
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

        // create admin user
        adminUser = new UserModel();
        adminUser.setUsername("admin_" + timestamp);
        adminUser.setEmail("admin_" + timestamp + "@test.com");
        adminUser.setDisplayName("Admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);

        
        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + ownerUsername + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");

        authToken = loginResponse.getCookie("AuthToken");

        
        Response adminLoginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin_" + timestamp + "\", \"password\":\"admin123\"}")
        .when()
            .post("/api/v1/auth/login");

        adminToken = adminLoginResponse.getCookie("AuthToken");
    }

    // Note creation tests

    @Test
    void createNoteAuthSuccess() {
        String noteJson = """
            {
                "title": "Integration Test Note",
                "overview": "Test Overview",
                "summary": "Test Summary",
                "visibility": "PRIVATE",
                "category": "SCIENCE"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(noteJson)
        .when()
            .post("/api/v1/notes")
        .then()
            .statusCode(CREATED.value())
            .body("title", equalTo("Integration Test Note"))
            .body("overview", equalTo("Test Overview"))
            .body("visibility", equalTo("PRIVATE"))
            .body("category", equalTo("SCIENCE"))
            .body("lastModified", notNullValue());
    }

    @Test
    void createNoteNotAuthReturns401() {
        String noteJson = """
            {
                "title": "Test Note",
                "overview": "Overview"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(noteJson)
        .when()
            .post("/api/v1/notes")
        .then()
            .statusCode(UNAUTHORIZED.value())
            .body("message", containsString("log in"));
    }

    @Test
    void createNoteWithNoTitleReturns400() {
        String noteJson = """
            {
                "overview": "Overview without title"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(noteJson)
        .when()
            .post("/api/v1/notes")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("message", containsString("Title is required"));
    }

    // get note tests

    @Test
    void getPublicNoteSuccess() {
        Note publicNote = new Note();
        publicNote.setUser(testUser);
        publicNote.setTitle("Public Note");
        publicNote.setOverview("Public Overview");
        publicNote.setSummary("Public Summary");
        publicNote.setJsonQuestions("{}");
        publicNote.setVisibility(NoteVisibility.PUBLIC);
        publicNote.setCategory(NoteCategory.OTHERS);
        publicNote.setLastModified(LocalDateTime.now());
        publicNote = noteRepository.save(publicNote);

        given()
        .when()
            .get("/api/v1/notes/" + publicNote.getId())
        .then()
            .statusCode(OK.value())
            .body("title", equalTo("Public Note"))
            .body("visibility", equalTo("PUBLIC"));
    }

    @Test
    void getPrivateNoteNoAuthReturns401() {
        Note privateNote = new Note();
        privateNote.setUser(testUser);
        privateNote.setTitle("Private Note");
        privateNote.setOverview("Private Overview");
        privateNote.setSummary("Private Summary");
        privateNote.setJsonQuestions("{}");
        privateNote.setVisibility(NoteVisibility.PRIVATE);
        privateNote.setCategory(NoteCategory.OTHERS);
        privateNote.setLastModified(LocalDateTime.now());
        privateNote = noteRepository.save(privateNote);

        given()
        .when()
            .get("/api/v1/notes/" + privateNote.getId())
        .then()
            .statusCode(UNAUTHORIZED.value())
            .body("message", containsString("log in"));
    }

    @Test
    void getOwnPrivateNoteSuccess() {
        Note privateNote = new Note();
        privateNote.setUser(testUser);
        privateNote.setTitle("My Private Note");
        privateNote.setOverview("My Overview");
        privateNote.setSummary("My Summary");
        privateNote.setJsonQuestions("{}");
        privateNote.setVisibility(NoteVisibility.PRIVATE);
        privateNote.setCategory(NoteCategory.OTHERS);
        privateNote.setLastModified(LocalDateTime.now());
        privateNote = noteRepository.save(privateNote);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes/" + privateNote.getId())
        .then()
            .statusCode(OK.value())
            .body("title", equalTo("My Private Note"));
    }

    @Test
    void getNonExistentNoteReturns404() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes/999999")
        .then()
            .statusCode(NOT_FOUND.value())
            .body("message", containsString("Note not found"));
    }

    //update notes tests

    @Test
    void OwnerUpdatesNoteSuccess() {
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

        String updateJson = """
            {
                "title": "Updated Title",
                "overview": "Updated Overview"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(OK.value())
            .body("title", equalTo("Updated Title"))
            .body("overview", equalTo("Updated Overview"));
    }

    @Test
    void NonOwnerUpdatesNote403() {
        Note note = new Note();
        note.setUser(otherUser);
        note.setTitle("Other's Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String updateJson = """
            {
                "title": "Trying to update"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("permission"));
    }

    @Test
    void updateNoteNotAuthReturns403() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Title");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String updateJson = """
            {
                "title": "Updated"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void updateNoteEmptyTitleReturns400() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Title");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String updateJson = """
            {
                "title": ""
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("message", containsString("empty"));
    }

    // sharing notes tests

    @Test
    void shareNoteWithValidUser() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note to Share");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String shareJson = """
            {
                "username": "%s"
            }
            """.formatted(otherUser.getUsername());

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(shareJson)
        .when()
            .put("/api/v1/notes/" + note.getId() + "/share")
        .then()
            .statusCode(OK.value());
    }

    @Test
    void shareNoteWithThemselvesNotAllowed() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String shareJson = """
            {
                "username": "%s"
            }
            """.formatted(testUser.getUsername());
        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(shareJson)
        .when()
            .put("/api/v1/notes/" + note.getId() + "/share")
        .then()
            .statusCode(NOT_FOUND.value())
            .body("message", containsString("not found"));
    }

    @Test
    void shareNoteWithNonExistentUserReturns404() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String shareJson = """
            {
                "username": "User"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(shareJson)
        .when()
            .put("/api/v1/notes/" + note.getId() + "/share")
        .then()
            .statusCode(NOT_FOUND.value())
            .body("message", containsString("not found"));
    }


    // note deletion tests

    @Test
    void deleteNoteByOwnerSuccess() {
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

        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(OK.value());

        
        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(NOT_FOUND.value());
    }

    @Test
    void NonOwnerDeletesNote403() {
        Note note = new Note();
        note.setUser(otherUser);
        note.setTitle("Other's Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", containsString("permission"));
    }

    @Test
    void deleteNoteNoAuth401() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        given()
        .when()
            .delete("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void deleteNonExistentNoteReturns403() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/notes/999999")
        .then()
            .statusCode(FORBIDDEN.value());
    }

    // Admin tests
    @Test
    void adminCannotCreateNote() {
        String noteJson = """
            {
                "title": "Admin Note",
                "overview": "Admin Overview",
                "summary": "Admin Summary",
                "visibility": "PUBLIC"
            }
            """;

        given()
            .cookie("AuthToken", adminToken)
            .contentType(ContentType.JSON)
            .body(noteJson)
        .when()
            .post("/api/v1/notes")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("message", equalTo("Admins cannot create notes"));
    }

    @Test
    void adminCanDeleteAnyNote() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("User Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .delete("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(OK.value());
    }

    @Test
    void adminCannotEditNote() {
        Note note = new Note();
        note.setUser(testUser);
        note.setTitle("User Note");
        note.setOverview("Overview");
        note.setSummary("Summary");
        note.setJsonQuestions("{}");
        note.setVisibility(NoteVisibility.PRIVATE);
        note.setCategory(NoteCategory.OTHERS);
        note.setLastModified(LocalDateTime.now());
        note = noteRepository.save(note);

        String updateJson = """
            {
                "title": "Updated by Admin",
                "overview": "Updated Overview",
                "summary": "Updated Summary"
            }
            """;

        given()
            .cookie("AuthToken", adminToken)
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(FORBIDDEN.value());
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

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .get("/api/v1/notes/" + note.getId())
        .then()
            .statusCode(OK.value())
            .body("title", equalTo("Private Note"));
    }

}
