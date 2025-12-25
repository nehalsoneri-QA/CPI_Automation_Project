package com.automation.pages;

import org.openqa.selenium.WebDriver;

/**
 * Home Page - High-Level Actions
 * Contains business-level operations that call basic methods from HomePageLocators
 * This is the class to be used in tests
 */
public class HomePage extends HomePageLocators {

    // Expected texts
    private static final String EXPECTED_WELCOME_TEXT = "Welcome to CP Insurance Associates";
    private static final String EXPECTED_USER_LABEL = "Admin User";

    // Expected URLs for Navigation Links (based on actual application URLs)
    private static final String URL_QUOTES = "/quotes";
    private static final String URL_POLICIES = "/policies";
    private static final String URL_INVOICES = "/invoices";
    private static final String URL_TRANSACTIONS = "/transactions";
    private static final String URL_REFUNDS = "/refunds";
    private static final String URL_AGENTS = "/agents";
    private static final String URL_USERS = "/users";
    private static final String URL_CARRIER = "/carrier";
    private static final String URL_INSURED = "/insured";
    private static final String URL_REPORTS = "/reports";
    private static final String URL_ANALYTICS = "/analytics";

    // Expected URLs for Dashboard Tiles (based on actual application URLs)
    private static final String URL_NEW_QUOTE = "/new_quote";

    // Constructor
    public HomePage(WebDriver driver) {
        super(driver);
        logger.info("HomePage initialized");
    }

    // ==================== Welcome Section Validation Methods ====================

    // Validate welcome text
    public boolean validateWelcomeText() {
        logger.info("Validating welcome text");
        String actualText = getWelcomeText();
        boolean isValid = actualText.contains(EXPECTED_WELCOME_TEXT);
        logger.info("Welcome text validation: {} (Expected: '{}', Actual: '{}')",
            isValid ? "PASSED" : "FAILED", EXPECTED_WELCOME_TEXT, actualText);
        return isValid;
    }

    // Validate date and time is displayed
    public boolean validateDateTimeDisplayed() {
        logger.info("Validating date and time is displayed");
        boolean isDisplayed = isDateTimeDisplayed();
        if (isDisplayed) {
            String dateTime = getDateTimeText();
            logger.info("Date/Time displayed: {}", dateTime);
        }
        return isDisplayed;
    }

    // Validate logged-in user label
    public boolean validateLoggedInUserLabel() {
        logger.info("Validating logged-in user label");
        String actualText = getLoggedInUserText();
        boolean isValid = actualText.contains(EXPECTED_USER_LABEL);
        logger.info("User label validation: {} (Expected: '{}', Actual: '{}')",
            isValid ? "PASSED" : "FAILED", EXPECTED_USER_LABEL, actualText);
        return isValid;
    }

    // Validate entire welcome section
    public boolean validateWelcomeSection() {
        logger.info("Validating entire welcome section");
        boolean welcomeValid = validateWelcomeText();
        boolean dateTimeValid = validateDateTimeDisplayed();
        boolean userLabelValid = validateLoggedInUserLabel();
        return welcomeValid && dateTimeValid && userLabelValid;
    }

    // ==================== Navigation Bar Validation Methods ====================

