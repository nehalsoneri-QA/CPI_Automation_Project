package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Locators for Create Premium Endorsement Page
 * Based on React source: CreateEndorsement.tsx
 */
public class CreatePremiumEndorsementLocators extends BasePage {

	// ==================== Page Header Elements ====================

	@FindBy(xpath = "//h1[contains(text(),'Create New Premium Endorsement')]")
	protected WebElement pageTitle;

	// ==================== Agent Section Locators ====================

	@FindBy(id = "create-endorsement-agent-select")
	protected WebElement agentDropdown;

	@FindBy(id = "create-endorsement-agent-commission-input")
	protected WebElement agentCommissionInput;

	@FindBy(id = "create-endorsement-agent-renewal-commission-input")
	protected WebElement agentRenewalCommissionInput;

	// ==================== Insured Section Locators ====================

	@FindBy(id = "create-endorsement-insured-select")
	protected WebElement insuredDropdown;

	// ==================== Aggregator Section Locators ====================

	@FindBy(id = "create-endorsement-aggregator-select")
	protected WebElement aggregatorDropdown;

	@FindBy(id = "create-endorsement-aggregator-commission-input")
	protected WebElement aggregatorCommissionInput;

	@FindBy(id = "create-endorsement-aggregator-renewal-commission-input")
	protected WebElement aggregatorRenewalCommissionInput;

	// ==================== Internal Agent Section Locators ====================

	@FindBy(id = "create-endorsement-internal-agent-select")
	protected WebElement internalAgentDropdown;

	@FindBy(id = "create-endorsement-internal-agent-commission-input")
	protected WebElement internalAgentCommissionInput;

	@FindBy(id = "create-endorsement-internal-agent-renewal-commission-input")
	protected WebElement internalAgentRenewalCommissionInput;

	// ==================== User Section Locators ====================

	@FindBy(id = "create-endorsement-user-select")
	protected WebElement userDropdown;

	// ==================== Date Section Locators ====================

	@FindBy(id = "create-endorsement-effective-date-picker")
	protected WebElement effectiveDatePicker;

	@FindBy(id = "create-endorsement-expiration-date-picker")
	protected WebElement expirationDatePicker;

	@FindBy(id = "create-endorsement-endorsement-effective-date-picker")
	protected WebElement endorsementEffectiveDatePicker;

	// ==================== Carrier Section Locators ====================

	@FindBy(id = "create-endorsement-carrier-select")
	protected WebElement carrierDropdown;

	// ==================== General Liability Section Locators ====================

	@FindBy(id = "create-endorsement-general-liability-yes-radio")
	protected WebElement generalLiabilityYesRadio;

	@FindBy(id = "create-endorsement-general-liability-no-radio")
	protected WebElement generalLiabilityNoRadio;

	@FindBy(id = "create-endorsement-general-liability-amount-input")
	protected WebElement generalLiabilityAmountInput;

	@FindBy(id = "create-endorsement-general-liability-apply-button")
	protected WebElement generalLiabilityApplyButton;

	// ==================== Water & Sewer Backup Section Locators ====================

	@FindBy(id = "create-endorsement-water-sewer-backup-yes-radio")
	protected WebElement waterSewerBackupYesRadio;

	@FindBy(id = "create-endorsement-water-sewer-backup-no-radio")
	protected WebElement waterSewerBackupNoRadio;

	@FindBy(id = "create-endorsement-water-sewer-backup-amount-input")
	protected WebElement waterSewerBackupAmountInput;

	@FindBy(id = "create-endorsement-water-sewer-backup-apply-button")
	protected WebElement waterSewerBackupApplyButton;

	// ==================== Animal Liability Section Locators ====================

	@FindBy(id = "create-endorsement-animal-liability-25000-radio")
	protected WebElement animalLiability25000Radio;

	@FindBy(id = "create-endorsement-animal-liability-no-radio")
	protected WebElement animalLiabilityNoRadio;

	@FindBy(id = "create-endorsement-animal-liability-no-radio-alt")
	protected WebElement animalLiabilityNoRadioAlt;

	// ==================== Policy Fee Section Locators ====================

	@FindBy(id = "create-endorsement-policy-fee-toggle")
	protected WebElement policyFeeToggle;

	// ==================== Suggested State Section Locators ====================

	@FindBy(id = "create-endorsement-suggested-state-select")
	protected WebElement suggestedStateDropdown;

	// ==================== Action Buttons Section Locators ====================

	@FindBy(id = "create-endorsement-submit-button")
	protected WebElement submitEndorsementButton;

	// ==================== Location Section Locators ====================

	@FindBy(xpath = "//button[contains(text(),'New Location')]")
	protected WebElement newLocationButton;

	@FindBy(xpath = "//button[contains(text(),'Multi Edit')]")
	protected WebElement multiEditButton;

	// ==================== Warning Modal Locators (EndorsementWarningModal) ====================

	@FindBy(xpath = "//div[contains(@class,'AlertDialogContent')]//button[contains(text(),'Continue')]")
	protected WebElement warningModalContinueButton;

	@FindBy(xpath = "//div[contains(@class,'AlertDialogContent')]//button[contains(text(),'Cancel')]")
	protected WebElement warningModalCancelButton;

	@FindBy(xpath = "//h2[contains(text(),'Caution: Premium Endorsement')]")
	protected WebElement warningModalTitle;

	// ==================== Endorsement Date Confirmation Dialog ====================

	@FindBy(xpath = "//div[contains(@class,'DialogContent')]//button[contains(text(),'Confirm')]")
	protected WebElement dateConfirmButton;

	@FindBy(xpath = "//div[contains(@class,'DialogContent')]//button[contains(text(),'Cancel')]")
	protected WebElement dateCancelButton;

	// ==================== Location Table Locators ====================

	@FindBy(xpath = "//table[contains(@class,'locations-table')]//tbody")
	protected WebElement locationTableBody;

	@FindBy(xpath = "//table[contains(@class,'locations-table')]//tbody//tr")
	protected java.util.List<WebElement> locationTableRows;

	// ==================== Total Calculation Section ====================

	@FindBy(xpath = "//div[contains(@class,'total-calculation')]//span[contains(text(),'Grand Total')]/../following-sibling::*")
	protected WebElement grandTotalValue;

	@FindBy(xpath = "//div[contains(@class,'total-calculation')]//span[contains(text(),'Due Amount')]/../following-sibling::*")
	protected WebElement dueAmountValue;

	public CreatePremiumEndorsementLocators(WebDriver driver) {
		super(driver);
	}
}
