package es.wrapitup.wrapitup_planner.e2e;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import es.wrapitup.wrapitup_planner.WrapitupPlannerApplication;

@Tag("client-e2e")
@SpringBootTest(classes = WrapitupPlannerApplication.class)
public abstract class BaseWebTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

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

    protected String getBaseUrl() {
        return "http://localhost:4200/";
    }

    protected void registerUser(String username, String email, String password) {
        driver.get(getBaseUrl() + "register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-card")));

        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("repeatPassword")).sendKeys(password);

        WebElement submitRegister = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".actions button[type='submit']")));
        submitRegister.click();

        wait.until(ExpectedConditions.urlContains("/login"));
    }

    protected void loginUser(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameLogin"))).sendKeys(username);
        driver.findElement(By.id("passwordLogin")).sendKeys(password);

        WebElement submitLogin = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".actions button[type='submit']")));
        submitLogin.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlToBe(getBaseUrl() + "profile"),
            ExpectedConditions.urlToBe(getBaseUrl()),
            ExpectedConditions.urlContains("/banned")
        ));
    }

    protected void registerAndLogin(String username, String email, String password) {
        registerUser(username, email, password);
        loginUser(username, password);
    }
}
