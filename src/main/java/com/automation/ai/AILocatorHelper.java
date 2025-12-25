package com.automation.ai;

import com.automation.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 * AI Locator Helper - Provides intelligent element location with fallback strategies
 * Implements self-healing locator patterns for robust test automation
 *
 * Key Features:
 * - Multiple fallback locator strategies
 * - Smart element scoring based on attributes
 * - Machine learning-ready data collection
 * - Automatic locator suggestion
 */
public class AILocatorHelper {

    private static final Logger logger = LogManager.getLogger(AILocatorHelper.class);
    private final WebDriver driver;
    private final ConfigReader config;
    private final Map<String, List<By>> locatorCache;
    private final Map<String, LocatorHistory> locatorHistory;

    /**
     * Constructor
     */
    public AILocatorHelper(WebDriver driver) {
        this.driver = driver;
        this.config = ConfigReader.getInstance();
        this.locatorCache = new HashMap<>();
        this.locatorHistory = new HashMap<>();
    }

    /**
     * Find element with multiple fallback strategies
     * If primary locator fails, tries alternative strategies
     */
    public WebElement findElementWithFallback(By primaryLocator, String elementDescription) {
        logger.info("Finding element: {} using primary locator: {}", elementDescription, primaryLocator);

        // Try primary locator first
        try {
            WebElement element = driver.findElement(primaryLocator);
            if (element.isDisplayed()) {
                recordSuccessfulLocator(elementDescription, primaryLocator);
                return element;
            }
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            logger.warn("Primary locator failed for: {}, trying fallback strategies", elementDescription);
        }

        // Generate and try fallback locators
        List<By> fallbackLocators = generateFallbackLocators(primaryLocator, elementDescription);

        for (By locator : fallbackLocators) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    logger.info("Found element using fallback locator: {}", locator);
                    recordSuccessfulLocator(elementDescription, locator);
                    suggestLocatorUpdate(elementDescription, primaryLocator, locator);
                    return element;
                }
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                // Continue to next fallback
            }
        }

        // Try AI-based smart search as last resort
        WebElement smartElement = smartElementSearch(elementDescription);
        if (smartElement != null) {
            return smartElement;
        }

        // If all strategies fail, throw exception with suggestions
        throw new NoSuchElementException(
            "Unable to find element: " + elementDescription +
                "\nPrimary locator: " + primaryLocator +
                "\nTried " + fallbackLocators.size() + " fallback strategies" +
                "\nSuggestion: Update the locator or check if element exists on page"
        );
    }

    /**
     * Generate fallback locators based on primary locator and element description
     */
    private List<By> generateFallbackLocators(By primaryLocator, String description) {
        List<By> fallbacks = new ArrayList<>();
        String locatorString = primaryLocator.toString();

        // Check cache first
        if (locatorCache.containsKey(description)) {
            fallbacks.addAll(locatorCache.get(description));
        }

        // Generate based on element description keywords
        String descLower = description.toLowerCase();

        // Username/Email field patterns
        if (descLower.contains("username") || descLower.contains("email") || descLower.contains("user")) {
            fallbacks.addAll(Arrays.asList(
                By.id("username"),
                By.id("email"),
                By.id("user"),
                By.id("login-username"),
                By.name("username"),
                By.name("email"),
                By.name("user"),
                By.cssSelector("input[type='text'][name*='user']"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[placeholder*='username' i]"),
                By.cssSelector("input[placeholder*='email' i]"),
                By.cssSelector("input[aria-label*='username' i]"),
                By.xpath("//input[@type='text' or @type='email'][contains(@class,'user') or contains(@id,'user')]"),
                By.xpath("//input[contains(@placeholder,'Username') or contains(@placeholder,'Email')]")
            ));
        }

        // Password field patterns
        if (descLower.contains("password") || descLower.contains("pwd")) {
            fallbacks.addAll(Arrays.asList(
                By.id("password"),
                By.id("pwd"),
                By.id("pass"),
                By.id("login-password"),
                By.name("password"),
                By.name("pwd"),
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[placeholder*='password' i]"),
                By.cssSelector("input[aria-label*='password' i]"),
                By.xpath("//input[@type='password']")
            ));
        }

        // Button patterns
        if (descLower.contains("button") || descLower.contains("submit") ||
            descLower.contains("login") || descLower.contains("sign in")) {
            fallbacks.addAll(Arrays.asList(
                By.id("loginButton"),
                By.id("login-button"),
                By.id("submit"),
                By.id("signIn"),
                By.name("login"),
                By.name("submit"),
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.cssSelector("button.login-btn"),
                By.cssSelector("button.submit-btn"),
                By.cssSelector("button[class*='login']"),
                By.xpath("//button[@type='submit']"),
                By.xpath("//button[contains(text(),'Login') or contains(text(),'Sign In') or contains(text(),'Log In')]"),
                By.xpath("//input[@type='submit']"),
                By.xpath("//*[contains(@class,'login') and (self::button or self::input)]")
            ));
        }

        // Link patterns
        if (descLower.contains("link") || descLower.contains("forgot")) {
            fallbacks.addAll(Arrays.asList(
                By.linkText("Forgot Password"),
                By.linkText("Forgot Password?"),
                By.partialLinkText("Forgot"),
                By.cssSelector("a[href*='forgot']"),
                By.cssSelector("a[href*='reset']"),
                By.xpath("//a[contains(text(),'Forgot')]")
            ));
        }

        // Remove duplicates while preserving order
        return new ArrayList<>(new LinkedHashSet<>(fallbacks));
    }

    /**
     * Smart element search using multiple strategies and scoring
     */
    private WebElement smartElementSearch(String description) {
        logger.info("Attempting smart element search for: {}", description);

        String descLower = description.toLowerCase();
        List<ScoredElement> candidates = new ArrayList<>();

        // Get all potentially matching elements
        List<WebElement> allInputs = driver.findElements(By.tagName("input"));
        List<WebElement> allButtons = driver.findElements(By.tagName("button"));
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));

        // Score input elements
        for (WebElement element : allInputs) {
            int score = calculateElementScore(element, descLower);
            if (score > 0) {
                candidates.add(new ScoredElement(element, score));
            }
        }

        // Score button elements
        for (WebElement element : allButtons) {
            int score = calculateElementScore(element, descLower);
            if (score > 0) {
                candidates.add(new ScoredElement(element, score));
            }
        }

        // Score link elements
        for (WebElement element : allLinks) {
            int score = calculateElementScore(element, descLower);
            if (score > 0) {
                candidates.add(new ScoredElement(element, score));
            }
        }

        // Sort by score descending and return best match
        candidates.sort((a, b) -> Integer.compare(b.score, a.score));

        if (!candidates.isEmpty()) {
            ScoredElement best = candidates.get(0);
            if (best.score >= 3) { // Minimum confidence threshold
                logger.info("Smart search found element with score: {}", best.score);
                return best.element;
            }
        }

        logger.warn("Smart element search failed for: {}", description);
        return null;
    }

    /**
     * Calculate score for an element based on how well it matches the description
     */
    private int calculateElementScore(WebElement element, String description) {
        int score = 0;

        try {
            if (!element.isDisplayed() || !element.isEnabled()) {
                return 0;
            }

            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            String placeholder = element.getAttribute("placeholder");
            String ariaLabel = element.getAttribute("aria-label");
            String className = element.getAttribute("class");
            String type = element.getAttribute("type");
            String text = element.getText();

            // ID match (highest weight)
            if (id != null && containsAny(id.toLowerCase(), getKeywords(description))) {
                score += 5;
            }

            // Name match
            if (name != null && containsAny(name.toLowerCase(), getKeywords(description))) {
                score += 4;
            }

            // Placeholder match
            if (placeholder != null && containsAny(placeholder.toLowerCase(), getKeywords(description))) {
                score += 3;
            }

            // Aria-label match
            if (ariaLabel != null && containsAny(ariaLabel.toLowerCase(), getKeywords(description))) {
                score += 3;
            }

            // Class name match
            if (className != null && containsAny(className.toLowerCase(), getKeywords(description))) {
                score += 2;
            }

            // Text content match
            if (text != null && containsAny(text.toLowerCase(), getKeywords(description))) {
                score += 2;
            }

            // Type-based scoring for specific elements
            if (description.contains("password") && "password".equals(type)) {
                score += 5;
            }
            if (description.contains("email") && "email".equals(type)) {
                score += 4;
            }
            if ((description.contains("submit") || description.contains("button")) && "submit".equals(type)) {
                score += 4;
            }

        } catch (StaleElementReferenceException e) {
            return 0;
        }

        return score;
    }

    /**
     * Extract keywords from description
     */
    private List<String> getKeywords(String description) {
        String[] words = description.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .split("\\s+");

        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            if (word.length() > 2 && !isStopWord(word)) {
                keywords.add(word);
            }
        }
        return keywords;
    }

    /**
     * Check if string contains any of the keywords
     */
    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if word is a stop word
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "for", "with", "this", "that", "from", "input", "field");
        return stopWords.contains(word);
    }

    /**
     * Record successful locator for learning
     */
    private void recordSuccessfulLocator(String description, By locator) {
        LocatorHistory history = locatorHistory.computeIfAbsent(description, k -> new LocatorHistory());
        history.recordSuccess(locator);
    }

    /**
     * Suggest locator update when fallback is used
     */
    private void suggestLocatorUpdate(String description, By original, By working) {
        logger.warn("LOCATOR UPDATE SUGGESTION");
        logger.warn("Element: {}", description);
        logger.warn("Original locator (failing): {}", original);
        logger.warn("Working fallback locator: {}", working);
        logger.warn("Consider updating your page object with the working locator");
    }

    /**
     * Get best performing locator from history
     */
    public By getBestLocator(String description) {
        LocatorHistory history = locatorHistory.get(description);
        if (history != null) {
            return history.getBestLocator();
        }
        return null;
    }

    /**
     * Inner class for scored element
     */
    private static class ScoredElement {
        WebElement element;
        int score;

        ScoredElement(WebElement element, int score) {
            this.element = element;
            this.score = score;
        }
    }

    /**
     * Inner class for locator history tracking
     */
    private static class LocatorHistory {
        private final Map<By, Integer> successCounts = new HashMap<>();

        void recordSuccess(By locator) {
            successCounts.merge(locator, 1, Integer::sum);
        }

        By getBestLocator() {
            return successCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        }
    }
}
