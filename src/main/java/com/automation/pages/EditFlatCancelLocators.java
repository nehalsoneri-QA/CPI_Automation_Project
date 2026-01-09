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
 * Edit Flat Cancel Page Locators and Basic Methods
 * Contains all element locators and low-level interaction methods for the Edit Flat Cancel functionality
 * This class should NOT be used directly in tests - use EditFlatCancelPage.java instead
 */
public class EditFlatCancelLocators extends BasePage {

    // ==================== Excel Data Provider ====================

    protected final ExcelReader excelReader;
    protected static final String TEST_DATA_PATH = "src/test/resources/testdata/TestData.xlsx";
    protected static final String EDIT_FLAT_CANCEL_SHEET = "EditFlatCancel";

    // ==================== URL Constants ====================

    protected static final String EDIT_CANCEL_ADDRESS_URL = "https://cpiai-dev.attri.ai/edit-cancel-address";

    // ==================== Page Header Locators ====================

    @FindBy(xpath = "//h1[contains(text(),'Edit Cancel Address')]")
    protected WebElement pageHeader;

    protected final By pageHeaderLocator = By.xpath("//h1[contains(text(),'Edit Cancel Address')]");

    // ==================== Location Table Locators ====================

    @FindBy(xpath = "//table")
    protected WebElement locationsTable;

    protected final By locationsTableLocator = By.xpath("//table");

    // Table rows
    protected final By tableRowsLocator = By.xpath("//table//tbody//tr");

    // Table header
    protected final By tableHeaderLocator = By.xpath("//table//thead//tr//th");

    // ==================== Form Field Locators ====================

    // Effective Date field
    @FindBy(id = "effective-date")
    protected WebElement effectiveDateInput;

    protected final By effectiveDateInputLocator = By.id("effective-date");

    // Cancel Date field
    @FindBy(id = "cancel-date")
    protected WebElement cancelDateInput;

    protected final By cancelDateInputLocator = By.id("cancel-date");

    // Cancellation Reason dropdown
    @FindBy(id = "cancellation-reason")
    protected WebElement cancellationReasonSelect;

    protected final By cancellationReasonSelectLocator = By.id("cancellation-reason");

    // Notes/Comments textarea
    @FindBy(id = "notes")
    protected WebElement notesTextarea;

    protected final By notesTextareaLocator = By.id("notes");

    // ==================== Button Locators ====================

    // Save/Submit button
    @FindBy(xpath = "//button[contains(text(),'Save') or contains(text(),'Submit')]")
    protected WebElement saveButton;

    protected final By saveButtonLocator = By.xpath("//button[contains(text(),'Save') or contains(text(),'Submit')]");

    // Cancel button
    @FindBy(xpath = "//button[contains(text(),'Cancel') and not(contains(text(),'Cancellation'))]")
    protected WebElement cancelButton;

    protected final By cancelButtonLocator = By.xpath("//button[contains(text(),'Cancel') and not(contains(text(),'Cancellation'))]");

    // Back button
    @FindBy(xpath = "//button[contains(text(),'Back')]")
    protected WebElement backButton;

    protected final By backButtonLocator = By.xpath("//button[contains(text(),'Back')]");

    // Confirm button (for confirmation dialogs)
    @FindBy(xpath = "//button[contains(text(),'Confirm')]")
    protected WebElement confirmButton;

    protected final By confirmButtonLocator = By.xpath("//button[contains(text(),'Confirm')]");

    // Update Endorsement (Flat Cancel) button - must be clicked before Bind
    @FindBy(xpath = "//button[contains(text(),'Update Endorsement') and contains(text(),'Flat Cancel')]")
    protected WebElement updateEndorsementFlatCancelButton;

    protected final By updateEndorsementFlatCancelButtonLocator = By.xpath("//button[contains(text(),'Update Endorsement') and contains(text(),'Flat Cancel')]");

    // Alternative Update Endorsement button locators
    protected final By updateEndorsementButtonLocator = By.xpath("//button[contains(text(),'Update Endorsement')]");

    // Bind (Flat Cancel) button - clicked after Update Endorsement
    @FindBy(xpath = "//button[contains(text(),'Bind') and contains(text(),'Flat Cancel')]")
    protected WebElement bindFlatCancelButton;

    protected final By bindFlatCancelButtonLocator = By.xpath("//button[contains(text(),'Bind') and contains(text(),'Flat Cancel')]");

    // Alternative Bind button locator
    protected final By bindButtonLocator = By.xpath("//button[contains(text(),'Bind')]");

    // ==================== Confirm Location Cancellation Dialogue Locators ====================

