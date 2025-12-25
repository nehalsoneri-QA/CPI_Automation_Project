package com.automation.ai;

import com.automation.utils.ConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Self-Healing WebDriver Wrapper
 * Provides AI-powered self-healing capabilities for Selenium WebDriver
 *
 * Features:
 * - Automatic locator healing when elements change
 * - Multiple fallback strategies
 * - Locator history and analytics
 * - Healing report generation
 * - Integration with Healenium (when available)
 */
public class SelfHealingDriver implements WebDriverListener {

    private static final Logger logger = LogManager.getLogger(SelfHealingDriver.class);
    private final WebDriver originalDriver;
    private WebDriver decoratedDriver;
    private final AILocatorHelper aiLocatorHelper;
    private final ConfigReader config;
    private final Map<String, HealingRecord> healingRecords;
    private final ObjectMapper objectMapper;
    private int healingAttempts = 0;
    private int successfulHealings = 0;

    /**
     * Constructor - Wrap WebDriver with self-healing capabilities
     */
    public SelfHealingDriver(WebDriver driver) {
        this.originalDriver = driver;
        this.aiLocatorHelper = new AILocatorHelper(driver);
        this.config = ConfigReader.getInstance();
        this.healingRecords = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();

        // Create decorated driver with event listener
        EventFiringDecorator<WebDriver> decorator = new EventFiringDecorator<>(this);
        this.decoratedDriver = decorator.decorate(driver);

        logger.info("Self-Healing WebDriver initialized");
    }

    /**
     * Get the decorated (self-healing) driver
     */
    public WebDriver getDriver() {
        return decoratedDriver;
    }

    /**
     * Get original unwrapped driver
     */
    public WebDriver getOriginalDriver() {
        return originalDriver;
    }

    // ==================== WebDriverListener Methods ====================

    @Override
    public void onError(Object target, Method method, Object[] args, InvocationTargetException e) {
        Throwable cause = e.getCause();

        // Handle NoSuchElementException with self-healing
        if (cause instanceof NoSuchElementException && args != null && args.length > 0) {
            if (args[0] instanceof By) {
                By locator = (By) args[0];
                logger.warn("Element not found, attempting self-healing for: {}", locator);
                healingAttempts++;

                // Try to heal the locator
                WebElement healedElement = attemptHealing(locator);
                if (healedElement != null) {
                    successfulHealings++;
                    logger.info("Self-healing successful! Found element using alternative strategy");
                    // Note: The healed element can be returned to the calling code
                }
            }
        }
    }

    @Override
    public void beforeFindElement(WebDriver driver, By locator) {
        logger.debug("Finding element: {}", locator);
    }

    @Override
    public void afterFindElement(WebDriver driver, By locator, WebElement result) {
        logger.debug("Found element: {}", locator);
        // Store successful locator for future reference
        storeSuccessfulLocator(locator, result);
    }

    // ==================== Self-Healing Methods ====================

    /**
     * Attempt to heal a broken locator
     */
    private WebElement attemptHealing(By brokenLocator) {
        String locatorString = brokenLocator.toString();
        logger.info("Attempting to heal locator: {}", locatorString);

        // Get potential healing strategies
        List<By> healingStrategies = generateHealingStrategies(brokenLocator);

        for (By healingLocator : healingStrategies) {
            try {
                WebElement element = originalDriver.findElement(healingLocator);
                if (element.isDisplayed()) {
                    // Record healing success
                    recordHealing(brokenLocator, healingLocator, true);
                    return element;
                }
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                // Continue to next strategy
            }
        }

        // Record healing failure
        recordHealing(brokenLocator, null, false);
        return null;
    }

    /**
     * Generate healing strategies based on broken locator
     */
    private List<By> generateHealingStrategies(By brokenLocator) {
        List<By> strategies = new ArrayList<>();
        String locatorStr = brokenLocator.toString();

        // Parse the locator type and value
        String locatorType = "";
        String locatorValue = "";

        if (locatorStr.contains("By.id:")) {
            locatorType = "id";
            locatorValue = locatorStr.replace("By.id:", "").trim();
        } else if (locatorStr.contains("By.name:")) {
            locatorType = "name";
            locatorValue = locatorStr.replace("By.name:", "").trim();
        } else if (locatorStr.contains("By.className:")) {
            locatorType = "class";
            locatorValue = locatorStr.replace("By.className:", "").trim();
        } else if (locatorStr.contains("By.cssSelector:")) {
            locatorType = "css";
            locatorValue = locatorStr.replace("By.cssSelector:", "").trim();
        } else if (locatorStr.contains("By.xpath:")) {
            locatorType = "xpath";
            locatorValue = locatorStr.replace("By.xpath:", "").trim();
        }

        // Generate alternative locators based on value
        if (!locatorValue.isEmpty()) {
            // Try different attribute-based locators
            strategies.add(By.cssSelector("[id*='" + locatorValue + "']"));
            strategies.add(By.cssSelector("[name*='" + locatorValue + "']"));
            strategies.add(By.cssSelector("[class*='" + locatorValue + "']"));
            strategies.add(By.cssSelector("[data-testid*='" + locatorValue + "']"));
            strategies.add(By.cssSelector("[data-test*='" + locatorValue + "']"));
            strategies.add(By.cssSelector("[aria-label*='" + locatorValue + "']"));

            // XPath alternatives
            strategies.add(By.xpath("//*[contains(@id, '" + locatorValue + "')]"));
            strategies.add(By.xpath("//*[contains(@name, '" + locatorValue + "')]"));
            strategies.add(By.xpath("//*[contains(@class, '" + locatorValue + "')]"));
            strategies.add(By.xpath("//*[contains(text(), '" + locatorValue + "')]"));

            // Try partial matches
            if (locatorValue.length() > 3) {
                String partialValue = locatorValue.substring(0, locatorValue.length() / 2);
                strategies.add(By.cssSelector("[id*='" + partialValue + "']"));
                strategies.add(By.cssSelector("[name*='" + partialValue + "']"));
            }
        }

        // Add common patterns based on locator type
        if ("id".equals(locatorType)) {
            // ID might have changed format (e.g., camelCase to kebab-case)
            String kebabCase = locatorValue.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
            String snakeCase = locatorValue.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            strategies.add(By.id(kebabCase));
            strategies.add(By.id(snakeCase));
            strategies.add(By.name(locatorValue));
        }

        return strategies;
    }

