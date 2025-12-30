package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Master Policy Details Page Locators
 * Contains web element locators specific to the Master Policy Details page
 * URL: /policies/{policyNumber}
 */
public class MasterPolicyLocators extends BasePage {

	// ==================== Page Header Elements ====================

	@FindBy(xpath = "//h1[contains(text(),'Master Policy Details') or contains(text(),'Policy Details')]")
	protected WebElement masterPolicyPageTitle;

	// ==================== Due Amount Section ====================

	@FindBy(xpath = "//*[contains(text(),'Due Amount')]/following::*[contains(text(),'$')][1]")
	protected WebElement dueAmountValue;

	@FindBy(xpath = "//*[contains(text(),'Due Amount')]/..//*[contains(text(),'$')]")
	protected WebElement dueAmountValueAlt;

	// ==================== Policy Information Section ====================

	@FindBy(xpath = "//*[contains(text(),'Insured Name') or contains(text(),'Named Insured')]/following::*[1]")
	protected WebElement insuredNameValue;

	@FindBy(xpath = "//*[contains(text(),'Agent Name') or contains(text(),'Agent')]/following::*[1]")
	protected WebElement agentNameValue;

	@FindBy(xpath = "//*[contains(text(),'Carrier') or contains(text(),'Insuring Company')]/following::*[1]")
	protected WebElement carrierValue;

	@FindBy(xpath = "//*[contains(text(),'Policy Effective') or contains(text(),'Effective Date')]/following::*[1]")
	protected WebElement policyEffectiveValue;

	@FindBy(xpath = "//*[contains(text(),'Policy Expiration') or contains(text(),'Expiration Date')]/following::*[1]")
	protected WebElement policyExpirationValue;

	@FindBy(xpath = "//*[contains(text(),'State')]/following::*[1]")
	protected WebElement stateValue;

	// ==================== Coverage Section ====================

	@FindBy(xpath = "//*[contains(text(),'General Liability') or contains(text(),'GL')]/following::*[contains(text(),'$')][1]")
	protected WebElement generalLiabilityValue;

	@FindBy(xpath = "//*[contains(text(),'Water/Sewer') or contains(text(),'WS') or contains(text(),'Water & Sewer')]/following::*[contains(text(),'$')][1]")
	protected WebElement waterSewerBackupValue;

	@FindBy(xpath = "//*[contains(text(),'Policy Fee')]/following::*[1]")
	protected WebElement policyFeeValue;

	// ==================== Location Table Section ====================

	@FindBy(xpath = "//table[.//th[contains(text(),'Property Address') or contains(text(),'Address')]]")
	protected WebElement locationTable;

	@FindBy(xpath = "//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'locations')]")
	protected WebElement locationCountText;

	// XPath for location count element
	protected final String locationCountXPath = "//*[@id='root']/div[2]/div[2]/div[2]/div[1]/div/div[2]/div[1]/p";

	// ==================== Tab Navigation ====================

	@FindBy(xpath = "//*[@id='policy-details-tab-bind-history']")
	protected WebElement bindHistoryTab;

	@FindBy(xpath = "//*[contains(@id,'tab-policy-details') or contains(text(),'Policy Details')]")
	protected WebElement policyDetailsTab;

	// ==================== Bind History Section ====================

	@FindBy(xpath = "//*[@id='radix-:rvu:-content-Bind History']/div/div/div/div/div/div/table/tbody/tr/td[6]/div/button[1]")
	protected WebElement downloadDocumentButton;

	// Alternative XPath for Download Document button
	@FindBy(xpath = "//button[contains(text(),'Download') or contains(@aria-label,'Download')]")
	protected WebElement downloadDocumentButtonAlt;

	// Bind History table
	@FindBy(xpath = "//div[contains(@id,'Bind History')]//table")
	protected WebElement bindHistoryTable;

	// ==================== Action Buttons ====================

	@FindBy(xpath = "//button[contains(text(),'Bind') and contains(text(),'Generate Policy')]")
	protected WebElement bindGeneratePolicyButton;

	@FindBy(xpath = "//button[contains(text(),'Bind')]")
	protected WebElement bindButton;

	// ==================== Constructor ====================

	public MasterPolicyLocators(WebDriver driver) {
		super(driver);
		logger.info("MasterPolicyLocators initialized");
	}

	// ==================== Helper Methods ====================

	/**
	 * Check if Master Policy Details page is displayed
	 */
	public boolean isMasterPolicyPageDisplayed() {
		try {
			String currentUrl = driver.getCurrentUrl();
			boolean isPolicyPage = currentUrl.contains("/policies/");
			logger.info("Master Policy page displayed: {} (URL: {})", isPolicyPage, currentUrl);
			return isPolicyPage;
		} catch (Exception e) {
			logger.warn("Error checking Master Policy page: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Get policy number from URL
	 */
	public String getPolicyNumberFromURL() {
		try {
			String currentUrl = driver.getCurrentUrl();
			if (currentUrl.contains("/policies/")) {
				String[] parts = currentUrl.split("/policies/");
				if (parts.length > 1) {
					String policyNumber = parts[1].split("[/?#]")[0];
					logger.info("Policy Number from URL: {}", policyNumber);
					return policyNumber;
				}
			}
		} catch (Exception e) {
			logger.warn("Error getting policy number from URL: {}", e.getMessage());
		}
		return "";
	}
}
