package com.automation.listeners;

import com.automation.base.BaseTest;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * TestNG Test Listener - Handles test lifecycle events
 * Provides Extent Reports integration and screenshot capture
 * Implements ISuiteListener to properly initialize/flush reports for entire suite
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static final String REPORT_PATH = "target/reports/";
    private static final String SCREENSHOT_PATH = "target/screenshots/";
    private static boolean isInitialized = false;
    private static String reportFilePath;

    // Called when entire suite starts (before any <test>)
    @Override
    public void onStart(ISuite suite) {
        logger.info("========================================");
        logger.info("SUITE STARTED: {}", suite.getName());
        logger.info("Start Time: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.info("========================================");

        // Initialize Extent Reports only once for entire suite
        if (!isInitialized) {
            createDirectories();
            initializeExtentReports(suite.getName());
            isInitialized = true;
        }
    }

    // Called when entire suite finishes (after all <test>)
    @Override
    public void onFinish(ISuite suite) {
        logger.info("========================================");
        logger.info("SUITE FINISHED: {}", suite.getName());
        logger.info("End Time: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.info("========================================");

        // Flush Extent Reports at the end of entire suite
        if (extent != null) {
            extent.flush();
            logger.info("Extent Report generated at: {}", reportFilePath);
        }

        // Reset for next run
        isInitialized = false;
    }

    // Called when each <test> element starts
    @Override
    public void onStart(ITestContext context) {
        logger.info("----------------------------------------");
        logger.info("Test Started: {}", context.getName());
        logger.info("----------------------------------------");
    }

    // Called when each <test> element finishes
    @Override
    public void onFinish(ITestContext context) {
        logger.info("----------------------------------------");
        logger.info("Test Finished: {}", context.getName());
        logger.info("Passed: {}", context.getPassedTests().size());
        logger.info("Failed: {}", context.getFailedTests().size());
        logger.info("Skipped: {}", context.getSkippedTests().size());
        logger.info("----------------------------------------");

        // Flush after each test section to save progress
        if (extent != null) {
            extent.flush();
        }
    }

    // Called before each test method starts
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();

        logger.info("----------------------------------------");
        logger.info("Starting Test: {}", testName);
        logger.info("Class: {}", className);
        logger.info("----------------------------------------");

        // Create Extent Test
        ExtentTest extentTest = extent.createTest(testName)
            .assignCategory(getCategories(result))
            .assignAuthor("Automation Framework");

        test.set(extentTest);
    }

    // Called when test method passes
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("TEST PASSED: {}", testName);
        logger.info("Duration: {} ms", result.getEndMillis() - result.getStartMillis());

        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.PASS, "Test Passed");
            extentTest.log(Status.INFO, "Execution Time: " +
                (result.getEndMillis() - result.getStartMillis()) + " ms");
        }
    }

    // Called when test method fails
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();

        logger.error("TEST FAILED: {}", testName);
        logger.error("Error: {}", throwable != null ? throwable.getMessage() : "Unknown error");

        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.FAIL, "Test Failed");
            if (throwable != null) {
                extentTest.log(Status.FAIL, throwable);
            }

            // Capture and embed screenshot as Base64 on failure
            String base64Screenshot = captureScreenshotAsBase64(testName + "_FAILED");
            if (base64Screenshot != null) {
                extentTest.addScreenCaptureFromBase64String(base64Screenshot, "Failure Screenshot");
                logger.info("Screenshot embedded in report for: {}", testName);
            }
        }
    }

    // Called when test method is skipped
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.warn("TEST SKIPPED: {}", testName);

        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.SKIP, "Test Skipped");
            if (result.getThrowable() != null) {
                extentTest.log(Status.SKIP, result.getThrowable());
            }
        }
    }

    // Called when test method fails but within success percentage
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.warn("TEST FAILED WITHIN SUCCESS PERCENTAGE: {}", testName);
    }

    // Initialize Extent Reports
    private void initializeExtentReports(String suiteName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        reportFilePath = REPORT_PATH + "ExtentReport_" + timestamp + ".html";

        // Create absolute path for report
        File reportFile = new File(reportFilePath);
        String absolutePath = reportFile.getAbsolutePath();

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(absolutePath);
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("CPI AI Automation - Test Report");
        sparkReporter.config().setReportName(suiteName);
        sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
        sparkReporter.config().setEncoding("UTF-8");

        // Enable offline mode to include all CSS/JS inline (fixes tab navigation issues)
        sparkReporter.config().setOfflineMode(true);

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Application", "CPI AI Automation Framework");
        extent.setSystemInfo("Environment", System.getProperty("environment", "QA"));
        extent.setSystemInfo("Browser", "Chrome");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));

        logger.info("Extent Report initialized at: {}", absolutePath);
    }

    // Static driver reference for screenshot capture
    private static ThreadLocal<WebDriver> driverRef = new ThreadLocal<>();

    // Set driver reference (call from test class)
    public static void setDriver(WebDriver driver) {
        driverRef.set(driver);
    }

    // Get driver reference
    public static WebDriver getDriver() {
        return driverRef.get();
    }

    // Capture screenshot as Base64 string (embedded in report)
    private String captureScreenshotAsBase64(String testName) {
        try {
            WebDriver driver = driverRef.get();
            if (driver == null) {
                // Fallback to BaseTest driver
                driver = BaseTest.getDriver();
            }
            if (driver == null) {
                logger.warn("WebDriver is null, cannot capture screenshot");
                return null;
            }

            // Capture screenshot as Base64
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

            // Also save to file for reference
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = testName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_PATH + fileName;

            byte[] decodedBytes = Base64.getDecoder().decode(base64Screenshot);
            Files.write(Paths.get(filePath), decodedBytes);

            logger.info("Screenshot saved: {}", filePath);
            return base64Screenshot;

        } catch (Exception e) {
            logger.error("Failed to capture screenshot", e);
            return null;
        }
    }

    // Get test categories from groups
    private String[] getCategories(ITestResult result) {
        String[] groups = result.getMethod().getGroups();
        return groups.length > 0 ? groups : new String[]{"Uncategorized"};
    }

    // Create required directories
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(REPORT_PATH));
            Files.createDirectories(Paths.get(SCREENSHOT_PATH));
        } catch (IOException e) {
            logger.error("Failed to create directories", e);
        }
    }

    // Get current Extent Test
    public static ExtentTest getExtentTest() {
        return test.get();
    }

    // Log info to Extent Report
    public static void logInfo(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.INFO, message);
        }
    }

    // Log pass to Extent Report
    public static void logPass(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.PASS, message);
        }
    }

    // Log fail to Extent Report
    public static void logFail(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.FAIL, message);
        }
    }
}
