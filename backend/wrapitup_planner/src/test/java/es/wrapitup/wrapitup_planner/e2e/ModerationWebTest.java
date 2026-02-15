package es.wrapitup.wrapitup_planner.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ModerationWebTest extends BaseWebTest {

    private void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

        private void acceptAlertIfPresent() {
                try {
                        acceptAlert();
                } catch (Exception ignored) {
                        
                }
        }

    private void logoutCurrentUser() {
        driver.get(getBaseUrl() + "profile");
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".logout-button")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    private void loginAsAdmin() {
        driver.manage().deleteAllCookies();
        driver.get(getBaseUrl() + "login");
        loginUser("admin", "12345678");
    }

    private void createNoteAndReportComment(String username, String email, String password, String noteTitle,
            String commentText) {
        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Overview for moderation checks");
        driver.findElement(By.id("summary")).sendKeys("Summary for moderation checks");
        driver.findElement(By.cssSelector(".btn-create")).click();

        acceptAlert();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement commentsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".comments-section")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", commentsSection);

        WebElement commentInput = driver.findElement(By.cssSelector(".comment-input"));
        commentInput.sendKeys(commentText);
        driver.findElement(By.cssSelector(".btn-send-comment")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".comment-item")));

        WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".menu-toggle")));
        menuToggle.click();

        WebElement reportButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".menu-item.report")));
        reportButton.click();

        acceptAlert();
        acceptAlertIfPresent();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".comment-item")));
    }

    private WebElement findReportedCommentCard(String commentText) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".comment-card")));
        List<WebElement> cards = driver.findElements(By.cssSelector(".comment-card"));
        return cards.stream()
                .filter(card -> card.getText().contains(commentText))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Reported comment card not found"));
    }

    @Test
    void testReportedCommentVisibleAndIgnorableByAdmin() {
        long ts = System.currentTimeMillis();
        String username = "reporter" + ts;
        String email = "reporter+" + ts + "@example.com";
        String password = "Password123";
        String commentText = "Reported comment " + ts;

        createNoteAndReportComment(username, email, password, "Moderation Note " + ts, commentText);
        logoutCurrentUser();

        loginAsAdmin();
        driver.get(getBaseUrl() + "admin/reported-comments");

        By commentCardByText = By.xpath("//div[contains(@class,'comment-card')][contains(., '" + commentText + "')]");
        WebElement commentCard = wait.until(ExpectedConditions.visibilityOfElementLocated(commentCardByText));
        assertTrue(commentCard.getText().contains(commentText));

        commentCard.findElement(By.cssSelector(".btn-ignore")).click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.invisibilityOfElementLocated(commentCardByText),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".no-comments"))));
    }

    @Test
    void testAdminBanAndUnbanUserFromReportedProfile() {
        long ts = System.currentTimeMillis();
        String offenderUsername = "offender" + ts;
        String offenderEmail = "offender+" + ts + "@example.com";
        String offenderPassword = "Password123";
        String commentText = "Comment to ban user " + ts;

        createNoteAndReportComment(offenderUsername, offenderEmail, offenderPassword,
                "Note For Ban " + ts, commentText);
        logoutCurrentUser();

        loginAsAdmin();
        driver.get(getBaseUrl() + "admin/reported-comments");

        WebElement commentCard = findReportedCommentCard(commentText);
        commentCard.findElement(By.cssSelector(".btn-view-profile")).click();

        wait.until(ExpectedConditions.urlContains("/profile"));
        String reportedProfileUrl = driver.getCurrentUrl();

        WebElement banButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".ban-button")));
        banButton.click();
        acceptAlert();
        acceptAlertIfPresent();

        By statusLocator = By.xpath("//label[text()='Status']/following-sibling::div[@class='info-value']");
        wait.until(ExpectedConditions.textToBe(statusLocator, "BANNED"));
        WebElement statusValue = wait.until(ExpectedConditions.visibilityOfElementLocated(statusLocator));
        assertEquals("BANNED", statusValue.getText());

        driver.get(getBaseUrl() + "profile");
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".logout-button")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.manage().deleteAllCookies();
        driver.get(getBaseUrl() + "login");
        loginUser(offenderUsername, offenderPassword);

        wait.until(ExpectedConditions.urlContains("/banned"));
        WebElement bannedTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".banned-card h1")));
        assertEquals("Account Banned", bannedTitle.getText());

        driver.manage().deleteAllCookies();
        driver.get(getBaseUrl() + "login");
        loginUser("admin", "12345678");
        driver.get(reportedProfileUrl);

        WebElement unbanButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".unban-button")));
        unbanButton.click();
        acceptAlert();
        acceptAlertIfPresent();

        wait.until(ExpectedConditions.textToBe(statusLocator, "ACTIVE"));
        WebElement statusAfterUnban = wait.until(ExpectedConditions.visibilityOfElementLocated(statusLocator));
        assertEquals("ACTIVE", statusAfterUnban.getText());
    }
}