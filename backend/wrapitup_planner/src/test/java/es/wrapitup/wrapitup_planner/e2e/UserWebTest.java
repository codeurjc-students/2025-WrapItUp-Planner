package es.wrapitup.wrapitup_planner.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UserWebTest extends BaseWebTest {

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
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";

        registerUser(username, email, password);

        WebElement h2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-card h2")));
        assertEquals("Login", h2.getText());
    }

    @Test
    void testProfilePageAccessAndDisplay() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);
        
        driver.get(getBaseUrl() + "profile");

        WebElement profileCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".profile-card")));
        
        assertNotNull(profileCard);

        WebElement usernameLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[contains(text(), 'Username (Login)')]/following-sibling::div[@class='info-value']")));
        
        assertTrue(usernameLabel.getText().equals(username));

        WebElement emailLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[text()='Email']/following-sibling::div[@class='info-value']")));
        
        assertTrue(emailLabel.getText().equals(email));
    }

    @Test
    void testProfileEditAndSave() {
        long ts = System.currentTimeMillis();
        String username = "genericUser" + ts;
        String email = "genericUser+" + ts + "@example.com";
        String password = "Password123";
        String newDisplayName = "Updated Name " + ts;
        String newEmail = "updated+" + ts + "@example.com";

        registerAndLogin(username, email, password);
        
        driver.get(getBaseUrl() + "profile");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".profile-card")));

        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Edit Profile')]")));
        editButton.click();

        WebElement displayNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("displayName")));
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("email")));

        displayNameInput.clear();
        displayNameInput.sendKeys(newDisplayName);
        
        emailInput.clear();
        emailInput.sendKeys(newEmail);

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Save Changes')]")));
        saveButton.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("displayName")));

        WebElement displayNameValue = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[text()='Display Name']/following-sibling::div[@class='info-value']")));
        assertTrue(displayNameValue.getText().equals(newDisplayName));

        WebElement emailValue = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[text()='Email']/following-sibling::div[@class='info-value']")));
        assertTrue(emailValue.getText().equals(newEmail));
    }
}
