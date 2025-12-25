package com.automation.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Screenshot Utility - Provides various screenshot capture methods
 * Supports full page, element, and viewport screenshots
 */
public class ScreenshotUtil {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_PATH = "target/screenshots/";

    /**
     * Capture full page screenshot
     */
    public static String captureFullPageScreenshot(WebDriver driver, String testName) {
        try {
            createScreenshotDirectory();
            String fileName = generateFileName(testName);
            String filePath = SCREENSHOT_PATH + fileName;

            // Use AShot for full page screenshot
            Screenshot screenshot = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(100))
                .takeScreenshot(driver);

            ImageIO.write(screenshot.getImage(), "PNG", new File(filePath));
            logger.info("Full page screenshot captured: {}", filePath);

            return filePath;
        } catch (IOException e) {
            logger.error("Failed to capture full page screenshot", e);
            return null;
        }
    }

    /**
     * Capture viewport screenshot (visible area only)
     */
    public static String captureViewportScreenshot(WebDriver driver, String testName) {
        try {
            createScreenshotDirectory();
            String fileName = generateFileName(testName);
            String filePath = SCREENSHOT_PATH + fileName;

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(filePath));

            logger.info("Viewport screenshot captured: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Failed to capture viewport screenshot", e);
            return null;
        }
    }

    /**
     * Capture screenshot of specific element
     */
    public static String captureElementScreenshot(WebDriver driver, WebElement element, String testName) {
        try {
            createScreenshotDirectory();
            String fileName = "element_" + generateFileName(testName);
            String filePath = SCREENSHOT_PATH + fileName;

            // Use AShot to capture element
            Screenshot screenshot = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(100))
                .takeScreenshot(driver, element);

            ImageIO.write(screenshot.getImage(), "PNG", new File(filePath));
            logger.info("Element screenshot captured: {}", filePath);

            return filePath;
        } catch (IOException e) {
            logger.error("Failed to capture element screenshot", e);
            return null;
        }
    }

    /**
     * Capture screenshot as Base64 string
     */
    public static String captureScreenshotAsBase64(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot as Base64", e);
            return null;
        }
    }

    /**
     * Capture screenshot as byte array
     */
    public static byte[] captureScreenshotAsBytes(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot as bytes", e);
            return null;
        }
    }

    /**
     * Capture screenshot with highlighted element
     */
    public static String captureHighlightedScreenshot(WebDriver driver, WebElement element, String testName) {
        try {
            createScreenshotDirectory();

            // Highlight element
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String originalStyle = element.getAttribute("style");
            js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 3px solid red;');", element);

            // Capture screenshot
            String fileName = "highlighted_" + generateFileName(testName);
            String filePath = SCREENSHOT_PATH + fileName;

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(filePath));

            // Restore original style
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originalStyle);

            logger.info("Highlighted screenshot captured: {}", filePath);
            return filePath;
        } catch (Exception e) {
            logger.error("Failed to capture highlighted screenshot", e);
            return null;
        }
    }

    /**
     * Compare two screenshots for visual differences
     */
    public static boolean compareScreenshots(String screenshot1Path, String screenshot2Path) {
        try {
            BufferedImage img1 = ImageIO.read(new File(screenshot1Path));
            BufferedImage img2 = ImageIO.read(new File(screenshot2Path));

            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                logger.warn("Screenshots have different dimensions");
                return false;
            }

            for (int y = 0; y < img1.getHeight(); y++) {
                for (int x = 0; x < img1.getWidth(); x++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        return false;
                    }
                }
            }

            return true;
        } catch (IOException e) {
            logger.error("Failed to compare screenshots", e);
            return false;
        }
    }

    /**
     * Generate unique file name with timestamp
     */
    private static String generateFileName(String testName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String sanitizedName = testName.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitizedName + "_" + timestamp + ".png";
    }

    /**
     * Create screenshot directory if not exists
     */
    private static void createScreenshotDirectory() {
        File directory = new File(SCREENSHOT_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
