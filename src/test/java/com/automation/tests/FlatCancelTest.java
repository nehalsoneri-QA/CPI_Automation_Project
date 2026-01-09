package com.automation.tests;

import com.automation.listeners.TestListener;
import com.automation.pages.FlatCancelPage;
import com.automation.pages.HomePage;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flat Cancel Test Class
 * Tests the Flat Cancel functionality including:
 * - Login with valid credentials
 * - Skip reset password page
 * - Navigate to Home Page
 * - Click Manage Policy link
 * - Search for Policy ID from Excel
 * - Click View Policy button
 * - Verify Master Policy Details page
 * - Click Flat Cancel button
 * - Verify Cancel Address page
 * - Select certificates from Excel in Locations Details table
 */
@Listeners(TestListener.class)
public class FlatCancelTest {

    protected static final Logger logger = LogManager.getLogger(FlatCancelTest.class);
    protected static ConfigReader config = ConfigReader.getInstance();

    private WebDriver driver;
    private LoginPage loginPage;
    private ResetPage resetPage;
    private HomePage homePage;
    private FlatCancelPage flatCancelPage;
    private TestWaitHelper waitHelper;

    // ==================== Setup and Teardown ====================

    @BeforeClass(alwaysRun = true)
    public void initDriver() {
        logger.info("=== Flat Cancel Test Suite Started ===");
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
        flatCancelPage = new FlatCancelPage(driver);
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
        logger.info("=== Flat Cancel Test Suite Completed ===");
    }

    // ==================== Test Methods ====================

    /**
     * Test 1: Login with valid admin credentials from Excel
     */
    @Test(priority = 1, groups = {"smoke", "flatcancel"})
    @Description("Login with valid admin credentials from Excel")
    public void testLoginWithValidCredentials() {
        logger.info("Starting test: Login with valid credentials");

        loginPage.captureScreenshotToReport("Step 1 - Login Page Loaded");

        loginPage.waitForPageLoad();
        loginPage.inputUsername(loginPage.getAdminEmail());
        loginPage.captureScreenshotToReport("Step 2 - Email Entered");

        loginPage.inputPassword(loginPage.getAdminPassword());
        loginPage.captureScreenshotToReport("Step 3 - Password Entered");

        loginPage.clickLogin();
        logger.info("Login credentials submitted");

        waitHelper.waitAfterNavigation();

        loginPage.captureScreenshotToReport("Step 4 - After Login Click");

        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL after login: {}", currentUrl);

        assertThat(currentUrl)
                .as("Should be redirected away from login page after successful login")
                .doesNotContain("/login");

        logger.info("Test PASSED: Login successful");
    }

    /**
     * Test 2: Click Skip button on Reset Password page (if displayed)
     */
    @Test(priority = 2, groups = {"smoke", "flatcancel"}, dependsOnMethods = "testLoginWithValidCredentials")
    @Description("Click Skip button on reset password page if displayed")
    public void testClickSkipButtonOnResetPage() {
        logger.info("Starting test: Handle reset password page");

        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL: {}", currentUrl);

        if (currentUrl.contains("/reset_password")) {
            logger.info("Reset password page detected");

            resetPage.captureScreenshotToReport("Step 1 - Reset Password Page Displayed");

            assertThat(resetPage.isSkipButtonDisplayed())
                    .as("Skip button should be displayed on reset password page")
                    .isTrue();

            resetPage.clickSkipButton();
            logger.info("Clicked skip button on reset password page");

            waitHelper.waitForUrlNotContains("/reset_password");

            resetPage.captureScreenshotToReport("Step 2 - After Skip Button Clicked");

            String urlAfterSkip = driver.getCurrentUrl();
            assertThat(urlAfterSkip)
                    .as("Should be redirected away from reset password page after clicking skip")
                    .doesNotContain("/reset_password");

            logger.info("Test PASSED: Successfully skipped reset password page");
        } else {
            resetPage.captureScreenshotToReport("Step 1 - Reset Password Page Not Shown");
            logger.info("Not on reset password page, skipping this step");
            logger.info("Test PASSED: Reset password page not shown");
        }
    }

