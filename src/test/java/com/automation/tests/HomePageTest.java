package com.automation.tests;

import com.automation.base.DriverManager;
import com.automation.listeners.TestListener;
import com.automation.pages.HomePage;
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
 * Home Page Test Class - TestNG based tests for CPI AI Home/Dashboard functionality
 * Uses shared browser session from LoginTest (user is already logged in)
 * Contains test methods for:
 * 1. Welcome Section validation
 * 2. Navigation Links validation (11 links)
 * 3. Dashboard Tiles validation (9 tiles)
 * 4. Admin User Menu validation
 * URL: https://cpiai-dev.attri.ai/
 */
public class HomePageTest {

    protected static final Logger logger = LogManager.getLogger(HomePageTest.class);
    protected static ConfigReader config = ConfigReader.getInstance();

    private HomePage homePage;
    private WebDriver driver;
    private TestWaitHelper waitHelper;

    @BeforeClass(alwaysRun = true)
    public void initPageObjects() {
        // Get shared driver instance (browser already open from LoginTest)
        driver = DriverManager.getDriver();
        waitHelper = new TestWaitHelper(driver);

        // Set driver reference for TestListener to capture screenshots
        TestListener.setDriver(driver);

        // Initialize page objects
        homePage = new HomePage(driver);

        // Verify user is already logged in from LoginTest
        if (DriverManager.isLoggedIn() && DriverManager.isOnHomePage()) {
            logger.info("Using existing browser session - user is already logged in and on home page");
        } else {
            logger.warn("User may not be logged in - tests might fail");
        }

        // Wait for home page to fully load
        homePage.waitForHomePageLoad();
        waitHelper.waitForPageStability();
        logger.info("Home Page initialized and ready for testing");
    }

    @AfterClass(alwaysRun = true)
    public void closeDriver() {
        // Close browser after all tests complete
        DriverManager.quitDriver();
        logger.info("Browser closed after all tests completed");
    }

    // ==================== Welcome Section Tests ====================

    @Test(priority = 1, groups = {"smoke", "home", "welcome"})
    @Description("Verify welcome text is displayed correctly on home page")
    public void verifyWelcomeTextDisplayed() {
        logger.info("Starting test: Verify welcome text is displayed");

        // Wait for user to view
        sleep(1500);

        boolean isValid = homePage.validateWelcomeText();
        homePage.captureScreenshotToReport("Welcome Text Validation");

        assertThat(isValid)
            .as("Welcome text should contain 'Welcome to CP Insurance Associates'")
            .isTrue();

        logger.info("Test passed: Welcome text validation");
    }

    @Test(priority = 2, groups = {"smoke", "home", "welcome"})
    @Description("Verify date and time is displayed on home page")
    public void verifyDateTimeDisplayed() {
        logger.info("Starting test: Verify date/time is displayed");

        // Wait for user to view
        sleep(1500);

        boolean isValid = homePage.validateDateTimeDisplayed();
        homePage.captureScreenshotToReport("Date Time Validation");

        assertThat(isValid)
            .as("Date and time should be displayed on home page")
            .isTrue();

        logger.info("Test passed: Date/Time display validation");
    }

    @Test(priority = 3, groups = {"smoke", "home", "welcome"})
    @Description("Verify logged-in user label shows Admin User")
    public void verifyLoggedInUserLabelDisplayed() {
        logger.info("Starting test: Verify logged-in user label");

        // Wait for user to view
        sleep(1500);

        boolean isValid = homePage.validateLoggedInUserLabel();
        homePage.captureScreenshotToReport("Logged In User Label Validation");

        assertThat(isValid)
            .as("Logged-in user label should show 'Admin User'")
            .isTrue();

        logger.info("Test passed: Logged-in user label validation");
    }

    @Test(priority = 4, groups = {"smoke", "home", "welcome"})
    @Description("Verify complete welcome section is displayed correctly")
    public void verifyCompleteWelcomeSection() {
        logger.info("Starting test: Verify complete welcome section");

        // Wait for user to view
        sleep(1500);

        boolean isValid = homePage.validateWelcomeSection();
        homePage.captureScreenshotToReport("Complete Welcome Section Validation");

        assertThat(isValid)
            .as("Complete welcome section (text, date/time, user label) should be valid")
            .isTrue();

        logger.info("Test passed: Complete welcome section validation");
    }

    // ==================== Navigation Bar Tests ====================

