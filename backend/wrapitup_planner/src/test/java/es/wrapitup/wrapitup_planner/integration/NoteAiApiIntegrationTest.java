package es.wrapitup.wrapitup_planner.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import es.wrapitup.wrapitup_planner.dto.AiNoteResult;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.DocumentTextExtractorService;
import es.wrapitup.wrapitup_planner.service.OpenAiService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NoteAiApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private OpenAiService openAiService;

    @MockBean
    private DocumentTextExtractorService documentTextExtractorService;

    private String authToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "aiuser_" + timestamp;
        String adminUsername = "aiadmin_" + timestamp;

        UserModel testUser = new UserModel();
        testUser.setUsername(username);
        testUser.setEmail(username + "@test.com");
        testUser.setDisplayName(username);
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(testUser);

        UserModel adminUser = new UserModel();
        adminUser.setUsername(adminUsername);
        adminUser.setEmail(adminUsername + "@test.com");
        adminUser.setDisplayName(adminUsername);
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(adminUser);

        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");

        authToken = loginResponse.getCookie("AuthToken");

        Response adminLoginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + adminUsername + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");

        adminToken = adminLoginResponse.getCookie("AuthToken");
    }

    @Test
    void createAiNoteSuccess() {
        AiNoteResult aiNoteResult = new AiNoteResult();
        aiNoteResult.setTitle("AI Title");
        aiNoteResult.setOverview("AI Overview");
        aiNoteResult.setCompleteSummary("AI Summary");

        org.mockito.Mockito.when(documentTextExtractorService.extractText(ArgumentMatchers.any()))
            .thenReturn("ai text");
        org.mockito.Mockito.when(openAiService.generateNoteFromText("ai text"))
            .thenReturn(aiNoteResult);

        given()
            .cookie("AuthToken", authToken)
            .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
            .multiPart("visibility", "PRIVATE")
            .multiPart("category", "SCIENCE")
        .when()
            .post("/api/v1/notes/ai")
        .then()
            .statusCode(CREATED.value())
            .body("title", equalTo("AI Title"))
            .body("overview", equalTo("AI Overview"))
            .body("summary", notNullValue())
            .body("visibility", equalTo("PRIVATE"))
            .body("category", equalTo("SCIENCE"));
    }

    @Test
    void createAiNoteDefaultsVisibilityAndCategory() {
        AiNoteResult aiNoteResult = new AiNoteResult();
        aiNoteResult.setTitle("AI Title");
        aiNoteResult.setOverview("AI Overview");
        aiNoteResult.setCompleteSummary("AI Summary");

        org.mockito.Mockito.when(documentTextExtractorService.extractText(ArgumentMatchers.any()))
            .thenReturn("ai text");
        org.mockito.Mockito.when(openAiService.generateNoteFromText("ai text"))
            .thenReturn(aiNoteResult);

        given()
            .cookie("AuthToken", authToken)
            .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
        .when()
            .post("/api/v1/notes/ai")
        .then()
            .statusCode(CREATED.value())
            .body("visibility", equalTo("PRIVATE"))
            .body("category", equalTo("OTHERS"));
    }

    @Test
    void createAiNoteAdminReturns403() {
        given()
            .cookie("AuthToken", adminToken)
            .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
        .when()
            .post("/api/v1/notes/ai")
        .then()
            .statusCode(FORBIDDEN.value());
    }

    @Test
    void createAiNoteBadRequestWhenExtractorFails() {
        org.mockito.Mockito.when(documentTextExtractorService.extractText(ArgumentMatchers.any()))
            .thenThrow(new IllegalArgumentException("File is empty"));

        given()
            .cookie("AuthToken", authToken)
            .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
        .when()
            .post("/api/v1/notes/ai")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("message", containsString("File"));
    }

    @Test
    void createAiNoteBadGatewayWhenAiFails() {
        org.mockito.Mockito.when(documentTextExtractorService.extractText(ArgumentMatchers.any()))
            .thenReturn("ai text");
        org.mockito.Mockito.when(openAiService.generateNoteFromText("ai text"))
            .thenThrow(new IllegalStateException("OpenAI request failed"));

        given()
            .cookie("AuthToken", authToken)
            .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
        .when()
            .post("/api/v1/notes/ai")
        .then()
            .statusCode(BAD_GATEWAY.value())
            .body("message", containsString("OpenAI"));
    }

    @Test
    void createAiNoteUnauthorizedReturns401() {
        given()
            .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
        .when()
            .post("/api/v1/notes/ai")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }
}