    // Validate all navigation links are displayed and clickable
    public boolean validateAllNavigationLinks() {
        logger.info("Validating all 11 navigation links");

        boolean allValid = true;

        // Quotes
        if (!isQuotesLinkDisplayed() || !isQuotesLinkClickable()) {
            logger.error("Quotes link validation FAILED");
            allValid = false;
        } else {
            logger.info("Quotes link: PASSED");
        }

        // Manage Policy
        if (!isManagePolicyLinkDisplayed() || !isManagePolicyLinkClickable()) {
            logger.error("Manage Policy link validation FAILED");
            allValid = false;
        } else {
            logger.info("Manage Policy link: PASSED");
        }

        // Invoices
        if (!isInvoicesLinkDisplayed() || !isInvoicesLinkClickable()) {
            logger.error("Invoices link validation FAILED");
            allValid = false;
        } else {
            logger.info("Invoices link: PASSED");
        }

        // Transactions
        if (!isTransactionsLinkDisplayed() || !isTransactionsLinkClickable()) {
            logger.error("Transactions link validation FAILED");
            allValid = false;
        } else {
            logger.info("Transactions link: PASSED");
        }

        // Refunds
        if (!isRefundsLinkDisplayed() || !isRefundsLinkClickable()) {
            logger.error("Refunds link validation FAILED");
            allValid = false;
        } else {
            logger.info("Refunds link: PASSED");
        }

        // Agents
        if (!isAgentsLinkDisplayed() || !isAgentsLinkClickable()) {
            logger.error("Agents link validation FAILED");
            allValid = false;
        } else {
            logger.info("Agents link: PASSED");
        }

        // Users
        if (!isUsersLinkDisplayed() || !isUsersLinkClickable()) {
            logger.error("Users link validation FAILED");
            allValid = false;
        } else {
            logger.info("Users link: PASSED");
        }

        // Carrier
        if (!isCarrierLinkDisplayed() || !isCarrierLinkClickable()) {
            logger.error("Carrier link validation FAILED");
            allValid = false;
        } else {
            logger.info("Carrier link: PASSED");
        }

        // Insured
        if (!isInsuredLinkDisplayed() || !isInsuredLinkClickable()) {
            logger.error("Insured link validation FAILED");
            allValid = false;
        } else {
            logger.info("Insured link: PASSED");
        }

        // Reports
        if (!isReportsLinkDisplayed() || !isReportsLinkClickable()) {
            logger.error("Reports link validation FAILED");
            allValid = false;
        } else {
            logger.info("Reports link: PASSED");
        }

        logger.info("Navigation links validation: {}", allValid ? "ALL PASSED" : "SOME FAILED");
        return allValid;
    }

    // Navigate to Quotes and verify
    public boolean navigateToQuotesAndVerify() {
        logger.info("Navigating to Quotes page");
        clickQuotes();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_QUOTES);
        logger.info("Quotes navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_QUOTES, actualUrl);
        captureScreenshotToReport("Quotes Page");
        return success;
    }

    // Navigate to Manage Policy and verify
    public boolean navigateToManagePolicyAndVerify() {
        logger.info("Navigating to Manage Policy page");
        clickManagePolicy();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_POLICIES);
        logger.info("Manage Policy navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_POLICIES, actualUrl);
        captureScreenshotToReport("Manage Policy Page");
        return success;
    }