    // Dialogue/Modal container
    protected final By confirmDialogueLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or contains(@role,'dialog')]");

    // Dialogue title - "Confirm Location Cancellation"
    protected final By confirmDialogueTitleLocator = By.xpath("//*[contains(text(),'Confirm Location Cancellation') or contains(text(),'Confirm Cancellation')]");

    // Certificate rows in dialogue
    protected final By dialogueCertificateRowsLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or contains(@role,'dialog')]//table//tbody//tr");

    // Dialogue certificate count text
    protected final By dialogueCertificateCountLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or contains(@role,'dialog')]//*[contains(text(),'certificate') or contains(text(),'location')]");

    // Dialogue Confirm button
    protected final By dialogueConfirmButtonLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or contains(@role,'dialog')]//button[contains(text(),'Confirm')]");

    // Dialogue Cancel button
    protected final By dialogueCancelButtonLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or contains(@role,'dialog')]//button[contains(text(),'Cancel')]");

    // ==================== Certificate Checkbox Locators ====================

    // Table rows in locations table
    protected final By certificateRowsLocator = By.xpath("//table//tbody//tr");

    // Checkbox in table
    protected final By certificateCheckboxLocator = By.xpath("//table//tbody//tr//td//input[@type='checkbox']");

    // ==================== Alert/Message Locators ====================

    // Success message
    protected final By successMessageLocator = By.xpath("//*[contains(@class,'success') or contains(@class,'alert-success')]");

    // Error message
    protected final By errorMessageLocator = By.xpath("//*[contains(@class,'error') or contains(@class,'alert-danger')]");

    // Toast notification
    protected final By toastNotificationLocator = By.xpath("//*[contains(@class,'toast') or contains(@class,'notification')]");

    // ==================== Common Locators ====================

    protected final By loaderLocator = By.xpath("//div[contains(@class,'bg-opacity-75')]");

    // ==================== Constructor ====================

    public EditFlatCancelLocators(WebDriver driver) {
        super(driver);
        this.excelReader = new ExcelReader(TEST_DATA_PATH);
        logger.info("EditFlatCancelLocators initialized");
    }

    // ==================== Excel Data Methods ====================

    /**
     * Get Policy ID from EditFlatCancel sheet (column B)
     * @param rowIndex Row index (0-based)
     * @return Policy ID string
     */
    public String getPolicyIdFromExcel(int rowIndex) {
        String policyId = excelReader.getCellData(EDIT_FLAT_CANCEL_SHEET, rowIndex, 1);
        logger.info("Policy ID from EditFlatCancel Excel (row {}, col B): {}", rowIndex, policyId);
        return policyId;
    }

    /**
     * Get Certificate IDs for a specific Policy from column C in EditFlatCancel sheet
     * @param policyId The policy ID to match
     * @return List of certificate IDs
     */
    public List<String> getCertificateIdsForPolicy(String policyId) {
        List<String> certIds = new ArrayList<>();
        int rowIndex = 0;

        while (true) {
            String currentPolicyId = excelReader.getCellData(EDIT_FLAT_CANCEL_SHEET, rowIndex, 1);
            if (currentPolicyId == null || currentPolicyId.trim().isEmpty()) {
                break;
            }

            if (currentPolicyId.trim().equals(policyId)) {
                String certIdCell = excelReader.getCellData(EDIT_FLAT_CANCEL_SHEET, rowIndex, 2);
                if (certIdCell != null && !certIdCell.trim().isEmpty()) {
                    // Handle comma-separated certificates
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

        logger.info("Found {} Certificate IDs for Policy {}", certIds.size(), policyId);
        return certIds;
    }

    // ==================== Basic Input Methods ====================

    /**
     * Enter effective date
     */
    public void inputEffectiveDate(String date) {
        logger.debug("Entering effective date: {}", date);
        waitForVisibility(effectiveDateInput);
        clearAndType(effectiveDateInput, date);
    }

    /**
     * Enter cancel date
     */
    public void inputCancelDate(String date) {
        logger.debug("Entering cancel date: {}", date);
        waitForVisibility(cancelDateInput);
        clearAndType(cancelDateInput, date);
    }

    /**
     * Enter notes
     */
    public void inputNotes(String notes) {
        logger.debug("Entering notes: {}", notes);
        waitForVisibility(notesTextarea);
        clearAndType(notesTextarea, notes);
    }

    // ==================== Basic Click Methods ====================

    /**
     * Click Save button
     */
    public void clickSaveButton() {
        logger.debug("Clicking Save button");
        waitForClickable(saveButton);
        click(saveButton);
    }

    /**
     * Click Cancel button
     */
    public void clickCancelButton() {
        logger.debug("Clicking Cancel button");
        waitForClickable(cancelButton);
        click(cancelButton);
    }

    /**
     * Click Back button
     */
    public void clickBackButton() {
        logger.debug("Clicking Back button");
        waitForClickable(backButton);
        click(backButton);
    }

    /**
     * Click Confirm button
     */
    public void clickConfirmButton() {
        logger.debug("Clicking Confirm button");
        waitForClickable(confirmButton);
        click(confirmButton);
    }

    // ==================== Basic Validation Methods ====================

    /**
     * Check if page header is displayed
     */
    public boolean isPageHeaderDisplayed() {
        return isDisplayed(pageHeaderLocator);
    }

    /**
     * Check if locations table is displayed
     */
    public boolean isLocationsTableDisplayed() {
        return isDisplayed(locationsTableLocator);
    }

    /**
     * Check if Save button is displayed
     */
    public boolean isSaveButtonDisplayed() {
        return isDisplayed(saveButtonLocator);
    }

    /**
     * Check if Cancel button is displayed
     */
    public boolean isCancelButtonDisplayed() {
        return isDisplayed(cancelButtonLocator);
    }

    /**
     * Check if success message is displayed
     */
    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMessageLocator);
    }

    /**
     * Check if error message is displayed
     */
    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessageLocator);
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
     * Wait for page header to be visible
     */
    public void waitForPageHeader() {
        waitForVisibility(pageHeaderLocator);
    }

    /**
     * Wait for locations table to be visible
     */
    public void waitForLocationsTable() {
        waitForVisibility(locationsTableLocator);
    }

    /**
     * Wait for page to load
     */
    public void waitForPageLoad() {
        logger.debug("Waiting for page to load");
        waitForLoaderToDisappear();
    }
}
