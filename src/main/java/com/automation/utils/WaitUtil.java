package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Wait Utility - Provides comprehensive wait methods for Selenium
 * Includes explicit, fluent, and custom waits
 */
public class WaitUtil {

    private static final Logger logger = LogManager.getLogger(WaitUtil.class);
    private final WebDriver driver;
    private final long defaultTimeout;
    private final long pollingInterval;

    /**
     * Constructor with default timeouts
     */
    public WaitUtil(WebDriver driver) {
        this(driver, 20, 500);
    }

    /**
     * Constructor with custom timeouts
     */
    public WaitUtil(WebDriver driver, long timeoutInSeconds, long pollingInMillis) {
        this.driver = driver;
        this.defaultTimeout = timeoutInSeconds;
        this.pollingInterval = pollingInMillis;
    }

    // ==================== Visibility Waits ====================

    /**
     * Wait for element to be visible
     */
    public WebElement waitForVisible(By locator) {
        return waitForVisible(locator, defaultTimeout);
    }

    /**
     * Wait for element to be visible with custom timeout
     */
    public WebElement waitForVisible(By locator, long timeoutInSeconds) {
        logger.debug("Waiting for element visibility: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be visible
     */
    public WebElement waitForVisible(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for all elements to be visible
     */
    public List<WebElement> waitForAllVisible(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ==================== Clickable Waits ====================

    /**
     * Wait for element to be clickable
     */
    public WebElement waitForClickable(By locator) {
        return waitForClickable(locator, defaultTimeout);
    }

    /**
     * Wait for element to be clickable with custom timeout
     */
    public WebElement waitForClickable(By locator, long timeoutInSeconds) {
        logger.debug("Waiting for element to be clickable: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for WebElement to be clickable
     */
    public WebElement waitForClickable(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.elementToBeClickable(element));
    }

    // ==================== Presence Waits ====================

    /**
     * Wait for element to be present in DOM
     */
    public WebElement waitForPresence(By locator) {
        logger.debug("Waiting for element presence: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for all elements to be present
     */
    public List<WebElement> waitForAllPresent(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    // ==================== Invisibility Waits ====================

    /**
     * Wait for element to disappear
     */
    public boolean waitForInvisible(By locator) {
        logger.debug("Waiting for element to disappear: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to disappear with custom timeout
     */
    public boolean waitForInvisible(By locator, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ==================== Text Waits ====================

    /**
     * Wait for text to be present in element
     */
    public boolean waitForTextPresent(By locator, String text) {
        logger.debug("Waiting for text '{}' in element: {}", text, locator);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Wait for text to be present in element value
     */
    public boolean waitForTextInValue(By locator, String text) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.textToBePresentInElementValue(locator, text));
    }

    // ==================== Frame Waits ====================

    /**
     * Wait and switch to frame
     */
    public WebDriver waitForFrameAndSwitch(By locator) {
        logger.debug("Waiting for frame: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    /**
     * Wait and switch to frame by index
     */
    public WebDriver waitForFrameAndSwitch(int index) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
    }

    // ==================== Alert Waits ====================

    /**
     * Wait for alert to be present
     */
    public Alert waitForAlert() {
        logger.debug("Waiting for alert");
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.alertIsPresent());
    }

    // ==================== URL Waits ====================

    /**
     * Wait for URL to contain text
     */
    public boolean waitForUrlContains(String text) {
        logger.debug("Waiting for URL to contain: {}", text);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.urlContains(text));
    }

    /**
     * Wait for exact URL
     */
    public boolean waitForUrl(String url) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.urlToBe(url));
    }

    // ==================== Page Load Waits ====================

    /**
     * Wait for page to load completely
     */
    public void waitForPageLoad() {
        logger.debug("Waiting for page to load");
        new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Wait for jQuery to complete
     */
    public void waitForJQuery() {
        logger.debug("Waiting for jQuery");
        new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(webDriver -> {
                try {
                    return (Boolean) ((JavascriptExecutor) webDriver)
                        .executeScript("return jQuery.active == 0");
                } catch (Exception e) {
                    return true; // jQuery not present
                }
            });
    }

    /**
     * Wait for Angular to complete
     */
    public void waitForAngular() {
        logger.debug("Waiting for Angular");
        String script = "return (window.angular !== undefined) && " +
            "(angular.element(document).injector() !== undefined) && " +
            "(angular.element(document).injector().get('$http').pendingRequests.length === 0)";

        new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(webDriver -> {
                try {
                    return (Boolean) ((JavascriptExecutor) webDriver).executeScript(script);
                } catch (Exception e) {
                    return true; // Angular not present
                }
            });
    }

    // ==================== Fluent Wait ====================

    /**
     * Create fluent wait with custom configuration
     */
    public <T> T fluentWait(Function<WebDriver, T> condition) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(defaultTimeout))
            .pollingEvery(Duration.ofMillis(pollingInterval))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .ignoring(ElementNotInteractableException.class);

        return wait.until(condition);
    }

    /**
     * Custom wait with condition
     */
    public <T> T customWait(ExpectedCondition<T> condition, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(condition);
    }

    // ==================== Attribute Waits ====================

    /**
     * Wait for attribute to contain value
     */
    public boolean waitForAttributeContains(By locator, String attribute, String value) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    /**
     * Wait for attribute to be exact value
     */
    public boolean waitForAttributeToBe(By locator, String attribute, String value) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.attributeToBe(locator, attribute, value));
    }

    // ==================== Number of Elements Wait ====================

    /**
     * Wait for specific number of elements
     */
    public List<WebElement> waitForNumberOfElements(By locator, int number) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.numberOfElementsToBe(locator, number));
    }

    /**
     * Wait for elements to be more than specified number
     */
    public List<WebElement> waitForMoreThan(By locator, int number) {
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, number));
    }

