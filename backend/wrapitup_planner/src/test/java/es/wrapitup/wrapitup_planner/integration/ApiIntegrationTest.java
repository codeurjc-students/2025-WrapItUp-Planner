package es.wrapitup.wrapitup_planner.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;
import static org.springframework.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void getTestData() {
        given()
            .when()
            .get("/api/v1/notes/1")
            .then()
            .statusCode(OK.value())
            .contentType(ContentType.JSON)
            .body("id", equalTo(1)); 
    }

    @Test
    void registerAndLoginSuccess() {
        String username = "itestuser";

        // register
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"username\":\"" + username + "\", \"email\":\"" + username + "@example.com\", \"password\":\"pass1234\"}"
            )
        .when()
            .post("/api/v1/auth/user")
        .then()
            .statusCode(CREATED.value())
            .body("message", containsString("User registered"));

        // login
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\", \"password\":\"pass1234\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(OK.value())
            .contentType(ContentType.JSON)
            .body("status", equalTo("SUCCESS"))
            .cookie("AuthToken", notNullValue())
            .cookie("RefreshToken", notNullValue());
    }

    @Test
    void loginFailureWrongPassword() {
        // attempt login with non-existing user / wrong password
        var resp =
            given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"nonexistent\", \"password\":\"wrong\"}")
            .when()
                .post("/api/v1/auth/login");

        if (resp.statusCode() == OK.value()) {
            resp.then().body("status", equalTo("FAILURE"));
        } else {
            resp.then().statusCode(401);
        }
    }
}