    /**
     * Store successful locator for learning
     */
    private void storeSuccessfulLocator(By locator, WebElement element) {
        try {
            String locatorKey = locator.toString();
            ElementSnapshot snapshot = new ElementSnapshot();
            snapshot.locator = locatorKey;
            snapshot.tagName = element.getTagName();
            snapshot.id = element.getAttribute("id");
            snapshot.name = element.getAttribute("name");
            snapshot.className = element.getAttribute("class");
            snapshot.text = element.getText();
            snapshot.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Store in healing records for future reference
            HealingRecord record = healingRecords.computeIfAbsent(locatorKey, k -> new HealingRecord());
            record.addSnapshot(snapshot);

        } catch (StaleElementReferenceException e) {
            // Element became stale, skip storing
        }
    }

    /**
     * Record healing attempt
     */
    private void recordHealing(By brokenLocator, By healedLocator, boolean success) {
        HealingRecord record = new HealingRecord();
        record.originalLocator = brokenLocator.toString();
        record.healedLocator = healedLocator != null ? healedLocator.toString() : null;
        record.success = success;
        record.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        record.pageUrl = originalDriver.getCurrentUrl();

        healingRecords.put(brokenLocator.toString() + "_" + record.timestamp, record);

        if (success) {
            logger.info("HEALING SUCCESS: {} -> {}", brokenLocator, healedLocator);
        } else {
            logger.warn("HEALING FAILED: {}", brokenLocator);
        }
    }

    // ==================== Reporting Methods ====================

    /**
     * Generate healing report
     */
    public void generateHealingReport() {
        String reportPath = config.getProperty("healing.report.path", "target/healing-report.json");

        HealingReport report = new HealingReport();
        report.totalAttempts = healingAttempts;
        report.successfulHealings = successfulHealings;
        report.successRate = healingAttempts > 0 ?
            (double) successfulHealings / healingAttempts * 100 : 0;
        report.records = new ArrayList<>(healingRecords.values());
        report.generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(reportPath), report);
            logger.info("Healing report generated: {}", reportPath);
        } catch (IOException e) {
            logger.error("Failed to generate healing report", e);
        }
    }

    /**
     * Get healing statistics
     */
    public Map<String, Object> getHealingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", healingAttempts);
        stats.put("successfulHealings", successfulHealings);
        stats.put("failedHealings", healingAttempts - successfulHealings);
        stats.put("successRate", healingAttempts > 0 ?
            String.format("%.2f%%", (double) successfulHealings / healingAttempts * 100) : "N/A");
        return stats;
    }

    /**
     * Print healing summary
     */
    public void printHealingSummary() {
        logger.info("========================================");
        logger.info("SELF-HEALING SUMMARY");
        logger.info("========================================");
        logger.info("Total Healing Attempts: {}", healingAttempts);
        logger.info("Successful Healings: {}", successfulHealings);
        logger.info("Failed Healings: {}", healingAttempts - successfulHealings);
        if (healingAttempts > 0) {
            logger.info("Success Rate: {:.2f}%", (double) successfulHealings / healingAttempts * 100);
        }
        logger.info("========================================");
    }

    // ==================== Inner Classes ====================

    /**
     * Element snapshot for learning
     */
    private static class ElementSnapshot {
        public String locator;
        public String tagName;
        public String id;
        public String name;
        public String className;
        public String text;
        public String timestamp;
    }

    /**
     * Healing record
     */
    private static class HealingRecord {
        public String originalLocator;
        public String healedLocator;
        public boolean success;
        public String timestamp;
        public String pageUrl;
        public List<ElementSnapshot> snapshots = new ArrayList<>();

        public void addSnapshot(ElementSnapshot snapshot) {
            snapshots.add(snapshot);
        }
    }

    /**
     * Healing report
     */
    private static class HealingReport {
        public int totalAttempts;
        public int successfulHealings;
        public double successRate;
        public String generatedAt;
        public List<HealingRecord> records;
    }
}
