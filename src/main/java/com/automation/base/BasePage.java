package com.automation.base;

import com.automation.ai.AILocatorHelper;
import com.automation.listeners.TestListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.TimeoutException;

/**
 * Base Page Class - Foundation for all Page Object classes
 * Provides common web element interactions with AI-powered fallback locators
 */
public abstract class BasePage {

    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor jsExecutor;
    protected AILocatorHelper aiLocatorHelper;

    /**
     * Constructor - Initialize page with WebDriver
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.actions = new Actions(driver);
        this.jsExecutor = (JavascriptExecutor) driver;
        this.aiLocatorHelper = new AILocatorHelper(driver);
        PageFactory.initElements(driver, this);
    }

    // ==================== Wait Methods ====================

    /**
     * Wait for element to be visible
     */
    protected WebElement waitForVisibility(By locator) {
        logger.debug("Waiting for element visibility: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be visible
     */
    protected WebElement waitForVisibility(WebElement element) {
        logger.debug("Waiting for element visibility");
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for element to be clickable
     */
    protected WebElement waitForClickable(By locator) {
        logger.debug("Waiting for element to be clickable: {}", locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for element to be clickable
     */
    protected WebElement waitForClickable(WebElement element) {
        logger.debug("Waiting for element to be clickable");
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wait for element to be present in DOM
     */
    protected WebElement waitForPresence(By locator) {
        logger.debug("Waiting for element presence: {}", locator);
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for element to disappear
     */
    protected boolean waitForInvisibility(By locator) {
        logger.debug("Waiting for element to disappear: {}", locator);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait for text to be present in element
     */
    protected boolean waitForTextPresent(By locator, String text) {
        logger.debug("Waiting for text '{}' in element: {}", text, locator);
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // ==================== Click Methods ====================

    /**
     * Click on element with wait
     */
    protected void click(By locator) {
        logger.info("Clicking element: {}", locator);
        try {
            waitForClickable(locator).click();
        } catch (StaleElementReferenceException e) {
            logger.warn("Stale element, retrying click: {}", locator);
            waitForClickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            logger.warn("Click intercepted, using JavaScript click: {}", locator);
            jsClick(locator);
        }
    }

    /**
     * Click on WebElement with wait
     */
    protected void click(WebElement element) {
        logger.info("Clicking element");
        try {
            waitForClickable(element).click();
        } catch (ElementClickInterceptedException e) {
            logger.warn("Click intercepted, using JavaScript click");
            jsClick(element);
        }
    }

    /**
     * JavaScript click - useful when regular click fails
     */
    protected void jsClick(By locator) {
        logger.info("JavaScript click on element: {}", locator);
        WebElement element = waitForPresence(locator);
        jsExecutor.executeScript("arguments[0].click();", element);
    }

    /**
     * JavaScript click on WebElement
     */
    protected void jsClick(WebElement element) {
        logger.info("JavaScript click on element");
        jsExecutor.executeScript("arguments[0].click();", element);
    }

    /**
     * Double click on element
     */
    protected void doubleClick(By locator) {
        logger.info("Double clicking element: {}", locator);
        WebElement element = waitForClickable(locator);
        actions.doubleClick(element).perform();
    }

    /**
     * Right click on element
     */
    protected void rightClick(By locator) {
        logger.info("Right clicking element: {}", locator);
        WebElement element = waitForClickable(locator);
        actions.contextClick(element).perform();
    }

    // ==================== Input Methods ====================

    /**
     * Type text into element after clearing it
     */
    protected void type(By locator, String text) {
        logger.info("Typing '{}' into element: {}", text, locator);
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Type text into WebElement after clearing it
     */
    protected void type(WebElement element, String text) {
        logger.info("Typing '{}' into element", text);
        waitForVisibility(element);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Type text using JavaScript (for stubborn elements)
     */
    protected void jsType(By locator, String text) {
        logger.info("JavaScript typing '{}' into element: {}", text, locator);
        WebElement element = waitForPresence(locator);
        jsExecutor.executeScript("arguments[0].value = arguments[1];", element, text);
    }

    /**
     * Send keys without clearing
     */
    protected void sendKeys(By locator, CharSequence... keys) {
        logger.info("Sending keys to element: {}", locator);
        waitForVisibility(locator).sendKeys(keys);
    }

    /**
     * Clear element text
     */
    protected void clear(By locator) {
        logger.info("Clearing element: {}", locator);
        waitForVisibility(locator).clear();
    }

    // ==================== Dropdown Methods ====================

    /**
     * Select dropdown option by visible text
     */
    protected void selectByVisibleText(By locator, String text) {
        logger.info("Selecting '{}' from dropdown: {}", text, locator);
        Select select = new Select(waitForVisibility(locator));
        select.selectByVisibleText(text);
    }

    /**
     * Select dropdown option by value
     */
    protected void selectByValue(By locator, String value) {
        logger.info("Selecting value '{}' from dropdown: {}", value, locator);
        Select select = new Select(waitForVisibility(locator));
        select.selectByValue(value);
    }

    /**
     * Select dropdown option by index
     */
    protected void selectByIndex(By locator, int index) {
        logger.info("Selecting index {} from dropdown: {}", index, locator);
        Select select = new Select(waitForVisibility(locator));
        select.selectByIndex(index);
    }

    /**
     * Get selected option text from dropdown
     */
    protected String getSelectedText(By locator) {
        Select select = new Select(waitForVisibility(locator));
        return select.getFirstSelectedOption().getText();
    }

    // ==================== Get Methods ====================

    /**
     * Get element text
     */
    protected String getText(By locator) {
        logger.debug("Getting text from element: {}", locator);
        return waitForVisibility(locator).getText();
    }

    /**
     * Get element text
     */
    protected String getText(WebElement element) {
        return waitForVisibility(element).getText();
    }

    /**
     * Get element attribute value
     */
    protected String getAttribute(By locator, String attribute) {
        logger.debug("Getting attribute '{}' from element: {}", attribute, locator);
        return waitForPresence(locator).getAttribute(attribute);
    }

    /**
     * Get element CSS property value
     */
    protected String getCssValue(By locator, String property) {
        logger.debug("Getting CSS property '{}' from element: {}", property, locator);
        return waitForPresence(locator).getCssValue(property);
    }

    // ==================== Verification Methods ====================

    // Check if element is displayed (By locator)
    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    // Check if element is displayed (WebElement)
    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    // Check if element is enabled (By locator)
    protected boolean isEnabled(By locator) {
        try {
            return driver.findElement(locator).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // Check if element is enabled (WebElement)
    protected boolean isEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Check if element is selected (checkbox/radio)
     */
    protected boolean isSelected(By locator) {
        try {
            return driver.findElement(locator).isSelected();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Check if element exists in DOM
     */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // ==================== Element List Methods ====================

    /**
     * Find all elements matching locator
     */
    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    /**
     * Get count of elements matching locator
     */
    protected int getElementCount(By locator) {
        return driver.findElements(locator).size();
    }

    // ==================== JavaScript Methods ====================

    /**
     * Scroll element into view
     */
    protected void scrollIntoView(By locator) {
        logger.debug("Scrolling element into view: {}", locator);
        WebElement element = waitForPresence(locator);
        jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /**
     * Scroll element into view
     */
    protected void scrollIntoView(WebElement element) {
        jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /**
     * Scroll to top of page
     */
    protected void scrollToTop() {
        jsExecutor.executeScript("window.scrollTo(0, 0);");
    }

    /**
     * Scroll to bottom of page
     */
    protected void scrollToBottom() {
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /**
     * Highlight element (useful for debugging)
     */
    protected void highlightElement(By locator) {
        WebElement element = waitForPresence(locator);
        String originalStyle = element.getAttribute("style");
        jsExecutor.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
        // Use explicit wait for visual feedback instead of Thread.sleep
        waitForPageStability();
        jsExecutor.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originalStyle);
    }

    // ==================== Actions Methods ====================

    /**
     * Hover over element
     */
    protected void hover(By locator) {
        logger.info("Hovering over element: {}", locator);
        WebElement element = waitForVisibility(locator);
        actions.moveToElement(element).perform();
    }

    /**
     * Drag and drop
     */
    protected void dragAndDrop(By source, By target) {
        logger.info("Drag and drop from {} to {}", source, target);
        WebElement sourceElement = waitForVisibility(source);
        WebElement targetElement = waitForVisibility(target);
        actions.dragAndDrop(sourceElement, targetElement).perform();
    }

    // ==================== Alert Methods ====================

    /**
     * Accept alert
     */
    protected void acceptAlert() {
        logger.info("Accepting alert");
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }

    /**
     * Dismiss alert
     */
    protected void dismissAlert() {
        logger.info("Dismissing alert");
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    /**
     * Get alert text
     */
    protected String getAlertText() {
        return wait.until(ExpectedConditions.alertIsPresent()).getText();
    }

    /**
     * Type in alert prompt
     */
    protected void typeInAlert(String text) {
        logger.info("Typing '{}' in alert", text);
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.sendKeys(text);
        alert.accept();
    }

    // ==================== Window/Frame Methods ====================

    /**
     * Switch to frame by locator
     */
    protected void switchToFrame(By locator) {
        logger.info("Switching to frame: {}", locator);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    /**
     * Switch to frame by index
     */
    protected void switchToFrame(int index) {
        logger.info("Switching to frame index: {}", index);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
    }

    /**
     * Switch to default content
     */
    protected void switchToDefaultContent() {
        logger.info("Switching to default content");
        driver.switchTo().defaultContent();
    }

    /**
     * Switch to new window/tab
     */
    protected void switchToNewWindow() {
        logger.info("Switching to new window");
        String mainWindow = driver.getWindowHandle();
        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(mainWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
    }

    /**
     * Close current window and switch back to main
     */
    protected void closeCurrentWindow() {
        logger.info("Closing current window");
        String mainWindow = driver.getWindowHandles().iterator().next();
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    // ==================== AI-Powered Methods ====================

    /**
     * Find element with AI fallback - uses multiple strategies
     * If primary locator fails, AI suggests alternative locators
     */
    protected WebElement findElementWithAI(By primaryLocator, String elementDescription) {
        logger.info("Finding element with AI fallback: {}", elementDescription);
        return aiLocatorHelper.findElementWithFallback(primaryLocator, elementDescription);
    }

    /**
     * Click element with AI healing
     */
    protected void clickWithAI(By primaryLocator, String elementDescription) {
        WebElement element = findElementWithAI(primaryLocator, elementDescription);
        click(element);
    }

    /**
     * Type with AI healing
     */
    protected void typeWithAI(By primaryLocator, String text, String elementDescription) {
        WebElement element = findElementWithAI(primaryLocator, elementDescription);
        type(element, text);
    }

    // ==================== Screenshot Methods ====================

    /**
     * Capture screenshot and attach to Extent Report
     * Call this method after navigating to a page to capture the landed page
     */
    public void captureScreenshotToReport(String screenshotName) {
        try {
            logger.info("Capturing screenshot: {}", screenshotName);
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

            // Attach to Extent Report
            if (TestListener.getExtentTest() != null) {
                TestListener.getExtentTest().addScreenCaptureFromBase64String(base64Screenshot, screenshotName);
                logger.info("Screenshot '{}' attached to report", screenshotName);
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    // ==================== Common Utility Methods ====================

    /**
     * Wait for page to be stable (document ready + AJAX complete + animations done)
     * Use this instead of Thread.sleep() for waiting after actions
     */
    protected void waitForPageStability() {
        logger.debug("Waiting for page stability");
        // Wait for document ready state
        wait.until(webDriver -> jsExecutor.executeScript("return document.readyState").equals("complete"));
        // Wait for any AJAX calls to complete
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
            // jQuery not present or other issue, continue
        }
        // Wait for animations to complete
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(webDriver -> {
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
    protected void waitForLoadingToDisappear() {
        logger.debug("Waiting for loading overlay to disappear");
        By[] loaders = {
            By.cssSelector("[class*='loading']"),
            By.cssSelector("[class*='spinner']"),
            By.xpath("//div[contains(@class,'bg-opacity')]")
        };
        for (By loader : loaders) {
            try {
                List<WebElement> elements = driver.findElements(loader);
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(loader));
                }
            } catch (Exception e) {
                // Continue
            }
        }
    }

    /**
     * @deprecated Use waitForPageStability() or explicit waits instead
     * Thread sleep - use only when absolutely necessary
     * @param milliseconds time to sleep
     */
    @Deprecated
    protected void sleep(long milliseconds) {
        logger.warn("Using Thread.sleep({}) - consider using explicit waits", milliseconds);
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted");
        }
    }

    /**
     * Get current URL
     * @return current page URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Clear field and type text - uses multiple strategies to ensure field is cleared
     * @param element WebElement to type into
     * @param text text to enter
     */
    protected void clearAndType(WebElement element, String text) {
        logger.debug("Clearing and typing '{}' into element", text);
        waitForVisibility(element);
        clearInputField(element);
        element.sendKeys(text);
    }

    /**
     * Clear field and type text using By locator
     * @param locator By locator
     * @param text text to enter
     */
    protected void clearAndType(By locator, String text) {
        logger.debug("Clearing and typing '{}' into element: {}", text, locator);
        WebElement element = waitForVisibility(locator);
        clearInputField(element);
        element.sendKeys(text);
    }

    /**
     * Clear input field using multiple strategies
     * Uses Ctrl+A followed by Delete/Backspace for reliable clearing
     * @param element the input element to clear
     */
    protected void clearInputField(WebElement element) {
        try {
            // First try standard clear
            element.clear();
            sleep(100);

            // If value still exists, use keyboard shortcuts
            String value = element.getAttribute("value");
            if (value != null && !value.isEmpty()) {
                // Select all and delete
                element.sendKeys(Keys.CONTROL + "a");
                sleep(50);
                element.sendKeys(Keys.DELETE);
                sleep(50);

                // Double check and use backspace if needed
                value = element.getAttribute("value");
                if (value != null && !value.isEmpty()) {
                    element.sendKeys(Keys.END);
                    for (int i = 0; i < value.length(); i++) {
                        element.sendKeys(Keys.BACK_SPACE);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error clearing field: {}", e.getMessage());
            // Fallback: try JavaScript clear
            try {
                jsExecutor.executeScript("arguments[0].value = '';", element);
            } catch (Exception jsError) {
                logger.debug("JS clear also failed: {}", jsError.getMessage());
            }
        }
    }

    /**
     * Navigate to URL
     * @param url URL to navigate to
     */
    protected void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        driver.get(url);
    }

    /**
     * Get page title
     * @return page title
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Refresh current page
     */
    protected void refreshPage() {
        logger.info("Refreshing page");
        driver.navigate().refresh();
    }

    /**
     * Navigate back
     */
    protected void navigateBack() {
        logger.info("Navigating back");
        driver.navigate().back();
    }

    /**
     * Wait for URL to contain specific text
     * @param urlPart partial URL to wait for
     * @return true if URL contains the text within timeout
     */
    protected boolean waitForUrlContains(String urlPart) {
        try {
            return wait.until(driver -> driver.getCurrentUrl().contains(urlPart));
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Execute JavaScript and return result
     * @param script JavaScript to execute
     * @param args arguments for script
     * @return script result
     */
    protected Object executeScript(String script, Object... args) {
        return jsExecutor.executeScript(script, args);
    }

    /**
     * Scroll by pixels
     * @param x horizontal scroll
     * @param y vertical scroll
     */
    protected void scrollBy(int x, int y) {
        jsExecutor.executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    // ==================== Dynamic Page Load Methods ====================

    /**
     * Wait for page to load by checking URL contains specific pattern
     * @param urlPattern the URL pattern to wait for (e.g., "/new_quote", "/quotes")
     * @return true if page loaded successfully
     */
    protected boolean waitForPageLoadByUrl(String urlPattern) {
        return waitForPageLoadByUrl(urlPattern, 10);
    }

    /**
     * Wait for page to load by checking URL contains specific pattern with custom timeout
     * @param urlPattern the URL pattern to wait for
     * @param timeoutSeconds timeout in seconds
     * @return true if page loaded successfully
     */
    protected boolean waitForPageLoadByUrl(String urlPattern, int timeoutSeconds) {
        logger.debug("Waiting for page with URL pattern: {}", urlPattern);
        sleep(2000); // Allow initial page render

        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            boolean result = customWait.until(d -> d.getCurrentUrl().contains(urlPattern));
            if (result) {
                logger.info("Page loaded successfully. URL contains: {}", urlPattern);
            }
            return result;
        } catch (TimeoutException e) {
            logger.error("Page load timeout. Expected URL pattern: {}, Current URL: {}",
                urlPattern, driver.getCurrentUrl());
            return false;
        }
    }

    /**
     * Wait for page to load and verify URL pattern, throw exception if not on expected page
     * @param urlPattern the URL pattern to verify
     * @param pageName name of the page for error messages
     */
    protected void waitForPageLoadOrFail(String urlPattern, String pageName) {
        logger.debug("Waiting for {} page to load", pageName);
        sleep(3000); // Allow initial page render

        String currentUrl = getCurrentUrl();
        if (!currentUrl.contains(urlPattern)) {
            logger.error("Not on {} page. Current URL: {}", pageName, currentUrl);
            captureScreenshotToReport("Wrong Page - Expected " + pageName);
            throw new RuntimeException("Not on " + pageName + " page. Current URL: " + currentUrl);
        }

        logger.info("On {} page", pageName);
        sleep(2000);
    }

    // ==================== Dynamic Toast/Notification Methods ====================

    /**
     * Check if a toast message with specific text is displayed
     * @param toastText the text to look for in the toast
     * @return true if toast is displayed
     */
    protected boolean isToastDisplayed(String toastText) {
        try {
            By toastLocator = By.xpath("//div[contains(@class,'toast')]//div[contains(text(),'" + toastText + "')]");
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a toast message is displayed using a specific locator
     * @param toastLocator the By locator for the toast element
     * @return true if toast is displayed
     */
    protected boolean isToastDisplayed(By toastLocator) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Safe Element State Check Methods ====================

    /**
     * Safely check if element is displayed without throwing exception
     * @param locator the By locator
     * @return true if displayed, false otherwise
     */
    protected boolean isElementDisplayedSafe(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely check if element is enabled without throwing exception
     * @param locator the By locator
     * @return true if enabled, false otherwise
     */
    protected boolean isElementEnabledSafe(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isDisplayed() && element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely check if element is disabled
     * @param locator the By locator
     * @return true if disabled, false otherwise
     */
    protected boolean isElementDisabledSafe(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return !element.isEnabled() ||
                   "true".equals(element.getAttribute("disabled")) ||
                   element.getAttribute("class").contains("disabled");
        } catch (Exception e) {
            return true; // Assume disabled if not found
        }
    }

    /**
     * Safely check if WebElement is displayed
     * @param element the WebElement
     * @return true if displayed, false otherwise
     */
    protected boolean isElementDisplayedSafe(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely check if WebElement is enabled
     * @param element the WebElement
     * @return true if enabled, false otherwise
     */
    protected boolean isElementEnabledSafe(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Dynamic Wait Methods ====================

    /**
     * Wait for element to appear with custom timeout
     * @param locator the By locator
     * @param timeoutSeconds timeout in seconds
     * @return true if element appeared
     */
    protected boolean waitForElementToAppear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Wait for element to be clickable with custom timeout
     * @param locator the By locator
     * @param timeoutSeconds timeout in seconds
     * @return true if element is clickable
     */
    protected boolean waitForElementToBeClickable(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.elementToBeClickable(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Wait for element to disappear with custom timeout
     * @param locator the By locator
     * @param timeoutSeconds timeout in seconds
     * @return true if element disappeared
     */
    protected boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ==================== Dynamic Element Finder Methods ====================

    /**
     * Find element by multiple XPath strategies
     * @param xpaths array of XPath strings to try
     * @return WebElement if found, null otherwise
     */
    protected WebElement findElementByMultipleXPaths(String... xpaths) {
        for (String xpath : xpaths) {
            try {
                java.util.List<WebElement> elements = driver.findElements(By.xpath(xpath));
                for (WebElement el : elements) {
                    if (el.isDisplayed()) {
                        logger.debug("Found element with xpath: {}", xpath);
                        return el;
                    }
                }
            } catch (Exception e) {
                // Continue to next strategy
            }
        }
        return null;
    }

    /**
     * Check if any element from multiple XPath strategies is displayed
     * @param xpaths array of XPath strings to try
     * @return true if any element is displayed
     */
    protected boolean isAnyElementDisplayed(String... xpaths) {
        return findElementByMultipleXPaths(xpaths) != null;
    }

    // ==================== PDF Utility Methods ====================

    /**
     * Get Chrome default download directory
     * @return path to downloads folder
     */
    protected String getDownloadDirectory() {
        String userHome = System.getProperty("user.home");
        return userHome + java.io.File.separator + "Downloads";
    }

    /**
     * Wait for PDF download and return file path
     * @param downloadDir directory to monitor
     * @param timeoutSeconds max wait time
     * @return path to downloaded PDF or null if timeout
     */
    protected String waitForPDFDownload(String downloadDir, int timeoutSeconds) {
        logger.info("Waiting for PDF download in: {}", downloadDir);
        java.io.File dir = new java.io.File(downloadDir);
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeout) {
            java.io.File[] files = dir.listFiles((d, name) ->
                name.toLowerCase().endsWith(".pdf") &&
                !name.endsWith(".crdownload") && !name.endsWith(".tmp"));

            if (files != null) {
                for (java.io.File file : files) {
                    if (file.lastModified() > startTime - 5000) {
                        logger.info("PDF downloaded: {}", file.getAbsolutePath());
                        return file.getAbsolutePath();
                    }
                }
            }
            sleep(1000);
        }
        logger.info("PDF download timeout");
        return null;
    }

    /**
     * Read PDF and extract text from all pages
     * @param pdfPath path to PDF file
     * @return extracted text
     */
    protected String readPDFText(String pdfPath) {
        try {
            java.io.File file = new java.io.File(pdfPath);
            org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(file);
            String text = new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
            doc.close();
            return text;
        } catch (Exception e) {
            logger.info("Error reading PDF: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Read specific page range from PDF
     * @param pdfPath path to PDF file
     * @param startPage start page (1-based)
     * @param endPage end page (1-based)
     * @return extracted text
     */
    protected String readPDFPages(String pdfPath, int startPage, int endPage) {
        try {
            java.io.File file = new java.io.File(pdfPath);
            org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(file);
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            String text = stripper.getText(doc);
            doc.close();
            return text;
        } catch (Exception e) {
            logger.info("Error reading PDF pages: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Check if text exists in PDF
     * @param pdfPath path to PDF file
     * @param searchText text to search for
     * @return true if found
     */
    protected boolean textExistsInPDF(String pdfPath, String searchText) {
        String pdfText = readPDFText(pdfPath);
        return pdfText.toLowerCase().contains(searchText.toLowerCase());
    }

    /**
     * Check if amount exists in PDF (handles various formats)
     * @param pdfPath path to PDF file
     * @param amount amount to search for
     * @return true if found
     */
    protected boolean amountExistsInPDFFile(String pdfPath, double amount) {
        String pdfText = readPDFText(pdfPath);
        String formatted = String.format("%.2f", amount);
        return pdfText.contains(formatted) || pdfText.contains("$" + formatted) ||
               pdfText.contains(String.valueOf((int) amount));
    }

    /**
     * Click download button using common patterns
     * @param buttonXpaths optional custom xpaths, uses defaults if empty
     * @return true if clicked successfully
     */
    protected boolean clickDownloadButton(String... buttonXpaths) {
        String[] xpaths = buttonXpaths.length > 0 ? buttonXpaths : new String[]{
            "//button[contains(text(),'Download')]",
            "//button[contains(@id,'download')]",
            "//a[contains(text(),'Download')]",
            "//button[contains(text(),'PDF')]"
        };

        WebElement btn = findElementByMultipleXPaths(xpaths);
        if (btn != null) {
            scrollIntoView(btn);
            sleep(500);
            btn.click();
            logger.info("Download button clicked");
            return true;
        }
        logger.info("Download button not found");
        return false;
    }

    /**
     * Log message to Extent Report
     * @param message message to log
     */
    protected void logToReport(String message) {
        try {
            com.aventstack.extentreports.ExtentTest extentTest =
                com.automation.listeners.TestListener.getExtentTest();
            if (extentTest != null) {
                extentTest.info("<pre>" + message.replace("\n", "<br/>") + "</pre>");
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Log HTML content to ExtentReport (for tables, formatted content)
     */
    public void logHtmlToReport(String htmlContent) {
        try {
            com.aventstack.extentreports.ExtentTest extentTest =
                com.automation.listeners.TestListener.getExtentTest();
            if (extentTest != null) {
                extentTest.info(htmlContent);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    // ==================== PDF Validation Helper Methods ====================

    /**
     * Validate a text field in PDF and add result to validation rows
     * @param validationRows List to store validation results
     * @param result PDFValidationResult object to track errors
     * @param pdfText Full PDF text content
     * @param fieldName Display name for the field
     * @param expectedValue Expected value from screen
     */
    protected void validatePDFTextField(java.util.List<String[]> validationRows, PDFValidationResult result,
                                        String pdfText, String fieldName, String expectedValue) {
        if (expectedValue == null || expectedValue.isEmpty()) return;

        String pdfTextLower = pdfText.toLowerCase();
        boolean found = pdfTextLower.contains(expectedValue.toLowerCase());
        String pdfValue = found ? expectedValue : "Not Found";
        validationRows.add(new String[]{fieldName, expectedValue, pdfValue, found ? "PASS" : "FAIL"});

        if (!found) {
            result.addError(fieldName + " '" + expectedValue + "' not found in PDF");
        }
    }

    /**
     * Validate an amount field in PDF and add result to validation rows
     * @param validationRows List to store validation results
     * @param result PDFValidationResult object to track errors
     * @param pdfText Full PDF text content
     * @param fieldName Display name for the field (e.g., "Coverage A Premium")
     * @param expectedValue Expected amount as string (e.g., "$1,500.00" or "1500")
     */
    protected void validatePDFAmountField(java.util.List<String[]> validationRows, PDFValidationResult result,
                                          String pdfText, String fieldName, String expectedValue) {
        double expected = parsePDFAmount(expectedValue);
        if (expected <= 0) return;

        boolean found = amountExistsInPDF(pdfText, expected);
        String formattedAmount = "$" + formatPDFAmount(expected);
        String pdfValue = found ? formattedAmount : "Not Found";
        validationRows.add(new String[]{fieldName, formattedAmount, pdfValue, found ? "PASS" : "FAIL"});

        if (!found) {
            result.addError(fieldName + " " + formattedAmount + " not found in PDF");
        }
    }

    /**
     * Validate a date field in PDF and add result to validation rows
     * @param validationRows List to store validation results
     * @param result PDFValidationResult object to track errors
     * @param pdfText Full PDF text content
     * @param fieldName Display name for the field
     * @param expectedDate Expected date value
     */
    protected void validatePDFDateField(java.util.List<String[]> validationRows, PDFValidationResult result,
                                        String pdfText, String fieldName, String expectedDate) {
        if (expectedDate == null || expectedDate.isEmpty()) return;

        boolean found = dateExistsInPDFText(pdfText, expectedDate);
        String pdfValue = found ? expectedDate : "Not Found";
        validationRows.add(new String[]{fieldName, expectedDate, pdfValue, found ? "PASS" : "FAIL"});

        if (!found) {
            result.addError(fieldName + " not found in PDF");
        }
    }

    /**
     * Add an info row to validation results (no validation, just display)
     */
    protected void addPDFInfoRow(java.util.List<String[]> validationRows, String fieldName, String pdfValue) {
        validationRows.add(new String[]{fieldName, "-", pdfValue, "INFO"});
    }

    /**
     * Check if date exists in PDF text (multiple formats)
     */
    protected boolean dateExistsInPDFText(String pdfText, String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;

        String pdfLower = pdfText.toLowerCase();
        String dateLower = dateStr.toLowerCase();

        // Direct match
        if (pdfLower.contains(dateLower)) return true;

        try {
            java.time.LocalDate date = null;

            // Parse input date
            String[] parsePatterns = {"yyyy-MM-dd", "MM/dd/yyyy", "M/d/yyyy", "dd/MM/yyyy", "MM-dd-yyyy"};
            for (String pattern : parsePatterns) {
                try {
                    date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern(pattern));
                    break;
                } catch (Exception ignored) {}
            }

            if (date == null) return false;

            // Check multiple output formats
            String[] checkPatterns = {
                "MM/dd/yyyy", "M/d/yyyy", "yyyy-MM-dd",
                "MMMM d, yyyy", "MMM d, yyyy", "MMMM dd, yyyy", "MMM dd, yyyy",
                "d MMMM yyyy", "dd MMMM yyyy"
            };

            for (String pattern : checkPatterns) {
                try {
                    String formatted = date.format(java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH));
                    if (pdfLower.contains(formatted.toLowerCase())) return true;
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            logger.debug("Date parsing failed: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Check if amount exists in PDF text (handles various formats)
     */
    protected boolean amountExistsInPDF(String pdfText, double amount) {
        String formatted = formatPDFAmount(amount);
        String withComma = formatPDFAmountWithComma(amount);
        int intAmount = (int) amount;

        // Check various formats
        if (pdfText.contains(formatted) || pdfText.contains("$" + formatted)) return true;
        if (pdfText.contains(withComma) || pdfText.contains("$" + withComma)) return true;
        if (pdfText.contains(String.valueOf(intAmount)) || pdfText.contains("$" + intAmount)) return true;

        // With spaces after $
        if (pdfText.contains("$ " + formatted) || pdfText.contains("$ " + withComma)) return true;

        // Rounded values
        String rounded1 = String.format("%.1f", amount);
        if (pdfText.contains(rounded1) || pdfText.contains("$" + rounded1)) return true;

        return false;
    }

    /**
     * Extract amount from PDF text following a label
     */
    protected double extractPDFAmountAfterLabel(String pdfText, String label) {
        try {
            String lowerText = pdfText.toLowerCase();
            String lowerLabel = label.toLowerCase();
            int labelIndex = lowerText.indexOf(lowerLabel);

            if (labelIndex == -1) return 0.0;

            // Get text after label (next 50 chars should contain the amount)
            int startIdx = labelIndex + label.length();
            int endIdx = Math.min(startIdx + 50, pdfText.length());
            String textAfterLabel = pdfText.substring(startIdx, endIdx);

            // Find amount pattern: $1,234.56 or 1234.56
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$?\\s*([\\d,]+\\.?\\d*)");
            java.util.regex.Matcher matcher = pattern.matcher(textAfterLabel);

            if (matcher.find()) {
                String amountStr = matcher.group(1).replace(",", "");
                return Double.parseDouble(amountStr);
            }
        } catch (Exception e) {
            logger.debug("Failed to extract amount for '{}': {}", label, e.getMessage());
        }
        return 0.0;
    }

    /**
     * Parse amount string to double
     */
    protected double parsePDFAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(amountStr.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Format amount to 2 decimal places
     */
    protected String formatPDFAmount(double amount) {
        return String.format("%.2f", amount);
    }

    /**
     * Format amount with comma separator
     */
    protected String formatPDFAmountWithComma(double amount) {
        return String.format("%,.2f", amount);
    }

    /**
     * Build HTML validation report header with timestamp and status
     */
    protected String buildPDFReportHeader(String title, String pdfPath, boolean passed) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String status = passed ? "PASSED" : "FAILED";
        String statusColor = passed ? "#28a745" : "#dc3545";

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif;'>");
        html.append("<h4 style='margin-bottom: 10px;'>").append(title).append("</h4>");
        html.append("<div style='margin-bottom: 15px; padding: 10px; background-color: #f8f9fa; border-radius: 5px;'>");
        html.append("<table style='width: 100%; border: none;'>");
        html.append("<tr>");
        html.append("<td style='text-align: left;'><strong>Timestamp:</strong> ").append(timestamp).append("</td>");
        html.append("<td style='text-align: right;'><strong>Status:</strong> <span style='color: ").append(statusColor).append("; font-weight: bold;'>").append(status).append("</span></td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("<div style='margin-top: 5px;'><strong>PDF File:</strong> ").append(pdfPath).append("</div>");
        html.append("</div>");
        return html.toString();
    }

    /**
     * Build HTML validation table from validation rows
     */
    protected String buildPDFValidationTable(java.util.List<String[]> validationRows) {
        StringBuilder html = new StringBuilder();
        html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>");
        html.append("<thead>");
        html.append("<tr style='background-color: #343a40; color: white;'>");
        html.append("<th style='padding: 10px; border: 1px solid #dee2e6; text-align: left; width: 25%;'>Field</th>");
        html.append("<th style='padding: 10px; border: 1px solid #dee2e6; text-align: left; width: 30%;'>New Quote Data</th>");
        html.append("<th style='padding: 10px; border: 1px solid #dee2e6; text-align: left; width: 30%;'>PDF Data</th>");
        html.append("<th style='padding: 10px; border: 1px solid #dee2e6; text-align: center; width: 15%;'>Status</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (String[] row : validationRows) {
            String rowBgColor = row[3].equals("PASS") ? "#d4edda" : (row[3].equals("FAIL") ? "#f8d7da" : "#e9ecef");
            String statusBadgeColor = row[3].equals("PASS") ? "#28a745" : (row[3].equals("FAIL") ? "#dc3545" : "#6c757d");

            html.append("<tr style='background-color: ").append(rowBgColor).append(";'>");
            html.append("<td style='padding: 8px; border: 1px solid #dee2e6;'>").append(escapeHtmlChars(row[0])).append("</td>");
            html.append("<td style='padding: 8px; border: 1px solid #dee2e6;'>").append(escapeHtmlChars(row[1])).append("</td>");
            html.append("<td style='padding: 8px; border: 1px solid #dee2e6;'>").append(escapeHtmlChars(row[2])).append("</td>");
            html.append("<td style='padding: 8px; border: 1px solid #dee2e6; text-align: center;'>");
            html.append("<span style='background-color: ").append(statusBadgeColor).append("; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>").append(row[3]).append("</span>");
            html.append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");
        return html.toString();
    }

    /**
     * Build HTML error section
     */
    protected String buildPDFErrorSection(java.util.List<String> errors) {
        if (errors == null || errors.isEmpty()) return "";

        StringBuilder html = new StringBuilder();
        html.append("<div style='margin-top: 15px; padding: 10px; background-color: #f8d7da; border: 1px solid #f5c6cb; border-radius: 5px;'>");
        html.append("<strong style='color: #721c24;'>Errors:</strong>");
        html.append("<ul style='margin: 5px 0; padding-left: 20px; color: #721c24;'>");
        for (String error : errors) {
            html.append("<li>").append(escapeHtmlChars(error)).append("</li>");
        }
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    /**
     * Escape HTML special characters
     */
    protected String escapeHtmlChars(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    /**
     * Build console report for logging
     */
    protected String buildPDFConsoleReport(String title, String pdfPath, java.util.List<String[]> validationRows, boolean passed) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        StringBuilder console = new StringBuilder();
        console.append("\n=== ").append(title).append(" ===\n");
        console.append("Timestamp: ").append(timestamp).append("\n");
        console.append("Status: ").append(passed ? "PASSED" : "FAILED").append("\n");
        console.append("PDF File: ").append(pdfPath).append("\n\n");
        console.append(String.format("%-25s | %-25s | %-25s | %-10s\n", "Field", "New Quote Data", "PDF Data", "Status"));
        console.append("-".repeat(95)).append("\n");

        for (String[] row : validationRows) {
            String truncField = row[0].length() > 25 ? row[0].substring(0, 22) + "..." : row[0];
            String truncQuote = row[1].length() > 25 ? row[1].substring(0, 22) + "..." : row[1];
            String truncPdf = row[2].length() > 25 ? row[2].substring(0, 22) + "..." : row[2];
            console.append(String.format("%-25s | %-25s | %-25s | %-10s\n", truncField, truncQuote, truncPdf, row[3]));
        }
        console.append("-".repeat(95)).append("\n");
        return console.toString();
    }

    /**
     * Inner class for PDF validation results
     */
    public static class PDFValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private String report = "";

        public void addError(String error) {
            errors.add(error);
            valid = false;
        }
        public boolean isValid() { return valid; }
        public java.util.List<String> getErrors() { return errors; }
        public void setReport(String report) { this.report = report; }
        public String getReport() { return report; }
    }

    /**
     * Inner class to store location-specific data for PDF validation
     * Stores Coverage A (Dwelling), B (Structures), C (Personal Prop), D (Rents)
     * TIV = A + B + C + D
     * Premium = (Coverage Value / 100) * Rate
     */
    public static class LocationData {
        private String address;
        private double coverageA;  // Dwelling
        private double coverageB;  // Structures
        private double coverageC;  // Personal Property
        private double coverageD;  // Rents/Loss of Rents
        private double rate;       // Suggested Rate
        private double tiv;        // Total Insurable Value
        private double fullTerm;   // Full Term Premium
        private double wsPremium;  // WS Premium
        private double glPremium;  // GL Premium
        private double tax;        // Tax amount

        // Calculated Premiums: Premium = (Coverage / 100) * Rate
        private double premiumA;
        private double premiumB;
        private double premiumC;
        private double premiumD;
        private double propertyPremium; // Property Premium = (TIV / 100) * Rate

        public LocationData(String address) {
            this.address = address;
        }

        // Setters with fluent interface
        public LocationData setCoverageA(double value) { this.coverageA = value; return this; }
        public LocationData setCoverageB(double value) { this.coverageB = value; return this; }
        public LocationData setCoverageC(double value) { this.coverageC = value; return this; }
        public LocationData setCoverageD(double value) { this.coverageD = value; return this; }
        public LocationData setRate(double value) { this.rate = value; return this; }
        public LocationData setTiv(double value) { this.tiv = value; return this; }
        public LocationData setFullTerm(double value) { this.fullTerm = value; return this; }
        public LocationData setWsPremium(double value) { this.wsPremium = value; return this; }
        public LocationData setGlPremium(double value) { this.glPremium = value; return this; }
        public LocationData setTax(double value) { this.tax = value; return this; }
        public LocationData setPropertyPremium(double value) { this.propertyPremium = value; return this; }

        // Calculate TIV = A + B + C + D
        public void calculateTIV() {
            this.tiv = coverageA + coverageB + coverageC + coverageD;
        }

        // Calculate Property Premium = (TIV / 100) * Rate
        public void calculatePropertyPremium() {
            calculateTIV();
            this.propertyPremium = (tiv / 100) * rate;
        }

        // Calculate Premiums using formula: Premium = (Coverage / 100) * Rate
        public void calculatePremiums() {
            this.premiumA = (coverageA / 100) * rate;
            this.premiumB = (coverageB / 100) * rate;
            this.premiumC = (coverageC / 100) * rate;
            this.premiumD = (coverageD / 100) * rate;
            calculatePropertyPremium();
        }

        // Calculate Full Term = Property Premium + WS + GL + Tax
        public void calculateFullTerm() {
            calculatePropertyPremium();
            this.fullTerm = propertyPremium + wsPremium + glPremium + tax;
        }

        // Getters
        public String getAddress() { return address; }
        public double getCoverageA() { return coverageA; }
        public double getCoverageB() { return coverageB; }
        public double getCoverageC() { return coverageC; }
        public double getCoverageD() { return coverageD; }
        public double getRate() { return rate; }
        public double getTIV() { return tiv; }
        public double getFullTerm() { return fullTerm; }
        public double getPremiumA() { return premiumA; }
        public double getPremiumB() { return premiumB; }
        public double getPremiumC() { return premiumC; }
        public double getPremiumD() { return premiumD; }
        public double getWsPremium() { return wsPremium; }
        public double getGlPremium() { return glPremium; }
        public double getTax() { return tax; }
        public double getPropertyPremium() { return propertyPremium; }

        // Get normalized address for matching (removes extra spaces, converts to lowercase)
        public String getNormalizedAddress() {
            if (address == null) return "";
            return address.toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("street", "st")
                .replaceAll("avenue", "ave")
                .replaceAll("drive", "dr")
                .replaceAll("road", "rd")
                .replaceAll("boulevard", "blvd")
                .replaceAll("lane", "ln")
                .trim();
        }
    }

    // ==================== PDF Location Validation Methods ====================

    /**
     * Validate location data from PDF page 2 against stored location data
     * @param pdfPage2Text Text content from PDF page 2
     * @param locations List of LocationData objects with expected values
     * @param validationRows List to store validation results
     * @param result PDFValidationResult object
     */
    protected void validatePDFLocationData(String pdfPage2Text, java.util.List<LocationData> locations,
                                           java.util.List<String[]> validationRows, PDFValidationResult result) {
        if (locations == null || locations.isEmpty()) {
            logger.info("No location data to validate");
            return;
        }

        // Add section header
        validationRows.add(new String[]{"--- LOCATION DETAILS (PDF Page 2) ---", "", "", "INFO"});

        for (int i = 0; i < locations.size(); i++) {
            LocationData loc = locations.get(i);
            String locLabel = "Location " + (i + 1);

            // Find location in PDF by address
            boolean addressFound = findAddressInPDFText(pdfPage2Text, loc.getAddress());

            // Add location header
            String shortAddr = loc.getAddress();
            if (shortAddr.length() > 35) shortAddr = shortAddr.substring(0, 32) + "...";
            validationRows.add(new String[]{locLabel + ": " + shortAddr, "", "", addressFound ? "PASS" : "FAIL"});

            if (!addressFound) {
                result.addError(locLabel + " address not found in PDF: " + loc.getAddress());
                continue;
            }

            // Validate Coverage A (Dwelling)
            validateLocationAmount(validationRows, result, pdfPage2Text, loc.getAddress(),
                "  Dwelling (Cov A)", loc.getCoverageA());

            // Validate Coverage B (Structures)
            validateLocationAmount(validationRows, result, pdfPage2Text, loc.getAddress(),
                "  Structures (Cov B)", loc.getCoverageB());

            // Validate Coverage C (Personal Prop)
            validateLocationAmount(validationRows, result, pdfPage2Text, loc.getAddress(),
                "  Personal Prop (Cov C)", loc.getCoverageC());

            // Validate Coverage D (Rents)
            validateLocationAmount(validationRows, result, pdfPage2Text, loc.getAddress(),
                "  Rents (Cov D)", loc.getCoverageD());

            // Validate TIV
            loc.calculateTIV();
            validateLocationAmount(validationRows, result, pdfPage2Text, loc.getAddress(),
                "  TIV (A+B+C+D)", loc.getTIV());

            // Validate Full Term Premium if available
            if (loc.getFullTerm() > 0) {
                validateLocationAmount(validationRows, result, pdfPage2Text, loc.getAddress(),
                    "  Full Term Premium", loc.getFullTerm());
            }
        }
    }

    /**
     * Find address in PDF text using normalized matching
     * Handles various address format variations including:
     * - Highway formats: KY-271 vs KY 271 vs Kentucky 271, US-60 vs U.S. 60
     * - Street abbreviations: Street vs St, Avenue vs Ave
     * - Directional variations: Main St vs West Main Street
     * - PO Box variations
     */
    protected boolean findAddressInPDFText(String pdfText, String address) {
        if (pdfText == null || address == null) return false;

        String pdfLower = pdfText.toLowerCase();
        String addrLower = address.toLowerCase();

        // Direct match
        if (pdfLower.contains(addrLower)) return true;

        // Normalize both strings for comparison
        String normalizedPdf = normalizeAddressForMatching(pdfLower);
        String normalizedAddr = normalizeAddressForMatching(addrLower);

        // Try normalized match
        if (normalizedPdf.contains(normalizedAddr)) return true;

        // Try normalized address matching with parts
        String[] addressParts = addrLower.split(",");
        if (addressParts.length > 0) {
            // Match street part with normalization
            String street = normalizeAddressForMatching(addressParts[0].trim());
            if (normalizedPdf.contains(street)) return true;

            // Also try the original street part with basic abbreviations
            String streetBasic = addressParts[0].trim()
                .replaceAll("street", "st")
                .replaceAll("avenue", "ave")
                .replaceAll("drive", "dr")
                .replaceAll("road", "rd")
                .replaceAll("court", "ct")
                .replaceAll("boulevard", "blvd")
                .replaceAll("lane", "ln");
            if (pdfLower.contains(streetBasic)) return true;
        }

        // Try matching just the street number and name
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d+\\s+\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(addrLower);
        if (matcher.find()) {
            String streetStart = matcher.group(1);
            if (pdfLower.contains(streetStart)) return true;
            // Also try normalized
            if (normalizedPdf.contains(normalizeAddressForMatching(streetStart))) return true;
        }

        // SPECIAL: Match highway/route addresses by street number + route number
        // Handles: "80 KY-271" matching "80 Kentucky 271" or "80 KY 271"
        String streetNumber = extractStreetNumber(addrLower);
        String routeNumber = extractRouteNumber(addrLower);
        if (!streetNumber.isEmpty() && !routeNumber.isEmpty()) {
            // Check if PDF contains both the street number and route number nearby
            if (pdfLower.contains(streetNumber) && pdfLower.contains(routeNumber)) {
                // Verify they're close together (same line or nearby)
                String routePattern = streetNumber + "\\s+(?:ky|kentucky|us|u\\.s\\.|highway|hwy|route)\\s*-?\\s*" + routeNumber;
                if (java.util.regex.Pattern.compile(routePattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(pdfLower).find()) {
                    return true;
                }
                // Also try simpler pattern: just number + route number
                String simplePattern = streetNumber + ".*" + routeNumber;
                java.util.regex.Pattern simpleP = java.util.regex.Pattern.compile(simplePattern);
                java.util.regex.Matcher simpleM = simpleP.matcher(pdfLower);
                while (simpleM.find()) {
                    // Make sure match is not too long (max 50 chars between street num and route num)
                    if (simpleM.group().length() < 50) {
                        return true;
                    }
                }
            }
        }

        // SPECIAL: Match street addresses with flexible word matching
        // Handles: "100 Main St" matching "100 West Main Street"
        if (!streetNumber.isEmpty()) {
            // Extract key words from address (excluding numbers and common words)
            String[] words = addressParts[0].trim().split("\\s+");
            for (String word : words) {
                // Skip numbers, short words, and common prefixes/suffixes
                if (word.matches("\\d+") || word.length() < 3) continue;
                if (word.matches("(?i)(st|ave|dr|rd|ct|ln|blvd|pl|cir|street|avenue|drive|road|court|lane|boulevard|place|circle)")) continue;
                if (word.matches("(?i)(north|south|east|west|n|s|e|w)")) continue;

                // If we find street number + this key word in PDF, it's likely a match
                String keyWordPattern = streetNumber + "\\s+.*\\b" + word + "\\b";
                if (java.util.regex.Pattern.compile(keyWordPattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(pdfLower).find()) {
                    return true;
                }
            }
        }

        // Try matching city and zip code combination
        if (addressParts.length >= 2) {
            String cityStateZip = addressParts[addressParts.length - 1].trim();
            // Extract zip code
            java.util.regex.Pattern zipPattern = java.util.regex.Pattern.compile("\\d{5}");
            java.util.regex.Matcher zipMatcher = zipPattern.matcher(cityStateZip);
            if (zipMatcher.find()) {
                String zipCode = zipMatcher.group();
                // Also get city name from second-to-last part
                if (addressParts.length >= 2) {
                    String cityPart = addressParts[addressParts.length - 2].trim();
                    // If both city and zip are found, consider it a match
                    if (pdfLower.contains(cityPart) && pdfLower.contains(zipCode)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Normalize address string for flexible matching
     * Handles highway format variations and common abbreviations
     */
    private String normalizeAddressForMatching(String address) {
        if (address == null) return "";

        String normalized = address.toLowerCase().trim();

        // Normalize state abbreviations to full names and vice versa
        // KY/Kentucky, US/U.S. variations
        normalized = normalized.replaceAll("\\bkentucky\\b", "ky");
        normalized = normalized.replaceAll("\\bu\\.s\\.\\s*", "us ");  // U.S. 60 -> us 60
        normalized = normalized.replaceAll("\\bu\\.s\\s*", "us ");  // U.S 60 -> us 60

        // Normalize highway/route formats: KY-271 -> ky 271, US-60 -> us 60
        normalized = normalized.replaceAll("([a-z]{2})-(\\d+)", "$1 $2");  // KY-271 -> ky 271
        normalized = normalized.replaceAll("([a-z]{2})\\s*-\\s*(\\d+)", "$1 $2");  // KY - 271 -> ky 271
        normalized = normalized.replaceAll("us-(\\d+)", "us $1");  // US-60 -> us 60
        normalized = normalized.replaceAll("us\\s+(\\d+)", "us $1");  // US 60 -> us 60 (normalize spacing)
        normalized = normalized.replaceAll("hwy\\s*-?\\s*(\\d+)", "highway $1");  // hwy-60, hwy 60 -> highway 60
        normalized = normalized.replaceAll("route\\s*-?\\s*(\\d+)", "route $1");  // route-60 -> route 60

        // Normalize common street abbreviations (both directions for matching)
        normalized = normalized.replaceAll("\\bstreet\\b", "st");
        normalized = normalized.replaceAll("\\bavenue\\b", "ave");
        normalized = normalized.replaceAll("\\bdrive\\b", "dr");
        normalized = normalized.replaceAll("\\broad\\b", "rd");
        normalized = normalized.replaceAll("\\bcourt\\b", "ct");
        normalized = normalized.replaceAll("\\bboulevard\\b", "blvd");
        normalized = normalized.replaceAll("\\blane\\b", "ln");
        normalized = normalized.replaceAll("\\bplace\\b", "pl");
        normalized = normalized.replaceAll("\\bcircle\\b", "cir");

        // Remove directional prefixes/suffixes for more flexible matching
        // Keep them but also try without: "West Main" matches "Main"
        // Don't remove here - we'll handle this in the matching logic

        // Normalize PO Box variations
        normalized = normalized.replaceAll("p\\.?o\\.?\\s*box", "po box");
        normalized = normalized.replaceAll("post\\s*office\\s*box", "po box");

        // Remove extra whitespace
        normalized = normalized.replaceAll("\\s+", " ");

        return normalized;
    }

    /**
     * Extract just the street number from an address
     */
    private String extractStreetNumber(String address) {
        if (address == null) return "";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(address.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Extract route/highway number from address (e.g., "271" from "KY-271" or "Kentucky 271")
     */
    private String extractRouteNumber(String address) {
        if (address == null) return "";
        // Match patterns like: KY-271, KY 271, Kentucky 271, US-60, U.S. 60
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:ky|kentucky|us|u\\.s\\.|highway|hwy|route)\\s*-?\\s*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(address);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Validate a specific amount for a location in PDF
     */
    protected void validateLocationAmount(java.util.List<String[]> validationRows, PDFValidationResult result,
                                          String pdfText, String address, String fieldName, double expectedAmount) {
        String formatted = "$" + String.format("%,.2f", expectedAmount);

        if (expectedAmount <= 0) {
            validationRows.add(new String[]{fieldName, "$0.00", "N/A", "INFO"});
            return;
        }

        // Check if amount exists in PDF
        boolean found = amountExistsInPDF(pdfText, expectedAmount);
        String pdfValue = found ? formatted : "Not Found";
        validationRows.add(new String[]{fieldName, formatted, pdfValue, found ? "PASS" : "FAIL"});

        if (!found) {
            result.addError(fieldName + " " + formatted + " not found in PDF for location");
        }
    }

    /**
     * Parse location row from PDF text and extract amounts
     * @param pdfText PDF text content
     * @param address Address to search for
     * @return Map of field names to values, or null if not found
     */
    protected java.util.Map<String, Double> parseLocationRowFromPDF(String pdfText, String address) {
        java.util.Map<String, Double> values = new java.util.HashMap<>();

        try {
            // Find the line containing the address
            String[] lines = pdfText.split("\n");
            for (String line : lines) {
                if (findAddressInPDFText(line, address)) {
                    // Extract all dollar amounts from this line
                    java.util.regex.Pattern amountPattern = java.util.regex.Pattern.compile("\\$([\\d,]+\\.\\d{2})");
                    java.util.regex.Matcher matcher = amountPattern.matcher(line);

                    java.util.List<Double> amounts = new java.util.ArrayList<>();
                    while (matcher.find()) {
                        String amountStr = matcher.group(1).replace(",", "");
                        amounts.add(Double.parseDouble(amountStr));
                    }

                    // Map amounts to fields based on position
                    // Order: Dwelling, Structures, Personal Prop, Rents, TIV, Full Term, Change
                    if (amounts.size() >= 6) {
                        values.put("Dwelling", amounts.get(0));
                        values.put("Structures", amounts.get(1));
                        values.put("PersonalProp", amounts.get(2));
                        values.put("Rents", amounts.get(3));
                        values.put("TIV", amounts.get(4));
                        values.put("FullTerm", amounts.get(5));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.debug("Error parsing location row: {}", e.getMessage());
        }

        return values;
    }

    // Inner class to hold PDF location data extracted from PDF - used for validating TIV and Full Term calculations
    public static class PDFLocationData {
        private String address;
        private double dwelling, additionalStructures, bpp, lossOfRents, rate, pdfTIV, pdfFullTerm;
        private double wsPremium, glPremium, tax; // For Full Term calculation
        public PDFLocationData() {}
        public void setAddress(String address) { this.address = address; }
        public void setDwelling(double dwelling) { this.dwelling = dwelling; }
        public void setAdditionalStructures(double as) { this.additionalStructures = as; }
        public void setBpp(double bpp) { this.bpp = bpp; }
        public void setLossOfRents(double lor) { this.lossOfRents = lor; }
        public void setRate(double rate) { this.rate = rate; }
        public void setPdfTIV(double tiv) { this.pdfTIV = tiv; }
        public void setPdfFullTerm(double fullTerm) { this.pdfFullTerm = fullTerm; }
        public void setWsPremium(double ws) { this.wsPremium = ws; }
        public void setGlPremium(double gl) { this.glPremium = gl; }
        public void setTax(double tax) { this.tax = tax; }
        public String getAddress() { return address; }
        public double getDwelling() { return dwelling; }
        public double getAdditionalStructures() { return additionalStructures; }
        public double getBpp() { return bpp; }
        public double getLossOfRents() { return lossOfRents; }
        public double getRate() { return rate; }
        public double getPdfTIV() { return pdfTIV; }
        public double getPdfFullTerm() { return pdfFullTerm; }
        public double getWsPremium() { return wsPremium; }
        public double getGlPremium() { return glPremium; }
        public double getTax() { return tax; }
        public double calculateExpectedTIV() { return dwelling + additionalStructures + bpp + lossOfRents; }
        public double calculatePropertyPremium() { return (calculateExpectedTIV() / 100.0) * rate; }
        // Full Term = Property Premium + WS + GL + Tax
        public double calculateExpectedFullTerm() { return calculatePropertyPremium() + wsPremium + glPremium + tax; }
        public boolean validateTIV() { return Math.abs(pdfTIV - calculateExpectedTIV()) <= 0.01; }
        public boolean validateFullTerm() { return Math.abs(pdfFullTerm - calculateExpectedFullTerm()) <= 0.01; }
    }

    // Extract location data from PDF text for a specific address (Dwelling, Structures, BPP, LOR, TIV, Full Term)
    protected PDFLocationData extractPDFLocationData(String pdfText, String address, double rate) {
        PDFLocationData data = new PDFLocationData();
        data.setAddress(address);
        data.setRate(rate);
        try {
            String[] lines = pdfText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (findAddressInPDFText(lines[i], address)) {
                    StringBuilder combinedText = new StringBuilder(lines[i]);
                    for (int j = 1; j <= 3 && (i + j) < lines.length; j++) combinedText.append(" ").append(lines[i + j]);
                    java.util.regex.Pattern amountPattern = java.util.regex.Pattern.compile("\\$?([\\d,]+\\.\\d{2})");
                    java.util.regex.Matcher matcher = amountPattern.matcher(combinedText.toString());
                    java.util.List<Double> amounts = new java.util.ArrayList<>();
                    while (matcher.find()) {
                        try { amounts.add(Double.parseDouble(matcher.group(1).replace(",", ""))); } catch (NumberFormatException ignored) {}
                    }
                    logger.debug("Extracted {} amounts from PDF for address: {}", amounts.size(), address);
                    if (amounts.size() >= 6) {
                        data.setDwelling(amounts.get(0)); data.setAdditionalStructures(amounts.get(1));
                        data.setBpp(amounts.get(2)); data.setLossOfRents(amounts.get(3));
                        data.setPdfTIV(amounts.get(4)); data.setPdfFullTerm(amounts.get(5));
                    } else if (amounts.size() >= 4) {
                        data.setDwelling(amounts.get(0)); data.setAdditionalStructures(amounts.get(1));
                        data.setBpp(amounts.get(2)); data.setLossOfRents(amounts.get(3));
                    }
                    break;
                }
            }
        } catch (Exception e) { logger.debug("Error extracting PDF location data: {}", e.getMessage()); }
        return data;
    }

    // Validate PDF location row - TIV = Dwelling + AS + BPP + LOR, Full Term = PropPrem + WS + GL + Tax
    protected void validatePDFLocationCalculations(java.util.List<String[]> validationRows, PDFValidationResult result, PDFLocationData pdfData, int locationNumber) {
        String locPrefix = "  Loc " + locationNumber + ": ";
        double expectedTIV = pdfData.calculateExpectedTIV();
        double propPremium = pdfData.calculatePropertyPremium();
        double expectedFullTerm = pdfData.calculateExpectedFullTerm();

        // Validate TIV = Dwelling + AS + BPP + LOR
        boolean tivValid = Math.abs(pdfData.getPdfTIV() - expectedTIV) <= 0.01;
        validationRows.add(new String[]{locPrefix + "TIV", "$" + formatAmountWithComma(expectedTIV) + " (D+AS+BPP+LOR)", "$" + formatAmountWithComma(pdfData.getPdfTIV()), tivValid ? "PASS" : "FAIL"});
        if (!tivValid) result.addError("Location " + locationNumber + ": TIV mismatch - Expected $" + formatAmountWithComma(expectedTIV) + ", PDF shows $" + formatAmountWithComma(pdfData.getPdfTIV()));

        // Validate Full Term = Property Premium + WS + GL + Tax
        boolean fullTermValid = Math.abs(pdfData.getPdfFullTerm() - expectedFullTerm) <= 0.01;
        String formulaDetail = String.format("$%s+$%s+$%s+$%s", formatAmountWithComma(propPremium), formatAmountWithComma(pdfData.getWsPremium()), formatAmountWithComma(pdfData.getGlPremium()), formatAmountWithComma(pdfData.getTax()));
        validationRows.add(new String[]{locPrefix + "Full Term", "$" + formatAmountWithComma(expectedFullTerm) + " (PropPrem+WS+GL+Tax)", "$" + formatAmountWithComma(pdfData.getPdfFullTerm()), fullTermValid ? "PASS" : "FAIL"});
        if (!fullTermValid) result.addError("Location " + locationNumber + ": Full Term mismatch - Expected $" + formatAmountWithComma(expectedFullTerm) + " (" + formulaDetail + "), PDF shows $" + formatAmountWithComma(pdfData.getPdfFullTerm()));

        logger.info("Location {} validation: TIV={} vs {}, FullTerm={} (PropPrem={} + WS={} + GL={} + Tax={}) vs {}",
            locationNumber, formatAmountWithComma(expectedTIV), formatAmountWithComma(pdfData.getPdfTIV()),
            formatAmountWithComma(expectedFullTerm), formatAmountWithComma(propPremium), formatAmountWithComma(pdfData.getWsPremium()),
            formatAmountWithComma(pdfData.getGlPremium()), formatAmountWithComma(pdfData.getTax()), formatAmountWithComma(pdfData.getPdfFullTerm()));
    }

    // ==================== Display Computation Validation Methods ====================

    // Inner class for Display Computation validation results - tracks status and generates HTML report
    public static class DisplayComputationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String[]> validationRows = new java.util.ArrayList<>();
        private java.util.List<LocationComparisonData> comparisonData = new java.util.ArrayList<>();
        private String htmlReport = "";

        public void addError(String error) {
            errors.add(error);
            valid = false;
        }

        public void addValidationRow(String address, String tivCalc, String tivDisp, String tivStatus,
                                     String premCalc, String premDisp, String premStatus, String overallStatus) {
            validationRows.add(new String[]{address, tivCalc, tivDisp, tivStatus, premCalc, premDisp, premStatus, overallStatus});
        }

        public void addComparisonData(LocationComparisonData data) {
            comparisonData.add(data);
            if (!data.isAllMatch()) {
                valid = false;
            }
        }

        public boolean isValid() { return valid; }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String[]> getValidationRows() { return validationRows; }
        public java.util.List<LocationComparisonData> getComparisonData() { return comparisonData; }
        public void setHtmlReport(String report) { this.htmlReport = report; }
        public String getHtmlReport() { return htmlReport; }
    }

    // Class to store comparison data between Excel (stored) and Frontend (displayed)
    public static class LocationComparisonData {
        private int locationNumber;
        private String address;
        // Excel (stored) values
        private double excelDwelling, excelAS, excelBPP, excelLOR, excelRate;
        // Frontend (displayed) values
        private double frontendDwelling, frontendAS, frontendBPP, frontendLOR, frontendRate, frontendTaxes, frontendTIV, frontendPropertyPremium;
        // Match status for each field
        private boolean dwellingMatch, asMatch, bppMatch, lorMatch, rateMatch;

        public LocationComparisonData(int locationNumber, String address) {
            this.locationNumber = locationNumber;
            this.address = address;
        }

        // Setters for Excel values
        public LocationComparisonData setExcelValues(double dwelling, double as, double bpp, double lor, double rate) {
            this.excelDwelling = dwelling;
            this.excelAS = as;
            this.excelBPP = bpp;
            this.excelLOR = lor;
            this.excelRate = rate;
            return this;
        }

        // Setters for Frontend values
        public LocationComparisonData setFrontendValues(double dwelling, double as, double bpp, double lor, double rate, double taxes, double tiv, double propPrem) {
            this.frontendDwelling = dwelling;
            this.frontendAS = as;
            this.frontendBPP = bpp;
            this.frontendLOR = lor;
            this.frontendRate = rate;
            this.frontendTaxes = taxes;
            this.frontendTIV = tiv;
            this.frontendPropertyPremium = propPrem;
            return this;
        }

        // Validate and set match status
        public LocationComparisonData validate() {
            this.dwellingMatch = Math.abs(excelDwelling - frontendDwelling) <= 0.01;
            this.asMatch = Math.abs(excelAS - frontendAS) <= 0.01;
            this.bppMatch = Math.abs(excelBPP - frontendBPP) <= 0.01;
            this.lorMatch = Math.abs(excelLOR - frontendLOR) <= 0.01;
            this.rateMatch = Math.abs(excelRate - frontendRate) <= 0.0001;
            return this;
        }

        public boolean isAllMatch() { return dwellingMatch && asMatch && bppMatch && lorMatch && rateMatch; }
        public int getLocationNumber() { return locationNumber; }
        public String getAddress() { return address; }
        public double getExcelDwelling() { return excelDwelling; }
        public double getExcelAS() { return excelAS; }
        public double getExcelBPP() { return excelBPP; }
        public double getExcelLOR() { return excelLOR; }
        public double getExcelRate() { return excelRate; }
        public double getFrontendDwelling() { return frontendDwelling; }
        public double getFrontendAS() { return frontendAS; }
        public double getFrontendBPP() { return frontendBPP; }
        public double getFrontendLOR() { return frontendLOR; }
        public double getFrontendRate() { return frontendRate; }
        public double getFrontendTaxes() { return frontendTaxes; }
        public double getFrontendTIV() { return frontendTIV; }
        public double getFrontendPropertyPremium() { return frontendPropertyPremium; }
        public boolean isDwellingMatch() { return dwellingMatch; }
        public boolean isAsMatch() { return asMatch; }
        public boolean isBppMatch() { return bppMatch; }
        public boolean isLorMatch() { return lorMatch; }
        public boolean isRateMatch() { return rateMatch; }
    }

    // Inner class to store location row data extracted from frontend table
    public static class LocationRowData {
        private int locationNumber;
        private String address;
        private double dwelling, additionalStructures, bpp, lossOfRents, rate, tiv, propertyPremium, taxes;
        private double wsPremium, glPremium, fullTerm, fees;
        public LocationRowData(int locationNumber) { this.locationNumber = locationNumber; }
        public LocationRowData setAddress(String value) { this.address = value; return this; }
        public LocationRowData setDwelling(double value) { this.dwelling = value; return this; }
        public LocationRowData setAdditionalStructures(double value) { this.additionalStructures = value; return this; }
        public LocationRowData setBpp(double value) { this.bpp = value; return this; }
        public LocationRowData setLossOfRents(double value) { this.lossOfRents = value; return this; }
        public LocationRowData setRate(double value) { this.rate = value; return this; }
        public LocationRowData setTiv(double value) { this.tiv = value; return this; }
        public LocationRowData setPropertyPremium(double value) { this.propertyPremium = value; return this; }
        public LocationRowData setTaxes(double value) { this.taxes = value; return this; }
        public LocationRowData setWsPremium(double value) { this.wsPremium = value; return this; }
        public LocationRowData setGlPremium(double value) { this.glPremium = value; return this; }
        public LocationRowData setFullTerm(double value) { this.fullTerm = value; return this; }
        public LocationRowData setFees(double value) { this.fees = value; return this; }
        public int getLocationNumber() { return locationNumber; }
        public String getAddress() { return address; }
        public double getDwelling() { return dwelling; }
        public double getAdditionalStructures() { return additionalStructures; }
        public double getBpp() { return bpp; }
        public double getLossOfRents() { return lossOfRents; }
        public double getRate() { return rate; }
        public double getTiv() { return tiv; }
        public double getPropertyPremium() { return propertyPremium; }
        public double getTaxes() { return taxes; }
        public double getWsPremium() { return wsPremium; }
        public double getGlPremium() { return glPremium; }
        public double getFullTerm() { return fullTerm; }
        public double getFees() { return fees; }
        public double calculateExpectedTIV() { return dwelling + additionalStructures + bpp + lossOfRents; }
        public double calculateExpectedPropertyPremium() { return (calculateExpectedTIV() / 100.0) * rate; }
        // Full Term = Property Premium + WS + GL + Tax
        public double calculateExpectedFullTerm() { return calculateExpectedPropertyPremium() + wsPremium + glPremium + taxes; }
        public boolean validateTIV() { return Math.abs(tiv - calculateExpectedTIV()) <= 0.01; }
        public boolean validatePropertyPremium() { return Math.abs(propertyPremium - calculateExpectedPropertyPremium()) <= 0.01; }
        public boolean validateFullTerm() { return Math.abs(fullTerm - calculateExpectedFullTerm()) <= 0.01; }
    }

    // Round value to 2 decimal places
    protected double roundTo2Decimals(double value) { return Math.round(value * 100.0) / 100.0; }

    // Format amount with 2 decimal places
    protected String formatAmount(double value) { return String.format("%.2f", value); }

    // Format amount with comma separator and 2 decimal places
    protected String formatAmountWithComma(double value) { return String.format("%,.2f", value); }

    // Parse amount string to double, handling various formats
    protected double parseAmountValue(String text) {
        if (text == null || text.isEmpty()) return 0.0;
        try { return Double.parseDouble(text.replaceAll("[^0-9.\\-]", "")); } catch (NumberFormatException e) { return 0.0; }
    }

    // Validate computation row - TIV = D+AS+BPP+LOR, Property Premium = (TIV/100)*Rate
    protected void validateComputationRow(DisplayComputationResult result, LocationRowData rowData) {
        int locNum = rowData.getLocationNumber();
        String address = rowData.getAddress();
        double dwelling = rowData.getDwelling();
        double addStruct = rowData.getAdditionalStructures();
        double bpp = rowData.getBpp();
        double lor = rowData.getLossOfRents();
        double rate = rowData.getRate();
        double displayedTIV = rowData.getTiv();
        double displayedPremium = rowData.getPropertyPremium();

        // Calculate expected values using formulas
        double calculatedTIV = dwelling + addStruct + bpp + lor;
        double calculatedPremium = (calculatedTIV / 100.0) * rate;

        // Validate: calculated vs displayed
        boolean tivMatch = Math.abs(displayedTIV - calculatedTIV) <= 0.01;
        boolean premiumMatch = Math.abs(displayedPremium - calculatedPremium) <= 0.01;
        String status = (tivMatch && premiumMatch) ? "PASS" : "FAIL";

        // Log detailed validation for each location
        logger.info("--- Location {} Validation ---", locNum);
        logger.info("Address: {}", address);
        logger.info("TIV: ${}+${}+${}+${} = ${} (Calculated) vs ${} (Displayed) - {}",
            formatAmountWithComma(dwelling), formatAmountWithComma(addStruct), formatAmountWithComma(bpp), formatAmountWithComma(lor),
            formatAmountWithComma(calculatedTIV), formatAmountWithComma(displayedTIV), tivMatch ? "MATCH" : "MISMATCH");
        logger.info("Property Premium: (${}/100)*{} = ${} (Calculated) vs ${} (Displayed) - {}",
            formatAmountWithComma(calculatedTIV), formatAmount(rate),
            formatAmountWithComma(calculatedPremium), formatAmountWithComma(displayedPremium), premiumMatch ? "MATCH" : "MISMATCH");
        logger.info("Location {} Status: {}", locNum, status);

        // Build TIV display string: Calculated | Displayed | Status
        String tivCalcStr = String.format("$%s+$%s+$%s+$%s = $%s", formatAmountWithComma(dwelling), formatAmountWithComma(addStruct), formatAmountWithComma(bpp), formatAmountWithComma(lor), formatAmountWithComma(calculatedTIV));
        String tivStatus = tivMatch ? "PASS" : "FAIL";

        // Build Property Premium display string: Calculated | Displayed | Status
        String premCalcStr = String.format("($%s/100)*%s = $%s", formatAmountWithComma(calculatedTIV), formatAmount(rate), formatAmountWithComma(calculatedPremium));
        String premStatus = premiumMatch ? "PASS" : "FAIL";

        // Add row: Address, TIV Calculated, TIV Displayed, TIV Status, PropPrem Calculated, PropPrem Displayed, PropPrem Status, Overall Status
        result.addValidationRow(
            address != null ? address : "N/A",
            tivCalcStr,
            "$" + formatAmountWithComma(displayedTIV),
            tivStatus,
            premCalcStr,
            "$" + formatAmountWithComma(displayedPremium),
            premStatus,
            status
        );

        if (!tivMatch) {
            result.addError("Location " + locNum + ": TIV mismatch - Calculated $" + formatAmountWithComma(calculatedTIV) + ", Displayed $" + formatAmountWithComma(displayedTIV));
        }
        if (!premiumMatch) {
            result.addError("Location " + locNum + ": Property Premium mismatch - Calculated $" + formatAmountWithComma(calculatedPremium) + ", Displayed $" + formatAmountWithComma(displayedPremium));
        }
    }

    // Build HTML report for Display Computation validation - compares Excel vs Frontend values
    protected String buildDisplayComputationReport(DisplayComputationResult result) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String status = result.isValid() ? "PASSED" : "FAILED";
        String statusColor = result.isValid() ? "#155724" : "#721c24";
        String statusBg = result.isValid() ? "#d4edda" : "#f8d7da";

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family:Arial,sans-serif;'>");

        // Title section
        html.append("<div style='background-color:#343a40; color:#ffffff; padding:12px 15px; border-radius:5px 5px 0 0;'>");
        html.append("<span style='font-size:16px; font-weight:bold;'>Display Computation Validation</span>");
        html.append("<span style='float:right; font-size:13px;'>").append(timestamp).append("</span>");
        html.append("</div>");

        // Status banner
        html.append("<div style='background-color:").append(statusBg).append("; padding:10px 15px; border:1px solid #000;'>");
        html.append("<b>Overall Status:</b> <span style='color:").append(statusColor).append("; font-weight:bold;'>").append(status).append("</span>");
        html.append(" | <b>Validation:</b> Excel Sheet Values vs Display Computation Values");
        html.append("</div>");

        // Check if we have comparison data
        java.util.List<LocationComparisonData> comparisons = result.getComparisonData();
        if (comparisons != null && !comparisons.isEmpty()) {
            // New comparison table format
            html.append("<table style='width:100%; border-collapse:collapse; font-size:12px; margin-top:10px; color:#000000;'>");

            // Header row
            html.append("<tr style='background-color:#495057; color:#ffffff;'>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:left;'>Location</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:center;'>Field</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:right;'>Excel (Expected)</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:right;'>Frontend (Displayed)</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:center;'>Status</th>");
            html.append("</tr>");

            int rowIdx = 0;
            for (LocationComparisonData data : comparisons) {
                String locLabel = "Location " + data.getLocationNumber();
                String addrShort = data.getAddress() != null && data.getAddress().length() > 30
                    ? data.getAddress().substring(0, 30) + "..." : (data.getAddress() != null ? data.getAddress() : "N/A");

                // Dwelling row
                String bgDwelling = data.isDwellingMatch() ? "#d4edda" : "#f8d7da";
                String statusDwelling = data.isDwellingMatch() ? "<span style='color:#155724;'>&#10004; PASS</span>" : "<span style='color:#721c24;'>&#10008; FAIL</span>";
                html.append("<tr style='background-color:").append(bgDwelling).append("; color:#000000;'>");
                html.append("<td rowspan='5' style='padding:8px; border:1px solid #000; vertical-align:top; color:#000000;'><b>").append(locLabel).append("</b><br/><small>").append(escapeHtmlChars(addrShort)).append("</small></td>");
                html.append("<td style='padding:6px; border:1px solid #000; color:#000000;'>Dwelling (Coverage A)</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getExcelDwelling())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendDwelling())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:center;'>").append(statusDwelling).append("</td>");
                html.append("</tr>");

                // Additional Structures row
                String bgAS = data.isAsMatch() ? "#d4edda" : "#f8d7da";
                String statusAS = data.isAsMatch() ? "<span style='color:#155724;'>&#10004; PASS</span>" : "<span style='color:#721c24;'>&#10008; FAIL</span>";
                html.append("<tr style='background-color:").append(bgAS).append("; color:#000000;'>");
                html.append("<td style='padding:6px; border:1px solid #000; color:#000000;'>Additional Structures (Coverage B)</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getExcelAS())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendAS())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:center;'>").append(statusAS).append("</td>");
                html.append("</tr>");

                // BPP row
                String bgBPP = data.isBppMatch() ? "#d4edda" : "#f8d7da";
                String statusBPP = data.isBppMatch() ? "<span style='color:#155724;'>&#10004; PASS</span>" : "<span style='color:#721c24;'>&#10008; FAIL</span>";
                html.append("<tr style='background-color:").append(bgBPP).append("; color:#000000;'>");
                html.append("<td style='padding:6px; border:1px solid #000; color:#000000;'>BPP (Coverage C)</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getExcelBPP())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendBPP())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:center;'>").append(statusBPP).append("</td>");
                html.append("</tr>");

                // Loss Of Rents row
                String bgLOR = data.isLorMatch() ? "#d4edda" : "#f8d7da";
                String statusLOR = data.isLorMatch() ? "<span style='color:#155724;'>&#10004; PASS</span>" : "<span style='color:#721c24;'>&#10008; FAIL</span>";
                html.append("<tr style='background-color:").append(bgLOR).append("; color:#000000;'>");
                html.append("<td style='padding:6px; border:1px solid #000; color:#000000;'>Loss Of Rents (Coverage D)</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getExcelLOR())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendLOR())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:center;'>").append(statusLOR).append("</td>");
                html.append("</tr>");

                // Rate row
                String bgRate = data.isRateMatch() ? "#d4edda" : "#f8d7da";
                String statusRate = data.isRateMatch() ? "<span style='color:#155724;'>&#10004; PASS</span>" : "<span style='color:#721c24;'>&#10008; FAIL</span>";
                html.append("<tr style='background-color:").append(bgRate).append("; color:#000000;'>");
                html.append("<td style='padding:6px; border:1px solid #000; color:#000000;'>Rate (Suggested Rate)</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>").append(formatAmount(data.getExcelRate())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>").append(formatAmount(data.getFrontendRate())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:center;'>").append(statusRate).append("</td>");
                html.append("</tr>");

                // Add separator row between locations
                if (rowIdx < comparisons.size() - 1) {
                    html.append("<tr><td colspan='5' style='background-color:#dee2e6; height:3px; padding:0;'></td></tr>");
                }
                rowIdx++;
            }

            html.append("</table>");

            // Summary table with computed values (TIV, Taxes, Property Premium)
            html.append("<div style='margin-top:15px;'>");
            html.append("<div style='background-color:#17a2b8; color:#ffffff; padding:8px 12px; font-weight:bold;'>Computed Values (Display Computation)</div>");
            html.append("<table style='width:100%; border-collapse:collapse; font-size:12px; color:#000000;'>");
            html.append("<tr style='background-color:#495057; color:#ffffff;'>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:left;'>Location</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:right;'>TIV</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:right;'>Taxes</th>");
            html.append("<th style='padding:8px; border:1px solid #000; text-align:right;'>Property Premium</th>");
            html.append("</tr>");

            for (LocationComparisonData data : comparisons) {
                String bgRow = data.isAllMatch() ? "#ffffff" : "#fff3cd";
                html.append("<tr style='background-color:").append(bgRow).append("; color:#000000;'>");
                html.append("<td style='padding:6px; border:1px solid #000; color:#000000;'>Location ").append(data.getLocationNumber()).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendTIV())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendTaxes())).append("</td>");
                html.append("<td style='padding:6px; border:1px solid #000; text-align:right; color:#000000;'>$").append(formatAmountWithComma(data.getFrontendPropertyPremium())).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</div>");
        }

        // Errors section if any
        if (!result.getErrors().isEmpty()) {
            html.append("<div style='margin-top:15px; padding:10px; background-color:#f8d7da; border:1px solid #f5c6cb; border-radius:5px;'>");
            html.append("<strong style='color:#721c24;'>Validation Errors:</strong>");
            html.append("<ul style='margin:5px 0; padding-left:20px; color:#721c24;'>");
            for (String error : result.getErrors()) {
                html.append("<li>").append(escapeHtmlChars(error)).append("</li>");
            }
            html.append("</ul>");
            html.append("</div>");
        }

        html.append("</div>");
        return html.toString();
    }

    // Build console log for Display Computation validation
    protected String buildDisplayComputationConsoleLog(DisplayComputationResult result) {
        StringBuilder console = new StringBuilder();
        console.append("\n=== Display Computation Validation ===\n");
        console.append("Formulas: TIV = Dwelling + AS + BPP + Loss Of Rents | Property Premium = (TIV / 100) * Rate\n");
        console.append("Status: ").append(result.isValid() ? "PASSED" : "FAILED").append("\n\n");

        console.append(String.format("%-50s | %-15s | %-15s | %-6s | %-15s | %-15s | %-6s | %-6s\n",
            "Address", "TIV Calculated", "TIV Displayed", "Match", "Prem Calculated", "Prem Displayed", "Match", "Status"));
        console.append("-".repeat(145)).append("\n");

        for (String[] row : result.getValidationRows()) {
            // row: Address[0], TIV Calc[1], TIV Disp[2], TIV Status[3], Prem Calc[4], Prem Disp[5], Prem Status[6], Overall[7]
            String addr = row[0].length() > 50 ? row[0].substring(0, 47) + "..." : row[0];
            // Extract just the result amount from calculation string for console
            String tivCalc = row[1].contains("=") ? row[1].substring(row[1].lastIndexOf("=") + 1).trim() : row[1];
            String premCalc = row[4].contains("=") ? row[4].substring(row[4].lastIndexOf("=") + 1).trim() : row[4];
            console.append(String.format("%-50s | %-15s | %-15s | %-6s | %-15s | %-15s | %-6s | %-6s\n",
                addr, tivCalc, row[2], row[3], premCalc, row[5], row[6], row[7]));
        }

        if (!result.getErrors().isEmpty()) {
            console.append("\nErrors:\n");
            for (String error : result.getErrors()) {
                console.append("  - ").append(error).append("\n");
            }
        }

        return console.toString();
    }
}
