package com.automation.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.automation.utils.ConfigReader;

import java.util.HashMap;

// Singleton Driver Manager - maintains single browser instance across all tests
public class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static WebDriver driver;
    private static boolean isLoggedIn = false;
    private static boolean isOnHomePage = false;

    // Private constructor to prevent instantiation
    private DriverManager() {}

    // Get or create driver instance
    public static synchronized WebDriver getDriver() {
        if (driver == null) {
            initializeDriver();
        }
        return driver;
    }

    // Initialize driver
    private static void initializeDriver() {
        // Read headless property from system property or config file
        String headlessStr = System.getProperty("headless",
        ConfigReader.getInstance().getProperty("headless", "false"));
        boolean headless = Boolean.parseBoolean(headlessStr);

        logger.info("Initializing Chrome WebDriver (headless={})", headless);
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();

        // Add headless mode if enabled
        if (headless) {
            chromeOptions.addArguments("--headless=new");
            logger.info("Running in headless mode");
        }

        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--remote-allow-origins=*");

        // Disable Chrome autofill popups (Save address?, Save password?, etc.)
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("autofill.profile_enabled", false);
        prefs.put("autofill.address_enabled", false);
        prefs.put("autofill.credit_card_enabled", false);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        chromeOptions.setExperimentalOption("prefs", prefs);

        // Disable autofill address save prompt feature
        chromeOptions.addArguments("--disable-features=AutofillAddressProfileSavePrompt");

        driver = new ChromeDriver(chromeOptions);
        logger.info("Chrome WebDriver initialized successfully");
    }

    // Close driver
    public static synchronized void quitDriver() {
        if (driver != null) {
            logger.info("Closing WebDriver");
            driver.quit();
            driver = null;
            isLoggedIn = false;
            isOnHomePage = false;
            logger.info("WebDriver closed successfully");
        }
    }

    // Check if driver is active
    public static boolean isDriverActive() {
        return driver != null;
    }

    // Set logged in status
    public static void setLoggedIn(boolean status) {
        isLoggedIn = status;
        logger.info("Login status set to: {}", status);
    }

    // Check if logged in
    public static boolean isLoggedIn() {
        return isLoggedIn;
    }

    // Set on home page status
    public static void setOnHomePage(boolean status) {
        isOnHomePage = status;
        logger.info("Home page status set to: {}", status);
    }

    // Check if on home page
    public static boolean isOnHomePage() {
        return isOnHomePage;
    }
}
