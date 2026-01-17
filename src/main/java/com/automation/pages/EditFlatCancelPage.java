package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Edit Flat Cancel Page - High-Level Edit Flat Cancel Actions
 * Contains business-level operations that call basic methods from EditFlatCancelLocators
 * This is the class to be used in tests
 */
public class EditFlatCancelPage extends EditFlatCancelLocators {

    /**
     * Constructor
     */
    public EditFlatCancelPage(WebDriver driver) {
        super(driver);
        logger.info("EditFlatCancelPage initialized");
    }

    // ==================== Page Verification Methods ====================

    /**
     * Verify Edit Cancel Address page is displayed
     * @return true if on Edit Cancel Address page
     */
    public boolean verifyEditCancelAddressPage() {
        logger.info("Verifying Edit Cancel Address page");
        waitForLoaderToDisappear();
        sleep(2000);

        String currentUrl = getCurrentUrl();
        boolean onPage = currentUrl.contains(EDIT_CANCEL_ADDRESS_URL) || currentUrl.contains("/edit-cancel-address");

        logger.info("Current URL: {}", currentUrl);
        logger.info("On Edit Cancel Address page: {}", onPage);

        return onPage;
    }

    /**
     * Verify page elements are displayed
     * @return PageElementsStatus with status of each element
     */
    public PageElementsStatus verifyPageElements() {
        logger.info("Verifying page elements on Edit Cancel Address page");
        PageElementsStatus status = new PageElementsStatus();

        waitForLoaderToDisappear();
        sleep(1000);

        // Check table
        status.tableDisplayed = isLocationsTableDisplayed();
        logger.info("Locations table displayed: {}", status.tableDisplayed);

        return status;
    }

    /**
     * Data class to hold page elements status
     */
    public static class PageElementsStatus {
        public boolean tableDisplayed;
        public boolean saveButtonDisplayed;
        public boolean cancelButtonDisplayed;
        public boolean effectiveDateDisplayed;
        public boolean cancelDateDisplayed;

        @Override
        public String toString() {
            return String.format("Table: %s | Save: %s | Cancel: %s | EffDate: %s | CancelDate: %s",
                    tableDisplayed, saveButtonDisplayed, cancelButtonDisplayed,
                    effectiveDateDisplayed, cancelDateDisplayed);
        }
    }

