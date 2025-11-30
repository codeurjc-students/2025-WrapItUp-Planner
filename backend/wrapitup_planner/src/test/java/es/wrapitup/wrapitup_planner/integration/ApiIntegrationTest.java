package es.wrapitup.wrapitup_planner.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

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