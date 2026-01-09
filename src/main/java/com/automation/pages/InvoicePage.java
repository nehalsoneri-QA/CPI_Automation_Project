package com.automation.pages;

import com.automation.utils.PDFReader;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Invoice Page - High-Level Invoice Actions
 * Contains business-level operations that call basic methods from InvoiceLocators
 * This is the class to be used in tests (Cucumber steps, TestNG tests)
 */
public class InvoicePage extends InvoiceLocators {

    // Expected URL path for Invoice page
    private static final String URL_INVOICES = "/invoices";
    private static final String EXPECTED_PAGE_TITLE = "Invoices";

    /**
     * Constructor
     */
    public InvoicePage(WebDriver driver) {
        super(driver);
        logger.info("InvoicePage initialized");
    }

    // ==================== High-Level Search Actions ====================

    /**
     * Search for invoice by Policy ID from Excel (B1 cell)
     * Reads Policy ID from Invoice sheet and enters it in search box
     */
    public void searchByPolicyIdFromExcel() {
        String policyId = getPolicyIdFromExcel();
        logger.info("Searching for Policy ID from Excel: {}", policyId);
        searchByPolicyId(policyId);
    }

    /**
     * Search for invoice by Policy ID
     * @param policyId The policy ID to search for
     */
    public void searchByPolicyId(String policyId) {
        logger.info("Searching for Policy ID: {}", policyId);
        waitForPageLoad();
        clearSearchInput();
        inputSearchText(policyId);
        // Press Enter to trigger search or wait for throttled search
        searchInput.sendKeys(Keys.ENTER);
        sleep(1500); // Wait for throttled search (1000ms throttle in frontend)
        waitForLoaderToDisappear();
        logger.info("Search completed for Policy ID: {}", policyId);
    }

    /**
     * Search by insured name
     * @param insuredName The insured name to search for
     */
    public void searchByInsuredName(String insuredName) {
        logger.info("Searching for Insured Name: {}", insuredName);
        waitForPageLoad();
        clearSearchInput();
        inputSearchText(insuredName);
        searchInput.sendKeys(Keys.ENTER);
        sleep(1500);
        waitForLoaderToDisappear();
        logger.info("Search completed for Insured Name: {}", insuredName);
    }

    /**
     * Clear search and reset results
     */
    public void clearSearch() {
        logger.info("Clearing search");
        clearSearchInput();
        sleep(1500);
        waitForLoaderToDisappear();
    }

    // ==================== Search Verification Methods ====================

