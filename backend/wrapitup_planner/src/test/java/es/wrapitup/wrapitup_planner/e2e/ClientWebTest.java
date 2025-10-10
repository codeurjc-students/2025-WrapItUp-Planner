package es.wrapitup.wrapitup_planner.e2e;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import es.wrapitup.wrapitup_planner.WrapitupPlannerApplication;

@Tag("client-e2e")
@SpringBootTest(classes = WrapitupPlannerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //change to random later
public class ClientWebTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new"); // o "--headless" para versiones más antiguas
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--remote-allow-origins=*");
    options.addArguments("--user-data-dir=/tmp/chrome-user-data-" + System.currentTimeMillis());
        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void teardown() {
        if (driver != null)
            driver.quit();
    }

    private String getBaseUrl() {
        return "http://localhost:4200/";
    }

    @Test
    void testBuscarAINoteId1() {
        driver.get(getBaseUrl() + "notes");
        WebElement inputId = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("noteId"))
        );
        inputId.clear();
        inputId.sendKeys("1");
        WebElement buscarButton = driver.findElement(By.xpath("//button[text()='Buscar']"));
        buscarButton.click();
        WebElement detailDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-ainote-detail div"))
        );
        String detailText = detailDiv.getText();
        assertTrue(detailText.contains("Id: 1"));
        assertTrue(detailText.contains("Overview: Resumen general de la sesión de IA"));
        assertTrue(detailText.contains("Summary: Este es el contenido detallado del resumen"));
        assertTrue(detailText.contains("JSON Questions: {\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}"));
        assertTrue(detailText.contains("Visibility: true"));
        assertTrue(detailText.contains("UserId: 1"));
    }

}