    /**
     * Test 3: Navigate to Home Page and verify
     */
    @Test(priority = 3, groups = {"smoke", "flatcancel"}, dependsOnMethods = "testClickSkipButtonOnResetPage")
    @Description("Navigate to home page and verify it is displayed")
    public void testNavigateToHomePage() {
        logger.info("Starting test: Navigate to home page");

        homePage.waitForHomePageLoad();

        homePage.captureScreenshotToReport("Step 1 - Home Page Loaded");

        boolean onHomePage = homePage.isOnHomePage();
        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL: {}, On Home Page: {}", currentUrl, onHomePage);

        if (homePage.isWelcomeTextDisplayed()) {
            homePage.captureScreenshotToReport("Step 2 - Welcome Section Visible");
        }

        assertThat(onHomePage || currentUrl.contains("cpiai"))
                .as("Should be on home page after login")
                .isTrue();

        homePage.captureScreenshotToReport("Step 3 - Navigation Bar Visible");

        logger.info("Test PASSED: Home page displayed successfully");
    }

    /**
     * Test 4: Click on Manage Policy link in navigation bar
     */
    @Test(priority = 4, groups = {"smoke", "flatcancel"}, dependsOnMethods = "testNavigateToHomePage")
    @Description("Click on Manage Policy link in navigation bar")
    public void testClickOnManagePolicyLink() {
        logger.info("Starting test: Click on Manage Policy link");

        homePage.captureScreenshotToReport("Step 1 - Before Clicking Manage Policy Link");

        assertThat(homePage.isManagePolicyLinkDisplayed())
                .as("Manage Policy link should be displayed in navigation bar")
                .isTrue();

        homePage.clickManagePolicy();
        logger.info("Clicked on Manage Policy navigation link");

        waitHelper.waitAfterNavigation();

        flatCancelPage.captureScreenshotToReport("Step 2 - Manage Policy Page Loading");

        flatCancelPage.waitForPageLoad();

        String currentUrl = driver.getCurrentUrl();
        boolean onPoliciesPage = currentUrl.contains("/policies");

        flatCancelPage.captureScreenshotToReport("Step 3 - Manage Policy Page Loaded");

        assertThat(onPoliciesPage)
                .as("Should be on Manage Policy page. Current URL: " + currentUrl)
                .isTrue();

        logger.info("Test PASSED: Successfully navigated to Manage Policy page");
    }

