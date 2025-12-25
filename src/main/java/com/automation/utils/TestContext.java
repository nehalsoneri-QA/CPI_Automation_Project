package com.automation.utils;

import com.automation.ai.SelfHealingDriver;
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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Context - Shared context between Cucumber steps
 * Manages WebDriver instance and scenario-specific data
 */
public class TestContext {

    private static final Logger logger = LogManager.getLogger(TestContext.class);
    private WebDriver driver;
    private final Map<String, Object> scenarioContext;
    private final ConfigReader config;

    /**
     * Constructor - Initialize test context
     */
    public TestContext() {
        this.scenarioContext = new HashMap<>();
        this.config = ConfigReader.getInstance();
    }

    /**
     * Initialize WebDriver
     */
    public void initializeDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", config.getProperty("browser", "chrome"));
            boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", config.getProperty("headless", "false"))
            );
            boolean enableSelfHealing = config.getBooleanProperty("healenium.enabled", false);

            logger.info("Initializing WebDriver: browser={}, headless={}, selfHealing={}",
                browser, headless, enableSelfHealing);

            driver = createDriver(browser, headless);

            // Wrap with self-healing if enabled
            if (enableSelfHealing) {
                driver = new SelfHealingDriver(driver).getDriver();
            }

            configureDriver();
        }
    }

    /**
     * Create WebDriver based on browser type
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
                webDriver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless");
                }
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
     * Configure WebDriver settings
     */
    private void configureDriver() {
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(config.getLongProperty("implicit.wait", 10))
        );
        driver.manage().timeouts().pageLoadTimeout(
            Duration.ofSeconds(config.getLongProperty("page.load.timeout", 30))
        );
    }

    /**
     * Get WebDriver instance
     */
    public WebDriver getDriver() {
        if (driver == null) {
            initializeDriver();
        }
        return driver;
    }

    /**
     * Close WebDriver
     */
    public void closeDriver() {
        if (driver != null) {
            logger.info("Closing WebDriver");
            driver.quit();
            driver = null;
        }
    }

    /**
     * Set value in scenario context
     */
    public void setScenarioContext(String key, Object value) {
        scenarioContext.put(key, value);
    }

    /**
     * Get value from scenario context
     */
    public Object getScenarioContext(String key) {
        return scenarioContext.get(key);
    }

    /**
     * Check if key exists in scenario context
     */
    public boolean containsKey(String key) {
        return scenarioContext.containsKey(key);
    }

    /**
     * Clear scenario context
     */
    public void clearScenarioContext() {
        scenarioContext.clear();
    }

    /**
     * Get scenario context as specific type
     */
    @SuppressWarnings("unchecked")
    public <T> T getScenarioContext(String key, Class<T> type) {
        Object value = scenarioContext.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}
