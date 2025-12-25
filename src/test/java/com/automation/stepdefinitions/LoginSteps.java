package com.automation.stepdefinitions;

import com.automation.pages.LoginPage;
import com.automation.utils.ConfigReader;
import com.automation.utils.LoginDataProvider;
import com.automation.utils.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step Definitions for Login Feature - CPI AI Application
 * URL: https://cpiai-dev.attri.ai/
 * Fetches credentials from Excel file for data-driven testing
 */
public class LoginSteps {

    private static final Logger logger = LogManager.getLogger(LoginSteps.class);
    private final TestContext testContext;
    private final WebDriver driver;
    private final LoginPage loginPage;
    private final ConfigReader config;
    private final LoginDataProvider loginDataProvider;

    // Constructor - Dependency injection via PicoContainer
    public LoginSteps(TestContext testContext) {
        this.testContext = testContext;
        this.driver = testContext.getDriver();
        this.loginPage = new LoginPage(driver);
        this.config = ConfigReader.getInstance();
        this.loginDataProvider = new LoginDataProvider();
    }

    // ==================== Given Steps ====================

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        logger.info("Navigating to CPI AI login page");
        String baseUrl = config.getProperty("base.url");
        driver.get(baseUrl);
        loginPage.waitForPageLoad();
    }

    @Given("I am on the CPI AI application")
    public void iAmOnTheCPIAIApplication() {
        logger.info("Navigating to CPI AI application");
        String baseUrl = config.getProperty("base.url");
        driver.get(baseUrl);
        logger.info("Opened URL: {}", baseUrl);
    }

    @Given("I am logged in as {string}")
    public void iAmLoggedInAs(String userType) {
        logger.info("Logging in as: {}", userType);
        Map<String, String> credentials = loginDataProvider.getCredentialsByUserType(userType);
        String email = credentials.get("Email");
        String password = credentials.get("Password");
        iAmOnTheLoginPage();
        loginPage.login(email, password);
    }

    @Given("I have valid admin credentials from Excel")
    public void iHaveValidAdminCredentialsFromExcel() {
        logger.info("Fetching valid admin credentials from Excel");
        Map<String, String> credentials = loginDataProvider.getValidAdminCredentials();
        testContext.setScenarioContext("email", credentials.get("Email"));
        testContext.setScenarioContext("password", credentials.get("Password"));
        logger.info("Credentials loaded - Email: {}", credentials.get("Email"));
    }

    // ==================== When Steps ====================

    @When("I enter username {string}")
    public void iEnterUsername(String username) {
        logger.info("Entering username: {}", username);
        loginPage.inputUsername(username);
        testContext.setScenarioContext("username", username);
    }

    @When("I enter email {string}")
    public void iEnterEmail(String email) {
        logger.info("Entering email: {}", email);
        loginPage.inputUsername(email);
        testContext.setScenarioContext("email", email);
    }

    @When("I enter password {string}")
    public void iEnterPassword(String password) {
        logger.info("Entering password");
        loginPage.inputPassword(password);
    }

    @When("I enter credentials from Excel")
    public void iEnterCredentialsFromExcel() {
        logger.info("Entering credentials from Excel");
        Map<String, String> credentials = loginDataProvider.getValidAdminCredentials();
        String email = credentials.get("Email");
        String password = credentials.get("Password");
        logger.info("Using Email from Excel: {}", email);
        loginPage.inputUsername(email);
        loginPage.inputPassword(password);
        testContext.setScenarioContext("email", email);
    }

    @When("I enter admin credentials from Excel")
    public void iEnterAdminCredentialsFromExcel() {
        logger.info("Entering admin credentials from Excel");
        String email = loginDataProvider.getAdminEmail();
        String password = loginDataProvider.getAdminPassword();
        logger.info("Admin Email: {}", email);
        loginPage.inputUsername(email);
        loginPage.inputPassword(password);
    }

    @When("I enter credentials for test case {string}")
    public void iEnterCredentialsForTestCase(String testCaseName) {
        logger.info("Entering credentials for test case: {}", testCaseName);
        Map<String, String> credentials = loginDataProvider.getCredentialsByTestCase(testCaseName);
        String email = credentials.get("Email");
        String password = credentials.get("Password");
        loginPage.inputUsername(email);
        loginPage.inputPassword(password);
        testContext.setScenarioContext("expectedResult", credentials.get("ExpectedResult"));
    }

    @When("I click on the login button")
    public void iClickOnTheLoginButton() {
        logger.info("Clicking login button");
        loginPage.clickLogin();
    }

    @When("I login with Excel credentials")
    public void iLoginWithExcelCredentials() {
        logger.info("Performing login with Excel credentials");
        Map<String, String> credentials = loginDataProvider.getValidAdminCredentials();
        loginPage.login(credentials.get("Email"), credentials.get("Password"));
    }

    @When("I login with invalid credentials")
    public void iLoginWithInvalidCredentials() {
        logger.info("Performing login with Faker-generated invalid credentials");
        loginPage.loginWithInvalidCredentials();
    }

    // ==================== Then Steps ====================

    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        logger.info("Verifying successful login");
        assertThat(loginPage.isSuccessDisplayed())
            .as("User should be logged in")
            .isTrue();
    }

    @Then("I should see an error message {string}")
    public void iShouldSeeAnErrorMessage(String expectedMessage) {
        logger.info("Verifying error message: {}", expectedMessage);
        loginPage.waitForErrorMessage();
        String actualMessage = loginPage.getErrorMessage();
        assertThat(actualMessage)
            .as("Error message should match")
            .containsIgnoringCase(expectedMessage);
    }

    @Then("I should see an error message")
    public void iShouldSeeAnErrorMessage() {
        logger.info("Verifying error message is displayed");
        assertThat(loginPage.isErrorDisplayed())
            .as("Error message should be displayed")
            .isTrue();
    }

    @Then("the login result should be {string}")
    public void theLoginResultShouldBe(String expectedResult) {
        logger.info("Verifying login result: {}", expectedResult);
        if (expectedResult.equalsIgnoreCase("success")) {
            assertThat(loginPage.isSuccessDisplayed())
                .as("Login should be successful")
                .isTrue();
        } else {
            assertThat(loginPage.isErrorDisplayed())
                .as("Error message should be displayed")
                .isTrue();
        }
    }

    @Then("the login result should match expected from Excel")
    public void theLoginResultShouldMatchExpectedFromExcel() {
        String expectedResult = (String) testContext.getScenarioContext("expectedResult");
        theLoginResultShouldBe(expectedResult);
    }

    @Then("I should see the login page")
    public void iShouldSeeTheLoginPage() {
        logger.info("Verifying login page is displayed");
        loginPage.waitForPageLoad();
    }

    @Then("the page title should be {string}")
    public void thePageTitleShouldBe(String expectedTitle) {
        logger.info("Verifying page title: {}", expectedTitle);
        String actualTitle = loginPage.getLoginPageTitle();
        assertThat(actualTitle)
            .as("Page title should match")
            .containsIgnoringCase(expectedTitle);
    }
}
