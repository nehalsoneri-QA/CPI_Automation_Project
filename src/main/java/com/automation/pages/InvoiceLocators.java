package com.automation.pages;

import com.automation.base.BasePage;
import com.automation.utils.ExcelReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Invoice Page Locators and Basic Methods
 * Contains all element locators and low-level interaction methods for the Invoice page
 * This class should NOT be used directly in tests - use InvoicePage.java instead
 */
public class InvoiceLocators extends BasePage {

    // ==================== Excel Data Provider ====================

    protected final ExcelReader excelReader;
    protected static final String TEST_DATA_PATH = "src/test/resources/testdata/TestData.xlsx";
    protected static final String INVOICE_SHEET = "Invoice";

    // ==================== Page Header Locators ====================

    @FindBy(xpath = "//h1[text()='Invoices']")
    protected WebElement pageTitle;

    // ==================== Search Section Locators ====================

    @FindBy(id = "invoice-list-search-input")
    protected WebElement searchInput;

    @FindBy(xpath = "//input[@id='invoice-list-search-input']/preceding-sibling::*[name()='svg']")
    protected WebElement searchIcon;

    // ==================== Filter Section Locators ====================

    @FindBy(id = "invoice-list-filter-carrier")
    protected WebElement carrierFilterDropdown;

    @FindBy(xpath = "//*[@id='root']/div[2]/div[1]/div/div[1]/div[1]/div/div/input")
    protected WebElement fromDateInput;

    @FindBy(xpath = "//*[@id='root']/div[2]/div[1]/div/div[1]/div[2]/div/div/input")
    protected WebElement toDateInput;

    // ==================== Table Locators ====================

    @FindBy(xpath = "//table")
    protected WebElement invoiceTable;

    @FindBy(xpath = "//table//thead//tr")
    protected WebElement tableHeader;

    @FindBy(xpath = "//table//tbody//tr")
    protected WebElement tableRows;

    // ==================== Pagination Locators ====================

    @FindBy(id = "invoice-list-page-size-selector")
    protected WebElement pageSizeSelector;

    @FindBy(id = "invoice-list-pagination-previous")
    protected WebElement previousPageButton;

    @FindBy(id = "invoice-list-pagination-next")
    protected WebElement nextPageButton;

    // ==================== Sort Button Locators ====================

    @FindBy(id = "invoice-list-sort-policy-no")
    protected WebElement sortByPolicyNo;

    @FindBy(id = "invoice-list-sort-insured-name")
    protected WebElement sortByInsuredName;

    @FindBy(id = "invoice-list-sort-effective-date")
    protected WebElement sortByEffectiveDate;

    @FindBy(id = "invoice-list-sort-expiration-date")
    protected WebElement sortByExpirationDate;

    @FindBy(id = "invoice-list-sort-due-amount")
    protected WebElement sortByDueAmount;

    @FindBy(id = "invoice-list-sort-credits")
    protected WebElement sortByCredits;

    // ==================== By Locators (for explicit waits) ====================

    protected final By pageTitleLocator = By.xpath("//h1[text()='Invoices']");
    protected final By searchInputLocator = By.id("invoice-list-search-input");
    protected final By invoiceTableLocator = By.xpath("//table");
    protected final By tableRowsLocator = By.xpath("//table//tbody//tr");
    protected final By loaderLocator = By.xpath("//div[contains(@class,'bg-opacity-75')]");
    protected final By policyButtonsLocator = By.xpath("//table//tr/td[1]//button");
    protected final By noResultsLocator = By.xpath("//*[contains(text(),'No results') or contains(text(),'No invoices')]");

    // ==================== Policy Expansion and Invoice Details Locators ====================

    // First policy row button (to click and expand)
    protected final By firstPolicyButtonLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[1]/td[1]");

    // Locators for expanded invoice section at tr[2] (after first policy row is clicked)
    // These use exact paths based on user-provided xpaths

