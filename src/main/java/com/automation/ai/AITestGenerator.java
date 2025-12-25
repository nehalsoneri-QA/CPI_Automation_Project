package com.automation.ai;

import com.automation.utils.ConfigReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI Test Generator - Uses AI/LLM APIs to generate test cases and locators
 *
 * Features:
 * - Generate test scenarios from page analysis
 * - Suggest locator strategies
 * - Generate test data
 * - Analyze page structure for testability
 *
 * Supports integration with:
 * - OpenAI GPT API
 * - Anthropic Claude API
 * - Local LLM endpoints
 */
public class AITestGenerator {

    private static final Logger logger = LogManager.getLogger(AITestGenerator.class);
    private final WebDriver driver;
    private final ConfigReader config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // API Configuration
    private final String aiProvider;
    private final String apiKey;
    private final String apiEndpoint;

    /**
     * Constructor
     */
    public AITestGenerator(WebDriver driver) {
        this.driver = driver;
        this.config = ConfigReader.getInstance();
        this.objectMapper = new ObjectMapper();

        // Configure HTTP client
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        // Load AI configuration
        this.aiProvider = config.getProperty("ai.provider", "openai");
        this.apiKey = config.getProperty("ai.api.key", "");
        this.apiEndpoint = config.getProperty("ai.api.endpoint", "https://api.openai.com/v1/chat/completions");
    }

    /**
     * Analyze current page and generate test suggestions
     */
    public List<TestSuggestion> analyzePageAndSuggestTests() {
        logger.info("Analyzing page for test suggestions: {}", driver.getCurrentUrl());

        PageAnalysis analysis = analyzePage();
        String prompt = buildTestSuggestionPrompt(analysis);

        try {
            String aiResponse = callAIAPI(prompt);
            return parseTestSuggestions(aiResponse);
        } catch (IOException e) {
            logger.error("Failed to get AI test suggestions", e);
            return generateDefaultSuggestions(analysis);
        }
    }

    /**
     * Generate locator suggestions for an element description
     */
    public List<LocatorSuggestion> suggestLocators(String elementDescription) {
        logger.info("Generating locator suggestions for: {}", elementDescription);

        PageAnalysis analysis = analyzePage();
        String prompt = buildLocatorSuggestionPrompt(elementDescription, analysis);

        try {
            String aiResponse = callAIAPI(prompt);
            return parseLocatorSuggestions(aiResponse);
        } catch (IOException e) {
            logger.error("Failed to get AI locator suggestions", e);
            return generateDefaultLocators(elementDescription);
        }
    }

    /**
     * Generate test data based on form analysis
     */
    public Map<String, String> generateTestData(String formDescription) {
        logger.info("Generating test data for: {}", formDescription);

        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        StringBuilder formFields = new StringBuilder();

        for (WebElement input : inputs) {
            String type = input.getAttribute("type");
            String name = input.getAttribute("name");
            String placeholder = input.getAttribute("placeholder");
            formFields.append(String.format("- %s (%s): %s\n", name, type, placeholder));
        }

        String prompt = "Generate realistic test data for a form with these fields:\n" +
            formFields +
            "\nReturn as JSON with field names as keys.";

        try {
            String aiResponse = callAIAPI(prompt);
            return parseTestData(aiResponse);
        } catch (IOException e) {
            logger.error("Failed to generate test data", e);
            return generateDefaultTestData();
        }
    }

    /**
     * Analyze page structure
     */
    private PageAnalysis analyzePage() {
        PageAnalysis analysis = new PageAnalysis();
        analysis.url = driver.getCurrentUrl();
        analysis.title = driver.getTitle();

        // Analyze forms
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        analysis.formCount = forms.size();

        // Analyze inputs
        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        analysis.inputElements = new ArrayList<>();
        for (WebElement input : inputs) {
            ElementInfo info = new ElementInfo();
            info.tagName = "input";
            info.type = input.getAttribute("type");
            info.id = input.getAttribute("id");
            info.name = input.getAttribute("name");
            info.placeholder = input.getAttribute("placeholder");
            analysis.inputElements.add(info);
        }

        // Analyze buttons
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        analysis.buttonCount = buttons.size();

        // Analyze links
        List<WebElement> links = driver.findElements(By.tagName("a"));
        analysis.linkCount = links.size();

        return analysis;
    }