    /**
     * Test 5: Process all policies from Excel - Search, View, Flat Cancel, Select Certificates
     */
    @Test(priority = 5, groups = {"smoke", "flatcancel"}, dependsOnMethods = "testClickOnManagePolicyLink")
    @Description("Process all policies from Excel - search, view policy, flat cancel, select certificates")
    public void testProcessFlatCancelForAllPolicies() {
        logger.info("Starting test: Process Flat Cancel for all policies from Excel");

        // Get all policies with their certificates from Excel
        Map<String, List<String>> policiesWithCerts = flatCancelPage.getPoliciesWithCertificates();
        int totalPolicies = policiesWithCerts.size();

        logger.info("Found {} unique policies in FlatCancel sheet", totalPolicies);
        flatCancelPage.logInfoToReport("=== Processing " + totalPolicies + " Policies for Flat Cancel ===");

        assertThat(policiesWithCerts)
                .as("At least one Policy should exist in FlatCancel sheet")
                .isNotEmpty();

        // Track results
        java.util.List<FlatCancelPage.FlatCancelResult> allResults = new java.util.ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        int policyNumber = 0;
        for (Map.Entry<String, List<String>> entry : policiesWithCerts.entrySet()) {
            policyNumber++;
            String policyId = entry.getKey();
            List<String> certificateIds = entry.getValue();

            logger.info("\n========== Processing Policy {}/{}: {} ==========", policyNumber, totalPolicies, policyId);
            logger.info("Certificates to cancel: {}", certificateIds);

            flatCancelPage.logInfoToReport("<hr><h3 style='color: #4472C4;'>Policy " + policyNumber + "/" + totalPolicies + ": " + policyId + "</h3>");
            flatCancelPage.logInfoToReport("Certificates to select: " + certificateIds.size());

            // Process this policy
            FlatCancelPage.FlatCancelResult result = processSinglePolicyFlatCancel(policyId, certificateIds, policyNumber);
            allResults.add(result);

            if (result.isSuccess) {
                successCount++;
                logger.info("Policy {} - Flat Cancel processing COMPLETED", policyId);
            } else {
                failureCount++;
                logger.warn("Policy {} - Flat Cancel processing FAILED: {}", policyId, result.failureReason);
            }

            // Navigate back to Manage Policy page for next policy (if not the last one)
            if (policyNumber < totalPolicies) {
                navigateBackToManagePolicy();
            }
        }

        // Generate final summary report
        generateFinalSummaryReport(allResults, successCount, failureCount, totalPolicies);

        // Final assertion
        logger.info("\n========== FINAL SUMMARY ==========");
        logger.info("Total Policies: {}", totalPolicies);
        logger.info("Successful: {}", successCount);
        logger.info("Failed: {}", failureCount);

        assertThat(successCount)
                .as("At least one policy should be processed successfully")
                .isGreaterThan(0);

        if (failureCount > 0) {
            flatCancelPage.logWarningToReport("WARNING: " + failureCount + " out of " + totalPolicies + " policies had issues");
        }

        logger.info("Test COMPLETED: Processed Flat Cancel for all {} policies from Excel", totalPolicies);
    }

    // ==================== Helper Methods ====================

