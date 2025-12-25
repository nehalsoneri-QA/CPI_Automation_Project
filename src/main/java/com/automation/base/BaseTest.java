package com.automation.base;

import com.automation.ai.SelfHealingDriver;
import com.automation.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Base Test Class - Foundation for all test classes
 * Provides WebDriver initialization, configuration, and common utilities
 * Supports AI-powered self-healing locators integration
 */
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    protected static ThreadLocal<WebDriverWait> wait = new ThreadLocal<>();
    protected static ConfigReader config;

    // AI Self-Healing flag
    protected boolean enableSelfHealing;

    static {
        config = ConfigReader.getInstance();
    }

    /**
     * Initialize WebDriver based on configuration
     * @param browser Browser type (chrome, firefox, edge)
     * @param headless Run in headless mode
     */
    @Parameters({"browser", "headless"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser, @Optional("false") String headless) {
        logger.info("Initializing WebDriver for browser: {}", browser);

        // Check if self-healing is enabled
        enableSelfHealing = Boolean.parseBoolean(config.getProperty("healenium.enabled", "false"));

        WebDriver webDriver = createDriver(browser, Boolean.parseBoolean(headless));

        // Wrap with self-healing driver if enabled
        if (enableSelfHealing) {
            logger.info("AI Self-Healing mode enabled");
            webDriver = new SelfHealingDriver(webDriver).getDriver();
        }

        driver.set(webDriver);

        // Configure WebDriver
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(
            Duration.ofSeconds(Long.parseLong(config.getProperty("implicit.wait", "10")))
        );
        getDriver().manage().timeouts().pageLoadTimeout(
            Duration.ofSeconds(Long.parseLong(config.getProperty("page.load.timeout", "30")))
        );

        // Initialize WebDriverWait
        wait.set(new WebDriverWait(getDriver(),
            Duration.ofSeconds(Long.parseLong(config.getProperty("explicit.wait", "20")))));

        logger.info("WebDriver initialized successfully");
    }

    /**
     * Create WebDriver instance based on browser type
     */
    private WebDriver createDriver(String browser, boolean headless) {
        WebDriver webDriver;

        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                firefoxOptions.addArguments("--width=1920");
                firefoxOptions.addArguments("--height=1080");
                webDriver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless");
                }
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--disable-gpu");
                webDriver = new EdgeDriver(edgeOptions);
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--remote-allow-origins=*");

                webDriver = new ChromeDriver(chromeOptions);
                break;
        }

        return webDriver;
    }

    /**
     * Create Remote WebDriver for Grid execution
     */
    protected WebDriver createRemoteDriver(String browser, String gridUrl) throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        return new RemoteWebDriver(new URL(gridUrl), options);
    }

    /**
     * Get current thread's WebDriver instance
     */
    public static WebDriver getDriver() {
        return driver.get();
    }

    /**
     * Get current thread's WebDriverWait instance
     */
    public static WebDriverWait getWait() {
        return wait.get();
    }

    /**
     * Navigate to specified URL
     */
    protected void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        getDriver().get(url);
    }

    /**
     * Navigate to application base URL from config
     */
    protected void navigateToBaseUrl() {
        String baseUrl = config.getProperty("base.url");
        navigateTo(baseUrl);
    }

    /**
     * Get current page title
     */
    protected String getPageTitle() {
        return getDriver().getTitle();
    }

    /**
     * Get current URL
     */
    protected String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    /**
     * Refresh the current page
     */
    protected void refreshPage() {
        logger.info("Refreshing current page");
        getDriver().navigate().refresh();
    }

    /**
     * Navigate back in browser history
     */
    protected void navigateBack() {
        logger.info("Navigating back");
        getDriver().navigate().back();
    }

    /**
     * Navigate forward in browser history
     */
    protected void navigateForward() {
        logger.info("Navigating forward");
        getDriver().navigate().forward();
    }

    /**
     * Tear down WebDriver after each test method
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            logger.info("Closing WebDriver");
            getDriver().quit();
            driver.remove();
            wait.remove();
        }
    }

    /**
     * Suite-level cleanup
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        logger.info("Test suite execution completed");
    }
}
