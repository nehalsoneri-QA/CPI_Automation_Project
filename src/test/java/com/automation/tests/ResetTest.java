package com.automation.tests;

import com.automation.listeners.TestListener;
import com.automation.pages.LoginPage;
import com.automation.pages.ResetPage;
import com.automation.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reset Password Test Class
 * Handles the reset password page that appears after successful login
 */
public class ResetTest {

    protected static final Logger logger = LogManager.getLogger(ResetTest.class);
    protected static ConfigReader config = ConfigReader.getInstance();

    private LoginPage loginPage;
    private ResetPage resetPage;
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void initDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(chromeOptions);

        // Set driver reference for TestListener to capture screenshots
        TestListener.setDriver(driver);

        loginPage = new LoginPage(driver);
        resetPage = new ResetPage(driver);
        driver.get(config.getProperty("base.url"));
        logger.info("Browser initialized and navigated to: {}", config.getProperty("base.url"));
    }

    @AfterClass(alwaysRun = true)
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
    }

    // Test: Login and handle reset password page
    @Test(priority = 1, groups = {"smoke", "reset"})
    @Description("Login with valid credentials and handle reset password page if displayed")
    public void testLoginAndHandleResetPassword() {
        logger.info("Starting test: Login and handle reset password page");

        // Login with valid credentials from Excel
        loginPage.login();

        // Wait for page to load after login
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if reset password page is displayed
        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL after login: {}", currentUrl);

        if (currentUrl.contains("/reset_password")) {
            // Reset password page is displayed - click skip button
            logger.info("Reset password page detected");
            assertThat(resetPage.isSkipButtonDisplayed())
                .as("Skip button should be displayed on reset password page")
                .isTrue();

            resetPage.clickSkipButton();
            logger.info("Clicked skip button on reset password page");

            // Wait for redirect after skip
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Verify redirected to dashboard/home page
            String urlAfterSkip = driver.getCurrentUrl();
            assertThat(urlAfterSkip)
                .as("Should be redirected away from reset password page")
                .doesNotContain("/reset_password");

            logger.info("Test passed: Successfully skipped reset password page");
        } else {
            // Not on reset password page - verify success message
            logger.info("Not on reset password page, verifying login success");
            assertThat(loginPage.isSuccessDisplayed())
                .as("Login should be successful")
                .isTrue();

            logger.info("Test passed: Login successful without reset password prompt");
        }
    }

    // ==================== Custom Annotation ====================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Description {
        String value();
    }
}
