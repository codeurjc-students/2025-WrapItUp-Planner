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
    options.addArguments("--ignore-certificate-errors");
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

    @Test
    void testRegisterShortPassword() {
        driver.get(getBaseUrl() + "register");

        WebElement usernameInput = driver.findElement(By.id("username"));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement repeatInput = driver.findElement(By.id("repeatPassword"));

        usernameInput.sendKeys("shortpass_user");
        emailInput.sendKeys("shortpass@example.com");
        passwordInput.sendKeys("12345");
        repeatInput.sendKeys("12345");

        WebElement submit = driver.findElement(By.cssSelector(".actions button[type='submit']"));
        assertFalse(submit.isEnabled());

        WebElement errorDiv = driver.findElement(By.cssSelector(".error"));
        assertEquals("Password must be at least 8 characters", errorDiv.getText());
    }

    @Test
    void testRegisterNewUser() {
        long ts = System.currentTimeMillis();
        String username = "e2e_user_" + ts;
        String email = "e2e+" + ts + "@example.com";
        String password = "Password123";

        driver.get(getBaseUrl() + "register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-card")));

        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("repeatPassword")).sendKeys(password);

        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".actions button[type='submit']")));
        submit.click();

        wait.until(ExpectedConditions.urlContains("/login"));

        WebElement h2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-card h2")));
        assertEquals("Login", h2.getText());
    }


}
