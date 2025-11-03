package es.wrapitup.wrapitup_planner.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import es.wrapitup.wrapitup_planner.repository.AINoteRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;
import static org.springframework.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

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
}