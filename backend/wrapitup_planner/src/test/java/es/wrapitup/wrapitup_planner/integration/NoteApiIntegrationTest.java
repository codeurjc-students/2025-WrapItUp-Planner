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
import java.util.HashSet;
import java.util.Set;

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
            .statusCode(UNAUTHORIZED.value());
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
            .statusCode(FORBIDDEN.value())
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
            .statusCode(FORBIDDEN.value())
            .body("message", equalTo("Admins cannot edit notes"));
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

    // Note filtering tests

    @Test
    void getRecentNotesWithCategoryFilterSuccess() {
        Note mathsNote = new Note();
        mathsNote.setUser(testUser);
        mathsNote.setTitle("Algebra Note");
        mathsNote.setOverview("Math overview");
        mathsNote.setSummary("Math summary");
        mathsNote.setJsonQuestions("{}");
        mathsNote.setVisibility(NoteVisibility.PRIVATE);
        mathsNote.setCategory(NoteCategory.MATHS);
        mathsNote.setLastModified(LocalDateTime.now());
        noteRepository.save(mathsNote);

        Note scienceNote = new Note();
        scienceNote.setUser(testUser);
        scienceNote.setTitle("Biology Note");
        scienceNote.setOverview("Science overview");
        scienceNote.setSummary("Science summary");
        scienceNote.setJsonQuestions("{}");
        scienceNote.setVisibility(NoteVisibility.PRIVATE);
        scienceNote.setCategory(NoteCategory.SCIENCE);
        scienceNote.setLastModified(LocalDateTime.now().minusDays(1));
        noteRepository.save(scienceNote);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes?category=MATHS&page=0&size=10")
        .then()
            .statusCode(OK.value())
            .body("content.size()", equalTo(1))
            .body("content[0].title", equalTo("Algebra Note"))
            .body("content[0].category", equalTo("MATHS"));
    }

    @Test
    void getRecentNotesWithSearchFilterSuccess() {
        // Create notes with different titles
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
        note2.setTitle("Biology Basics");
        note2.setOverview("Overview");
        note2.setSummary("Summary");
        note2.setJsonQuestions("{}");
        note2.setVisibility(NoteVisibility.PRIVATE);
        note2.setCategory(NoteCategory.SCIENCE);
        note2.setLastModified(LocalDateTime.now().minusDays(1));
        noteRepository.save(note2);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes?search=Pythagorean&page=0&size=10")
        .then()
            .statusCode(OK.value())
            .body("content.size()", equalTo(1))
            .body("content[0].title", equalTo("Pythagorean Theorem"));
    }

    @Test
    void getRecentNotesWithCategoryAndSearchFilterSuccess() {
        // Create multiple notes
        Note mathsNote1 = new Note();
        mathsNote1.setUser(testUser);
        mathsNote1.setTitle("Algebra Equations");
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

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes?category=MATHS&search=Algebra&page=0&size=10")
        .then()
            .statusCode(OK.value())
            .body("content.size()", equalTo(1))
            .body("content[0].title", equalTo("Algebra Equations"))
            .body("content[0].category", equalTo("MATHS"));
    }

    @Test
    void getRecentNotesNotAuthenticatedReturns401() {
        given()
        .when()
            .get("/api/v1/notes?page=0&size=10")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    //Shared notes tests

    @Test
    void getSharedNotesSuccess() {
        Note sharedNote = new Note();
        sharedNote.setUser(otherUser);
        sharedNote.setTitle("Shared Note");
        sharedNote.setOverview("Shared Overview");
        sharedNote.setSummary("Shared Summary");
        sharedNote.setJsonQuestions("{}");
        sharedNote.setVisibility(NoteVisibility.PRIVATE);
        sharedNote.setCategory(NoteCategory.HISTORY);
        sharedNote.setLastModified(LocalDateTime.now());
        
        Set<UserModel> sharedWith = new HashSet<>();
        sharedWith.add(testUser);
        sharedNote.setSharedWith(sharedWith);
        noteRepository.save(sharedNote);

        Note ownNote = new Note();
        ownNote.setUser(testUser);
        ownNote.setTitle("Own Note");
        ownNote.setOverview("Overview");
        ownNote.setSummary("Summary");
        ownNote.setJsonQuestions("{}");
        ownNote.setVisibility(NoteVisibility.PRIVATE);
        ownNote.setCategory(NoteCategory.MATHS);
        ownNote.setLastModified(LocalDateTime.now());
        noteRepository.save(ownNote);

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes/shared?page=0&size=10")
        .then()
            .statusCode(OK.value())
            .body("content.size()", equalTo(1))
            .body("content[0].title", equalTo("Shared Note"))
            .body("content[0].category", equalTo("HISTORY"));
    }

    @Test
    void getSharedNotesWithSearchFilterSuccess() {
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

        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/notes/shared?search=Pythagorean&page=0&size=10")
        .then()
            .statusCode(OK.value())
            .body("content.size()", equalTo(1))
            .body("content[0].title", equalTo("Pythagorean Theorem"));
    }

    @Test
    void getSharedNotesNotAuthenticatedReturns401() {
        given()
        .when()
            .get("/api/v1/notes/shared?page=0&size=10")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void adminCannotViewOwnNotes() {
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .get("/api/v1/notes?page=0&size=10")
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", equalTo("Admins do not have their own notes"));
    }

    @Test
    void adminCannotViewSharedNotes() {
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .get("/api/v1/notes/shared?page=0&size=10")
        .then()
            .statusCode(FORBIDDEN.value())
            .body("message", equalTo("Admins do not have shared notes"));
    }

}