    // ==================== Page Stability Waits ====================

    /**
     * Wait for page to be stable (document ready + no active AJAX + no animations)
     * Use this as a replacement for arbitrary Thread.sleep() calls
     */
    public void waitForPageStability() {
        waitForPageStability(defaultTimeout);
    }

    /**
     * Wait for page to be stable with custom timeout
     */
    public void waitForPageStability(long timeoutInSeconds) {
        logger.debug("Waiting for page stability");
        waitForPageLoad();
        waitForAjaxComplete();
        waitForAnimationsComplete();
    }

    /**
     * Wait for all AJAX requests to complete (jQuery + vanilla XHR)
     */
    public void waitForAjaxComplete() {
        logger.debug("Waiting for AJAX to complete");
        new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(webDriver -> {
                try {
                    // Check jQuery AJAX
                    Boolean jQueryComplete = (Boolean) ((JavascriptExecutor) webDriver)
                        .executeScript("return (typeof jQuery === 'undefined' || jQuery.active === 0)");
                    // Check vanilla XHR via custom tracking (if available)
                    Boolean xhrComplete = (Boolean) ((JavascriptExecutor) webDriver)
                        .executeScript("return (typeof window.activeXHR === 'undefined' || window.activeXHR === 0)");
                    return jQueryComplete && xhrComplete;
                } catch (Exception e) {
                    return true;
                }
            });
    }

    /**
     * Wait for CSS animations and transitions to complete
     */
    public void waitForAnimationsComplete() {
        logger.debug("Waiting for animations to complete");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> {
                    try {
                        String script = "return document.getAnimations ? document.getAnimations().length === 0 : true";
                        return (Boolean) ((JavascriptExecutor) webDriver).executeScript(script);
                    } catch (Exception e) {
                        return true;
                    }
                });
        } catch (Exception e) {
            // Animations API may not be supported, continue
            logger.debug("Animation check skipped: {}", e.getMessage());
        }
    }

    /**
     * Wait for element to be stable (no attribute changes for a short period)
     */
    public boolean waitForElementStable(By locator) {
        return waitForElementStable(locator, 500);
    }

    /**
     * Wait for element to be stable with custom stability period in milliseconds
     */
    public boolean waitForElementStable(By locator, long stabilityPeriodMs) {
        logger.debug("Waiting for element to be stable: {}", locator);
        try {
            WebElement element = waitForVisible(locator);
            String previousHtml = element.getAttribute("outerHTML");

            // Wait for stability period
            try {
                Thread.sleep(stabilityPeriodMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            // Check if element is still the same
            String currentHtml = element.getAttribute("outerHTML");
            return previousHtml.equals(currentHtml);
        } catch (Exception e) {
            logger.warn("Element stability check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Wait for loading overlay/spinner to disappear
     */
    public void waitForLoadingOverlayToDisappear() {
        logger.debug("Waiting for loading overlay to disappear");
        By[] commonLoaders = {
            By.cssSelector(".loading"),
            By.cssSelector(".loader"),
            By.cssSelector(".spinner"),
            By.cssSelector("[class*='loading']"),
            By.cssSelector("[class*='spinner']"),
            By.xpath("//div[contains(@class,'bg-opacity')]")
        };

        for (By loader : commonLoaders) {
            try {
                List<WebElement> elements = driver.findElements(loader);
                if (!elements.isEmpty()) {
                    waitForInvisible(loader, 10);
                }
            } catch (Exception e) {
                // Continue checking other loaders
            }
        }
    }

    /**
     * Wait for element text to be non-empty
     */
    public boolean waitForTextNotEmpty(By locator) {
        return waitForTextNotEmpty(locator, defaultTimeout);
    }

    /**
     * Wait for element text to be non-empty with custom timeout
     */
    public boolean waitForTextNotEmpty(By locator, long timeoutInSeconds) {
        logger.debug("Waiting for text to be non-empty: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
            .until(webDriver -> {
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
     * Wait for element value attribute to be non-empty (for input fields)
     */
    public boolean waitForValueNotEmpty(By locator) {
        logger.debug("Waiting for value to be non-empty: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(webDriver -> {
                try {
                    WebElement element = webDriver.findElement(locator);
                    String value = element.getAttribute("value");
                    return value != null && !value.trim().isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });
    }

    /**
     * Wait for element count to change (useful after adding/removing items)
     */
    public boolean waitForElementCountChange(By locator, int previousCount) {
        logger.debug("Waiting for element count to change from {}: {}", previousCount, locator);
        return new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout))
            .until(webDriver -> {
                try {
                    int currentCount = webDriver.findElements(locator).size();
                    return currentCount != previousCount;
                } catch (Exception e) {
                    return false;
                }
            });
    }

    // ==================== Static Wait (use sparingly) ====================

    /**
     * Hard wait - use only when absolutely necessary (e.g., third-party integrations)
     * @deprecated Prefer explicit waits like waitForPageStability(), waitForVisible(), etc.
     */
    @Deprecated
    public void hardWait(long milliseconds) {
        logger.warn("Using hard wait for {} ms - consider using explicit wait", milliseconds);
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