    // Navigate to Invoices and verify
    public boolean navigateToInvoicesAndVerify() {
        logger.info("Navigating to Invoices page");
        clickInvoices();
        sleep(4000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_INVOICES);
        logger.info("Invoices navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_INVOICES, actualUrl);
        captureScreenshotToReport("Invoices Page");
        return success;
    }

    // Navigate to Transactions and verify
    public boolean navigateToTransactionsAndVerify() {
        logger.info("Navigating to Transactions page");
        clickTransactions();
        sleep(4000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_TRANSACTIONS);
        logger.info("Transactions navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_TRANSACTIONS, actualUrl);
        captureScreenshotToReport("Transactions Page");
        return success;
    }

    // Navigate to Refunds and verify
    public boolean navigateToRefundsAndVerify() {
        logger.info("Navigating to Refunds page");
        clickRefunds();
        sleep(4000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_REFUNDS);
        logger.info("Refunds navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_REFUNDS, actualUrl);
        captureScreenshotToReport("Refunds Page");
        return success;
    }

    // Navigate to Agents and verify
    public boolean navigateToAgentsAndVerify() {
        logger.info("Navigating to Agents page");
        clickAgents();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_AGENTS);
        logger.info("Agents navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_AGENTS, actualUrl);
        captureScreenshotToReport("Agents Page");
        return success;
    }

    // Navigate to Users and verify
    public boolean navigateToUsersAndVerify() {
        logger.info("Navigating to Users page");
        clickUsers();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_USERS);
        logger.info("Users navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_USERS, actualUrl);
        captureScreenshotToReport("Users Page");
        return success;
    }

    // Navigate to Carrier and verify
    public boolean navigateToCarrierAndVerify() {
        logger.info("Navigating to Carrier page");
        clickCarrier();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_CARRIER);
        logger.info("Carrier navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_CARRIER, actualUrl);
        captureScreenshotToReport("Carrier Page");
        return success;
    }

    // Navigate to Insured and verify
    public boolean navigateToInsuredAndVerify() {
        logger.info("Navigating to Insured page");
        clickInsured();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_INSURED);
        logger.info("Insured navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_INSURED, actualUrl);
        captureScreenshotToReport("Insured Page");
        return success;
    }

    // Navigate to Reports and verify
    public boolean navigateToReportsAndVerify() {
        logger.info("Navigating to Reports page");
        clickReports();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_REPORTS);
        logger.info("Reports navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_REPORTS, actualUrl);
        captureScreenshotToReport("Reports Page");
        return success;
    }

    // ==================== Dashboard Tiles Validation Methods ====================

    // Validate all dashboard tiles are visible and enabled
    public boolean validateAllDashboardTiles() {
        logger.info("Validating all 9 dashboard tiles");

        boolean allValid = true;

        // Create Quote
        if (!isCreateQuoteTileDisplayed() || !isCreateQuoteTileEnabled()) {
            logger.error("Create Quote tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Create Quote tile: PASSED");
        }

        // View Quotes
        if (!isViewQuotesTileDisplayed() || !isViewQuotesTileEnabled()) {
            logger.error("View Quotes tile validation FAILED");
            allValid = false;
        } else {
            logger.info("View Quotes tile: PASSED");
        }

        // Manage Policies
        if (!isManagePoliciesTileDisplayed() || !isManagePoliciesTileEnabled()) {
            logger.error("Manage Policies tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Manage Policies tile: PASSED");
        }

        // Manage Users
        if (!isManageUsersTileDisplayed() || !isManageUsersTileEnabled()) {
            logger.error("Manage Users tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Manage Users tile: PASSED");
        }

        // Manage Agents
        if (!isManageAgentsTileDisplayed() || !isManageAgentsTileEnabled()) {
            logger.error("Manage Agents tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Manage Agents tile: PASSED");
        }

        // View Carriers
        if (!isViewCarriersTileDisplayed() || !isViewCarriersTileEnabled()) {
            logger.error("View Carriers tile validation FAILED");
            allValid = false;
        } else {
            logger.info("View Carriers tile: PASSED");
        }

        // Invoices
        if (!isInvoicesTileDisplayed() || !isInvoicesTileEnabled()) {
            logger.error("Invoices tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Invoices tile: PASSED");
        }

        // Transactions
        if (!isTransactionsTileDisplayed() || !isTransactionsTileEnabled()) {
            logger.error("Transactions tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Transactions tile: PASSED");
        }

        // Refunds
        if (!isRefundsTileDisplayed() || !isRefundsTileEnabled()) {
            logger.error("Refunds tile validation FAILED");
            allValid = false;
        } else {
            logger.info("Refunds tile: PASSED");
        }

        logger.info("Dashboard tiles validation: {}", allValid ? "ALL PASSED" : "SOME FAILED");
        return allValid;
    }

    // Click Create Quote tile and verify navigation
    public boolean clickCreateQuoteAndVerify() {
        logger.info("Clicking Create Quote tile and verifying navigation");
        clickCreateQuoteTile();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_NEW_QUOTE);
        logger.info("Create Quote navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_NEW_QUOTE, actualUrl);
        captureScreenshotToReport("Create Quote Page");
        return success;
    }

    // Click View Quotes tile and verify navigation
    public boolean clickViewQuotesAndVerify() {
        logger.info("Clicking View Quotes tile and verifying navigation");
        clickViewQuotesTile();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_QUOTES);
        logger.info("View Quotes navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_QUOTES, actualUrl);
        captureScreenshotToReport("View Quotes Page");
        return success;
    }

    // Click Manage Policies tile and verify navigation
    public boolean clickManagePoliciesAndVerify() {
        logger.info("Clicking Manage Policies tile and verifying navigation");
        clickManagePoliciesTile();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_POLICIES);
        logger.info("Manage Policies navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_POLICIES, actualUrl);
        captureScreenshotToReport("Manage Policies Page");
        return success;
    }

    // Click Manage Users tile and verify navigation
    public boolean clickManageUsersAndVerify() {
        logger.info("Clicking Manage Users tile and verifying navigation");
        clickManageUsersTile();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_USERS);
        logger.info("Manage Users navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_USERS, actualUrl);
        captureScreenshotToReport("Manage Users Page");
        return success;
    }

    // Click Manage Agents tile and verify navigation
    public boolean clickManageAgentsAndVerify() {
        logger.info("Clicking Manage Agents tile and verifying navigation");
        clickManageAgentsTile();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_AGENTS);
        logger.info("Manage Agents navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_AGENTS, actualUrl);
        captureScreenshotToReport("Manage Agents Page");
        return success;
    }

    // Click View Carriers tile and verify navigation
    public boolean clickViewCarriersAndVerify() {
        logger.info("Clicking View Carriers tile and verifying navigation");
        clickViewCarriersTile();
        sleep(3000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_CARRIER);
        logger.info("View Carriers navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_CARRIER, actualUrl);
        captureScreenshotToReport("View Carriers Page");
        return success;
    }

    // Click Invoices tile and verify navigation
    public boolean clickInvoicesTileAndVerify() {
        logger.info("Clicking Invoices tile and verifying navigation");
        clickInvoicesTile();
        sleep(4000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_INVOICES);
        logger.info("Invoices navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_INVOICES, actualUrl);
        captureScreenshotToReport("Invoices Tile Page");
        return success;
    }

    // Click Transactions tile and verify navigation
    public boolean clickTransactionsTileAndVerify() {
        logger.info("Clicking Transactions tile and verifying navigation");
        clickTransactionsTile();
        sleep(4000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_TRANSACTIONS);
        logger.info("Transactions navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_TRANSACTIONS, actualUrl);
        captureScreenshotToReport("Transactions Tile Page");
        return success;
    }

    // Click Refunds tile and verify navigation
    public boolean clickRefundsTileAndVerify() {
        logger.info("Clicking Refunds tile and verifying navigation");
        clickRefundsTile();
        sleep(4000);
        String actualUrl = getCurrentUrl();
        boolean success = actualUrl.contains(URL_REFUNDS);
        logger.info("Refunds navigation: {} (Expected URL contains: '{}', Actual URL: '{}')",
            success ? "SUCCESS" : "FAILED", URL_REFUNDS, actualUrl);
        captureScreenshotToReport("Refunds Tile Page");
        return success;
    }

    // ==================== Page State Methods ====================

    // Check if on home page
    public boolean isOnHomePage() {
        String url = getCurrentUrl();
        return url.contains("/home") || url.contains("/dashboard") || url.endsWith("/");
    }

    // Wait for home page to load
    public void waitForHomePageLoad() {
        logger.info("Waiting for home page to load");
        waitForVisibility(welcomeText);
    }

    // Navigate back to home page
    public void goBackToHomePage() {
        logger.info("Navigating back to home page");
        navigateBack();
        sleep(1000);
        waitForHomePageLoad();
    }

    // Note: Common methods (sleep, getCurrentUrl, clearAndType) are inherited from BasePage
}
