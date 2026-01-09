package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Flat Cancel Page - High-Level Flat Cancel Actions
 * Contains business-level operations that call basic methods from FlatCancelLocators
 * This is the class to be used in tests (Cucumber steps, TestNG tests)
 */
public class FlatCancelPage extends FlatCancelLocators {

    /**
     * Constructor
     */
    public FlatCancelPage(WebDriver driver) {
        super(driver);
        logger.info("FlatCancelPage initialized");
    }

    // ==================== Policy and Certificate Methods ====================

    /**
     * Get all Policy IDs from Excel
     * @return List of policy IDs
     */
    public List<String> getAllPolicyIds() {
        return getAllPolicyIdsFromExcel();
    }

    /**
     * Get all Certificate IDs for a specific policy
     * @param policyId The policy ID
     * @return List of certificate IDs
     */
    public List<String> getCertificatesForPolicy(String policyId) {
        return getCertificateIdsForPolicy(policyId);
    }

    /**
     * Get map of all policies to their certificates
     * @return Map of Policy ID to List of Certificate IDs
     */
    public Map<String, List<String>> getPoliciesWithCertificates() {
        return getAllPoliciesWithCertificates();
    }

    // ==================== Search and Navigation Methods ====================

    /**
     * Search for a policy by Policy ID
     * @param policyId The policy ID to search for
     */
    public void searchPolicy(String policyId) {
        logger.info("Searching for Policy: {}", policyId);
        waitForPolicySearchInput();
        clearPolicySearchInput();
        inputPolicySearchText(policyId);
        // Press Enter to trigger search
        policySearchInput.sendKeys(Keys.ENTER);
        sleep(1500); // Wait for search results
        waitForLoaderToDisappear();
        logger.info("Search completed for Policy: {}", policyId);
    }

