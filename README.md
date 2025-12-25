# AI-Powered Automation Framework

A comprehensive Selenium-based test automation framework with AI self-healing capabilities, supporting both Cucumber BDD and TestNG execution.

## Features

- **AI Self-Healing Locators**: Automatically heals broken locators when UI changes
- **Dual Test Runners**: Run tests with both Cucumber (BDD) and TestNG
- **Page Object Model (POM)**: Clean and maintainable page object architecture
- **Data-Driven Testing**: Excel integration for test data management
- **Parallel Execution**: Support for parallel test execution
- **Comprehensive Reporting**: Extent Reports, Cucumber Reports, and custom reporting
- **Cross-Browser Support**: Chrome, Firefox, Edge
- **Configurable**: Environment-specific configurations

## Project Structure

```
ai-automation-framework/
├── pom.xml                                    # Maven configuration
├── testng.xml                                 # TestNG suite configuration
├── README.md                                  # Project documentation
├── src/
│   ├── main/java/com/automation/
│   │   ├── base/
│   │   │   ├── BaseTest.java                  # Base test class
│   │   │   └── BasePage.java                  # Base page object class
│   │   ├── pages/
│   │   │   └── LoginPage.java                 # Login page object
│   │   ├── utils/
│   │   │   ├── ConfigReader.java              # Configuration reader
│   │   │   ├── ExcelReader.java               # Excel data reader
│   │   │   ├── TestContext.java               # Cucumber test context
│   │   │   ├── ScreenshotUtil.java            # Screenshot utilities
│   │   │   └── WaitUtil.java                  # Wait utilities
│   │   └── ai/
│   │       ├── AILocatorHelper.java           # AI locator helper
│   │       ├── SelfHealingDriver.java         # Self-healing WebDriver
│   │       └── AITestGenerator.java           # AI test generator
│   └── test/
│       ├── java/com/automation/
│       │   ├── tests/
│       │   │   └── LoginTest.java             # TestNG login tests
│       │   ├── runners/
│       │   │   ├── CucumberTestRunner.java    # Main Cucumber runner
│       │   │   ├── SmokeTestRunner.java       # Smoke test runner
│       │   │   └── FailedTestRunner.java      # Failed test re-runner
│       │   ├── stepdefinitions/
│       │   │   └── LoginSteps.java            # Cucumber step definitions
│       │   ├── hooks/
│       │   │   └── Hooks.java                 # Cucumber hooks
│       │   └── listeners/
│       │       ├── TestListener.java          # TestNG listener
│       │       └── RetryListener.java         # Retry analyzer
│       └── resources/
│           ├── features/
│           │   └── Login.feature              # Cucumber feature files
│           ├── testdata/
│           │   └── TestData.xlsx              # Test data Excel file
│           ├── config/
│           │   ├── config.properties          # Main configuration
│           │   ├── qa.properties              # QA environment config
│           │   └── prod.properties            # Production config
│           ├── log4j2.xml                     # Logging configuration
│           ├── extent.properties              # Extent report config
│           └── extent-config.xml              # Extent report styling
```

## Prerequisites

- Java JDK 17 or higher
- Maven 3.6+
- Chrome/Firefox/Edge browser

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd ai-automation-framework
```

2. Install dependencies:
```bash
mvn clean install -DskipTests
```

## Running Tests

### Run All Tests (TestNG)
```bash
mvn clean test
```

### Run Cucumber Tests
```bash
mvn clean test -Pcucumber
```

### Run TestNG Tests Only
```bash
mvn clean test -Ptestng
```

### Run Smoke Tests
```bash
mvn clean test -Dcucumber.filter.tags="@Smoke"
```

### Run with Specific Browser
```bash
mvn clean test -Dbrowser=firefox
```

### Run in Headless Mode
```bash
mvn clean test -Dheadless=true
```

### Run with AI Self-Healing Enabled
```bash
mvn clean test -Pai-healing -Dhealenium.enabled=true
```

### Run Specific Test Class
```bash
mvn clean test -Dtest=LoginTest
```

### Run Failed Tests
```bash
mvn clean test -Dtest=FailedTestRunner
```

## AI Self-Healing Feature

The framework includes AI-powered self-healing capabilities:

### How It Works:
1. When an element locator fails, the framework attempts multiple fallback strategies
2. It uses intelligent element matching based on:
   - Element attributes (id, name, class, placeholder)
   - Element text content
   - Element position and structure
3. Successful alternative locators are logged for future reference

### Enable Self-Healing:
```properties
# In config.properties
healenium.enabled=true
```

### AI Integration (Optional):
Configure AI API for advanced test generation:
```properties
ai.provider=openai
ai.api.key=your-api-key
ai.model=gpt-4
```

## Configuration

### config.properties
```properties
# Browser settings
browser=chrome
headless=false

# Timeouts
implicit.wait=10
explicit.wait=20

# Application
base.url=https://example.com

# AI Settings
healenium.enabled=false
```

## Reporting

### Extent Reports
Located at: `target/reports/ExtentReport.html`

### Cucumber Reports
Located at: `target/cucumber-reports/cucumber-html-report.html`

### Healing Report
Located at: `target/healing-report.json`

## Data-Driven Testing

### Excel Format (TestData.xlsx)
| TestCase | Username | Password | ExpectedResult | Execute |
|----------|----------|----------|----------------|---------|
| TC001    | user1    | pass1    | success        | Yes     |
| TC002    | invalid  | wrong    | failure        | Yes     |

### Using Excel Data in Tests:
```java
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    ExcelReader reader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");
    return reader.getSheetData("LoginData");
}
```

## Cucumber Tags

| Tag | Description |
|-----|-------------|
| @Smoke | Smoke tests |
| @Regression | Regression tests |
| @Login | Login-related tests |
| @Positive | Positive test cases |
| @Negative | Negative test cases |
| @AIHealing | Tests with AI self-healing |
| @DataDriven | Data-driven tests |

## Best Practices

1. **Page Objects**: Keep locators and actions in page classes
2. **Test Data**: Use Excel or config files, not hardcoded values
3. **Assertions**: Use AssertJ for fluent assertions
4. **Waits**: Use explicit waits, avoid Thread.sleep()
5. **Logging**: Use Log4j2 for consistent logging
6. **AI Healing**: Enable for flaky tests, review suggestions

## Troubleshooting

### Common Issues:

1. **WebDriver not found**
   - Solution: WebDriverManager handles this automatically

2. **Element not found**
   - Enable AI self-healing
   - Check if locators need updating

3. **Tests timing out**
   - Increase timeout values in config.properties

4. **Parallel execution issues**
   - Ensure ThreadLocal is used for WebDriver

## License

MIT License

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
