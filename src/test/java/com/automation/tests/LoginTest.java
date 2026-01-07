package com.automation.tests;

import com.automation.base.DriverManager;
import com.automation.listeners.TestListener;
import com.automation.pages.LoginPage;
import com.automation.pages.ResetPage;
import com.automation.utils.ConfigReader;
import com.automation.utils.TestWaitHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Login Test Class - TestNG based tests for CPI AI Login functionality
 * Contains only 2 test methods for login flow:
 * 1. Negative flow (invalid credentials using Faker) - runs first
 * 2. Positive flow (valid credentials from Excel) - runs second, handles reset password
 * URL: https://cpiai-dev.attri.ai/
 */
public class LoginTest {

    protected static final Logger logger = LogManager.getLogger(LoginTest.class);
    protected static ConfigReader config = ConfigReader.getInstance();

    private LoginPage loginPage;
    private ResetPage resetPage;
    private WebDriver driver;
    private TestWaitHelper waitHelper;
    private boolean isFirstTest = true;

    @BeforeClass(alwaysRun = true)
    public void initDriver() {
        // Get shared driver instance
        driver = DriverManager.getDriver();
        waitHelper = new TestWaitHelper(driver);

        // Set driver reference for TestListener to capture screenshots
        TestListener.setDriver(driver);

        // Initialize page objects
        loginPage = new LoginPage(driver);
        resetPage = new ResetPage(driver);

        // Navigate to login page
        driver.get(config.getProperty("base.url"));
        logger.info("Browser initialized and navigated to: {}", config.getProperty("base.url"));
    }

    @BeforeMethod(alwaysRun = true)
    public void refreshBeforeTest() {
        if (!isFirstTest) {
            driver.navigate().refresh();
            loginPage.waitForPageLoad();
            logger.info("Page refreshed for next test");
        }
        isFirstTest = false;
    }

    // Note: No @AfterClass to close browser - browser stays open for HomePageTest

    // ==================== Login Test Methods (Only 2 Methods) ====================

    // Test 1: Negative Login Flow - Uses Faker to generate invalid credentials (runs FIRST)
    @Test(priority = 1, groups = {"smoke", "login", "negative"})
    @Description("Verify login fails with Faker-generated invalid credentials")
    public void verifyLoginWithInvalidCredentials() {
        logger.info("Starting test: Negative Login Flow with Faker-generated credentials");

        // Wait for user to view the login page
        sleep(1500);
        loginPage.captureScreenshotToReport("Login Page - Before Invalid Login");

        // Use LoginPage method that generates invalid credentials using Faker
        String errorMessage = loginPage.loginWithInvalidCredentials();

        // Wait for user to view the error message
        sleep(2000);
        loginPage.captureScreenshotToReport("Login Page - Invalid Credentials Error");

        assertThat(loginPage.isErrorDisplayed())
            .as("Error message should be displayed for invalid credentials").isTrue();

        logger.info("Negative login test passed - Error message displayed: {}", errorMessage);
    }

    // Test 2: Positive Login Flow - Uses valid credentials from Excel (runs SECOND)
    // This test also handles reset password page and leaves user on home page
    @Test(priority = 2, groups = {"smoke", "login", "positive"})
    @Description("Verify login with admin credentials from UserData sheet and handle reset password")
    public void verifyLoginWithValidCredentials() {
        logger.info("Starting test: Admin Login from UserData Sheet");

        // Wait for user to view the login page
        sleep(1500);
        loginPage.captureScreenshotToReport("Login Page - Before Valid Login");

        // Use login method from LoginPage which fetches admin credentials from Excel
        loginPage.login();

        // Wait for page to load after login
        sleep(2000);

        // Check current URL after login
        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL after login: {}", currentUrl);

        // If reset password page is displayed, click skip button
        if (currentUrl.contains("/reset_password")) {
            logger.info("Reset password page detected, clicking skip button");

            // Wait for user to view reset password page
            sleep(1500);
            loginPage.captureScreenshotToReport("Reset Password Page");

            assertThat(resetPage.isSkipButtonDisplayed())
                .as("Skip button should be displayed on reset password page")
                .isTrue();

            resetPage.clickSkipButton();
            logger.info("Clicked skip button");

            // Wait for redirect to home page
            sleep(2000);
            loginPage.captureScreenshotToReport("Home Page - After Login");

            // Verify redirected away from reset password page
            String urlAfterSkip = driver.getCurrentUrl();
            assertThat(urlAfterSkip)
                .as("Should be redirected away from reset password page")
                .doesNotContain("/reset_password");

            logger.info("Test passed: Admin Login successful (skipped reset password)");
        } else {
            // Wait for user to view success
            sleep(1500);
            loginPage.captureScreenshotToReport("Home Page - After Login");
            logger.info("No reset password page, user is on dashboard");
        }

        // Mark as logged in and on home page for next test class
        DriverManager.setLoggedIn(true);
        DriverManager.setOnHomePage(true);
        logger.info("User is now logged in and on home page - ready for HomePageTest");
    }

    // ==================== Helper Methods ====================

    /**
     * Wait for page stability - replaces Thread.sleep with explicit waits
     * @param milliseconds ignored - kept for backward compatibility, uses explicit wait instead
     */
    private void sleep(long milliseconds) {
        // Use explicit wait instead of Thread.sleep for more reliable test execution
        waitHelper.waitForPageStability();
    }

    // ==================== Custom Annotation ====================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Description {
        String value();
    }
}
