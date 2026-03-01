package es.wrapitup.wrapitup_planner.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CalendarWebTest extends BaseWebTest {

    @Test
    void testNavigateToCalendar() {
        long ts = System.currentTimeMillis();
        String username = "calendarUser" + ts;
        String email = "calendar+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "calendar");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar-header")));
        
        WebElement monthDisplay = driver.findElement(By.cssSelector(".calendar-header h2"));
        assertTrue(monthDisplay.getText().length() > 0, "Month display should show current month");
    }

    @Test
    void testCreateEvent() {
        long ts = System.currentTimeMillis();
        String username = "eventUser" + ts;
        String email = "event+" + ts + "@example.com";
        String password = "Password123";
        String eventTitle = "Meeting " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "calendar");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar-header")));

        WebElement newEventButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".new-event-button")));
        newEventButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dialog-container")));

        wait.until(ExpectedConditions.elementToBeClickable(By.id("event-title"))).sendKeys(eventTitle);
        
        WebElement startDateInput = driver.findElement(By.id("start-date"));
        WebElement endDateInput = driver.findElement(By.id("end-date"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2026-03-15';", startDateInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2026-03-16';", endDateInput);
        
        WebElement colorOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".color-option")));
        colorOption.click();

        WebElement saveButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".save-button")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".dialog-container")));

        WebElement calendarGrid = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".calendar-grid")));
        assertTrue(calendarGrid.isDisplayed(), "Calendar grid should be visible after creating event");
    }

    @Test
    void testOpenDayViewAndCreateTask() {
        long ts = System.currentTimeMillis();
        String username = "taskUser" + ts;
        String email = "task+" + ts + "@example.com";
        String password = "Password123";
        String taskTitle = "Buy groceries " + ts;

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "calendar");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar-header")));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".calendar-day")));

        WebElement firstDay = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".calendar-day:not(.empty)")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstDay);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dialog-container")));

        WebElement newTaskButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".add-task-button")));
        newTaskButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("task-title")));

        driver.findElement(By.id("task-title")).sendKeys(taskTitle);
        driver.findElement(By.id("task-description")).sendKeys("Don't forget milk");

        WebElement saveTaskButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".save-button")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveTaskButton);

        WebElement taskList = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".task-list")));
        assertTrue(taskList.isDisplayed(), "Task list should be visible after creating task");
    }

    @Test
    void testToggleTaskCompletion() {
        long ts = System.currentTimeMillis();
        String username = "toggleUser" + ts;
        String email = "toggle+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "calendar");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar-header")));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".calendar-day")));

        WebElement firstDay = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".calendar-day:not(.empty)")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstDay);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dialog-container")));

        WebElement newTaskButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".add-task-button")));
        newTaskButton.click();

        driver.findElement(By.id("task-title")).sendKeys("Complete this task");
        WebElement saveButton = driver.findElement(By.cssSelector(".save-button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".task-list")));
        
        WebElement taskCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".task-checkbox")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", taskCheckbox);

        wait.until(ExpectedConditions.attributeContains(
                By.cssSelector(".task-item"), "class", "completed"));
        
        WebElement completedTask = driver.findElement(By.cssSelector(".task-item.completed"));
        assertTrue(completedTask.isDisplayed(), "Task should show as completed");
    }

    @Test
    void testDeleteEvent() {
        long ts = System.currentTimeMillis();
        String username = "deleteUser" + ts;
        String email = "delete+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "calendar");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar-header")));

        WebElement newEventButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".new-event-button")));
        newEventButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dialog-container")));

        driver.findElement(By.id("event-title")).sendKeys("Event to delete");
        WebElement saveButton = driver.findElement(By.cssSelector(".save-button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".dialog-container")));

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".calendar-day")));
        WebElement firstDay = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".calendar-day:not(.empty)")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstDay);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dialog-container")));

        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".delete-button-small")));
        deleteButton.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".event-item")));
    }

    @Test
    void testNavigateBetweenMonths() {
        long ts = System.currentTimeMillis();
        String username = "navUser" + ts;
        String email = "nav+" + ts + "@example.com";
        String password = "Password123";

        registerAndLogin(username, email, password);

        driver.get(getBaseUrl() + "calendar");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar-header")));

        WebElement currentMonth = driver.findElement(By.cssSelector(".calendar-header h2"));
        String originalMonth = currentMonth.getText();

        WebElement nextMonthButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".calendar-header .nav-button:last-child")));
        nextMonthButton.click();

        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(
                By.cssSelector(".calendar-header h2"), originalMonth)));

        WebElement previousMonthButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".calendar-header .nav-button:first-child")));
        previousMonthButton.click();

        wait.until(ExpectedConditions.textToBe(
                By.cssSelector(".calendar-header h2"), originalMonth));
    }
}
