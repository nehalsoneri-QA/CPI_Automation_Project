package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base Page Object for Premium Endorsement Pages (Create and Edit)
 * Contains all common methods that are identical for both Create and Edit Premium Endorsement
 * Uses dynamic locators based on prefix ("create-endorsement" or "edit-endorsement")
 */
public abstract class BasePremiumEndorsementPage extends CreateQuotePage {

	// Prefix for element IDs - to be set by subclass ("create-endorsement" or "edit-endorsement")
	protected final String elementPrefix;

	// Page type for logging
	protected final String pageType;

	public BasePremiumEndorsementPage(WebDriver driver, String elementPrefix, String pageType) {
		super(driver);
		this.elementPrefix = elementPrefix;
		this.pageType = pageType;
		logger.info("{} Page initialized with prefix: {}", pageType, elementPrefix);
	}

	// ==================== Dynamic Element Locators ====================

	protected WebElement getAgentDropdown() {
		return driver.findElement(By.id(elementPrefix + "-agent-select"));
	}

	protected WebElement getInsuredDropdown() {
		return driver.findElement(By.id(elementPrefix + "-insured-select"));
	}

	protected WebElement getCarrierDropdown() {
		return driver.findElement(By.id(elementPrefix + "-carrier-select"));
	}

	protected WebElement getEffectiveDatePicker() {
		return driver.findElement(By.id(elementPrefix + "-effective-date-picker"));
	}

	protected WebElement getExpirationDatePicker() {
		return driver.findElement(By.id(elementPrefix + "-expiration-date-picker"));
	}

	protected WebElement getEndorsementEffectiveDatePicker() {
		return driver.findElement(By.id(elementPrefix + "-endorsement-effective-date-picker"));
	}

	protected WebElement getGeneralLiabilityAmountInput() {
		return driver.findElement(By.id(elementPrefix + "-general-liability-amount-input"));
	}

	protected WebElement getWaterSewerBackupAmountInput() {
		return driver.findElement(By.id(elementPrefix + "-water-sewer-backup-amount-input"));
	}

	protected WebElement getSuggestedStateDropdown() {
		return driver.findElement(By.id(elementPrefix + "-suggested-state-select"));
	}

	protected WebElement getSubmitButton() {
		return driver.findElement(By.id(elementPrefix + "-submit-button"));
	}

	// ==================== Page Load Methods ====================

	/**
	 * Verify if page is loaded
	 */
	public boolean isPageLoaded() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

			// Refresh page to ensure SPA content loads properly
			driver.navigate().refresh();
			sleep(3000);

			// Check for form elements
			wait.until(ExpectedConditions.or(
				ExpectedConditions.presenceOfElementLocated(By.id(elementPrefix + "-agent-select")),
				ExpectedConditions.presenceOfElementLocated(By.id(elementPrefix + "-carrier-select"))
			));

