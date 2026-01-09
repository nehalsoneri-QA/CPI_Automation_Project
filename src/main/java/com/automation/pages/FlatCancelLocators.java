package com.automation.pages;

import com.automation.base.BasePage;
import com.automation.utils.ExcelReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

/**
 * Flat Cancel Page Locators and Basic Methods
 * Contains all element locators and low-level interaction methods for the Flat Cancel functionality
 * This class should NOT be used directly in tests - use FlatCancelPage.java instead
 */
public class FlatCancelLocators extends BasePage {

    // ==================== Excel Data Provider ====================

    protected final ExcelReader excelReader;
    protected static final String TEST_DATA_PATH = "src/test/resources/testdata/TestData.xlsx";
    protected static final String FLAT_CANCEL_SHEET = "CreateFlatCancel";

    // ==================== URL Constants ====================

    protected static final String MASTER_POLICY_URL = "https://cpiai-dev.attri.ai/policies/";
    protected static final String CANCEL_ADDRESS_URL = "https://cpiai-dev.attri.ai/cancel-address";
    protected static final String EDIT_CANCEL_ADDRESS_URL = "https://cpiai-dev.attri.ai/edit-cancel-address";

    // ==================== Create Endorsement Button ====================

    @FindBy(xpath = "//button[contains(text(),'Create Endorsement')]")
    protected WebElement createEndorsementButton;

    protected final By createEndorsementButtonLocator = By.xpath("//button[contains(text(),'Create Endorsement')]");

    // ==================== Manage Policy Page Locators ====================

    // Search input on Policy List page
    @FindBy(id = "policy-list-search-input")
    protected WebElement policySearchInput;

    protected final By policySearchInputLocator = By.id("policy-list-search-input");

    // View Policy button - ID pattern: policy-list-action-view-policy-{id}
    // Using xpath to find any View Policy button
    protected final By viewPolicyButtonLocator = By.xpath("//*[starts-with(@id, 'policy-list-action-view-policy-')]");

    // ==================== Master Policy Details Page Locators ====================

    // Flat Cancel button on Master Policy Details page
    @FindBy(id = "policy-details-flat-cancel-button")
    protected WebElement flatCancelButton;

    protected final By flatCancelButtonLocator = By.id("policy-details-flat-cancel-button");

    // ==================== Cancel Address Page Locators ====================

    // Locations Details Table
    @FindBy(xpath = "//table")
    protected WebElement locationsDetailsTable;

    protected final By locationsDetailsTableLocator = By.xpath("//table");

    // Table rows in Locations Details
    protected final By tableRowsLocator = By.xpath("//table//tbody//tr");

    // Cert No. column - typically in a table cell
    // Pattern to find checkbox by certificate number
    // The checkbox is on the left side of each certificate row
    protected final By certificateCheckboxLocator = By.xpath("//table//tbody//tr//td//input[@type='checkbox']");

    // ==================== Common Locators ====================

    protected final By loaderLocator = By.xpath("//div[contains(@class,'bg-opacity-75')]");

    // ==================== Constructor ====================

    public FlatCancelLocators(WebDriver driver) {
        super(driver);
        this.excelReader = new ExcelReader(TEST_DATA_PATH);
        logger.info("FlatCancelLocators initialized");
    }

    // ==================== Excel Data Methods ====================

    /**
     * Get Policy ID from FlatCancel sheet (column B, row 0)
     * @return Policy ID string
     */
    public String getPolicyIdFromExcel() {
        String policyId = excelReader.getCellData(FLAT_CANCEL_SHEET, 0, 1); // B1 = row 0, col 1
        logger.info("Policy ID from Excel (B1): {}", policyId);
        return policyId;
    }

