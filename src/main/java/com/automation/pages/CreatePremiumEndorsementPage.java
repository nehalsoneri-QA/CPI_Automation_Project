package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
 * Page Object for Create Premium Endorsement Page
 * Handles endorsement creation workflow
 */
public class CreatePremiumEndorsementPage extends CreatePremiumEndorsementLocators {

	public CreatePremiumEndorsementPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Verify if Create Premium Endorsement page is loaded
	 */
	public boolean isPageLoaded() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
			wait.until(ExpectedConditions.urlContains("create-premium-endorsement"));

			// Refresh page to ensure SPA content loads properly
			driver.navigate().refresh();
			sleep(3000);

			// Check for form elements (more reliable than title)
			wait.until(ExpectedConditions.or(
				ExpectedConditions.presenceOfElementLocated(By.id("create-endorsement-agent-select")),
				ExpectedConditions.presenceOfElementLocated(By.id("create-endorsement-carrier-select"))
			));

			logger.info("Create Premium Endorsement page loaded successfully");
			return true;
		} catch (Exception e) {
			logger.error("Create Premium Endorsement page not loaded: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Wait for page data to load (after API calls complete)
	 */
	public void waitForPageDataLoad() {
		try {
			sleep(3000); // Wait for API calls
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
			// Wait for form fields to be populated
			wait.until(ExpectedConditions.elementToBeClickable(submitEndorsementButton));
			logger.info("Page data loaded");
		} catch (Exception e) {
			logger.warn("Page data load wait timeout: {}", e.getMessage());
		}
	}

	/**
	 * Handle Warning Modal - Click Continue to proceed
	 */
	public boolean handleWarningModal() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(ExpectedConditions.visibilityOf(warningModalTitle));
			logger.info("Warning modal detected: {}", warningModalTitle.getText());

			// Click Continue button
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", warningModalContinueButton);
			logger.info("Clicked Continue on warning modal");
			sleep(2000);
			return true;
		} catch (Exception e) {
			logger.info("No warning modal detected or already dismissed");
			return false;
		}
	}

	/**
	 * Click Continue button on "Caution: Premium Endorsement" dialog
	 * This dialog appears when clicking Premium Endorsement button from Master Policy
	 */
	public boolean clickContinueOnCautionDialog() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			// Wait for the Caution dialog title to appear
			wait.until(ExpectedConditions.visibilityOf(warningModalTitle));
			String dialogTitle = warningModalTitle.getText();
			logger.info("Caution dialog detected with title: {}", dialogTitle);

			// Verify it's the Premium Endorsement caution dialog
			if (dialogTitle.contains("Caution") || dialogTitle.contains("Premium Endorsement")) {
				logger.info("'Caution: Premium Endorsement' dialog found - clicking Continue button");

				// Click Continue button using JavaScript for reliability
				wait.until(ExpectedConditions.elementToBeClickable(warningModalContinueButton));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", warningModalContinueButton);

				logger.info("Successfully clicked Continue on Caution: Premium Endorsement dialog");
				sleep(2000);
				return true;
			} else {
				logger.warn("Dialog found but title does not match expected: {}", dialogTitle);
				return false;
			}
		} catch (Exception e) {
			logger.info("Caution: Premium Endorsement dialog not displayed or already dismissed: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Get selected agent value
	 */
	public String getSelectedAgent() {
		try {
			return agentDropdown.getText();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get selected insured value
	 */
	public String getSelectedInsured() {
		try {
			return insuredDropdown.getText();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get Policy Number from URL or page
	 */
	public String getPolicyNumber() {
		try {
			// Try from URL first: /create-premium-endorsement/{policyNumber}
			String url = driver.getCurrentUrl();
			if (url.contains("create-premium-endorsement/")) {
				String[] parts = url.split("create-premium-endorsement/");
				if (parts.length > 1) {
					String policyNum = parts[1].split("[?#]")[0];
					logger.info("Policy Number from URL: {}", policyNum);
					return policyNum;
				}
			}

			// Try from page elements
			String[] xpaths = {
				"//h1[contains(text(),'Policy')]/following::*[1]",
				"//*[contains(text(),'Policy #')]/following::*[1]",
				"//*[contains(text(),'Policy Number')]/following::*[1]",
				"//span[contains(@class,'policy-number')]",
				"//*[@id='policy-number']"
			};

			for (String xpath : xpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement el : elements) {
						String text = el.getText().trim();
						if (text != null && !text.isEmpty() && text.matches(".*\\d+.*")) {
							logger.info("Policy Number from page: {}", text);
							return text;
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}
		} catch (Exception e) {
			logger.warn("Error getting policy number: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Get selected carrier value
	 */
	public String getSelectedCarrier() {
		try {
			return carrierDropdown.getText();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get effective date value (handles disabled fields)
	 */
	public String getEffectiveDate() {
		try {
			return getDateFromDisabledField(effectiveDatePicker, "create-endorsement-effective-date-picker");
		} catch (Exception e) {
			logger.warn("Error getting effective date: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Get expiration date value (handles disabled fields)
	 */
	public String getExpirationDate() {
		try {
			return getDateFromDisabledField(expirationDatePicker, "create-endorsement-expiration-date-picker");
		} catch (Exception e) {
			logger.warn("Error getting expiration date: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Helper method to get date value from disabled field
	 * Tries multiple approaches: value attribute, text content, JavaScript
	 * Handles segmented date pickers where month/day/year are in separate inputs
	 */
	private String getDateFromDisabledField(WebElement element, String elementId) {
		String value = "";

		// Approach 1: Try aria-valuetext (often contains formatted date)
		try {
			value = element.getAttribute("aria-valuetext");
			if (isValidDateValue(value)) {
				logger.info("Got date from aria-valuetext: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 2: Try data-value attribute
		try {
			value = element.getAttribute("data-value");
			if (isValidDateValue(value)) {
				logger.info("Got date from data-value: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 3: Try getAttribute("value")
		try {
			value = element.getAttribute("value");
			if (isValidDateValue(value)) {
				logger.info("Got date from value attribute: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 4: Try getText()
		try {
			value = element.getText();
			if (isValidDateValue(value)) {
				logger.info("Got date from getText(): {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 5: Try JavaScript to get value from disabled input
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"return document.getElementById('" + elementId + "').value;");
			if (isValidDateValue(value)) {
				logger.info("Got date from JavaScript: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 6: Try finding input element inside and get value
		try {
			WebElement input = element.findElement(By.tagName("input"));
			value = input.getAttribute("value");
			if (isValidDateValue(value)) {
				logger.info("Got date from inner input: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 7: Try reading segmented date picker (month-day-year in separate inputs/spans)
		try {
			List<WebElement> segments = element.findElements(By.cssSelector("input[data-segment], span[data-segment], [role='spinbutton']"));
			if (segments.size() >= 3) {
				StringBuilder dateBuilder = new StringBuilder();
				for (WebElement segment : segments) {
					String segmentValue = segment.getAttribute("value");
					if (segmentValue == null || segmentValue.isEmpty()) {
						segmentValue = segment.getText().trim();
					}
					if (segmentValue != null && !segmentValue.isEmpty() && !segmentValue.equals("0")) {
						if (dateBuilder.length() > 0) dateBuilder.append("/");
						dateBuilder.append(segmentValue);
					}
				}
				value = dateBuilder.toString();
				if (isValidDateValue(value)) {
					logger.info("Got date from segmented inputs: {}", value);
					return value;
				}
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 8: Try getting displayed text from spans inside element (excluding separator spans)
		try {
			List<WebElement> spans = element.findElements(By.tagName("span"));
			StringBuilder dateText = new StringBuilder();
			for (WebElement span : spans) {
				String text = span.getText().trim();
				// Skip separator spans like "-" or "/" or literal "0"
				if (text != null && !text.isEmpty() && !text.equals("-") && !text.equals("/") && !text.equals("0")) {
					if (dateText.length() > 0 && !dateText.toString().endsWith("/") && !dateText.toString().endsWith("-")) {
						dateText.append("/");
					}
					dateText.append(text);
				}
			}
			value = dateText.toString();
			if (isValidDateValue(value)) {
				logger.info("Got date from inner spans combined: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 9: Try JavaScript comprehensive approach with segmented date handling
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"var el = document.getElementById('" + elementId + "');" +
				"if(!el) return '';" +
				"// Try aria-valuetext first" +
				"if(el.getAttribute('aria-valuetext')) return el.getAttribute('aria-valuetext');" +
				"// Try data-value" +
				"if(el.getAttribute('data-value')) return el.getAttribute('data-value');" +
				"// Try value" +
				"if(el.value && el.value !== '--' && !/^0[-\\/]0[-\\/]/.test(el.value)) return el.value;" +
				"// Try segmented inputs with role spinbutton" +
				"var segments = el.querySelectorAll('[role=\"spinbutton\"], input[data-segment], [data-placeholder]');" +
				"if(segments.length >= 3) {" +
				"  var parts = [];" +
				"  segments.forEach(function(s) {" +
				"    var v = s.getAttribute('aria-valuenow') || s.value || s.textContent || '';" +
				"    if(v && v !== '0' && v.trim()) parts.push(v.trim());" +
				"  });" +
				"  if(parts.length >= 3) return parts.join('/');" +
				"}" +
				"// Try getting all numeric text content" +
				"var allText = el.innerText || el.textContent || '';" +
				"var dateMatch = allText.match(/(\\d{1,2})[-\\/](\\d{1,2})[-\\/](\\d{2,4})/);" +
				"if(dateMatch) return dateMatch[0];" +
				"return allText.replace(/\\s+/g, '').replace(/--/g, '');");
			if (isValidDateValue(value)) {
				logger.info("Got date from JavaScript comprehensive: {}", value);
				return value;
			}
		} catch (Exception e) {
			logger.warn("All approaches failed to get date value for element: {}", elementId);
		}

		logger.warn("Could not extract valid date from element: {}. Raw value: '{}'", elementId, value);
		return value != null ? value : "";
	}

	/**
	 * Check if date value is valid (not null, not empty, not placeholder, not invalid format)
	 */
	private boolean isValidDateValue(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		// Reject known invalid patterns
		if (value.equals("--") || value.equals("null") || value.equals("Select")) {
			return false;
		}
		// Reject patterns like "0-0-", "0/0/", "0-0-0", etc. (empty segmented date)
		if (value.matches("^0[-/]0[-/].*") || value.matches(".*[-/]0[-/]0$") || value.equals("0-0-") || value.equals("0/0/")) {
			return false;
		}
		// Reject if it's just separators
		if (value.matches("^[-/]+$")) {
			return false;
		}
		// Must contain at least one digit to be a valid date
		if (!value.matches(".*\\d+.*")) {
			return false;
		}
		return true;
	}

	/**
	 * Get endorsement effective date value (handles disabled fields)
	 */
	public String getEndorsementEffectiveDate() {
		try {
			return getDateFromDisabledField(endorsementEffectiveDatePicker, "create-endorsement-endorsement-effective-date-picker");
		} catch (Exception e) {
			logger.warn("Error getting endorsement effective date: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Set endorsement effective date
	 */
	public void setEndorsementEffectiveDate(String date) {
		try {
			endorsementEffectiveDatePicker.clear();
			endorsementEffectiveDatePicker.sendKeys(date);
			logger.info("Set endorsement effective date: {}", date);
		} catch (Exception e) {
			logger.error("Error setting endorsement effective date: {}", e.getMessage());
		}
	}

	/**
	 * Get General Liability amount
	 */
	public String getGeneralLiabilityAmount() {
		try {
			return generalLiabilityAmountInput.getAttribute("value");
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get Water/Sewer Backup amount
	 */
	public String getWaterSewerBackupAmount() {
		try {
			return waterSewerBackupAmountInput.getAttribute("value");
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Check if General Liability is selected as Yes
	 */
	public boolean isGeneralLiabilityYes() {
		try {
			return generalLiabilityYesRadio.isSelected() ||
				"true".equals(generalLiabilityYesRadio.getAttribute("data-state"));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if Water/Sewer Backup is selected as Yes
	 */
	public boolean isWaterSewerBackupYes() {
		try {
			return waterSewerBackupYesRadio.isSelected() ||
				"true".equals(waterSewerBackupYesRadio.getAttribute("data-state"));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Get selected suggested state (handles disabled dropdowns)
	 */
	public String getSelectedState() {
		try {
			String value = getValueFromDisabledDropdown(suggestedStateDropdown, "create-endorsement-suggested-state-select");

			// If still empty, try alternative xpaths for Radix UI Select
			if (value == null || value.isEmpty() || value.equals("--")) {
				value = getStateByAlternativeXpath();
			}

			return value;
		} catch (Exception e) {
			logger.warn("Error getting selected state: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Get state value using alternative xpaths for Radix UI Select component
	 */
	private String getStateByAlternativeXpath() {
		String[] xpaths = {
			"//*[@id='create-endorsement-suggested-state-select']//span[contains(@class,'SelectValue')]",
			"//*[@id='create-endorsement-suggested-state-select']/span",
			"//*[@id='create-endorsement-suggested-state-select']",
			"//button[@id='create-endorsement-suggested-state-select']//span",
			"//label[contains(text(),'State')]/following-sibling::*//span",
			"//label[contains(text(),'Suggested State')]/following-sibling::*//span",
			"//*[contains(@id,'state-select')]//span[not(contains(@class,'icon'))]",
			"//*[contains(@id,'state')]//button//span"
		};

		for (String xpath : xpaths) {
			try {
				List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					String text = el.getText();
					if (text != null && !text.isEmpty() && !text.equals("--") && !text.equals("Select") && text.length() >= 2) {
						logger.info("Got state from xpath '{}': {}", xpath, text);
						return text;
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Try JavaScript to find state value
		try {
			String value = (String) ((JavascriptExecutor) driver).executeScript(
				"var selects = document.querySelectorAll('[id*=\"state\"]');" +
				"for(var i=0; i<selects.length; i++) {" +
				"  var el = selects[i];" +
				"  var text = el.innerText || el.textContent || '';" +
				"  if(text && text.trim().length >= 2 && text !== '--' && text !== 'Select') {" +
				"    return text.trim();" +
				"  }" +
				"}" +
				"return '';");
			if (value != null && !value.isEmpty() && !value.equals("--")) {
				logger.info("Got state from JavaScript query: {}", value);
				return value;
			}
		} catch (Exception e) {
			logger.warn("JavaScript state query failed: {}", e.getMessage());
		}

		return "";
	}

	/**
	 * Helper method to get value from disabled dropdown
	 * Tries multiple approaches: getText, value attribute, JavaScript
	 */
	private String getValueFromDisabledDropdown(WebElement element, String elementId) {
		String value = "";

		// Approach 1: Try getText()
		try {
			value = element.getText();
			if (value != null && !value.isEmpty() && !value.equals("--") && !value.equals("Select")) {
				logger.info("Got dropdown value from getText(): {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 2: Try getAttribute("value")
		try {
			value = element.getAttribute("value");
			if (value != null && !value.isEmpty()) {
				logger.info("Got dropdown value from value attribute: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 3: Try JavaScript to get innerText
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"var el = document.getElementById('" + elementId + "');" +
				"if(el) { return el.innerText || el.textContent || el.value || ''; }" +
				"return '';");
			if (value != null && !value.isEmpty() && !value.equals("--")) {
				logger.info("Got dropdown value from JavaScript innerText: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 4: Try finding selected option or span inside
		try {
			List<WebElement> spans = element.findElements(By.tagName("span"));
			for (WebElement span : spans) {
				String text = span.getText();
				if (text != null && !text.isEmpty() && !text.equals("--") && !text.equals("Select")) {
					logger.info("Got dropdown value from inner span: {}", text);
					return text;
				}
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 5: Try xpath to find text content
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"var el = document.getElementById('" + elementId + "');" +
				"if(el) {" +
				"  var spans = el.getElementsByTagName('span');" +
				"  for(var i=0; i<spans.length; i++) {" +
				"    var text = spans[i].innerText || spans[i].textContent;" +
				"    if(text && text.trim() && text !== '--' && text !== 'Select') return text.trim();" +
				"  }" +
				"  return el.innerText || el.textContent || '';" +
				"}" +
				"return '';");
			if (value != null && !value.isEmpty() && !value.equals("--")) {
				logger.info("Got dropdown value from JavaScript spans: {}", value);
				return value;
			}
		} catch (Exception e) {
			logger.warn("All approaches failed to get dropdown value");
		}

		return value != null ? value : "";
	}

	/**
	 * Override clickNewLocationButton from parent to use endorsement page XPaths
	 */
	@Override
	public void clickNewLocationButton() {
		clickNewLocation();
	}

	/**
	 * Click New Location button to add a new location
	 * Tries multiple xpath strategies to find the button
	 */
	public void clickNewLocation() {
		String[] buttonXpaths = {
			"//button[contains(text(),'New Location')]",
			"//button[contains(text(),'Add Location')]",
			"//button[contains(text(),'New location')]",
			"//button[contains(text(),'Add location')]",
			"//button[normalize-space()='New Location']",
			"//button[normalize-space()='Add Location']",
			"//*[contains(@id,'new-location') or contains(@id,'add-location')]",
			"//*[contains(@id,'NewLocation') or contains(@id,'AddLocation')]",
			"//button[contains(@class,'location')]//span[contains(text(),'New') or contains(text(),'Add')]/..",
			"//button[.//*[contains(text(),'New Location') or contains(text(),'Add Location')]]",
			"//button[contains(@aria-label,'New Location') or contains(@aria-label,'Add Location')]",
			"//div[contains(@class,'location')]//button[contains(@class,'add') or contains(@class,'new')]",
			"//button[contains(@class,'btn') and (contains(.,'New') or contains(.,'Add')) and contains(.,'Location')]"
		};

		for (String xpath : buttonXpaths) {
			try {
				List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				for (WebElement button : buttons) {
					if (button.isDisplayed() && button.isEnabled()) {
						// Scroll to button first
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", button);
						sleep(500);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
						logger.info("Clicked New Location button using xpath: {}", xpath);
						sleep(1000);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Last resort: try using the @FindBy locator
		try {
			if (newLocationButton != null && newLocationButton.isDisplayed()) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", newLocationButton);
				logger.info("Clicked New Location button using @FindBy locator");
				sleep(1000);
				return;
			}
		} catch (Exception e) {
			// Continue
		}

		logger.error("Could not find New Location button with any xpath strategy");
		throw new RuntimeException("New Location button not found");
	}

	/**
	 * Click Multi Edit button
	 */
	public void clickMultiEdit() {
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", multiEditButton);
			logger.info("Clicked Multi Edit button");
			sleep(1000);
		} catch (Exception e) {
			logger.error("Error clicking Multi Edit: {}", e.getMessage());
		}
	}

	/**
	 * Get location count from "Showing X to Y of Z locations" text
	 */
	public int getLocationCount() {
		try {
			// Find the text "Showing X to Y of Z locations"
			String[] xpaths = {
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'locations')]",
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'location')]",
				"//p[contains(text(),'Showing')]",
				"//span[contains(text(),'Showing')]",
				"//div[contains(text(),'Showing') and contains(text(),'locations')]"
			};

			for (String xpath : xpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement element : elements) {
						String text = element.getText();
						if (text != null && text.contains("of") && text.contains("location")) {
							// Extract count using regex: "Showing 1 to 13 of 13 locations"
							java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("of\\s+(\\d+)\\s+location");
							java.util.regex.Matcher matcher = pattern.matcher(text);
							if (matcher.find()) {
								int count = Integer.parseInt(matcher.group(1));
								logger.info("Location count from text '{}': {}", text, count);
								return count;
							}
						}
					}
				} catch (Exception e) {
					// Continue to next xpath
				}
			}

			// Fallback: count table rows
			logger.info("Falling back to counting table rows");
			return locationTableRows.size();

		} catch (Exception e) {
			logger.warn("Error getting location count: {}", e.getMessage());
			return 0;
		}
	}

	/**
	 * Get location count text (e.g., "Showing 1 to 13 of 13 locations")
	 */
	public String getLocationCountText() {
		try {
			String[] xpaths = {
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'locations')]",
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'location')]",
				"//p[contains(text(),'Showing')]",
				"//span[contains(text(),'Showing')]"
			};

			for (String xpath : xpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement element : elements) {
						String text = element.getText();
						if (text != null && text.contains("Showing") && text.contains("of")) {
							logger.info("Found location count text: {}", text);
							return text;
						}
					}
				} catch (Exception e) {
					// Continue to next xpath
				}
			}
		} catch (Exception e) {
			logger.warn("Error getting location count text: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Check if submit button is enabled
	 */
	public boolean isSubmitButtonEnabled() {
		try {
			return submitEndorsementButton.isEnabled() &&
				!submitEndorsementButton.getAttribute("class").contains("disabled");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Click Submit Endorsement button
	 */
	public void clickSubmitEndorsement() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(ExpectedConditions.elementToBeClickable(submitEndorsementButton));
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitEndorsementButton);
			logger.info("Clicked Submit Endorsement button");
			sleep(3000);
		} catch (Exception e) {
			logger.error("Error clicking Submit Endorsement: {}", e.getMessage());
		}
	}

	/**
	 * Handle date confirmation dialog if it appears
	 */
	public void handleDateConfirmDialog() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
			wait.until(ExpectedConditions.elementToBeClickable(dateConfirmButton));
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateConfirmButton);
			logger.info("Clicked Confirm on date confirmation dialog");
			sleep(1000);
		} catch (Exception e) {
			logger.info("No date confirmation dialog detected");
		}
	}

	/**
	 * Capture all endorsement form values
	 */
	public Map<String, String> captureEndorsementFormValues() {
		Map<String, String> values = new HashMap<>();

		try {
			values.put("Agent", getSelectedAgent());
			values.put("Insured", getSelectedInsured());
			values.put("Carrier", getSelectedCarrier());

			// Capture dates with validation
			String effectiveDate = getEffectiveDate();
			String expirationDate = getExpirationDate();
			String endorsementEffectiveDate = getEndorsementEffectiveDate();

			// Validate dates are properly captured
			if (!isValidDateValue(effectiveDate)) {
				logger.warn("EffectiveDate not captured properly: '{}'", effectiveDate);
				effectiveDate = "CAPTURE_FAILED";
			}
			if (!isValidDateValue(expirationDate)) {
				logger.warn("ExpirationDate not captured properly: '{}'", expirationDate);
				expirationDate = "CAPTURE_FAILED";
			}
			if (!isValidDateValue(endorsementEffectiveDate)) {
				logger.warn("EndorsementEffectiveDate not captured properly: '{}'", endorsementEffectiveDate);
				endorsementEffectiveDate = "CAPTURE_FAILED";
			}

			values.put("EffectiveDate", effectiveDate);
			values.put("ExpirationDate", expirationDate);
			values.put("EndorsementEffectiveDate", endorsementEffectiveDate);
			values.put("GLAmount", getGeneralLiabilityAmount());
			values.put("WSAmount", getWaterSewerBackupAmount());
			values.put("State", getSelectedState());
			values.put("LocationCount", String.valueOf(getLocationCount()));

			// Log captured values
			logger.info("Captured endorsement form values: {}", values);

			// Log date capture status
			boolean datesCapturedOk = !effectiveDate.equals("CAPTURE_FAILED") &&
				!expirationDate.equals("CAPTURE_FAILED") &&
				!endorsementEffectiveDate.equals("CAPTURE_FAILED");

			if (!datesCapturedOk) {
				logger.error("One or more dates failed to capture properly!");
				captureEndorsementScreenshot("Date Capture Failed");
			}
		} catch (Exception e) {
			logger.error("Error capturing endorsement form values: {}", e.getMessage());
		}

		return values;
	}

	/**
	 * Check if all critical values were captured correctly
	 * @param values the captured form values
	 * @return true if all critical values are valid
	 */
	public boolean validateCapturedValues(Map<String, String> values) {
		if (values == null || values.isEmpty()) {
			return false;
		}

		boolean valid = true;
		String[] criticalFields = {"EffectiveDate", "ExpirationDate", "EndorsementEffectiveDate"};

		for (String field : criticalFields) {
			String value = values.get(field);
			if (value == null || value.isEmpty() || value.equals("CAPTURE_FAILED")) {
				logger.error("Critical field '{}' not captured properly: '{}'", field, value);
				valid = false;
			}
		}

		return valid;
	}

	/**
	 * Verify endorsement was created successfully by checking URL redirect
	 */
	public boolean verifyEndorsementCreated() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
			// After successful creation, it redirects to edit-premium-endorsement or quotes page
			wait.until(driver -> {
				String url = driver.getCurrentUrl();
				return url.contains("edit-premium-endorsement") || url.contains("/quotes");
			});
			logger.info("Endorsement created successfully, redirected to: {}", driver.getCurrentUrl());
			return true;
		} catch (Exception e) {
			logger.error("Endorsement creation verification failed: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Get Grand Total value from calculation section
	 */
	public String getGrandTotal() {
		try {
			// Try multiple approaches to find Grand Total
			List<WebElement> elements = driver.findElements(
				By.xpath("//*[contains(text(),'Grand Total')]/following-sibling::*"));
			if (!elements.isEmpty()) {
				return elements.get(0).getText();
			}
			// Alternative: look in table rows
			elements = driver.findElements(
				By.xpath("//tr[contains(.,'Grand Total')]//td[last()]"));
			if (!elements.isEmpty()) {
				return elements.get(0).getText();
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get Due Amount value
	 */
	public String getDueAmount() {
		try {
			List<WebElement> elements = driver.findElements(
				By.xpath("//*[contains(text(),'Due Amount')]/following-sibling::*"));
			if (!elements.isEmpty()) {
				return elements.get(0).getText();
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	// ==================== Premium Validation Methods ====================

	/**
	 * Validate premium calculations on endorsement page
	 * Returns a result object with all validation details
	 */
	public PremiumValidationResult validateEndorsementPremiums() {
		logger.info("=== Validating Premium Calculations ===");
		PremiumValidationResult result = new PremiumValidationResult();

		try {
			// Get column sums from location table
			double propertyPremiumSum = getColumnSum("Property Premium", "Premium");
			double glPremiumSum = getColumnSum("GL Premium", "GL");
			double wsPremiumSum = getColumnSum("Water/Sewer Premium", "Water/Sewer", "WS Premium", "WSB");
			double taxesSum = getColumnSum("Taxes", "Tax");
			double feesSum = getColumnSum("Fees", "Fee");

			result.setPropertyPremiumSum(propertyPremiumSum);
			result.setGlPremiumSum(glPremiumSum);
			result.setWsPremiumSum(wsPremiumSum);
			result.setTaxesSum(taxesSum);
			result.setFeesSum(feesSum);

			logger.info("Column Sums - Property: {}, GL: {}, WS: {}, Taxes: {}, Fees: {}",
				propertyPremiumSum, glPremiumSum, wsPremiumSum, taxesSum, feesSum);

			// Get values from page summary section
			String premiumGLWSText = getValueFromXpath("//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[1]/div[1]/div/h2");
			String taxesText = getValueFromXpath("//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[1]/div[2]/div/h2");
			String totalFeesText = getValueFromXpath("//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[1]/div[3]/div/h2");
			String grandTotalText = getValueFromXpath("//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[1]/div[4]/div/h2");

			double premiumGLWSValue = parseAmount(premiumGLWSText);
			double taxesValue = parseAmount(taxesText);
			double totalFeesValue = parseAmount(totalFeesText);
			double grandTotalValue = parseAmount(grandTotalText);

			result.setPremiumGLWSValue(premiumGLWSValue);
			result.setTaxesValue(taxesValue);
			result.setTotalFeesValue(totalFeesValue);
			result.setGrandTotalValue(grandTotalValue);

			logger.info("Page Values - Premium(GL+WS): {}, Taxes: {}, Fees: {}, Grand Total: {}",
				premiumGLWSValue, taxesValue, totalFeesValue, grandTotalValue);

			// Validation 1: Property Premium + GL Premium + WS Premium = Premium (GL + WS)
			double calculatedPremiumGLWS = propertyPremiumSum + glPremiumSum + wsPremiumSum;
			result.setCalculatedPremiumGLWS(calculatedPremiumGLWS);
			boolean premiumMatch = Math.abs(calculatedPremiumGLWS - premiumGLWSValue) < 0.01;
			result.setPremiumGLWSMatch(premiumMatch);
			logger.info("Validation 1: {} + {} + {} = {} vs {} = {}",
				propertyPremiumSum, glPremiumSum, wsPremiumSum, calculatedPremiumGLWS, premiumGLWSValue, premiumMatch ? "PASS" : "FAIL");

			// Validation 2: Sum of Taxes column = Taxes value
			boolean taxesMatch = Math.abs(taxesSum - taxesValue) < 0.01;
			result.setTaxesMatch(taxesMatch);
			logger.info("Validation 2: Taxes Sum {} vs {} = {}",
				taxesSum, taxesValue, taxesMatch ? "PASS" : "FAIL");

			// Validation 3: Sum of Fees column = Total Fees value
			boolean feesMatch = Math.abs(feesSum - totalFeesValue) < 0.01;
			result.setFeesMatch(feesMatch);
			logger.info("Validation 3: Fees Sum {} vs {} = {}",
				feesSum, totalFeesValue, feesMatch ? "PASS" : "FAIL");

			// Validation 4: Premium (GL + WS) + Taxes + Total Fees = Grand Total
			double calculatedGrandTotal = premiumGLWSValue + taxesValue + totalFeesValue;
			result.setCalculatedGrandTotal(calculatedGrandTotal);
			boolean grandTotalMatch = Math.abs(calculatedGrandTotal - grandTotalValue) < 0.01;
			result.setGrandTotalMatch(grandTotalMatch);
			logger.info("Validation 4: {} + {} + {} = {} vs {} = {}",
				premiumGLWSValue, taxesValue, totalFeesValue, calculatedGrandTotal, grandTotalValue, grandTotalMatch ? "PASS" : "FAIL");

			// Overall result
			result.setAllValidationsPassed(premiumMatch && taxesMatch && feesMatch && grandTotalMatch);

		} catch (Exception e) {
			logger.error("Error validating premium calculations: {}", e.getMessage());
			result.setError(e.getMessage());
		}

		return result;
	}

	/**
	 * Get sum of a column from location table
	 */
	private double getColumnSum(String... columnNames) {
		double sum = 0;
		try {
			// Find the table
			WebElement table = findLocationTable();
			if (table == null) {
				logger.warn("Location table not found for column sum");
				return 0;
			}

			// Get headers to find column index
			List<WebElement> headers = table.findElements(By.xpath(".//thead//th | .//tr[1]//th"));
			int columnIndex = -1;

			for (int i = 0; i < headers.size(); i++) {
				String headerText = headers.get(i).getText().trim().toLowerCase();
				for (String colName : columnNames) {
					if (headerText.contains(colName.toLowerCase())) {
						columnIndex = i;
						break;
					}
				}
				if (columnIndex >= 0) break;
			}

			if (columnIndex < 0) {
				logger.warn("Column not found: {}", String.join("/", columnNames));
				return 0;
			}

			// Get all rows and sum the column values
			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			for (WebElement row : rows) {
				try {
					List<WebElement> cells = row.findElements(By.xpath(".//td"));
					if (cells.size() > columnIndex) {
						String cellText = cells.get(columnIndex).getText().trim();
						double value = parseAmount(cellText);
						sum += value;
					}
				} catch (Exception e) {
					// Skip row
				}
			}

			logger.info("Column '{}' sum: {}", columnNames[0], sum);

		} catch (Exception e) {
			logger.error("Error calculating column sum: {}", e.getMessage());
		}
		return sum;
	}

	/**
	 * Find the location table on the page
	 */
	private WebElement findLocationTable() {
		String[] tableXpaths = {
			"//table[.//th[contains(text(),'Premium') or contains(text(),'Address')]]",
			"//table[contains(@class,'location')]",
			"//table[.//th[contains(text(),'GL') or contains(text(),'Taxes')]]",
			"//*[@id='root']//table"
		};

		for (String xpath : tableXpaths) {
			try {
				List<WebElement> tables = driver.findElements(By.xpath(xpath));
				for (WebElement table : tables) {
					if (table.isDisplayed()) {
						List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
						if (rows.size() > 0) {
							return table;
						}
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
		return null;
	}

	/**
	 * Get value from specific xpath
	 */
	private String getValueFromXpath(String xpath) {
		try {
			WebElement element = driver.findElement(By.xpath(xpath));
			String text = element.getText().trim();
			logger.info("Value from xpath {}: {}", xpath, text);
			return text;
		} catch (Exception e) {
			logger.warn("Could not get value from xpath: {}", xpath);
			return "0";
		}
	}

	/**
	 * Parse amount string to double (removes $, commas, etc.)
	 */
	private double parseAmount(String text) {
		if (text == null || text.isEmpty()) return 0;
		try {
			String cleaned = text.replaceAll("[^0-9.-]", "");
			if (cleaned.isEmpty()) return 0;
			return Double.parseDouble(cleaned);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	// ==================== Property Premium Validation Methods ====================

	/**
	 * Validate Display Computations by clicking dialog once and capturing all location data
	 * Formula: TIV = Dwelling + AS + BPP + LossOfRents
	 *          Property Premium = TIV / 100 * Rate
	 * Handles pagination if Next button is enabled
	 */
	public PropertyPremiumValidationResult validateDisplayComputations() {
		logger.info("=== Validating Display Computations for All Locations ===");
		PropertyPremiumValidationResult result = new PropertyPremiumValidationResult();

		try {
			// First get table data for comparison
			WebElement table = findLocationTable();
			if (table == null) {
				logger.error("Location table not found");
				result.setError("Location table not found");
				return result;
			}

			// Capture table data (address -> displayed premium)
			Map<String, Double> tableDisplayedPremiums = new java.util.HashMap<>();
			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			List<WebElement> headers = table.findElements(By.xpath(".//thead//th"));

			int propertyPremiumColIndex = -1;
			int addressColIndex = -1;
			for (int i = 0; i < headers.size(); i++) {
				String headerText = headers.get(i).getText().trim().toLowerCase();
				if (headerText.contains("property") && headerText.contains("premium")) {
					propertyPremiumColIndex = i;
				}
				if (headerText.contains("address") || headerText.contains("location")) {
					addressColIndex = i;
				}
			}

			for (int i = 0; i < rows.size(); i++) {
				List<WebElement> cells = rows.get(i).findElements(By.xpath(".//td"));
				String address = addressColIndex >= 0 && cells.size() > addressColIndex ?
					cells.get(addressColIndex).getText().trim() : "Location " + (i + 1);
				double premium = propertyPremiumColIndex >= 0 && cells.size() > propertyPremiumColIndex ?
					parseAmount(cells.get(propertyPremiumColIndex).getText()) : 0;
				tableDisplayedPremiums.put(address, premium);
			}
			logger.info("Captured {} locations from table", tableDisplayedPremiums.size());

			// Click Display Computation dialog button ONCE
			boolean dialogOpened = openDisplayComputationDialog();
			if (!dialogOpened) {
				logger.error("Failed to open Display Computation dialog");
				result.setError("Failed to open Display Computation dialog");
				return result;
			}

			// Capture all location data from dialog (with pagination)
			List<LocationComputationData> allLocationData = captureAllLocationsFromDialog();
			logger.info("Captured {} locations from dialog", allLocationData.size());

			// Close the dialog - click "Close Rate Calculation Details" button
			try {
				WebElement closeBtn = driver.findElement(By.xpath("//button[contains(text(),'Close Rate Calculation Details')]"));
				if (closeBtn.isDisplayed()) {
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);
					logger.info("Closed Display Computation dialog");
					sleep(500);
				}
			} catch (Exception e) {
				logger.warn("Could not find Close button, trying Escape key");
				driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
				sleep(300);
			}

			// Match captured data with table and validate
			for (LocationComputationData compData : allLocationData) {
				String address = compData.getAddress();
				Double displayedPremium = tableDisplayedPremiums.getOrDefault(address, 0.0);

				// Calculate TIV and Property Premium
				double tiv = compData.getDwelling() + compData.getAdditionalStructures() +
					compData.getBpp() + compData.getLossOfRents();
				double calculatedPremium = (tiv / 100.0) * compData.getRate();
				calculatedPremium = Math.round(calculatedPremium * 100.0) / 100.0;

				boolean isMatch = Math.abs(calculatedPremium - displayedPremium) < 1.0;

				LocationPremiumValidation locValidation = new LocationPremiumValidation();
				locValidation.setAddress(address);
				locValidation.setDwelling(compData.getDwelling());
				locValidation.setAdditionalStructures(compData.getAdditionalStructures());
				locValidation.setBpp(compData.getBpp());
				locValidation.setLossOfRents(compData.getLossOfRents());
				locValidation.setRate(compData.getRate());
				locValidation.setTaxes(compData.getTaxes());
				locValidation.setTiv(tiv);
				locValidation.setCalculatedPremium(calculatedPremium);
				locValidation.setDisplayedPremium(displayedPremium);
				locValidation.setMatch(isMatch);

				result.addLocationValidation(locValidation);

				logger.info("Location {}: TIV={}, Rate={}, Calculated={}, Displayed={}, Match={}",
					address, tiv, compData.getRate(), calculatedPremium, displayedPremium, isMatch);
			}

			result.calculateOverallResult();

		} catch (Exception e) {
			logger.error("Error in display computations validation: {}", e.getMessage());
			result.setError(e.getMessage());
		}

		return result;
	}

	/**
	 * Open Display Computation dialog
	 */
	private boolean openDisplayComputationDialog() {
		String[] dialogBtnXpaths = {
			"//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[2]/div/button",
			"//button[contains(text(),'Display Computation')]",
			"//button[contains(@title,'Display Computation')]"
		};

		for (String xpath : dialogBtnXpaths) {
			try {
				List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					if (el.isDisplayed()) {
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", el);
						sleep(300);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
						sleep(1500);

						// Wait for dialog
						WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
						try {
							wait.until(ExpectedConditions.or(
								ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='dialog']")),
								ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'Dialog')]"))
							));
							logger.info("Display Computation dialog opened successfully");
							return true;
						} catch (Exception e) {
							// Continue trying
						}
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
		return false;
	}

	/**
	 * Capture all location data from dialog with pagination support
	 */
	private List<LocationComputationData> captureAllLocationsFromDialog() {
		List<LocationComputationData> allData = new java.util.ArrayList<>();

		do {
			// Capture current page data
			List<LocationComputationData> pageData = captureCurrentPageLocations();
			allData.addAll(pageData);
			logger.info("Captured {} locations from current page", pageData.size());

			// Check if Next button exists and is enabled
		} while (clickNextPageIfEnabled());

		return allData;
	}

	/**
	 * Capture location data from current dialog page
	 */
	private List<LocationComputationData> captureCurrentPageLocations() {
		List<LocationComputationData> locations = new java.util.ArrayList<>();

		try {
			// Wait for dialog to be visible and find dialog table rows
			// IMPORTANT: Only get table inside the dialog, not the main page table
			sleep(1000);

			List<WebElement> dialogRows = null;

			// First, find the dialog container
			String[] dialogXpaths = {
				"//*[contains(@id,'radix-')]",
				"//div[@role='dialog']",
				"//div[contains(@class,'DialogContent')]"
			};

			WebElement dialogContainer = null;
			for (String xpath : dialogXpaths) {
				try {
					List<WebElement> dialogs = driver.findElements(By.xpath(xpath));
					for (WebElement d : dialogs) {
						if (d.isDisplayed()) {
							dialogContainer = d;
							logger.info("Found dialog container using: {}", xpath);
							break;
						}
					}
					if (dialogContainer != null) break;
				} catch (Exception e) {
					// Continue
				}
			}

			if (dialogContainer != null) {
				// Find table rows ONLY within the dialog container
				dialogRows = dialogContainer.findElements(By.xpath(".//table//tbody//tr"));
				logger.info("Found {} rows in dialog table", dialogRows.size());
			}

			if (dialogRows == null || dialogRows.isEmpty()) {
				logger.warn("No rows found in dialog table");
				return locations;
			}

			// Use fixed column indices based on dialog table structure
			// Dialog columns: Location(0), Dwelling(1), Additional Structures(2), BPP(3), Loss Of Rents(4), Rate(5), Taxes(6), TIV(7), Property Premium(8)
			Map<String, Integer> headerIndices = new java.util.HashMap<>();
			headerIndices.put("location", 0);
			headerIndices.put("dwelling", 1);
			headerIndices.put("as", 2);
			headerIndices.put("bpp", 3);
			headerIndices.put("lor", 4);
			headerIndices.put("rate", 5);
			headerIndices.put("taxes", 6);
			logger.info("Using fixed column indices for dialog table");

			for (WebElement row : dialogRows) {
				try {
					List<WebElement> cells = row.findElements(By.xpath(".//td"));

					if (cells.size() < 6) {
						logger.warn("Row has only {} cells, skipping", cells.size());
						continue;
					}

					LocationComputationData data = new LocationComputationData();

					// Extract values using column indices
					data.setAddress(cells.get(headerIndices.get("location")).getText().trim());
					data.setDwelling(getCellValue(cells, headerIndices.get("dwelling")));
					data.setAdditionalStructures(getCellValue(cells, headerIndices.get("as")));
					data.setBpp(getCellValue(cells, headerIndices.get("bpp")));
					data.setLossOfRents(getCellValue(cells, headerIndices.get("lor")));
					data.setRate(getCellValue(cells, headerIndices.get("rate")));
					data.setTaxes(getCellValue(cells, headerIndices.get("taxes")));

					logger.info("Location: {}, Dwelling: {}, AS: {}, BPP: {}, LOR: {}, Rate: {}, Taxes: {}",
						data.getAddress(), data.getDwelling(), data.getAdditionalStructures(),
						data.getBpp(), data.getLossOfRents(), data.getRate(), data.getTaxes());

					locations.add(data);
				} catch (Exception e) {
					logger.warn("Error parsing dialog row: {}", e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("Error capturing dialog locations: {}", e.getMessage());
		}

		return locations;
	}

	/**
	 * Get header column indices from dialog table
	 * Column mappings: Location, Dwelling(Coverage A), Additional Structures(Coverage B),
	 * BPP(Coverage C), Loss Of Rents(Coverage D), Rate, Taxes
	 */
	private Map<String, Integer> getDialogHeaderIndices() {
		Map<String, Integer> indices = new java.util.HashMap<>();

		try {
			String[] headerXpaths = {
				"//*[contains(@id,'radix')]//table//thead//th",
				"//div[@role='dialog']//table//thead//th",
				"//div[contains(@class,'Dialog')]//table//thead//th",
				"//table//thead//th"
			};

			List<WebElement> headers = null;
			for (String xpath : headerXpaths) {
				try {
					headers = driver.findElements(By.xpath(xpath));
					if (!headers.isEmpty()) {
						logger.info("Found {} headers using xpath: {}", headers.size(), xpath);
						break;
					}
				} catch (Exception e) {
					// Continue
				}
			}

			if (headers != null) {
				// Log all header texts
				StringBuilder headerTexts = new StringBuilder("Header texts: ");
				for (int i = 0; i < headers.size(); i++) {
					String text = headers.get(i).getText().trim();
					headerTexts.append("[").append(i).append("]=").append(text).append(" ");
				}
				logger.info(headerTexts.toString());

				for (int i = 0; i < headers.size(); i++) {
					String text = headers.get(i).getText().trim().toLowerCase();
					// Location column
					if (text.contains("location")) indices.put("location", i);
					// Dwelling = Coverage A
					if (text.contains("dwelling") || text.contains("coverage a")) indices.put("dwelling", i);
					// Additional Structures = Coverage B
					if (text.contains("additional") || text.contains("coverage b")) indices.put("as", i);
					// BPP = Coverage C
					if (text.contains("bpp") || text.contains("coverage c")) indices.put("bpp", i);
					// Loss Of Rents = Coverage D
					if (text.contains("loss") || text.contains("coverage d")) indices.put("lor", i);
					// Rate
					if (text.contains("rate")) indices.put("rate", i);
					// Taxes
					if (text.contains("tax")) indices.put("taxes", i);
				}
			}
		} catch (Exception e) {
			logger.warn("Error getting dialog header indices: {}", e.getMessage());
		}

		return indices;
	}

	/**
	 * Get cell value at index
	 */
	private double getCellValue(List<WebElement> cells, int index) {
		if (index < 0 || index >= cells.size()) return 0;
		return parseAmount(cells.get(index).getText());
	}

	/**
	 * Click Next page button if enabled
	 */
	private boolean clickNextPageIfEnabled() {
		String[] nextBtnXpaths = {
			"//*[contains(@id,'radix')]//button[contains(text(),'Next')]",
			"//div[@role='dialog']//button[contains(text(),'Next')]",
			"//div[contains(@class,'Dialog')]//button[contains(text(),'Next')]"
		};

		for (String xpath : nextBtnXpaths) {
			try {
				List<WebElement> btns = driver.findElements(By.xpath(xpath));
				for (WebElement btn : btns) {
					if (btn.isDisplayed()) {
						// Check if button is disabled
						String disabled = btn.getAttribute("disabled");
						boolean isDisabled = disabled != null && (disabled.equals("true") || disabled.equals("disabled"));
						String ariaDisabled = btn.getAttribute("aria-disabled");
						boolean isAriaDisabled = ariaDisabled != null && ariaDisabled.equals("true");

						if (!isDisabled && !isAriaDisabled && btn.isEnabled()) {
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
							sleep(1000);
							logger.info("Clicked Next page button");
							return true;
						} else {
							logger.info("Next button found but disabled - on last page");
							return false;
						}
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
		return false;
	}

	/**
	 * Click on row to open Display Computation dialog and extract values
	 */
	private LocationComputationData getLocationComputationData(WebElement row, int rowIndex) {
		LocationComputationData data = new LocationComputationData();

		try {
			// Find and click the Display Computation button/icon in the row
			WebElement displayCompBtn = null;

			String[] btnXpaths = {
				".//button[contains(@title,'Display') or contains(@aria-label,'Display') or contains(text(),'Display')]",
				".//button[contains(@class,'computation') or contains(@class,'display')]",
				".//td//button[1]",
				".//button[contains(@title,'View') or contains(@title,'Details')]",
				".//a[contains(@title,'Display') or contains(text(),'Display')]",
				".//span[contains(@class,'icon')]/parent::button"
			};

			for (String xpath : btnXpaths) {
				try {
					List<WebElement> btns = row.findElements(By.xpath(xpath));
					for (WebElement btn : btns) {
						if (btn.isDisplayed()) {
							displayCompBtn = btn;
							break;
						}
					}
					if (displayCompBtn != null) break;
				} catch (Exception e) {
					// Continue
				}
			}

			// If no button found, try clicking on the row itself
			if (displayCompBtn == null) {
				// Try to find any clickable element in the first cell
				try {
					List<WebElement> cells = row.findElements(By.xpath(".//td"));
					if (!cells.isEmpty()) {
						displayCompBtn = cells.get(0);
					}
				} catch (Exception e) {
					// Continue
				}
			}

			if (displayCompBtn != null) {
				// Scroll and click
				((JavascriptExecutor) driver).executeScript(
					"arguments[0].scrollIntoView({block: 'center'});", displayCompBtn);
				sleep(300);
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", displayCompBtn);
				sleep(1500);

				// Wait for dialog to appear
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
				try {
					wait.until(ExpectedConditions.or(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='dialog']")),
						ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'Dialog')]")),
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Dwelling')]"))
					));
				} catch (Exception e) {
					logger.warn("Dialog may not have appeared for row {}", rowIndex);
				}

				// Extract values from dialog
				data.setDwelling(extractValueFromDialog("Dwelling"));
				data.setAdditionalStructures(extractValueFromDialog("Additional Structures", "AS", "Addl Structures"));
				data.setBpp(extractValueFromDialog("BPP", "Business Personal Property"));
				data.setLossOfRents(extractValueFromDialog("Loss of Rents", "Loss Of Rents", "LOR"));
				data.setRate(extractValueFromDialog("Rate", "Suggested Rate"));
				data.setTaxes(extractValueFromDialog("Taxes", "Tax"));

				// Close dialog
				closeDialog();
				sleep(500);

				return data;
			}
		} catch (Exception e) {
			logger.error("Error getting computation data for row {}: {}", rowIndex, e.getMessage());
		}

		return null;
	}

	/**
	 * Extract value from Display Computation dialog
	 */
	private double extractValueFromDialog(String... fieldNames) {
		for (String fieldName : fieldNames) {
			String[] xpaths = {
				"//*[contains(text(),'" + fieldName + "')]/following-sibling::*[1]",
				"//*[contains(text(),'" + fieldName + "')]/../following-sibling::*[1]",
				"//label[contains(text(),'" + fieldName + "')]/following::input[1]",
				"//label[contains(text(),'" + fieldName + "')]/following::*[contains(@class,'value')][1]",
				"//*[contains(text(),'" + fieldName + "')]/parent::*//input",
				"//td[contains(text(),'" + fieldName + "')]/following-sibling::td[1]",
				"//th[contains(text(),'" + fieldName + "')]/following-sibling::td[1]"
			};

			for (String xpath : xpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement el : elements) {
						String text = el.getText().trim();
						if (text.isEmpty()) {
							text = el.getAttribute("value");
						}
						if (text != null && !text.isEmpty()) {
							double value = parseAmount(text);
							if (value > 0) {
								logger.debug("Found {} = {} from xpath: {}", fieldName, value, xpath);
								return value;
							}
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}
		}
		return 0;
	}

	/**
	 * Close any open dialog
	 */
	private void closeDialog() {
		try {
			// Try clicking close/X button
			String[] closeXpaths = {
				"//button[contains(@aria-label,'Close')]",
				"//button[contains(@class,'close')]",
				"//button[contains(text(),'Close')]",
				"//div[@role='dialog']//button[last()]",
				"//*[contains(@class,'DialogClose')]"
			};

			for (String xpath : closeXpaths) {
				try {
					List<WebElement> btns = driver.findElements(By.xpath(xpath));
					for (WebElement btn : btns) {
						if (btn.isDisplayed()) {
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
							sleep(300);
							return;
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}

			// Fallback: press Escape
			driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
		} catch (Exception e) {
			// Ignore
		}
	}

	/**
	 * Log Property Premium validation result to HTML report
	 */
	public void logPropertyPremiumValidationToReport(PropertyPremiumValidationResult result) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		if (result.isAllPassed()) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Property Premium Validation: ALL PASSED</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Property Premium Validation: FAILED</h3>");
		}

		html.append("<p style='color: #000000; font-weight: bold; font-size: 13px; margin: 10px 0; padding: 8px; background-color: #fff3cd; border-radius: 4px;'>Formula: TIV = Dwelling + AS + BPP + Loss of Rents | Property Premium = TIV / 100 × Rate</p>");

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; font-size: 12px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 6px;'>Loc #</th>");
		html.append("<th style='padding: 6px;'>Address</th>");
		html.append("<th style='padding: 6px;'>Dwelling</th>");
		html.append("<th style='padding: 6px;'>AS</th>");
		html.append("<th style='padding: 6px;'>BPP</th>");
		html.append("<th style='padding: 6px;'>LOR</th>");
		html.append("<th style='padding: 6px;'>TIV</th>");
		html.append("<th style='padding: 6px;'>Rate</th>");
		html.append("<th style='padding: 6px;'>Calculated</th>");
		html.append("<th style='padding: 6px;'>Displayed</th>");
		html.append("<th style='padding: 6px;'>Status</th>");
		html.append("</tr>");

		int locationNumber = 1;
		for (LocationPremiumValidation loc : result.getLocationValidations()) {
			String bgColor = loc.isMatch() ? "#d4edda" : "#f8d7da";
			String statusColor = loc.isMatch() ? "#28a745" : "#dc3545";

			html.append(String.format("<tr style='background-color: %s; border-bottom: 1px solid #dee2e6;'>", bgColor));
			html.append(String.format("<td style='padding: 6px; text-align: center; font-weight: bold;'>%d</td>", locationNumber));
			html.append(String.format("<td style='padding: 6px;'>%s</td>", truncateAddress(loc.getAddress())));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.0f</td>", loc.getDwelling()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.0f</td>", loc.getAdditionalStructures()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.0f</td>", loc.getBpp()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.0f</td>", loc.getLossOfRents()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.0f</td>", loc.getTiv()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>%.4f</td>", loc.getRate()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.2f</td>", loc.getCalculatedPremium()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.2f</td>", loc.getDisplayedPremium()));
			html.append(String.format("<td style='padding: 6px; text-align: center; font-weight: bold; color: %s;'>%s</td>",
				statusColor, loc.isMatch() ? "PASS" : "FAIL"));
			html.append("</tr>");
			locationNumber++;
		}

		html.append("</table>");
		html.append(String.format("<p style='margin-top: 10px; color: #000;'><strong>Summary:</strong> %d Passed, %d Failed out of %d locations</p>",
			result.getPassedCount(), result.getFailedCount(), result.getTotalCount()));
		html.append("</div>");

		logHtmlToReport(html.toString());
	}

	/**
	 * Truncate address for display
	 */
	private String truncateAddress(String address) {
		if (address == null) return "";
		return address.length() > 30 ? address.substring(0, 27) + "..." : address;
	}

	// Inner classes for Property Premium validation
	public static class PropertyPremiumValidationResult {
		private List<LocationPremiumValidation> locationValidations = new java.util.ArrayList<>();
		private boolean allPassed;
		private int passedCount;
		private int failedCount;
		private String error;

		public void addLocationValidation(LocationPremiumValidation v) { locationValidations.add(v); }
		public List<LocationPremiumValidation> getLocationValidations() { return locationValidations; }
		public boolean isAllPassed() { return allPassed; }
		public int getPassedCount() { return passedCount; }
		public int getFailedCount() { return failedCount; }
		public int getTotalCount() { return locationValidations.size(); }
		public String getError() { return error; }
		public void setError(String e) { this.error = e; }

		public void calculateOverallResult() {
			passedCount = 0;
			failedCount = 0;
			for (LocationPremiumValidation v : locationValidations) {
				if (v.isMatch()) passedCount++;
				else failedCount++;
			}
			allPassed = failedCount == 0 && passedCount > 0;
		}
	}

	public static class LocationPremiumValidation {
		private String address;
		private double dwelling;
		private double additionalStructures;
		private double bpp;
		private double lossOfRents;
		private double rate;
		private double taxes;
		private double tiv;
		private double calculatedPremium;
		private double displayedPremium;
		private boolean match;

		public String getAddress() { return address; }
		public void setAddress(String v) { this.address = v; }
		public double getDwelling() { return dwelling; }
		public void setDwelling(double v) { this.dwelling = v; }
		public double getAdditionalStructures() { return additionalStructures; }
		public void setAdditionalStructures(double v) { this.additionalStructures = v; }
		public double getBpp() { return bpp; }
		public void setBpp(double v) { this.bpp = v; }
		public double getLossOfRents() { return lossOfRents; }
		public void setLossOfRents(double v) { this.lossOfRents = v; }
		public double getRate() { return rate; }
		public void setRate(double v) { this.rate = v; }
		public double getTaxes() { return taxes; }
		public void setTaxes(double v) { this.taxes = v; }
		public double getTiv() { return tiv; }
		public void setTiv(double v) { this.tiv = v; }
		public double getCalculatedPremium() { return calculatedPremium; }
		public void setCalculatedPremium(double v) { this.calculatedPremium = v; }
		public double getDisplayedPremium() { return displayedPremium; }
		public void setDisplayedPremium(double v) { this.displayedPremium = v; }
		public boolean isMatch() { return match; }
		public void setMatch(boolean v) { this.match = v; }
	}

	public static class LocationComputationData {
		private String address;
		private double dwelling;
		private double additionalStructures;
		private double bpp;
		private double lossOfRents;
		private double rate;
		private double taxes;

		public String getAddress() { return address; }
		public void setAddress(String v) { this.address = v; }
		public double getDwelling() { return dwelling; }
		public void setDwelling(double v) { this.dwelling = v; }
		public double getAdditionalStructures() { return additionalStructures; }
		public void setAdditionalStructures(double v) { this.additionalStructures = v; }
		public double getBpp() { return bpp; }
		public void setBpp(double v) { this.bpp = v; }
		public double getLossOfRents() { return lossOfRents; }
		public void setLossOfRents(double v) { this.lossOfRents = v; }
		public double getRate() { return rate; }
		public void setRate(double v) { this.rate = v; }
		public double getTaxes() { return taxes; }
		public void setTaxes(double v) { this.taxes = v; }
	}

	/**
	 * Log premium validation result to HTML report
	 */
	public void logPremiumValidationToReport(PremiumValidationResult result) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		if (result.isAllValidationsPassed()) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Premium Calculation Validation: ALL PASSED</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Premium Calculation Validation: FAILED</h3>");
		}

		// Column Sums Section
		html.append("<h4 style='color: #007bff; margin: 15px 0 10px 0;'>Column Sums from Location Table</h4>");
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; margin-bottom: 15px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 8px; text-align: left;'>Column</th>");
		html.append("<th style='padding: 8px; text-align: right;'>Sum</th></tr>");
		html.append(String.format("<tr style='background-color: #f8f9fa;'><td style='padding: 8px;'>Property Premium</td><td style='padding: 8px; text-align: right;'>$%.2f</td></tr>", result.getPropertyPremiumSum()));
		html.append(String.format("<tr><td style='padding: 8px;'>GL Premium</td><td style='padding: 8px; text-align: right;'>$%.2f</td></tr>", result.getGlPremiumSum()));
		html.append(String.format("<tr style='background-color: #f8f9fa;'><td style='padding: 8px;'>Water/Sewer Premium</td><td style='padding: 8px; text-align: right;'>$%.2f</td></tr>", result.getWsPremiumSum()));
		html.append(String.format("<tr><td style='padding: 8px;'>Taxes</td><td style='padding: 8px; text-align: right;'>$%.2f</td></tr>", result.getTaxesSum()));
		html.append(String.format("<tr style='background-color: #f8f9fa;'><td style='padding: 8px;'>Fees</td><td style='padding: 8px; text-align: right;'>$%.2f</td></tr>", result.getFeesSum()));
		html.append("</table>");

		// Validations Section
		html.append("<h4 style='color: #007bff; margin: 15px 0 10px 0;'>Validation Results</h4>");
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 8px; text-align: left;'>Validation</th>");
		html.append("<th style='padding: 8px; text-align: left;'>Expected</th>");
		html.append("<th style='padding: 8px; text-align: left;'>Actual</th>");
		html.append("<th style='padding: 8px; text-align: center;'>Status</th></tr>");

		// Validation 1
		String status1 = result.isPremiumGLWSMatch() ? "PASS" : "FAIL";
		String color1 = result.isPremiumGLWSMatch() ? "#28a745" : "#dc3545";
		String bgColor1 = result.isPremiumGLWSMatch() ? "#d4edda" : "#f8d7da";
		html.append(String.format("<tr style='background-color: %s;'>", bgColor1));
		html.append("<td style='padding: 8px;'>Property + GL + WS = Premium (GL+WS)</td>");
		html.append(String.format("<td style='padding: 8px;'>$%.2f + $%.2f + $%.2f = $%.2f</td>",
			result.getPropertyPremiumSum(), result.getGlPremiumSum(), result.getWsPremiumSum(), result.getCalculatedPremiumGLWS()));
		html.append(String.format("<td style='padding: 8px;'>$%.2f</td>", result.getPremiumGLWSValue()));
		html.append(String.format("<td style='padding: 8px; text-align: center; font-weight: bold; color: %s;'>%s</td></tr>", color1, status1));

		// Validation 2
		String status2 = result.isTaxesMatch() ? "PASS" : "FAIL";
		String color2 = result.isTaxesMatch() ? "#28a745" : "#dc3545";
		String bgColor2 = result.isTaxesMatch() ? "#d4edda" : "#f8d7da";
		html.append(String.format("<tr style='background-color: %s;'>", bgColor2));
		html.append("<td style='padding: 8px;'>Taxes Column Sum = Taxes Value</td>");
		html.append(String.format("<td style='padding: 8px;'>$%.2f</td>", result.getTaxesSum()));
		html.append(String.format("<td style='padding: 8px;'>$%.2f</td>", result.getTaxesValue()));
		html.append(String.format("<td style='padding: 8px; text-align: center; font-weight: bold; color: %s;'>%s</td></tr>", color2, status2));

		// Validation 3
		String status3 = result.isFeesMatch() ? "PASS" : "FAIL";
		String color3 = result.isFeesMatch() ? "#28a745" : "#dc3545";
		String bgColor3 = result.isFeesMatch() ? "#d4edda" : "#f8d7da";
		html.append(String.format("<tr style='background-color: %s;'>", bgColor3));
		html.append("<td style='padding: 8px;'>Fees Column Sum = Total Fees Value</td>");
		html.append(String.format("<td style='padding: 8px;'>$%.2f</td>", result.getFeesSum()));
		html.append(String.format("<td style='padding: 8px;'>$%.2f</td>", result.getTotalFeesValue()));
		html.append(String.format("<td style='padding: 8px; text-align: center; font-weight: bold; color: %s;'>%s</td></tr>", color3, status3));

		// Validation 4
		String status4 = result.isGrandTotalMatch() ? "PASS" : "FAIL";
		String color4 = result.isGrandTotalMatch() ? "#28a745" : "#dc3545";
		String bgColor4 = result.isGrandTotalMatch() ? "#d4edda" : "#f8d7da";
		html.append(String.format("<tr style='background-color: %s;'>", bgColor4));
		html.append("<td style='padding: 8px;'>Premium + Taxes + Fees = Grand Total</td>");
		html.append(String.format("<td style='padding: 8px;'>$%.2f + $%.2f + $%.2f = $%.2f</td>",
			result.getPremiumGLWSValue(), result.getTaxesValue(), result.getTotalFeesValue(), result.getCalculatedGrandTotal()));
		html.append(String.format("<td style='padding: 8px;'>$%.2f</td>", result.getGrandTotalValue()));
		html.append(String.format("<td style='padding: 8px; text-align: center; font-weight: bold; color: %s;'>%s</td></tr>", color4, status4));

		html.append("</table></div>");

		logHtmlToReport(html.toString());
	}

	/**
	 * Inner class to hold premium validation results
	 */
	public static class PremiumValidationResult {
		private double propertyPremiumSum;
		private double glPremiumSum;
		private double wsPremiumSum;
		private double taxesSum;
		private double feesSum;
		private double premiumGLWSValue;
		private double taxesValue;
		private double totalFeesValue;
		private double grandTotalValue;
		private double calculatedPremiumGLWS;
		private double calculatedGrandTotal;
		private boolean premiumGLWSMatch;
		private boolean taxesMatch;
		private boolean feesMatch;
		private boolean grandTotalMatch;
		private boolean allValidationsPassed;
		private String error;

		// Getters and Setters
		public double getPropertyPremiumSum() { return propertyPremiumSum; }
		public void setPropertyPremiumSum(double v) { this.propertyPremiumSum = v; }
		public double getGlPremiumSum() { return glPremiumSum; }
		public void setGlPremiumSum(double v) { this.glPremiumSum = v; }
		public double getWsPremiumSum() { return wsPremiumSum; }
		public void setWsPremiumSum(double v) { this.wsPremiumSum = v; }
		public double getTaxesSum() { return taxesSum; }
		public void setTaxesSum(double v) { this.taxesSum = v; }
		public double getFeesSum() { return feesSum; }
		public void setFeesSum(double v) { this.feesSum = v; }
		public double getPremiumGLWSValue() { return premiumGLWSValue; }
		public void setPremiumGLWSValue(double v) { this.premiumGLWSValue = v; }
		public double getTaxesValue() { return taxesValue; }
		public void setTaxesValue(double v) { this.taxesValue = v; }
		public double getTotalFeesValue() { return totalFeesValue; }
		public void setTotalFeesValue(double v) { this.totalFeesValue = v; }
		public double getGrandTotalValue() { return grandTotalValue; }
		public void setGrandTotalValue(double v) { this.grandTotalValue = v; }
		public double getCalculatedPremiumGLWS() { return calculatedPremiumGLWS; }
		public void setCalculatedPremiumGLWS(double v) { this.calculatedPremiumGLWS = v; }
		public double getCalculatedGrandTotal() { return calculatedGrandTotal; }
		public void setCalculatedGrandTotal(double v) { this.calculatedGrandTotal = v; }
		public boolean isPremiumGLWSMatch() { return premiumGLWSMatch; }
		public void setPremiumGLWSMatch(boolean v) { this.premiumGLWSMatch = v; }
		public boolean isTaxesMatch() { return taxesMatch; }
		public void setTaxesMatch(boolean v) { this.taxesMatch = v; }
		public boolean isFeesMatch() { return feesMatch; }
		public void setFeesMatch(boolean v) { this.feesMatch = v; }
		public boolean isGrandTotalMatch() { return grandTotalMatch; }
		public void setGrandTotalMatch(boolean v) { this.grandTotalMatch = v; }
		public boolean isAllValidationsPassed() { return allValidationsPassed; }
		public void setAllValidationsPassed(boolean v) { this.allValidationsPassed = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
	}

	/**
	 * Take screenshot and log to report
	 */
	public void captureEndorsementScreenshot(String description) {
		captureScreenshotToReport("Create Premium Endorsement - " + description);
	}

	// ==================== Add Location Methods ====================

	/**
	 * Add locations from a list of location data maps
	 * Uses inherited addLocation method from CreateQuotePage (same as EditQuotePage)
	 */
	public int addLocationsFromSheet(List<Map<String, String>> locations) {
		logger.info("Adding {} locations to Premium Endorsement", locations.size());
		int addedCount = 0;
		int rejectedDueToCA = 0;

		for (int i = 0; i < locations.size(); i++) {
			Map<String, String> location = locations.get(i);
			String address = location.getOrDefault("Address", "");

			if (address.isEmpty()) {
				logger.warn("Skipping location {} - no address provided", i + 1);
				continue;
			}

			String paymentPlan = location.getOrDefault("PaymentPlan", "PAID_IN_FULL");
			logger.info("Adding location {}/{}: {}", (i + 1), locations.size(), address);

			try {
				// Use inherited addLocation method from CreateQuotePage (same as EditQuotePage)
				addLocation(location, paymentPlan, null);
				sleep(2000);

				// Check for Arch California error (same as EditQuotePage)
				if (isArchCaliforniaErrorDisplayed()) {
					String errorMsg = getArchCaliforniaErrorMessage();
					logger.info("EXPECTED: Arch California error - Location rejected: {} - {}", address, errorMsg);
					captureScreenshotToReport("Arch California Error - " + address);
					rejectedDueToCA++;
					dismissErrorDialog();
				} else {
					addedCount++;
					logger.info("Successfully added location: {}", address);
				}
			} catch (Exception e) {
				logger.error("Failed to add location {}: {}", address, e.getMessage());
				if (isArchCaliforniaErrorDisplayed()) {
					captureScreenshotToReport("Arch California Error - " + address);
					rejectedDueToCA++;
					dismissErrorDialog();
				}
			}
		}

		captureScreenshotToReport("After Adding " + addedCount + " Locations");
		logger.info("Added {}/{} locations (rejected due to Arch+CA: {})", addedCount, locations.size(), rejectedDueToCA);
		return addedCount;
	}

	/**
	 * Find the location dialog element
	 */
	private WebElement findLocationDialog() {
		String[] dialogIds = {
			"unit-skill-dialog",
			"location-dialog",
			"add-location-dialog"
		};

		for (String id : dialogIds) {
			try {
				WebElement dialog = driver.findElement(By.id(id));
				if (dialog.isDisplayed()) {
					logger.info("Found location dialog with id: {}", id);
					return dialog;
				}
			} catch (Exception e) {
				// Continue to next
			}
		}

		// Try xpath patterns
		String[] xpaths = {
			"//div[contains(@class,'DialogContent')]",
			"//div[@role='dialog']",
			"//div[contains(@class,'modal')]//form"
		};

		for (String xpath : xpaths) {
			try {
				List<WebElement> dialogs = driver.findElements(By.xpath(xpath));
				for (WebElement dialog : dialogs) {
					if (dialog.isDisplayed()) {
						logger.info("Found location dialog with xpath: {}", xpath);
						return dialog;
					}
				}
			} catch (Exception e) {
				// Continue to next
			}
		}

		return null;
	}

	/**
	 * Fill location dialog with data - uses same design as CreateQuotePage
	 * Searches within dialog context using relative XPaths
	 * Handles all columns from CreateEndorsement sheet
	 */
	private void fillLocationDialog(WebElement dialog, Map<String, String> locationData) {
		// Log all location data being entered
		logger.info("Filling location dialog with data: {}", locationData);

		// Address with auto-complete (same as CreateQuotePage)
		String address = locationData.getOrDefault("Address", "");
		if (!address.isEmpty()) {
			enterAddressWithAutoComplete(dialog, address);
			sleep(1000);
		}

		// City, State, ZipCode - enter if not auto-filled from address
		String city = locationData.getOrDefault("City", "");
		if (!city.isEmpty()) {
			enterFieldInDialog(dialog, "City", city);
		}

		String state = locationData.getOrDefault("State", "");
		if (!state.isEmpty()) {
			enterFieldInDialog(dialog, "State", state);
		}

		String zipCode = locationData.getOrDefault("ZipCode", "");
		if (!zipCode.isEmpty()) {
			enterFieldInDialog(dialog, "Zip", zipCode);
		}

		// Municipality (dropdown)
		String municipality = locationData.getOrDefault("Municipality", "");
		if (!municipality.isEmpty()) {
			selectMunicipalityInDialog(dialog, municipality);
		}

		// Select Property Type (RS = Residential, RM = Multi-Family)
		String propertyType = locationData.getOrDefault("PropertyType", "RS");
		selectPropertyTypeInDialog(dialog, propertyType);
		sleep(500);

		// Enter Units (# of Units field)
		String units = locationData.getOrDefault("Units", "");
		if (!units.isEmpty()) {
			enterFieldInDialog(dialog, "Units", units);
		}

		// Year Built
		String yearBuilt = locationData.getOrDefault("YearBuilt", "");
		if (!yearBuilt.isEmpty()) {
			enterFieldInDialog(dialog, "Year Built", yearBuilt);
		}

		// Roof Year
		String roofYear = locationData.getOrDefault("RoofYear", "");
		if (!roofYear.isEmpty()) {
			enterFieldInDialog(dialog, "Roof", roofYear);
		}

		// Stories
		String stories = locationData.getOrDefault("Stories", "");
		if (!stories.isEmpty()) {
			enterFieldInDialog(dialog, "Stories", stories);
		}

		// Sq. Ft.
		String sqFt = locationData.getOrDefault("SqFt", "");
		if (!sqFt.isEmpty()) {
			enterFieldInDialog(dialog, "Sq. Ft", sqFt);
		}

		// Coverage A
		String coverageA = locationData.getOrDefault("CoverageA", "");
		if (!coverageA.isEmpty()) {
			enterFieldInDialog(dialog, "Coverage A", coverageA);
		}

		// Coverage B
		String coverageB = locationData.getOrDefault("CoverageB", "");
		if (!coverageB.isEmpty()) {
			enterFieldInDialog(dialog, "Coverage B", coverageB);
		}

		// Scroll down in dialog
		scrollDialogDown(dialog);
		sleep(500);

		// Coverage C
		String coverageC = locationData.getOrDefault("CoverageC", "");
		if (!coverageC.isEmpty()) {
			enterFieldInDialog(dialog, "Coverage C", coverageC);
		}

		// Loss of Rents
		String lossOfRents = locationData.getOrDefault("LossOfRents", "");
		if (!lossOfRents.isEmpty()) {
			enterFieldInDialog(dialog, "Loss of Rents", lossOfRents);
		}

		// Scroll down again
		scrollDialogDown(dialog);
		sleep(300);

		// Suggested Rate
		String suggestedRate = locationData.getOrDefault("SuggestedRate", "");
		if (!suggestedRate.isEmpty()) {
			enterFieldInDialog(dialog, "Suggested Rate", suggestedRate);
		}

		// AOP Deductible
		String aop = locationData.getOrDefault("AOP", "");
		if (!aop.isEmpty()) {
			enterFieldInDialog(dialog, "AOP", aop);
		}

		// Wind Deductible
		String windDeductible = locationData.getOrDefault("WindDeductible", "");
		if (!windDeductible.isEmpty()) {
			enterFieldInDialog(dialog, "Wind", windDeductible);
		}

		// RCV/ACV selection
		String rcvAcv = locationData.getOrDefault("RCV_ACV", "");
		if (!rcvAcv.isEmpty()) {
			selectRcvAcvInDialog(dialog, rcvAcv);
		}

		// Exclude Wind
		String excludeWind = locationData.getOrDefault("ExcludeWind", "");
		if (!excludeWind.isEmpty() && excludeWind.equalsIgnoreCase("Yes")) {
			selectExcludeWindInDialog(dialog);
		}

		// Scroll to bottom for payment plan and mortgagee
		scrollDialogDown(dialog);
		sleep(300);
	}

	/**
	 * Select Municipality in dialog (dropdown)
	 */
	private void selectMunicipalityInDialog(WebElement dialog, String municipality) {
		logger.info("Selecting Municipality: {}", municipality);

		String[] dropdownXpaths = {
			".//label[contains(text(),'Municipality')]/following::button[1]",
			".//label[contains(text(),'municipality')]/following::button[1]",
			".//*[contains(text(),'Municipality')]/parent::div//button"
		};

		for (String xpath : dropdownXpaths) {
			try {
				List<WebElement> buttons = dialog.findElements(By.xpath(xpath));
				for (WebElement btn : buttons) {
					if (btn.isDisplayed() && btn.isEnabled()) {
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", btn);
						sleep(300);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
						sleep(1000);

						// Select option from dropdown - search for the option
						String[] optionXpaths = {
							"//div[@role='option'][contains(text(),'" + municipality + "')]",
							"//div[contains(@class,'option')][contains(text(),'" + municipality + "')]",
							"//*[contains(text(),'" + municipality + "')][@role='option']"
						};

						for (String optXpath : optionXpaths) {
							try {
								List<WebElement> options = driver.findElements(By.xpath(optXpath));
								for (WebElement opt : options) {
									if (opt.isDisplayed()) {
										((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
										logger.info("Municipality selected: {}", municipality);
										return;
									}
								}
							} catch (Exception e2) {
								// Continue
							}
						}
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
	}

	/**
	 * Select RCV or ACV in dialog
	 */
	private void selectRcvAcvInDialog(WebElement dialog, String rcvAcv) {
		logger.info("Selecting RCV/ACV: {}", rcvAcv);

		String[] xpaths;
		if (rcvAcv.toUpperCase().contains("RCV")) {
			xpaths = new String[] {
				".//button[contains(text(),'RCV')]",
				".//label[contains(text(),'RCV')]",
				".//*[@role='radio' and contains(.,'RCV')]"
			};
		} else {
			xpaths = new String[] {
				".//button[contains(text(),'ACV')]",
				".//label[contains(text(),'ACV')]",
				".//*[@role='radio' and contains(.,'ACV')]"
			};
		}

		for (String xpath : xpaths) {
			try {
				List<WebElement> elements = dialog.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					if (el.isDisplayed()) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
						logger.info("Selected RCV/ACV: {}", rcvAcv);
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
	}

	/**
	 * Select Exclude Wind checkbox in dialog
	 */
	private void selectExcludeWindInDialog(WebElement dialog) {
		logger.info("Selecting Exclude Wind");

		String[] xpaths = {
			".//label[contains(text(),'Exclude Wind')]//input[@type='checkbox']",
			".//input[contains(@id,'wind') or contains(@name,'wind')][@type='checkbox']",
			".//*[contains(text(),'Exclude Wind')]/following::input[@type='checkbox'][1]"
		};

		for (String xpath : xpaths) {
			try {
				List<WebElement> checkboxes = dialog.findElements(By.xpath(xpath));
				for (WebElement cb : checkboxes) {
					if (cb.isDisplayed() && !cb.isSelected()) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
						logger.info("Selected Exclude Wind checkbox");
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
	}

	/**
	 * Enter address with auto-complete handling (same design as CreateQuotePage)
	 */
	private void enterAddressWithAutoComplete(WebElement dialog, String address) {
		logger.info("Entering address with auto-complete: {}", address);

		// Find the Physical address input field within dialog
		String[] xpaths = {
			".//label[contains(text(),'Physical address')]/following::input[1]",
			".//label[contains(text(),'Physical Address')]/following::input[1]",
			".//label[contains(text(),'Address')]/following::input[1]",
			".//input[contains(@placeholder,'address') or contains(@placeholder,'Address')]",
			".//input[contains(@name,'address')]"
		};

		WebElement addressInput = null;
		for (String xpath : xpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed() && input.isEnabled()) {
						addressInput = input;
						break;
					}
				}
				if (addressInput != null) break;
			} catch (Exception e) {
				// Continue
			}
		}

		if (addressInput == null) {
			logger.warn("Could not find address input field");
			return;
		}

		// Scroll to the input
		((JavascriptExecutor) driver).executeScript(
			"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", addressInput);
		sleep(300);

		// Clear and type the address
		clearInputField(addressInput);
		sleep(100);
		addressInput.sendKeys(address);
		sleep(1500); // Wait for auto-complete suggestions to appear

		// Try to find and click the first suggestion in the auto-complete dropdown
		try {
			String[] suggestionXpaths = {
				"//div[contains(@class,'pac-container')]//div[contains(@class,'pac-item')][1]",
				"//div[contains(@class,'autocomplete')]//div[contains(@class,'suggestion')][1]",
				"//ul[contains(@class,'suggestions')]//li[1]",
				"//div[contains(@class,'dropdown')]//div[contains(text(),'" + address.split(",")[0] + "')]",
				"(//div[contains(@class,'pac-item')])[1]",
				"//div[@class='pac-container']//div[@class='pac-item'][1]"
			};

			WebElement suggestion = null;
			for (String xpath : suggestionXpaths) {
				try {
					List<WebElement> suggestions = driver.findElements(By.xpath(xpath));
					for (WebElement s : suggestions) {
						if (s.isDisplayed()) {
							suggestion = s;
							break;
						}
					}
					if (suggestion != null) break;
				} catch (Exception e) {
					// Continue
				}
			}

			if (suggestion != null) {
				logger.info("Found auto-complete suggestion, clicking it");
				sleep(500);
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", suggestion);
				sleep(1000);
				logger.info("Auto-complete suggestion selected for: {}", address);
			} else {
				// Try pressing down arrow and enter to select first suggestion
				logger.info("No suggestion element found, trying keyboard navigation");
				addressInput.sendKeys(Keys.ARROW_DOWN);
				sleep(300);
				addressInput.sendKeys(Keys.ENTER);
				sleep(1000);
			}
		} catch (Exception e) {
			logger.warn("Auto-complete selection failed: {}", e.getMessage());
		}
	}

	/**
	 * Enter value in a field within the dialog context (same design as CreateQuotePage)
	 */
	private void enterFieldInDialog(WebElement dialog, String fieldLabel, String value) {
		if (value == null || value.isEmpty()) return;

		// Search within dialog using relative XPaths
		String[] xpaths = {
			".//label[contains(text(),'" + fieldLabel + "')]/following::input[1]",
			".//label[contains(text(),'" + fieldLabel + "')]/..//input",
			".//label[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + fieldLabel.toLowerCase() + "')]/following::input[1]",
			".//label[text()='" + fieldLabel + "']/following::input[1]",
			".//*[contains(text(),'" + fieldLabel + "')]/following::input[1]",
			".//input[contains(@placeholder,'" + fieldLabel + "')]"
		};

		for (String xpath : xpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed() && input.isEnabled()) {
						// Scroll element into view
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
						sleep(300);

						// Clear and enter value
						clearInputField(input);
						sleep(100);
						input.sendKeys(value);
						sleep(200);

						logger.info("Entered '{}' in field '{}'", value, fieldLabel);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}
		logger.warn("Could not find field: {}", fieldLabel);
	}

	/**
	 * Select Property Type in dialog (RS = Residential, RM = Multi-Family)
	 */
	private void selectPropertyTypeInDialog(WebElement dialog, String propertyType) {
		logger.info("Selecting Property Type: {}", propertyType);

		boolean isMultiFamily = "RM".equalsIgnoreCase(propertyType) ||
			propertyType.toLowerCase().contains("multi") ||
			propertyType.toLowerCase().contains("rm");

		String[] xpaths;
		if (isMultiFamily) {
			xpaths = new String[] {
				".//button[text()='RM']",
				".//button[contains(text(),'RM')]",
				".//button[contains(text(),'Multi')]",
				".//label[contains(text(),'RM')]",
				".//*[@role='radio' and contains(.,'RM')]"
			};
		} else {
			xpaths = new String[] {
				".//button[text()='RS']",
				".//button[contains(text(),'RS')]",
				".//label[contains(text(),'Residential') and not(contains(text(),'Multi'))]",
				".//*[@role='radio' and contains(.,'RS')]"
			};
		}

		for (String xpath : xpaths) {
			try {
				List<WebElement> elements = dialog.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					if (el.isDisplayed()) {
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", el);
						sleep(200);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
						logger.info("Selected Property Type: {}", isMultiFamily ? "RM" : "RS");
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
	}

	/**
	 * Scroll down within dialog
	 */
	private void scrollDialogDown(WebElement dialog) {
		try {
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollTop += 300;", dialog);
		} catch (Exception e) {
			// Try scrolling the page instead
			((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 300);");
		}
	}

	/**
	 * Select payment plan in dialog
	 */
	private void selectPaymentPlanInDialog(String paymentPlan) {
		String normalizedPlan = paymentPlan.toLowerCase().replace("_", " ").trim();

		String[] planXpaths = {
			"//label[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + normalizedPlan + "')]//input[@type='radio']",
			"//input[@type='radio'][following-sibling::*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + normalizedPlan + "')]]",
			"//div[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + normalizedPlan + "')]",
			"//*[contains(@id,'payment') or contains(@name,'payment')]//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + normalizedPlan + "')]"
		};

		for (String xpath : planXpaths) {
			try {
				List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement element : elements) {
					if (element.isDisplayed()) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
						logger.info("Selected payment plan: {}", paymentPlan);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Default: try to select "Paid In Full" if plan not found
		logger.warn("Could not find payment plan '{}', trying default", paymentPlan);
	}

	/**
	 * Click Add location button in location dialog
	 */
	private void clickAddButtonInDialog() {
		String[] buttonXpaths = {
			"//button[normalize-space()='Add location']",
			"//button[contains(text(),'Add location')]",
			"//button[contains(text(),'Add Location')]",
			"//button[text()='Add location']",
			"//button[text()='Add Location']",
			"//div[@role='dialog']//button[contains(text(),'Add')]",
			"//button[contains(@class,'primary') and contains(text(),'Add')]",
			"//button[@type='submit']"
		};

		for (String xpath : buttonXpaths) {
			try {
				List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				for (WebElement button : buttons) {
					if (button.isDisplayed() && button.isEnabled()) {
						String buttonText = button.getText();
						if (!buttonText.toLowerCase().contains("cancel") && !buttonText.toLowerCase().contains("close")) {
							// Scroll to button first
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].scrollIntoView({block: 'center'});", button);
							sleep(300);
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
							logger.info("Clicked Add location button: {}", buttonText);
							return;
						}
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}
		logger.warn("Could not find Add location button in dialog");
	}

	/**
	 * Check if error dialog is displayed
	 */
	private boolean isErrorDisplayed() {
		String[] errorXpaths = {
			"//*[contains(@class,'error') or contains(@class,'Error')]",
			"//*[contains(text(),'Error') or contains(text(),'error')]",
			"//div[contains(@class,'alert') and contains(@class,'danger')]"
		};

		for (String xpath : errorXpaths) {
			try {
				List<WebElement> errors = driver.findElements(By.xpath(xpath));
				for (WebElement error : errors) {
					if (error.isDisplayed() && !error.getText().isEmpty()) {
						return true;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
		return false;
	}

	/**
	 * Dismiss error dialog
	 */
	private void dismissErrorDialog() {
		try {
			driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
			sleep(500);
		} catch (Exception ignored) {}
	}

	/**
	 * Log HTML to report
	 */
	public void logEndorsementValuesToReport(Map<String, String> values) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #007bff; color: #000000;'>");
		html.append("<h3 style='color: #007bff; margin-top: 0;'>Create Premium Endorsement - Form Values</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 40%;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 60%;'>Value</th>");
		html.append("</tr>");

		for (Map.Entry<String, String> entry : values.entrySet()) {
			html.append("<tr style='background-color: #ffffff; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; font-weight: bold; color: #000000;'>").append(entry.getKey()).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(entry.getValue()).append("</td>");
			html.append("</tr>");
		}
		html.append("</table></div>");

		logHtmlToReport(html.toString());
	}
}