    // Invoice count text element (shows "X Invoices") - in expanded section at tr[2]
    protected final By invoiceCountLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[3]/div[1]/p");

    // Due amount column header in nested invoice table
    protected final By dueAmountHeaderLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[2]/div/table/thead/tr/th[6]/button");

    // Invoice table rows (nested table inside expanded policy at tr[2])
    protected final By invoiceTableRowsLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[2]/div/table/tbody/tr");

    // Nested invoice table directly at tr[2]
    protected final By nestedInvoiceTableLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[2]/div/table");

    // Individual invoice row columns (relative to each row)
    // Column 1: Checkbox, Column 2: Invoice ID, Column 3: Certificate, Column 4: Location,
    // Column 5: Due Amount, Column 6: Invoice Amount, Column 7: Due Date, Column 8: Status

    // Invoice pagination (inside expanded policy) - exact xpath from user
    // This is the Next button inside the expanded invoice section at tr[2]
    protected final By invoiceNextButtonLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[3]/div[2]/button[2]");
    protected final By invoicePreviousButtonLocator = By.xpath("//*[@id='root']/div[2]/div[2]/div/table/tbody/tr[2]/td/div/div/div[3]/div[2]/button[1]");

    // ==================== Download Button Locators ====================

    // Download button locator (dynamically includes invoice ID, e.g., invoice-list-action-download-207)
    // The ID pattern is: invoice-list-action-download-{policyId}
    protected final By downloadButtonLocator = By.xpath("//*[starts-with(@id, 'invoice-list-action-download-')]");

    // PDF Download directory
    protected static final String PDF_DOWNLOAD_DIR = "C:\\Users\\HP\\Downloads\\";

    // ==================== Constructor ====================

    public InvoiceLocators(WebDriver driver) {
        super(driver);
        this.excelReader = new ExcelReader(TEST_DATA_PATH);
        logger.info("InvoiceLocators initialized");
    }

    // ==================== Excel Data Methods ====================

    /**
     * Get Policy ID from Invoice sheet cell B1 (row 0, column 1)
     * Note: Row 0 is typically the header row, so B1 means first row, second column
     * If B1 contains the header, use B2 (row 1, column 1) for actual data
     */
    public String getPolicyIdFromExcel() {
        // B1 in Excel = row index 0, column index 1
        String policyId = excelReader.getCellData(INVOICE_SHEET, 0, 1);
        logger.info("Policy ID from Excel (B1): {}", policyId);
        return policyId;
    }

    /**
     * Get Policy ID from specific row (if B1 is header, use row 1 for first data row)
     */
    public String getPolicyIdFromExcel(int rowIndex) {
        String policyId = excelReader.getCellData(INVOICE_SHEET, rowIndex, 1);
        logger.info("Policy ID from Excel (row {}, col B): {}", rowIndex, policyId);
        return policyId;
    }

    /**
     * Get ALL Policy IDs from column B in Invoice sheet
     * Reads all rows starting from row 0 (B1) until empty cell is found
     * @return List of all policy IDs from column B
     */
    public java.util.List<String> getAllPolicyIdsFromExcel() {
        java.util.List<String> policyIds = new java.util.ArrayList<>();
        int rowIndex = 0;

        while (true) {
            String policyId = excelReader.getCellData(INVOICE_SHEET, rowIndex, 1);
            if (policyId == null || policyId.trim().isEmpty()) {
                break;
            }
            policyIds.add(policyId.trim());
            rowIndex++;
        }

        logger.info("Found {} Policy IDs in column B of Invoice sheet", policyIds.size());
        for (int i = 0; i < policyIds.size(); i++) {
            logger.info("  Policy {}: {}", i + 1, policyIds.get(i));
        }
        return policyIds;
    }

    // ==================== Basic Input Methods ====================