			logger.info("{} page loaded successfully", pageType);
			return true;
		} catch (Exception e) {
			logger.error("{} page not loaded: {}", pageType, e.getMessage());
			return false;
		}
	}

	/**
	 * Wait for page data to load
	 */
	public void waitForPageDataLoad() {
		try {
			sleep(3000);
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
			wait.until(ExpectedConditions.elementToBeClickable(By.id(elementPrefix + "-submit-button")));
			logger.info("{} page data loaded", pageType);
		} catch (Exception e) {
			logger.warn("{} page data load timeout: {}", pageType, e.getMessage());
		}
	}

	// ==================== Screenshot Methods ====================

	/**
	 * Capture screenshot with endorsement context
	 */
	public void captureEndorsementScreenshot(String description) {
		captureScreenshotToReport(pageType + " - " + description);
	}

	/**
	 * Log HTML content to report
	 */
	public void logHtmlToReport(String html) {
		logToReport(html);
	}

	// ==================== Dropdown Selection Methods ====================

	/**
	 * Get selected agent value
	 */
	public String getSelectedAgent() {
		try {
			WebElement dropdown = getAgentDropdown();
			return dropdown.getText().trim();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get selected insured value
	 */
	public String getSelectedInsured() {
		try {
			WebElement dropdown = getInsuredDropdown();
			return dropdown.getText().trim();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get selected carrier value
	 */
	public String getSelectedCarrier() {
		try {
			WebElement dropdown = getCarrierDropdown();
			return dropdown.getText().trim();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get selected state value
	 */
	public String getSelectedState() {
		try {
			WebElement dropdown = getSuggestedStateDropdown();
			return dropdown.getText().trim();
		} catch (Exception e) {
			return "";
		}
	}

	// ==================== Date Methods ====================

	/**
	 * Get effective date value
	 */
	public String getEffectiveDate() {
		try {
			return getDateFromDisabledField(getEffectiveDatePicker(), elementPrefix + "-effective-date-picker");
		} catch (Exception e) {
			logger.warn("Error getting effective date: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Get expiration date value
	 */
	public String getExpirationDate() {
		try {
			return getDateFromDisabledField(getExpirationDatePicker(), elementPrefix + "-expiration-date-picker");
		} catch (Exception e) {
			logger.warn("Error getting expiration date: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Get endorsement effective date value
	 */
	public String getEndorsementEffectiveDate() {
		try {
			return getDateFromDisabledField(getEndorsementEffectiveDatePicker(), elementPrefix + "-endorsement-effective-date-picker");
		} catch (Exception e) {
			logger.warn("Error getting endorsement effective date: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Helper method to get date value from disabled field
	 */
	protected String getDateFromDisabledField(WebElement element, String elementId) {
		String value = "";

		// Approach 1: Try aria-valuetext
		try {
			value = element.getAttribute("aria-valuetext");
			if (isValidDateValue(value)) {
				logger.info("Got date from aria-valuetext: {}", value);
				return value;
			}
		} catch (Exception e) {}

		// Approach 2: Try data-value attribute
		try {
			value = element.getAttribute("data-value");
			if (isValidDateValue(value)) {
				logger.info("Got date from data-value: {}", value);
				return value;
			}
		} catch (Exception e) {}

		// Approach 3: Try getAttribute("value")
		try {
			value = element.getAttribute("value");
			if (isValidDateValue(value)) {
				logger.info("Got date from value attribute: {}", value);
				return value;
			}
		} catch (Exception e) {}

		// Approach 4: Try getText()
		try {
			value = element.getText();
			if (isValidDateValue(value)) {
				logger.info("Got date from getText(): {}", value);
				return value;
			}
		} catch (Exception e) {}

		// Approach 5: Try JavaScript
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"return document.getElementById('" + elementId + "').value;");
			if (isValidDateValue(value)) {
				logger.info("Got date from JavaScript: {}", value);
				return value;
			}
		} catch (Exception e) {}

		// Approach 6: Try finding input element inside
		try {
			WebElement input = element.findElement(By.tagName("input"));
			value = input.getAttribute("value");
			if (isValidDateValue(value)) {
				logger.info("Got date from inner input: {}", value);
				return value;
			}
		} catch (Exception e) {}

		// Approach 7: Try segmented date picker
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
		} catch (Exception e) {}

		// Approach 8: Try spans inside element
		try {
			List<WebElement> spans = element.findElements(By.tagName("span"));
			StringBuilder dateText = new StringBuilder();
			for (WebElement span : spans) {
				String text = span.getText().trim();
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
		} catch (Exception e) {}

		// Approach 9: JavaScript comprehensive
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"var el = document.getElementById('" + elementId + "');" +
				"if(!el) return '';" +
				"if(el.getAttribute('aria-valuetext')) return el.getAttribute('aria-valuetext');" +
				"if(el.getAttribute('data-value')) return el.getAttribute('data-value');" +
				"if(el.value && el.value !== '--' && !/^0[-\\/]0[-\\/]/.test(el.value)) return el.value;" +
				"var segments = el.querySelectorAll('[role=\"spinbutton\"], input[data-segment], [data-placeholder]');" +
				"if(segments.length >= 3) {" +
				"  var parts = [];" +
				"  segments.forEach(function(s) {" +
				"    var v = s.getAttribute('aria-valuenow') || s.value || s.textContent || '';" +
				"    if(v && v !== '0' && v.trim()) parts.push(v.trim());" +
				"  });" +
				"  if(parts.length >= 3) return parts.join('/');" +
				"}" +
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
	 * Check if date value is valid
	 */
	protected boolean isValidDateValue(String value) {
		if (value == null || value.isEmpty()) return false;
		if (value.equals("--") || value.equals("null") || value.equals("Select")) return false;
		if (value.matches("^0[-/]0[-/].*") || value.matches(".*[-/]0[-/]0$") || value.equals("0-0-") || value.equals("0/0/")) return false;
		if (value.matches("^[-/]+$")) return false;
		if (!value.matches(".*\\d+.*")) return false;
		return true;
	}

	/**
	 * Change Endorsement Effective Date to 6 months from current date
	 * Handles the Confirm Date Change dialog by clicking Confirm Changes button
	 */
	public boolean changeEndorsementDateTo6MonthsLater() {
		logger.info("=== Changing Endorsement Effective Date to 6 months later ===");

		try {
			LocalDate currentDate = LocalDate.now();
			LocalDate futureDate = currentDate.plusMonths(6);
			String formattedDate = futureDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

			logger.info("Current date: {}, New date (6 months later): {}", currentDate, formattedDate);

			// Find the date field
			WebElement dateField = null;
			String[] dateFieldXpaths = {
				"//*[@id='" + elementPrefix + "-endorsement-effective-date-picker']",
				"//input[contains(@id,'endorsement-effective-date')]",
				"//div[contains(@id,'endorsement-effective-date')]//input",
				"//*[contains(@id,'endorsement-effective-date')]"
			};

			for (String xpath : dateFieldXpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement el : elements) {
						if (el.isDisplayed()) {
							dateField = el;
							logger.info("Found date field with xpath: {}", xpath);
							break;
						}
					}
					if (dateField != null) break;
				} catch (Exception e) {}
			}

			if (dateField == null) {
				logger.error("Endorsement effective date field not found");
				captureEndorsementScreenshot("Date Field Not Found");
				return false;
			}

			// Scroll to the date field
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", dateField);
			sleep(500);

			captureEndorsementScreenshot("Before Date Change");

			// Click to focus
			try {
				dateField.click();
				sleep(300);
			} catch (Exception e) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateField);
				sleep(300);
			}

			// Clear existing value
			try {
				dateField.sendKeys(Keys.CONTROL + "a");
				sleep(100);
				dateField.sendKeys(Keys.DELETE);
				sleep(100);
			} catch (Exception e) {
				((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", dateField);
			}

			// Enter the new date
			dateField.sendKeys(formattedDate);
			sleep(500);

			// Press Tab to trigger the change
			dateField.sendKeys(Keys.TAB);
			sleep(1000);

			logger.info("Entered new endorsement effective date: {}", formattedDate);
			captureEndorsementScreenshot("After Date Entry");

			// Handle the Confirm Date Change dialog
			boolean dialogHandled = handleConfirmDateChangeDialog();

			if (dialogHandled) {
				logger.info("Successfully changed endorsement date to: {}", formattedDate);
				captureEndorsementScreenshot("Date Change Confirmed");
				return true;
			} else {
				logger.warn("Confirm Date Change dialog not found or not handled");
				return true;
			}

		} catch (Exception e) {
			logger.error("Error changing endorsement effective date: {}", e.getMessage());
			captureEndorsementScreenshot("Date Change Error");
			return false;
		}
	}

	/**
	 * Handle the Confirm Date Change dialog
	 */
	public boolean handleConfirmDateChangeDialog() {
		logger.info("Looking for Confirm Date Change dialog...");

		try {
			String[] dialogXpaths = {
				"//div[contains(@role,'dialog') or contains(@class,'dialog') or contains(@class,'modal')]",
				"//*[contains(@id,'radix-')]//div[contains(@role,'alertdialog') or contains(@role,'dialog')]",
				"//div[contains(text(),'Confirm') and contains(text(),'Date')]//ancestor::div[contains(@role,'dialog')]",
				"//h2[contains(text(),'Confirm')]//ancestor::div[contains(@role,'dialog')]"
			};

			WebElement dialog = null;
			for (String xpath : dialogXpaths) {
				try {
					List<WebElement> dialogs = driver.findElements(By.xpath(xpath));
					for (WebElement d : dialogs) {
						if (d.isDisplayed()) {
							String dialogText = d.getText().toLowerCase();
							if (dialogText.contains("confirm") && (dialogText.contains("date") || dialogText.contains("change"))) {
								dialog = d;
								logger.info("Found Confirm Date Change dialog");
								break;
							}
						}
					}
					if (dialog != null) break;
				} catch (Exception e) {}
			}

			if (dialog == null) {
				logger.info("Confirm Date Change dialog not detected");
				return false;
			}

			captureEndorsementScreenshot("Confirm Date Change Dialog");

			// Find and click the Confirm Changes button
			String[] confirmButtonXpaths = {
				"//button[contains(text(),'Confirm Changes')]",
				"//button[contains(text(),'Confirm')]",
				"//button[contains(text(),'Yes')]",
				"//button[contains(text(),'OK')]",
				"//div[contains(@role,'dialog')]//button[contains(text(),'Confirm')]",
				"//*[contains(@id,'radix-')]//button[contains(text(),'Confirm')]"
			};

			WebElement confirmButton = null;
			for (String xpath : confirmButtonXpaths) {
				try {
					List<WebElement> buttons = driver.findElements(By.xpath(xpath));
					for (WebElement btn : buttons) {
						if (btn.isDisplayed() && btn.isEnabled()) {
							String btnText = btn.getText().toLowerCase();
							if (btnText.contains("confirm") || btnText.contains("yes") || btnText.contains("ok")) {
								confirmButton = btn;
								logger.info("Found Confirm button: '{}'", btn.getText());
								break;
							}
						}
					}
					if (confirmButton != null) break;
				} catch (Exception e) {}
			}

			if (confirmButton == null) {
				logger.warn("Confirm Changes button not found in dialog");
				return false;
			}

			// Click the Confirm Changes button
			try {
				confirmButton.click();
				logger.info("Clicked Confirm Changes button");
			} catch (Exception e) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
				logger.info("Clicked Confirm Changes button via JavaScript");
			}

			sleep(2000);
			logger.info("Confirm Date Change dialog handled successfully");
			return true;

		} catch (Exception e) {
			logger.warn("Error handling Confirm Date Change dialog: {}", e.getMessage());
			return false;
		}
	}

	// ==================== Amount Methods ====================

	/**
	 * Get General Liability amount
	 */
	public String getGeneralLiabilityAmount() {
		try {
			return getGeneralLiabilityAmountInput().getAttribute("value");
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get Water/Sewer Backup amount
	 */
	public String getWaterSewerBackupAmount() {
		try {
			return getWaterSewerBackupAmountInput().getAttribute("value");
		} catch (Exception e) {
			return "";
		}
	}

	// ==================== Submit Button Methods ====================

	/**
	 * Check if submit button is enabled
	 */
	public boolean isSubmitButtonEnabled() {
		try {
			WebElement submitBtn = getSubmitButton();
			return submitBtn.isEnabled() && submitBtn.getAttribute("disabled") == null;
		} catch (Exception e) {
			return false;
		}
	}

	// ==================== Form Values Capture ====================

	/**
	 * Capture all endorsement form values
	 */
	public Map<String, String> captureEndorsementFormValues() {
		Map<String, String> values = new HashMap<>();

		try {
			values.put("Agent", getSelectedAgent());
			values.put("Insured", getSelectedInsured());
			values.put("Carrier", getSelectedCarrier());

			String effectiveDate = getEffectiveDate();
			String expirationDate = getExpirationDate();
			String endorsementEffectiveDate = getEndorsementEffectiveDate();

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

			logger.info("Captured {} form values: {}", pageType, values);

			boolean datesCapturedOk = !effectiveDate.equals("CAPTURE_FAILED") &&
				!expirationDate.equals("CAPTURE_FAILED") &&
				!endorsementEffectiveDate.equals("CAPTURE_FAILED");

			if (!datesCapturedOk) {
				logger.error("One or more dates failed to capture properly!");
				captureEndorsementScreenshot("Date Capture Failed");
			}
		} catch (Exception e) {
			logger.error("Error capturing {} form values: {}", pageType, e.getMessage());
		}

		return values;
	}

	/**
	 * Validate captured values
	 */
	public boolean validateCapturedValues(Map<String, String> values) {
		if (values == null || values.isEmpty()) return false;

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

	// ==================== Location Methods ====================

	/**
	 * Get location count from table
	 */
	@Override
	public int getLocationCount() {
		sleep(2000);
		try {
			List<WebElement> rows = driver.findElements(By.xpath("//table//tbody//tr[td]"));
			int count = 0;
			for (WebElement row : rows) {
				String rowText = row.getText().trim().toLowerCase();
				if (!rowText.isEmpty() && !rowText.contains("no data") && !rowText.contains("no location")) {
					count++;
				}
			}
			logger.info("{} location count: {}", pageType, count);
			return count;
		} catch (Exception e) {
			logger.warn("Error getting location count: {}", e.getMessage());
			return 0;
		}
	}

	/**
	 * Get policy number from URL
	 */
	public String getPolicyNumber() {
		try {
			String url = driver.getCurrentUrl();
			String[] parts = url.split("/");
			for (int i = 0; i < parts.length; i++) {
				if (parts[i].equals("policies") && i + 1 < parts.length) {
					return parts[i + 1].split("\\?")[0];
				}
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	// ==================== Abstract Methods (to be implemented by subclasses if needed) ====================

	/**
	 * Get expected URL part for this page type
	 */
	public abstract String getExpectedUrlPart();

	// ==================== Report Logging Methods ====================

	/**
	 * Log endorsement values to HTML report
	 */
	public void logEndorsementValuesToReport(Map<String, String> values) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #17a2b8;'>");
		html.append("<h3 style='color: #17a2b8; margin-top: 0;'>").append(pageType).append(" Form Values</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");

		for (Map.Entry<String, String> entry : values.entrySet()) {
			html.append("<tr style='border-bottom: 1px solid #dee2e6;'>");
			html.append("<td style='padding: 8px; font-weight: bold;'>").append(entry.getKey()).append("</td>");
			html.append("<td style='padding: 8px;'>").append(entry.getValue()).append("</td>");
			html.append("</tr>");
		}

		html.append("</table></div>");
		logHtmlToReport(html.toString());
	}
}
