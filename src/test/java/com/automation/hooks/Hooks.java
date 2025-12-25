package com.automation.hooks;

import com.automation.utils.ConfigReader;
import com.automation.utils.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cucumber Hooks - Setup and teardown for scenarios
 * Provides before/after hooks for test execution lifecycle
 */
public class Hooks {

    private static final Logger logger = LogManager.getLogger(Hooks.class);
    private final TestContext testContext;
    private final ConfigReader config;
    private static int scenarioCount = 0;
    private static int passedCount = 0;
    private static int failedCount = 0;

    /**
     * Constructor - Dependency injection
     */
    public Hooks(TestContext testContext) {
        this.testContext = testContext;
        this.config = ConfigReader.getInstance();
    }

    // ==================== Before Hooks ====================

    /**
     * Setup before all scenarios - runs once per feature
     */
    @Before(order = 0)
    public void beforeAll() {
        logger.info("========================================");
        logger.info("Starting Test Execution");
        logger.info("Timestamp: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.info("========================================");
    }

    /**
     * Setup WebDriver before each scenario
     */
    @Before(order = 1)
    public void setUp(Scenario scenario) {
        scenarioCount++;
        logger.info("----------------------------------------");
        logger.info("Starting Scenario #{}: {}", scenarioCount, scenario.getName());
        logger.info("Tags: {}", scenario.getSourceTagNames());
        logger.info("----------------------------------------");

        // Initialize WebDriver
        testContext.initializeDriver();

        // Store scenario in context for use in steps
        testContext.setScenarioContext("currentScenario", scenario);
    }

    /**
     * Conditional setup for specific tags
     */
    @Before(value = "@AIHealing", order = 2)
    public void setUpAIHealing(Scenario scenario) {
        logger.info("AI Healing enabled for scenario: {}", scenario.getName());
        config.setProperty("healenium.enabled", "true");
    }

    /**
     * Setup for smoke tests
     */
    @Before(value = "@Smoke", order = 2)
    public void setUpSmokeTest(Scenario scenario) {
        logger.info("Running Smoke Test: {}", scenario.getName());
    }

    /**
     * Setup for regression tests
     */
    @Before(value = "@Regression", order = 2)
    public void setUpRegressionTest(Scenario scenario) {
        logger.info("Running Regression Test: {}", scenario.getName());
    }

    // ==================== Before Step Hooks ====================

    /**
     * Log before each step
     */
    @BeforeStep
    public void beforeStep(Scenario scenario) {
        // Can add step-level logging or actions here
    }

    // ==================== After Step Hooks ====================

    /**
     * Capture screenshot after each failed step (optional)
     */
    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            captureScreenshot(scenario, "step_failure");
        }
    }

    // ==================== After Hooks ====================

    /**
     * Teardown after each scenario
     */
    @After(order = 1)
    public void tearDown(Scenario scenario) {
        logger.info("----------------------------------------");
        logger.info("Finishing Scenario: {}", scenario.getName());
        logger.info("Status: {}", scenario.getStatus());
        logger.info("----------------------------------------");

        // Update counts
        if (scenario.isFailed()) {
            failedCount++;
            logger.error("Scenario FAILED: {}", scenario.getName());
            captureScreenshot(scenario, "scenario_failure");
        } else {
            passedCount++;
            logger.info("Scenario PASSED: {}", scenario.getName());

            // Optionally capture screenshot on pass
            if (config.getBooleanProperty("screenshot.on.pass", false)) {
                captureScreenshot(scenario, "scenario_pass");
            }
        }

        // Clear scenario context
        testContext.clearScenarioContext();
    }

    /**
     * Close WebDriver after each scenario
     */
    @After(order = 0)
    public void closeDriver() {
        testContext.closeDriver();
    }

    /**
     * Final cleanup after all scenarios
     */
    @After(order = Integer.MIN_VALUE)
    public void afterAll(Scenario scenario) {
        logger.info("========================================");
        logger.info("Test Execution Summary");
        logger.info("Total Scenarios: {}", scenarioCount);
        logger.info("Passed: {}", passedCount);
        logger.info("Failed: {}", failedCount);
        logger.info("Pass Rate: {}%", (scenarioCount > 0) ?
            String.format("%.2f", (passedCount * 100.0 / scenarioCount)) : "N/A");
        logger.info("========================================");
    }

    // ==================== Helper Methods ====================

    /**
     * Capture screenshot and attach to scenario report
     */
    private void captureScreenshot(Scenario scenario, String prefix) {
        try {
            WebDriver driver = testContext.getDriver();
            if (driver != null) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String screenshotName = prefix + "_" +
                    scenario.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                scenario.attach(screenshot, "image/png", screenshotName);
                logger.info("Screenshot captured: {}", screenshotName);
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    /**
     * Get current scenario from context
     */
    public Scenario getCurrentScenario() {
        return testContext.getScenarioContext("currentScenario", Scenario.class);
    }

    /**
     * Log test data for debugging
     */
    private void logTestData(Scenario scenario) {
        logger.debug("Scenario ID: {}", scenario.getId());
        logger.debug("Scenario Line: {}", scenario.getLine());
        logger.debug("Scenario URI: {}", scenario.getUri());
    }
}
