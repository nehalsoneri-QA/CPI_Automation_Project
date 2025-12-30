package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
			wait.until(ExpectedConditions.urlContains("create-premium-endorsement"));
			wait.until(ExpectedConditions.visibilityOf(pageTitle));
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
	 */
	private String getDateFromDisabledField(WebElement element, String elementId) {
		String value = "";

		// Approach 1: Try getAttribute("value")
		try {
			value = element.getAttribute("value");
			if (isValidDateValue(value)) {
				logger.info("Got date from value attribute: {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 2: Try getText()
		try {
			value = element.getText();
			if (isValidDateValue(value)) {
				logger.info("Got date from getText(): {}", value);
				return value;
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 3: Try JavaScript to get value from disabled input
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

		// Approach 4: Try finding input element inside and get value
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

		// Approach 5: Try getting displayed text from spans inside element
		try {
			List<WebElement> spans = element.findElements(By.tagName("span"));
			for (WebElement span : spans) {
				String text = span.getText();
				if (isValidDateValue(text)) {
					logger.info("Got date from inner span: {}", text);
					return text;
				}
			}
		} catch (Exception e) {
			// Continue to next approach
		}

		// Approach 6: Try JavaScript to get all text content
		try {
			value = (String) ((JavascriptExecutor) driver).executeScript(
				"var el = document.getElementById('" + elementId + "');" +
				"if(el) {" +
				"  if(el.value && el.value !== '--') return el.value;" +
				"  var inputs = el.getElementsByTagName('input');" +
				"  for(var i=0; i<inputs.length; i++) {" +
				"    if(inputs[i].value && inputs[i].value !== '--') return inputs[i].value;" +
				"  }" +
				"  var spans = el.getElementsByTagName('span');" +
				"  for(var i=0; i<spans.length; i++) {" +
				"    var text = spans[i].innerText || spans[i].textContent;" +
				"    if(text && text.trim() && text !== '--') return text.trim();" +
				"  }" +
				"  return el.innerText || el.textContent || '';" +
				"}" +
				"return '';");
			if (isValidDateValue(value)) {
				logger.info("Got date from JavaScript comprehensive: {}", value);
				return value;
			}
		} catch (Exception e) {
			logger.warn("All approaches failed to get date value");
		}

		return value != null ? value : "";
	}

	/**
	 * Check if date value is valid (not null, not empty, not placeholder)
	 */
	private boolean isValidDateValue(String value) {
		return value != null && !value.isEmpty() && !value.equals("--") && !value.equals("null") && !value.equals("Select");
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
			return getValueFromDisabledDropdown(suggestedStateDropdown, "create-endorsement-suggested-state-select");
		} catch (Exception e) {
			logger.warn("Error getting selected state: {}", e.getMessage());
			return "";
		}
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
	 * Click New Location button to add a new location
	 */
	public void clickNewLocation() {
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", newLocationButton);
			logger.info("Clicked New Location button");
			sleep(1000);
		} catch (Exception e) {
			logger.error("Error clicking New Location: {}", e.getMessage());
		}
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
			values.put("EffectiveDate", getEffectiveDate());
			values.put("ExpirationDate", getExpirationDate());
			values.put("EndorsementEffectiveDate", getEndorsementEffectiveDate());
			values.put("GLAmount", getGeneralLiabilityAmount());
			values.put("WSAmount", getWaterSewerBackupAmount());
			values.put("State", getSelectedState());
			values.put("LocationCount", String.valueOf(getLocationCount()));

			logger.info("Captured endorsement form values: {}", values);
		} catch (Exception e) {
			logger.error("Error capturing endorsement form values: {}", e.getMessage());
		}

		return values;
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

	/**
	 * Take screenshot and log to report
	 */
	public void captureEndorsementScreenshot(String description) {
		captureScreenshotToReport("Create Premium Endorsement - " + description);
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