    /**
     * Process flat cancel for a single policy
     */
    private FlatCancelPage.FlatCancelResult processSinglePolicyFlatCancel(String policyId, List<String> certificateIds, int policyNumber) {
        FlatCancelPage.FlatCancelResult result = new FlatCancelPage.FlatCancelResult(policyId);
        result.certificatesToCancel = certificateIds.size();

        try {
            // Step 1: Search for policy
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 1: Searching for " + policyId);
            flatCancelPage.searchPolicy(policyId);
            logger.info("Searched for Policy ID: {}", policyId);

            // Step 2: Verify search results
            if (!flatCancelPage.verifySearchResultsDisplayed()) {
                result.failureReason = "No search results found for policy";
                flatCancelPage.logWarningToReport("No search results found for Policy: " + policyId);
                return result;
            }
            result.policyFound = true;
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 2: Search Results Displayed");

            // Step 3: Click View Policy button
            logger.info("Clicking View Policy button for: {}", policyId);
            if (!flatCancelPage.clickViewPolicyButton()) {
                result.failureReason = "Failed to click View Policy button";
                flatCancelPage.logWarningToReport("Failed to click View Policy button for: " + policyId);
                return result;
            }
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 3: View Policy Clicked");

            // Step 4: Verify Master Policy Details page
            if (!flatCancelPage.verifyMasterPolicyDetailsPage()) {
                result.failureReason = "Master Policy Details page not displayed";
                flatCancelPage.logWarningToReport("Master Policy Details page not displayed for: " + policyId);
                return result;
            }
            result.masterPolicyPageLoaded = true;
            flatCancelPage.logInfoToReport("Master Policy Details page verified - URL: " + driver.getCurrentUrl());
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 4: Master Policy Details Page");

            // Step 5: Click Flat Cancel button
            logger.info("Clicking Flat Cancel button for: {}", policyId);
            if (!flatCancelPage.clickFlatCancel()) {
                result.failureReason = "Failed to click Flat Cancel button";
                flatCancelPage.logWarningToReport("Failed to click Flat Cancel button for: " + policyId);
                return result;
            }
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 5: Flat Cancel Clicked");

            // Step 6: Verify Cancel Address page
            if (!flatCancelPage.verifyCancelAddressPage()) {
                result.failureReason = "Cancel Address page not displayed";
                flatCancelPage.logWarningToReport("Cancel Address page not displayed for: " + policyId);
                return result;
            }
            result.cancelAddressPageLoaded = true;
            flatCancelPage.logInfoToReport("Cancel Address page verified - URL: " + driver.getCurrentUrl());
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 6: Cancel Address Page");

            // Step 7: Verify Locations Details table is displayed
            if (!flatCancelPage.verifyLocationsDetailsTableDisplayed()) {
                result.failureReason = "Locations Details table not displayed";
                flatCancelPage.logWarningToReport("Locations Details table not displayed for: " + policyId);
                return result;
            }
            flatCancelPage.logInfoToReport("Locations Details table displayed");

            // Step 8: Select all certificates from Excel
            logger.info("Selecting {} certificates for policy {}", certificateIds.size(), policyId);
            List<FlatCancelPage.CertificateSelectionResult> selectionResults = flatCancelPage.selectAllCertificates(certificateIds);
            result.certificateSelectionResults = selectionResults;
            result.certificatesFound = (int) selectionResults.stream().filter(r -> r.found).count();
            result.certificatesSelected = (int) selectionResults.stream().filter(r -> r.selected).count();

            // Log certificate selection report
            String selectionReportHtml = flatCancelPage.generateCertificateSelectionReport(selectionResults);
            flatCancelPage.logHtmlToReport(selectionReportHtml);

            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 7: Certificates Selected");

            logger.info("Certificate selection for {}: Found {}/{}, Selected {}/{}",
                    policyId, result.certificatesFound, certificateIds.size(),
                    result.certificatesSelected, certificateIds.size());

            // Step 9: Extract location details for selected certificates
            logger.info("Extracting location details for {} certificates", certificateIds.size());
            java.util.List<FlatCancelPage.LocationDetails> locationDetails = flatCancelPage.extractAllLocationDetails(certificateIds);
            result.locationDetails = locationDetails;

            // Log location details to report
            String locationReportHtml = flatCancelPage.generateLocationDetailsReport(locationDetails);
            flatCancelPage.logHtmlToReport(locationReportHtml);

            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 8: Location Details Extracted");

            // Step 10: Click Create Endorsement button
            logger.info("Clicking Create Endorsement button for policy {}", policyId);
            if (!flatCancelPage.clickCreateEndorsementButton()) {
                result.failureReason = "Failed to click Create Endorsement button";
                flatCancelPage.logWarningToReport("Failed to click Create Endorsement button for: " + policyId);
                return result;
            }
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 9: Create Endorsement Clicked");

            // Step 11: Verify Edit Cancel Address page
            if (!flatCancelPage.verifyEditCancelAddressPage()) {
                result.failureReason = "Edit Cancel Address page not displayed";
                flatCancelPage.logWarningToReport("Edit Cancel Address page not displayed for: " + policyId);
                return result;
            }
            result.editCancelAddressPageLoaded = true;
            flatCancelPage.logInfoToReport("Edit Cancel Address page verified - URL: " + driver.getCurrentUrl());
            flatCancelPage.captureScreenshotToReport("Policy " + policyNumber + " - Step 10: Edit Cancel Address Page");

            result.isSuccess = true;

        } catch (Exception e) {
            result.failureReason = "Exception: " + e.getMessage();
            logger.error("Error processing flat cancel for policy {}: {}", policyId, e.getMessage());
            flatCancelPage.logWarningToReport("Error processing Policy " + policyId + ": " + e.getMessage());
        }

        return result;
    }

    /**
     * Navigate back to Manage Policy page
     */
    private void navigateBackToManagePolicy() {
        logger.info("Navigating back to Manage Policy page");
        try {
            // Click on Manage Policy link in navigation
            homePage.clickManagePolicy();
            waitHelper.waitAfterNavigation();
            flatCancelPage.waitForPageLoad();
            logger.info("Navigated back to Manage Policy page");
        } catch (Exception e) {
            logger.warn("Error navigating back: {}", e.getMessage());
            // Try direct navigation
            driver.get(config.getProperty("base.url") + "/policies");
            waitHelper.waitAfterNavigation();
        }
    }