    /**
     * Build prompt for test suggestions
     */
    private String buildTestSuggestionPrompt(PageAnalysis analysis) {
        return String.format("""
            Analyze this web page and suggest test cases:

            URL: %s
            Title: %s
            Forms: %d
            Input Fields: %d
            Buttons: %d
            Links: %d

            Input Elements:
            %s

            Generate a list of test cases in this format:
            1. Test Name: [name]
               Type: [positive/negative/edge]
               Steps: [steps]
               Expected: [expected result]

            Focus on:
            - Form validation tests
            - Authentication flows if applicable
            - Error handling
            - Edge cases
            """,
            analysis.url,
            analysis.title,
            analysis.formCount,
            analysis.inputElements.size(),
            analysis.buttonCount,
            analysis.linkCount,
            formatElementList(analysis.inputElements)
        );
    }

    /**
     * Build prompt for locator suggestions
     */
    private String buildLocatorSuggestionPrompt(String description, PageAnalysis analysis) {
        return String.format("""
            I need to find a web element described as: "%s"

            Page elements available:
            %s

            Suggest the best Selenium locator strategies in order of reliability:
            1. By.id
            2. By.name
            3. By.cssSelector
            4. By.xpath

            Return as JSON array with: {"strategy": "...", "value": "...", "confidence": 0-100}
            """,
            description,
            formatElementList(analysis.inputElements)
        );
    }

    /**
     * Call AI API
     */
    private String callAIAPI(String prompt) throws IOException {
        if (apiKey.isEmpty()) {
            logger.warn("AI API key not configured, using fallback");
            throw new IOException("API key not configured");
        }

        String requestBody;
        Request request;

        switch (aiProvider.toLowerCase()) {
            case "anthropic":
            case "claude":
                requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", config.getProperty("ai.model", "claude-3-sonnet-20240229"),
                    "max_tokens", 2048,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
                ));
                request = new Request.Builder()
                    .url(apiEndpoint)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();
                break;