    /**
     * Get Policy ID from specific row
     * @param rowIndex Row index (0-based)
     * @return Policy ID string
     */
    public String getPolicyIdFromExcel(int rowIndex) {
        String policyId = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 1);
        logger.info("Policy ID from Excel (row {}, col B): {}", rowIndex, policyId);
        return policyId;
    }

    /**
     * Get ALL Policy IDs from column B in FlatCancel sheet
     * @return List of all policy IDs
     */
    public List<String> getAllPolicyIdsFromExcel() {
        List<String> policyIds = new ArrayList<>();
        int rowIndex = 0;

        while (true) {
            String policyId = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 1);
            if (policyId == null || policyId.trim().isEmpty()) {
                break;
            }
            // Only add unique policy IDs
            if (!policyIds.contains(policyId.trim())) {
                policyIds.add(policyId.trim());
            }
            rowIndex++;
        }

        logger.info("Found {} unique Policy IDs in column B of FlatCancel sheet", policyIds.size());
        return policyIds;
    }

    /**
     * Get Certificate ID from FlatCancel sheet (column C)
     * @param rowIndex Row index (0-based)
     * @return Certificate ID string
     */
    public String getCertificateIdFromExcel(int rowIndex) {
        String certId = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 2); // Column C = index 2
        logger.info("Certificate ID from Excel (row {}, col C): {}", rowIndex, certId);
        return certId;
    }

    /**
     * Get ALL Certificate IDs for a specific Policy from column C
     * Reads all rows that have the same Policy ID in column B
     * Supports both formats:
     * 1. Multiple rows with same policy and different certificates
     * 2. Comma-separated certificates in one cell
     * @param policyId The policy ID to match
     * @return List of certificate IDs for that policy
     */
    public List<String> getCertificateIdsForPolicy(String policyId) {
        List<String> certIds = new ArrayList<>();
        int rowIndex = 0;

        while (true) {
            String currentPolicyId = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 1);
            if (currentPolicyId == null || currentPolicyId.trim().isEmpty()) {
                break;
            }

            if (currentPolicyId.trim().equals(policyId)) {
                String certIdCell = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 2);
                if (certIdCell != null && !certIdCell.trim().isEmpty()) {
                    // Handle comma-separated certificates in one cell
                    if (certIdCell.contains(",")) {
                        String[] certs = certIdCell.split(",");
                        for (String cert : certs) {
                            String trimmedCert = cert.trim();
                            if (!trimmedCert.isEmpty() && !certIds.contains(trimmedCert)) {
                                certIds.add(trimmedCert);
                            }
                        }
                    } else {
                        String trimmedCert = certIdCell.trim();
                        if (!certIds.contains(trimmedCert)) {
                            certIds.add(trimmedCert);
                        }
                    }
                }
            }
            rowIndex++;
        }

        logger.info("Found {} Certificate IDs for Policy {} in FlatCancel sheet", certIds.size(), policyId);
        for (String cert : certIds) {
            logger.info("  Certificate: {}", cert);
        }
        return certIds;
    }

    /**
     * Get all unique Policy IDs with their Certificate IDs
     * Supports both formats:
     * 1. Multiple rows with same policy and different certificates
     * 2. Comma-separated certificates in one cell
     * @return Map of Policy ID to List of Certificate IDs
     */
    public java.util.Map<String, List<String>> getAllPoliciesWithCertificates() {
        java.util.Map<String, List<String>> policyToCerts = new java.util.LinkedHashMap<>();
        int rowIndex = 0;

        while (true) {
            String policyId = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 1);
            if (policyId == null || policyId.trim().isEmpty()) {
                break;
            }

            policyId = policyId.trim();
            String certIdCell = excelReader.getCellData(FLAT_CANCEL_SHEET, rowIndex, 2);

            if (!policyToCerts.containsKey(policyId)) {
                policyToCerts.put(policyId, new ArrayList<>());
            }

            if (certIdCell != null && !certIdCell.trim().isEmpty()) {
                // Handle comma-separated certificates in one cell
                if (certIdCell.contains(",")) {
                    String[] certs = certIdCell.split(",");
                    for (String cert : certs) {
                        String trimmedCert = cert.trim();
                        if (!trimmedCert.isEmpty() && !policyToCerts.get(policyId).contains(trimmedCert)) {
                            policyToCerts.get(policyId).add(trimmedCert);
                        }
                    }
                } else {
                    String trimmedCert = certIdCell.trim();
                    if (!policyToCerts.get(policyId).contains(trimmedCert)) {
                        policyToCerts.get(policyId).add(trimmedCert);
                    }
                }
            }

            rowIndex++;
        }

        logger.info("Found {} unique policies with certificates in FlatCancel sheet", policyToCerts.size());
        for (String policy : policyToCerts.keySet()) {
            logger.info("  Policy: {} -> {} certificates: {}", policy, policyToCerts.get(policy).size(), policyToCerts.get(policy));
        }
        return policyToCerts;
    }

    // ==================== Basic Input Methods ====================

    /**
     * Enter text in policy search input field
     */
    public void inputPolicySearchText(String searchText) {
        logger.debug("Entering policy search text: {}", searchText);
        waitForVisibility(policySearchInput);
        clearAndType(policySearchInput, searchText);
    }

    /**
     * Clear policy search input field
     */
    public void clearPolicySearchInput() {
        logger.debug("Clearing policy search input");
        waitForVisibility(policySearchInput);
        policySearchInput.clear();
    }

    // ==================== Basic Click Methods ====================

    /**
     * Click Flat Cancel button
     */
    public void clickFlatCancelButton() {
        logger.debug("Clicking Flat Cancel button");
        waitForClickable(flatCancelButton);
        click(flatCancelButton);
    }

    // ==================== Basic Validation Methods ====================

    /**
     * Check if policy search input is displayed
     */
    public boolean isPolicySearchInputDisplayed() {
        return isDisplayed(policySearchInputLocator);
    }

    /**
     * Check if Flat Cancel button is displayed
     */
    public boolean isFlatCancelButtonDisplayed() {
        return isDisplayed(flatCancelButtonLocator);
    }

    /**
     * Check if Locations Details table is displayed
     */
    public boolean isLocationsDetailsTableDisplayed() {
        return isDisplayed(locationsDetailsTableLocator);
    }

    // ==================== Basic Wait Methods ====================

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
     * Wait for policy search input to be visible
     */
    public void waitForPolicySearchInput() {
        waitForVisibility(policySearchInputLocator);
    }

    /**
     * Wait for Flat Cancel button to be visible
     */
    public void waitForFlatCancelButton() {
        waitForVisibility(flatCancelButtonLocator);
    }

    /**
     * Wait for Locations Details table to be visible
     */
    public void waitForLocationsDetailsTable() {
        waitForVisibility(locationsDetailsTableLocator);
    }

    /**
     * Wait for page to load
     */
    public void waitForPageLoad() {
        logger.debug("Waiting for page to load");
        waitForLoaderToDisappear();
    }
}