    /**
     * Generate final summary report for all policies
     */
    private void generateFinalSummaryReport(java.util.List<FlatCancelPage.FlatCancelResult> allResults,
                                            int successCount, int failureCount, int totalPolicies) {
        StringBuilder summaryHtml = new StringBuilder();
        summaryHtml.append("<hr><h2 style='color: #4472C4;'>===== FLAT CANCEL FINAL SUMMARY =====</h2>");
        summaryHtml.append("<p><strong>Total Policies Processed:</strong> ").append(totalPolicies).append("</p>");
        summaryHtml.append("<p style='color: green;'><strong>Successful:</strong> ").append(successCount).append("</p>");
        if (failureCount > 0) {
            summaryHtml.append("<p style='color: red;'><strong>Failed:</strong> ").append(failureCount).append("</p>");
        }

        // Summary table
        summaryHtml.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        summaryHtml.append("<thead><tr style='background-color: #4472C4; color: white;'>");
        summaryHtml.append("<th>#</th><th>Policy ID</th><th>Status</th><th>Certs Selected</th>");
        summaryHtml.append("<th>Cancel Page</th><th>Edit Cancel Page</th><th>Total Premium</th><th>Notes</th>");
        summaryHtml.append("</tr></thead><tbody>");

        int index = 1;
        int totalCertsSelected = 0;
        double grandTotalPremium = 0;

        for (FlatCancelPage.FlatCancelResult result : allResults) {
            // Calculate total premium sum
            result.calculateTotalPremiumSum();

            String rowStyle = result.isSuccess ? "style='background-color: #90EE90; color: black;'"
                    : "style='background-color: #FFB6C1; color: black;'";
            String statusIcon = result.isSuccess ? "&#10004; SUCCESS" : "&#10008; FAILED";

            summaryHtml.append("<tr ").append(rowStyle).append(">");
            summaryHtml.append("<td>").append(index++).append("</td>");
            summaryHtml.append("<td>").append(result.policyId).append("</td>");
            summaryHtml.append("<td>").append(statusIcon).append("</td>");
            summaryHtml.append("<td>").append(result.certificatesSelected).append("/").append(result.certificatesToCancel).append("</td>");
            summaryHtml.append("<td>").append(result.cancelAddressPageLoaded ? "&#10004;" : "&#10008;").append("</td>");
            summaryHtml.append("<td>").append(result.editCancelAddressPageLoaded ? "&#10004;" : "&#10008;").append("</td>");
            summaryHtml.append("<td>$").append(String.format("%.2f", result.totalPremiumSum)).append("</td>");
            summaryHtml.append("<td>").append(result.failureReason).append("</td>");
            summaryHtml.append("</tr>");

            totalCertsSelected += result.certificatesSelected;
            grandTotalPremium += result.totalPremiumSum;
        }

        // Totals row
        summaryHtml.append("<tr style='background-color: #4472C4; color: white; font-weight: bold;'>");
        summaryHtml.append("<td colspan='3'>TOTALS</td>");
        summaryHtml.append("<td>").append(totalCertsSelected).append("</td>");
        summaryHtml.append("<td colspan='2'></td>");
        summaryHtml.append("<td style='background-color: #FFD700; color: black;'>$").append(String.format("%.2f", grandTotalPremium)).append("</td>");
        summaryHtml.append("<td></td>");
        summaryHtml.append("</tr>");

        summaryHtml.append("</tbody></table>");

        flatCancelPage.logHtmlToReport(summaryHtml.toString());
        flatCancelPage.captureScreenshotToReport("Final Summary - All Policies Processed");
    }

    // ==================== Custom Annotation ====================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Description {
        String value();
    }
}