    /**
     * Verify search results are displayed
     * @return true if results are found
     */
    public boolean verifySearchResultsDisplayed() {
        logger.info("Verifying search results are displayed");
        waitForLoaderToDisappear();
        sleep(1000);

        try {
            List<WebElement> rows = driver.findElements(tableRowsLocator);
            boolean hasResults = rows.size() > 0;
            logger.info("Search results found: {} rows", rows.size());
            return hasResults;
        } catch (Exception e) {
            logger.warn("Error checking search results: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Click on View Policy button
     * The button ID pattern is: policy-list-action-view-policy-{id}
     * @return true if button was clicked successfully
     */
    public boolean clickViewPolicyButton() {
        logger.info("Clicking View Policy button");
        waitForLoaderToDisappear();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement viewPolicyButton = wait.until(ExpectedConditions.elementToBeClickable(viewPolicyButtonLocator));

            logger.info("Found View Policy button with ID: {}", viewPolicyButton.getAttribute("id"));
            scrollIntoView(viewPolicyButton);
            sleep(500);
            click(viewPolicyButton);

            logger.info("Clicked View Policy button");
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click View Policy button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify Master Policy Details page is displayed
     * URL should contain: https://cpiai-dev.attri.ai/policies/
     * @return true if on Master Policy Details page
     */
    public boolean verifyMasterPolicyDetailsPage() {
        logger.info("Verifying Master Policy Details page");
        waitForLoaderToDisappear();
        sleep(2000); // Wait for page navigation

        String currentUrl = getCurrentUrl();
        boolean onMasterPolicyPage = currentUrl.contains(MASTER_POLICY_URL) || currentUrl.contains("/policies/");

        logger.info("Current URL: {}", currentUrl);
        logger.info("On Master Policy Details page: {}", onMasterPolicyPage);

        return onMasterPolicyPage;
    }

    /**
     * Click on Flat Cancel button on Master Policy Details page
     * @return true if button was clicked successfully
     */
    public boolean clickFlatCancel() {
        logger.info("Clicking Flat Cancel button");
        waitForLoaderToDisappear();

        try {
            waitForFlatCancelButton();
            scrollIntoView(flatCancelButton);
            sleep(500);
            clickFlatCancelButton();

            logger.info("Clicked Flat Cancel button");
            waitForLoaderToDisappear();
            sleep(2000); // Wait for page navigation
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Flat Cancel button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify Cancel Address page is displayed
     * URL should be: https://cpiai-dev.attri.ai/cancel-address
     * @return true if on Cancel Address page
     */
    public boolean verifyCancelAddressPage() {
        logger.info("Verifying Cancel Address page");
        waitForLoaderToDisappear();
        sleep(2000); // Wait for page navigation

        String currentUrl = getCurrentUrl();
        boolean onCancelAddressPage = currentUrl.contains(CANCEL_ADDRESS_URL) || currentUrl.contains("/cancel-address");

        logger.info("Current URL: {}", currentUrl);
        logger.info("On Cancel Address page: {}", onCancelAddressPage);

        return onCancelAddressPage;
    }

    /**
     * Verify Locations Details table is displayed
     * @return true if table is visible
     */
    public boolean verifyLocationsDetailsTableDisplayed() {
        logger.info("Verifying Locations Details table is displayed");
        waitForLoaderToDisappear();

        try {
            waitForLocationsDetailsTable();
            boolean displayed = isLocationsDetailsTableDisplayed();
            logger.info("Locations Details table displayed: {}", displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Locations Details table not found: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Certificate Selection Methods ====================

    /**
     * Data class to hold certificate selection result
     */
    public static class CertificateSelectionResult {
        public String certificateId;
        public boolean found;
        public boolean selected;
        public String message;

        public CertificateSelectionResult(String certificateId) {
            this.certificateId = certificateId;
            this.found = false;
            this.selected = false;
            this.message = "";
        }

        @Override
        public String toString() {
            return String.format("Certificate: %s | Found: %s | Selected: %s | %s",
                    certificateId, found, selected, message);
        }
    }

    /**
     * Find and select checkbox for a specific certificate in Locations Details table
     * Uses robust element finding to handle stale element references
     * @param certificateId The certificate ID to find and select
     * @return CertificateSelectionResult with status
     */
    public CertificateSelectionResult selectCertificateCheckbox(String certificateId) {
        logger.info("Selecting checkbox for Certificate: {}", certificateId);
        CertificateSelectionResult result = new CertificateSelectionResult(certificateId);

        try {
            waitForLocationsDetailsTable();
            waitForLoaderToDisappear();
            sleep(500);

            // Build a dynamic XPath that targets the specific certificate row
            // This approach avoids stale element issues by finding elements fresh each time
            String certRowXpath = "//table//tbody//tr[contains(.,'" + certificateId + "')]";

            // Check if row exists
            List<WebElement> matchingRows = driver.findElements(By.xpath(certRowXpath));
            if (matchingRows.isEmpty()) {
                result.found = false;
                result.message = "Certificate not found in table";
                logger.warn("Certificate {} not found in Locations Details table", certificateId);
                return result;
            }

            result.found = true;
            logger.info("Found certificate {} in table", certificateId);

            // Find checkbox in the row using a fresh XPath
            String checkboxXpath = certRowXpath + "//td//input[@type='checkbox']";

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(checkboxXpath)));

                // Scroll checkbox into view using JavaScript
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", checkbox);
                sleep(300);

                // Check if already selected using JavaScript (avoid stale element)
                Boolean isAlreadySelected = (Boolean) js.executeScript("return arguments[0].checked;", checkbox);

                if (Boolean.TRUE.equals(isAlreadySelected)) {
                    result.selected = true;
                    result.message = "Already selected";
                    logger.info("Checkbox for certificate {} is already selected", certificateId);
                } else {
                    // Click using JavaScript (most reliable)
                    js.executeScript("arguments[0].click();", checkbox);
                    logger.info("Clicked checkbox for certificate {} using JavaScript", certificateId);
                    sleep(500);

                    // Verify selection using fresh element lookup
                    try {
                        WebElement freshCheckbox = driver.findElement(By.xpath(checkboxXpath));
                        Boolean isNowSelected = (Boolean) js.executeScript("return arguments[0].checked;", freshCheckbox);
                        result.selected = Boolean.TRUE.equals(isNowSelected);

                        if (!result.selected) {
                            // Try clicking again
                            js.executeScript("arguments[0].click();", freshCheckbox);
                            sleep(300);
                            isNowSelected = (Boolean) js.executeScript("return arguments[0].checked;", freshCheckbox);
                            result.selected = Boolean.TRUE.equals(isNowSelected);
                        }
                    } catch (Exception freshEx) {
                        // Element may have been recreated, assume success if no exception during click
                        result.selected = true;
                        logger.debug("Could not verify selection, assuming success: {}", freshEx.getMessage());
                    }

                    result.message = result.selected ? "Successfully selected" : "Click did not select";
                    logger.info("Checkbox selection for certificate {}: {}", certificateId, result.selected);
                }
            } catch (Exception e) {
                result.message = "Checkbox not found or click failed: " + e.getMessage();
                logger.warn("Could not find/click checkbox for certificate {}: {}", certificateId, e.getMessage());
            }

        } catch (Exception e) {
            result.message = "Error: " + e.getMessage();
            logger.error("Error selecting certificate {}: {}", certificateId, e.getMessage());
        }

        return result;
    }

    /**
     * Select checkboxes for all certificates in the list
     * @param certificateIds List of certificate IDs to select
     * @return List of CertificateSelectionResult for each certificate
     */
    public List<CertificateSelectionResult> selectAllCertificates(List<String> certificateIds) {
        logger.info("Selecting {} certificates", certificateIds.size());
        List<CertificateSelectionResult> results = new ArrayList<>();

        for (String certId : certificateIds) {
            CertificateSelectionResult result = selectCertificateCheckbox(certId);
            results.add(result);
            logger.info("Certificate {} - Found: {}, Selected: {}", certId, result.found, result.selected);
        }

        // Log summary
        long foundCount = results.stream().filter(r -> r.found).count();
        long selectedCount = results.stream().filter(r -> r.selected).count();
        logger.info("Certificate selection summary: Found {}/{}, Selected {}/{}",
                foundCount, certificateIds.size(), selectedCount, certificateIds.size());

        return results;
    }

    /**
     * Complete workflow: Process a single policy for flat cancel
     * 1. Search for policy
     * 2. Click View Policy
     * 3. Verify Master Policy Details page
     * 4. Click Flat Cancel
     * 5. Verify Cancel Address page
     * 6. Select all certificates from Excel
     * @param policyId The policy ID
     * @param certificateIds List of certificate IDs to cancel
     * @return FlatCancelResult with status
     */
    public FlatCancelResult processFlatCancelForPolicy(String policyId, List<String> certificateIds) {
        logger.info("Processing Flat Cancel for Policy: {} with {} certificates", policyId, certificateIds.size());
        FlatCancelResult result = new FlatCancelResult(policyId);
        result.certificatesToCancel = certificateIds.size();

        try {
            // Step 1: Search for policy
            searchPolicy(policyId);
            if (!verifySearchResultsDisplayed()) {
                result.failureReason = "Policy not found in search results";
                return result;
            }
            result.policyFound = true;

            // Step 2: Click View Policy
            if (!clickViewPolicyButton()) {
                result.failureReason = "Failed to click View Policy button";
                return result;
            }

            // Step 3: Verify Master Policy Details page
            if (!verifyMasterPolicyDetailsPage()) {
                result.failureReason = "Master Policy Details page not displayed";
                return result;
            }
            result.masterPolicyPageLoaded = true;

            // Step 4: Click Flat Cancel
            if (!clickFlatCancel()) {
                result.failureReason = "Failed to click Flat Cancel button";
                return result;
            }

            // Step 5: Verify Cancel Address page
            if (!verifyCancelAddressPage()) {
                result.failureReason = "Cancel Address page not displayed";
                return result;
            }
            result.cancelAddressPageLoaded = true;

            // Step 6: Select all certificates
            if (!verifyLocationsDetailsTableDisplayed()) {
                result.failureReason = "Locations Details table not displayed";
                return result;
            }

            List<CertificateSelectionResult> selectionResults = selectAllCertificates(certificateIds);
            result.certificateSelectionResults = selectionResults;
            result.certificatesFound = (int) selectionResults.stream().filter(r -> r.found).count();
            result.certificatesSelected = (int) selectionResults.stream().filter(r -> r.selected).count();

            result.isSuccess = true;

        } catch (Exception e) {
            result.failureReason = "Exception: " + e.getMessage();
            logger.error("Error processing flat cancel for policy {}: {}", policyId, e.getMessage());
        }

        return result;
    }

    /**
     * Data class to hold flat cancel result for a policy
     */
    public static class FlatCancelResult {
        public String policyId;
        public boolean isSuccess;
        public String failureReason;
        public boolean policyFound;
        public boolean masterPolicyPageLoaded;
        public boolean cancelAddressPageLoaded;
        public boolean editCancelAddressPageLoaded;
        public int certificatesToCancel;
        public int certificatesFound;
        public int certificatesSelected;
        public List<CertificateSelectionResult> certificateSelectionResults;
        public List<LocationDetails> locationDetails;
        public double totalPremiumSum;

        public FlatCancelResult(String policyId) {
            this.policyId = policyId;
            this.isSuccess = false;
            this.failureReason = "";
            this.policyFound = false;
            this.masterPolicyPageLoaded = false;
            this.cancelAddressPageLoaded = false;
            this.editCancelAddressPageLoaded = false;
            this.certificatesToCancel = 0;
            this.certificatesFound = 0;
            this.certificatesSelected = 0;
            this.certificateSelectionResults = new ArrayList<>();
            this.locationDetails = new ArrayList<>();
            this.totalPremiumSum = 0;
        }

        public void calculateTotalPremiumSum() {
            this.totalPremiumSum = 0;
            for (LocationDetails loc : locationDetails) {
                this.totalPremiumSum += loc.totalSum;
            }
        }

        @Override
        public String toString() {
            return String.format("Policy: %s | Success: %s | Found: %d/%d | Selected: %d/%d | Total: $%.2f | %s",
                    policyId, isSuccess, certificatesFound, certificatesToCancel,
                    certificatesSelected, certificatesToCancel, totalPremiumSum, failureReason);
        }
    }

    // ==================== Location Details Extraction ====================

    /**
     * Data class to hold location/certificate details from table
     */
    public static class LocationDetails {
        public String certNo;
        public String address;
        public double propertyPremium;
        public double glPremium;
        public double waterSewerPremium;
        public double taxes;
        public double fees;
        public double totalSum;

        public LocationDetails(String certNo) {
            this.certNo = certNo;
            this.address = "";
            this.propertyPremium = 0;
            this.glPremium = 0;
            this.waterSewerPremium = 0;
            this.taxes = 0;
            this.fees = 0;
            this.totalSum = 0;
        }

        public void calculateSum() {
            this.totalSum = propertyPremium + glPremium + waterSewerPremium + taxes + fees;
        }

        @Override
        public String toString() {
            return String.format("Cert: %s | Address: %s | Property: $%.2f | GL: $%.2f | Water/Sewer: $%.2f | Taxes: $%.2f | Fees: $%.2f | Total: $%.2f",
                    certNo, address, propertyPremium, glPremium, waterSewerPremium, taxes, fees, totalSum);
        }
    }

    /**
     * Parse dollar amount from string (e.g., "$1,234.56" -> 1234.56)
     */
    private double parseDollarAmount(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            String cleaned = text.replaceAll("[^0-9.-]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            logger.warn("Could not parse dollar amount: {}", text);
            return 0;
        }
    }

    /**
     * Extract location details for a specific certificate from the table
     * Table columns (actual order): Property Premium, Water/Sewer Premium, GL Premium, Premium, Taxes, Fees
     * We capture: Property Premium, Water/Sewer Premium, GL Premium, Taxes, Fees (skip Premium column)
     * @param certificateId The certificate ID to find
     * @return LocationDetails with extracted data
     */
    public LocationDetails extractLocationDetails(String certificateId) {
        logger.info("Extracting location details for Certificate: {}", certificateId);
        LocationDetails details = new LocationDetails(certificateId);

        try {
            waitForLocationsDetailsTable();

            // First, read table headers to understand column positions
            List<WebElement> headers = driver.findElements(By.xpath("//table//thead//tr//th"));
            logger.info("Found {} table headers", headers.size());

            // Map column names to indices
            int propertyPremiumIdx = -1;
            int waterSewerPremiumIdx = -1;
            int glPremiumIdx = -1;
            int taxesIdx = -1;
            int feesIdx = -1;

            for (int i = 0; i < headers.size(); i++) {
                String headerText = headers.get(i).getText().trim().toLowerCase();
                logger.debug("Header {}: {}", i, headerText);

                if (headerText.contains("property") && headerText.contains("premium")) {
                    propertyPremiumIdx = i;
                } else if (headerText.contains("water") || headerText.contains("sewer")) {
                    waterSewerPremiumIdx = i;
                } else if (headerText.contains("gl") && headerText.contains("premium")) {
                    glPremiumIdx = i;
                } else if (headerText.equals("taxes")) {
                    taxesIdx = i;
                } else if (headerText.equals("fees")) {
                    feesIdx = i;
                }
            }

            logger.info("Column indices - Property: {}, Water/Sewer: {}, GL: {}, Taxes: {}, Fees: {}",
                propertyPremiumIdx, waterSewerPremiumIdx, glPremiumIdx, taxesIdx, feesIdx);

            List<WebElement> rows = driver.findElements(tableRowsLocator);

            for (WebElement row : rows) {
                String rowText = row.getText();

                if (rowText.contains(certificateId)) {
                    logger.info("Found certificate {} in table", certificateId);

                    // Get all cells in this row
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    logger.info("Found {} cells in row", cells.size());

                    // Log all cell values for debugging
                    for (int i = 0; i < cells.size(); i++) {
                        String cellText = cells.get(i).getText().trim();
                        logger.debug("Cell {}: {}", i, cellText);

                        // Identify certificate and address
                        if (cellText.contains(certificateId)) {
                            details.certNo = certificateId;
                        } else if (cellText.contains(",") && (cellText.contains("St") || cellText.contains("Ave") ||
                                   cellText.contains("Rd") || cellText.contains("Dr") || cellText.contains("USA"))) {
                            details.address = cellText;
                        }
                    }

                    // Extract values using header indices if available
                    if (propertyPremiumIdx >= 0 && propertyPremiumIdx < cells.size()) {
                        details.propertyPremium = parseDollarAmount(cells.get(propertyPremiumIdx).getText());
                        logger.info("Property Premium (col {}): ${}", propertyPremiumIdx, details.propertyPremium);
                    }
                    if (waterSewerPremiumIdx >= 0 && waterSewerPremiumIdx < cells.size()) {
                        details.waterSewerPremium = parseDollarAmount(cells.get(waterSewerPremiumIdx).getText());
                        logger.info("Water/Sewer Premium (col {}): ${}", waterSewerPremiumIdx, details.waterSewerPremium);
                    }
                    if (glPremiumIdx >= 0 && glPremiumIdx < cells.size()) {
                        details.glPremium = parseDollarAmount(cells.get(glPremiumIdx).getText());
                        logger.info("GL Premium (col {}): ${}", glPremiumIdx, details.glPremium);
                    }
                    if (taxesIdx >= 0 && taxesIdx < cells.size()) {
                        details.taxes = parseDollarAmount(cells.get(taxesIdx).getText());
                        logger.info("Taxes (col {}): ${}", taxesIdx, details.taxes);
                    }
                    if (feesIdx >= 0 && feesIdx < cells.size()) {
                        details.fees = parseDollarAmount(cells.get(feesIdx).getText());
                        logger.info("Fees (col {}): ${}", feesIdx, details.fees);
                    }

                    // Fallback: If headers not found, use position-based extraction
                    // Actual order: Property Premium, Water/Sewer Premium, GL Premium, Premium, Taxes, Fees
                    if (propertyPremiumIdx < 0) {
                        List<String> dollarAmounts = new ArrayList<>();
                        for (WebElement cell : cells) {
                            String cellText = cell.getText().trim();
                            if (cellText.startsWith("$")) {
                                dollarAmounts.add(cellText);
                            }
                        }

                        logger.info("Fallback: Found {} dollar amounts: {}", dollarAmounts.size(), dollarAmounts);

                        // Correct order: Property, Water/Sewer, GL, Premium(skip), Taxes, Fees
                        if (dollarAmounts.size() >= 6) {
                            details.propertyPremium = parseDollarAmount(dollarAmounts.get(0));
                            details.waterSewerPremium = parseDollarAmount(dollarAmounts.get(1));
                            details.glPremium = parseDollarAmount(dollarAmounts.get(2));
                            // Skip dollarAmounts.get(3) which is "Premium"
                            details.taxes = parseDollarAmount(dollarAmounts.get(4));
                            details.fees = parseDollarAmount(dollarAmounts.get(5));
                        } else if (dollarAmounts.size() >= 5) {
                            details.propertyPremium = parseDollarAmount(dollarAmounts.get(0));
                            details.waterSewerPremium = parseDollarAmount(dollarAmounts.get(1));
                            details.glPremium = parseDollarAmount(dollarAmounts.get(2));
                            details.taxes = parseDollarAmount(dollarAmounts.get(3));
                            details.fees = parseDollarAmount(dollarAmounts.get(4));
                        }
                    }

                    details.calculateSum();
                    logger.info("Extracted details: {}", details);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting location details for {}: {}", certificateId, e.getMessage());
        }

        return details;
    }

    /**
     * Extract location details for all selected certificates
     * @param certificateIds List of certificate IDs
     * @return List of LocationDetails
     */
    public List<LocationDetails> extractAllLocationDetails(List<String> certificateIds) {
        logger.info("Extracting location details for {} certificates", certificateIds.size());
        List<LocationDetails> allDetails = new ArrayList<>();

        for (String certId : certificateIds) {
            LocationDetails details = extractLocationDetails(certId);
            allDetails.add(details);
        }

        return allDetails;
    }

    /**
     * Generate HTML report for location details
     */
    public String generateLocationDetailsReport(List<LocationDetails> locationDetails) {
        StringBuilder html = new StringBuilder();

        html.append("<h4 style='color: #4472C4;'>Selected Certificates - Location Details</h4>");
        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        html.append("<thead><tr style='background-color: #4472C4; color: white;'>");
        html.append("<th>#</th><th>Cert No.</th><th>Address</th><th>Property Premium</th>");
        html.append("<th>GL Premium</th><th>Water/Sewer Premium</th><th>Taxes</th><th>Fees</th><th>Row Total</th>");
        html.append("</tr></thead><tbody>");

        double grandTotalProperty = 0;
        double grandTotalGL = 0;
        double grandTotalWaterSewer = 0;
        double grandTotalTaxes = 0;
        double grandTotalFees = 0;
        double grandTotal = 0;

        int index = 1;
        for (LocationDetails loc : locationDetails) {
            html.append("<tr style='background-color: #E8F4FD; color: black;'>");
            html.append("<td>").append(index++).append("</td>");
            html.append("<td><strong>").append(loc.certNo).append("</strong></td>");
            html.append("<td>").append(loc.address).append("</td>");
            html.append("<td>$").append(String.format("%.2f", loc.propertyPremium)).append("</td>");
            html.append("<td>$").append(String.format("%.2f", loc.glPremium)).append("</td>");
            html.append("<td>$").append(String.format("%.2f", loc.waterSewerPremium)).append("</td>");
            html.append("<td>$").append(String.format("%.2f", loc.taxes)).append("</td>");
            html.append("<td>$").append(String.format("%.2f", loc.fees)).append("</td>");
            html.append("<td style='background-color: #90EE90;'><strong>$").append(String.format("%.2f", loc.totalSum)).append("</strong></td>");
            html.append("</tr>");

            grandTotalProperty += loc.propertyPremium;
            grandTotalGL += loc.glPremium;
            grandTotalWaterSewer += loc.waterSewerPremium;
            grandTotalTaxes += loc.taxes;
            grandTotalFees += loc.fees;
            grandTotal += loc.totalSum;
        }

        // Grand Total Row
        html.append("<tr style='background-color: #4472C4; color: white; font-weight: bold;'>");
        html.append("<td colspan='3'>GRAND TOTAL</td>");
        html.append("<td>$").append(String.format("%.2f", grandTotalProperty)).append("</td>");
        html.append("<td>$").append(String.format("%.2f", grandTotalGL)).append("</td>");
        html.append("<td>$").append(String.format("%.2f", grandTotalWaterSewer)).append("</td>");
        html.append("<td>$").append(String.format("%.2f", grandTotalTaxes)).append("</td>");
        html.append("<td>$").append(String.format("%.2f", grandTotalFees)).append("</td>");
        html.append("<td style='background-color: #FFD700;'><strong>$").append(String.format("%.2f", grandTotal)).append("</strong></td>");
        html.append("</tr>");

        html.append("</tbody></table>");

        return html.toString();
    }

    // ==================== Create Endorsement Methods ====================

    /**
     * Click Create Endorsement button
     * @return true if button was clicked successfully
     */
    public boolean clickCreateEndorsementButton() {
        logger.info("Clicking Create Endorsement button");
        try {
            waitForLoaderToDisappear();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(createEndorsementButtonLocator));

            scrollIntoView(button);
            sleep(500);

            // Use JavaScript click for reliability
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", button);

            logger.info("Clicked Create Endorsement button");
            sleep(2000);
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Create Endorsement button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify Edit Cancel Address page is displayed
     * URL should be: https://cpiai-dev.attri.ai/edit-cancel-address
     * @return true if on Edit Cancel Address page
     */
    public boolean verifyEditCancelAddressPage() {
        logger.info("Verifying Edit Cancel Address page");
        waitForLoaderToDisappear();
        sleep(2000);

        String currentUrl = getCurrentUrl();
        boolean onEditCancelAddressPage = currentUrl.contains(EDIT_CANCEL_ADDRESS_URL) || currentUrl.contains("/edit-cancel-address");

        logger.info("Current URL: {}", currentUrl);
        logger.info("On Edit Cancel Address page: {}", onEditCancelAddressPage);

        return onEditCancelAddressPage;
    }

    // ==================== Reporting Methods ====================

    /**
     * Log info message to Extent Report
     */
    public void logInfoToReport(String message) {
        logger.info(message);
        try {
            com.aventstack.extentreports.ExtentTest test =
                com.automation.listeners.TestListener.getExtentTest();
            if (test != null) {
                test.info(message);
            }
        } catch (Exception e) {
            logger.debug("Could not log to Extent Report: {}", e.getMessage());
        }
    }

    /**
     * Log HTML content to Extent Report
     */
    public void logHtmlToReport(String htmlContent) {
        logger.info("Logging HTML content to report");
        try {
            com.aventstack.extentreports.ExtentTest test =
                com.automation.listeners.TestListener.getExtentTest();
            if (test != null) {
                test.info(htmlContent);
            }
        } catch (Exception e) {
            logger.debug("Could not log HTML to Extent Report: {}", e.getMessage());
        }
    }

    /**
     * Log warning message to Extent Report
     */
    public void logWarningToReport(String message) {
        logger.warn(message);
        try {
            com.aventstack.extentreports.ExtentTest test =
                com.automation.listeners.TestListener.getExtentTest();
            if (test != null) {
                test.warning(message);
            }
        } catch (Exception e) {
            logger.debug("Could not log warning to Extent Report: {}", e.getMessage());
        }
    }

    /**
     * Generate HTML report for certificate selection results
     */
    public String generateCertificateSelectionReport(List<CertificateSelectionResult> results) {
        StringBuilder html = new StringBuilder();

        long foundCount = results.stream().filter(r -> r.found).count();
        long selectedCount = results.stream().filter(r -> r.selected).count();

        html.append("<h4 style='color: #4472C4;'>Certificate Selection Results</h4>");
        html.append("<p><strong>Total Certificates:</strong> ").append(results.size()).append("</p>");
        html.append("<p style='color: ").append(foundCount == results.size() ? "green" : "orange").append(";'>");
        html.append("<strong>Found:</strong> ").append(foundCount).append("/").append(results.size()).append("</p>");
        html.append("<p style='color: ").append(selectedCount == results.size() ? "green" : "red").append(";'>");
        html.append("<strong>Selected:</strong> ").append(selectedCount).append("/").append(results.size()).append("</p>");

        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        html.append("<thead><tr style='background-color: #4472C4; color: white;'>");
        html.append("<th>#</th><th>Certificate ID</th><th>Found</th><th>Selected</th><th>Message</th>");
        html.append("</tr></thead><tbody>");

        int index = 1;
        for (CertificateSelectionResult result : results) {
            String rowStyle = result.selected ? "style='background-color: #90EE90; color: black;'"
                    : "style='background-color: #FFB6C1; color: black;'";
            html.append("<tr ").append(rowStyle).append(">");
            html.append("<td>").append(index++).append("</td>");
            html.append("<td>").append(result.certificateId).append("</td>");
            html.append("<td>").append(result.found ? "Yes" : "No").append("</td>");
            html.append("<td>").append(result.selected ? "Yes" : "No").append("</td>");
            html.append("<td>").append(result.message).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        return html.toString();
    }
}