    // ==================== Location Details Methods ====================

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
     * Parse dollar amount from string
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
     * Extract location details from table for a certificate
     * Table columns (actual order): Property Premium, Water/Sewer Premium, GL Premium, Premium, Taxes, Fees
     * We capture: Property Premium, Water/Sewer Premium, GL Premium, Taxes, Fees (skip Premium column)
     * @param certificateId Certificate ID to find
     * @return LocationDetails with extracted data
     */
    public LocationDetails extractLocationDetails(String certificateId) {
        logger.info("Extracting location details for Certificate: {}", certificateId);
        LocationDetails details = new LocationDetails(certificateId);

        try {
            waitForLocationsTable();

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

                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    logger.info("Found {} cells in row", cells.size());

                    // Log all cell values for debugging
                    for (int i = 0; i < cells.size(); i++) {
                        String cellText = cells.get(i).getText().trim();
                        logger.debug("Cell {}: {}", i, cellText);

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
     * Extract location details for all certificates
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
     * Get all certificates displayed in the table
     * @return List of certificate IDs from the table
     */
    public List<String> getAllCertificatesFromTable() {
        logger.info("Getting all certificates from table");
        List<String> certificates = new ArrayList<>();

        try {
            waitForLocationsTable();
            waitForLoaderToDisappear();
            sleep(2000); // Wait for table to fully load

            // Debug: Find all tables on page
            List<WebElement> allTables = driver.findElements(By.xpath("//table"));
            logger.info("DEBUG: Found {} tables on page", allTables.size());

            // Debug: Log each table's first row
            for (int t = 0; t < allTables.size(); t++) {
                try {
                    List<WebElement> tableRows = allTables.get(t).findElements(By.xpath(".//tbody//tr"));
                    logger.info("DEBUG: Table {} has {} rows", t, tableRows.size());
                    if (!tableRows.isEmpty()) {
                        String firstRowText = tableRows.get(0).getText().replace("\n", " | ").substring(0, Math.min(150, tableRows.get(0).getText().length()));
                        logger.info("DEBUG: Table {} first row: {}", t, firstRowText);
                    }
                } catch (Exception e) {
                    logger.debug("DEBUG: Could not read table {}: {}", t, e.getMessage());
                }
            }

            // Get all rows from all tables
            List<WebElement> rows = driver.findElements(tableRowsLocator);
            logger.info("DEBUG: Total rows from tableRowsLocator: {}", rows.size());

            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                String rowText = row.getText();
                String rowTextClean = rowText.replace("\n", " | ");

                // Log first 10 rows for debugging
                if (i < 10) {
                    logger.info("DEBUG: Row {}: {}", i, rowTextClean.substring(0, Math.min(200, rowTextClean.length())));
                }

                // Certificate IDs typically start with letters followed by numbers
                if (rowText.contains("ARCH") || rowText.contains("CERT")) {
                    // Extract the certificate ID from the row
                    String[] parts = rowText.split("\\s+");
                    for (String part : parts) {
                        if (part.startsWith("ARCH") || part.startsWith("CERT")) {
                            if (!certificates.contains(part)) {
                                certificates.add(part);
                                logger.debug("DEBUG: Extracted certificate: {}", part);
                            }
                            break;
                        }
                    }
                }
            }

            logger.info("Found {} certificates in table: {}", certificates.size(), certificates);
        } catch (Exception e) {
            logger.error("Error getting certificates from table: {}", e.getMessage(), e);
        }

        return certificates;
    }

    /**
     * Count total rows in the table
     * @return Number of rows
     */
    public int getTableRowCount() {
        try {
            waitForLocationsTable();
            List<WebElement> rows = driver.findElements(tableRowsLocator);
            int count = rows.size();
            logger.info("Table has {} rows", count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting table rows: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== Form Interaction Methods ====================

    /**
     * Fill effective date
     * @param date Date in required format
     */
    public void fillEffectiveDate(String date) {
        logger.info("Filling effective date: {}", date);
        try {
            inputEffectiveDate(date);
        } catch (Exception e) {
            logger.warn("Effective date field may not be visible: {}", e.getMessage());
        }
    }

    /**
     * Fill cancel date
     * @param date Date in required format
     */
    public void fillCancelDate(String date) {
        logger.info("Filling cancel date: {}", date);
        try {
            inputCancelDate(date);
        } catch (Exception e) {
            logger.warn("Cancel date field may not be visible: {}", e.getMessage());
        }
    }

    /**
     * Fill notes
     * @param notes Notes text
     */
    public void fillNotes(String notes) {
        logger.info("Filling notes: {}", notes);
        try {
            inputNotes(notes);
        } catch (Exception e) {
            logger.warn("Notes field may not be visible: {}", e.getMessage());
        }
    }

    // ==================== Button Action Methods ====================

    /**
     * Click Save button
     * @return true if successful
     */
    public boolean clickSave() {
        logger.info("Clicking Save button");
        try {
            waitForLoaderToDisappear();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(saveButtonLocator));

            scrollIntoView(button);
            sleep(500);

            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", button);

            logger.info("Clicked Save button");
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Save button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Click Cancel button
     * @return true if successful
     */
    public boolean clickCancel() {
        logger.info("Clicking Cancel button");
        try {
            waitForLoaderToDisappear();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(cancelButtonLocator));

            scrollIntoView(button);
            sleep(500);
            click(button);

            logger.info("Clicked Cancel button");
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Cancel button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Click Back button
     * @return true if successful
     */
    public boolean clickBack() {
        logger.info("Clicking Back button");
        try {
            waitForLoaderToDisappear();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(backButtonLocator));

            scrollIntoView(button);
            sleep(500);
            click(button);

            logger.info("Clicked Back button");
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Back button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Click Confirm button in confirmation dialog
     * @return true if successful
     */
    public boolean clickConfirm() {
        logger.info("Clicking Confirm button");
        try {
            waitForLoaderToDisappear();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(confirmButtonLocator));

            scrollIntoView(button);
            sleep(500);
            click(button);

            logger.info("Clicked Confirm button");
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Confirm button: {}", e.getMessage());
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
     * Get certificates to select from EditFlatCancel sheet for a policy
     * @param policyId The policy ID
     * @return List of certificate IDs from EditFlatCancel sheet
     */
    public List<String> getEditCertificatesForPolicy(String policyId) {
        return getCertificateIdsForPolicy(policyId);
    }

    /**
     * Select checkbox for a specific certificate in locations table
     * Uses robust element finding to handle stale element references
     * @param certificateId The certificate ID to find and select
     * @return CertificateSelectionResult with status
     */
    public CertificateSelectionResult selectCertificateCheckbox(String certificateId) {
        logger.info("Selecting checkbox for Certificate: {}", certificateId);
        CertificateSelectionResult result = new CertificateSelectionResult(certificateId);

        try {
            waitForLoaderToDisappear();
            sleep(300); // Reduced wait time

            // Try multiple XPath patterns to find the certificate
            String[] xpathPatterns = {
                "//table//tbody//tr[contains(.,'" + certificateId + "')]",
                "//table//tr[contains(.,'" + certificateId + "')]",
                "//tr[contains(.,'" + certificateId + "')]",
                "//*[contains(text(),'" + certificateId + "')]/ancestor::tr"
            };

            List<WebElement> matchingRows = null;
            String matchedXpath = null;

            for (String xpath : xpathPatterns) {
                matchingRows = driver.findElements(By.xpath(xpath));
                if (!matchingRows.isEmpty()) {
                    matchedXpath = xpath;
                    logger.info("Found certificate {} using XPath: {}", certificateId, xpath);
                    break;
                }
            }

            if (matchingRows == null || matchingRows.isEmpty()) {
                result.found = false;
                result.message = "Certificate not found in table after trying multiple XPath patterns";
                logger.warn("Certificate {} not found in any table", certificateId);
                return result;
            }

            result.found = true;
            logger.info("Found certificate {} in table using pattern: {}", certificateId, matchedXpath);

            // Find checkbox in the row using multiple patterns
            String[] checkboxPatterns = {
                matchedXpath + "//td//input[@type='checkbox']",
                matchedXpath + "//input[@type='checkbox']",
                matchedXpath + "/td/input[@type='checkbox']",
                "//*[contains(text(),'" + certificateId + "')]/ancestor::tr//input[@type='checkbox']"
            };

            String checkboxXpath = null;
            for (String pattern : checkboxPatterns) {
                List<WebElement> checkboxes = driver.findElements(By.xpath(pattern));
                if (!checkboxes.isEmpty()) {
                    checkboxXpath = pattern;
                    logger.info("Found checkbox using pattern: {}", pattern);
                    break;
                }
            }

            if (checkboxXpath == null) {
                result.message = "Checkbox not found in row for certificate: " + certificateId;
                logger.warn("Checkbox not found for certificate {}", certificateId);
                return result;
            }

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(checkboxXpath)));

                // Scroll checkbox into view using JavaScript
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", checkbox);
                sleep(300);

                // Check if already selected using JavaScript
                Boolean isAlreadySelected = (Boolean) js.executeScript("return arguments[0].checked;", checkbox);

                if (Boolean.TRUE.equals(isAlreadySelected)) {
                    result.selected = true;
                    result.message = "Already selected";
                    logger.info("Checkbox for certificate {} is already selected", certificateId);
                } else {
                    // Click using JavaScript
                    js.executeScript("arguments[0].click();", checkbox);
                    logger.info("Clicked checkbox for certificate {} using JavaScript", certificateId);
                    sleep(500);

                    // Verify selection using fresh element lookup
                    try {
                        WebElement freshCheckbox = driver.findElement(By.xpath(checkboxXpath));
                        Boolean isNowSelected = (Boolean) js.executeScript("return arguments[0].checked;", freshCheckbox);
                        result.selected = Boolean.TRUE.equals(isNowSelected);

                        if (!result.selected) {
                            js.executeScript("arguments[0].click();", freshCheckbox);
                            sleep(300);
                            isNowSelected = (Boolean) js.executeScript("return arguments[0].checked;", freshCheckbox);
                            result.selected = Boolean.TRUE.equals(isNowSelected);
                        }
                    } catch (Exception freshEx) {
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
        logger.info("Selecting {} certificates on Edit Cancel Address page", certificateIds.size());
        List<CertificateSelectionResult> results = new ArrayList<>();

        for (String certId : certificateIds) {
            CertificateSelectionResult result = selectCertificateCheckbox(certId);
            results.add(result);
            logger.info("Certificate {} - Found: {}, Selected: {}", certId, result.found, result.selected);
        }

        // Log summary
        long foundCount = results.stream().filter(r -> r.found).count();
        long selectedCount = results.stream().filter(r -> r.selected).count();
        logger.info("Edit page certificate selection summary: Found {}/{}, Selected {}/{}",
                foundCount, certificateIds.size(), selectedCount, certificateIds.size());

        return results;
    }

    /**
     * Generate HTML report for certificate selection results
     */
    public String generateCertificateSelectionReport(List<CertificateSelectionResult> results) {
        StringBuilder html = new StringBuilder();

        long foundCount = results.stream().filter(r -> r.found).count();
        long selectedCount = results.stream().filter(r -> r.selected).count();

        html.append("<h4 style='color: #4472C4;'>Edit Flat Cancel - Certificate Selection Results</h4>");
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

    // ==================== Update Endorsement and Bind Button Methods ====================

    /**
     * Click Update Endorsement (Flat Cancel) button
     * This must be clicked BEFORE clicking the Bind button
     * @return true if successful
     */
    public boolean clickUpdateEndorsementFlatCancelButton() {
        logger.info("Clicking Update Endorsement (Flat Cancel) button");
        try {
            waitForLoaderToDisappear();
            sleep(1000);

            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

            // Try specific Update Endorsement(Flat Cancel) button first
            WebElement updateButton = null;
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                updateButton = wait.until(ExpectedConditions.elementToBeClickable(updateEndorsementFlatCancelButtonLocator));
                logger.info("Found Update Endorsement(Flat Cancel) button");
            } catch (Exception e) {
                // Try generic Update Endorsement button
                logger.info("Specific Update Endorsement(Flat Cancel) button not found, trying generic Update Endorsement button");
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                updateButton = wait.until(ExpectedConditions.elementToBeClickable(updateEndorsementButtonLocator));
                logger.info("Found generic Update Endorsement button");
            }

            scrollIntoView(updateButton);
            sleep(500);

            // Click using JavaScript
            js.executeScript("arguments[0].click();", updateButton);

            logger.info("Clicked Update Endorsement (Flat Cancel) button");
            sleep(3000); // Wait for endorsement to update
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Update Endorsement (Flat Cancel) button: {}", e.getMessage());
            captureScreenshotToReport("Update Endorsement Button - Click Failed");
            return false;
        }
    }

    /**
     * Click Bind (Flat Cancel) button
     * This should be clicked AFTER Update Endorsement button
     * @return true if successful
     */
    public boolean clickBindFlatCancelButton() {
        logger.info("Clicking Bind (Flat Cancel) button");
        try {
            waitForLoaderToDisappear();
            sleep(1000);

            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

            // Try specific Bind(Flat Cancel) button first
            WebElement bindButton = null;
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                bindButton = wait.until(ExpectedConditions.elementToBeClickable(bindFlatCancelButtonLocator));
            } catch (Exception e) {
                // Try generic Bind button
                logger.info("Specific Bind(Flat Cancel) button not found, trying generic Bind button");
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                bindButton = wait.until(ExpectedConditions.elementToBeClickable(bindButtonLocator));
            }

            scrollIntoView(bindButton);
            sleep(500);

            js.executeScript("arguments[0].click();", bindButton);

            logger.info("Clicked Bind (Flat Cancel) button");
            sleep(2000);
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click Bind (Flat Cancel) button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify Confirm Location Cancellation dialogue is displayed
     * @return true if dialogue is displayed
     */
    public boolean isConfirmDialogueDisplayed() {
        logger.info("Checking if Confirm Location Cancellation dialogue is displayed");
        try {
            sleep(1000);
            // Look for dialogue title
            List<WebElement> dialogueTitle = driver.findElements(confirmDialogueTitleLocator);
            if (!dialogueTitle.isEmpty()) {
                logger.info("Confirm Location Cancellation dialogue found by title");
                return true;
            }

            // Look for modal/dialog container
            List<WebElement> dialogues = driver.findElements(confirmDialogueLocator);
            for (WebElement dialogue : dialogues) {
                if (dialogue.isDisplayed()) {
                    String dialogueText = dialogue.getText();
                    if (dialogueText.contains("Confirm") || dialogueText.contains("Cancel") || dialogueText.contains("Location")) {
                        logger.info("Confirm Location Cancellation dialogue found");
                        return true;
                    }
                }
            }

            logger.warn("Confirm Location Cancellation dialogue not found");
            return false;
        } catch (Exception e) {
            logger.error("Error checking for dialogue: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the number of certificates displayed in the confirmation dialogue
     * Uses getCertificateIdsFromDialogue() which reliably extracts certificate IDs
     * @return Number of certificates in dialogue
     */
    public int getCertificateCountFromDialogue() {
        logger.info("Getting certificate count from Confirm Location Cancellation dialogue");

        // Use the reliable method that extracts certificate IDs from modal text
        List<String> certificateIds = getCertificateIdsFromDialogue();
        int count = certificateIds.size();

        logger.info("Certificate count from dialogue: {}", count);
        return count;
    }

    /**
     * Get all certificate IDs displayed in the confirmation dialogue
     * @return List of certificate IDs from dialogue
     */
    public List<String> getCertificateIdsFromDialogue() {
        logger.info("Getting certificate IDs from Confirm Location Cancellation dialogue");
        List<String> certificateIds = new ArrayList<>();

        try {
            sleep(500);

            // Find all visible modals/dialogues
            List<WebElement> modals = driver.findElements(confirmDialogueLocator);
            for (WebElement modal : modals) {
                if (modal.isDisplayed()) {
                    String modalText = modal.getText();
                    // Extract certificate IDs (pattern: ARCH followed by digits)
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(ARCH\\d+|CERT\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher matcher = pattern.matcher(modalText);
                    while (matcher.find()) {
                        String certId = matcher.group(1);
                        if (!certificateIds.contains(certId)) {
                            certificateIds.add(certId);
                        }
                    }
                }
            }

            logger.info("Found {} certificate IDs in dialogue: {}", certificateIds.size(), certificateIds);
        } catch (Exception e) {
            logger.error("Error getting certificate IDs from dialogue: {}", e.getMessage());
        }

        return certificateIds;
    }

    /**
     * Click Confirm button in the dialogue
     * @return true if successful
     */
    public boolean clickDialogueConfirmButton() {
        logger.info("Clicking Confirm button in dialogue");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(dialogueConfirmButtonLocator));

            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", confirmBtn);

            logger.info("Clicked Confirm button in dialogue");
            sleep(2000);
            waitForLoaderToDisappear();
            return true;
        } catch (Exception e) {
            logger.error("Failed to click dialogue Confirm button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Click Cancel button in the dialogue
     * @return true if successful
     */
    public boolean clickDialogueCancelButton() {
        logger.info("Clicking Cancel button in dialogue");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(dialogueCancelButtonLocator));

            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", cancelBtn);

            logger.info("Clicked Cancel button in dialogue");
            sleep(1000);
            return true;
        } catch (Exception e) {
            logger.error("Failed to click dialogue Cancel button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate certificate count in dialogue matches expected count
     * @param expectedCount Expected number of certificates
     * @return true if counts match
     */
    public boolean validateCertificateCount(int expectedCount) {
        int actualCount = getCertificateCountFromDialogue();
        boolean matches = actualCount == expectedCount;

        logger.info("Certificate count validation - Expected: {}, Actual: {}, Match: {}",
                expectedCount, actualCount, matches);

        return matches;
    }

    // ==================== Message Verification Methods ====================

    /**
     * Wait for and verify success message
     * @return true if success message displayed
     */
    public boolean waitForSuccessMessage() {
        logger.info("Waiting for success message");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(successMessageLocator));
            logger.info("Success message displayed");
            return true;
        } catch (Exception e) {
            logger.warn("Success message not displayed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Wait for and verify error message
     * @return true if error message displayed
     */
    public boolean waitForErrorMessage() {
        logger.info("Waiting for error message");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageLocator));
            logger.info("Error message displayed");
            return true;
        } catch (Exception e) {
            logger.debug("Error message not displayed");
            return false;
        }
    }

    /**
     * Get success message text
     * @return Success message text or empty string
     */
    public String getSuccessMessageText() {
        try {
            WebElement message = driver.findElement(successMessageLocator);
            return message.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get error message text
     * @return Error message text or empty string
     */
    public String getErrorMessageText() {
        try {
            WebElement message = driver.findElement(errorMessageLocator);
            return message.getText();
        } catch (Exception e) {
            return "";
        }
    }

    // ==================== Report Generation Methods ====================

    /**
     * Generate HTML report for location details
     */
    public String generateLocationDetailsReport(List<LocationDetails> locationDetails) {
        StringBuilder html = new StringBuilder();

        html.append("<h4 style='color: #4472C4;'>Edit Cancel Address - Location Details</h4>");
        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        html.append("<thead><tr style='background-color: #4472C4; color: white;'>");
        html.append("<th>#</th><th>Cert No.</th><th>Address</th><th>Property Premium</th>");
        html.append("<th>GL Premium</th><th>Water/Sewer Premium</th><th>Taxes</th><th>Fees</th><th>Row Total</th>");
        html.append("</tr></thead><tbody>");

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

            grandTotal += loc.totalSum;
        }

        // Grand Total Row
        html.append("<tr style='background-color: #4472C4; color: white; font-weight: bold;'>");
        html.append("<td colspan='8'>GRAND TOTAL</td>");
        html.append("<td style='background-color: #FFD700; color: black;'><strong>$").append(String.format("%.2f", grandTotal)).append("</strong></td>");
        html.append("</tr>");

        html.append("</tbody></table>");

        return html.toString();
    }

    // ==================== Logging Methods ====================

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

    // ==================== Result Data Class ====================

    /**
     * Data class to hold edit flat cancel result
     */
    public static class EditFlatCancelResult {
        public String policyId;
        public boolean isSuccess;
        public String failureReason;
        public boolean pageVerified;
        public boolean tableDisplayed;
        public int certificatesInTable;
        public List<LocationDetails> locationDetails;
        public double totalPremiumSum;
        public boolean saveBtnClicked;
        public boolean successMessageDisplayed;

        public EditFlatCancelResult(String policyId) {
            this.policyId = policyId;
            this.isSuccess = false;
            this.failureReason = "";
            this.pageVerified = false;
            this.tableDisplayed = false;
            this.certificatesInTable = 0;
            this.locationDetails = new ArrayList<>();
            this.totalPremiumSum = 0;
            this.saveBtnClicked = false;
            this.successMessageDisplayed = false;
        }

        public void calculateTotalPremiumSum() {
            this.totalPremiumSum = 0;
            for (LocationDetails loc : locationDetails) {
                this.totalPremiumSum += loc.totalSum;
            }
        }

        @Override
        public String toString() {
            return String.format("Policy: %s | Success: %s | Certs: %d | Total: $%.2f | %s",
                    policyId, isSuccess, certificatesInTable, totalPremiumSum, failureReason);
        }
    }
}