            case "openai":
            default:
                requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", config.getProperty("ai.model", "gpt-4"),
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 2048
                ));
                request = new Request.Builder()
                    .url(apiEndpoint)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();
                break;
        }

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Extract content based on provider
            if ("anthropic".equalsIgnoreCase(aiProvider) || "claude".equalsIgnoreCase(aiProvider)) {
                return jsonResponse.path("content").path(0).path("text").asText();
            } else {
                return jsonResponse.path("choices").path(0).path("message").path("content").asText();
            }
        }
    }

    /**
     * Parse test suggestions from AI response
     */
    private List<TestSuggestion> parseTestSuggestions(String response) {
        List<TestSuggestion> suggestions = new ArrayList<>();
        // Parse the AI response and create TestSuggestion objects
        // This is a simplified implementation - enhance based on actual AI response format

        String[] lines = response.split("\n");
        TestSuggestion current = null;

        for (String line : lines) {
            if (line.contains("Test Name:")) {
                if (current != null) {
                    suggestions.add(current);
                }
                current = new TestSuggestion();
                current.name = line.replace("Test Name:", "").trim();
            } else if (line.contains("Type:") && current != null) {
                current.type = line.replace("Type:", "").trim();
            } else if (line.contains("Steps:") && current != null) {
                current.steps = line.replace("Steps:", "").trim();
            } else if (line.contains("Expected:") && current != null) {
                current.expectedResult = line.replace("Expected:", "").trim();
            }
        }

        if (current != null) {
            suggestions.add(current);
        }

        return suggestions;
    }

    /**
     * Parse locator suggestions from AI response
     */
    private List<LocatorSuggestion> parseLocatorSuggestions(String response) {
        List<LocatorSuggestion> suggestions = new ArrayList<>();

        try {
            JsonNode jsonArray = objectMapper.readTree(response);
            if (jsonArray.isArray()) {
                for (JsonNode node : jsonArray) {
                    LocatorSuggestion suggestion = new LocatorSuggestion();
                    suggestion.strategy = node.path("strategy").asText();
                    suggestion.value = node.path("value").asText();
                    suggestion.confidence = node.path("confidence").asInt();
                    suggestions.add(suggestion);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse locator suggestions as JSON, using regex fallback");
        }

        return suggestions;
    }

    /**
     * Parse test data from AI response
     */
    private Map<String, String> parseTestData(String response) {
        try {
            // Find JSON in response
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                String json = response.substring(start, end);
                return objectMapper.readValue(json, Map.class);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse test data JSON", e);
        }
        return new HashMap<>();
    }

    /**
     * Generate default suggestions when AI is unavailable
     */
    private List<TestSuggestion> generateDefaultSuggestions(PageAnalysis analysis) {
        List<TestSuggestion> suggestions = new ArrayList<>();

        // Add basic suggestions based on page analysis
        if (analysis.formCount > 0) {
            TestSuggestion formTest = new TestSuggestion();
            formTest.name = "Form Submission Test";
            formTest.type = "positive";
            formTest.steps = "Fill form with valid data and submit";
            formTest.expectedResult = "Form submitted successfully";
            suggestions.add(formTest);

            TestSuggestion validationTest = new TestSuggestion();
            validationTest.name = "Form Validation Test";
            validationTest.type = "negative";
            validationTest.steps = "Submit form with empty/invalid data";
            validationTest.expectedResult = "Validation errors displayed";
            suggestions.add(validationTest);
        }

        return suggestions;
    }

    /**
     * Generate default locators
     */
    private List<LocatorSuggestion> generateDefaultLocators(String description) {
        List<LocatorSuggestion> suggestions = new ArrayList<>();
        String descLower = description.toLowerCase();

        if (descLower.contains("username") || descLower.contains("email")) {
            suggestions.add(new LocatorSuggestion("id", "username", 90));
            suggestions.add(new LocatorSuggestion("name", "username", 85));
            suggestions.add(new LocatorSuggestion("cssSelector", "input[type='text']", 70));
        } else if (descLower.contains("password")) {
            suggestions.add(new LocatorSuggestion("id", "password", 90));
            suggestions.add(new LocatorSuggestion("cssSelector", "input[type='password']", 85));
        } else if (descLower.contains("button") || descLower.contains("submit")) {
            suggestions.add(new LocatorSuggestion("cssSelector", "button[type='submit']", 85));
            suggestions.add(new LocatorSuggestion("xpath", "//button[contains(text(),'Submit')]", 75));
        }

        return suggestions;
    }

    /**
     * Generate default test data
     */
    private Map<String, String> generateDefaultTestData() {
        Map<String, String> data = new HashMap<>();
        data.put("username", "testuser@example.com");
        data.put("password", "Test@123");
        data.put("email", "test@example.com");
        data.put("firstName", "Test");
        data.put("lastName", "User");
        return data;
    }

    /**
     * Format element list for prompt
     */
    private String formatElementList(List<ElementInfo> elements) {
        StringBuilder sb = new StringBuilder();
        for (ElementInfo el : elements) {
            sb.append(String.format("- %s: id=%s, name=%s, type=%s, placeholder=%s\n",
                el.tagName, el.id, el.name, el.type, el.placeholder));
        }
        return sb.toString();
    }

    // ==================== Inner Classes ====================

    private static class PageAnalysis {
        String url;
        String title;
        int formCount;
        int buttonCount;
        int linkCount;
        List<ElementInfo> inputElements = new ArrayList<>();
    }

    private static class ElementInfo {
        String tagName;
        String type;
        String id;
        String name;
        String placeholder;
    }

    public static class TestSuggestion {
        public String name;
        public String type;
        public String steps;
        public String expectedResult;
    }

    public static class LocatorSuggestion {
        public String strategy;
        public String value;
        public int confidence;

        public LocatorSuggestion() {}

        public LocatorSuggestion(String strategy, String value, int confidence) {
            this.strategy = strategy;
            this.value = value;
            this.confidence = confidence;
        }
    }
}
