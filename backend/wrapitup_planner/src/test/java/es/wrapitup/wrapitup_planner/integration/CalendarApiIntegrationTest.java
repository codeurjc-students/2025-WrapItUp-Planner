package es.wrapitup.wrapitup_planner.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.wrapitup.wrapitup_planner.model.EventColor;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CalendarEventRepository;
import es.wrapitup.wrapitup_planner.repository.CalendarTaskRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalendarApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private CalendarEventRepository eventRepository;

    @Autowired
    private CalendarTaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "caluser_" + timestamp;

        testUser = new UserModel();
        testUser.setUsername(username);
        testUser.setEmail(username + "@test.com");
        testUser.setDisplayName(username);
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");

        authToken = loginResponse.getCookie("AuthToken");
    }

    // Calendar view tests

    @Test
    void getMonthViewReturnsCalendarData() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/calendar/month/2026/2")
        .then()
            .statusCode(OK.value())
            .body("year", equalTo(2026))
            .body("month", equalTo(2))
            .body("days", notNullValue())
            .body("days.size()", equalTo(28));
    }

    @Test
    void getDayViewReturnsEventsAndTasks() {
        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/calendar/day/2026/2/25")
        .then()
            .statusCode(OK.value())
            .body("date", notNullValue())
            .body("events", notNullValue())
            .body("tasks", notNullValue());
    }

    @Test
    void getMonthViewWithoutAuthReturnsUnauthorized() {
        given()
        .when()
            .get("/api/v1/calendar/month/2026/2")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    // Event API tests

    @Test
    void createEventReturnsCreated() {
        String eventJson = """
            {
                "title": "Test Event",
                "description": "Test Description",
                "startDate": "2026-02-25T10:00:00",
                "endDate": "2026-02-25T11:00:00",
                "color": "BLUE",
                "allDay": false
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(eventJson)
        .when()
            .post("/api/v1/calendar/events")
        .then()
            .statusCode(CREATED.value())
            .body("id", notNullValue())
            .body("title", equalTo("Test Event"))
            .body("color", equalTo("BLUE"));
    }

    @Test
    void createEventWithInvalidDataReturnsBadRequest() {
        String eventJson = """
            {
                "title": "",
                "startDate": "2026-02-25T10:00:00",
                "endDate": "2026-02-25T11:00:00"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(eventJson)
        .when()
            .post("/api/v1/calendar/events")
        .then()
            .statusCode(BAD_REQUEST.value());
    }

    @Test
    void updateEventReturnsOk() {
    
        String createJson = """
            {
                "title": "Original Event",
                "description": "Original",
                "startDate": "2026-02-25T10:00:00",
                "endDate": "2026-02-25T11:00:00",
                "color": "BLUE",
                "allDay": false
            }
            """;

        Response createResponse = given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(createJson)
        .when()
            .post("/api/v1/calendar/events");

        int eventId = createResponse.path("id");

        String updateJson = """
            {
                "title": "Updated Event",
                "description": "Updated",
                "startDate": "2026-02-25T14:00:00",
                "endDate": "2026-02-25T15:00:00",
                "color": "GREEN",
                "allDay": false
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/calendar/events/" + eventId)
        .then()
            .statusCode(OK.value())
            .body("title", equalTo("Updated Event"))
            .body("color", equalTo("GREEN"));
    }

    @Test
    void deleteEventReturnsOk() {
        String createJson = """
            {
                "title": "Event to Delete",
                "startDate": "2026-02-25T10:00:00",
                "endDate": "2026-02-25T11:00:00",
                "color": "RED",
                "allDay": false
            }
            """;

        Response createResponse = given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(createJson)
        .when()
            .post("/api/v1/calendar/events");

        int eventId = createResponse.path("id");

        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/calendar/events/" + eventId)
        .then()
            .statusCode(OK.value());
    }

    @Test
    void getEventsByDateRangeReturnsEvents() {
        given()
            .cookie("AuthToken", authToken)
            .queryParam("start", "2026-02-01T00:00:00")
            .queryParam("end", "2026-02-28T23:59:59")
        .when()
            .get("/api/v1/calendar/events")
        .then()
            .statusCode(OK.value())
            .body("$", notNullValue());
    }

    // Task API tests

    @Test
    void createTaskReturnsCreated() {
        String taskJson = """
            {
                "title": "Test Task",
                "description": "Test Description",
                "taskDate": "2026-02-25"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(taskJson)
        .when()
            .post("/api/v1/calendar/tasks")
        .then()
            .statusCode(CREATED.value())
            .body("id", notNullValue())
            .body("title", equalTo("Test Task"))
            .body("completed", equalTo(false));
    }

    @Test
    void createTaskWithEmptyTitleReturnsBadRequest() {
        String taskJson = """
            {
                "title": "",
                "taskDate": "2026-02-25"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(taskJson)
        .when()
            .post("/api/v1/calendar/tasks")
        .then()
            .statusCode(BAD_REQUEST.value());
    }

    @Test
    void updateTaskReturnsOk() {
        String createJson = """
            {
                "title": "Original Task",
                "description": "Original",
                "taskDate": "2026-02-25"
            }
            """;

        Response createResponse = given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(createJson)
        .when()
            .post("/api/v1/calendar/tasks");

        int taskId = createResponse.path("id");
        String updateJson = """
            {
                "title": "Updated Task",
                "description": "Updated",
                "taskDate": "2026-02-26"
            }
            """;

        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/v1/calendar/tasks/" + taskId)
        .then()
            .statusCode(OK.value())
            .body("title", equalTo("Updated Task"));
    }

    @Test
    void toggleTaskCompleteReturnsOk() {

        String createJson = """
            {
                "title": "Task to Toggle",
                "taskDate": "2026-02-25"
            }
            """;

        Response createResponse = given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(createJson)
        .when()
            .post("/api/v1/calendar/tasks");

        int taskId = createResponse.path("id");

        given()
            .cookie("AuthToken", authToken)
        .when()
            .patch("/api/v1/calendar/tasks/" + taskId + "/toggle")
        .then()
            .statusCode(OK.value())
            .body("completed", equalTo(true));

        given()
            .cookie("AuthToken", authToken)
        .when()
            .patch("/api/v1/calendar/tasks/" + taskId + "/toggle")
        .then()
            .statusCode(OK.value())
            .body("completed", equalTo(false));
    }

    @Test
    void deleteTaskReturnsOk() {
        String createJson = """
            {
                "title": "Task to Delete",
                "taskDate": "2026-02-25"
            }
            """;

        Response createResponse = given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body(createJson)
        .when()
            .post("/api/v1/calendar/tasks");

        int taskId = createResponse.path("id");

        given()
            .cookie("AuthToken", authToken)
        .when()
            .delete("/api/v1/calendar/tasks/" + taskId)
        .then()
            .statusCode(OK.value());
    }

    @Test
    void getTasksByDateReturnsTasksOrError() {
        given()
            .cookie("AuthToken", authToken)
            .queryParam("date", "2026-02-25")
        .when()
            .get("/api/v1/calendar/tasks")
        .then()
            .statusCode(OK.value())
            .body("$", notNullValue());
    }

    @Test
    void getPendingTasksReturnsTasks() {
        given()
            .cookie("AuthToken", authToken)
            .queryParam("pendingOnly", "true")
        .when()
            .get("/api/v1/calendar/tasks")
        .then()
            .statusCode(OK.value())
            .body("$", notNullValue());
    }
}
