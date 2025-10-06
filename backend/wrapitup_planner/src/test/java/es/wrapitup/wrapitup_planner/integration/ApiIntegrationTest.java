package es.wrapitup.wrapitup_planner.integration;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private AINoteRepository aiNoteRepository;

    @BeforeEach
    void setUp(){
        RestAssured.port = port;
    }

    @Test
    void getTestData() {
        given()
            .when()
            .get("/api/v1/notes/1")
            .then()
            .statusCode(OK.value()) // Verify HTTP 200 OK
            .contentType(ContentType.JSON)
            .body("id", equalTo(1))
            .body("overview", equalTo("Resumen general de la sesión de IA"))
            .body("summary", equalTo("Este es el contenido detallado del resumen"))
            .body("jsonQuestions", equalTo("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}"))
            .body("visibility", equalTo(true));
    }


}