    /**
     * Enter text in search input field
     */
    public void inputSearchText(String searchText) {
        logger.debug("Entering search text: {}", searchText);
        waitForVisibility(searchInput);
        clearAndType(searchInput, searchText);
    }

    /**
     * Clear search input field
     */
    public void clearSearchInput() {
        logger.debug("Clearing search input");
        waitForVisibility(searchInput);
        searchInput.clear();
    }

    // ==================== Basic Click Methods ====================

    /**
     * Click search icon
     */
    public void clickSearchIcon() {
        logger.debug("Clicking search icon");
        click(searchIcon);
    }

    /**
     * Click page size selector
     */
    public void clickPageSizeSelector() {
        logger.debug("Clicking page size selector");
        waitForClickable(pageSizeSelector);
        click(pageSizeSelector);
    }

    /**
     * Click previous page button
     */
    public void clickPreviousPage() {
        logger.debug("Clicking previous page button");
        waitForClickable(previousPageButton);
        click(previousPageButton);
    }

    /**
     * Click next page button
     */
    public void clickNextPage() {
        logger.debug("Clicking next page button");
        waitForClickable(nextPageButton);
        click(nextPageButton);
    }

    /**
     * Click sort by policy number
     */
    public void clickSortByPolicyNo() {
        logger.debug("Clicking sort by policy number");
        waitForClickable(sortByPolicyNo);
        click(sortByPolicyNo);
    }

    /**
     * Click sort by insured name
     */
    public void clickSortByInsuredName() {
        logger.debug("Clicking sort by insured name");
        waitForClickable(sortByInsuredName);
        click(sortByInsuredName);
    }

    // ==================== Basic Validation Methods ====================

    /**
     * Check if page title is displayed
     */
    public boolean isPageTitleDisplayed() {
        return isDisplayed(pageTitleLocator);
    }

    /**
     * Check if search input is displayed
     */
    public boolean isSearchInputDisplayed() {
        return isDisplayed(searchInputLocator);
    }

    /**
     * Check if invoice table is displayed
     */
    public boolean isInvoiceTableDisplayed() {
        return isDisplayed(invoiceTableLocator);
    }

    /**
     * Check if next page button is enabled
     */
    public boolean isNextPageEnabled() {
        return nextPageButton.isEnabled();
    }

    /**
     * Check if previous page button is enabled
     */
    public boolean isPreviousPageEnabled() {
        return previousPageButton.isEnabled();
    }

    /**
     * Check if no results message is displayed
     */
    public boolean isNoResultsDisplayed() {
        try {
            return isDisplayed(noResultsLocator);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Basic Get Methods ====================

    /**
     * Get page title text
     */
    public String getPageTitleText() {
        waitForVisibility(pageTitle);
        return getText(pageTitle);
    }

    /**
     * Get search input value
     */
    public String getSearchInputValue() {
        return searchInput.getAttribute("value");
    }

    /**
     * Get current URL
     */
    public String getCurrentPageUrl() {
        return getCurrentUrl();
    }

    // ==================== Basic Wait Methods ====================

    /**
     * Wait for search input to be visible
     */
    public void waitForSearchInput() {
        waitForVisibility(searchInputLocator);
    }

    /**
     * Wait for invoice table to be visible
     */
    public void waitForInvoiceTable() {
        waitForVisibility(invoiceTableLocator);
    }

    /**
     * Wait for table rows to be present
     */
    public void waitForTableRows() {
        waitForPresence(tableRowsLocator);
    }

    /**
     * Wait for loader to disappear
     */
    public void waitForLoaderToDisappear() {
        try {
            waitForInvisibility(loaderLocator);
        } catch (Exception e) {
            logger.debug("Loader wait skipped or already invisible");
        }
    }

    /**
     * Wait for page to fully load
     */
    public void waitForPageLoad() {
        logger.debug("Waiting for Invoice page to load");
        waitForSearchInput();
        waitForInvoiceTable();
        waitForLoaderToDisappear();
    }
}
