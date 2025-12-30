package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Edit Quote Page Locators
 * Contains web element locators specific to the Edit Quote page
 * URL: /edit_quote/{quoteId}
 *
 * Note: Most locators are similar to CreateQuoteLocators but with edit-quote specific IDs
 * Reuses parent class methods where possible
 */
public class EditQuoteLocators extends BasePage {

	// ==================== Page Header Elements ====================

	@FindBy(xpath = "//h1[contains(text(),'Edit Quote')]")
	protected WebElement editPageTitle;

	@FindBy(id = "edit-quote-reset-form-button")
	protected WebElement editResetFormButton;

	// ==================== Agent Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Select Agent')]/following::button[1]")
	protected WebElement editAgentDropdown;

	@FindBy(xpath = "//label[contains(text(),'Commission')]/following-sibling::input")
	protected WebElement editAgentCommissionInput;

	// ==================== Insured Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Insured')]/following::button[1]")
	protected WebElement editInsuredDropdown;

	// ==================== Carrier Section Locators ====================

	@FindBy(id = "edit-quote-carrier-select")
	protected WebElement editCarrierDropdown;

	// Fallback - use same pattern as create quote if edit-specific ID doesn't exist
	protected final By editCarrierDropdownLocator = By.cssSelector("#edit-quote-carrier-select, #new-quote-carrier-select");

	// ==================== General Liability Section Locators ====================

	@FindBy(id = "edit-quote-general-liability-yes-radio")
	protected WebElement editGeneralLiabilityYesRadio;

	@FindBy(id = "edit-quote-general-liability-no-radio")
	protected WebElement editGeneralLiabilityNoRadio;

	@FindBy(id = "edit-quote-general-liability-amount-input")
	protected WebElement editGeneralLiabilityAmountInput;

	// Fallback locators using new-quote IDs
	protected final By glAmountInputLocator = By.cssSelector("#edit-quote-general-liability-amount-input, #new-quote-general-liability-amount-input");

	// ==================== Water & Sewer Backup Section Locators ====================

	@FindBy(id = "edit-quote-water-sewer-backup-yes-radio")
	protected WebElement editWaterSewerBackupYesRadio;

	@FindBy(id = "edit-quote-water-sewer-backup-no-radio")
	protected WebElement editWaterSewerBackupNoRadio;

	@FindBy(id = "edit-quote-water-sewer-backup-amount-input")
	protected WebElement editWaterSewerBackupAmountInput;

	// Fallback locators using new-quote IDs
	protected final By wsAmountInputLocator = By.cssSelector("#edit-quote-water-sewer-backup-amount-input, #new-quote-water-sewer-backup-amount-input");

	// ==================== Suggested State Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Suggested States')]/following::button[1]")
	protected WebElement editSuggestedStateDropdown;

	// ==================== Action Buttons Section Locators ====================

	@FindBy(id = "edit-quote-submit-button")
	protected WebElement editSubmitQuoteButton;

	// Fallback - use new-quote ID if edit-specific doesn't exist
	protected final By submitButtonLocator = By.cssSelector("#edit-quote-submit-button, #new-quote-submit-button");

	// ==================== Location Table Locators ====================

	@FindBy(xpath = "//table[contains(@class,'location') or .//th[contains(text(),'Address')]]")
	protected WebElement editLocationTable;

	// ==================== Display Computation Dialog Locators ====================

	@FindBy(xpath = "//button[contains(text(),'Display Computation')]")
	protected WebElement editDisplayComputationButton;

	@FindBy(xpath = "//div[@role='dialog']//table")
	protected WebElement editDisplayComputationTable;

	// ==================== Download Button Locators ====================

	@FindBy(xpath = "//button[contains(text(),'Download') or contains(@id,'download')]")
	protected WebElement editDownloadButton;

	// ==================== Premium Display Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Grand Total')]/following-sibling::*[1] | //span[contains(text(),'Grand Total')]/following-sibling::*[1] | //*[contains(text(),'Grand Total')]/following::*[contains(text(),'$')][1]")
	protected WebElement editGrandTotalValue;

	@FindBy(xpath = "//*[contains(text(),'Property Premium') or contains(text(),'Prop Premium')]/following::*[contains(text(),'$')][1]")
	protected WebElement editPropertyPremiumValue;