    /**
     * Verify search results are displayed after searching
     * @return true if results are found, false if no results
     */
    public boolean verifySearchResultsDisplayed() {
        logger.info("Verifying search results are displayed");
        waitForLoaderToDisappear();
        sleep(1000);

        // Check if table has data rows
        try {
            List<WebElement> rows = driver.findElements(tableRowsLocator);
            boolean hasResults = rows.size() > 0 && !isNoResultsDisplayed();
            logger.info("Search results found: {} rows", rows.size());
            return hasResults;
        } catch (Exception e) {
            logger.warn("Error checking search results: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify search results contain the searched policy ID
     * @param policyId The policy ID to verify in results
     * @return true if policy ID is found in results
     */
    public boolean verifyPolicyIdInResults(String policyId) {
        logger.info("Verifying Policy ID {} in search results", policyId);
        waitForLoaderToDisappear();

        try {
            // Look for policy ID in the table
            By policyLocator = By.xpath("//table//tr/td//button[contains(text(),'" + policyId + "')]");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(policyLocator));
            logger.info("Policy ID {} found in search results", policyId);
            return true;
        } catch (Exception e) {
            logger.warn("Policy ID {} not found in search results", policyId);
            return false;
        }
    }

    /**
     * Search by Policy ID from Excel and verify results
     * @return true if search was successful and results are displayed
     */
    public boolean searchByPolicyIdFromExcelAndVerify() {
        String policyId = getPolicyIdFromExcel();
        logger.info("Searching and verifying Policy ID from Excel: {}", policyId);
        searchByPolicyId(policyId);
        boolean resultsFound = verifySearchResultsDisplayed();
        if (resultsFound) {
            captureScreenshotToReport("Invoice Search Results - " + policyId);
        }
        return resultsFound;
    }

    /**
     * Complete search verification with Policy ID match
     * @return true if Policy ID from Excel is found in search results
     */
    public boolean searchAndVerifyPolicyIdMatch() {
        String policyId = getPolicyIdFromExcel();
        logger.info("Searching and verifying exact Policy ID match: {}", policyId);
        searchByPolicyId(policyId);
        boolean matched = verifyPolicyIdInResults(policyId);
        logger.info("Policy ID {} match verification: {}", policyId, matched ? "PASSED" : "FAILED");
        captureScreenshotToReport("Invoice Search Verification - " + policyId);
        return matched;
    }

    // ==================== Page Navigation & Validation ====================

    /**
     * Check if on Invoice page
     * @return true if current URL contains /invoices
     */
    public boolean isOnInvoicePage() {
        String currentUrl = getCurrentUrl();
        boolean onPage = currentUrl.contains(URL_INVOICES);
        logger.info("On Invoice page: {} (URL: {})", onPage, currentUrl);
        return onPage;
    }

    /**
     * Validate Invoice page is fully loaded
     * @return true if all main elements are displayed
     */
    public boolean validateInvoicePageLoaded() {
        logger.info("Validating Invoice page is loaded");
        waitForPageLoad();

        boolean urlValid = isOnInvoicePage();
        boolean searchDisplayed = isSearchInputDisplayed();
        boolean tableDisplayed = isInvoiceTableDisplayed();

        boolean allValid = urlValid && searchDisplayed && tableDisplayed;
        logger.info("Invoice page validation: URL={}, Search={}, Table={}, Overall={}",
                urlValid, searchDisplayed, tableDisplayed, allValid ? "PASSED" : "FAILED");

        if (allValid) {
            captureScreenshotToReport("Invoice Page Loaded");
        }

        return allValid;
    }

    /**
     * Get the number of rows displayed in the invoice table
     * @return count of table rows
     */
    public int getTableRowCount() {
        waitForLoaderToDisappear();
        List<WebElement> rows = driver.findElements(tableRowsLocator);
        int count = rows.size();
        logger.info("Invoice table row count: {}", count);
        return count;
    }

    // ==================== Pagination Methods ====================

    /**
     * Select page size from dropdown
     * @param size The page size to select (e.g., "10", "25", "50", "100", "250")
     */
    public void selectPageSize(String size) {
        logger.info("Selecting page size: {}", size);
        clickPageSizeSelector();
        sleep(500);

        By optionLocator = By.xpath("//span[text()='" + size + "']");
        WebElement option = driver.findElement(optionLocator);
        click(option);

        waitForLoaderToDisappear();
        waitForTableRows();
        logger.info("Page size set to: {}", size);
    }

    /**
     * Navigate to next page if available
     * @return true if navigation successful, false if no next page
     */
    public boolean goToNextPage() {
        if (isNextPageEnabled()) {
            logger.info("Navigating to next page");
            clickNextPage();
            waitForLoaderToDisappear();
            waitForTableRows();
            return true;
        }
        logger.info("No next page available");
        return false;
    }

    /**
     * Navigate to previous page if available
     * @return true if navigation successful, false if no previous page
     */
    public boolean goToPreviousPage() {
        if (isPreviousPageEnabled()) {
            logger.info("Navigating to previous page");
            clickPreviousPage();
            waitForLoaderToDisappear();
            waitForTableRows();
            return true;
        }
        logger.info("No previous page available");
        return false;
    }

    // ==================== Sorting Methods ====================

    /**
     * Sort table by policy number
     */
    public void sortByPolicyNumber() {
        logger.info("Sorting by policy number");
        clickSortByPolicyNo();
        waitForLoaderToDisappear();
    }

    /**
     * Sort table by insured name
     */
    public void sortByInsuredName() {
        logger.info("Sorting by insured name");
        clickSortByInsuredName();
        waitForLoaderToDisappear();
    }

    // ==================== Policy Row Interaction ====================

    /**
     * Click on a policy to expand its invoices
     * @param policyNumber The policy number to expand
     * @return true if policy was found and clicked
     */
    public boolean expandPolicy(String policyNumber) {
        logger.info("Expanding policy: {}", policyNumber);
        waitForLoaderToDisappear();

        try {
            By policyButtonLocator = By.xpath("//table//tr/td[1]//button[contains(text(),'" + policyNumber + "')]");
            WebElement policyButton = driver.findElement(policyButtonLocator);
            scrollIntoView(policyButton);
            click(policyButton);
            waitForLoaderToDisappear();
            sleep(500);
            logger.info("Policy {} expanded", policyNumber);
            return true;
        } catch (Exception e) {
            logger.error("Failed to expand policy {}: {}", policyNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Collapse an expanded policy
     * @param policyNumber The policy number to collapse
     * @return true if policy was found and collapsed
     */
    public boolean collapsePolicy(String policyNumber) {
        logger.info("Collapsing policy: {}", policyNumber);
        return expandPolicy(policyNumber); // Same click action toggles expansion
    }

    // ==================== Complete Workflow Methods ====================

    /**
     * Complete workflow: Search by Policy ID from Excel and expand the result
     * @return true if search and expand were successful
     */
    public boolean searchAndExpandPolicyFromExcel() {
        String policyId = getPolicyIdFromExcel();
        logger.info("Complete workflow: Search and expand policy {}", policyId);

        searchByPolicyId(policyId);

        if (verifySearchResultsDisplayed()) {
            return expandPolicy(policyId);
        }

        logger.warn("No results found for policy {}", policyId);
        return false;
    }

    /**
     * Get the Policy ID that was read from Excel
     * @return Policy ID string from Excel B1 cell
     */
    public String getSearchedPolicyId() {
        return getPolicyIdFromExcel();
    }

    /**
     * Get all Policy IDs from column B in Invoice sheet
     * @return List of all policy IDs
     */
    public java.util.List<String> getAllPolicyIds() {
        return getAllPolicyIdsFromExcel();
    }

    /**
     * Collapse an expanded policy by clicking on it again
     * This is needed when iterating through multiple policies
     */
    public void collapseExpandedPolicy() {
        logger.info("Collapsing expanded policy");
        try {
            // Check if a policy is expanded (tr[2] exists with nested table)
            List<WebElement> nestedTables = driver.findElements(nestedInvoiceTableLocator);
            if (!nestedTables.isEmpty()) {
                // Click on the first policy button to collapse it
                clickFirstPolicyToExpand();
                sleep(500);
                waitForLoaderToDisappear();
                logger.info("Policy collapsed");
            } else {
                logger.info("No expanded policy to collapse");
            }
        } catch (Exception e) {
            logger.debug("Error collapsing policy: {}", e.getMessage());
        }
    }

    // ==================== Policy Expansion and Invoice Verification Methods ====================

    /**
     * Click on the first policy row to expand it
     * Uses the xpath: //*[@id="root"]/div[2]/div[2]/div/table/tbody/tr[1]/td[1]
     */
    public void clickFirstPolicyToExpand() {
        logger.info("Clicking on first policy to expand");
        waitForLoaderToDisappear();
        WebElement policyButton = driver.findElement(firstPolicyButtonLocator);
        scrollIntoView(policyButton);
        click(policyButton);
        waitForLoaderToDisappear();
        sleep(1000); // Wait for expansion animation
        logger.info("First policy expanded");
    }

    /**
     * Click on the policy that matches the Policy ID from Excel (B1 cell)
     * IMPORTANT: After searching, clicks on the FIRST row (tr[1]) so expanded section appears at tr[2]
     */
    public void clickPolicyFromExcelToExpand() {
        String policyId = getPolicyIdFromExcel();
        logger.info("Policy ID from Excel: {} - will click on first search result (tr[1])", policyId);

        // After searching, always click on the first row (tr[1])
        // This ensures the expanded invoice section appears at tr[2]
        clickFirstPolicyToExpand();

        // Wait for the nested invoice table to appear
        waitForNestedInvoiceTable();
    }

    /**
     * Click on a specific policy by its Policy ID to expand it
     * NOTE: For tr[2] xpath to work, we must click on tr[1] (first row)
     * @param policyId The Policy ID to find and click
     */
    public void clickPolicyToExpand(String policyId) {
        logger.info("Clicking on policy to expand: {}", policyId);
        waitForLoaderToDisappear();

        // Always click on the first row (tr[1]) so expanded section is at tr[2]
        // The search should have already filtered to show the desired policy first
        clickFirstPolicyToExpand();

        // Wait for the nested invoice table to appear
        waitForNestedInvoiceTable();

        logger.info("Policy expanded - invoice section should now be at tr[2]");
    }

    /**
     * Wait for the nested invoice table to appear after expanding a policy
     */
    public void waitForNestedInvoiceTable() {
        logger.info("Waiting for nested invoice table to appear...");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.presenceOfElementLocated(nestedInvoiceTableLocator));
            sleep(1000); // Additional wait for table data to load
            logger.info("Nested invoice table found");
        } catch (Exception e) {
            logger.warn("Timeout waiting for nested invoice table: {}", e.getMessage());
        }
    }

    /**
     * Get the total invoice count from the expanded policy
     * Reads from xpath: //*[@id="root"]/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[3]/div[1]/p
     * @return invoice count as integer, or 0 if not found
     */
    public int getInvoiceCount() {
        logger.info("Getting invoice count from expanded policy");
        waitForLoaderToDisappear();
        try {
            WebElement countElement = driver.findElement(invoiceCountLocator);
            String countText = countElement.getText().trim();
            logger.info("Invoice count text: {}", countText);

            // Extract number from text like "5 Invoices" or "1 Invoice"
            String numberOnly = countText.replaceAll("[^0-9]", "");
            int count = numberOnly.isEmpty() ? 0 : Integer.parseInt(numberOnly);
            logger.info("Parsed invoice count: {}", count);
            return count;
        } catch (Exception e) {
            logger.warn("Could not get invoice count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get all invoice rows from the expanded policy table
     * @return List of WebElements representing invoice rows
     */
    public List<WebElement> getInvoiceRows() {
        waitForLoaderToDisappear();
        sleep(1000); // Wait for data to load
        try {
            // First check if nested table exists
            List<WebElement> nestedTables = driver.findElements(nestedInvoiceTableLocator);
            logger.info("Found {} nested invoice tables", nestedTables.size());

            if (nestedTables.isEmpty()) {
                logger.warn("No nested invoice table found - trying alternative locators");
                // Try alternative xpath
                By altLocator = By.xpath("//table//table/tbody/tr");
                List<WebElement> rows = driver.findElements(altLocator);
                logger.info("Alternative locator found {} rows", rows.size());
                return rows;
            }

            // Get rows from the nested table
            List<WebElement> rows = driver.findElements(invoiceTableRowsLocator);
            logger.info("Found {} invoice rows using primary locator", rows.size());

            // If no rows found, try from the nested table directly
            if (rows.isEmpty() && !nestedTables.isEmpty()) {
                WebElement nestedTable = nestedTables.get(0);
                rows = nestedTable.findElements(By.xpath(".//tbody/tr"));
                logger.info("Found {} invoice rows from nested table element", rows.size());
            }

            // Debug: log first row content if found
            if (!rows.isEmpty()) {
                try {
                    String firstRowText = rows.get(0).getText();
                    logger.debug("First row content: {}", firstRowText.substring(0, Math.min(100, firstRowText.length())));
                } catch (Exception e) {
                    logger.debug("Could not get first row text");
                }
            }

            return rows;
        } catch (Exception e) {
            logger.warn("Could not find invoice rows: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Data class to hold invoice row information
     * Table columns: Checkbox | Invoice ID | Certificate | Location | Due Amount | Invoice Amount | Due Date | Status
     */
    public static class InvoiceRowData {
        public String invoiceId;
        public String certificate;
        public String location;        // Property address/location
        public String dueAmount;       // Column 5 - Due Amount (e.g., $0.00)
        public String invoiceAmount;   // Column 6 - Invoice Amount (can be negative)
        public String dueDate;         // Column 7 - Due Date (e.g., 2025-12-03)
        public String status;          // Column 8 - Status (PAID_IN_FULL, INSTALLMENTS, etc.)
        public double dueAmountValue;
        public double invoiceAmountValue;
        public boolean isNegativeDue;           // Due Amount is negative
        public boolean isNegativeInvoice;       // Invoice Amount is negative
        public boolean isZeroDueAndZeroInvoice; // Both Due and Invoice amounts are zero

        @Override
        public String toString() {
            return String.format("Invoice ID: %s | Certificate: %s | Location: %s | Due Amt: %s | Invoice Amt: %s | Due Date: %s | Status: %s",
                    invoiceId, certificate, location, dueAmount, invoiceAmount, dueDate, status);
        }

        public String toHtmlTableRow() {
            String rowClass = "";
            if (isNegativeDue || isNegativeInvoice) {
                rowClass = "style='background-color: #ffcccc;'"; // Light red for negative
            } else if (isZeroDueAndZeroInvoice) {
                rowClass = "style='background-color: #ffffcc;'"; // Light yellow for zero/zero
            }
            return String.format("<tr %s><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                    rowClass, invoiceId, certificate, location, dueAmount, invoiceAmount, dueDate, status);
        }

        /**
         * Check if this invoice has an issue (negative due/invoice OR zero due & zero invoice)
         */
        public boolean hasIssue() {
            return isNegativeDue || isNegativeInvoice || isZeroDueAndZeroInvoice;
        }

        /**
         * Get issue type description
         */
        public String getIssueType() {
            if (isNegativeDue) return "NEGATIVE DUE AMOUNT";
            if (isNegativeInvoice) return "NEGATIVE INVOICE AMOUNT";
            if (isZeroDueAndZeroInvoice) return "ZERO DUE & ZERO INVOICE";
            return "NONE";
        }
    }

    /**
     * Parse invoice row data from a WebElement row
     * Actual Columns: 0-Checkbox, 1-Invoice ID, 2-Certificate, 3-Location, 4-Due Amount, 5-Invoice Amount, 6-Due Date, 7-Status
     * @param row WebElement representing a table row
     * @return InvoiceRowData object with parsed values
     */
    public InvoiceRowData parseInvoiceRow(WebElement row) {
        InvoiceRowData data = new InvoiceRowData();
        try {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            logger.debug("Found {} cells in row", cells.size());

            if (cells.size() >= 7) {
                // Correct column mapping based on actual table structure
                data.invoiceId = cells.get(1).getText().trim();      // Column 2: Invoice ID
                data.certificate = cells.get(2).getText().trim();    // Column 3: Certificate
                data.location = cells.get(3).getText().trim();       // Column 4: Location/Address
                data.dueAmount = cells.get(4).getText().trim();      // Column 5: Due Amount ($0.00)
                data.invoiceAmount = cells.get(5).getText().trim();  // Column 6: Invoice Amount (can be negative)
                data.dueDate = cells.get(6).getText().trim();        // Column 7: Due Date (2025-12-03)

                // Get Status if available (column 8, index 7)
                if (cells.size() >= 8) {
                    data.status = cells.get(7).getText().trim();     // Column 8: Status (PAID_IN_FULL, etc.)
                } else {
                    data.status = "N/A";
                }

                // Parse Due Amount value to check if negative
                String dueAmountStr = data.dueAmount.replace("$", "").replace(",", "").replace("-", "").trim();
                boolean isDueNegative = data.dueAmount.contains("-");
                try {
                    data.dueAmountValue = Double.parseDouble(dueAmountStr);
                    if (isDueNegative) data.dueAmountValue = -data.dueAmountValue;
                    data.isNegativeDue = data.dueAmountValue < 0;
                } catch (NumberFormatException e) {
                    data.dueAmountValue = 0;
                    data.isNegativeDue = false;
                }

                // Parse Invoice Amount value to check if negative
                String invoiceAmountStr = data.invoiceAmount.replace("$", "").replace(",", "").replace("-", "").trim();
                boolean isInvoiceNegative = data.invoiceAmount.contains("-");
                try {
                    data.invoiceAmountValue = Double.parseDouble(invoiceAmountStr);
                    if (isInvoiceNegative) data.invoiceAmountValue = -data.invoiceAmountValue;
                    data.isNegativeInvoice = data.invoiceAmountValue < 0;
                } catch (NumberFormatException e) {
                    data.invoiceAmountValue = 0;
                    data.isNegativeInvoice = false;
                }

                // Check if both Due Amount and Invoice Amount are zero (no payment due or owed)
                data.isZeroDueAndZeroInvoice = (data.dueAmountValue == 0.00 && data.invoiceAmountValue == 0.00);

                logger.debug("Parsed invoice: ID={}, DueAmt={}, InvAmt={}, isNegDue={}, isNegInv={}, isZeroZero={}",
                        data.invoiceId, data.dueAmountValue, data.invoiceAmountValue,
                        data.isNegativeDue, data.isNegativeInvoice, data.isZeroDueAndZeroInvoice);
            }
        } catch (Exception e) {
            logger.warn("Error parsing invoice row: {}", e.getMessage());
        }
        return data;
    }

    /**
     * Get all invoice data from the expanded policy
     * @return List of InvoiceRowData objects
     */
    public List<InvoiceRowData> getAllInvoiceData() {
        List<InvoiceRowData> invoiceDataList = new java.util.ArrayList<>();
        List<WebElement> rows = getInvoiceRows();

        for (WebElement row : rows) {
            InvoiceRowData data = parseInvoiceRow(row);
            if (data.invoiceId != null && !data.invoiceId.isEmpty()) {
                invoiceDataList.add(data);
                logger.debug("Parsed invoice: {}", data);
            }
        }

        logger.info("Parsed {} invoices total", invoiceDataList.size());
        return invoiceDataList;
    }

    /**
     * Get only invoices with negative amounts (Due Amount OR Invoice Amount is negative)
     * @return List of InvoiceRowData objects with negative amounts
     */
    public List<InvoiceRowData> getNegativeDueInvoices() {
        List<InvoiceRowData> allInvoices = getAllInvoiceData();
        List<InvoiceRowData> negativeInvoices = new java.util.ArrayList<>();

        for (InvoiceRowData invoice : allInvoices) {
            if (invoice.isNegativeDue || invoice.isNegativeInvoice) {
                negativeInvoices.add(invoice);
                logger.info("Found negative invoice: {} | Due: {} | Invoice Amt: {} | Type: {}",
                        invoice.invoiceId, invoice.dueAmount, invoice.invoiceAmount, invoice.getIssueType());
            }
        }

        logger.info("Found {} invoices with negative amounts", negativeInvoices.size());
        return negativeInvoices;
    }

    /**
     * Get invoices where both Due Amount and Invoice Amount are $0.00
     * @return List of InvoiceRowData objects with zero due and zero invoice
     */
    public List<InvoiceRowData> getZeroDueZeroPaidInvoices() {
        List<InvoiceRowData> allInvoices = getAllInvoiceData();
        List<InvoiceRowData> zeroZeroInvoices = new java.util.ArrayList<>();

        for (InvoiceRowData invoice : allInvoices) {
            if (invoice.isZeroDueAndZeroInvoice) {
                zeroZeroInvoices.add(invoice);
                logger.info("Found zero due & zero invoice: {} | Due: {} | Invoice Amt: {}",
                        invoice.invoiceId, invoice.dueAmount, invoice.invoiceAmount);
            }
        }

        logger.info("Found {} invoices with zero due AND zero invoice amounts", zeroZeroInvoices.size());
        return zeroZeroInvoices;
    }

    /**
     * Get all invoices with any issue (negative due OR zero due & zero paid)
     * @return List of InvoiceRowData objects with issues
     */
    public List<InvoiceRowData> getInvoicesWithIssues() {
        List<InvoiceRowData> allInvoices = getAllInvoiceData();
        List<InvoiceRowData> issueInvoices = new java.util.ArrayList<>();

        for (InvoiceRowData invoice : allInvoices) {
            if (invoice.hasIssue()) {
                issueInvoices.add(invoice);
                logger.info("Found invoice with issue: {} | Type: {} | Due: {} | Invoice Amt: {}",
                        invoice.invoiceId, invoice.getIssueType(), invoice.dueAmount, invoice.invoiceAmount);
            }
        }

        logger.info("Found {} invoices with issues (negative due or zero/zero)", issueInvoices.size());
        return issueInvoices;
    }

    // ==================== Invoice Pagination Methods ====================

    /**
     * Scroll down inside the expanded invoice section to find the Next button
     */
    private void scrollToInvoiceNextButton() {
        try {
            // First scroll the invoice table container to bottom to reveal pagination
            WebElement invoiceContainer = driver.findElement(By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div"));
            if (invoiceContainer != null) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollTop = arguments[0].scrollHeight;", invoiceContainer);
                sleep(500);
                logger.debug("Scrolled invoice container to bottom");
            }
        } catch (Exception e) {
            logger.debug("Could not scroll invoice container: {}", e.getMessage());
        }

        try {
            // Now scroll the Next button into view
            WebElement nextButton = driver.findElement(invoiceNextButtonLocator);
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", nextButton);
            sleep(500);
            logger.debug("Scrolled Next button into view");
        } catch (Exception e) {
            logger.debug("Could not scroll to Next button: {}", e.getMessage());
        }
    }

    /**
     * Check if the invoice Next button is enabled (inside expanded policy)
     * @return true if Next button exists and is enabled
     */
    public boolean isInvoiceNextButtonEnabled() {
        try {
            // First scroll to make the Next button visible
            scrollToInvoiceNextButton();
            sleep(300);

            List<WebElement> nextButtons = driver.findElements(invoiceNextButtonLocator);
            if (nextButtons.isEmpty()) {
                logger.debug("Invoice Next button not found at xpath");
                return false;
            }
            WebElement nextButton = nextButtons.get(0);

            // Scroll the specific button into view
            scrollIntoView(nextButton);
            sleep(300);

            String classAttr = nextButton.getAttribute("class");
            String disabledAttr = nextButton.getAttribute("disabled");
            String ariaDisabled = nextButton.getAttribute("aria-disabled");
            boolean isDisplayed = nextButton.isDisplayed();
            boolean isEnabled = nextButton.isEnabled();

            // Check for disabled attribute (most reliable)
            boolean hasDisabledAttr = "true".equals(disabledAttr) || "disabled".equals(disabledAttr);
            boolean hasAriaDisabled = "true".equals(ariaDisabled);

            // Check for cursor-not-allowed class (indicates visually disabled)
            boolean hasCursorNotAllowed = classAttr != null && classAttr.contains("cursor-not-allowed");

            // Button is enabled if: displayed, Selenium says enabled, no disabled attribute, no cursor-not-allowed
            boolean enabled = isDisplayed && isEnabled && !hasDisabledAttr && !hasAriaDisabled && !hasCursorNotAllowed;

            logger.info("Invoice Next button - displayed:{}, isEnabled:{}, disabledAttr:{}, ariaDisabled:{}, cursorNotAllowed:{}, FINAL:{}",
                    isDisplayed, isEnabled, hasDisabledAttr, hasAriaDisabled, hasCursorNotAllowed, enabled);
            logger.debug("Button class: {}", classAttr);

            return enabled;
        } catch (Exception e) {
            logger.debug("Invoice Next button not found or error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Wait for invoice table data to refresh after pagination
     */
    private void waitForInvoiceTableRefresh() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Wait for loader to disappear
            waitForLoaderToDisappear();

            // Wait for invoice rows to be present
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(invoiceTableRowsLocator));

            // Additional wait for data to fully load
            sleep(2000);

            logger.debug("Invoice table data refreshed");
        } catch (Exception e) {
            logger.warn("Timeout waiting for invoice table refresh: {}", e.getMessage());
        }
    }

    /**
     * Click the invoice Next button to go to next page of invoices
     * @return true if click was successful
     */
    public boolean clickInvoiceNextButton() {
        try {
            // First scroll to the Next button
            scrollToInvoiceNextButton();

            List<WebElement> nextButtons = driver.findElements(invoiceNextButtonLocator);
            if (nextButtons.isEmpty()) {
                logger.info("Invoice Next button not found");
                return false;
            }

            WebElement nextButton = nextButtons.get(0);

            // Scroll the button into view
            scrollIntoView(nextButton);
            sleep(500);

            // Check if button is enabled before clicking
            String disabledAttr = nextButton.getAttribute("disabled");
            boolean hasCursorNotAllowed = nextButton.getAttribute("class") != null &&
                    nextButton.getAttribute("class").contains("cursor-not-allowed");

            if ("true".equals(disabledAttr) || "disabled".equals(disabledAttr) || hasCursorNotAllowed) {
                logger.info("Invoice Next button is disabled - cannot click");
                return false;
            }

            // Click using JavaScript for reliability
            logger.info("Clicking invoice Next button...");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);

            logger.info("Clicked invoice Next button - waiting for data to load...");

            // Wait for data to refresh after clicking
            waitForInvoiceTableRefresh();

            logger.info("Invoice Next button clicked successfully - moved to next page");
            return true;
        } catch (Exception e) {
            logger.warn("Error clicking invoice Next button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all invoices from ALL pages (iterates through pagination)
     * Clicks Next button until it becomes disabled
     * @return List of all InvoiceRowData objects from all pages
     */
    public List<InvoiceRowData> getAllInvoiceDataFromAllPages() {
        List<InvoiceRowData> allInvoices = new java.util.ArrayList<>();
        int pageNumber = 1;
        int maxPages = 100; // Safety limit to prevent infinite loops
        int consecutiveEmptyPages = 0;

        logger.info("=== Starting invoice collection from all pages ===");

        while (pageNumber <= maxPages) {
            logger.info("Processing invoice page {} ...", pageNumber);

            // Wait for data to be available on current page
            waitForInvoiceTableRefresh();

            // Get invoices from current page
            List<InvoiceRowData> pageInvoices = getAllInvoiceData();

            if (pageInvoices.isEmpty()) {
                consecutiveEmptyPages++;
                logger.warn("No invoices found on page {} (consecutive empty: {})", pageNumber, consecutiveEmptyPages);
                if (consecutiveEmptyPages >= 2) {
                    logger.info("Stopping pagination - multiple consecutive empty pages");
                    break;
                }
            } else {
                consecutiveEmptyPages = 0;
                allInvoices.addAll(pageInvoices);
                logger.info("Found {} invoices on page {} (Total so far: {})",
                        pageInvoices.size(), pageNumber, allInvoices.size());
            }

            // Check if Next button is enabled
            logger.info("Checking if Next button is enabled for page {}...", pageNumber + 1);

            if (isInvoiceNextButtonEnabled()) {
                logger.info("Next button is ENABLED - clicking to go to page {}", pageNumber + 1);
                boolean clicked = clickInvoiceNextButton();

                if (clicked) {
                    pageNumber++;
                    logger.info("Successfully moved to page {}", pageNumber);
                } else {
                    logger.info("Failed to click Next button - stopping pagination");
                    break;
                }
            } else {
                logger.info("Next button is DISABLED - reached last page (page {})", pageNumber);
                break;
            }
        }

        logger.info("=== Invoice collection complete ===");
        logger.info("Total pages processed: {}", pageNumber);
        logger.info("Total invoices collected: {}", allInvoices.size());

        return allInvoices;
    }

    /**
     * Get all invoices with issues from ALL pages
     * @return List of InvoiceRowData objects with issues from all pages
     */
    public List<InvoiceRowData> getInvoicesWithIssuesFromAllPages() {
        List<InvoiceRowData> allInvoices = getAllInvoiceDataFromAllPages();
        List<InvoiceRowData> issueInvoices = new java.util.ArrayList<>();

        for (InvoiceRowData invoice : allInvoices) {
            if (invoice.hasIssue()) {
                issueInvoices.add(invoice);
            }
        }

        logger.info("Found {} invoices with issues from all pages", issueInvoices.size());
        return issueInvoices;
    }

    /**
     * Get negative due invoices from ALL pages
     * @return List of negative due invoices from all pages
     */
    public List<InvoiceRowData> getNegativeDueInvoicesFromAllPages() {
        List<InvoiceRowData> allInvoices = getAllInvoiceDataFromAllPages();
        List<InvoiceRowData> negativeInvoices = new java.util.ArrayList<>();

        for (InvoiceRowData invoice : allInvoices) {
            if (invoice.isNegativeDue || invoice.isNegativeInvoice) {
                negativeInvoices.add(invoice);
            }
        }

        logger.info("Found {} negative invoices from all pages", negativeInvoices.size());
        return negativeInvoices;
    }

    /**
     * Get zero due & zero invoice invoices from ALL pages
     * @return List of zero/zero invoices from all pages
     */
    public List<InvoiceRowData> getZeroDueZeroPaidInvoicesFromAllPages() {
        List<InvoiceRowData> allInvoices = getAllInvoiceDataFromAllPages();
        List<InvoiceRowData> zeroZeroInvoices = new java.util.ArrayList<>();

        for (InvoiceRowData invoice : allInvoices) {
            if (invoice.isZeroDueAndZeroInvoice) {
                zeroZeroInvoices.add(invoice);
            }
        }

        logger.info("Found {} zero due & zero invoice invoices from all pages", zeroZeroInvoices.size());
        return zeroZeroInvoices;
    }

    /**
     * Expand policy, get invoice count, check for negative dues, and log to report
     * @return Summary string with results
     */
    public String verifyInvoicesAndLogNegativeDues() {
        logger.info("Starting invoice verification for negative due amounts");
        StringBuilder reportSummary = new StringBuilder();

        // Step 1: Click to expand first policy
        clickFirstPolicyToExpand();
        captureScreenshotToReport("Policy Expanded - Invoice Table Visible");

        // Step 2: Get invoice count
        int invoiceCount = getInvoiceCount();
        reportSummary.append("Total Invoices: ").append(invoiceCount).append("\n");
        logger.info("Total invoice count: {}", invoiceCount);

        // Log invoice count to report
        logInfoToReport("Total Invoice Count: " + invoiceCount);

        // Step 3: Get all invoices and check for negative dues
        List<InvoiceRowData> allInvoices = getAllInvoiceData();
        List<InvoiceRowData> negativeInvoices = getNegativeDueInvoices();

        reportSummary.append("Negative Invoices: ").append(negativeInvoices.size()).append("\n");

        // Step 4: Log negative invoices to report
        if (negativeInvoices.isEmpty()) {
            logInfoToReport("No invoices with negative amounts found.");
            captureScreenshotToReport("No Negative Invoices Found");
        } else {
            // Create HTML table for negative invoices
            StringBuilder htmlTable = new StringBuilder();
            htmlTable.append("<h4>Invoices with Negative Amounts (").append(negativeInvoices.size()).append(" found)</h4>");
            htmlTable.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
            htmlTable.append("<thead><tr style='background-color: #f0f0f0;'>");
            htmlTable.append("<th>Invoice ID</th><th>Certificate</th><th>Location</th>");
            htmlTable.append("<th>Due Amount</th><th>Invoice Amount</th><th>Due Date</th><th>Status</th>");
            htmlTable.append("</tr></thead><tbody>");

            for (InvoiceRowData invoice : negativeInvoices) {
                htmlTable.append(invoice.toHtmlTableRow());
                reportSummary.append("  - ").append(invoice.toString()).append("\n");
            }

            htmlTable.append("</tbody></table>");

            // Log HTML table to Extent Report
            logHtmlToReport(htmlTable.toString());
            captureScreenshotToReport("Negative Invoices Found: " + negativeInvoices.size());
        }

        // Step 5: Also log all invoices summary
        StringBuilder allInvoicesSummary = new StringBuilder();
        allInvoicesSummary.append("<h4>All Invoices Summary (").append(allInvoices.size()).append(" total)</h4>");
        allInvoicesSummary.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        allInvoicesSummary.append("<thead><tr style='background-color: #e0e0e0;'>");
        allInvoicesSummary.append("<th>Invoice ID</th><th>Certificate</th><th>Location</th>");
        allInvoicesSummary.append("<th>Due Amount</th><th>Invoice Amount</th><th>Due Date</th><th>Status</th>");
        allInvoicesSummary.append("</tr></thead><tbody>");

        for (InvoiceRowData invoice : allInvoices) {
            allInvoicesSummary.append(invoice.toHtmlTableRow());
        }
        allInvoicesSummary.append("</tbody></table>");

        logHtmlToReport(allInvoicesSummary.toString());

        return reportSummary.toString();
    }

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
                test.info(com.aventstack.extentreports.markuputils.MarkupHelper.createLabel(
                    "Invoice Details", com.aventstack.extentreports.markuputils.ExtentColor.BLUE));
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

    // ==================== PDF Download and Verification Methods ====================

    /**
     * Data class to hold aggregated certificate data from UI
     * Aggregates multiple invoice entries per certificate by summing their amounts
     */
    public static class CertificateAggregateData {
        public String certificate;
        public String location;
        public double totalDueAmount;
        public double totalInvoiceAmount;
        public double totalCombined;  // Sum of Due Amount + Invoice Amount
        public int entryCount;  // Number of invoice entries for this certificate

        public CertificateAggregateData(String certificate) {
            this.certificate = certificate;
            this.location = "";
            this.totalDueAmount = 0;
            this.totalInvoiceAmount = 0;
            this.totalCombined = 0;
            this.entryCount = 0;
        }

        public void addEntry(InvoiceRowData invoice) {
            if (this.location.isEmpty() && invoice.location != null && !invoice.location.isEmpty()) {
                this.location = invoice.location;
            }
            this.totalDueAmount += invoice.dueAmountValue;
            this.totalInvoiceAmount += invoice.invoiceAmountValue;
            this.entryCount++;
            this.totalCombined = this.totalDueAmount + this.totalInvoiceAmount;
        }

        @Override
        public String toString() {
            return String.format("CERT: %s | Location: %s | Total: $%.2f (Due: $%.2f + Invoice: $%.2f) | Entries: %d",
                    certificate, location, totalCombined, totalDueAmount, totalInvoiceAmount, entryCount);
        }
    }

    /**
     * Data class to hold PDF vs UI comparison result for a single certificate
     */
    public static class CertificateComparisonResult {
        public String certificate;
        public String uiLocation;
        public String pdfPropertyAddress;
        public double uiTotal;
        public double pdfTotal;
        public boolean locationMatches;
        public boolean totalMatches;
        public boolean isMatch;
        public String mismatchReason;

        public CertificateComparisonResult(String certificate) {
            this.certificate = certificate;
            this.mismatchReason = "";
        }

        public String toHtmlTableRow() {
            String rowClass = isMatch ? "style='background-color: #90EE90; color: black;'"
                                      : "style='background-color: #FFB6C1; color: black;'";
            String statusIcon = isMatch ? "&#10004;" : "&#10008;";
            return String.format("<tr %s><td>%s</td><td>%s</td><td>%s</td><td>$%.2f</td><td>$%.2f</td><td>%s %s</td><td>%s</td></tr>",
                    rowClass, certificate, uiLocation, pdfPropertyAddress, uiTotal, pdfTotal,
                    statusIcon, isMatch ? "MATCH" : "MISMATCH", mismatchReason);
        }
    }

    /**
     * Click the download button to download the PDF
     * The button has ID pattern: invoice-list-action-download-{id}
     * @return true if button was clicked successfully
     */
    public boolean clickDownloadButton() {
        logger.info("Clicking download button to download PDF");
        waitForLoaderToDisappear();

        try {
            // Find download button using the locator
            List<WebElement> downloadButtons = driver.findElements(downloadButtonLocator);
            if (downloadButtons.isEmpty()) {
                logger.warn("No download button found");
                return false;
            }

            WebElement downloadButton = downloadButtons.get(0);
            logger.info("Found download button with ID: {}", downloadButton.getAttribute("id"));

            // Scroll the button into view
            scrollIntoView(downloadButton);
            sleep(500);

            // Click using JavaScript for reliability
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", downloadButton);
            logger.info("Clicked download button - PDF download initiated");

            // Wait for download to start
            sleep(2000);

            return true;
        } catch (Exception e) {
            logger.error("Error clicking download button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Click a specific download button by ID
     * @param buttonId The button ID (e.g., "invoice-list-action-download-207")
     * @return true if button was clicked successfully
     */
    public boolean clickDownloadButtonById(String buttonId) {
        logger.info("Clicking download button by ID: {}", buttonId);
        waitForLoaderToDisappear();

        try {
            By specificButton = By.id(buttonId);
            WebElement downloadButton = driver.findElement(specificButton);

            // Scroll the button into view
            scrollIntoView(downloadButton);
            sleep(500);

            // Click using JavaScript for reliability
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", downloadButton);
            logger.info("Clicked download button - PDF download initiated");

            // Wait for download to start
            sleep(2000);

            return true;
        } catch (Exception e) {
            logger.error("Error clicking download button by ID: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Aggregate UI invoice data by certificate number
     * Sums Due Amount + Invoice Amount for each unique certificate
     * @param invoices List of invoice data from UI
     * @return Map of certificate number to aggregated data
     */
    public Map<String, CertificateAggregateData> aggregateInvoicesByCertificate(List<InvoiceRowData> invoices) {
        Map<String, CertificateAggregateData> aggregatedData = new HashMap<>();

        for (InvoiceRowData invoice : invoices) {
            String cert = invoice.certificate;
            if (cert == null || cert.isEmpty()) {
                continue;
            }

            CertificateAggregateData aggregate = aggregatedData.get(cert);
            if (aggregate == null) {
                aggregate = new CertificateAggregateData(cert);
                aggregatedData.put(cert, aggregate);
            }
            aggregate.addEntry(invoice);
        }

        logger.info("Aggregated {} unique certificates from {} invoices", aggregatedData.size(), invoices.size());
        return aggregatedData;
    }

    /**
     * Compare UI aggregated data with PDF certificate data
     * @param uiData Map of UI aggregated data by certificate
     * @param pdfData Map of PDF certificate data
     * @return List of comparison results
     */
    public List<CertificateComparisonResult> compareUIWithPDF(
            Map<String, CertificateAggregateData> uiData,
            Map<String, PDFReader.PDFCertificateData> pdfData) {

        List<CertificateComparisonResult> results = new java.util.ArrayList<>();

        // Compare each UI certificate with PDF
        for (String cert : uiData.keySet()) {
            CertificateAggregateData uiCert = uiData.get(cert);
            PDFReader.PDFCertificateData pdfCert = pdfData.get(cert);

            CertificateComparisonResult result = new CertificateComparisonResult(cert);
            result.uiLocation = uiCert.location;
            result.uiTotal = uiCert.totalCombined;

            if (pdfCert == null) {
                result.isMatch = false;
                result.pdfPropertyAddress = "NOT FOUND IN PDF";
                result.pdfTotal = 0;
                result.mismatchReason = "Certificate not found in PDF";
                logger.warn("Certificate {} from UI not found in PDF", cert);
            } else {
                result.pdfPropertyAddress = pdfCert.propertyAddress;
                result.pdfTotal = pdfCert.totalValue;

                // Compare locations (partial match due to formatting differences)
                result.locationMatches = compareLocations(uiCert.location, pdfCert.propertyAddress);

                // Compare totals (with 1.0 tolerance for rounding issues)
                double totalDifference = Math.abs(uiCert.totalCombined - pdfCert.totalValue);
                result.totalMatches = totalDifference <= 1.00;

                result.isMatch = result.locationMatches && result.totalMatches;

                if (!result.isMatch) {
                    StringBuilder reason = new StringBuilder();
                    if (!result.locationMatches) {
                        reason.append("Location mismatch; ");
                    }
                    if (!result.totalMatches) {
                        reason.append(String.format("Total mismatch (UI: $%.2f vs PDF: $%.2f, Diff: $%.2f > $1.00 tolerance)",
                                uiCert.totalCombined, pdfCert.totalValue, totalDifference));
                    }
                    result.mismatchReason = reason.toString();
                    logger.warn("Certificate {} MISMATCH: {}", cert, result.mismatchReason);
                } else {
                    if (totalDifference > 0) {
                        logger.info("Certificate {} MATCH: Total within tolerance (Diff: $%.2f)", cert, totalDifference);
                    } else {
                        logger.info("Certificate {} MATCH: Location and Total verified", cert);
                    }
                }
            }

            results.add(result);
        }

        // Check for PDF certificates not in UI
        for (String pdfCert : pdfData.keySet()) {
            if (!uiData.containsKey(pdfCert)) {
                CertificateComparisonResult result = new CertificateComparisonResult(pdfCert);
                result.uiLocation = "NOT FOUND IN UI";
                result.uiTotal = 0;
                result.pdfPropertyAddress = pdfData.get(pdfCert).propertyAddress;
                result.pdfTotal = pdfData.get(pdfCert).totalValue;
                result.isMatch = false;
                result.mismatchReason = "Certificate exists in PDF but not in UI";
                results.add(result);
                logger.warn("Certificate {} in PDF not found in UI", pdfCert);
            }
        }

        return results;
    }

    /**
     * Compare UI location with PDF property address
     * Uses partial matching due to formatting differences
     */
    private boolean compareLocations(String uiLocation, String pdfAddress) {
        if (uiLocation == null || pdfAddress == null) {
            return false;
        }

        // Normalize both strings for comparison
        String normalizedUI = uiLocation.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();
        String normalizedPDF = pdfAddress.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();

        // Check if one contains significant parts of the other
        if (normalizedUI.equals(normalizedPDF)) {
            return true;
        }

        // Extract key address components and check for partial match
        String[] uiParts = normalizedUI.split(" ");
        String[] pdfParts = normalizedPDF.split(" ");

        // Check if at least street number and street name match
        if (uiParts.length >= 2 && pdfParts.length >= 2) {
            // Check first two significant words (usually street number and name)
            boolean firstMatch = uiParts[0].equals(pdfParts[0]) || normalizedPDF.contains(uiParts[0]);
            boolean secondMatch = uiParts.length > 1 && pdfParts.length > 1 &&
                    (uiParts[1].equals(pdfParts[1]) || normalizedPDF.contains(uiParts[1]));

            if (firstMatch && secondMatch) {
                return true;
            }
        }

        // Check if PDF contains the UI address or vice versa
        return normalizedPDF.contains(normalizedUI) || normalizedUI.contains(normalizedPDF);
    }

    /**
     * Complete PDF verification workflow:
     * 1. Click download button
     * 2. Wait for PDF to download
     * 3. Parse PDF content
     * 4. Compare with UI data
     * @param allInvoices List of all invoices from UI (from all pages)
     * @return Comparison results
     */
    public List<CertificateComparisonResult> downloadAndVerifyPDF(List<InvoiceRowData> allInvoices) {
        logger.info("=== Starting PDF Download and Verification ===");

        // Step 1: Click download button
        boolean downloadClicked = clickDownloadButton();
        if (!downloadClicked) {
            logger.error("Failed to click download button");
            return new java.util.ArrayList<>();
        }

        // Step 2: Wait for PDF to download
        logger.info("Waiting for PDF download in: {}", PDF_DOWNLOAD_DIR);
        String pdfPath = PDFReader.waitForPDFDownload(PDF_DOWNLOAD_DIR, 60);
        if (pdfPath == null) {
            logger.error("PDF download timeout - no PDF file found");
            return new java.util.ArrayList<>();
        }
        logger.info("PDF downloaded: {}", pdfPath);

        // Step 3: Read and parse PDF
        String pdfContent = PDFReader.readPDF(pdfPath);
        if (pdfContent.isEmpty()) {
            logger.error("Failed to read PDF content");
            return new java.util.ArrayList<>();
        }

        // Step 4: Extract MASTER POLICY STATEMENT section
        String masterPolicySection = PDFReader.extractMasterPolicySection(pdfContent);
        if (masterPolicySection.isEmpty()) {
            logger.warn("MASTER POLICY STATEMENT section not found in PDF");
        }

        // Step 5: Parse certificate data from PDF
        Map<String, PDFReader.PDFCertificateData> pdfData = PDFReader.parseCertificateData(masterPolicySection);
        logger.info("Parsed {} certificates from PDF", pdfData.size());

        // Step 6: Aggregate UI data by certificate
        Map<String, CertificateAggregateData> uiData = aggregateInvoicesByCertificate(allInvoices);
        logger.info("Aggregated {} unique certificates from UI", uiData.size());

        // Step 7: Compare UI with PDF
        List<CertificateComparisonResult> results = compareUIWithPDF(uiData, pdfData);

        logger.info("=== PDF Verification Complete ===");
        logger.info("Total comparisons: {}", results.size());
        long matchCount = results.stream().filter(r -> r.isMatch).count();
        logger.info("Matches: {}, Mismatches: {}", matchCount, results.size() - matchCount);

        return results;
    }

    /**
     * Generate HTML report for PDF comparison results
     * @param results List of comparison results
     * @return HTML string with comparison table
     */
    public String generatePDFComparisonReport(List<CertificateComparisonResult> results) {
        StringBuilder html = new StringBuilder();

        long matchCount = results.stream().filter(r -> r.isMatch).count();
        long mismatchCount = results.size() - matchCount;

        html.append("<h3 style='color: black;'>PDF vs UI Verification Results</h3>");
        html.append("<p style='color: black;'><strong>Total Certificates: ").append(results.size()).append("</strong></p>");
        html.append("<p style='color: green;'>&#10004; Matches: ").append(matchCount).append("</p>");
        if (mismatchCount > 0) {
            html.append("<p style='color: red;'>&#10008; Mismatches: ").append(mismatchCount).append("</p>");
        }

        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        html.append("<thead><tr style='background-color: #4472C4; color: white;'>");
        html.append("<th>Certificate</th><th>UI Location</th><th>PDF Property Address</th>");
        html.append("<th>UI Total</th><th>PDF Total</th><th>Status</th><th>Details</th>");
        html.append("</tr></thead><tbody>");

        for (CertificateComparisonResult result : results) {
            html.append(result.toHtmlTableRow());
        }

        html.append("</tbody></table>");

        // Summary
        if (mismatchCount == 0) {
            html.append("<p style='color: green; font-weight: bold;'>&#10004; All certificates verified successfully!</p>");
        } else {
            html.append("<p style='color: red; font-weight: bold;'>&#9888; ").append(mismatchCount)
                    .append(" certificate(s) have mismatches - review required!</p>");
        }

        return html.toString();
    }

    /**
     * Get PDF download directory path
     */
    public String getPDFDownloadDirectory() {
        return PDF_DOWNLOAD_DIR;
    }
}