    @Test(priority = 5, groups = {"smoke", "home", "navigation"})
    @Description("Verify all 11 navigation links are displayed and clickable")
    public void verifyAllNavigationLinksDisplayed() {
        logger.info("Starting test: Verify all 11 navigation links");

        // Wait for user to view
        sleep(1500);

        boolean allValid = homePage.validateAllNavigationLinks();
        homePage.captureScreenshotToReport("All Navigation Links Validation");

        assertThat(allValid)
            .as("All 11 navigation links (Quotes, Manage Policy, Invoices, Transactions, Refunds, Agents, Users, Carrier, Insured, Reports, Analytics) should be displayed and clickable")
            .isTrue();

        logger.info("Test passed: All navigation links validation");
    }

    @Test(priority = 6, groups = {"regression", "home", "navigation"})
    @Description("Verify Quotes link navigation works correctly")
    public void verifyQuotesLinkNavigation() {
        logger.info("Starting test: Verify Quotes link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToQuotesAndVerify();

        assertThat(success)
            .as("Clicking Quotes link should navigate to quotes page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Quotes navigation validation");
    }

    @Test(priority = 7, groups = {"regression", "home", "navigation"})
    @Description("Verify Manage Policy link navigation works correctly")
    public void verifyManagePolicyLinkNavigation() {
        logger.info("Starting test: Verify Manage Policy link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToManagePolicyAndVerify();

        assertThat(success)
            .as("Clicking Manage Policy link should navigate to policy page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Manage Policy navigation validation");
    }

    @Test(priority = 8, groups = {"regression", "home", "navigation"})
    @Description("Verify Invoices link navigation works correctly")
    public void verifyInvoicesLinkNavigation() {
        logger.info("Starting test: Verify Invoices link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToInvoicesAndVerify();

        assertThat(success)
            .as("Clicking Invoices link should navigate to invoices page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Invoices navigation validation");
    }

    @Test(priority = 9, groups = {"regression", "home", "navigation"})
    @Description("Verify Transactions link navigation works correctly")
    public void verifyTransactionsLinkNavigation() {
        logger.info("Starting test: Verify Transactions link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToTransactionsAndVerify();

        assertThat(success)
            .as("Clicking Transactions link should navigate to transactions page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Transactions navigation validation");
    }

    @Test(priority = 10, groups = {"regression", "home", "navigation"})
    @Description("Verify Refunds link navigation works correctly")
    public void verifyRefundsLinkNavigation() {
        logger.info("Starting test: Verify Refunds link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToRefundsAndVerify();

        assertThat(success)
            .as("Clicking Refunds link should navigate to refunds page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Refunds navigation validation");
    }

    @Test(priority = 11, groups = {"regression", "home", "navigation"})
    @Description("Verify Agents link navigation works correctly")
    public void verifyAgentsLinkNavigation() {
        logger.info("Starting test: Verify Agents link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToAgentsAndVerify();

        assertThat(success)
            .as("Clicking Agents link should navigate to agents page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Agents navigation validation");
    }

    @Test(priority = 12, groups = {"regression", "home", "navigation"})
    @Description("Verify Users link navigation works correctly")
    public void verifyUsersLinkNavigation() {
        logger.info("Starting test: Verify Users link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToUsersAndVerify();

        assertThat(success)
            .as("Clicking Users link should navigate to users page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Users navigation validation");
    }

    @Test(priority = 13, groups = {"regression", "home", "navigation"})
    @Description("Verify Carrier link navigation works correctly")
    public void verifyCarrierLinkNavigation() {
        logger.info("Starting test: Verify Carrier link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToCarrierAndVerify();

        assertThat(success)
            .as("Clicking Carrier link should navigate to carrier page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Carrier navigation validation");
    }

    @Test(priority = 14, groups = {"regression", "home", "navigation"})
    @Description("Verify Insured link navigation works correctly")
    public void verifyInsuredLinkNavigation() {
        logger.info("Starting test: Verify Insured link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToInsuredAndVerify();

        assertThat(success)
            .as("Clicking Insured link should navigate to insured page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Insured navigation validation");
    }

    @Test(priority = 15, groups = {"regression", "home", "navigation"})
    @Description("Verify Reports link navigation works correctly")
    public void verifyReportsLinkNavigation() {
        logger.info("Starting test: Verify Reports link navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.navigateToReportsAndVerify();

        assertThat(success)
            .as("Clicking Reports link should navigate to reports page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Reports navigation validation");
    }

    // ==================== Dashboard Tiles Tests ====================

    @Test(priority = 17, groups = {"smoke", "home", "dashboard"})
    @Description("Verify all 9 dashboard tiles are visible and enabled")
    public void verifyAllDashboardCardsDisplayed() {
        logger.info("Starting test: Verify all 9 dashboard tiles");

        // Wait for user to view
        sleep(1500);

        boolean allValid = homePage.validateAllDashboardTiles();
        homePage.captureScreenshotToReport("All Dashboard Tiles Validation");

        assertThat(allValid)
            .as("All 9 dashboard tiles (Create Quote, View Quotes, Manage Policies, Manage Users, Manage Agents, View Carriers, Invoices, Transactions, Refunds) should be visible and enabled")
            .isTrue();

        logger.info("Test passed: All dashboard tiles validation");
    }

    @Test(priority = 18, groups = {"regression", "home", "dashboard"})
    @Description("Verify Create Quote tile navigation works correctly")
    public void verifyCreateQuoteCardNavigation() {
        logger.info("Starting test: Verify Create Quote tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickCreateQuoteAndVerify();

        assertThat(success)
            .as("Clicking Create Quote tile should navigate to quote creation page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Create Quote tile navigation validation");
    }

    @Test(priority = 19, groups = {"regression", "home", "dashboard"})
    @Description("Verify View Quotes tile navigation works correctly")
    public void verifyViewQuotesCardNavigation() {
        logger.info("Starting test: Verify View Quotes tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickViewQuotesAndVerify();

        assertThat(success)
            .as("Clicking View Quotes tile should navigate to quotes list page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: View Quotes tile navigation validation");
    }

    @Test(priority = 20, groups = {"regression", "home", "dashboard"})
    @Description("Verify Manage Policies tile navigation works correctly")
    public void verifyManagePoliciesCardNavigation() {
        logger.info("Starting test: Verify Manage Policies tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickManagePoliciesAndVerify();

        assertThat(success)
            .as("Clicking Manage Policies tile should navigate to policies page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Manage Policies tile navigation validation");
    }

    @Test(priority = 21, groups = {"regression", "home", "dashboard"})
    @Description("Verify Manage Users tile navigation works correctly")
    public void verifyManageUsersCardsNavigation() {
        logger.info("Starting test: Verify Manage Users tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickManageUsersAndVerify();

        assertThat(success)
            .as("Clicking Manage Users tile should navigate to users page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Manage Users tile navigation validation");
    }

    @Test(priority = 22, groups = {"regression", "home", "dashboard"})
    @Description("Verify Manage Agents tile navigation works correctly")
    public void verifyManageAgentsCardsNavigation() {
        logger.info("Starting test: Verify Manage Agents tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickManageAgentsAndVerify();

        assertThat(success)
            .as("Clicking Manage Agents tile should navigate to agents page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Manage Agents tile navigation validation");
    }

    @Test(priority = 23, groups = {"regression", "home", "dashboard"})
    @Description("Verify View Carriers tile navigation works correctly")
    public void verifyViewCarriersCardsNavigation() {
        logger.info("Starting test: Verify View Carriers tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickViewCarriersAndVerify();

        assertThat(success)
            .as("Clicking View Carriers tile should navigate to carriers page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: View Carriers tile navigation validation");
    }

    @Test(priority = 24, groups = {"regression", "home", "dashboard"})
    @Description("Verify Invoices tile navigation works correctly")
    public void verifyInvoicesCardsNavigation() {
        logger.info("Starting test: Verify Invoices tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickInvoicesTileAndVerify();

        assertThat(success)
            .as("Clicking Invoices tile should navigate to invoices page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Invoices tile navigation validation");
    }

    @Test(priority = 25, groups = {"regression", "home", "dashboard"})
    @Description("Verify Transactions tile navigation works correctly")
    public void verifyTransactionsCardNavigation() {
        logger.info("Starting test: Verify Transactions tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickTransactionsTileAndVerify();

        assertThat(success)
            .as("Clicking Transactions tile should navigate to transactions page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Transactions tile navigation validation");
    }

    @Test(priority = 26, groups = {"regression", "home", "dashboard"})
    @Description("Verify Refunds tile navigation works correctly")
    public void verifyRefundsCardsNavigation() {
        logger.info("Starting test: Verify Refunds tile navigation");

        // Wait for user to view
        sleep(1500);

        boolean success = homePage.clickRefundsTileAndVerify();

        assertThat(success)
            .as("Clicking Refunds tile should navigate to refunds page")
            .isTrue();

        // Navigate back to home page
        homePage.goBackToHomePage();

        logger.info("Test passed: Refunds tile navigation validation");
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
