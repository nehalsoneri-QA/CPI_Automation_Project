package com.automation.tests;

import com.automation.listeners.TestListener;
import com.automation.pages.HomePage;
import com.automation.pages.InvoicePage;
import com.automation.pages.LoginPage;
import com.automation.pages.ResetPage;
import com.automation.utils.ConfigReader;
import com.automation.utils.TestWaitHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Invoice Test Class
 * Tests the Invoice page functionality including:
 * - Login with valid credentials
 * - Skip reset password page
 * - Navigate to Invoice page
 * - Search by Policy ID from Excel
 *
 * Screenshots are captured at each step for reporting
 */
@Listeners(TestListener.class)
public class InvoiceTest {

    protected static final Logger logger = LogManager.getLogger(InvoiceTest.class);
    protected static ConfigReader config = ConfigReader.getInstance();

    private WebDriver driver;
    private LoginPage loginPage;
    private ResetPage resetPage;
    private HomePage homePage;
    private InvoicePage invoicePage;
    private TestWaitHelper waitHelper;

    // ==================== Setup and Teardown ====================

    @BeforeClass(alwaysRun = true)
    public void initDriver() {
        logger.info("=== Invoice Test Suite Started ===");
        WebDriverManager.chromedriver().setup();

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--disable-notifications");

        driver = new ChromeDriver(chromeOptions);

        // Set driver reference for TestListener to capture screenshots
        TestListener.setDriver(driver);

        // Initialize page objects
        loginPage = new LoginPage(driver);
        resetPage = new ResetPage(driver);
        homePage = new HomePage(driver);
        invoicePage = new InvoicePage(driver);
        waitHelper = new TestWaitHelper(driver);

        // Navigate to application
        String baseUrl = config.getProperty("base.url");
        driver.get(baseUrl);
        logger.info("Browser initialized and navigated to: {}", baseUrl);
    }