	@FindBy(xpath = "//*[contains(text(),'GL Premium') or contains(text(),'General Liability')]/following::*[contains(text(),'$')][1]")
	protected WebElement editGLPremiumValue;

	@FindBy(xpath = "//*[contains(text(),'WS Premium') or contains(text(),'Water')]/following::*[contains(text(),'$')][1]")
	protected WebElement editWSPremiumValue;

	@FindBy(xpath = "//*[contains(text(),'Tax') or contains(text(),'Taxes')]/following::*[contains(text(),'$')][1]")
	protected WebElement editTaxValue;

	// ==================== Date Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Effective')]/following::input[1]")
	protected WebElement editEffectiveDateInput;

	@FindBy(xpath = "//label[contains(text(),'Expiration')]/following::input[1]")
	protected WebElement editExpirationDateInput;

	// ==================== New Location Button ====================

	@FindBy(xpath = "//button[contains(text(),'New Location') or contains(text(),'Add Location')]")
	protected WebElement editNewLocationButton;

	// ==================== Constructor ====================

	public EditQuoteLocators(WebDriver driver) {
		super(driver);
		logger.info("EditQuoteLocators initialized");
	}

	// ==================== Helper Methods ====================

	/**
	 * Check if Edit Quote page is displayed
	 */
	public boolean isEditQuotePageDisplayed() {
		try {
			// Try edit-specific title first
			if (isDisplayed(editPageTitle)) {
				return true;
			}
			// Fallback - check URL
			String currentUrl = driver.getCurrentUrl();
			return currentUrl.contains("edit_quote") || currentUrl.contains("edit-quote");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Get the current carrier dropdown value
	 */
	protected String getCarrierDropdownValue() {
		try {
			// Try edit-specific first
			WebElement dropdown = findElementSafely(editCarrierDropdownLocator);
			if (dropdown != null && dropdown.isDisplayed()) {
				return dropdown.getText().trim();
			}
			// Try by ID
			WebElement carrierById = driver.findElement(By.id("edit-quote-carrier-select"));
			if (carrierById.isDisplayed()) {
				return carrierById.getText().trim();
			}
			// Fallback to new-quote ID
			carrierById = driver.findElement(By.id("new-quote-carrier-select"));
			return carrierById.getText().trim();
		} catch (Exception e) {
			logger.warn("Could not get carrier dropdown value: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Get Agent dropdown value
	 */
	protected String getAgentDropdownValue() {
		try {
			if (isDisplayed(editAgentDropdown)) {
				return editAgentDropdown.getText().trim();
			}
		} catch (Exception e) {
			logger.warn("Could not get agent dropdown value: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Get Insured dropdown value
	 */
	protected String getInsuredDropdownValue() {
		try {
			if (isDisplayed(editInsuredDropdown)) {
				return editInsuredDropdown.getText().trim();
			}
		} catch (Exception e) {
			logger.warn("Could not get insured dropdown value: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Get GL Amount value
	 */
	protected String getGLAmountValue() {
		try {
			WebElement input = findElementSafely(glAmountInputLocator);
			if (input != null) {
				return input.getAttribute("value");
			}
		} catch (Exception e) {
			logger.warn("Could not get GL amount value: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Get WS Amount value
	 */
	protected String getWSAmountValue() {
		try {
			WebElement input = findElementSafely(wsAmountInputLocator);
			if (input != null) {
				return input.getAttribute("value");
			}
		} catch (Exception e) {
			logger.warn("Could not get WS amount value: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Get location count from table
	 */
	protected int getLocationCount() {
		try {
			java.util.List<WebElement> rows = driver.findElements(
				By.xpath("//table[contains(@class,'location') or .//th[contains(text(),'Address')]]//tbody/tr"));
			return rows.size();
		} catch (Exception e) {
			logger.warn("Could not get location count: {}", e.getMessage());
			return 0;
		}
	}

	/**
	 * Find element safely with fallback
	 */
	private WebElement findElementSafely(By locator) {
		try {
			java.util.List<WebElement> elements = driver.findElements(locator);
			for (WebElement el : elements) {
				if (el.isDisplayed()) {
					return el;
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		return null;
	}
}
