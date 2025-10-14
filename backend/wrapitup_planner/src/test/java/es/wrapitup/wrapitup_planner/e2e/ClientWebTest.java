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
@SpringBootTest(classes = WrapitupPlannerApplication.class)
public class ClientWebTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new"); 
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
    void testMostrarAINoteId1() {
        driver.get(getBaseUrl() + "notes/1");

        WebElement detailDiv = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-ainote-detail div")));

        String detailText = detailDiv.getText();

        assertTrue(detailText.contains("Id: 1")); 
    }
}