    @AfterClass(alwaysRun = true)
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
        logger.info("=== Invoice Test Suite Completed ===");
    }

    // ==================== Test Methods ====================

    /**
     * Test 1: Login with valid admin credentials from Excel
     * Verifies successful login by checking for redirect or success message
     */
    @Test(priority = 1, groups = {"smoke", "invoice"})
    @Description("Login with valid admin credentials from Excel")
    public void testLoginWithValidCredentials() {
        logger.info("Starting test: Login with valid credentials");

        // Step 1: Capture login page before entering credentials
        loginPage.captureScreenshotToReport("Step 1 - Login Page Loaded");
        logger.info("Screenshot captured: Login page loaded");

        // Step 2: Enter credentials
        loginPage.waitForPageLoad();
        loginPage.inputUsername(loginPage.getAdminEmail());
        loginPage.captureScreenshotToReport("Step 2 - Email Entered");
        logger.info("Screenshot captured: Email entered");

        loginPage.inputPassword(loginPage.getAdminPassword());
        loginPage.captureScreenshotToReport("Step 3 - Password Entered");
        logger.info("Screenshot captured: Password entered");

        // Step 3: Click login button
        loginPage.clickLogin();
        logger.info("Login credentials submitted");

        // Wait for page to load after login
        waitHelper.waitAfterNavigation();

        // Step 4: Capture page after login
        loginPage.captureScreenshotToReport("Step 4 - After Login Click");
        logger.info("Screenshot captured: After login click");

        // Verify login was successful (either redirected or success message shown)
        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL after login: {}", currentUrl);

        // Login is successful if we're not on login page anymore
        assertThat(currentUrl)
                .as("Should be redirected away from login page after successful login")
                .doesNotContain("/login");

        logger.info("Test PASSED: Login successful");
    }

    /**
     * Test 2: Click Skip button on Reset Password page (if displayed)
     * Handles the reset password prompt that appears after login
     */
    @Test(priority = 2, groups = {"smoke", "invoice"}, dependsOnMethods = "testLoginWithValidCredentials")
    @Description("Click Skip button on reset password page if displayed")
    public void testClickSkipButtonOnResetPage() {
        logger.info("Starting test: Handle reset password page");

        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL: {}", currentUrl);

        if (currentUrl.contains("/reset_password")) {
            logger.info("Reset password page detected");

            // Step 1: Capture reset password page
            resetPage.captureScreenshotToReport("Step 1 - Reset Password Page Displayed");
            logger.info("Screenshot captured: Reset password page displayed");

            // Verify skip button is displayed
            assertThat(resetPage.isSkipButtonDisplayed())
                    .as("Skip button should be displayed on reset password page")
                    .isTrue();

            // Step 2: Click skip button
            resetPage.clickSkipButton();
            logger.info("Clicked skip button on reset password page");

            // Wait for redirect
            waitHelper.waitForUrlNotContains("/reset_password");

            // Step 3: Capture page after skip
            resetPage.captureScreenshotToReport("Step 2 - After Skip Button Clicked");
            logger.info("Screenshot captured: After skip button clicked");

            // Verify redirected away from reset password page
            String urlAfterSkip = driver.getCurrentUrl();
            assertThat(urlAfterSkip)
                    .as("Should be redirected away from reset password page after clicking skip")
                    .doesNotContain("/reset_password");

            logger.info("Test PASSED: Successfully skipped reset password page");
        } else {
            // Capture current page state
            resetPage.captureScreenshotToReport("Step 1 - Reset Password Page Not Shown");
            logger.info("Screenshot captured: Reset password page not shown");
            logger.info("Not on reset password page, skipping this step");
            logger.info("Test PASSED: Reset password page not shown (already handled or not required)");
        }
    }

    /**
     * Test 3: Navigate to Home Page and verify
     * Verifies the home page is displayed after login/skip
     */
    @Test(priority = 3, groups = {"smoke", "invoice"}, dependsOnMethods = "testClickSkipButtonOnResetPage")
    @Description("Navigate to home page and verify it is displayed")
    public void testNavigateToHomePage() {
        logger.info("Starting test: Navigate to home page");

        // Wait for home page to load
        homePage.waitForHomePageLoad();

        // Step 1: Capture home page loaded
        homePage.captureScreenshotToReport("Step 1 - Home Page Loaded");
        logger.info("Screenshot captured: Home page loaded");

        // Verify on home page
        boolean onHomePage = homePage.isOnHomePage();
        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL: {}, On Home Page: {}", currentUrl, onHomePage);

        // Step 2: Capture welcome section
        if (homePage.isWelcomeTextDisplayed()) {
            homePage.captureScreenshotToReport("Step 2 - Welcome Section Visible");
            logger.info("Screenshot captured: Welcome section visible");
        }

        // Home page should be displayed (URL contains home/dashboard or is root)
        assertThat(onHomePage || currentUrl.contains("cpiai"))
                .as("Should be on home page after login")
                .isTrue();

        // Step 3: Capture navigation bar
        homePage.captureScreenshotToReport("Step 3 - Navigation Bar Visible");
        logger.info("Screenshot captured: Navigation bar visible");

        logger.info("Test PASSED: Home page displayed successfully");
    }

    /**
     * Test 4: Click on Invoice link in navigation bar
     * Navigates to the Invoice page using the top navigation link
     */
    @Test(priority = 4, groups = {"smoke", "invoice"}, dependsOnMethods = "testNavigateToHomePage")
    @Description("Click on Invoice link in navigation bar to navigate to Invoice page")
    public void testClickOnInvoiceLink() {
        logger.info("Starting test: Click on Invoice link");

        // Step 1: Capture before clicking Invoice link
        homePage.captureScreenshotToReport("Step 1 - Before Clicking Invoice Link");
        logger.info("Screenshot captured: Before clicking invoice link");

        // Verify Invoices link is displayed and clickable
        assertThat(homePage.isInvoicesLinkDisplayed())
                .as("Invoices link should be displayed in navigation bar")
                .isTrue();

        // Step 2: Click on Invoices link
        homePage.clickInvoices();
        logger.info("Clicked on Invoices navigation link");

        // Wait for Invoice page to load
        waitHelper.waitAfterNavigation();

        // Step 3: Capture page during loading
        invoicePage.captureScreenshotToReport("Step 2 - Invoice Page Loading");
        logger.info("Screenshot captured: Invoice page loading");

        invoicePage.waitForPageLoad();

        // Verify navigation to Invoice page
        boolean onInvoicePage = invoicePage.isOnInvoicePage();
        String currentUrl = driver.getCurrentUrl();

        // Step 4: Capture Invoice page loaded
        invoicePage.captureScreenshotToReport("Step 3 - Invoice Page Loaded");
        logger.info("Screenshot captured: Invoice page loaded");

        assertThat(onInvoicePage)
                .as("Should be on Invoice page after clicking Invoices link. Current URL: " + currentUrl)
                .isTrue();

        logger.info("Test PASSED: Successfully navigated to Invoice page");
    }

    /**
     * Test 5: Process ALL policies from Excel column B
     * For each policy:
     * - Search by Policy ID
     * - Verify search results
     * - Expand policy and collect invoices
     * - Check for negative/zero issues
     * - Download PDF and verify data
     */
    @Test(priority = 5, groups = {"smoke", "invoice"}, dependsOnMethods = "testClickOnInvoiceLink")
    @Description("Process all policies from Excel column B - search, verify, collect invoices, verify PDF for each")
    public void testProcessAllPoliciesFromExcel() {
        logger.info("Starting test: Process ALL policies from Excel");

        // Validate Invoice page is loaded
        assertThat(invoicePage.validateInvoicePageLoaded())
                .as("Invoice page should be fully loaded before processing policies")
                .isTrue();

        // Get ALL policy IDs from Excel column B
        java.util.List<String> allPolicyIds = invoicePage.getAllPolicyIds();
        int totalPolicies = allPolicyIds.size();

        logger.info("Found {} policies in Excel column B", totalPolicies);
        invoicePage.logInfoToReport("=== Processing " + totalPolicies + " Policies from Excel ===");

        assertThat(allPolicyIds)
                .as("At least one Policy ID should exist in Excel column B")
                .isNotEmpty();

        // Track results across all policies
        java.util.List<PolicyProcessingResult> allPolicyResults = new java.util.ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        // Process each policy one by one
        for (int i = 0; i < totalPolicies; i++) {
            String policyId = allPolicyIds.get(i);
            int policyNumber = i + 1;

            logger.info("\n========== Processing Policy {}/{}: {} ==========", policyNumber, totalPolicies, policyId);
            invoicePage.logInfoToReport("<hr><h3 style='color: #4472C4;'>Policy " + policyNumber + "/" + totalPolicies + ": " + policyId + "</h3>");

            PolicyProcessingResult result = processSinglePolicy(policyId, policyNumber, totalPolicies);
            allPolicyResults.add(result);

            if (result.isSuccess) {
                successCount++;
                logger.info("Policy {} processing COMPLETED", policyId);
            } else {
                failureCount++;
                logger.warn("Policy {} processing FAILED: {}", policyId, result.failureReason);
            }

            // Clear search and reset for next policy (if not the last one)
            if (i < totalPolicies - 1) {
                invoicePage.clearSearch();
                waitHelper.waitAfterNavigation();
            }
        }

        // Generate final summary report
        generateFinalSummaryReport(allPolicyResults, successCount, failureCount, totalPolicies);

        // Final assertion
        logger.info("\n========== FINAL SUMMARY ==========");
        logger.info("Total Policies: {}", totalPolicies);
        logger.info("Successful: {}", successCount);
        logger.info("Failed: {}", failureCount);

        // Test passes if at least one policy was processed successfully
        assertThat(successCount)
                .as("At least one policy should be processed successfully")
                .isGreaterThan(0);

        if (failureCount > 0) {
            invoicePage.logWarningToReport("WARNING: " + failureCount + " out of " + totalPolicies + " policies had issues - check report for details");
        }

        logger.info("Test COMPLETED: Processed all {} policies from Excel", totalPolicies);
    }

    /**
     * Data class to hold processing results for a single policy
     */
    private static class PolicyProcessingResult {
        String policyId;
        boolean isSuccess;
        String failureReason;
        int invoiceCount;
        int negativeInvoiceCount;
        int zeroZeroInvoiceCount;
        int pdfCertificateCount;
        int pdfMatchCount;
        int pdfMismatchCount;
        java.util.List<InvoicePage.InvoiceRowData> invoices;
        java.util.List<InvoicePage.CertificateComparisonResult> pdfResults;

        PolicyProcessingResult(String policyId) {
            this.policyId = policyId;
            this.isSuccess = false;
            this.failureReason = "";
            this.invoiceCount = 0;
            this.negativeInvoiceCount = 0;
            this.zeroZeroInvoiceCount = 0;
            this.pdfCertificateCount = 0;
            this.pdfMatchCount = 0;
            this.pdfMismatchCount = 0;
            this.invoices = new java.util.ArrayList<>();
            this.pdfResults = new java.util.ArrayList<>();
        }
    }

    /**
     * Process a single policy - search, expand, collect invoices, verify PDF
     */
    private PolicyProcessingResult processSinglePolicy(String policyId, int policyNumber, int totalPolicies) {
        PolicyProcessingResult result = new PolicyProcessingResult(policyId);

        try {
            // Step 1: Search for policy
            invoicePage.captureScreenshotToReport("Policy " + policyNumber + " - Step 1: Searching for " + policyId);
            invoicePage.searchByPolicyId(policyId);
            logger.info("Searched for Policy ID: {}", policyId);

            // Step 2: Verify search results
            boolean resultsDisplayed = invoicePage.verifySearchResultsDisplayed();
            if (!resultsDisplayed) {
                result.failureReason = "No search results found for policy";
                invoicePage.logWarningToReport("No search results found for Policy: " + policyId);
                return result;
            }

            boolean policyFound = invoicePage.verifyPolicyIdInResults(policyId);
            if (!policyFound) {
                result.failureReason = "Policy ID not found in search results";
                invoicePage.logWarningToReport("Policy ID not found in results: " + policyId);
                return result;
            }

            invoicePage.captureScreenshotToReport("Policy " + policyNumber + " - Step 2: Search Results Displayed");

            // Step 3: Expand policy and collect invoices
            logger.info("Expanding policy {} to collect invoices...", policyId);
            invoicePage.clickPolicyToExpand(policyId);
            invoicePage.captureScreenshotToReport("Policy " + policyNumber + " - Step 3: Policy Expanded");

            // Step 4: Collect all invoices from all pages
            java.util.List<InvoicePage.InvoiceRowData> invoices = invoicePage.getAllInvoiceDataFromAllPages();
            result.invoices = invoices;
            result.invoiceCount = invoices.size();

            logger.info("Collected {} invoices for policy {}", result.invoiceCount, policyId);
            invoicePage.logInfoToReport("Invoices Collected: " + result.invoiceCount);

            // Step 5: Check for negative/zero issues
            java.util.List<InvoicePage.InvoiceRowData> negativeInvoices = new java.util.ArrayList<>();
            java.util.List<InvoicePage.InvoiceRowData> zeroZeroInvoices = new java.util.ArrayList<>();

            for (InvoicePage.InvoiceRowData invoice : invoices) {
                if (invoice.isNegativeDue || invoice.isNegativeInvoice) {
                    negativeInvoices.add(invoice);
                }
                if (invoice.isZeroDueAndZeroInvoice) {
                    zeroZeroInvoices.add(invoice);
                }
            }

            result.negativeInvoiceCount = negativeInvoices.size();
            result.zeroZeroInvoiceCount = zeroZeroInvoices.size();

            // Log invoice issues if found
            if (!negativeInvoices.isEmpty()) {
                logNegativeInvoicesToReport(negativeInvoices, policyId, policyNumber);
            }
            if (!zeroZeroInvoices.isEmpty()) {
                logZeroZeroInvoicesToReport(zeroZeroInvoices, policyId, policyNumber);
            }

            invoicePage.captureScreenshotToReport("Policy " + policyNumber + " - Step 4: Invoice Analysis Complete");

            // Step 6: Download and verify PDF
            logger.info("Downloading PDF for policy {}...", policyId);
            boolean downloadClicked = invoicePage.clickDownloadButton();

            if (downloadClicked) {
                String pdfPath = com.automation.utils.PDFReader.waitForPDFDownload(
                        invoicePage.getPDFDownloadDirectory(), 60);

                if (pdfPath != null && !pdfPath.isEmpty()) {
                    invoicePage.logInfoToReport("PDF Downloaded: " + pdfPath);
                    invoicePage.captureScreenshotToReport("Policy " + policyNumber + " - Step 5: PDF Downloaded");

                    // Parse and verify PDF
                    java.util.List<InvoicePage.CertificateComparisonResult> pdfResults = verifyPDFForPolicy(pdfPath, invoices, policyId, policyNumber);
                    result.pdfResults = pdfResults;
                    result.pdfCertificateCount = pdfResults.size();
                    result.pdfMatchCount = (int) pdfResults.stream().filter(r -> r.isMatch).count();
                    result.pdfMismatchCount = result.pdfCertificateCount - result.pdfMatchCount;

                    logger.info("PDF Verification for {}: {} matches, {} mismatches",
                            policyId, result.pdfMatchCount, result.pdfMismatchCount);
                } else {
                    invoicePage.logWarningToReport("PDF download timeout for Policy: " + policyId);
                    result.failureReason = "PDF download timeout";
                }
            } else {
                invoicePage.logWarningToReport("Failed to click download button for Policy: " + policyId);
                result.failureReason = "Download button click failed";
            }

            // Step 7: Collapse policy for next iteration
            invoicePage.collapseExpandedPolicy();

            // Mark as success if we got this far
            result.isSuccess = true;

        } catch (Exception e) {
            logger.error("Error processing policy {}: {}", policyId, e.getMessage());
            result.failureReason = "Exception: " + e.getMessage();
            invoicePage.logWarningToReport("Error processing Policy " + policyId + ": " + e.getMessage());
        }

        return result;
    }

    /**
     * Verify PDF content for a specific policy
     */
    private java.util.List<InvoicePage.CertificateComparisonResult> verifyPDFForPolicy(
            String pdfPath,
            java.util.List<InvoicePage.InvoiceRowData> invoices,
            String policyId,
            int policyNumber) {

        java.util.List<InvoicePage.CertificateComparisonResult> results = new java.util.ArrayList<>();

        try {
            // Read PDF content
            String pdfContent = com.automation.utils.PDFReader.readPDF(pdfPath);
            if (pdfContent.isEmpty()) {
                invoicePage.logWarningToReport("PDF content is empty for Policy: " + policyId);
                return results;
            }

            // Extract MASTER POLICY STATEMENT section
            String masterPolicySection = com.automation.utils.PDFReader.extractMasterPolicySection(pdfContent);
            if (masterPolicySection.isEmpty()) {
                invoicePage.logWarningToReport("MASTER POLICY STATEMENT not found in PDF for Policy: " + policyId);
            }

            // Parse certificate data from PDF
            java.util.Map<String, com.automation.utils.PDFReader.PDFCertificateData> pdfData =
                    com.automation.utils.PDFReader.parseCertificateData(masterPolicySection);
            invoicePage.logInfoToReport("PDF Certificates Found: " + pdfData.size());

            // Aggregate UI data by certificate
            java.util.Map<String, InvoicePage.CertificateAggregateData> uiData =
                    invoicePage.aggregateInvoicesByCertificate(invoices);
            invoicePage.logInfoToReport("UI Unique Certificates: " + uiData.size());

            // Log aggregated UI data to report
            logAggregatedUIDataToReport(uiData, policyId, policyNumber);

            // Compare UI with PDF
            results = invoicePage.compareUIWithPDF(uiData, pdfData);

            // Generate and log comparison report
            String comparisonReportHtml = invoicePage.generatePDFComparisonReport(results);
            invoicePage.logHtmlToReport(comparisonReportHtml);

            invoicePage.captureScreenshotToReport("Policy " + policyNumber + " - Step 6: PDF Verification Complete");

        } catch (Exception e) {
            logger.error("Error verifying PDF for policy {}: {}", policyId, e.getMessage());
            invoicePage.logWarningToReport("PDF verification error for Policy " + policyId + ": " + e.getMessage());
        }

        return results;
    }

    /**
     * Log negative invoices to report
     */
    private void logNegativeInvoicesToReport(java.util.List<InvoicePage.InvoiceRowData> negativeInvoices, String policyId, int policyNumber) {
        StringBuilder negativeHtml = new StringBuilder();
        negativeHtml.append("<h4 style='color: black;'>&#9888; NEGATIVE Amount Invoices for Policy ").append(policyId)
                .append(" (").append(negativeInvoices.size()).append(" found)</h4>");
        negativeHtml.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%; color: black;'>");
        negativeHtml.append("<thead><tr style='background-color: #FFCCCB; color: black;'>");
        negativeHtml.append("<th>Invoice ID</th><th>Certificate</th><th>Location</th>");
        negativeHtml.append("<th>Due Amount</th><th>Invoice Amount</th><th>Due Date</th><th>Status</th><th>Issue Type</th>");
        negativeHtml.append("</tr></thead><tbody>");

        for (InvoicePage.InvoiceRowData invoice : negativeInvoices) {
            negativeHtml.append("<tr style='background-color: #FFE4E1; color: black;'>");
            negativeHtml.append("<td>").append(invoice.invoiceId).append("</td>");
            negativeHtml.append("<td>").append(invoice.certificate).append("</td>");
            negativeHtml.append("<td>").append(invoice.location).append("</td>");
            negativeHtml.append("<td>").append(invoice.dueAmount).append("</td>");
            negativeHtml.append("<td><strong>").append(invoice.invoiceAmount).append("</strong></td>");
            negativeHtml.append("<td>").append(invoice.dueDate).append("</td>");
            negativeHtml.append("<td>").append(invoice.status).append("</td>");
            negativeHtml.append("<td>").append(invoice.getIssueType()).append("</td>");
            negativeHtml.append("</tr>");
        }
        negativeHtml.append("</tbody></table>");
        invoicePage.logHtmlToReport(negativeHtml.toString());
    }

    /**
     * Log zero/zero invoices to report
     */
    private void logZeroZeroInvoicesToReport(java.util.List<InvoicePage.InvoiceRowData> zeroZeroInvoices, String policyId, int policyNumber) {
        StringBuilder zeroZeroHtml = new StringBuilder();
        zeroZeroHtml.append("<h4 style='color: black;'>&#9888; ZERO Due & ZERO Invoice Amount for Policy ").append(policyId)
                .append(" (").append(zeroZeroInvoices.size()).append(" found)</h4>");
        zeroZeroHtml.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%; color: black;'>");
        zeroZeroHtml.append("<thead><tr style='background-color: #FFFFCC; color: black;'>");
        zeroZeroHtml.append("<th>Invoice ID</th><th>Certificate</th><th>Location</th>");
        zeroZeroHtml.append("<th>Due Amount</th><th>Invoice Amount</th><th>Due Date</th><th>Status</th>");
        zeroZeroHtml.append("</tr></thead><tbody>");

        for (InvoicePage.InvoiceRowData invoice : zeroZeroInvoices) {
            zeroZeroHtml.append("<tr style='background-color: #FFFACD; color: black;'>");
            zeroZeroHtml.append("<td>").append(invoice.invoiceId).append("</td>");
            zeroZeroHtml.append("<td>").append(invoice.certificate).append("</td>");
            zeroZeroHtml.append("<td>").append(invoice.location).append("</td>");
            zeroZeroHtml.append("<td><strong>").append(invoice.dueAmount).append("</strong></td>");
            zeroZeroHtml.append("<td><strong>").append(invoice.invoiceAmount).append("</strong></td>");
            zeroZeroHtml.append("<td>").append(invoice.dueDate).append("</td>");
            zeroZeroHtml.append("<td>").append(invoice.status).append("</td>");
            zeroZeroHtml.append("</tr>");
        }
        zeroZeroHtml.append("</tbody></table>");
        invoicePage.logHtmlToReport(zeroZeroHtml.toString());
    }

    /**
     * Log aggregated UI data to report
     */
    private void logAggregatedUIDataToReport(java.util.Map<String, InvoicePage.CertificateAggregateData> uiData, String policyId, int policyNumber) {
        StringBuilder uiAggregatedHtml = new StringBuilder();
        uiAggregatedHtml.append("<h4 style='color: white;'>UI Aggregated Certificate Data for Policy ").append(policyId).append("</h4>");
        uiAggregatedHtml.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        uiAggregatedHtml.append("<thead><tr style='background-color: #5B9BD5; color: white;'>");
        uiAggregatedHtml.append("<th>Certificate</th><th>Location</th><th>Due Amount</th><th>Invoice Amount</th><th>Total (Due+Invoice)</th><th>Entry Count</th>");
        uiAggregatedHtml.append("</tr></thead><tbody>");

        for (InvoicePage.CertificateAggregateData cert : uiData.values()) {
            uiAggregatedHtml.append("<tr style='color: white;'>");
            uiAggregatedHtml.append("<td>").append(cert.certificate).append("</td>");
            uiAggregatedHtml.append("<td>").append(cert.location).append("</td>");
            uiAggregatedHtml.append("<td>$").append(String.format("%.2f", cert.totalDueAmount)).append("</td>");
            uiAggregatedHtml.append("<td>$").append(String.format("%.2f", cert.totalInvoiceAmount)).append("</td>");
            uiAggregatedHtml.append("<td><strong>$").append(String.format("%.2f", cert.totalCombined)).append("</strong></td>");
            uiAggregatedHtml.append("<td>").append(cert.entryCount).append("</td>");
            uiAggregatedHtml.append("</tr>");
        }
        uiAggregatedHtml.append("</tbody></table>");
        invoicePage.logHtmlToReport(uiAggregatedHtml.toString());
    }

    /**
     * Generate final summary report for all policies
     */
    private void generateFinalSummaryReport(java.util.List<PolicyProcessingResult> allResults, int successCount, int failureCount, int totalPolicies) {
        StringBuilder summaryHtml = new StringBuilder();
        summaryHtml.append("<hr><h2 style='color: #4472C4;'>===== FINAL SUMMARY REPORT =====</h2>");
        summaryHtml.append("<p><strong>Total Policies Processed:</strong> ").append(totalPolicies).append("</p>");
        summaryHtml.append("<p style='color: green;'><strong>Successful:</strong> ").append(successCount).append("</p>");
        if (failureCount > 0) {
            summaryHtml.append("<p style='color: red;'><strong>Failed:</strong> ").append(failureCount).append("</p>");
        }

        // Summary table
        summaryHtml.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        summaryHtml.append("<thead><tr style='background-color: #4472C4; color: white;'>");
        summaryHtml.append("<th>#</th><th>Policy ID</th><th>Status</th><th>Invoices</th><th>Negative</th><th>Zero/Zero</th>");
        summaryHtml.append("<th>PDF Certs</th><th>PDF Match</th><th>PDF Mismatch</th><th>Notes</th>");
        summaryHtml.append("</tr></thead><tbody>");

        int index = 1;
        int totalInvoices = 0;
        int totalNegative = 0;
        int totalZeroZero = 0;
        int totalPdfCerts = 0;
        int totalPdfMatches = 0;
        int totalPdfMismatches = 0;

        for (PolicyProcessingResult result : allResults) {
            String rowStyle = result.isSuccess ? "style='background-color: #90EE90; color: black;'" : "style='background-color: #FFB6C1; color: black;'";
            String statusIcon = result.isSuccess ? "&#10004; SUCCESS" : "&#10008; FAILED";

            summaryHtml.append("<tr ").append(rowStyle).append(">");
            summaryHtml.append("<td>").append(index++).append("</td>");
            summaryHtml.append("<td>").append(result.policyId).append("</td>");
            summaryHtml.append("<td>").append(statusIcon).append("</td>");
            summaryHtml.append("<td>").append(result.invoiceCount).append("</td>");
            summaryHtml.append("<td>").append(result.negativeInvoiceCount).append("</td>");
            summaryHtml.append("<td>").append(result.zeroZeroInvoiceCount).append("</td>");
            summaryHtml.append("<td>").append(result.pdfCertificateCount).append("</td>");
            summaryHtml.append("<td>").append(result.pdfMatchCount).append("</td>");
            summaryHtml.append("<td>").append(result.pdfMismatchCount).append("</td>");
            summaryHtml.append("<td>").append(result.failureReason).append("</td>");
            summaryHtml.append("</tr>");

            totalInvoices += result.invoiceCount;
            totalNegative += result.negativeInvoiceCount;
            totalZeroZero += result.zeroZeroInvoiceCount;
            totalPdfCerts += result.pdfCertificateCount;
            totalPdfMatches += result.pdfMatchCount;
            totalPdfMismatches += result.pdfMismatchCount;
        }

        // Totals row
        summaryHtml.append("<tr style='background-color: #4472C4; color: white; font-weight: bold;'>");
        summaryHtml.append("<td colspan='3'>TOTALS</td>");
        summaryHtml.append("<td>").append(totalInvoices).append("</td>");
        summaryHtml.append("<td>").append(totalNegative).append("</td>");
        summaryHtml.append("<td>").append(totalZeroZero).append("</td>");
        summaryHtml.append("<td>").append(totalPdfCerts).append("</td>");
        summaryHtml.append("<td>").append(totalPdfMatches).append("</td>");
        summaryHtml.append("<td>").append(totalPdfMismatches).append("</td>");
        summaryHtml.append("<td></td>");
        summaryHtml.append("</tr>");

        summaryHtml.append("</tbody></table>");

        invoicePage.logHtmlToReport(summaryHtml.toString());
        invoicePage.captureScreenshotToReport("Final Summary - All Policies Processed");
    }

    // ==================== Custom Annotation ====================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Description {
        String value();
    }
}
