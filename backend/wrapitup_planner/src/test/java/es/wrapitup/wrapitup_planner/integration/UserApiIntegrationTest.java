package es.wrapitup.wrapitup_planner.integration;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.springframework.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    private void registerUser(String username, String email, String password) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"email\":\"" + email + "\", \"password\":\"" + password + "\"}")
        .when()
            .post("/api/v1/auth/user");
    }

    private String loginUser(String username, String password) {
        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}")
        .when()
            .post("/api/v1/auth/login");

        return loginResponse.getCookie("AuthToken");
    }

    private UserModel createAdminUser(String username, String password) {
        UserModel admin = new UserModel();
        admin.setUsername(username);
        admin.setEmail(username + "@example.com");
        admin.setDisplayName(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRoles(Arrays.asList("USER", "ADMIN"));
        admin.setStatus(UserStatus.ACTIVE);
        return userRepository.save(admin);
    }

    @Test
    void registerUserWithValidData() {
        String username = "validuser_" + System.currentTimeMillis();

        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"email\":\"" + username + "@example.com\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/user")
        .then()
            .statusCode(CREATED.value())
            .body("message", containsString("User registered"));
    }

    @Test
    void registerUserWithShortPassword() {
        String username = "shortpass_" + System.currentTimeMillis();

        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"email\":\"" + username + "@example.com\", \"password\":\"pass\"}")
        .when()
            .post("/api/v1/auth/user")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("error", equalTo("Password must be at least 8 characters long"));
    }

    @Test
    void registerUserWithMissingFields() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"\", \"email\":\"test@example.com\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/user")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("error", equalTo("Missing or blank fields"));
    }

    @Test
    void registerDuplicateUsername() {
        String username = "duplicate_" + System.currentTimeMillis();

        registerUser(username, username + "@example.com", "password123");

        // Second user attempts to use used username
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"email\":\"different@example.com\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/user")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("error", equalTo("User already exists"));
    }

    @Test
    void getCurrentUserWithoutAuthentication() {
        given()
        .when()
            .get("/api/v1/users")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void getCurrentUserWithAuthentication() {
        String username = "authuser_" + System.currentTimeMillis();

        registerUser(username, username + "@example.com", "password123");
        authToken = loginUser(username, "password123");

        // Get current user
        given()
            .cookie("AuthToken", authToken)
        .when()
            .get("/api/v1/users")
        .then()
            .statusCode(OK.value())
            .body("username", equalTo(username))
            .body("email", equalTo(username + "@example.com"))
            .body("displayName", equalTo(username)); // displayName defaults to username
    }

    @Test
    void updateUserWithValidData() {
        String username = "updateuser_" + System.currentTimeMillis();

        registerUser(username, username + "@example.com", "password123");
        authToken = loginUser(username, "password123");

        // Update user
        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body("{\"displayName\":\"New Display Name\", \"email\":\"newemail@example.com\"}")
        .when()
            .put("/api/v1/users")
        .then()
            .statusCode(OK.value())
            .body("displayName", equalTo("New Display Name"))
            .body("email", equalTo("newemail@example.com"))
            .body("username", equalTo(username)); // username should not change
    }

    @Test
    void updateUserWithBlankEmail() {
        String username = "blankemail_" + System.currentTimeMillis();

        registerUser(username, username + "@example.com", "password123");
        authToken = loginUser(username, "password123");

        
        given()
            .cookie("AuthToken", authToken)
            .contentType(ContentType.JSON)
            .body("{\"displayName\":\"New Name\", \"email\":\"\"}")
        .when()
            .put("/api/v1/users")
        .then()
            .statusCode(BAD_REQUEST.value())
            .body(equalTo("Email is required"));
    }

    @Test
    void testLogoutAndRefreshToken() {
        String username = "logoutuser_" + System.currentTimeMillis();

        registerUser(username, username + "@example.com", "password123");
        
        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");

        authToken = loginResponse.getCookie("AuthToken");
        refreshToken = loginResponse.getCookie("RefreshToken");

        // Logout
        given()
            .cookie("AuthToken", authToken)
        .when()
            .post("/api/v1/auth/logout")
        .then()
            .statusCode(OK.value())
            .body("status", equalTo("SUCCESS"));

        // Refresh token
        given()
            .cookie("RefreshToken", refreshToken)
        .when()
            .post("/api/v1/auth/refresh")
        .then()
            .statusCode(OK.value())
            .cookie("AuthToken", notNullValue());
    }

    // Ban and unban tests

    @Test
    void adminCanBanUser() {
        String adminUsername = "admin_" + System.currentTimeMillis();
        String targetUsername = "target_" + System.currentTimeMillis();

        // Create admin user directly in database
        createAdminUser(adminUsername, "admin123");
        String adminToken = loginUser(adminUsername, "admin123");

        // Register target user
        registerUser(targetUsername, targetUsername + "@example.com", "password123");
        
        // Get target user ID
        Response targetUserResponse = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + targetUsername + "\", \"password\":\"password123\"}")
        .when()
            .post("/api/v1/auth/login");

        String targetToken = targetUserResponse.getCookie("AuthToken");
        
        Response getUserResponse = given()
            .cookie("AuthToken", targetToken)
        .when()
            .get("/api/v1/users");
        
        Long targetUserId = getUserResponse.jsonPath().getLong("id");

        // Admin bans target user
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/users/" + targetUserId + "/ban")
        .then()
            .statusCode(OK.value())
            .body("status", equalTo("BANNED"));
    }

    @Test
    void adminCanUnbanUser() {
        String adminUsername = "admin_unban_" + System.currentTimeMillis();
        String targetUsername = "target_unban_" + System.currentTimeMillis();

        // Create admin user directly in database
        createAdminUser(adminUsername, "admin123");
        String adminToken = loginUser(adminUsername, "admin123");

        // Register and ban target user
        registerUser(targetUsername, targetUsername + "@example.com", "password123");
        String targetToken = loginUser(targetUsername, "password123");
        
        Response getUserResponse = given()
            .cookie("AuthToken", targetToken)
        .when()
            .get("/api/v1/users");
        
        Long targetUserId = getUserResponse.jsonPath().getLong("id");

        // Ban user first
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/users/" + targetUserId + "/ban");

        // Admin unbans target user
        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/users/" + targetUserId + "/unban")
        .then()
            .statusCode(OK.value())
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    void nonAdminCannotBanUser() {
        String regularUsername = "regular_" + System.currentTimeMillis();
        String targetUsername = "target_regular_" + System.currentTimeMillis();

        registerUser(regularUsername, regularUsername + "@example.com", "password123");
        String regularToken = loginUser(regularUsername, "password123");

        registerUser(targetUsername, targetUsername + "@example.com", "password123");
        String targetToken = loginUser(targetUsername, "password123");
        
        Response getUserResponse = given()
            .cookie("AuthToken", targetToken)
        .when()
            .get("/api/v1/users");
        
        Long targetUserId = getUserResponse.jsonPath().getLong("id");

        // Regular user tries to ban
        given()
            .cookie("AuthToken", regularToken)
        .when()
            .post("/api/v1/users/" + targetUserId + "/ban")
        .then()
            .statusCode(FORBIDDEN.value())
            .body(containsString("Only admins can ban users"));
    }

    @Test
    void nonAdminCannotUnbanUser() {
        String regularUsername = "regular_unban_" + System.currentTimeMillis();
        String targetUsername = "target_regular_unban_" + System.currentTimeMillis();

        registerUser(regularUsername, regularUsername + "@example.com", "password123");
        String regularToken = loginUser(regularUsername, "password123");

        registerUser(targetUsername, targetUsername + "@example.com", "password123");
        String targetToken = loginUser(targetUsername, "password123");
        
        Response getUserResponse = given()
            .cookie("AuthToken", targetToken)
        .when()
            .get("/api/v1/users");
        
        Long targetUserId = getUserResponse.jsonPath().getLong("id");

        // Regular user tries to unban
        given()
            .cookie("AuthToken", regularToken)
        .when()
            .post("/api/v1/users/" + targetUserId + "/unban")
        .then()
            .statusCode(FORBIDDEN.value())
            .body(containsString("Only admins can unban users"));
    }

    @Test
    void banNonExistentUserReturns404() {
        String adminUsername = "admin_notfound_" + System.currentTimeMillis();
        createAdminUser(adminUsername, "admin123");
        String adminToken = loginUser(adminUsername, "admin123");

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/users/999999/ban")
        .then()
            .statusCode(NOT_FOUND.value())
            .body(containsString("User not found"));
    }

    @Test
    void unbanNonExistentUserReturns404() {
        String adminUsername = "admin_unban_notfound_" + System.currentTimeMillis();
        createAdminUser(adminUsername, "admin123");
        String adminToken = loginUser(adminUsername, "admin123");

        given()
            .cookie("AuthToken", adminToken)
        .when()
            .post("/api/v1/users/999999/unban")
        .then()
            .statusCode(NOT_FOUND.value())
            .body(containsString("User not found"));
    }

    @Test
    void banUserWithoutAuthReturns401() {
        given()
        .when()
            .post("/api/v1/users/1/ban")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void unbanUserWithoutAuthReturns401() {
        given()
        .when()
            .post("/api/v1/users/1/unban")
        .then()
            .statusCode(UNAUTHORIZED.value());
    }
}
