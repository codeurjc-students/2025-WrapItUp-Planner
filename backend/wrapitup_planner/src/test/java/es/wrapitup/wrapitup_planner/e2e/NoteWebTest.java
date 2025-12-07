package es.wrapitup.wrapitup_planner.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NoteWebTest extends BaseWebTest {

    @Test
    void testCreateNote() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Test Note " + ts;
        String noteOverview = "This is a test overview";
        String noteSummary = "This is a detailed test summary";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement overviewInput = driver.findElement(By.id("overview"));
        WebElement summaryInput = driver.findElement(By.id("summary"));
        WebElement visibilitySelect = driver.findElement(By.id("visibility"));

        titleInput.sendKeys(noteTitle);
        overviewInput.sendKeys(noteOverview);
        summaryInput.sendKeys(noteSummary);
        visibilitySelect.sendKeys("PUBLIC");

        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-create")));
        createButton.click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        
        wait.until(ExpectedConditions.urlContains("/notes/"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/notes/"), "URL should contain /notes/ but was: " + currentUrl);

        WebElement noteHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".note-title")));
        assertEquals(noteTitle, noteHeading.getText());

        WebElement visibilityBadge = driver.findElement(By.cssSelector(".visibility-badge"));
        assertEquals("PUBLIC", visibilityBadge.getText());
    }

    @Test
    void testCreatePrivateNote() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Private Note " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Private overview");
        driver.findElement(By.id("summary")).sendKeys("Private summary");
        driver.findElement(By.id("visibility")).sendKeys("PRIVATE");

        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-create")));
        createButton.click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement visibilityBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".visibility-badge")));
        assertEquals("PRIVATE", visibilityBadge.getText());
    }

    @Test
    void testEditNote() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String originalTitle = "Original Title " + ts;
        String updatedTitle = "Updated Title " + ts;
        String updatedOverview = "Updated overview content";
        String updatedSummary = "Updated summary content";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(originalTitle);
        driver.findElement(By.id("overview")).sendKeys("Original overview");
        driver.findElement(By.id("summary")).sendKeys("Original summary");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement editButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".btn-edit")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".edit-mode")));

        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement overviewInput = driver.findElement(By.id("overview"));
        WebElement summaryInput = driver.findElement(By.id("summary"));

        titleInput.clear();
        titleInput.sendKeys(updatedTitle);
        overviewInput.clear();
        overviewInput.sendKeys(updatedOverview);
        summaryInput.clear();
        summaryInput.sendKeys(updatedSummary);

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-save")));
        saveButton.click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".edit-mode")));

        WebElement noteTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".note-title")));
        assertEquals(updatedTitle, noteTitle.getText());

        String pageContent = driver.findElement(By.cssSelector(".note-content")).getText();
        assertTrue(pageContent.contains(updatedOverview));
        assertTrue(pageContent.contains(updatedSummary));
    }

    @Test
    void testEditNoteChangeVisibility() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Visibility Test Note " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Test overview");
        driver.findElement(By.id("summary")).sendKeys("Test summary");
        driver.findElement(By.id("visibility")).sendKeys("PUBLIC");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement visibilityBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".visibility-badge")));
        assertEquals("PUBLIC", visibilityBadge.getText());

        WebElement editButton = driver.findElement(By.cssSelector(".btn-edit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".edit-mode")));

        WebElement visibilitySelect = driver.findElement(By.id("visibility"));
        visibilitySelect.sendKeys("PRIVATE");

        driver.findElement(By.cssSelector(".btn-save")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".edit-mode")));

        visibilityBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".visibility-badge")));
        assertEquals("PRIVATE", visibilityBadge.getText());
    }

    @Test
    void testDeleteNote() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Note to Delete " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Will be deleted");
        driver.findElement(By.id("summary")).sendKeys("This note will be deleted");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));
        String noteUrl = driver.getCurrentUrl();

        WebElement deleteButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".btn-delete")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        
        try {
            Thread.sleep(500);
            if (ExpectedConditions.alertIsPresent().apply(driver) != null) {
                driver.switchTo().alert().accept();
            }
        } catch (Exception e) {
            
        }

        driver.get(noteUrl);
        
        // Verify "Note not found" alert appears when accessing deleted note
        wait.until(ExpectedConditions.alertIsPresent());
        String alertText = driver.switchTo().alert().getText();
        assertEquals("Note not found", alertText, "Should show 'Note not found' alert for deleted note");
        driver.switchTo().alert().accept();
        
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".loading")),
            ExpectedConditions.urlContains("/")
        ));
        
        String finalUrl = driver.getCurrentUrl();
        assertFalse(finalUrl.equals(noteUrl), "Should have been redirected away from deleted note URL");
    }

    @Test
    void testCancelNoteCreation() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys("This will be cancelled");
        driver.findElement(By.id("overview")).sendKeys("Cancel test");

        WebElement cancelButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-cancel")));
        cancelButton.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlToBe(getBaseUrl()),
            ExpectedConditions.urlContains("/profile")
        ));
        
        assertFalse(driver.getCurrentUrl().contains("/notes/create"));
    }

    @Test
    void testCancelNoteEdit() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String originalTitle = "Original Title " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(originalTitle);
        driver.findElement(By.id("overview")).sendKeys("Original overview");
        driver.findElement(By.id("summary")).sendKeys("Original summary");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement editButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".btn-edit")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".edit-mode")));

        WebElement titleInput = driver.findElement(By.id("title"));
        titleInput.clear();
        titleInput.sendKeys("This change will be cancelled");

        editButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".btn-edit")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".edit-mode")));

        WebElement noteTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".note-title")));
        assertEquals(originalTitle, noteTitle.getText());
    }

    @Test
    void testShareNoteWorkflow() {
        long ts = System.currentTimeMillis();
        String ownerUsername = "genericUser" + ts;
        String ownerEmail = "genericUser+" + ts + "@example.com";
        String sharedUsername = "genericUser2" + ts;
        String sharedEmail = "genericUser2+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Shared Note " + ts;

        registerUser(sharedUsername, sharedEmail, password);

        driver.get(getBaseUrl() + "login");
        registerAndLogin(ownerUsername, ownerEmail, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("This will be shared");
        driver.findElement(By.id("summary")).sendKeys("Shared note content");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement shareButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".btn-share")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", shareButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-overlay")));

        WebElement usernameInput = driver.findElement(By.id("shareUsername"));
        usernameInput.sendKeys(sharedUsername);

        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-confirm")));
        confirmButton.click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal-overlay")));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/notes/"), "Should still be on note page after sharing");
        assertFalse(driver.findElements(By.cssSelector(".modal-overlay")).stream()
                .anyMatch(WebElement::isDisplayed), "Modal should be closed");
    }

    @Test
    void testCreateComment() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Note with Comments " + ts;
        String commentText = "This is a test comment";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Test overview");
        driver.findElement(By.id("summary")).sendKeys("Test summary");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement commentsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".comments-section")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", commentsSection);

        // Add a comment
        WebElement commentInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".comment-input")));
        commentInput.sendKeys(commentText);

        WebElement sendButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-send-comment")));
        sendButton.click();

        //Verify comment appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".comment-item")));
        
        WebElement commentContent = driver.findElement(By.cssSelector(".comment-content"));
        assertEquals(commentText, commentContent.getText());

        //Verify comment author
        WebElement commentAuthor = driver.findElement(By.cssSelector(".comment-author"));
        assertTrue(commentAuthor.getText().contains(username) || 
                   commentAuthor.getText().length() > 0);
    }

    @Test
    void testDeleteComment() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Note for Comment Deletion " + ts;
        String commentText = "This comment will be deleted";

        registerAndLogin(username, email, password);


        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Test overview");
        driver.findElement(By.id("summary")).sendKeys("Test summary");
        driver.findElement(By.cssSelector(".btn-create")).click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));

        WebElement commentsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".comments-section")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", commentsSection);

        WebElement commentInput = driver.findElement(By.cssSelector(".comment-input"));
        commentInput.sendKeys(commentText);
        driver.findElement(By.cssSelector(".btn-send-comment")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".comment-item")));

        //Delete comment
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".menu-toggle")));
        menuButton.click();

        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".menu-item.delete")));
        deleteButton.click();

        wait.until(ExpectedConditions.alertIsPresent()).accept();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//p[@class='comment-content' and text()='" + commentText + "']")));

        //Verify no comments
        assertTrue(driver.findElements(By.cssSelector(".comment-item")).isEmpty() ||
                   driver.findElement(By.cssSelector(".comments-section")).getText().contains("No comments"));
    }

    @Test
    void testAccessMyNotesPage() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "my-notes");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-notes-container")));

        WebElement pageTitle = driver.findElement(By.cssSelector("h1"));
        assertTrue(pageTitle.getText().contains("My Notes") || pageTitle.getText().contains("Mis Notas"));
    }

    @Test
    void testFilterNotesByCategory() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String mathNoteTitle = "Math Note " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(mathNoteTitle);
        driver.findElement(By.id("overview")).sendKeys("Math overview");
        driver.findElement(By.id("summary")).sendKeys("Math summary");
        
        WebElement categorySelect = driver.findElement(By.id("category"));
        categorySelect.sendKeys("MATHS");
        
        driver.findElement(By.cssSelector(".btn-create")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));


        driver.get(getBaseUrl() + "my-notes");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-notes-container")));

        // Click on MATHS category
        WebElement mathsCategory = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'category-card')]//span[contains(@class, 'category-name') and contains(text(), 'Maths')]")));
        mathsCategory.click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-card")));
        
        WebElement noteCard = driver.findElement(By.cssSelector(".note-card"));
        assertTrue(noteCard.getText().contains(mathNoteTitle));
    }

    @Test
    void testSearchNotesInMyNotes() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String searchableTitle = "Searchable Note " + ts;

        registerAndLogin(username, email, password);

        // Create a note
        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(searchableTitle);
        driver.findElement(By.id("overview")).sendKeys("Overview");
        driver.findElement(By.id("summary")).sendKeys("Summary");
        driver.findElement(By.cssSelector(".btn-create")).click();
        
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));


        driver.get(getBaseUrl() + "my-notes");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-notes-container")));

        WebElement searchInput = driver.findElement(By.cssSelector("input[type='text']"));
        searchInput.sendKeys("Searchable");

 
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-card")));
        
        WebElement noteCard = driver.findElement(By.cssSelector(".note-card"));
        assertTrue(noteCard.getText().contains(searchableTitle));
    }

    @Test
    void testDeleteNoteFromMyNotes() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String noteTitle = "Note to Delete " + ts;

        registerAndLogin(username, email, password);

        // Create a note
        driver.get(getBaseUrl() + "notes/create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-container")));

        driver.findElement(By.id("title")).sendKeys(noteTitle);
        driver.findElement(By.id("overview")).sendKeys("Overview");
        driver.findElement(By.id("summary")).sendKeys("Summary");
        driver.findElement(By.cssSelector(".btn-create")).click();
        
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("/notes/"));


        driver.get(getBaseUrl() + "my-notes");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-notes-container")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".note-card")));

        // Delete the note
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-options")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);


        wait.until(ExpectedConditions.alertIsPresent()).accept();

 
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'note-card') and contains(., '" + noteTitle + "')]")));
    }
}
