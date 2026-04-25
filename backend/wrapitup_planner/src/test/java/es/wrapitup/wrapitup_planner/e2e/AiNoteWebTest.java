package es.wrapitup.wrapitup_planner.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AiNoteWebTest extends BaseWebTest {

    @Test
    void aiModeShowsDropzoneAndHidesManualFields() {
        long ts = System.currentTimeMillis();
        String username = "aiuser" + ts;
        String email = "aiuser+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mode-toggle")));

        clickMode("ai");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dropzone")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("visibilityAi")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("categoryAi")));

        assertTrue(driver.findElements(By.id("title")).isEmpty());
        assertTrue(driver.findElements(By.id("overview")).isEmpty());
        assertTrue(driver.findElements(By.id("summary")).isEmpty());
        assertTrue(driver.findElements(By.id("visibility")).isEmpty());
        assertTrue(driver.findElements(By.id("category")).isEmpty());
    }

    @Test
    void aiFileSelectionShowsFilename() throws Exception {
        long ts = System.currentTimeMillis();
        String username = "aiuser" + ts;
        String email = "aiuser+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        clickMode("ai");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("aiFile")));

        Path tempFile = Files.createTempFile("wrapitup-ai-", ".txt");
        Files.writeString(tempFile, "AI file test", StandardCharsets.UTF_8);

        driver.findElement(By.id("aiFile")).sendKeys(tempFile.toAbsolutePath().toString());

        WebElement selectedFile = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dropzone-file")));
        assertTrue(selectedFile.getText().contains(tempFile.getFileName().toString()));
    }

    @Test
    void switchingBackToManualShowsManualFields() {
        long ts = System.currentTimeMillis();
        String username = "aiuser" + ts;
        String email = "aiuser+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        clickMode("ai");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dropzone")));

        clickMode("manual");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        assertTrue(driver.findElements(By.cssSelector(".dropzone")).isEmpty());
    }

    private void clickMode(String value) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mode-toggle")));
        List<WebElement> modes = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("input[name='mode']"), 1));

        for (WebElement mode : modes) {
            if (value.equals(mode.getAttribute("value"))) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", mode);
                return;
            }
        }

        WebElement fallback = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(., '" + ("ai".equals(value) ? "Create with AI" : "Manual") + "')]")));
        fallback.click();
    }
}
