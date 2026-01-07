package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Test Wait Helper - Provides explicit wait methods for test classes
 * Use these methods instead of Thread.sleep() for more reliable test execution
 */
public class TestWaitHelper {

    private static final Logger logger = LogManager.getLogger(TestWaitHelper.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor jsExecutor;

    public TestWaitHelper(WebDriver driver) {
        this(driver, 20);
    }

    public TestWaitHelper(WebDriver driver, long timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    /**
     * Wait for page to be fully stable (document ready + AJAX + animations)
     * Use this as a replacement for Thread.sleep() after page actions
     */
    public void waitForPageStability() {
        logger.debug("Waiting for page stability");
        waitForDocumentReady();
        waitForAjaxComplete();
        waitForAnimationsComplete();
    }

    /**
     * Wait for document ready state
     */
    public void waitForDocumentReady() {
        wait.until(webDriver -> jsExecutor.executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Wait for AJAX calls to complete
     */
    public void waitForAjaxComplete() {
        try {
            wait.until(webDriver -> {
                try {
                    return (Boolean) jsExecutor.executeScript(
                        "return (typeof jQuery === 'undefined' || jQuery.active === 0)");
                } catch (Exception e) {
                    return true;
                }
            });
        } catch (Exception e) {
            // jQuery not present, continue
        }
    }

    /**
     * Wait for CSS animations to complete
     */
    public void waitForAnimationsComplete() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(webDriver -> {
                try {
                    return (Boolean) jsExecutor.executeScript(
                        "return document.getAnimations ? document.getAnimations().length === 0 : true");
                } catch (Exception e) {
                    return true;
                }
            });
        } catch (Exception e) {
            // Animations API not supported, continue
        }
    }

    /**
     * Wait for loading overlay/spinner to disappear
     */
    public void waitForLoadingToDisappear() {
        logger.debug("Waiting for loading overlay to disappear");
        By[] loaders = {
            By.cssSelector("[class*='loading']"),
            By.cssSelector("[class*='spinner']"),
            By.xpath("//div[contains(@class,'bg-opacity')]")
        };
        for (By loader : loaders) {
            try {
                if (!driver.findElements(loader).isEmpty()) {
                    new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.invisibilityOfElementLocated(loader));
                }
            } catch (Exception e) {
                // Continue
            }
        }
    }

    /**
     * Wait for element to be visible
     */
    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be visible with custom timeout
     */
    public WebElement waitForVisible(By locator, long timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be clickable
     */
    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for element to be clickable with custom timeout
     */
    public WebElement waitForClickable(By locator, long timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for element to be present in DOM
     */
    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for element to disappear
     */
    public boolean waitForInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait for URL to contain specific text
     */
    public boolean waitForUrlContains(String text) {
        return wait.until(ExpectedConditions.urlContains(text));
    }

    /**
     * Wait for URL to not contain specific text
     */
    public boolean waitForUrlNotContains(String text) {
        return wait.until(webDriver -> !webDriver.getCurrentUrl().contains(text));
    }

    /**
     * Wait for text to be present in element
     */
    public boolean waitForTextPresent(By locator, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Wait for element text to be non-empty
     */
    public boolean waitForTextNotEmpty(By locator) {
        return wait.until(webDriver -> {
            try {
                WebElement element = webDriver.findElement(locator);
                String text = element.getText();
                return text != null && !text.trim().isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Wait for table rows to be present
     */
    public boolean waitForTableRows(By tableRowLocator) {
        return wait.until(webDriver -> {
            try {
                return webDriver.findElements(tableRowLocator).size() > 0;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Wait for element count to be more than specified number
     */
    public boolean waitForMoreThan(By locator, int count) {
        return wait.until(webDriver -> webDriver.findElements(locator).size() > count);
    }

    /**
     * Wait for specific number of elements
     */
    public boolean waitForNumberOfElements(By locator, int count) {
        try {
            wait.until(ExpectedConditions.numberOfElementsToBe(locator, count));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait after navigation action (e.g., clicking a link, form submission)
     * This combines page stability with loader check
     */
    public void waitAfterNavigation() {
        waitForLoadingToDisappear();
        waitForPageStability();
    }

    /**
     * Wait after form interaction (e.g., selecting dropdown, entering text)
     * Shorter wait for UI updates
     */
    public void waitAfterFormAction() {
        waitForAnimationsComplete();
        waitForAjaxComplete();
    }

    /**
     * Wait for alert to be present and return it
     */
    public Alert waitForAlert() {
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    /**
     * Get the underlying WebDriverWait for custom conditions
     */
    public WebDriverWait getWait() {
        return wait;
    }
}
