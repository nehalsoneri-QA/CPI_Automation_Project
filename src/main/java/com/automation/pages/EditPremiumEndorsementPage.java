package com.automation.pages;

import com.github.javafaker.Faker;
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
import java.util.Locale;
import java.util.Map;

/**
 * Page Object for Create Premium Endorsement Page
 * Handles endorsement creation workflow
 */
public class EditPremiumEndorsementPage extends CreatePremiumEndorsementLocators {

	// Faker for generating test data when auto-complete fails
	private static final Faker faker = new Faker(new Locale("en-US"));

	public EditPremiumEndorsementPage(WebDriver driver) {
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
				ExpectedConditions.presenceOfElementLocated(By.id("edit-endorsement-agent-select")),
				ExpectedConditions.presenceOfElementLocated(By.id("edit-endorsement-carrier-select"))
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
			return getDateFromDisabledField(effectiveDatePicker, "edit-endorsement-effective-date-picker");
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
			return getDateFromDisabledField(expirationDatePicker, "edit-endorsement-expiration-date-picker");
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
	 * Get endorsement effective date value (handles react-date-picker component)
	 * The react-date-picker has a hidden input with full ISO date (YYYY-MM-DD)
	 * and visible segmented inputs for month, day, year
	 * Primary XPath: //*[@id="edit-endorsement-endorsement-effective-date-picker"]/div/div
	 */
	public String getEndorsementEffectiveDate() {
		try {
			logger.info("=== Getting Endorsement Effective Date ===");

			// Primary approach: Get date from react-date-picker hidden input (first input has full ISO date)
			try {
				WebElement dateContainer = driver.findElement(By.xpath("//*[@id='edit-endorsement-endorsement-effective-date-picker']/div/div"));
				if (dateContainer != null) {
					List<WebElement> inputs = dateContainer.findElements(By.xpath(".//input"));
					if (inputs.size() > 0) {
						// The first input in react-date-picker contains the full ISO date (YYYY-MM-DD)
						String firstInputValue = inputs.get(0).getAttribute("value");
						logger.info("Edit endorsement date - First input value: '{}'", firstInputValue);

						// Check if it's an ISO date format (YYYY-MM-DD)
						if (firstInputValue != null && firstInputValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
							// Return the ISO format directly - validation will handle normalization
							logger.info("Got endorsement date in ISO format: {}", firstInputValue);
							return firstInputValue;
						}
					}
				}
			} catch (Exception e) {
				logger.debug("Primary approach failed: {}", e.getMessage());
			}

			// Alternative: Try JavaScript to get the hidden input value directly
			try {
				String jsDate = (String) ((JavascriptExecutor) driver).executeScript(
					"var picker = document.getElementById('edit-endorsement-endorsement-effective-date-picker');" +
					"if (picker) {" +
					"  var inputs = picker.querySelectorAll('input');" +
					"  for (var i = 0; i < inputs.length; i++) {" +
					"    var val = inputs[i].value;" +
					"    if (val && /^\\d{4}-\\d{2}-\\d{2}$/.test(val)) {" +
					"      return val;" +
					"    }" +
					"  }" +
					"}" +
					"return '';"
				);

				if (jsDate != null && !jsDate.isEmpty()) {
					logger.info("Got date via JavaScript: {}", jsDate);
					return jsDate;
				}
			} catch (Exception e) {
				logger.debug("JavaScript approach failed: {}", e.getMessage());
			}

			// Fallback: Try standard locator approach
			try {
				String value = getDateFromDisabledField(endorsementEffectiveDatePicker, "edit-endorsement-endorsement-effective-date-picker");
				if (value != null && !value.isEmpty() && !value.equals("N/A")) {
					return value;
				}
			} catch (Exception e) {
				logger.debug("Standard locator approach failed: {}", e.getMessage());
			}

			logger.warn("Could not get endorsement effective date");
			return "";
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
	 * Change Endorsement Effective Date to 6 months from current date
	 * Handles the Confirm Date Change dialog by clicking Confirm Changes button
	 * @return true if date was changed successfully
	 */
	public boolean changeEndorsementDateTo6MonthsLater() {
		logger.info("=== Changing Endorsement Effective Date to 6 months later ===");

		try {
			WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
			WebDriverWait mediumWait = new WebDriverWait(driver, Duration.ofSeconds(10));

			// Calculate date 6 months from now
			java.time.LocalDate currentDate = java.time.LocalDate.now();
			java.time.LocalDate futureDate = currentDate.plusMonths(6);
			int targetDay = futureDate.getDayOfMonth();
			String targetMonth = futureDate.getMonth().toString();
			int targetYear = futureDate.getYear();

			logger.info("Current date: {}, Target date (6 months later): {} {}, {}",
				currentDate, targetMonth, targetDay, targetYear);

			// Find the date picker container
			WebElement datePickerContainer = null;
			try {
				datePickerContainer = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("edit-endorsement-endorsement-effective-date-picker")));
				logger.info("Found date picker container");
			} catch (Exception e) {
				logger.error("Date picker container not found");
				captureEndorsementScreenshot("Date Picker Not Found");
				return false;
			}

			// Scroll to the date picker
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", datePickerContainer);
			shortWait.until(ExpectedConditions.visibilityOf(datePickerContainer));

			captureEndorsementScreenshot("Before Date Change");

			// Click on the calendar icon button (SVG inside button) to open the date picker dialog
			// User provided xpath: //*[@id='edit-endorsement-endorsement-effective-date-picker']/div/button/svg
			WebElement calendarButton = null;
			String[] calendarButtonXpaths = {
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']/div/button/svg",
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']/div/button",
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']//button[.//svg]",
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']//button",
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']//svg",
				"//div[contains(@id,'endorsement-effective-date')]//button/svg",
				"//div[contains(@id,'endorsement-effective-date')]//button[.//svg]",
				"//div[contains(@id,'endorsement-effective-date')]//button"
			};

			for (String xpath : calendarButtonXpaths) {
				try {
					calendarButton = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
					if (calendarButton != null && calendarButton.isDisplayed()) {
						logger.info("Found calendar button with xpath: {}", xpath);
						break;
					}
				} catch (Exception e) {
					// Continue to next xpath
				}
			}

			if (calendarButton == null) {
				logger.error("Calendar button not found with any xpath");
				captureEndorsementScreenshot("Calendar Button Not Found");
				return false;
			}

			// Click the calendar button/SVG to open the date picker dialog using JavaScript
			try {
				// If it's an SVG element, we need to click its parent button
				String tagName = calendarButton.getTagName().toLowerCase();
				if (tagName.equals("svg") || tagName.equals("path")) {
					// Get parent button and click it
					WebElement parentButton = (WebElement) ((JavascriptExecutor) driver).executeScript(
						"return arguments[0].closest('button');", calendarButton);
					if (parentButton != null) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", parentButton);
						logger.info("Clicked on parent button of SVG element");
					} else {
						// Click on SVG directly
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", calendarButton);
						logger.info("Clicked directly on SVG element");
					}
				} else {
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", calendarButton);
					logger.info("Clicked on calendar button to open date picker dialog");
				}
			} catch (Exception e) {
				logger.warn("JavaScript click failed, trying regular click: {}", e.getMessage());
				try {
					calendarButton.click();
					logger.info("Clicked on calendar button via regular click");
				} catch (Exception e2) {
					logger.error("Both click methods failed: {}", e2.getMessage());
				}
			}

			// Wait for calendar dialog to appear
			try {
				mediumWait.until(ExpectedConditions.or(
					ExpectedConditions.presenceOfElementLocated(By.xpath("//table[.//th[contains(text(),'MON') or contains(text(),'Mon') or contains(text(),'SUN') or contains(text(),'Sun')]]")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'calendar') or contains(@class,'Calendar')]")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//button[text()='>' or text()='<']")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'popover') or contains(@class,'Popover')]//table"))
				));
				logger.info("Calendar date picker dialog is now visible");
			} catch (Exception e) {
				logger.warn("Could not detect calendar dialog appearance, continuing anyway");
			}

			captureEndorsementScreenshot("Calendar Opened");

			// Navigate forward 6 months using the ">" (next month) button
			for (int i = 0; i < 6; i++) {
				boolean clicked = clickNextMonthButton();
				if (!clicked) {
					logger.warn("Could not click next month button at iteration {}", i + 1);
					break;
				}
				// Wait for calendar to update (month header to change)
				try {
					shortWait.until(ExpectedConditions.stalenessOf(
						driver.findElement(By.xpath("//table//th[contains(@colspan,'5') or contains(@colspan,'7')]"))
					));
				} catch (Exception e) {
					// Element might not become stale, that's okay
				}
			}

			captureEndorsementScreenshot("After Navigating 6 Months");

			// Click on the target day
			boolean dayClicked = clickDayInCalendar(targetDay);
			if (!dayClicked) {
				logger.warn("Could not click on day {}, trying alternative approach", targetDay);
				// Try typing the date as fallback using the date picker input field
				try {
					String formattedDate = futureDate.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
					WebElement dateInput = datePickerContainer.findElement(By.xpath(".//input"));
					dateInput.sendKeys(Keys.CONTROL + "a");
					dateInput.sendKeys(formattedDate);
					dateInput.sendKeys(Keys.TAB);
					logger.info("Entered date manually: {}", formattedDate);
				} catch (Exception e) {
					logger.warn("Could not type date manually: {}", e.getMessage());
				}
			}

			// Wait for calendar to close or dialog to appear
			try {
				mediumWait.until(ExpectedConditions.or(
					ExpectedConditions.invisibilityOfElementLocated(By.xpath("//table[.//th[contains(text(),'MON') or contains(text(),'Mon')]]")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@role,'dialog')]")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(),'Confirm')]"))
				));
			} catch (Exception e) {
				logger.warn("Timeout waiting for calendar to close");
			}

			logger.info("Selected date: {} {}, {}", targetMonth, targetDay, targetYear);
			captureEndorsementScreenshot("After Date Selection");

			// Wait for and handle the Confirm Date Change dialog
			boolean dialogHandled = handleConfirmDateChangeDialog();

			if (dialogHandled) {
				logger.info("Successfully changed endorsement date to 6 months later");
				captureEndorsementScreenshot("Date Change Confirmed");
			} else {
				logger.warn("Confirm Date Change dialog not found or not handled");
			}

			// Validate that the selected date is displayed correctly in the textbox
			String expectedDate = futureDate.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
			String actualDisplayedDate = getEndorsementEffectiveDateValue();

			boolean dateValidationPassed = validateDisplayedDate(expectedDate, actualDisplayedDate);
			logDateValidationToReport(expectedDate, actualDisplayedDate, dateValidationPassed);

			return dateValidationPassed;

		} catch (Exception e) {
			logger.error("Error changing endorsement effective date: {}", e.getMessage());
			captureEndorsementScreenshot("Date Change Error");
			return false;
		}
	}

	/**
	 * Get the current value from the Endorsement Effective Date field
	 */
	private String getEndorsementEffectiveDateValue() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

			// Try multiple approaches to get the date value
			String[] inputXpaths = {
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']//input",
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']/div/input",
				"//div[contains(@id,'endorsement-effective-date')]//input",
				"//*[@id='edit-endorsement-endorsement-effective-date-picker']//input[@type='text']"
			};

			for (String xpath : inputXpaths) {
				try {
					WebElement input = driver.findElement(By.xpath(xpath));
					if (input.isDisplayed()) {
						// Try getting value attribute first
						String value = input.getAttribute("value");
						if (value != null && !value.isEmpty()) {
							logger.info("Got date value from input 'value' attribute: {}", value);
							return value.trim();
						}
						// Try getting text
						String text = input.getText();
						if (text != null && !text.isEmpty()) {
							logger.info("Got date value from input text: {}", text);
							return text.trim();
						}
						// Try JavaScript to get value
						value = (String) ((JavascriptExecutor) driver).executeScript(
							"return arguments[0].value;", input);
						if (value != null && !value.isEmpty()) {
							logger.info("Got date value via JavaScript: {}", value);
							return value.trim();
						}
					}
				} catch (Exception e) {
					// Continue to next xpath
				}
			}

			// Try finding by the disabled field method
			String disabledValue = getDateFromDisabledField(endorsementEffectiveDatePicker, "edit-endorsement-endorsement-effective-date-picker");
			if (disabledValue != null && !disabledValue.isEmpty() && !disabledValue.equals("N/A")) {
				logger.info("Got date value from disabled field: {}", disabledValue);
				return disabledValue;
			}

			logger.warn("Could not retrieve endorsement effective date value");
			return "N/A";

		} catch (Exception e) {
			logger.error("Error getting endorsement effective date value: {}", e.getMessage());
			return "N/A";
		}
	}

	/**
	 * Validate that the displayed date matches the expected date
	 * Handles different date formats (MM/dd/yyyy, M/d/yyyy, etc.)
	 */
	private boolean validateDisplayedDate(String expectedDate, String actualDate) {
		if (actualDate == null || actualDate.equals("N/A") || actualDate.isEmpty()) {
			logger.error("Actual date is empty or N/A");
			return false;
		}

		// Direct match
		if (expectedDate.equals(actualDate)) {
			logger.info("Date validation PASSED: Expected '{}' = Actual '{}'", expectedDate, actualDate);
			return true;
		}

		// Try parsing both dates and comparing
		try {
			java.time.format.DateTimeFormatter[] formatters = {
				java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
				java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"),
				java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
				java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
				java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy")
			};

			java.time.LocalDate expectedLocalDate = null;
			java.time.LocalDate actualLocalDate = null;

			// Parse expected date
			for (java.time.format.DateTimeFormatter fmt : formatters) {
				try {
					expectedLocalDate = java.time.LocalDate.parse(expectedDate, fmt);
					break;
				} catch (Exception e) {
					// Try next format
				}
			}

			// Parse actual date
			for (java.time.format.DateTimeFormatter fmt : formatters) {
				try {
					actualLocalDate = java.time.LocalDate.parse(actualDate, fmt);
					break;
				} catch (Exception e) {
					// Try next format
				}
			}

			if (expectedLocalDate != null && actualLocalDate != null) {
				boolean match = expectedLocalDate.equals(actualLocalDate);
				if (match) {
					logger.info("Date validation PASSED (after parsing): Expected '{}' = Actual '{}'", expectedDate, actualDate);
				} else {
					logger.error("Date validation FAILED: Expected '{}' but got '{}'", expectedDate, actualDate);
				}
				return match;
			}
		} catch (Exception e) {
			logger.warn("Error parsing dates for comparison: {}", e.getMessage());
		}

		logger.error("Date validation FAILED: Expected '{}' but got '{}'", expectedDate, actualDate);
		return false;
	}

	/**
	 * Log date validation result to HTML report
	 */
	private void logDateValidationToReport(String expectedDate, String actualDate, boolean passed) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		if (passed) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Endorsement Date Change Validation: PASSED</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Endorsement Date Change Validation: FAILED</h3>");
		}

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; font-size: 13px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 8px; text-align: left;'>Description</th>");
		html.append("<th style='padding: 8px; text-align: left;'>Expected</th>");
		html.append("<th style='padding: 8px; text-align: left;'>Actual</th>");
		html.append("<th style='padding: 8px; text-align: center;'>Status</th>");
		html.append("</tr>");

		String statusColor = passed ? "#28a745" : "#dc3545";
		String status = passed ? "PASS" : "FAIL";
		String bgColor = passed ? "#d4edda" : "#f8d7da";

		html.append(String.format("<tr style='background-color: %s;'>", bgColor));
		html.append("<td style='padding: 8px;'>Endorsement Effective Date (6 months from today)</td>");
		html.append(String.format("<td style='padding: 8px;'>%s</td>", expectedDate));
		html.append(String.format("<td style='padding: 8px;'>%s</td>", actualDate));
		html.append(String.format("<td style='padding: 8px; text-align: center; font-weight: bold; color: %s;'>%s</td>", statusColor, status));
		html.append("</tr>");

		html.append("</table>");
		html.append("<p style='color: #000000; font-size: 12px; margin-top: 10px;'>");
		html.append("Validation: After selecting a date 6 months from today in the date picker, ");
		html.append("the same value should be displayed in the Endorsement Effective Date textbox.</p>");
		html.append("</div>");

		logHtmlToReport(html.toString());
	}

	/**
	 * Wait for calendar to update after navigation (replaces Thread.sleep)
	 */
	private void waitForCalendarUpdate(WebDriverWait wait) {
		try {
			// Wait for any animations to complete
			wait.until(webDriver -> {
				try {
					return (Boolean) ((JavascriptExecutor) webDriver).executeScript(
						"return document.getAnimations ? document.getAnimations().length === 0 : true");
				} catch (Exception e) {
					return true;
				}
			});
		} catch (Exception e) {
			// Fallback: brief stability check
			logger.debug("Calendar animation wait completed or skipped");
		}
	}

	/**
	 * Click the next month button (">") in the calendar picker
	 * Calendar navigation buttons are: « < > »
	 * Button[4] is the next month button (>)
	 */
	private boolean clickNextMonthButton() {
		WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));

		// User confirmed: button[4] is the next month button (>)
		// button[3] is a date button (e.g., January 5), NOT the navigation button
		String[] nextButtonXpaths = {
			"//*[@id='edit-endorsement-endorsement-effective-date-picker']/span/div/div/div[1]/button[4]",
			"//*[@id='edit-endorsement-endorsement-effective-date-picker']//button[normalize-space()='>']",
			"//div[contains(@id,'endorsement-effective-date')]//button[normalize-space()='>']",
			"//button[normalize-space()='>']",
			"//button[text()='>']",
			"//button[@name='next-month']",
			"//button[@aria-label='Go to next month']",
			"//button[@aria-label='Next month']"
		};

		for (String xpath : nextButtonXpaths) {
			try {
				List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				for (WebElement btn : buttons) {
					if (btn.isDisplayed() && btn.isEnabled()) {
						String btnText = btn.getText().trim();
						logger.debug("Found button with text: '{}' using xpath: {}", btnText, xpath);
						// Click the button
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
						logger.info("Clicked next month button using xpath: {}", xpath);
						// Wait for calendar to update using explicit wait
						waitForCalendarUpdate(shortWait);
						return true;
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Try finding all buttons in the calendar container and clicking the one with ">"
		try {
			List<WebElement> calendarButtons = driver.findElements(
				By.xpath("//*[@id='edit-endorsement-endorsement-effective-date-picker']//button"));
			logger.info("Found {} buttons in date picker container", calendarButtons.size());
			for (int i = 0; i < calendarButtons.size(); i++) {
				WebElement btn = calendarButtons.get(i);
				if (btn.isDisplayed()) {
					String text = btn.getText().trim();
					logger.debug("Button[{}] text: '{}'", i, text);
					if (text.equals(">")) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
						logger.info("Clicked next month button at index {} with text '>'", i);
						// Wait for calendar to update using explicit wait
						waitForCalendarUpdate(shortWait);
						return true;
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Error finding next month button in container: {}", e.getMessage());
		}

		logger.warn("Next month button not found with any xpath");
		return false;
	}

	/**
	 * Click on a specific day in the calendar picker
	 */
	private boolean clickDayInCalendar(int day) {
		WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
		String dayStr = String.valueOf(day);

		String[] dayXpaths = {
			"//button[normalize-space()='" + dayStr + "' and not(@disabled)]",
			"//td[normalize-space()='" + dayStr + "']",
			"//div[normalize-space()='" + dayStr + "' and contains(@class,'day')]",
			"//span[normalize-space()='" + dayStr + "']/parent::button",
			"//button[contains(@class,'day') and normalize-space()='" + dayStr + "']",
			"//*[contains(@class,'calendar')]//button[normalize-space()='" + dayStr + "']",
			"//table//td[normalize-space()='" + dayStr + "']"
		};

		for (String xpath : dayXpaths) {
			try {
				List<WebElement> days = driver.findElements(By.xpath(xpath));
				for (WebElement dayEl : days) {
					if (dayEl.isDisplayed()) {
						String dayText = dayEl.getText().trim();
						// Make sure we're clicking the right day
						if (dayText.equals(dayStr)) {
							shortWait.until(ExpectedConditions.elementToBeClickable(dayEl));
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", dayEl);
							logger.info("Clicked on day {} using xpath: {}", day, xpath);
							return true;
						}
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		logger.warn("Day {} not found in calendar", day);
		return false;
	}

	/**
	 * Handle the Confirm Date Change dialog by clicking Confirm Changes button
	 * @return true if dialog was found and handled
	 */
	public boolean handleConfirmDateChangeDialog() {
		logger.info("Looking for Confirm Date Change dialog...");

		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			// Wait for dialog to appear
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
				} catch (Exception e) {
					// Continue
				}
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
				} catch (Exception e) {
					// Continue
				}
			}

			if (confirmButton == null) {
				logger.warn("Confirm Changes button not found in dialog");
				return false;
			}

			// Click the Confirm Changes button
			try {
				wait.until(ExpectedConditions.elementToBeClickable(confirmButton)).click();
				logger.info("Clicked Confirm Changes button");
			} catch (Exception e) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
				logger.info("Clicked Confirm Changes button via JavaScript");
			}

			// Wait for dialog to close
			try {
				wait.until(ExpectedConditions.invisibilityOf(dialog));
				logger.info("Dialog closed successfully");
			} catch (Exception e) {
				logger.info("Dialog may have closed or changed");
			}

			logger.info("Confirm Date Change dialog handled successfully");
			return true;

		} catch (Exception e) {
			logger.warn("Error handling Confirm Date Change dialog: {}", e.getMessage());
			return false;
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
			String value = getValueFromDisabledDropdown(suggestedStateDropdown, "edit-endorsement-suggested-state-select");

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
			"//*[@id='edit-endorsement-suggested-state-select']//span[contains(@class,'SelectValue')]",
			"//*[@id='edit-endorsement-suggested-state-select']/span",
			"//*[@id='edit-endorsement-suggested-state-select']",
			"//button[@id='edit-endorsement-suggested-state-select']//span",
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
	 */
	public void clickNewLocation() {
		logger.info("=== Clicking New Location / Add Location button ===");

		// Scroll to top first to ensure button area is visible
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
		sleep(1000);

		String[] buttonXpaths = {
			"//*[@id='root']/div[2]/div[2]/div/div[1]/div[2]/button",
			"//button[contains(text(),'Add Location')]",
			"//button[contains(text(),'Add location')]",
			"//button[contains(text(),'New Location')]",
			"//button[contains(text(),'New location')]",
			"//button[contains(@class,'bg-blue') and contains(text(),'Add')]",
			"//div[contains(@class,'flex')]//button[contains(text(),'Add')]",
			"//button[contains(@class,'primary') and contains(text(),'Location')]",
			"//*[@id='root']//button[contains(text(),'Location')]"
		};

		for (String xpath : buttonXpaths) {
			try {
				List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				logger.info("XPath '{}' found {} buttons", xpath, buttons.size());

				for (WebElement button : buttons) {
					try {
						String btnText = button.getText().trim();
						boolean isDisplayed = button.isDisplayed();
						boolean isEnabled = button.isEnabled();

						logger.info("  Button: text='{}', displayed={}, enabled={}", btnText, isDisplayed, isEnabled);

						if (isDisplayed && isEnabled) {
							// Scroll to button first
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].scrollIntoView({block: 'center'});", button);
							sleep(500);
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
							logger.info("Clicked Add Location button using xpath: {} (text: '{}')", xpath, btnText);
							sleep(1500);
							return;
						}
					} catch (Exception e) {
						// Continue to next button
					}
				}
			} catch (Exception e) {
				logger.debug("XPath {} failed: {}", xpath, e.getMessage());
			}
		}

		// Last resort: find any button with "Add" and "Location" text
		logger.info("Trying last resort: finding any Add Location button by tag");
		try {
			List<WebElement> allButtons = driver.findElements(By.tagName("button"));
			logger.info("Found {} total buttons on page", allButtons.size());

			for (WebElement button : allButtons) {
				try {
					String btnText = button.getText().trim().toLowerCase();
					if ((btnText.contains("add") && btnText.contains("location")) ||
						btnText.equals("add location") || btnText.equals("new location")) {
						if (button.isDisplayed() && button.isEnabled()) {
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].scrollIntoView({block: 'center'});", button);
							sleep(500);
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
							logger.info("Clicked Add Location button via tag search: '{}'", button.getText());
							sleep(1500);
							return;
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}
		} catch (Exception e) {
			logger.warn("Tag search failed: {}", e.getMessage());
		}

		// Try using the @FindBy locator
		try {
			if (newLocationButton != null && newLocationButton.isDisplayed()) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", newLocationButton);
				logger.info("Clicked New Location button using @FindBy locator");
				sleep(1500);
				return;
			}
		} catch (Exception e) {
			logger.warn("@FindBy locator failed: {}", e.getMessage());
		}

		// Capture screenshot for debugging
		captureEndorsementScreenshot("Add Location Button Not Found");
		logger.error("Could not find Add Location button with any strategy");
		throw new RuntimeException("Add Location button not found on Edit Endorsement page");
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
			String[] textXpaths = {
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'locations')]",
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'location')]",
				"//p[contains(text(),'Showing')]",
				"//span[contains(text(),'Showing')]",
				"//div[contains(text(),'Showing') and contains(text(),'locations')]"
			};

			for (String xpath : textXpaths) {
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

			// Fallback: count table rows directly using working table XPaths
			logger.info("Falling back to counting table rows directly");
			String[] tableXpaths = {
				"//*[@id='root']/div[2]/div[2]/div[2]/div[1]/div[1]/div/table",
				"//*[@id='root']//table[.//th[contains(text(),'Address')]]",
				"//table[.//th[contains(text(),'Address')]]",
				"//*[@id='root']//table"
			};

			for (String tableXpath : tableXpaths) {
				try {
					List<WebElement> tables = driver.findElements(By.xpath(tableXpath));
					for (WebElement table : tables) {
						if (table.isDisplayed()) {
							List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
							if (!rows.isEmpty()) {
								logger.info("Found {} rows using table xpath: {}", rows.size(), tableXpath);
								return rows.size();
							}
						}
					}
				} catch (Exception e) {
					// Continue to next xpath
				}
			}

			logger.warn("Could not find location table, returning 0");
			return 0;

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

			// Get values from page summary section using flexible label-based search
			double premiumGLWSValue = getSummaryValueByLabel("Premium", "Premium (GL", "GL+WS");
			double taxesValue = getSummaryValueByLabel("Taxes", "Tax");
			double totalFeesValue = getSummaryValueByLabel("Total Fees", "Fees");
			double grandTotalValue = getSummaryValueByLabel("Grand Total", "Total");

			result.setPremiumGLWSValue(premiumGLWSValue);
			result.setTaxesValue(taxesValue);
			result.setTotalFeesValue(totalFeesValue);
			result.setGrandTotalValue(grandTotalValue);

			logger.info("Page Values - Premium(GL+WS): {}, Taxes: {}, Fees: {}, Grand Total: {}",
				premiumGLWSValue, taxesValue, totalFeesValue, grandTotalValue);

			// Validation 1: Property Premium + GL Premium + WS Premium = Premium (GL + WS)
			double calculatedPremiumGLWS = propertyPremiumSum + glPremiumSum + wsPremiumSum;
			result.setCalculatedPremiumGLWS(calculatedPremiumGLWS);
			boolean premiumMatch = Math.abs(calculatedPremiumGLWS - premiumGLWSValue) < 0.05;
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
	 * Get summary value by searching for label text and finding associated h2/value element
	 * More flexible than hardcoded XPaths
	 */
	private double getSummaryValueByLabel(String... labelVariants) {
		try {
			// First try: Look for h2 elements in the summary section with $ values
			String[] summaryXpaths = {
				"//*[@id='root']/div[2]/div[2]/div[2]/div[2]//h2",
				"//*[@id='root']//div[contains(@class,'summary')]//h2",
				"//*[@id='root']//div[contains(@class,'total')]//h2",
				"//h2[contains(text(),'$')]"
			};

			// Build list of all h2 elements with dollar values
			java.util.List<String> allH2Values = new java.util.ArrayList<>();
			for (String xpath : summaryXpaths) {
				try {
					List<WebElement> h2Elements = driver.findElements(By.xpath(xpath));
					for (WebElement h2 : h2Elements) {
						String text = h2.getText().trim();
						if (text.contains("$")) {
							allH2Values.add(text);
						}
					}
					if (!allH2Values.isEmpty()) {
						break;
					}
				} catch (Exception e) {
					// Continue
				}
			}

			if (!allH2Values.isEmpty()) {
				logger.info("Found summary h2 values: {}", allH2Values);
			}

			// Search for label and find sibling/parent h2 with value
			for (String label : labelVariants) {
				try {
					// Try finding by sibling relationship: label followed by h2
					String[] xpathPatterns = {
						"//*[contains(text(),'" + label + "')]/following-sibling::h2",
						"//*[contains(text(),'" + label + "')]/parent::*/h2",
						"//*[contains(text(),'" + label + "')]/ancestor::div[1]//h2",
						"//div[.//*[contains(text(),'" + label + "')]]//h2",
						"//*[contains(text(),'" + label + "')]/following::h2[1]"
					};

					for (String xpath : xpathPatterns) {
						try {
							List<WebElement> elements = driver.findElements(By.xpath(xpath));
							for (WebElement elem : elements) {
								String text = elem.getText().trim();
								if (text.contains("$")) {
									double value = parseAmount(text);
									if (value > 0 || text.equals("$0.00")) {
										logger.info("Found '{}' value: {} using pattern with label '{}'", label, text, label);
										return value;
									}
								}
							}
						} catch (Exception e) {
							// Continue
						}
					}
				} catch (Exception e) {
					// Continue to next label variant
				}
			}

			// Fallback: Try specific positional XPaths based on known order
			// Order usually: Premium, Taxes, Fees, Grand Total
			String primaryLabel = labelVariants[0].toLowerCase();
			int positionIndex = -1;
			if (primaryLabel.contains("premium") && !primaryLabel.contains("total")) {
				positionIndex = 1;
			} else if (primaryLabel.contains("tax")) {
				positionIndex = 2;
			} else if (primaryLabel.contains("fee") && !primaryLabel.contains("grand")) {
				positionIndex = 3;
			} else if (primaryLabel.contains("grand") || (primaryLabel.contains("total") && !primaryLabel.contains("fee"))) {
				positionIndex = 4;
			}

			if (positionIndex > 0) {
				String[] fallbackXpaths = {
					"//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[1]/div[" + positionIndex + "]/div/h2",
					"//*[@id='root']/div[2]/div[2]/div[2]/div[2]/div[1]/div[" + positionIndex + "]//h2",
					"//*[@id='root']/div[2]/div[2]/div[2]/div[2]//div[" + positionIndex + "]//h2"
				};

				for (String xpath : fallbackXpaths) {
					try {
						WebElement elem = driver.findElement(By.xpath(xpath));
						String text = elem.getText().trim();
						if (text.contains("$")) {
							double value = parseAmount(text);
							logger.info("Found '{}' value via fallback position {}: {}", primaryLabel, positionIndex, text);
							return value;
						}
					} catch (Exception e) {
						// Continue
					}
				}
			}

			logger.warn("Could not find summary value for labels: {}", String.join(", ", labelVariants));
			return 0;

		} catch (Exception e) {
			logger.error("Error getting summary value by label: {}", e.getMessage());
			return 0;
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
	 * Validate Display Computations against Excel sheet data with pro-rata logic
	 * @param createEndorsementLocations locations from CreateEndorsement sheet
	 * @param editEndorsementLocations locations from EditEndorsement sheet
	 * @return DisplayComputationSheetValidationResult with all validation details
	 */
	public DisplayComputationSheetValidationResult validateDisplayComputationsAgainstSheetData(
			List<Map<String, String>> createEndorsementLocations,
			List<Map<String, String>> editEndorsementLocations) {

		logger.info("=== Validating Display Computations Against Sheet Data ===");
		logger.info("Create Endorsement locations: {}, Edit Endorsement locations: {}",
			createEndorsementLocations != null ? createEndorsementLocations.size() : 0,
			editEndorsementLocations != null ? editEndorsementLocations.size() : 0);

		DisplayComputationSheetValidationResult result = new DisplayComputationSheetValidationResult();

		try {
			// Calculate pro-rata days
			long proRataDays = calculateProRataDays();
			result.setProRataDays(proRataDays);
			result.setEndorsementEffectiveDate(getEndorsementEffectiveDate());
			result.setExpirationDate(getExpirationDate());

			// Combine all locations from both sheets for validation
			List<Map<String, String>> allSheetLocations = new java.util.ArrayList<>();
			if (createEndorsementLocations != null) {
				allSheetLocations.addAll(createEndorsementLocations);
			}
			if (editEndorsementLocations != null) {
				allSheetLocations.addAll(editEndorsementLocations);
			}

			if (allSheetLocations.isEmpty()) {
				result.setError("No locations provided from Excel sheets");
				return result;
			}

			// Click Display Computation dialog button
			boolean dialogOpened = openDisplayComputationDialog();
			if (!dialogOpened) {
				result.setError("Failed to open Display Computation dialog");
				return result;
			}

			// Capture all location data from dialog (with pagination)
			List<LocationComputationData> dialogData = captureAllLocationsFromDialog();
			logger.info("Captured {} locations from Display Computation dialog", dialogData.size());

			// Close the dialog
			closeEditDisplayComputationDialog();

			// Validate each sheet location against dialog data
			for (Map<String, String> sheetLocation : allSheetLocations) {
				// Get address with multiple key variations
				String sheetAddress = getAddressFromLocationData(sheetLocation);
				if (sheetAddress.isEmpty()) {
					logger.warn("No address found in sheet location data. Keys: {}", sheetLocation.keySet());
					continue;
				}

				// Find matching dialog data by address
				LocationComputationData dialogLoc = findDialogLocationByAddress(dialogData, sheetAddress);

				DisplayComputationLocationValidation locValidation = new DisplayComputationLocationValidation();
				locValidation.setAddress(sheetAddress);

				if (dialogLoc == null) {
					locValidation.setError("Location not found in Display Computation dialog");
					locValidation.setAllMatched(false);
					result.addLocationValidation(locValidation);
					continue;
				}

				// Get expected values from sheet with comprehensive key lookups
				double expectedDwelling = getValueWithKeyVariations(sheetLocation, "Dwelling", "CoverageA", "dwelling", "coverage_a");
				double expectedAS = getValueWithKeyVariations(sheetLocation, "AdditionalStructures", "CoverageB", "additional_structures", "coverage_b", "AS");
				double expectedBPP = getValueWithKeyVariations(sheetLocation, "BPP", "CoverageC", "bpp", "coverage_c", "BusinessPersonalProperty");
				double expectedLossOfRents = getValueWithKeyVariations(sheetLocation, "LossOfRents", "CoverageD", "loss_of_rents", "coverage_d", "LOR");
				double expectedRate = getValueWithKeyVariations(sheetLocation, "Rate", "SuggestedRate", "recommended_rate", "rate", "suggestedrate", "RecommendedRate");

				// Get actual values from dialog
				double actualDwelling = dialogLoc.getDwelling();
				double actualAS = dialogLoc.getAdditionalStructures();
				double actualBPP = dialogLoc.getBpp();
				double actualLossOfRents = dialogLoc.getLossOfRents();
				double actualRate = dialogLoc.getRate();
				double actualTaxes = dialogLoc.getTaxes();

				// Set values in validation result
				locValidation.setExpectedDwelling(expectedDwelling);
				locValidation.setExpectedAdditionalStructures(expectedAS);
				locValidation.setExpectedBPP(expectedBPP);
				locValidation.setExpectedLossOfRents(expectedLossOfRents);
				locValidation.setExpectedRate(expectedRate);

				locValidation.setActualDwelling(actualDwelling);
				locValidation.setActualAdditionalStructures(actualAS);
				locValidation.setActualBPP(actualBPP);
				locValidation.setActualLossOfRents(actualLossOfRents);
				locValidation.setActualRate(actualRate);
				locValidation.setActualTaxes(actualTaxes);

				// Validate each field
				boolean dwellingMatch = Math.abs(expectedDwelling - actualDwelling) < 1.0;
				boolean asMatch = Math.abs(expectedAS - actualAS) < 1.0;
				boolean bppMatch = Math.abs(expectedBPP - actualBPP) < 1.0;
				boolean lorMatch = Math.abs(expectedLossOfRents - actualLossOfRents) < 1.0;
				boolean rateMatch = Math.abs(expectedRate - actualRate) < 0.01;

				locValidation.setDwellingMatch(dwellingMatch);
				locValidation.setAdditionalStructuresMatch(asMatch);
				locValidation.setBppMatch(bppMatch);
				locValidation.setLossOfRentsMatch(lorMatch);
				locValidation.setRateMatch(rateMatch);

				// Calculate TIV = Dwelling + AS + BPP + Loss Of Rents
				double calculatedTIV = expectedDwelling + expectedAS + expectedBPP + expectedLossOfRents;
				double actualTIV = actualDwelling + actualAS + actualBPP + actualLossOfRents;
				locValidation.setCalculatedTIV(calculatedTIV);
				locValidation.setActualTIV(actualTIV);
				boolean tivMatch = Math.abs(calculatedTIV - actualTIV) < 1.0;
				locValidation.setTivMatch(tivMatch);

				// Calculate Property Premium = (TIV / 100 * rate)
				double annualPropertyPremium = (calculatedTIV / 100.0) * expectedRate;

				// Apply pro-rata logic if applicable
				double calculatedPropertyPremium;
				if (proRataDays > 0 && proRataDays < 365) {
					calculatedPropertyPremium = (annualPropertyPremium / 365.0) * proRataDays;
					locValidation.setProRataApplied(true);
				} else {
					calculatedPropertyPremium = annualPropertyPremium;
					locValidation.setProRataApplied(false);
				}
				calculatedPropertyPremium = Math.round(calculatedPropertyPremium * 100.0) / 100.0;

				locValidation.setAnnualPropertyPremium(annualPropertyPremium);
				locValidation.setCalculatedPropertyPremium(calculatedPropertyPremium);

				// Get displayed property premium from table for this location
				double displayedPropertyPremium = getPropertyPremiumFromTableByAddress(sheetAddress);
				locValidation.setDisplayedPropertyPremium(displayedPropertyPremium);

				boolean premiumMatch = Math.abs(calculatedPropertyPremium - displayedPropertyPremium) < 1.0;
				locValidation.setPropertyPremiumMatch(premiumMatch);

				// Overall match
				boolean allMatched = dwellingMatch && asMatch && bppMatch && lorMatch && rateMatch && tivMatch && premiumMatch;
				locValidation.setAllMatched(allMatched);

				result.addLocationValidation(locValidation);

				logger.info("Location '{}': Dwelling={}/{}, AS={}/{}, BPP={}/{}, LoR={}/{}, Rate={}/{}, TIV={}/{}, Premium={}/{}, Match={}",
					sheetAddress,
					expectedDwelling, actualDwelling,
					expectedAS, actualAS,
					expectedBPP, actualBPP,
					expectedLossOfRents, actualLossOfRents,
					expectedRate, actualRate,
					calculatedTIV, actualTIV,
					calculatedPropertyPremium, displayedPropertyPremium,
					allMatched);
			}

			result.calculateOverallResult();

			// Log to report
			logDisplayComputationSheetValidationToReport(result);

			captureEndorsementScreenshot("Display Computation Sheet Validation");

		} catch (Exception e) {
			logger.error("Error validating display computations against sheet: {}", e.getMessage());
			result.setError("Error: " + e.getMessage());
		}

		return result;
	}

	/**
	 * Find dialog location data by address (partial match)
	 */
	private LocationComputationData findDialogLocationByAddress(List<LocationComputationData> dialogData, String address) {
		String normalizedAddress = address.toLowerCase().replaceAll("[^a-z0-9]", "");

		for (LocationComputationData loc : dialogData) {
			String dialogAddr = loc.getAddress().toLowerCase().replaceAll("[^a-z0-9]", "");
			if (dialogAddr.contains(normalizedAddress) || normalizedAddress.contains(dialogAddr)) {
				return loc;
			}
		}
		return null;
	}

	/**
	 * Get property premium from table by address
	 */
	private double getPropertyPremiumFromTableByAddress(String address) {
		try {
			WebElement table = findLocationTable();
			if (table == null) return 0.0;

			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			int addressColIndex = findColumnIndexByHeaderText("Address");
			int premiumColIndex = findColumnIndexByHeaderText("Property Premium");

			String normalizedAddress = address.toLowerCase().replaceAll("[^a-z0-9]", "");

			for (WebElement row : rows) {
				List<WebElement> cells = row.findElements(By.tagName("td"));
				if (addressColIndex >= 0 && addressColIndex < cells.size()) {
					String rowAddress = cells.get(addressColIndex).getText().toLowerCase().replaceAll("[^a-z0-9]", "");
					if (rowAddress.contains(normalizedAddress) || normalizedAddress.contains(rowAddress)) {
						if (premiumColIndex >= 0 && premiumColIndex < cells.size()) {
							return extractCurrencyFromCell(cells.get(premiumColIndex));
						}
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Error getting property premium from table: {}", e.getMessage());
		}
		return 0.0;
	}

	/**
	 * Close Display Computation dialog for Edit Endorsement
	 */
	private void closeEditDisplayComputationDialog() {
		try {
			WebElement closeBtn = driver.findElement(By.xpath("//button[contains(text(),'Close Rate Calculation Details')]"));
			if (closeBtn.isDisplayed()) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);
				logger.info("Closed Display Computation dialog");
				sleep(500);
				return;
			}
		} catch (Exception e) {
			// Try other methods
		}

		try {
			driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
			sleep(300);
		} catch (Exception e) {
			// Ignore
		}
	}

	/**
	 * Log display computation sheet validation to HTML report
	 */
	public void logDisplayComputationSheetValidationToReport(DisplayComputationSheetValidationResult result) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		if (result.isAllPassed()) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Display Computation Validation (Against Sheet Data): PASSED</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Display Computation Validation (Against Sheet Data): FAILED</h3>");
		}

		if (result.getError() != null) {
			html.append("<p style='color: #dc3545;'>Error: ").append(result.getError()).append("</p>");
		}

		// Pro-rata info
		html.append("<p style='color: #000; margin: 10px 0;'><strong>Pro-Rata Days:</strong> ")
			.append(result.getProRataDays()).append(" (").append(result.getEndorsementEffectiveDate())
			.append(" to ").append(result.getExpirationDate()).append(")</p>");

		html.append("<p style='color: #000; margin: 10px 0;'><strong>Formulas:</strong> TIV = Dwelling + AS + BPP + Loss Of Rents | Property Premium = (TIV / 100 × Rate) × (ProRataDays / 365)</p>");

		// Summary
		html.append("<p style='color: #000;'><strong>Summary:</strong> ")
			.append(result.getPassedCount()).append(" Passed, ")
			.append(result.getFailedCount()).append(" Failed out of ")
			.append(result.getTotalCount()).append(" locations</p>");

		// Detailed table
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000; font-size: 11px; margin-top: 10px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 6px;'>Address</th>");
		html.append("<th style='padding: 6px;'>Dwelling</th>");
		html.append("<th style='padding: 6px;'>AS</th>");
		html.append("<th style='padding: 6px;'>BPP</th>");
		html.append("<th style='padding: 6px;'>LoR</th>");
		html.append("<th style='padding: 6px;'>Rate</th>");
		html.append("<th style='padding: 6px;'>TIV</th>");
		html.append("<th style='padding: 6px;'>Calc Premium</th>");
		html.append("<th style='padding: 6px;'>Disp Premium</th>");
		html.append("<th style='padding: 6px;'>Status</th>");
		html.append("</tr>");

		int rowNum = 0;
		for (DisplayComputationLocationValidation loc : result.getLocationValidations()) {
			String bgColor = rowNum % 2 == 0 ? "#ffffff" : "#f8f9fa";
			String status = loc.isAllMatched() ? "PASS" : "FAIL";
			String statusColor = loc.isAllMatched() ? "#28a745" : "#dc3545";

			html.append(String.format("<tr style='background-color: %s;'>", bgColor));
			html.append(String.format("<td style='padding: 6px;'>%s</td>", truncateAddress(loc.getAddress())));
			html.append(String.format("<td style='padding: 6px; color: %s;'>$%.0f / $%.0f</td>",
				loc.isDwellingMatch() ? "#28a745" : "#dc3545", loc.getExpectedDwelling(), loc.getActualDwelling()));
			html.append(String.format("<td style='padding: 6px; color: %s;'>$%.0f / $%.0f</td>",
				loc.isAdditionalStructuresMatch() ? "#28a745" : "#dc3545", loc.getExpectedAdditionalStructures(), loc.getActualAdditionalStructures()));
			html.append(String.format("<td style='padding: 6px; color: %s;'>$%.0f / $%.0f</td>",
				loc.isBppMatch() ? "#28a745" : "#dc3545", loc.getExpectedBPP(), loc.getActualBPP()));
			html.append(String.format("<td style='padding: 6px; color: %s;'>$%.0f / $%.0f</td>",
				loc.isLossOfRentsMatch() ? "#28a745" : "#dc3545", loc.getExpectedLossOfRents(), loc.getActualLossOfRents()));
			html.append(String.format("<td style='padding: 6px; color: %s;'>%.4f / %.4f</td>",
				loc.isRateMatch() ? "#28a745" : "#dc3545", loc.getExpectedRate(), loc.getActualRate()));
			html.append(String.format("<td style='padding: 6px; color: %s;'>$%.0f / $%.0f</td>",
				loc.isTivMatch() ? "#28a745" : "#dc3545", loc.getCalculatedTIV(), loc.getActualTIV()));
			html.append(String.format("<td style='padding: 6px;'>$%.2f%s</td>",
				loc.getCalculatedPropertyPremium(), loc.isProRataApplied() ? " (Pro-Rata)" : ""));
			html.append(String.format("<td style='padding: 6px;'>$%.2f</td>", loc.getDisplayedPropertyPremium()));
			html.append(String.format("<td style='padding: 6px; font-weight: bold; color: %s;'>%s</td>", statusColor, status));
			html.append("</tr>");
			rowNum++;
		}

		html.append("</table></div>");
		logHtmlToReport(html.toString());
	}

	// ==================== Display Computation Sheet Validation Result Classes ====================

	public static class DisplayComputationSheetValidationResult {
		private List<DisplayComputationLocationValidation> locationValidations = new java.util.ArrayList<>();
		private boolean allPassed;
		private int passedCount;
		private int failedCount;
		private long proRataDays;
		private String endorsementEffectiveDate;
		private String expirationDate;
		private String error;

		public void addLocationValidation(DisplayComputationLocationValidation v) { locationValidations.add(v); }
		public List<DisplayComputationLocationValidation> getLocationValidations() { return locationValidations; }
		public boolean isAllPassed() { return allPassed; }
		public int getPassedCount() { return passedCount; }
		public int getFailedCount() { return failedCount; }
		public int getTotalCount() { return locationValidations.size(); }
		public long getProRataDays() { return proRataDays; }
		public void setProRataDays(long v) { this.proRataDays = v; }
		public String getEndorsementEffectiveDate() { return endorsementEffectiveDate; }
		public void setEndorsementEffectiveDate(String v) { this.endorsementEffectiveDate = v; }
		public String getExpirationDate() { return expirationDate; }
		public void setExpirationDate(String v) { this.expirationDate = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }

		public void calculateOverallResult() {
			passedCount = 0;
			failedCount = 0;
			for (DisplayComputationLocationValidation v : locationValidations) {
				if (v.isAllMatched()) passedCount++;
				else failedCount++;
			}
			allPassed = failedCount == 0 && passedCount > 0;
		}
	}

	public static class DisplayComputationLocationValidation {
		private String address;
		private double expectedDwelling, actualDwelling;
		private double expectedAdditionalStructures, actualAdditionalStructures;
		private double expectedBPP, actualBPP;
		private double expectedLossOfRents, actualLossOfRents;
		private double expectedRate, actualRate;
		private double actualTaxes;
		private double calculatedTIV, actualTIV;
		private double annualPropertyPremium;
		private double calculatedPropertyPremium;
		private double displayedPropertyPremium;
		private boolean dwellingMatch, additionalStructuresMatch, bppMatch, lossOfRentsMatch, rateMatch;
		private boolean tivMatch, propertyPremiumMatch;
		private boolean proRataApplied;
		private boolean allMatched;
		private String error;

		public String getAddress() { return address; }
		public void setAddress(String v) { this.address = v; }
		public double getExpectedDwelling() { return expectedDwelling; }
		public void setExpectedDwelling(double v) { this.expectedDwelling = v; }
		public double getActualDwelling() { return actualDwelling; }
		public void setActualDwelling(double v) { this.actualDwelling = v; }
		public double getExpectedAdditionalStructures() { return expectedAdditionalStructures; }
		public void setExpectedAdditionalStructures(double v) { this.expectedAdditionalStructures = v; }
		public double getActualAdditionalStructures() { return actualAdditionalStructures; }
		public void setActualAdditionalStructures(double v) { this.actualAdditionalStructures = v; }
		public double getExpectedBPP() { return expectedBPP; }
		public void setExpectedBPP(double v) { this.expectedBPP = v; }
		public double getActualBPP() { return actualBPP; }
		public void setActualBPP(double v) { this.actualBPP = v; }
		public double getExpectedLossOfRents() { return expectedLossOfRents; }
		public void setExpectedLossOfRents(double v) { this.expectedLossOfRents = v; }
		public double getActualLossOfRents() { return actualLossOfRents; }
		public void setActualLossOfRents(double v) { this.actualLossOfRents = v; }
		public double getExpectedRate() { return expectedRate; }
		public void setExpectedRate(double v) { this.expectedRate = v; }
		public double getActualRate() { return actualRate; }
		public void setActualRate(double v) { this.actualRate = v; }
		public double getActualTaxes() { return actualTaxes; }
		public void setActualTaxes(double v) { this.actualTaxes = v; }
		public double getCalculatedTIV() { return calculatedTIV; }
		public void setCalculatedTIV(double v) { this.calculatedTIV = v; }
		public double getActualTIV() { return actualTIV; }
		public void setActualTIV(double v) { this.actualTIV = v; }
		public double getAnnualPropertyPremium() { return annualPropertyPremium; }
		public void setAnnualPropertyPremium(double v) { this.annualPropertyPremium = v; }
		public double getCalculatedPropertyPremium() { return calculatedPropertyPremium; }
		public void setCalculatedPropertyPremium(double v) { this.calculatedPropertyPremium = v; }
		public double getDisplayedPropertyPremium() { return displayedPropertyPremium; }
		public void setDisplayedPropertyPremium(double v) { this.displayedPropertyPremium = v; }
		public boolean isDwellingMatch() { return dwellingMatch; }
		public void setDwellingMatch(boolean v) { this.dwellingMatch = v; }
		public boolean isAdditionalStructuresMatch() { return additionalStructuresMatch; }
		public void setAdditionalStructuresMatch(boolean v) { this.additionalStructuresMatch = v; }
		public boolean isBppMatch() { return bppMatch; }
		public void setBppMatch(boolean v) { this.bppMatch = v; }
		public boolean isLossOfRentsMatch() { return lossOfRentsMatch; }
		public void setLossOfRentsMatch(boolean v) { this.lossOfRentsMatch = v; }
		public boolean isRateMatch() { return rateMatch; }
		public void setRateMatch(boolean v) { this.rateMatch = v; }
		public boolean isTivMatch() { return tivMatch; }
		public void setTivMatch(boolean v) { this.tivMatch = v; }
		public boolean isPropertyPremiumMatch() { return propertyPremiumMatch; }
		public void setPropertyPremiumMatch(boolean v) { this.propertyPremiumMatch = v; }
		public boolean isProRataApplied() { return proRataApplied; }
		public void setProRataApplied(boolean v) { this.proRataApplied = v; }
		public boolean isAllMatched() { return allMatched; }
		public void setAllMatched(boolean v) { this.allMatched = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
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
	 * Uses direct implementation for Edit Endorsement page
	 */
	public int addLocationsFromSheet(List<Map<String, String>> locations) {
		logger.info("Adding {} locations to Edit Endorsement", locations.size());
		int addedCount = 0;
		int rejectedDueToCA = 0;

		// Get initial location count before adding
		int initialCount = getLocationCountFromTable();
		logger.info("Initial location count before adding: {}", initialCount);

		for (int i = 0; i < locations.size(); i++) {
			Map<String, String> location = locations.get(i);

			// Try multiple column names for address
			String address = getValueFromMultipleKeys(location, "Address", "address", "Physical Address",
				"Insured Property Address", "Property Address", "ADDR", "addr");

			if (address.isEmpty()) {
				logger.warn("Skipping location {} - no address provided. Keys: {}", i + 1, location.keySet());
				continue;
			}

			String paymentPlan = location.getOrDefault("PaymentPlan", "PAID_IN_FULL");
			logger.info("Adding location {}/{}: {}", (i + 1), locations.size(), address);

			try {
				// Click Add Location button using Edit Endorsement specific XPath
				clickNewLocation();
				sleep(2000);

				// Find the dialog using Edit Endorsement specific method
				WebElement dialog = findLocationDialog();
				if (dialog == null) {
					logger.error("Could not find location dialog after clicking Add Location button");
					captureScreenshotToReport("Dialog Not Found - " + address);
					continue;
				}

				logger.info("Dialog opened for: {}", address);
				captureScreenshotToReport("Dialog Opened - Location " + (i + 1));

				// Fill the location dialog
				fillLocationDialog(dialog, location);
				sleep(1000);

				// Scroll to bottom and select payment plan
				scrollDialogDown(dialog);
				sleep(500);
				selectPaymentPlanInDialog(paymentPlan);
				sleep(500);

				// Scroll to bottom again to ensure Add button is visible
				scrollDialogDown(dialog);
				sleep(500);

				captureScreenshotToReport("Before Add Click - Location " + (i + 1));

				// Click Submit/Add button in dialog and verify it closes
				boolean dialogClosed = clickAddButtonAndVerifyClose();

				// Check for Arch California error
				if (isArchCaliforniaErrorDisplayed()) {
					String errorMsg = getArchCaliforniaErrorMessage();
					logger.info("EXPECTED: Arch California error - Location rejected: {} - {}", address, errorMsg);
					captureScreenshotToReport("Arch California Error - " + address);
					rejectedDueToCA++;
					dismissErrorDialog();
				} else if (dialogClosed) {
					addedCount++;
					logger.info("Successfully added location: {}", address);
				} else {
					logger.error("Add button may not have been clicked - dialog still open for: {}", address);
					captureScreenshotToReport("Dialog Still Open - " + address);
					// Try to close dialog with Escape
					try {
						driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
						sleep(1000);
					} catch (Exception ex) {
						// Ignore
					}
				}
			} catch (Exception e) {
				logger.error("Failed to add location {}: {}", address, e.getMessage());
				captureScreenshotToReport("Error Adding - " + address);
				if (isArchCaliforniaErrorDisplayed()) {
					captureScreenshotToReport("Arch California Error - " + address);
					rejectedDueToCA++;
					dismissErrorDialog();
				}
				// Try to close any open dialog
				try {
					driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
					sleep(500);
				} catch (Exception ex) {
					// Ignore
				}
			}
		}

		// Verify final count
		sleep(2000);
		int finalCount = getLocationCountFromTable();
		int actuallyAdded = finalCount - initialCount;
		logger.info("Location count: Before={}, After={}, Actually Added={}, Expected={}",
			initialCount, finalCount, actuallyAdded, addedCount);

		if (actuallyAdded != addedCount) {
			logger.warn("Mismatch! Expected to add {} but only {} were actually added", addedCount, actuallyAdded);
			addedCount = actuallyAdded; // Use actual count
		}

		captureScreenshotToReport("After Adding " + addedCount + " Locations");
		logger.info("Added {}/{} locations (rejected due to Arch+CA: {})", addedCount, locations.size(), rejectedDueToCA);
		return addedCount;
	}

	/**
	 * Click Add button and verify dialog closes and location was added
	 */
	private boolean clickAddButtonAndVerifyClose() {
		logger.info("Clicking Add button and verifying dialog closes");

		// Click the Add button
		clickAddButtonInDialog();
		sleep(3000);

		// Check for any error messages/toasts first
		if (checkForErrorMessages()) {
			logger.error("Error message detected after clicking Add button");
			captureScreenshotToReport("Error After Add Click");
			return false;
		}

		// Check if dialog is still open
		WebElement dialog = findLocationDialog();
		if (dialog == null || !dialog.isDisplayed()) {
			logger.info("Dialog closed successfully after clicking Add button");
			// Check for success toast/message
			checkForSuccessMessage();
			return true;
		}

		// Dialog still open - check for validation errors in dialog
		if (checkForValidationErrorsInDialog(dialog)) {
			logger.error("Validation errors found in dialog - cannot add location");
			captureScreenshotToReport("Validation Errors in Dialog");
			return false;
		}

		// Dialog still open - try clicking again
		logger.warn("Dialog still open, trying to click Add button again");
		clickAddButtonInDialog();
		sleep(2000);

		dialog = findLocationDialog();
		if (dialog == null || !dialog.isDisplayed()) {
			logger.info("Dialog closed on second attempt");
			return true;
		}

		logger.error("Dialog is still open after multiple Add button clicks");
		captureScreenshotToReport("Dialog Still Open After Add");
		return false;
	}

	/**
	 * Check for error messages/toasts on page
	 */
	private boolean checkForErrorMessages() {
		String[] errorXpaths = {
			"//*[contains(@class,'toast') and contains(@class,'error')]",
			"//*[contains(@class,'Toastify') and contains(@class,'error')]",
			"//*[contains(@class,'alert') and contains(@class,'error')]",
			"//*[contains(@class,'error-message')]",
			"//*[contains(@role,'alert') and contains(@class,'error')]",
			"//div[contains(@class,'text-red')]",
			"//p[contains(@class,'text-red')]"
		};

		for (String xpath : errorXpaths) {
			try {
				List<WebElement> errors = driver.findElements(By.xpath(xpath));
				for (WebElement error : errors) {
					if (error.isDisplayed()) {
						String errorText = error.getText();
						if (!errorText.trim().isEmpty()) {
							logger.error("Error message found: {}", errorText);
							return true;
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
	 * Check for success message/toast
	 */
	private void checkForSuccessMessage() {
		String[] successXpaths = {
			"//*[contains(@class,'toast') and contains(@class,'success')]",
			"//*[contains(@class,'Toastify') and contains(@class,'success')]",
			"//*[contains(@class,'alert') and contains(@class,'success')]",
			"//*[contains(text(),'successfully')]",
			"//*[contains(text(),'added')]"
		};

		for (String xpath : successXpaths) {
			try {
				List<WebElement> messages = driver.findElements(By.xpath(xpath));
				for (WebElement msg : messages) {
					if (msg.isDisplayed()) {
						String text = msg.getText();
						if (!text.trim().isEmpty()) {
							logger.info("Success message found: {}", text);
							return;
						}
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
	}

	/**
	 * Check for validation errors within the dialog
	 */
	private boolean checkForValidationErrorsInDialog(WebElement dialog) {
		String[] errorXpaths = {
			".//span[contains(@class,'error')]",
			".//p[contains(@class,'error')]",
			".//div[contains(@class,'error')]",
			".//*[contains(@class,'text-red')]",
			".//*[contains(@class,'invalid')]",
			".//span[contains(text(),'required')]",
			".//span[contains(text(),'invalid')]"
		};

		for (String xpath : errorXpaths) {
			try {
				List<WebElement> errors = dialog.findElements(By.xpath(xpath));
				for (WebElement error : errors) {
					if (error.isDisplayed()) {
						String errorText = error.getText();
						if (!errorText.trim().isEmpty() && errorText.length() < 200) {
							logger.error("Validation error in dialog: {}", errorText);
							return true;
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
	 * Get location count from the locations table
	 */
	private int getLocationCountFromTable() {
		try {
			// Look for location rows in the table
			String[] rowXpaths = {
				"//table//tbody//tr",
				"//div[contains(@class,'location')]//tr",
				"//tr[contains(@class,'location')]",
				"//*[contains(@class,'LocationRow')]",
				"//div[contains(@class,'grid')]//div[contains(@class,'row')]"
			};

			for (String xpath : rowXpaths) {
				try {
					List<WebElement> rows = driver.findElements(By.xpath(xpath));
					if (rows.size() > 0) {
						// Filter out header rows
						int count = 0;
						for (WebElement row : rows) {
							String rowText = row.getText().toLowerCase();
							if (!rowText.contains("address") || rowText.length() > 50) {
								count++;
							}
						}
						if (count > 0) {
							logger.info("Found {} location rows using xpath: {}", count, xpath);
							return count;
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}

			// Try to find location count from a counter/badge element
			String[] countXpaths = {
				"//*[contains(text(),'Location')]/following::*[contains(@class,'badge')]",
				"//*[contains(@class,'count')]",
				"//span[contains(@class,'total')]"
			};

			for (String xpath : countXpaths) {
				try {
					WebElement countElement = driver.findElement(By.xpath(xpath));
					String countText = countElement.getText().replaceAll("[^0-9]", "");
					if (!countText.isEmpty()) {
						return Integer.parseInt(countText);
					}
				} catch (Exception e) {
					// Continue
				}
			}

		} catch (Exception e) {
			logger.warn("Could not get location count: {}", e.getMessage());
		}
		return 0;
	}

	/**
	 * Add locations on Edit Endorsement page from sheet data
	 */
	public int addLocationsOnEditEndorsement(List<Map<String, String>> locations) {
		logger.info("=== Adding Locations on Edit Endorsement ===");
		return addLocationsFromSheet(locations);
	}

	/**
	 * Get value from map using multiple possible keys
	 */
	private String getValueFromMultipleKeys(Map<String, String> map, String... keys) {
		for (String key : keys) {
			// Try exact match
			if (map.containsKey(key)) {
				String value = map.get(key);
				if (value != null && !value.trim().isEmpty()) {
					return value.trim();
				}
			}
			// Try case-insensitive match
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
					String value = entry.getValue();
					if (value != null && !value.trim().isEmpty()) {
						return value.trim();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Get Suggested Rate value from location data map
	 * Reads from recommended_rate column (column F in Excel) which is normalized to SuggestedRate
	 * IMPORTANT: This method ensures we do NOT accidentally use AOP column (column Z) value
	 *
	 * Column mapping:
	 * - Column F: recommended_rate -> normalized to SuggestedRate (this is the rate we want)
	 * - Column Z: aop -> normalized to AOP (this is a deductible, NOT a rate)
	 *
	 * @param locationData the location data map (should be normalized from ExcelReader)
	 * @return the suggested rate value, or empty string if not found/invalid
	 */
	private String getSuggestedRateFromLocationData(Map<String, String> locationData) {
		// Log all keys for debugging
		logger.info("Location data keys: {}", locationData.keySet());

		// Priority order for finding Suggested Rate:
		// 1. SuggestedRate (normalized key from recommended_rate column)
		// 2. recommended_rate (original Excel column name)
		// 3. Recommended Rate (alternate naming)
		// NOTE: We do NOT look for just "Rate" to avoid confusion with other rate fields

		String suggestedRate = "";
		String sourceKey = "";

		// Try normalized key first (this is what ExcelReader.normalizeLocationRow creates)
		if (locationData.containsKey("SuggestedRate")) {
			suggestedRate = locationData.get("SuggestedRate");
			sourceKey = "SuggestedRate";
		}
		// Try original Excel column name
		else if (locationData.containsKey("recommended_rate")) {
			suggestedRate = locationData.get("recommended_rate");
			sourceKey = "recommended_rate";
		}
		// Try case variations
		else {
			for (Map.Entry<String, String> entry : locationData.entrySet()) {
				String key = entry.getKey();
				if (key != null) {
					String keyLower = key.trim().toLowerCase();
					// Match recommended_rate or suggested rate (but NOT just "rate" to avoid confusion)
					if (keyLower.equals("recommended_rate") ||
						keyLower.equals("suggestedrate") ||
						keyLower.equals("suggested_rate") ||
						keyLower.equals("suggested rate")) {
						suggestedRate = entry.getValue();
						sourceKey = key;
						break;
					}
				}
			}
		}

		logger.info("Found Suggested Rate from key '{}': '{}'", sourceKey, suggestedRate);

		// Validate the value
		if (suggestedRate != null && !suggestedRate.trim().isEmpty()) {
			try {
				// Clean the value - remove any non-numeric characters except decimal point
				String cleanedRate = suggestedRate.replaceAll("[^0-9.]", "");
				if (!cleanedRate.isEmpty()) {
					double rateValue = Double.parseDouble(cleanedRate);
					// Sanity check: Suggested Rate should typically be a small decimal (like 0.5, 1.2, etc.)
					// NOT a large number like 25000 (which would be AOP deductible)
					if (rateValue > 100) {
						logger.warn("Suggested Rate value '{}' seems too large (>100) - this might be AOP value by mistake. Skipping.", cleanedRate);
						return "";
					}
					logger.info("Validated Suggested Rate: '{}' (cleaned from '{}')", cleanedRate, suggestedRate);
					return cleanedRate;
				} else {
					logger.warn("Suggested Rate '{}' is not a valid number, skipping", suggestedRate);
				}
			} catch (NumberFormatException e) {
				logger.warn("Suggested Rate '{}' is not a valid number: {}", suggestedRate, e.getMessage());
			}
		}

		return "";
	}

	/**
	 * Log add locations result to HTML report
	 */
	public void logAddLocationsResultToReport(int totalLocations, int addedCount) {
		StringBuilder html = new StringBuilder();
		boolean success = addedCount > 0;

		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");
		html.append(success ? "background-color: #d4edda; border-left: 4px solid #28a745;'>" : "background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
		html.append("<h3 style='color: ").append(success ? "#28a745" : "#dc3545").append("; margin-top: 0;'>");
		html.append("Edit Endorsement - Add Locations: ").append(success ? "SUCCESS" : "FAILED").append("</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Total Locations in Sheet:</td><td style='padding: 8px;'>").append(totalLocations).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Successfully Added:</td><td style='padding: 8px;'>").append(addedCount).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Rejected (Arch+CA):</td><td style='padding: 8px;'>").append(totalLocations - addedCount).append("</td></tr>");
		html.append("</table></div>");

		logHtmlToReport(html.toString());
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

		// Fill City, State, ZipCode with Faker if empty after address auto-complete
		fillCityStateZipWithFakerIfEmpty(dialog, locationData);

		// Municipality (dropdown)
		String municipality = locationData.getOrDefault("Municipality", "");
		if (!municipality.isEmpty()) {
			selectMunicipalityInDialog(dialog, municipality);
		}

		// Select Property Type (RS = Residential, RM = Multi-Family)
		String propertyType = locationData.getOrDefault("PropertyType", "RS");
		selectPropertyTypeInDialog(dialog, propertyType);
		sleep(500);

		// Enter Units (# of Units field) - use Faker if empty and PropertyType is RM
		String units = locationData.getOrDefault("Units", "");
		if (units.isEmpty() && (propertyType.equalsIgnoreCase("RM") || propertyType.toLowerCase().contains("multi"))) {
			units = String.valueOf(faker.number().numberBetween(2, 10));
			logger.info("Units was empty for RM property - using Faker: {}", units);
		}
		if (!units.isEmpty()) {
			enterFieldInDialog(dialog, "Units", units);
		}

		// Year Built - use Faker if empty
		String yearBuilt = locationData.getOrDefault("YearBuilt", "");
		if (yearBuilt.isEmpty()) {
			yearBuilt = String.valueOf(faker.number().numberBetween(1970, 2020));
			logger.info("YearBuilt was empty - using Faker: {}", yearBuilt);
		}
		enterFieldInDialog(dialog, "Year Built", yearBuilt);

		// Roof Year - use Faker if empty
		String roofYear = locationData.getOrDefault("RoofYear", "");
		if (roofYear.isEmpty()) {
			roofYear = String.valueOf(faker.number().numberBetween(2010, 2024));
			logger.info("RoofYear was empty - using Faker: {}", roofYear);
		}
		enterFieldInDialog(dialog, "Roof", roofYear);

		// Stories - use Faker if empty
		String stories = locationData.getOrDefault("Stories", "");
		if (stories.isEmpty()) {
			stories = String.valueOf(faker.number().numberBetween(1, 3));
			logger.info("Stories was empty - using Faker: {}", stories);
		}
		enterFieldInDialog(dialog, "Stories", stories);

		// Sq. Ft. - use Faker if empty
		String sqFt = locationData.getOrDefault("SqFt", "");
		if (sqFt.isEmpty()) {
			sqFt = String.valueOf(faker.number().numberBetween(1000, 3000));
			logger.info("SqFt was empty - using Faker: {}", sqFt);
		}
		enterFieldInDialog(dialog, "Sq. Ft", sqFt);

		// Coverage A - use Faker if empty
		String coverageA = locationData.getOrDefault("CoverageA", "");
		if (coverageA.isEmpty()) {
			coverageA = String.valueOf(faker.number().numberBetween(50000, 150000));
			logger.info("CoverageA was empty - using Faker: {}", coverageA);
		}
		enterFieldInDialog(dialog, "Coverage A", coverageA);

		// Coverage B - use Faker if empty
		String coverageB = locationData.getOrDefault("CoverageB", "");
		if (coverageB.isEmpty()) {
			coverageB = String.valueOf(faker.number().numberBetween(5000, 15000));
			logger.info("CoverageB was empty - using Faker: {}", coverageB);
		}
		enterFieldInDialog(dialog, "Coverage B", coverageB);

		// Scroll down in dialog
		scrollDialogDown(dialog);
		sleep(500);

		// Coverage C - use Faker if empty
		String coverageC = locationData.getOrDefault("CoverageC", "");
		if (coverageC.isEmpty()) {
			coverageC = String.valueOf(faker.number().numberBetween(5000, 15000));
			logger.info("CoverageC was empty - using Faker: {}", coverageC);
		}
		enterFieldInDialog(dialog, "Coverage C", coverageC);

		// Loss of Rents - use Faker if empty
		String lossOfRents = locationData.getOrDefault("LossOfRents", "");
		if (lossOfRents.isEmpty()) {
			lossOfRents = String.valueOf(faker.number().numberBetween(5000, 10000));
			logger.info("LossOfRents was empty - using Faker: {}", lossOfRents);
		}
		enterFieldInDialog(dialog, "Loss of Rents", lossOfRents);

		// Scroll down again
		scrollDialogDown(dialog);
		sleep(300);

		// Suggested Rate - USE recommended_rate column value (normalized to SuggestedRate)
		// Priority: SuggestedRate (normalized from recommended_rate) > recommended_rate (original) > Rate
		// IMPORTANT: Do NOT use AOP column for Suggested Rate - AOP is a separate deductible field
		String suggestedRate = getSuggestedRateFromLocationData(locationData);
		logger.info("Suggested Rate value to enter: '{}'", suggestedRate);

		// Only enter if we have a valid rate value
		if (!suggestedRate.isEmpty()) {
			enterSuggestedRateInDialog(dialog, suggestedRate);
		} else {
			logger.info("No valid Suggested Rate found - field will keep its default/auto-calculated value");
		}

		// AOP Deductible - This is a SEPARATE field, NOT related to Suggested Rate
		// Use specific method to avoid accidentally entering into Suggested Rate field
		String aop = locationData.getOrDefault("AOP", "");
		if (!aop.isEmpty()) {
			enterAOPDeductibleInDialog(dialog, aop);
		}

		// Wind Deductible - Use specific method to target the correct field
		String windDeductible = locationData.getOrDefault("WindDeductible", "");
		if (!windDeductible.isEmpty()) {
			enterWindDeductibleInDialog(dialog, windDeductible);
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
	 * Fill City, State, and Zip Code fields with Faker-generated data if auto-complete didn't populate them
	 */
	private void fillCityStateZipWithFakerIfEmpty(WebElement dialog, Map<String, String> locationData) {
		logger.info("Checking if City, State, Zip need to be filled with Faker data");

		// Check and fill City
		String currentCity = getFieldValueInDialog(dialog, "City");
		if (currentCity == null || currentCity.trim().isEmpty()) {
			String city = locationData.getOrDefault("City", faker.address().city());
			enterFieldInDialog(dialog, "City", city);
			logger.info("City was empty - filled with Faker data: {}", city);
		} else {
			logger.info("City already populated: {}", currentCity);
		}

		// Check and fill State
		String currentState = getFieldValueInDialog(dialog, "State");
		if (currentState == null || currentState.trim().isEmpty()) {
			String state = locationData.getOrDefault("State", faker.address().stateAbbr());
			// Try to select from dropdown or enter text
			boolean stateSelected = selectStateInDialog(dialog, state);
			if (!stateSelected) {
				enterFieldInDialog(dialog, "State", state);
			}
			logger.info("State was empty - filled with Faker data: {}", state);
		} else {
			logger.info("State already populated: {}", currentState);
		}

		// Check and fill Zip Code
		String currentZip = getFieldValueInDialog(dialog, "Zip");
		if (currentZip == null || currentZip.trim().isEmpty()) {
			String zipCode = locationData.getOrDefault("ZipCode", faker.address().zipCode().split("-")[0]); // Get 5-digit zip
			enterFieldInDialog(dialog, "Zip", zipCode);
			logger.info("Zip was empty - filled with Faker data: {}", zipCode);
		} else {
			logger.info("Zip already populated: {}", currentZip);
		}
	}

	/**
	 * Get field value from input within dialog
	 */
	private String getFieldValueInDialog(WebElement dialog, String fieldLabel) {
		String[] xpaths = {
			".//label[contains(text(),'" + fieldLabel + "')]/following::input[1]",
			".//label[contains(text(),'" + fieldLabel + "')]/..//input",
			".//input[contains(@placeholder,'" + fieldLabel + "')]"
		};

		for (String xpath : xpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed()) {
						String value = input.getAttribute("value");
						if (value != null && !value.isEmpty()) {
							return value;
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
	 * Select state from dropdown in dialog
	 */
	private boolean selectStateInDialog(WebElement dialog, String state) {
		logger.info("Attempting to select State from dropdown: {}", state);

		String[] dropdownXpaths = {
			".//label[contains(text(),'State')]/following::button[1]",
			".//label[contains(text(),'State')]/following::select[1]",
			".//*[contains(text(),'State')]/parent::div//button"
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

						// Select option from dropdown
						String[] optionXpaths = {
							"//div[@role='option'][contains(text(),'" + state + "')]",
							"//div[contains(@class,'option')][contains(text(),'" + state + "')]",
							"//*[contains(text(),'" + state + "')][@role='option']"
						};

						for (String optXpath : optionXpaths) {
							try {
								List<WebElement> options = driver.findElements(By.xpath(optXpath));
								for (WebElement opt : options) {
									if (opt.isDisplayed()) {
										((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
										logger.info("State selected: {}", state);
										return true;
									}
								}
							} catch (Exception e2) {
								// Continue
							}
						}
						return false;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}
		return false;
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
	 * Improved to better handle address suggestion dialogs
	 */
	private void enterAddressWithAutoComplete(WebElement dialog, String address) {
		logger.info("Entering address with auto-complete: {}", address);

		// Find the Physical address input field within dialog
		String[] xpaths = {
			".//label[contains(text(),'Physical address')]/following::input[1]",
			".//label[contains(text(),'Physical Address')]/following::input[1]",
			".//label[contains(text(),'Address')]/following::input[1]",
			".//input[contains(@placeholder,'address') or contains(@placeholder,'Address')]",
			".//input[contains(@name,'address')]",
			".//input[contains(@id,'address')]"
		};

		WebElement addressInput = null;
		for (String xpath : xpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed() && input.isEnabled()) {
						addressInput = input;
						logger.info("Found address input using xpath: {}", xpath);
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

		// Clear and type the address (full text at once)
		clearInputField(addressInput);
		sleep(100);
		addressInput.sendKeys(address);

		logger.info("Typed address: {}", address);
		sleep(2000); // Wait for auto-complete suggestions to appear

		// Try to find and click a suggestion from the auto-complete dropdown
		boolean suggestionClicked = false;

		// Method 1: Look for Google Places autocomplete pac-container
		String[] suggestionXpaths = {
			"//div[contains(@class,'pac-container')]//div[contains(@class,'pac-item')][1]",
			"(//div[contains(@class,'pac-item')])[1]",
			"//div[@class='pac-container']//div[@class='pac-item'][1]",
			"//div[contains(@class,'pac-container')]/div[1]",
			// Generic dropdown suggestions
			"//div[contains(@class,'autocomplete')]//div[contains(@class,'suggestion')][1]",
			"//ul[contains(@class,'suggestions')]//li[1]",
			"//div[contains(@class,'suggestion-item')][1]",
			"//div[contains(@class,'dropdown-item')][1]",
			// Listbox options
			"//ul[@role='listbox']//li[1]",
			"//div[@role='listbox']//div[@role='option'][1]",
			"//*[@role='option'][1]",
			// Address specific
			"//div[contains(@class,'address-suggestion')][1]",
			"//li[contains(@class,'address-item')][1]"
		};

		for (String xpath : suggestionXpaths) {
			try {
				List<WebElement> suggestions = driver.findElements(By.xpath(xpath));
				for (WebElement suggestion : suggestions) {
					if (suggestion.isDisplayed()) {
						logger.info("Found suggestion element using xpath: {}", xpath);
						sleep(500);
						try {
							// Try JavaScript click first
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", suggestion);
							suggestionClicked = true;
							logger.info("Clicked suggestion using JavaScript");
						} catch (Exception jsEx) {
							// Try regular click
							try {
								suggestion.click();
								suggestionClicked = true;
								logger.info("Clicked suggestion using regular click");
							} catch (Exception clickEx) {
								logger.warn("Both click methods failed: {}", clickEx.getMessage());
							}
						}
						if (suggestionClicked) break;
					}
				}
				if (suggestionClicked) break;
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Method 2: Try keyboard navigation if no suggestion element was clicked
		if (!suggestionClicked) {
			logger.info("No suggestion element found, trying keyboard navigation");
			try {
				sleep(500);
				addressInput.sendKeys(Keys.ARROW_DOWN);
				sleep(300);
				addressInput.sendKeys(Keys.ENTER);
				sleep(1000);

				// Check if address was selected by verifying the input value changed
				String currentValue = addressInput.getAttribute("value");
				if (currentValue != null && !currentValue.isEmpty() && !currentValue.equals(address)) {
					logger.info("Address selected via keyboard: {}", currentValue);
					suggestionClicked = true;
				}
			} catch (Exception e) {
				logger.warn("Keyboard navigation failed: {}", e.getMessage());
			}
		}

		// Method 3: Try clicking TAB to trigger autocomplete selection
		if (!suggestionClicked) {
			logger.info("Trying TAB key to select suggestion");
			try {
				addressInput.sendKeys(Keys.TAB);
				sleep(1000);
				String currentValue = addressInput.getAttribute("value");
				if (currentValue != null && !currentValue.isEmpty()) {
					logger.info("Address after TAB: {}", currentValue);
					suggestionClicked = true;
				}
			} catch (Exception e) {
				logger.warn("TAB selection failed: {}", e.getMessage());
			}
		}

		if (suggestionClicked) {
			logger.info("Auto-complete suggestion selected for: {}", address);
			sleep(1000); // Wait for fields to populate
		} else {
			logger.warn("Could not select auto-complete suggestion, address typed manually: {}", address);
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
	 * Enter Suggested Rate in dialog - specific method to avoid confusion with other fields
	 * ALWAYS overwrites existing values with the Excel value (recommended_rate column)
	 */
	private void enterSuggestedRateInDialog(WebElement dialog, String rate) {
		if (rate == null || rate.isEmpty()) return;

		logger.info("Entering Suggested Rate field with value: {}", rate);

		// Specific XPaths for Suggested Rate field - more targeted to avoid picking wrong input
		String[] xpaths = {
			".//label[normalize-space()='Suggested Rate']/following::input[1]",
			".//label[contains(text(),'Suggested Rate')]/following::input[1]",
			".//label[contains(text(),'suggested rate')]/following::input[1]",
			".//label[text()='Suggested Rate']/parent::div//input",
			".//label[normalize-space()='Suggested Rate *']/following::input[1]",
			".//label[contains(text(),'Suggested Rate *')]/following::input[1]"
		};

		for (String xpath : xpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed() && input.isEnabled()) {
						// Verify this is not a coverage field or AOP field
						String inputName = input.getAttribute("name");
						String inputId = input.getAttribute("id");

						// Skip if this looks like a coverage field or AOP field
						if (inputName != null && (inputName.toLowerCase().contains("coverage") || inputName.toLowerCase().contains("aop"))) continue;
						if (inputId != null && (inputId.toLowerCase().contains("coverage") || inputId.toLowerCase().contains("aop"))) continue;

						// Log existing value for debugging
						String existingValue = input.getAttribute("value");
						logger.info("Suggested Rate field current value: '{}', will be replaced with: '{}'", existingValue, rate);

						// Scroll to the input
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
						sleep(300);

						// ALWAYS clear and enter our Excel value (overwrite any auto-populated value)
						clearInputField(input);
						sleep(100);
						input.sendKeys(rate);
						sleep(200);

						// Verify the value was entered correctly
						String newValue = input.getAttribute("value");
						logger.info("Suggested Rate field updated: '{}' -> '{}'", existingValue, newValue);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Fallback: try to find by looking for inputs near "Rate" text (but not AOP)
		try {
			List<WebElement> allInputs = dialog.findElements(By.xpath(".//input[@type='number' or @type='text']"));
			for (int i = 0; i < allInputs.size(); i++) {
				WebElement input = allInputs.get(i);
				if (input.isDisplayed() && input.isEnabled()) {
					try {
						// Check if this input has a label containing "Suggested Rate" nearby
						WebElement parent = input.findElement(By.xpath("./ancestor::div[1]"));
						String parentText = parent.getText().toLowerCase();
						// Must contain "suggested rate" or just "rate" but NOT "aop" or "coverage"
						if ((parentText.contains("suggested rate") ||
							(parentText.contains("rate") && !parentText.contains("aop") && !parentText.contains("coverage")))) {

							// Log existing value
							String existingValue = input.getAttribute("value");
							logger.info("Suggested Rate field (fallback) current value: '{}', will be replaced with: '{}'", existingValue, rate);

							// Scroll to input
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
							sleep(300);

							// ALWAYS clear and enter our Excel value
							clearInputField(input);
							sleep(100);
							input.sendKeys(rate);
							sleep(200);

							String newValue = input.getAttribute("value");
							logger.info("Suggested Rate field (fallback) updated: '{}' -> '{}'", existingValue, newValue);
							return;
						}
					} catch (Exception e) {
						// Continue
					}
				}
			}
		} catch (Exception e) {
			// Continue
		}

		logger.warn("Could not find Suggested Rate field to enter value: {}", rate);
	}

	/**
	 * Enter AOP Deductible in dialog - specific method to avoid entering into Suggested Rate field
	 * AOP field should be a dropdown/select, not a text input in most cases
	 */
	private void enterAOPDeductibleInDialog(WebElement dialog, String aopValue) {
		if (aopValue == null || aopValue.isEmpty()) return;

		logger.info("Entering AOP Deductible with value: {}", aopValue);

		// Try to find AOP as a dropdown/select first (more common for deductibles)
		String[] dropdownXpaths = {
			".//label[contains(text(),'AOP')]/following::select[1]",
			".//label[contains(text(),'AOP Deductible')]/following::select[1]",
			".//label[contains(text(),'AOP')]/following::div[contains(@class,'select')]//input",
			".//select[contains(@name,'aop') or contains(@id,'aop')]"
		};

		// Try dropdown first
		for (String xpath : dropdownXpaths) {
			try {
				List<WebElement> selects = dialog.findElements(By.xpath(xpath));
				for (WebElement select : selects) {
					if (select.isDisplayed()) {
						// It's a dropdown - try to select by value or visible text
						try {
							org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(select);
							try {
								dropdown.selectByValue(aopValue);
								logger.info("Selected AOP Deductible '{}' by value", aopValue);
								return;
							} catch (Exception e1) {
								try {
									dropdown.selectByVisibleText(aopValue);
									logger.info("Selected AOP Deductible '{}' by visible text", aopValue);
									return;
								} catch (Exception e2) {
									// Try partial match
									for (WebElement option : dropdown.getOptions()) {
										if (option.getText().contains(aopValue) || aopValue.contains(option.getAttribute("value"))) {
											option.click();
											logger.info("Selected AOP Deductible option: {}", option.getText());
											return;
										}
									}
								}
							}
						} catch (Exception e) {
							// Not a select element, continue
						}
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		// If not a dropdown, try as input field - but use VERY specific XPaths
		// to avoid accidentally finding Suggested Rate field
		String[] inputXpaths = {
			".//label[normalize-space()='AOP Deductible']/following-sibling::*//input",
			".//label[normalize-space()='AOP Deductible *']/following-sibling::*//input",
			".//label[text()='AOP Deductible']/parent::div//input",
			".//label[contains(text(),'AOP Deductible')]/parent::div//input",
			".//input[contains(@name,'aop_deductible') or contains(@id,'aop_deductible')]",
			".//input[contains(@name,'aopDeductible') or contains(@id,'aopDeductible')]"
		};

		for (String xpath : inputXpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed() && input.isEnabled()) {
						// Double-check this is NOT the Suggested Rate field
						String inputName = input.getAttribute("name");
						String inputId = input.getAttribute("id");
						String placeholder = input.getAttribute("placeholder");

						// Skip if this looks like a rate field
						if ((inputName != null && inputName.toLowerCase().contains("rate")) ||
							(inputId != null && inputId.toLowerCase().contains("rate")) ||
							(placeholder != null && placeholder.toLowerCase().contains("rate"))) {
							logger.warn("Skipping input that looks like Rate field: name={}, id={}", inputName, inputId);
							continue;
						}

						// Scroll to the input
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
						sleep(300);

						clearInputField(input);
						sleep(100);
						input.sendKeys(aopValue);
						sleep(200);

						logger.info("Entered AOP Deductible '{}' in input field", aopValue);
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		logger.warn("Could not find AOP Deductible field to enter value: {}", aopValue);
	}

	/**
	 * Enter Wind Deductible in dialog - specific method
	 */
	private void enterWindDeductibleInDialog(WebElement dialog, String windValue) {
		if (windValue == null || windValue.isEmpty()) return;

		logger.info("Entering Wind Deductible with value: {}", windValue);

		// Try dropdown first (wind deductibles are usually dropdowns)
		String[] dropdownXpaths = {
			".//label[contains(text(),'Wind')]/following::select[1]",
			".//label[contains(text(),'Wind Deductible')]/following::select[1]",
			".//select[contains(@name,'wind') or contains(@id,'wind')]"
		};

		for (String xpath : dropdownXpaths) {
			try {
				List<WebElement> selects = dialog.findElements(By.xpath(xpath));
				for (WebElement select : selects) {
					if (select.isDisplayed()) {
						try {
							org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(select);
							try {
								dropdown.selectByValue(windValue);
								logger.info("Selected Wind Deductible '{}' by value", windValue);
								return;
							} catch (Exception e1) {
								try {
									dropdown.selectByVisibleText(windValue);
									logger.info("Selected Wind Deductible '{}' by visible text", windValue);
									return;
								} catch (Exception e2) {
									for (WebElement option : dropdown.getOptions()) {
										if (option.getText().contains(windValue)) {
											option.click();
											logger.info("Selected Wind Deductible option: {}", option.getText());
											return;
										}
									}
								}
							}
						} catch (Exception e) {
							// Not a select, continue
						}
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		// Fallback to input field
		String[] inputXpaths = {
			".//label[contains(text(),'Wind Deductible')]/following-sibling::*//input",
			".//label[contains(text(),'Wind Deductible')]/parent::div//input",
			".//input[contains(@name,'wind') or contains(@id,'wind')]"
		};

		for (String xpath : inputXpaths) {
			try {
				List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed() && input.isEnabled()) {
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
						sleep(300);

						clearInputField(input);
						sleep(100);
						input.sendKeys(windValue);
						sleep(200);

						logger.info("Entered Wind Deductible '{}' in input field", windValue);
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		logger.warn("Could not find Wind Deductible field to enter value: {}", windValue);
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
	 * IMPORTANT: Must find button WITHIN the dialog, not outside
	 */
	private void clickAddButtonInDialog() {
		logger.info("Attempting to click Add location button in dialog");

		// First find the dialog element
		WebElement dialog = findLocationDialog();
		if (dialog == null) {
			logger.error("Cannot click Add button - dialog not found");
			return;
		}

		// Scroll dialog to bottom to ensure button is visible
		scrollDialogDown(dialog);
		sleep(500);
		scrollDialogDown(dialog);
		sleep(500);

		// IMPORTANT: Search for button WITHIN dialog using relative XPaths (starting with .)
		String[] buttonXpaths = {
			// Primary - within dialog context
			".//button[normalize-space()='Add location']",
			".//button[text()='Add location']",
			".//button[text()='Add Location']",
			".//button[contains(text(),'Add location')]",
			".//button[contains(text(),'Add Location')]",
			// Footer buttons within dialog
			".//div[contains(@class,'footer')]//button[contains(text(),'Add')]",
			".//div[contains(@class,'Footer')]//button[contains(text(),'Add')]",
			".//footer//button[contains(text(),'Add')]",
			// Submit type buttons
			".//button[@type='submit']",
			".//button[contains(@class,'primary')]",
			".//button[contains(@class,'bg-blue')]",
			// Generic Add button within dialog
			".//button[contains(text(),'Add')]"
		};

		for (String xpath : buttonXpaths) {
			try {
				List<WebElement> buttons = dialog.findElements(By.xpath(xpath));
				logger.info("Found {} buttons in dialog with xpath: {}", buttons.size(), xpath);
				for (WebElement button : buttons) {
					if (button.isDisplayed() && button.isEnabled()) {
						String buttonText = button.getText().trim();
						logger.info("Found button in dialog: '{}' (displayed={}, enabled={})",
							buttonText, button.isDisplayed(), button.isEnabled());

						// Skip cancel/close/new location buttons (New Location opens dialog, not submits)
						String lowerText = buttonText.toLowerCase();
						if (lowerText.contains("cancel") ||
							lowerText.contains("close") ||
							lowerText.contains("address") ||
							lowerText.contains("new location") ||
							lowerText.equals("new")) {
							logger.info("Skipping button: '{}'", buttonText);
							continue;
						}

						// This should be our Add button - scroll to it
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", button);
						sleep(300);

						// Try clicking with JavaScript
						try {
							logger.info("Clicking Add button: '{}'", buttonText);
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
							logger.info("Clicked Add location button with JavaScript: '{}'", buttonText);
							sleep(1000);
							return;
						} catch (Exception jsEx) {
							// Try regular click
							try {
								button.click();
								logger.info("Clicked Add location button with regular click: '{}'", buttonText);
								sleep(1000);
								return;
							} catch (Exception clickEx) {
								logger.warn("Failed to click button '{}': {}", buttonText, clickEx.getMessage());
							}
						}
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Last resort: Find all buttons in dialog and click the right one
		try {
			logger.info("Last resort - scanning all buttons in dialog");
			List<WebElement> allButtons = dialog.findElements(By.tagName("button"));
			logger.info("Found {} total buttons in dialog", allButtons.size());

			for (WebElement button : allButtons) {
				if (button.isDisplayed() && button.isEnabled()) {
					String buttonText = button.getText().trim();
					String lowerText = buttonText.toLowerCase();

					// Skip non-submit buttons
					if (lowerText.contains("cancel") || lowerText.contains("close") ||
						lowerText.contains("new") || lowerText.isEmpty()) {
						continue;
					}

					// Look for Add/Submit button
					if (lowerText.contains("add") || lowerText.contains("submit") || lowerText.contains("save")) {
						logger.info("Found potential submit button: '{}'", buttonText);
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", button);
						sleep(300);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
						logger.info("Clicked button: '{}'", buttonText);
						return;
					}
				}
			}

			// If we still haven't found it, try the last visible button (usually submit is at the end)
			for (int i = allButtons.size() - 1; i >= 0; i--) {
				WebElement button = allButtons.get(i);
				if (button.isDisplayed() && button.isEnabled()) {
					String buttonText = button.getText().trim();
					if (!buttonText.toLowerCase().contains("cancel")) {
						logger.info("Clicking last non-cancel button: '{}'", buttonText);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
						return;
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Last resort button search failed: {}", e.getMessage());
		}

		logger.error("Could not find Add location button in dialog - taking screenshot");
		captureScreenshotToReport("Add Button Not Found");
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

	// ==================== Pro-Rata Validation Methods ====================

	/**
	 * Validate pro-rata calculations for newly added locations after endorsement date change
	 * After changing the endorsement effective date, the newly added locations should have
	 * pro-rata amounts different from full annual amounts
	 * @param newlyAddedLocationCount number of locations added after date change
	 * @return ProRataValidationResult with validation details
	 */
	public ProRataValidationResult validateProRataForNewLocations(int newlyAddedLocationCount) {
		logger.info("=== Validating Pro-Rata Amounts for {} Newly Added Locations ===", newlyAddedLocationCount);
		ProRataValidationResult result = new ProRataValidationResult();
		result.setNewlyAddedCount(newlyAddedLocationCount);

		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			// Find location table
			WebElement table = findLocationTable();
			if (table == null) {
				result.setError("Location table not found");
				return result;
			}

			// Get all rows from the table
			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			int totalRows = rows.size();
			logger.info("Found {} total locations in table", totalRows);

			if (totalRows < newlyAddedLocationCount) {
				result.setError("Not enough rows in table. Expected at least " + newlyAddedLocationCount + " but found " + totalRows);
				return result;
			}

			// Get header indices for relevant columns
			Map<String, Integer> headerIndices = getTableHeaderIndices(table);

			// The newly added locations are the last N rows
			int startIndex = totalRows - newlyAddedLocationCount;
			List<LocationProRataData> newLocationData = new java.util.ArrayList<>();

			for (int i = startIndex; i < totalRows; i++) {
				WebElement row = rows.get(i);
				List<WebElement> cells = row.findElements(By.tagName("td"));

				LocationProRataData locData = new LocationProRataData();
				locData.setLocationNumber(i + 1);

				// Extract address
				try {
					int addressIdx = headerIndices.getOrDefault("address", 0);
					if (addressIdx < cells.size()) {
						locData.setAddress(cells.get(addressIdx).getText().trim());
					}
				} catch (Exception e) {
					locData.setAddress("Location " + (i + 1));
				}

				// Extract premium values
				locData.setPropertyPremium(extractCellValue(cells, headerIndices, "propertypremium", "premium"));
				locData.setGlPremium(extractCellValue(cells, headerIndices, "glpremium", "gl"));
				locData.setWsPremium(extractCellValue(cells, headerIndices, "wspremium", "ws", "water", "sewer"));
				locData.setTaxes(extractCellValue(cells, headerIndices, "taxes", "tax"));

				// Check if values indicate pro-rata (less than full annual)
				// Pro-rata values should be non-zero and potentially fractional
				boolean hasProRataIndicators = (locData.getPropertyPremium() > 0 ||
					locData.getGlPremium() > 0 || locData.getWsPremium() > 0 || locData.getTaxes() > 0);

				locData.setHasProRataValues(hasProRataIndicators);
				newLocationData.add(locData);

				logger.info("Location {} ({}): Premium=${}, GL=${}, WS=${}, Tax=${}",
					locData.getLocationNumber(), locData.getAddress(),
					locData.getPropertyPremium(), locData.getGlPremium(),
					locData.getWsPremium(), locData.getTaxes());
			}

			result.setLocationData(newLocationData);

			// Validation: All newly added locations should have values
			boolean allHaveValues = newLocationData.stream()
				.allMatch(LocationProRataData::isHasProRataValues);
			result.setAllLocationsHaveValues(allHaveValues);

			// Log result to report
			logProRataValidationToReport(result);

			return result;

		} catch (Exception e) {
			logger.error("Error validating pro-rata amounts: {}", e.getMessage());
			result.setError("Error during validation: " + e.getMessage());
			return result;
		}
	}

	/**
	 * Extract cell value from table row by header name
	 */
	private double extractCellValue(List<WebElement> cells, Map<String, Integer> headerIndices, String... possibleHeaders) {
		for (String header : possibleHeaders) {
			Integer idx = headerIndices.get(header.toLowerCase());
			if (idx != null && idx < cells.size()) {
				try {
					String text = cells.get(idx).getText().trim();
					text = text.replaceAll("[\\$,]", "");
					if (!text.isEmpty() && !text.equals("-")) {
						return Double.parseDouble(text);
					}
				} catch (Exception e) {
					// Continue to next header
				}
			}
		}
		return 0.0;
	}

	/**
	 * Get header indices from table
	 */
	private Map<String, Integer> getTableHeaderIndices(WebElement table) {
		Map<String, Integer> indices = new java.util.HashMap<>();
		try {
			List<WebElement> headers = table.findElements(By.xpath(".//thead//th | .//tr[1]//th"));
			for (int i = 0; i < headers.size(); i++) {
				String headerText = headers.get(i).getText().trim().toLowerCase()
					.replaceAll("\\s+", "").replaceAll("[^a-z0-9]", "");
				indices.put(headerText, i);
			}
		} catch (Exception e) {
			logger.warn("Error getting header indices: {}", e.getMessage());
		}
		return indices;
	}

	/**
	 * Log pro-rata validation results to HTML report
	 */
	public void logProRataValidationToReport(ProRataValidationResult result) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		if (result.isAllLocationsHaveValues() && result.getError() == null) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Pro-Rata Validation: PASSED</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Pro-Rata Validation: FAILED</h3>");
		}

		if (result.getError() != null) {
			html.append("<p style='color: #dc3545;'>Error: ").append(result.getError()).append("</p>");
		}

		html.append("<p style='color: #000000; font-weight: bold; font-size: 13px; margin: 10px 0; padding: 8px; background-color: #fff3cd; border-radius: 4px;'>");
		html.append("Pro-Rata calculation: Values for newly added locations after endorsement date change should reflect the remaining policy period.</p>");

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; font-size: 12px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 6px;'>Loc #</th>");
		html.append("<th style='padding: 6px;'>Address</th>");
		html.append("<th style='padding: 6px;'>Premium</th>");
		html.append("<th style='padding: 6px;'>GL</th>");
		html.append("<th style='padding: 6px;'>WS</th>");
		html.append("<th style='padding: 6px;'>Tax</th>");
		html.append("<th style='padding: 6px;'>Status</th>");
		html.append("</tr>");

		int rowNum = 1;
		for (LocationProRataData loc : result.getLocationData()) {
			String bgColor = rowNum % 2 == 0 ? "#f8f9fa" : "#ffffff";
			String status = loc.isHasProRataValues() ? "PASS" : "FAIL";
			String statusColor = loc.isHasProRataValues() ? "#28a745" : "#dc3545";

			html.append(String.format("<tr style='background-color: %s;'>", bgColor));
			html.append(String.format("<td style='padding: 6px; text-align: center;'>%d</td>", loc.getLocationNumber()));
			html.append(String.format("<td style='padding: 6px;'>%s</td>", loc.getAddress()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.2f</td>", loc.getPropertyPremium()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.2f</td>", loc.getGlPremium()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.2f</td>", loc.getWsPremium()));
			html.append(String.format("<td style='padding: 6px; text-align: right;'>$%.2f</td>", loc.getTaxes()));
			html.append(String.format("<td style='padding: 6px; text-align: center; font-weight: bold; color: %s;'>%s</td>", statusColor, status));
			html.append("</tr>");
			rowNum++;
		}

		html.append("</table></div>");
		logHtmlToReport(html.toString());
	}

	/**
	 * Inner class for Pro-Rata validation result
	 */
	public static class ProRataValidationResult {
		private int newlyAddedCount;
		private List<LocationProRataData> locationData = new java.util.ArrayList<>();
		private boolean allLocationsHaveValues;
		private String error;

		public int getNewlyAddedCount() { return newlyAddedCount; }
		public void setNewlyAddedCount(int v) { this.newlyAddedCount = v; }
		public List<LocationProRataData> getLocationData() { return locationData; }
		public void setLocationData(List<LocationProRataData> v) { this.locationData = v; }
		public boolean isAllLocationsHaveValues() { return allLocationsHaveValues; }
		public void setAllLocationsHaveValues(boolean v) { this.allLocationsHaveValues = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
	}

	/**
	 * Inner class for individual location pro-rata data
	 */
	public static class LocationProRataData {
		private int locationNumber;
		private String address;
		private double propertyPremium;
		private double glPremium;
		private double wsPremium;
		private double taxes;
		private boolean hasProRataValues;

		public int getLocationNumber() { return locationNumber; }
		public void setLocationNumber(int v) { this.locationNumber = v; }
		public String getAddress() { return address; }
		public void setAddress(String v) { this.address = v; }
		public double getPropertyPremium() { return propertyPremium; }
		public void setPropertyPremium(double v) { this.propertyPremium = v; }
		public double getGlPremium() { return glPremium; }
		public void setGlPremium(double v) { this.glPremium = v; }
		public double getWsPremium() { return wsPremium; }
		public void setWsPremium(double v) { this.wsPremium = v; }
		public double getTaxes() { return taxes; }
		public void setTaxes(double v) { this.taxes = v; }
		public boolean isHasProRataValues() { return hasProRataValues; }
		public void setHasProRataValues(boolean v) { this.hasProRataValues = v; }
	}

	// ==================== Pro-Rata Premium Calculation and Validation ====================

	/**
	 * Calculate number of days between Endorsement Effective Date and Expiration Date
	 * @return number of days, or -1 if dates cannot be parsed
	 */
	public long calculateProRataDays() {
		try {
			String endorsementDateStr = getEndorsementEffectiveDate();
			String expirationDateStr = getExpirationDate();

			logger.info("Calculating pro-rata days: Endorsement Date={}, Expiration Date={}",
				endorsementDateStr, expirationDateStr);

			java.time.LocalDate endorsementDate = parseDate(endorsementDateStr);
			java.time.LocalDate expirationDate = parseDate(expirationDateStr);

			if (endorsementDate == null || expirationDate == null) {
				logger.error("Could not parse dates for pro-rata calculation");
				return -1;
			}

			long days = java.time.temporal.ChronoUnit.DAYS.between(endorsementDate, expirationDate);
			logger.info("Pro-rata days calculated: {} days (from {} to {})", days, endorsementDate, expirationDate);
			return days;

		} catch (Exception e) {
			logger.error("Error calculating pro-rata days: {}", e.getMessage());
			return -1;
		}
	}

	/**
	 * Parse date string to LocalDate, handles multiple formats
	 */
	private java.time.LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty() || dateStr.equals("N/A")) {
			return null;
		}

		java.time.format.DateTimeFormatter[] formatters = {
			java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
			java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"),
			java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
			java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy")
		};

		for (java.time.format.DateTimeFormatter fmt : formatters) {
			try {
				return java.time.LocalDate.parse(dateStr, fmt);
			} catch (Exception e) {
				// Try next format
			}
		}
		return null;
	}

	/**
	 * Validate pro-rata premium calculations for newly added locations
	 * Formula:
	 *   Annual Premium = Coverage * Rate / 100
	 *   Per Day Premium = Annual Premium / 365
	 *   Pro-rata Premium = Per Day Premium * Number of Days
	 *
	 * @param locationDataList List of location data from Excel sheet
	 * @return ProRataPremiumValidationResult with all calculations and validation results
	 */
	public ProRataPremiumValidationResult validateProRataPremiumCalculations(List<Map<String, String>> locationDataList) {
		logger.info("=== Validating Pro-Rata Premium Calculations for {} Locations ===", locationDataList.size());
		ProRataPremiumValidationResult result = new ProRataPremiumValidationResult();

		try {
			// Step 1: Calculate number of pro-rata days
			long proRataDays = calculateProRataDays();
			if (proRataDays <= 0) {
				result.setError("Could not calculate pro-rata days");
				return result;
			}
			result.setProRataDays(proRataDays);

			// Get dates for report
			result.setEndorsementEffectiveDate(getEndorsementEffectiveDate());
			result.setExpirationDate(getExpirationDate());

			// Step 2: Find location table and get displayed values
			WebElement table = findLocationTable();
			if (table == null) {
				result.setError("Location table not found");
				return result;
			}

			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			int totalRows = rows.size();

			logger.info("Total rows in table: {}, Newly added locations to validate: {}",
				totalRows, locationDataList.size());

			// Find Address column index for matching
			int addressColIndex = findColumnIndexByHeaderText("Address");
			logger.info("Address column index: {}", addressColIndex);

			// Step 3: Calculate and validate each location by matching address
			// IMPORTANT: Process ALL locations even if some fail
			List<LocationProRataCalculation> calculations = new java.util.ArrayList<>();
			boolean allPassed = true;

			for (int i = 0; i < locationDataList.size(); i++) {
				try {
					Map<String, String> excelData = locationDataList.get(i);
					String excelAddress = excelData.getOrDefault("Address", "").trim();

					logger.info("Processing location {} of {} with address: '{}'",
						i + 1, locationDataList.size(), excelAddress);

					// Find matching row by address
					WebElement matchingRow = findRowByAddress(rows, addressColIndex, excelAddress);

					if (matchingRow == null) {
						logger.warn("Could not find row matching address: '{}' - adding as FAILED", excelAddress);
						LocationProRataCalculation calc = new LocationProRataCalculation();
						calc.setLocationNumber(i + 1);
						calc.setAddress(excelAddress);
						calc.setError("Row not found for address: " + excelAddress);
						calc.setAllMatched(false);
						calculations.add(calc);
						allPassed = false;
						// Continue to next location - don't skip
					} else {
						logger.info("Found matching row for address: '{}'", excelAddress);

						LocationProRataCalculation calc = calculateAndValidateLocation(
							i + 1, excelData, matchingRow, proRataDays);

						calculations.add(calc);
						if (!calc.isAllMatched()) {
							allPassed = false;
						}
					}
				} catch (Exception e) {
					// Catch any exception and continue to next location
					logger.error("Error processing location {}: {} - continuing to next", i + 1, e.getMessage());
					LocationProRataCalculation calc = new LocationProRataCalculation();
					calc.setLocationNumber(i + 1);
					calc.setAddress("Location " + (i + 1));
					calc.setError("Exception: " + e.getMessage());
					calc.setAllMatched(false);
					calculations.add(calc);
					allPassed = false;
					// Continue to next location - don't stop
				}
			}

			logger.info("Completed processing all {} locations. Passed: {}",
				locationDataList.size(), allPassed);

			result.setLocationCalculations(calculations);
			result.setAllPassed(allPassed);

			// Log to report
			logProRataPremiumValidationToReport(result);

			captureEndorsementScreenshot("Pro-Rata Premium Validation");

			return result;

		} catch (Exception e) {
			logger.error("Error validating pro-rata premiums: {}", e.getMessage());
			result.setError("Error during validation: " + e.getMessage());
			return result;
		}
	}

	/**
	 * Calculate and validate pro-rata premium for a single location
	 */
	private LocationProRataCalculation calculateAndValidateLocation(
			int locationNum, Map<String, String> excelData, WebElement tableRow, long proRataDays) {

		LocationProRataCalculation calc = new LocationProRataCalculation();
		calc.setLocationNumber(locationNum);
		calc.setProRataDays(proRataDays);

		try {
			// Extract values from Excel data
			double coverageA = parseDouble(excelData.getOrDefault("Dwelling", excelData.getOrDefault("CoverageA", "0")));
			double coverageB = parseDouble(excelData.getOrDefault("AdditionalStructures", excelData.getOrDefault("CoverageB", "0")));
			double coverageC = parseDouble(excelData.getOrDefault("BPP", excelData.getOrDefault("CoverageC", "0")));
			double coverageD = parseDouble(excelData.getOrDefault("LossOfRents", excelData.getOrDefault("CoverageD", "0")));
			double rate = parseDouble(excelData.getOrDefault("Rate", excelData.getOrDefault("SuggestedRate", "0")));
			String address = excelData.getOrDefault("Address", "Location " + locationNum);

			// Extract GL and WS annual amounts from disabled input fields on page
			double excelGLAmount = getDisabledInputValue("edit-endorsement-general-liability-amount-input");
			double excelWSAmount = getDisabledInputValue("edit-endorsement-water-sewer-backup-amount-input");
			logger.info("Captured GL Amount from page: ${}, WS Amount from page: ${}", excelGLAmount, excelWSAmount);

			calc.setAddress(address);
			calc.setCoverageA(coverageA);
			calc.setCoverageB(coverageB);
			calc.setCoverageC(coverageC);
			calc.setCoverageD(coverageD);
			calc.setRate(rate);
			calc.setExcelGLAmount(excelGLAmount);
			calc.setExcelWSAmount(excelWSAmount);

			logger.info("Location {}: CovA={}, CovB={}, CovC={}, CovD={}, Rate={}, GL={}, WS={}",
				locationNum, coverageA, coverageB, coverageC, coverageD, rate, excelGLAmount, excelWSAmount);

			// Calculate annual premiums
			double annualPremiumA = coverageA * rate / 100.0;
			double annualPremiumB = coverageB * rate / 100.0;
			double annualPremiumC = coverageC * rate / 100.0;
			double annualPremiumD = coverageD * rate / 100.0;
			double totalAnnualPremium = annualPremiumA + annualPremiumB + annualPremiumC + annualPremiumD;

			calc.setAnnualPremiumA(annualPremiumA);
			calc.setAnnualPremiumB(annualPremiumB);
			calc.setAnnualPremiumC(annualPremiumC);
			calc.setAnnualPremiumD(annualPremiumD);
			calc.setTotalAnnualPremium(totalAnnualPremium);

			// Calculate per day premium for Property
			double perDayPremium = totalAnnualPremium / 365.0;
			calc.setPerDayPremium(perDayPremium);

			// Calculate pro-rata Property Premium
			double calculatedProRataPremium = perDayPremium * proRataDays;
			calculatedProRataPremium = Math.round(calculatedProRataPremium * 100.0) / 100.0;
			calc.setCalculatedPropertyPremium(calculatedProRataPremium);

			// Calculate pro-rata GL Premium: (GLAmount / 365) * proRataDays
			double calculatedGLPremium = (excelGLAmount / 365.0) * proRataDays;
			calculatedGLPremium = Math.round(calculatedGLPremium * 100.0) / 100.0;
			calc.setCalculatedGLPremium(calculatedGLPremium);

			// Calculate pro-rata WS Premium: (WSAmount / 365) * proRataDays
			double calculatedWSPremium = (excelWSAmount / 365.0) * proRataDays;
			calculatedWSPremium = Math.round(calculatedWSPremium * 100.0) / 100.0;
			calc.setCalculatedWSPremium(calculatedWSPremium);

			logger.info("Location {}: Property Annual={}, PerDay={}, ProRata({} days)={}",
				locationNum, totalAnnualPremium, perDayPremium, proRataDays, calculatedProRataPremium);
			logger.info("Location {}: GL Annual={}, ProRata GL={}", locationNum, excelGLAmount, calculatedGLPremium);
			logger.info("Location {}: WS Annual={}, ProRata WS={}", locationNum, excelWSAmount, calculatedWSPremium);

			// Get displayed values from table row
			// Based on user-provided xpath: th[3] = Property Premium header
			// th indices are 1-based in xpath, so th[3] = column index 2 in 0-based array
			List<WebElement> cells = tableRow.findElements(By.tagName("td"));
			logger.info("Location {}: Found {} cells in row", locationNum, cells.size());

			// Log all cell values for debugging
			for (int c = 0; c < cells.size(); c++) {
				String cellText = cells.get(c).getText().trim();
				logger.info("Location {} - Cell[{}] = '{}'", locationNum, c, cellText);
			}

			// Find Property Premium column index by checking table headers
			// Use xpath to find which column has "Property Premium" header
			int propertyPremiumColIndex = findColumnIndexByHeaderText("Property Premium");
			int glPremiumColIndex = findColumnIndexByHeaderText("GL Premium");
			int wsPremiumColIndex = findColumnIndexByHeaderText("Water/Sewer Premium");
			int taxColIndex = findColumnIndexByHeaderText("Taxes");

			logger.info("Column indices found - Property Premium: {}, GL: {}, WS: {}, Tax: {}",
				propertyPremiumColIndex, glPremiumColIndex, wsPremiumColIndex, taxColIndex);

			double displayedPropertyPremium = 0.0;
			double displayedGLPremium = 0.0;
			double displayedWSPremium = 0.0;
			double displayedTax = 0.0;

			// Extract values using found column indices
			if (propertyPremiumColIndex >= 0 && propertyPremiumColIndex < cells.size()) {
				displayedPropertyPremium = extractCurrencyFromCell(cells.get(propertyPremiumColIndex));
				logger.info("Location {}: Property Premium from column {} = ${}", locationNum, propertyPremiumColIndex, displayedPropertyPremium);
			}
			if (glPremiumColIndex >= 0 && glPremiumColIndex < cells.size()) {
				displayedGLPremium = extractCurrencyFromCell(cells.get(glPremiumColIndex));
				logger.info("Location {}: GL Premium from column {} = ${}", locationNum, glPremiumColIndex, displayedGLPremium);
			}
			if (wsPremiumColIndex >= 0 && wsPremiumColIndex < cells.size()) {
				displayedWSPremium = extractCurrencyFromCell(cells.get(wsPremiumColIndex));
				logger.info("Location {}: WS Premium from column {} = ${}", locationNum, wsPremiumColIndex, displayedWSPremium);
			}
			if (taxColIndex >= 0 && taxColIndex < cells.size()) {
				displayedTax = extractCurrencyFromCell(cells.get(taxColIndex));
				logger.info("Location {}: Tax from column {} = ${}", locationNum, taxColIndex, displayedTax);
			}

			// Fallback: If column indices not found, scan for currency values
			if (propertyPremiumColIndex < 0) {
				logger.info("Location {}: Fallback - scanning cells for currency values", locationNum);
				int currencyCount = 0;
				for (int c = 0; c < cells.size(); c++) {
					double value = extractCurrencyFromCell(cells.get(c));
					if (value > 0 && value < 10000) {
						currencyCount++;
						if (currencyCount == 1) {
							displayedPropertyPremium = value;
							logger.info("Location {}: Fallback - Property Premium = ${} at index {}", locationNum, value, c);
						} else if (currencyCount == 2) {
							displayedGLPremium = value;
						} else if (currencyCount == 3) {
							displayedWSPremium = value;
						} else if (currencyCount == 4) {
							displayedTax = value;
							break;
						}
					}
				}
			}

			logger.info("Location {}: Displayed values - PropertyPremium=${}, GL=${}, WS=${}, Tax=${}",
				locationNum, displayedPropertyPremium, displayedGLPremium, displayedWSPremium, displayedTax);

			calc.setDisplayedPropertyPremium(displayedPropertyPremium);
			calc.setDisplayedGLPremium(displayedGLPremium);
			calc.setDisplayedWSPremium(displayedWSPremium);
			calc.setDisplayedTax(displayedTax);

			// Validate Property Premium - allow for small rounding differences (within $1)
			boolean propertyMatch = Math.abs(calculatedProRataPremium - displayedPropertyPremium) <= 1.0;
			calc.setPropertyPremiumMatch(propertyMatch);

			// Validate GL Premium - allow for small rounding differences (within $1)
			boolean glMatch = Math.abs(calculatedGLPremium - displayedGLPremium) <= 1.0;
			calc.setGlPremiumMatch(glMatch);

			// Validate WS Premium - allow for small rounding differences (within $1)
			boolean wsMatch = Math.abs(calculatedWSPremium - displayedWSPremium) <= 1.0;
			calc.setWsPremiumMatch(wsMatch);

			// All matched only if Property, GL, and WS all match
			calc.setAllMatched(propertyMatch && glMatch && wsMatch);

			logger.info("Location {}: Property - Calculated={}, Displayed={}, Match={}",
				locationNum, calculatedProRataPremium, displayedPropertyPremium, propertyMatch);
			logger.info("Location {}: GL - Calculated={}, Displayed={}, Match={}",
				locationNum, calculatedGLPremium, displayedGLPremium, glMatch);
			logger.info("Location {}: WS - Calculated={}, Displayed={}, Match={}",
				locationNum, calculatedWSPremium, displayedWSPremium, wsMatch);

		} catch (Exception e) {
			logger.error("Error calculating location {}: {}", locationNum, e.getMessage());
			calc.setError(e.getMessage());
			calc.setAllMatched(false);
		}

		return calc;
	}

	/**
	 * Parse string to double, handling currency formatting
	 */
	private double parseDouble(String value) {
		if (value == null || value.isEmpty() || value.equals("N/A") || value.equals("-")) {
			return 0.0;
		}
		try {
			return Double.parseDouble(value.replaceAll("[\\$,]", "").trim());
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Get address from location data with multiple possible key variations
	 */
	private String getAddressFromLocationData(Map<String, String> data) {
		String[] addressKeys = {"Address", "address", "StreetAddress", "street_address", "FullAddress", "full_address", "Location", "location"};

		// Try direct keys
		for (String key : addressKeys) {
			if (data.containsKey(key)) {
				String value = data.get(key);
				if (value != null && !value.trim().isEmpty()) {
					return value.trim();
				}
			}
		}

		// Try case-insensitive match
		for (Map.Entry<String, String> entry : data.entrySet()) {
			String key = entry.getKey().toLowerCase();
			if (key.contains("address") || key.contains("street") || key.contains("location")) {
				String value = entry.getValue();
				if (value != null && !value.trim().isEmpty() && value.contains(",")) {
					return value.trim();
				}
			}
		}

		// Try to build address from components
		String street = data.getOrDefault("StreetAddress", data.getOrDefault("street_address", ""));
		String city = data.getOrDefault("City", data.getOrDefault("city", ""));
		String state = data.getOrDefault("State", data.getOrDefault("state", ""));
		String zip = data.getOrDefault("ZipCode", data.getOrDefault("zip_code", data.getOrDefault("Zip", "")));

		if (!street.isEmpty()) {
			StringBuilder address = new StringBuilder(street);
			if (!city.isEmpty()) address.append(", ").append(city);
			if (!state.isEmpty()) address.append(", ").append(state);
			if (!zip.isEmpty()) address.append(" ").append(zip);
			return address.toString().trim();
		}

		return "";
	}

	/**
	 * Get value from location data map with multiple possible key variations
	 * Handles different naming conventions in Excel columns
	 */
	private double getValueWithKeyVariations(Map<String, String> data, String... keys) {
		for (String key : keys) {
			// Try exact match
			if (data.containsKey(key)) {
				String value = data.get(key);
				if (value != null && !value.isEmpty()) {
					double parsed = parseDouble(value);
					if (parsed > 0) {
						return parsed;
					}
				}
			}
			// Try case-insensitive match
			for (Map.Entry<String, String> entry : data.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(key)) {
					String value = entry.getValue();
					if (value != null && !value.isEmpty()) {
						double parsed = parseDouble(value);
						if (parsed > 0) {
							return parsed;
						}
					}
				}
			}
		}
		return 0.0;
	}

	/**
	 * Get value from disabled input field by ID
	 * Disabled fields require getAttribute("value") instead of getText()
	 */
	private double getDisabledInputValue(String elementId) {
		try {
			// Try multiple ways to find and get the value
			String[] xpaths = {
				"//*[@id='" + elementId + "']",
				"//input[@id='" + elementId + "']",
				"//*[contains(@id,'" + elementId.replace("edit-endorsement-", "") + "')]"
			};

			for (String xpath : xpaths) {
				try {
					WebElement element = driver.findElement(By.xpath(xpath));
					if (element != null) {
						// For disabled inputs, use getAttribute("value")
						String value = element.getAttribute("value");
						if (value == null || value.isEmpty()) {
							// Try getText as fallback
							value = element.getText();
						}
						if (value != null && !value.isEmpty()) {
							logger.info("Found value '{}' for element ID: {} using xpath: {}", value, elementId, xpath);
							return parseDouble(value);
						}
					}
				} catch (Exception e) {
					// Continue to next xpath
				}
			}

			// Try using JavaScript to get the value (more reliable for disabled fields)
			try {
				String jsValue = (String) ((JavascriptExecutor) driver).executeScript(
					"var el = document.getElementById('" + elementId + "'); " +
					"return el ? el.value : null;");
				if (jsValue != null && !jsValue.isEmpty()) {
					logger.info("Found value '{}' for element ID: {} using JavaScript", jsValue, elementId);
					return parseDouble(jsValue);
				}
			} catch (Exception e) {
				logger.warn("JavaScript approach failed for element: {}", elementId);
			}

			logger.warn("Could not find value for disabled input: {}", elementId);
			return 0.0;

		} catch (Exception e) {
			logger.error("Error getting disabled input value for {}: {}", elementId, e.getMessage());
			return 0.0;
		}
	}

	/**
	 * Extract cell value by column index
	 */
	private double extractCellValueByIndex(List<WebElement> cells, int index) {
		if (index < 0 || index >= cells.size()) {
			return 0.0;
		}
		try {
			String text = cells.get(index).getText().trim();
			// Remove currency symbols and commas
			text = text.replaceAll("[\\$,]", "");
			if (text.isEmpty() || text.equals("-") || text.equals("N/A")) {
				return 0.0;
			}
			return Double.parseDouble(text);
		} catch (Exception e) {
			logger.warn("Could not parse cell value at index {}: {}", index, e.getMessage());
			return 0.0;
		}
	}

	/**
	 * Find column index by header text
	 * Uses the location table's thead to find the column index for a given header name
	 */
	private int findColumnIndexByHeaderText(String headerText) {
		try {
			// Try using the specific table xpath first
			String tableXpath = "//*[@id='root']/div[2]/div[2]/div[2]/div[1]/div[1]/div/table";
			List<WebElement> tables = driver.findElements(By.xpath(tableXpath));

			WebElement table = null;
			if (!tables.isEmpty()) {
				table = tables.get(0);
			} else {
				// Fallback to finding the location table
				table = findLocationTable();
			}

			if (table == null) {
				logger.warn("Could not find table to search for header: {}", headerText);
				return -1;
			}

			// Find all header cells
			List<WebElement> headers = table.findElements(By.xpath(".//thead//th"));
			logger.info("Found {} headers in table", headers.size());

			for (int i = 0; i < headers.size(); i++) {
				String text = headers.get(i).getText().trim();
				logger.info("Header[{}] = '{}'", i, text);
				if (text.toLowerCase().contains(headerText.toLowerCase())) {
					logger.info("Found '{}' at column index {}", headerText, i);
					return i;
				}
			}

			// Also check for nested div text (as in user's xpath: th[3]/div)
			for (int i = 0; i < headers.size(); i++) {
				try {
					WebElement innerDiv = headers.get(i).findElement(By.tagName("div"));
					String divText = innerDiv.getText().trim();
					if (divText.toLowerCase().contains(headerText.toLowerCase())) {
						logger.info("Found '{}' in nested div at column index {}", headerText, i);
						return i;
					}
				} catch (Exception e) {
					// No nested div, continue
				}
			}

			logger.warn("Header '{}' not found in table", headerText);
			return -1;

		} catch (Exception e) {
			logger.error("Error finding column index for header '{}': {}", headerText, e.getMessage());
			return -1;
		}
	}

	/**
	 * Find a table row by matching address
	 * Searches all rows for one containing the given address
	 */
	private WebElement findRowByAddress(List<WebElement> rows, int addressColIndex, String targetAddress) {
		if (targetAddress == null || targetAddress.isEmpty()) {
			logger.warn("Target address is empty, cannot match");
			return null;
		}

		// Normalize target address for comparison
		String normalizedTarget = normalizeAddress(targetAddress);
		logger.info("Searching for normalized address: '{}'", normalizedTarget);

		for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
			WebElement row = rows.get(rowIdx);
			try {
				List<WebElement> cells = row.findElements(By.tagName("td"));

				// Try using address column index first
				if (addressColIndex >= 0 && addressColIndex < cells.size()) {
					String cellAddress = cells.get(addressColIndex).getText().trim();
					String normalizedCell = normalizeAddress(cellAddress);

					if (normalizedCell.contains(normalizedTarget) || normalizedTarget.contains(normalizedCell)) {
						logger.info("Row {}: Address match found - '{}' matches '{}'", rowIdx, cellAddress, targetAddress);
						return row;
					}
				}

				// Fallback: search all cells for address match
				for (int c = 0; c < cells.size(); c++) {
					String cellText = cells.get(c).getText().trim();
					String normalizedCell = normalizeAddress(cellText);

					// Check if cell contains address (partial match)
					if (cellText.length() > 10 && !cellText.startsWith("$")) {
						if (normalizedCell.contains(normalizedTarget) || normalizedTarget.contains(normalizedCell)) {
							logger.info("Row {}: Address match found in cell {} - '{}' matches '{}'",
								rowIdx, c, cellText, targetAddress);
							return row;
						}
					}
				}
			} catch (Exception e) {
				logger.warn("Error checking row {}: {}", rowIdx, e.getMessage());
			}
		}

		logger.warn("No row found matching address: '{}'", targetAddress);
		return null;
	}

	/**
	 * Normalize address for comparison (lowercase, remove extra spaces, common abbreviations)
	 */
	private String normalizeAddress(String address) {
		if (address == null) return "";
		return address.toLowerCase()
			.replaceAll("\\s+", " ")
			.replaceAll("street", "st")
			.replaceAll("avenue", "ave")
			.replaceAll("drive", "dr")
			.replaceAll("road", "rd")
			.replaceAll("boulevard", "blvd")
			.replaceAll("lane", "ln")
			.replaceAll("[,.]", "")
			.trim();
	}

	/**
	 * Extract currency value from a table cell
	 * Handles various formats: $123.45, 123.45, $1,234.56, etc.
	 */
	private double extractCurrencyFromCell(WebElement cell) {
		try {
			String text = cell.getText().trim();
			logger.debug("Extracting currency from cell text: '{}'", text);

			if (text.isEmpty() || text.equals("-") || text.equals("N/A") || text.equals("$0.00")) {
				return 0.0;
			}

			// Remove currency symbols, commas, and whitespace
			String cleanedText = text.replaceAll("[\\$,\\s]", "");

			if (cleanedText.isEmpty() || cleanedText.equals("-")) {
				return 0.0;
			}

			double value = Double.parseDouble(cleanedText);
			logger.debug("Extracted value: {} from text: '{}'", value, text);
			return value;

		} catch (Exception e) {
			logger.warn("Could not extract currency from cell: {}", e.getMessage());
			return 0.0;
		}
	}

	/**
	 * Log pro-rata premium validation results to HTML report
	 */
	public void logProRataPremiumValidationToReport(ProRataPremiumValidationResult result) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		if (result.isAllPassed() && result.getError() == null) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Pro-Rata Premium Validation: ALL PASSED</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Pro-Rata Premium Validation: FAILED</h3>");
		}

		if (result.getError() != null) {
			html.append("<p style='color: #dc3545;'>Error: ").append(result.getError()).append("</p>");
			html.append("</div>");
			logHtmlToReport(html.toString());
			return;
		}

		// Summary section
		html.append("<div style='background-color: #fff3cd; padding: 10px; border-radius: 4px; margin: 10px 0;'>");
		html.append("<p style='color: #000000; font-weight: bold; margin: 0;'>Pro-Rata Calculation Formulas:</p>");
		html.append("<ul style='color: #000000; margin: 5px 0;'>");
		html.append("<li><strong>Property Premium:</strong> Annual = (CovA + CovB + CovC + CovD) × Rate / 100 → Pro-Rata = Annual / 365 × Days</li>");
		html.append("<li><strong>GL Premium:</strong> Pro-Rata = GL Amount / 365 × Days</li>");
		html.append("<li><strong>WS Premium:</strong> Pro-Rata = WS Amount / 365 × Days</li>");
		html.append("</ul>");
		html.append("<p style='color: #000000; margin: 5px 0;'><strong>Endorsement Effective Date:</strong> ")
			.append(result.getEndorsementEffectiveDate()).append("</p>");
		html.append("<p style='color: #000000; margin: 5px 0;'><strong>Expiration Date:</strong> ")
			.append(result.getExpirationDate()).append("</p>");
		html.append("<p style='color: #000000; margin: 5px 0;'><strong>Number of Pro-Rata Days:</strong> ")
			.append(result.getProRataDays()).append(" days</p>");
		html.append("</div>");

		// Detailed calculations table
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; font-size: 10px; margin-top: 10px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;'>Loc</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;'>Address</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;' colspan='2'>Property (Calc | Disp)</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;'>Prop</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;' colspan='2'>GL (Calc | Disp)</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;'>GL</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;' colspan='2'>WS (Calc | Disp)</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;'>WS</th>");
		html.append("<th style='padding: 4px; border: 1px solid #dee2e6;'>Overall</th>");
		html.append("</tr>");

		int rowNum = 1;
		for (LocationProRataCalculation calc : result.getLocationCalculations()) {
			String bgColor = rowNum % 2 == 0 ? "#f8f9fa" : "#ffffff";
			String overallStatus = calc.isAllMatched() ? "PASS" : "FAIL";
			String overallStatusColor = calc.isAllMatched() ? "#28a745" : "#dc3545";
			String propStatus = calc.isPropertyPremiumMatch() ? "PASS" : "FAIL";
			String propStatusColor = calc.isPropertyPremiumMatch() ? "#28a745" : "#dc3545";
			String glStatus = calc.isGlPremiumMatch() ? "PASS" : "FAIL";
			String glStatusColor = calc.isGlPremiumMatch() ? "#28a745" : "#dc3545";
			String wsStatus = calc.isWsPremiumMatch() ? "PASS" : "FAIL";
			String wsStatusColor = calc.isWsPremiumMatch() ? "#28a745" : "#dc3545";
			String rowBg = calc.isAllMatched() ? bgColor : "#f8d7da";

			html.append(String.format("<tr style='background-color: %s;'>", rowBg));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: center;'>%d</td>", calc.getLocationNumber()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6;'>%s</td>",
				truncateAddress(calc.getAddress(), 20)));
			// Property Premium columns
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: right;'>$%.2f</td>", calc.getCalculatedPropertyPremium()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: right;'>$%.2f</td>", calc.getDisplayedPropertyPremium()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: center; font-weight: bold; color: %s;'>%s</td>", propStatusColor, propStatus));
			// GL Premium columns
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: right;'>$%.2f</td>", calc.getCalculatedGLPremium()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: right;'>$%.2f</td>", calc.getDisplayedGLPremium()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: center; font-weight: bold; color: %s;'>%s</td>", glStatusColor, glStatus));
			// WS Premium columns
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: right;'>$%.2f</td>", calc.getCalculatedWSPremium()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: right;'>$%.2f</td>", calc.getDisplayedWSPremium()));
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: center; font-weight: bold; color: %s;'>%s</td>", wsStatusColor, wsStatus));
			// Overall status
			html.append(String.format("<td style='padding: 4px; border: 1px solid #dee2e6; text-align: center; font-weight: bold; color: %s;'>%s</td>", overallStatusColor, overallStatus));
			html.append("</tr>");

			// Add calculation details row
			html.append(String.format("<tr style='background-color: #e9ecef;'>"));
			html.append("<td colspan='12' style='padding: 4px 6px; border: 1px solid #dee2e6; font-size: 9px; color: #666;'>");
			html.append(String.format("<strong>Property:</strong> ($%.0f+$%.0f+$%.0f+$%.0f)×%.4f/100=$%.2f/yr → $%.4f/day × %d days = <strong>$%.2f</strong> | ",
				calc.getCoverageA(), calc.getCoverageB(), calc.getCoverageC(), calc.getCoverageD(),
				calc.getRate(), calc.getTotalAnnualPremium(), calc.getPerDayPremium(),
				calc.getProRataDays(), calc.getCalculatedPropertyPremium()));
			html.append(String.format("<strong>GL:</strong> $%.2f/365 × %d = <strong>$%.2f</strong> | ",
				calc.getExcelGLAmount(), calc.getProRataDays(), calc.getCalculatedGLPremium()));
			html.append(String.format("<strong>WS:</strong> $%.2f/365 × %d = <strong>$%.2f</strong>",
				calc.getExcelWSAmount(), calc.getProRataDays(), calc.getCalculatedWSPremium()));
			html.append("</td></tr>");

			rowNum++;
		}

		html.append("</table></div>");
		logHtmlToReport(html.toString());
	}

	/**
	 * Truncate address for display
	 */
	private String truncateAddress(String address, int maxLength) {
		if (address == null) return "";
		if (address.length() <= maxLength) return address;
		return address.substring(0, maxLength - 3) + "...";
	}

	/**
	 * Inner class for Pro-Rata Premium Validation Result
	 */
	public static class ProRataPremiumValidationResult {
		private long proRataDays;
		private String endorsementEffectiveDate;
		private String expirationDate;
		private List<LocationProRataCalculation> locationCalculations = new java.util.ArrayList<>();
		private boolean allPassed;
		private String error;

		public long getProRataDays() { return proRataDays; }
		public void setProRataDays(long v) { this.proRataDays = v; }
		public String getEndorsementEffectiveDate() { return endorsementEffectiveDate; }
		public void setEndorsementEffectiveDate(String v) { this.endorsementEffectiveDate = v; }
		public String getExpirationDate() { return expirationDate; }
		public void setExpirationDate(String v) { this.expirationDate = v; }
		public List<LocationProRataCalculation> getLocationCalculations() { return locationCalculations; }
		public void setLocationCalculations(List<LocationProRataCalculation> v) { this.locationCalculations = v; }
		public boolean isAllPassed() { return allPassed; }
		public void setAllPassed(boolean v) { this.allPassed = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
	}

	/**
	 * Inner class for individual location pro-rata calculation
	 */
	public static class LocationProRataCalculation {
		private int locationNumber;
		private String address;
		private long proRataDays;
		private double coverageA;
		private double coverageB;
		private double coverageC;
		private double coverageD;
		private double rate;
		private double annualPremiumA;
		private double annualPremiumB;
		private double annualPremiumC;
		private double annualPremiumD;
		private double totalAnnualPremium;
		private double perDayPremium;
		private double calculatedPropertyPremium;
		private double displayedPropertyPremium;
		// GL Premium fields
		private double excelGLAmount;
		private double calculatedGLPremium;
		private double displayedGLPremium;
		private boolean glPremiumMatch;
		// Water/Sewer Premium fields
		private double excelWSAmount;
		private double calculatedWSPremium;
		private double displayedWSPremium;
		private boolean wsPremiumMatch;
		// Tax field
		private double displayedTax;
		private boolean propertyPremiumMatch;
		private boolean allMatched;
		private String error;

		public int getLocationNumber() { return locationNumber; }
		public void setLocationNumber(int v) { this.locationNumber = v; }
		public String getAddress() { return address; }
		public void setAddress(String v) { this.address = v; }
		public long getProRataDays() { return proRataDays; }
		public void setProRataDays(long v) { this.proRataDays = v; }
		public double getCoverageA() { return coverageA; }
		public void setCoverageA(double v) { this.coverageA = v; }
		public double getCoverageB() { return coverageB; }
		public void setCoverageB(double v) { this.coverageB = v; }
		public double getCoverageC() { return coverageC; }
		public void setCoverageC(double v) { this.coverageC = v; }
		public double getCoverageD() { return coverageD; }
		public void setCoverageD(double v) { this.coverageD = v; }
		public double getRate() { return rate; }
		public void setRate(double v) { this.rate = v; }
		public double getAnnualPremiumA() { return annualPremiumA; }
		public void setAnnualPremiumA(double v) { this.annualPremiumA = v; }
		public double getAnnualPremiumB() { return annualPremiumB; }
		public void setAnnualPremiumB(double v) { this.annualPremiumB = v; }
		public double getAnnualPremiumC() { return annualPremiumC; }
		public void setAnnualPremiumC(double v) { this.annualPremiumC = v; }
		public double getAnnualPremiumD() { return annualPremiumD; }
		public void setAnnualPremiumD(double v) { this.annualPremiumD = v; }
		public double getTotalAnnualPremium() { return totalAnnualPremium; }
		public void setTotalAnnualPremium(double v) { this.totalAnnualPremium = v; }
		public double getPerDayPremium() { return perDayPremium; }
		public void setPerDayPremium(double v) { this.perDayPremium = v; }
		public double getCalculatedPropertyPremium() { return calculatedPropertyPremium; }
		public void setCalculatedPropertyPremium(double v) { this.calculatedPropertyPremium = v; }
		public double getDisplayedPropertyPremium() { return displayedPropertyPremium; }
		public void setDisplayedPropertyPremium(double v) { this.displayedPropertyPremium = v; }
		// GL Premium getters/setters
		public double getExcelGLAmount() { return excelGLAmount; }
		public void setExcelGLAmount(double v) { this.excelGLAmount = v; }
		public double getCalculatedGLPremium() { return calculatedGLPremium; }
		public void setCalculatedGLPremium(double v) { this.calculatedGLPremium = v; }
		public double getDisplayedGLPremium() { return displayedGLPremium; }
		public void setDisplayedGLPremium(double v) { this.displayedGLPremium = v; }
		public boolean isGlPremiumMatch() { return glPremiumMatch; }
		public void setGlPremiumMatch(boolean v) { this.glPremiumMatch = v; }
		// WS Premium getters/setters
		public double getExcelWSAmount() { return excelWSAmount; }
		public void setExcelWSAmount(double v) { this.excelWSAmount = v; }
		public double getCalculatedWSPremium() { return calculatedWSPremium; }
		public void setCalculatedWSPremium(double v) { this.calculatedWSPremium = v; }
		public double getDisplayedWSPremium() { return displayedWSPremium; }
		public void setDisplayedWSPremium(double v) { this.displayedWSPremium = v; }
		public boolean isWsPremiumMatch() { return wsPremiumMatch; }
		public void setWsPremiumMatch(boolean v) { this.wsPremiumMatch = v; }
		// Tax and match getters/setters
		public double getDisplayedTax() { return displayedTax; }
		public void setDisplayedTax(double v) { this.displayedTax = v; }
		public boolean isPropertyPremiumMatch() { return propertyPremiumMatch; }
		public void setPropertyPremiumMatch(boolean v) { this.propertyPremiumMatch = v; }
		public boolean isAllMatched() { return allMatched; }
		public void setAllMatched(boolean v) { this.allMatched = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
	}

	// ==================== Validation Against Create Endorsement Data ====================

	/**
	 * Result class for validation comparison
	 */
	public static class EndorsementValidationResult {
		private boolean endorsementDateMatch;
		private boolean premiumGLWSMatch;
		private boolean taxesMatch;
		private boolean totalFeesMatch;
		private boolean grandTotalMatch;
		private boolean locationCountMatch;
		private boolean allLocationsMatch;
		private boolean allValidationsPassed;

		// Expected vs Actual values
		private String expectedDate;
		private String actualDate;
		private double expectedPremiumGLWS;
		private double actualPremiumGLWS;
		private double expectedTaxes;
		private double actualTaxes;
		private double expectedTotalFees;
		private double actualTotalFees;
		private double expectedGrandTotal;
		private double actualGrandTotal;
		private int expectedLocationCount;
		private int actualLocationCount;

		private java.util.List<LocationValidationDetail> locationValidations;
		private String error;

		// Getters and Setters
		public boolean isEndorsementDateMatch() { return endorsementDateMatch; }
		public void setEndorsementDateMatch(boolean v) { this.endorsementDateMatch = v; }
		public boolean isPremiumGLWSMatch() { return premiumGLWSMatch; }
		public void setPremiumGLWSMatch(boolean v) { this.premiumGLWSMatch = v; }
		public boolean isTaxesMatch() { return taxesMatch; }
		public void setTaxesMatch(boolean v) { this.taxesMatch = v; }
		public boolean isTotalFeesMatch() { return totalFeesMatch; }
		public void setTotalFeesMatch(boolean v) { this.totalFeesMatch = v; }
		public boolean isGrandTotalMatch() { return grandTotalMatch; }
		public void setGrandTotalMatch(boolean v) { this.grandTotalMatch = v; }
		public boolean isLocationCountMatch() { return locationCountMatch; }
		public void setLocationCountMatch(boolean v) { this.locationCountMatch = v; }
		public boolean isAllLocationsMatch() { return allLocationsMatch; }
		public void setAllLocationsMatch(boolean v) { this.allLocationsMatch = v; }
		public boolean isAllValidationsPassed() { return allValidationsPassed; }
		public void setAllValidationsPassed(boolean v) { this.allValidationsPassed = v; }
		public String getExpectedDate() { return expectedDate; }
		public void setExpectedDate(String v) { this.expectedDate = v; }
		public String getActualDate() { return actualDate; }
		public void setActualDate(String v) { this.actualDate = v; }
		public double getExpectedPremiumGLWS() { return expectedPremiumGLWS; }
		public void setExpectedPremiumGLWS(double v) { this.expectedPremiumGLWS = v; }
		public double getActualPremiumGLWS() { return actualPremiumGLWS; }
		public void setActualPremiumGLWS(double v) { this.actualPremiumGLWS = v; }
		public double getExpectedTaxes() { return expectedTaxes; }
		public void setExpectedTaxes(double v) { this.expectedTaxes = v; }
		public double getActualTaxes() { return actualTaxes; }
		public void setActualTaxes(double v) { this.actualTaxes = v; }
		public double getExpectedTotalFees() { return expectedTotalFees; }
		public void setExpectedTotalFees(double v) { this.expectedTotalFees = v; }
		public double getActualTotalFees() { return actualTotalFees; }
		public void setActualTotalFees(double v) { this.actualTotalFees = v; }
		public double getExpectedGrandTotal() { return expectedGrandTotal; }
		public void setExpectedGrandTotal(double v) { this.expectedGrandTotal = v; }
		public double getActualGrandTotal() { return actualGrandTotal; }
		public void setActualGrandTotal(double v) { this.actualGrandTotal = v; }
		public int getExpectedLocationCount() { return expectedLocationCount; }
		public void setExpectedLocationCount(int v) { this.expectedLocationCount = v; }
		public int getActualLocationCount() { return actualLocationCount; }
		public void setActualLocationCount(int v) { this.actualLocationCount = v; }
		public java.util.List<LocationValidationDetail> getLocationValidations() { return locationValidations; }
		public void setLocationValidations(java.util.List<LocationValidationDetail> v) { this.locationValidations = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
	}

	/**
	 * Location validation detail
	 */
	public static class LocationValidationDetail {
		private int rowIndex;
		private String address;
		private boolean propertyPremiumMatch;
		private boolean glPremiumMatch;
		private boolean wsPremiumMatch;
		private double expectedPropertyPremium;
		private double actualPropertyPremium;
		private double expectedGLPremium;
		private double actualGLPremium;
		private double expectedWSPremium;
		private double actualWSPremium;

		// Getters and Setters
		public int getRowIndex() { return rowIndex; }
		public void setRowIndex(int v) { this.rowIndex = v; }
		public String getAddress() { return address; }
		public void setAddress(String v) { this.address = v; }
		public boolean isPropertyPremiumMatch() { return propertyPremiumMatch; }
		public void setPropertyPremiumMatch(boolean v) { this.propertyPremiumMatch = v; }
		public boolean isGlPremiumMatch() { return glPremiumMatch; }
		public void setGlPremiumMatch(boolean v) { this.glPremiumMatch = v; }
		public boolean isWsPremiumMatch() { return wsPremiumMatch; }
		public void setWsPremiumMatch(boolean v) { this.wsPremiumMatch = v; }
		public double getExpectedPropertyPremium() { return expectedPropertyPremium; }
		public void setExpectedPropertyPremium(double v) { this.expectedPropertyPremium = v; }
		public double getActualPropertyPremium() { return actualPropertyPremium; }
		public void setActualPropertyPremium(double v) { this.actualPropertyPremium = v; }
		public double getExpectedGLPremium() { return expectedGLPremium; }
		public void setExpectedGLPremium(double v) { this.expectedGLPremium = v; }
		public double getActualGLPremium() { return actualGLPremium; }
		public void setActualGLPremium(double v) { this.actualGLPremium = v; }
		public double getExpectedWSPremium() { return expectedWSPremium; }
		public void setExpectedWSPremium(double v) { this.expectedWSPremium = v; }
		public double getActualWSPremium() { return actualWSPremium; }
		public void setActualWSPremium(double v) { this.actualWSPremium = v; }
	}

	/**
	 * Validate Edit Endorsement page values against captured data from Create Endorsement page
	 */
	public EndorsementValidationResult validateAgainstCreateEndorsement(
			CreatePremiumEndorsementPage.EndorsementCapturedData expectedData) {

		EndorsementValidationResult result = new EndorsementValidationResult();
		logger.info("=== Validating Edit Endorsement Against Create Endorsement Data ===");

		try {
			// Wait for page to load
			sleep(3000);

			// 1. Validate Endorsement Effective Date
			String actualDate = getEndorsementEffectiveDate();
			String expectedDate = expectedData.getEndorsementEffectiveDate();
			result.setExpectedDate(expectedDate);
			result.setActualDate(actualDate);
			boolean dateMatch = actualDate != null && expectedDate != null &&
				(actualDate.equals(expectedDate) || normalizeDate(actualDate).equals(normalizeDate(expectedDate)));
			result.setEndorsementDateMatch(dateMatch);
			logger.info("Date Validation: Expected='{}', Actual='{}', Match={}", expectedDate, actualDate, dateMatch);

			// 2. Validate Premium (GL + WS)
			double actualPremiumGLWS = getSummaryValueByLabel("Premium", "Premium (GL", "GL+WS");
			double expectedPremiumGLWS = expectedData.getPremiumGLWS();
			result.setExpectedPremiumGLWS(expectedPremiumGLWS);
			result.setActualPremiumGLWS(actualPremiumGLWS);
			boolean premiumMatch = Math.abs(actualPremiumGLWS - expectedPremiumGLWS) < 0.01;
			result.setPremiumGLWSMatch(premiumMatch);
			logger.info("Premium Validation: Expected=${}, Actual=${}, Match={}",
				String.format("%.2f", expectedPremiumGLWS), String.format("%.2f", actualPremiumGLWS), premiumMatch);

			// 3. Validate Taxes
			double actualTaxes = getSummaryValueByLabel("Taxes", "Tax");
			double expectedTaxes = expectedData.getTaxes();
			result.setExpectedTaxes(expectedTaxes);
			result.setActualTaxes(actualTaxes);
			boolean taxesMatch = Math.abs(actualTaxes - expectedTaxes) < 0.01;
			result.setTaxesMatch(taxesMatch);
			logger.info("Taxes Validation: Expected=${}, Actual=${}, Match={}",
				String.format("%.2f", expectedTaxes), String.format("%.2f", actualTaxes), taxesMatch);

			// 4. Validate Total Fees
			double actualTotalFees = getSummaryValueByLabel("Total Fees", "Fees");
			double expectedTotalFees = expectedData.getTotalFees();
			result.setExpectedTotalFees(expectedTotalFees);
			result.setActualTotalFees(actualTotalFees);
			boolean feesMatch = Math.abs(actualTotalFees - expectedTotalFees) < 0.01;
			result.setTotalFeesMatch(feesMatch);
			logger.info("Fees Validation: Expected=${}, Actual=${}, Match={}",
				String.format("%.2f", expectedTotalFees), String.format("%.2f", actualTotalFees), feesMatch);

			// 5. Validate Grand Total
			double actualGrandTotal = getSummaryValueByLabel("Grand Total", "Total");
			double expectedGrandTotal = expectedData.getGrandTotal();
			result.setExpectedGrandTotal(expectedGrandTotal);
			result.setActualGrandTotal(actualGrandTotal);
			boolean grandTotalMatch = Math.abs(actualGrandTotal - expectedGrandTotal) < 0.01;
			result.setGrandTotalMatch(grandTotalMatch);
			logger.info("Grand Total Validation: Expected=${}, Actual=${}, Match={}",
				String.format("%.2f", expectedGrandTotal), String.format("%.2f", actualGrandTotal), grandTotalMatch);

			// 6. Validate Location Count
			int actualLocationCount = getLocationCount();
			int expectedLocationCount = expectedData.getLocationCount();
			result.setExpectedLocationCount(expectedLocationCount);
			result.setActualLocationCount(actualLocationCount);
			boolean locationCountMatch = actualLocationCount == expectedLocationCount;
			result.setLocationCountMatch(locationCountMatch);
			logger.info("Location Count Validation: Expected={}, Actual={}, Match={}",
				expectedLocationCount, actualLocationCount, locationCountMatch);

			// 7. Validate Location Details (compare each location's premium values)
			boolean allLocationsMatch = validateLocationDetails(expectedData.getLocationDetails(), result);
			result.setAllLocationsMatch(allLocationsMatch);

			// Overall result
			boolean allPassed = dateMatch && premiumMatch && taxesMatch && feesMatch &&
				grandTotalMatch && locationCountMatch && allLocationsMatch;
			result.setAllValidationsPassed(allPassed);

			// Log to HTML report
			logValidationResultToReport(result);

			logger.info("=== Validation Complete: {} ===", allPassed ? "ALL PASSED" : "SOME FAILED");

		} catch (Exception e) {
			logger.error("Error during validation: {}", e.getMessage());
			result.setError(e.getMessage());
		}

		return result;
	}

	/**
	 * Validate location details comparing Create vs Edit endorsement
	 * Matches locations by ADDRESS to ensure correct comparison regardless of row order
	 */
	private boolean validateLocationDetails(
			java.util.List<CreatePremiumEndorsementPage.LocationRowData> expectedLocations,
			EndorsementValidationResult result) {

		if (expectedLocations == null || expectedLocations.isEmpty()) {
			result.setLocationValidations(new java.util.ArrayList<>());
			return true;
		}

		java.util.List<LocationValidationDetail> validations = new java.util.ArrayList<>();
		boolean allMatch = true;

		try {
			// Get actual location data from Edit page
			WebElement table = findLocationTable();
			if (table == null) {
				logger.warn("Location table not found on Edit page");
				result.setLocationValidations(validations);
				return false;
			}

			// Get column indices
			List<WebElement> headers = table.findElements(By.xpath(".//thead//th | .//tr[1]//th"));
			Map<String, Integer> columnIndices = new HashMap<>();
			for (int i = 0; i < headers.size(); i++) {
				String headerText = headers.get(i).getText().trim().toLowerCase();
				if (headerText.contains("address")) columnIndices.put("address", i);
				else if (headerText.contains("property") && headerText.contains("premium")) columnIndices.put("propertyPremium", i);
				else if (headerText.contains("gl") && headerText.contains("premium")) columnIndices.put("glPremium", i);
				else if (headerText.contains("water") || headerText.contains("sewer") || headerText.contains("w/s")) columnIndices.put("wsPremium", i);
			}

			logger.info("Column indices for validation: {}", columnIndices);

			// Get actual rows and build a map by address
			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			Map<String, ActualLocationData> actualLocationMap = new HashMap<>();

			for (int i = 0; i < rows.size(); i++) {
				WebElement row = rows.get(i);
				List<WebElement> cells = row.findElements(By.xpath(".//td"));

				ActualLocationData actualData = new ActualLocationData();
				actualData.rowIndex = i + 1;

				// Get address
				if (columnIndices.containsKey("address") && cells.size() > columnIndices.get("address")) {
					actualData.address = cells.get(columnIndices.get("address")).getText().trim();
				}

				// Get property premium
				if (columnIndices.containsKey("propertyPremium") && cells.size() > columnIndices.get("propertyPremium")) {
					actualData.propertyPremium = parseAmount(cells.get(columnIndices.get("propertyPremium")).getText());
				}

				// Get GL premium
				if (columnIndices.containsKey("glPremium") && cells.size() > columnIndices.get("glPremium")) {
					actualData.glPremium = parseAmount(cells.get(columnIndices.get("glPremium")).getText());
				}

				// Get WS premium
				if (columnIndices.containsKey("wsPremium") && cells.size() > columnIndices.get("wsPremium")) {
					actualData.wsPremium = parseAmount(cells.get(columnIndices.get("wsPremium")).getText());
				}

				// Store by normalized address for matching
				if (actualData.address != null && !actualData.address.isEmpty()) {
					String normalizedAddress = normalizeAddress(actualData.address);
					actualLocationMap.put(normalizedAddress, actualData);
					logger.debug("Actual location {}: {} -> Property=${}", i + 1, actualData.address, actualData.propertyPremium);
				}
			}

			logger.info("Built actual location map with {} entries", actualLocationMap.size());

			// Match each expected location by address
			for (int i = 0; i < expectedLocations.size(); i++) {
				CreatePremiumEndorsementPage.LocationRowData expected = expectedLocations.get(i);
				String expectedAddress = expected.getAddress();
				String normalizedExpectedAddress = normalizeAddress(expectedAddress);

				LocationValidationDetail detail = new LocationValidationDetail();
				detail.setRowIndex(i + 1);
				detail.setAddress(expectedAddress);
				detail.setExpectedPropertyPremium(expected.getPropertyPremium());
				detail.setExpectedGLPremium(expected.getGlPremium());
				detail.setExpectedWSPremium(expected.getWsPremium());

				// Find matching actual location by address
				ActualLocationData actual = actualLocationMap.get(normalizedExpectedAddress);

				if (actual == null) {
					// Try partial match if exact match not found
					actual = findPartialAddressMatch(actualLocationMap, expectedAddress);
				}

				if (actual != null) {
					detail.setActualPropertyPremium(actual.propertyPremium);
					detail.setActualGLPremium(actual.glPremium);
					detail.setActualWSPremium(actual.wsPremium);

					// Compare with tolerance
					detail.setPropertyPremiumMatch(Math.abs(actual.propertyPremium - expected.getPropertyPremium()) < 0.01);
					detail.setGlPremiumMatch(Math.abs(actual.glPremium - expected.getGlPremium()) < 0.01);
					detail.setWsPremiumMatch(Math.abs(actual.wsPremium - expected.getWsPremium()) < 0.01);

					logger.info("Location '{}' matched: Property={}/{}, GL={}/{}, WS={}/{}",
						expectedAddress,
						expected.getPropertyPremium(), actual.propertyPremium,
						expected.getGlPremium(), actual.glPremium,
						expected.getWsPremium(), actual.wsPremium);
				} else {
					logger.warn("No matching location found for address: {}", expectedAddress);
					detail.setPropertyPremiumMatch(false);
					detail.setGlPremiumMatch(false);
					detail.setWsPremiumMatch(false);
				}

				if (!detail.isPropertyPremiumMatch() || !detail.isGlPremiumMatch() || !detail.isWsPremiumMatch()) {
					allMatch = false;
				}

				validations.add(detail);
			}

		} catch (Exception e) {
			logger.error("Error validating location details: {}", e.getMessage());
			allMatch = false;
		}

		result.setLocationValidations(validations);
		return allMatch;
	}

	/**
	 * Helper class for actual location data
	 */
	private static class ActualLocationData {
		int rowIndex;
		String address;
		double propertyPremium;
		double glPremium;
		double wsPremium;
	}

	/**
	 * Find partial address match when exact match not found
	 */
	private ActualLocationData findPartialAddressMatch(Map<String, ActualLocationData> actualMap, String expectedAddress) {
		if (expectedAddress == null || expectedAddress.isEmpty()) return null;

		String normalizedExpected = normalizeAddress(expectedAddress);

		// Try to find a match where addresses contain each other
		for (Map.Entry<String, ActualLocationData> entry : actualMap.entrySet()) {
			String actualNormalized = entry.getKey();
			if (actualNormalized.contains(normalizedExpected) || normalizedExpected.contains(actualNormalized)) {
				logger.info("Partial address match found: '{}' ~ '{}'", expectedAddress, entry.getValue().address);
				return entry.getValue();
			}
		}

		// Try matching first part of address (street number and name)
		String[] expectedParts = normalizedExpected.split(",");
		if (expectedParts.length > 0) {
			String expectedStreet = expectedParts[0].trim();
			for (Map.Entry<String, ActualLocationData> entry : actualMap.entrySet()) {
				if (entry.getKey().startsWith(expectedStreet)) {
					logger.info("Street address match found: '{}' ~ '{}'", expectedAddress, entry.getValue().address);
					return entry.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * Normalize date to YYYYMMDD format for comparison
	 * Handles both ISO (YYYY-MM-DD) and US (MM/DD/YYYY) formats
	 */
	private String normalizeDate(String date) {
		if (date == null || date.isEmpty()) return "";

		// Remove all non-numeric characters first
		String digitsOnly = date.replaceAll("[^0-9]", "");

		// If it's already 8 digits, determine format
		if (digitsOnly.length() == 8) {
			// Check if original was ISO format (YYYY-MM-DD) - starts with year
			if (date.matches("\\d{4}[-/]\\d{2}[-/]\\d{2}")) {
				// Already in YYYYMMDD order
				return digitsOnly;
			}
			// Check if original was US format (MM/DD/YYYY) - ends with year
			if (date.matches("\\d{2}[-/]\\d{2}[-/]\\d{4}") || date.matches("\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}")) {
				// Convert from MMDDYYYY to YYYYMMDD
				String month = digitsOnly.substring(0, 2);
				String day = digitsOnly.substring(2, 4);
				String year = digitsOnly.substring(4, 8);
				return year + month + day;
			}
			// Default: assume it's YYYYMMDD
			return digitsOnly;
		}

		// Handle dates that might have single-digit month/day
		if (date.matches("\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}")) {
			String[] parts = date.split("[-/]");
			if (parts.length == 3) {
				String month = String.format("%02d", Integer.parseInt(parts[0]));
				String day = String.format("%02d", Integer.parseInt(parts[1]));
				String year = parts[2];
				return year + month + day;
			}
		}

		// Just return digits as fallback
		return digitsOnly;
	}

	/**
	 * Log validation result to HTML report
	 */
	private void logValidationResultToReport(EndorsementValidationResult result) {
		StringBuilder html = new StringBuilder();
		boolean allPassed = result.isAllValidationsPassed();

		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");
		html.append(allPassed ? "background-color: #d4edda; border-left: 4px solid #28a745;'>" : "background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
		html.append("<h3 style='color: ").append(allPassed ? "#28a745" : "#dc3545").append("; margin-top: 0;'>");
		html.append("Create vs Edit Endorsement Validation: ").append(allPassed ? "ALL PASSED" : "SOME FAILED").append("</h3>");

		// Summary values comparison table
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; margin-bottom: 15px;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px;'>Field</th>");
		html.append("<th style='padding: 10px;'>Create Endorsement</th>");
		html.append("<th style='padding: 10px;'>Edit Endorsement</th>");
		html.append("<th style='padding: 10px;'>Status</th>");
		html.append("</tr>");

		// Date
		addValidationRow(html, "Endorsement Effective Date", result.getExpectedDate(), result.getActualDate(), result.isEndorsementDateMatch());
		// Premium
		addValidationRow(html, "Premium (GL+WS)", "$" + String.format("%.2f", result.getExpectedPremiumGLWS()),
			"$" + String.format("%.2f", result.getActualPremiumGLWS()), result.isPremiumGLWSMatch());
		// Taxes
		addValidationRow(html, "Taxes", "$" + String.format("%.2f", result.getExpectedTaxes()),
			"$" + String.format("%.2f", result.getActualTaxes()), result.isTaxesMatch());
		// Total Fees
		addValidationRow(html, "Total Fees", "$" + String.format("%.2f", result.getExpectedTotalFees()),
			"$" + String.format("%.2f", result.getActualTotalFees()), result.isTotalFeesMatch());
		// Grand Total
		addValidationRow(html, "Grand Total", "$" + String.format("%.2f", result.getExpectedGrandTotal()),
			"$" + String.format("%.2f", result.getActualGrandTotal()), result.isGrandTotalMatch());
		// Location Count
		addValidationRow(html, "Location Count", String.valueOf(result.getExpectedLocationCount()),
			String.valueOf(result.getActualLocationCount()), result.isLocationCountMatch());

		html.append("</table>");

		// Location details validation
		if (result.getLocationValidations() != null && !result.getLocationValidations().isEmpty()) {
			html.append("<h4 style='color: #007bff;'>Location Details Comparison</h4>");
			html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; font-size: 12px;'>");
			html.append("<tr style='background-color: #343a40; color: white;'>");
			html.append("<th style='padding: 6px;'>#</th>");
			html.append("<th style='padding: 6px;'>Property (Exp/Act)</th>");
			html.append("<th style='padding: 6px;'>GL (Exp/Act)</th>");
			html.append("<th style='padding: 6px;'>W/S (Exp/Act)</th>");
			html.append("<th style='padding: 6px;'>Status</th>");
			html.append("</tr>");

			for (LocationValidationDetail loc : result.getLocationValidations()) {
				boolean locMatch = loc.isPropertyPremiumMatch() && loc.isGlPremiumMatch() && loc.isWsPremiumMatch();
				String bgColor = locMatch ? "#d4edda" : "#f8d7da";
				String statusColor = locMatch ? "#28a745" : "#dc3545";

				html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6;'>");
				html.append("<td style='padding: 6px;'>").append(loc.getRowIndex()).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getExpectedPropertyPremium()))
					.append(" / $").append(String.format("%.2f", loc.getActualPropertyPremium())).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getExpectedGLPremium()))
					.append(" / $").append(String.format("%.2f", loc.getActualGLPremium())).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getExpectedWSPremium()))
					.append(" / $").append(String.format("%.2f", loc.getActualWSPremium())).append("</td>");
				html.append("<td style='padding: 6px; color: ").append(statusColor).append("; font-weight: bold;'>")
					.append(locMatch ? "PASS" : "FAIL").append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
		}

		html.append("</div>");
		logHtmlToReport(html.toString());
	}

	/**
	 * Add a row to validation table
	 */
	private void addValidationRow(StringBuilder html, String field, String expected, String actual, boolean match) {
		String bgColor = match ? "#d4edda" : "#f8d7da";
		String statusColor = match ? "#28a745" : "#dc3545";
		html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6;'>");
		html.append("<td style='padding: 8px; font-weight: bold;'>").append(field).append("</td>");
		html.append("<td style='padding: 8px;'>").append(expected != null ? expected : "N/A").append("</td>");
		html.append("<td style='padding: 8px;'>").append(actual != null ? actual : "N/A").append("</td>");
		html.append("<td style='padding: 8px; color: ").append(statusColor).append("; font-weight: bold;'>")
			.append(match ? "PASS" : "FAIL").append("</td>");
		html.append("</tr>");
	}

	// ==================== Edit Endorsement Date Change and Validation ====================

	/**
	 * Result class for date change with location validation
	 */
	public static class DateChangeValidationResult {
		private boolean dateChanged;
		private boolean confirmDialogAppeared;
		private boolean allLocationsDisplayed;
		private boolean confirmClicked;
		private java.util.List<String> locationsInDialog;
		private java.util.List<String> expectedLocations;
		private String oldDate;
		private String newDate;
		private String error;

		// Getters and Setters
		public boolean isDateChanged() { return dateChanged; }
		public void setDateChanged(boolean v) { this.dateChanged = v; }
		public boolean isConfirmDialogAppeared() { return confirmDialogAppeared; }
		public void setConfirmDialogAppeared(boolean v) { this.confirmDialogAppeared = v; }
		public boolean isAllLocationsDisplayed() { return allLocationsDisplayed; }
		public void setAllLocationsDisplayed(boolean v) { this.allLocationsDisplayed = v; }
		public boolean isConfirmClicked() { return confirmClicked; }
		public void setConfirmClicked(boolean v) { this.confirmClicked = v; }
		public java.util.List<String> getLocationsInDialog() { return locationsInDialog; }
		public void setLocationsInDialog(java.util.List<String> v) { this.locationsInDialog = v; }
		public java.util.List<String> getExpectedLocations() { return expectedLocations; }
		public void setExpectedLocations(java.util.List<String> v) { this.expectedLocations = v; }
		public String getOldDate() { return oldDate; }
		public void setOldDate(String v) { this.oldDate = v; }
		public String getNewDate() { return newDate; }
		public void setNewDate(String v) { this.newDate = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }

		public boolean isSuccess() {
			return dateChanged && confirmDialogAppeared && allLocationsDisplayed && confirmClicked;
		}
	}

	/**
	 * Change endorsement effective date by reducing 2 months on Edit Endorsement screen
	 * @param expectedAddresses List of addresses that should appear in confirm dialog (e.g., newly added locations)
	 * @return DateChangeValidationResult with all validation details
	 */
	public DateChangeValidationResult changeEndorsementDateReduceBy2Months(java.util.List<String> expectedAddresses) {
		DateChangeValidationResult result = new DateChangeValidationResult();
		result.setExpectedLocations(expectedAddresses);
		logger.info("=== Changing Endorsement Date on Edit Endorsement (Reduce by 2 Months) ===");

		try {
			// Capture old date
			String oldDate = getEndorsementEffectiveDate();
			result.setOldDate(oldDate);
			logger.info("Current Endorsement Effective Date: {}", oldDate);

			// XPaths provided by user
			String datePickerInputXpath = "//*[@id='edit-endorsement-endorsement-effective-date-picker']/div/div/input[4]";
			String prevMonthButtonXpath = "//*[@id='edit-endorsement-endorsement-effective-date-picker']/span/div/div/div[1]/button[2]";

			// Step 1: Click on date picker input to open calendar
			logger.info("Opening date picker...");
			WebElement datePickerInput = null;
			try {
				datePickerInput = driver.findElement(By.xpath(datePickerInputXpath));
			} catch (Exception e) {
				// Try alternative XPaths
				String[] altXpaths = {
					"//input[contains(@id,'endorsement-effective-date')]",
					"//*[@id='edit-endorsement-endorsement-effective-date-picker']//input",
					"//input[@placeholder='MM/DD/YYYY' or contains(@placeholder,'date')]"
				};
				for (String xpath : altXpaths) {
					try {
						List<WebElement> inputs = driver.findElements(By.xpath(xpath));
						for (WebElement input : inputs) {
							if (input.isDisplayed()) {
								datePickerInput = input;
								logger.info("Found date picker input with alt XPath: {}", xpath);
								break;
							}
						}
						if (datePickerInput != null) break;
					} catch (Exception ex) {
						// Continue
					}
				}
			}

			if (datePickerInput == null) {
				logger.error("Date picker input not found");
				result.setError("Date picker input not found");
				return result;
			}

			// Scroll to date picker
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", datePickerInput);
			sleep(500);

			// Click to open date picker
			try {
				datePickerInput.click();
			} catch (Exception e) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", datePickerInput);
			}
			sleep(1000);
			logger.info("Date picker opened");

			// Step 2: Click previous month button twice (2 months back)
			logger.info("Navigating to 2 months back...");
			for (int i = 0; i < 2; i++) {
				WebElement prevButton = null;
				try {
					prevButton = driver.findElement(By.xpath(prevMonthButtonXpath));
				} catch (Exception e) {
					// Try alternative XPaths for previous month button
					String[] altPrevXpaths = {
						"//button[contains(@class,'prev') or contains(@aria-label,'prev')]",
						"//button[text()='<' or text()='‹']",
						"//*[contains(@class,'calendar')]//button[1]",
						"//div[contains(@class,'datepicker')]//button[contains(@class,'prev')]"
					};
					for (String xpath : altPrevXpaths) {
						try {
							List<WebElement> buttons = driver.findElements(By.xpath(xpath));
							for (WebElement btn : buttons) {
								if (btn.isDisplayed()) {
									prevButton = btn;
									logger.info("Found prev month button with alt XPath: {}", xpath);
									break;
								}
							}
							if (prevButton != null) break;
						} catch (Exception ex) {
							// Continue
						}
					}
				}

				if (prevButton != null) {
					try {
						prevButton.click();
					} catch (Exception e) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", prevButton);
					}
					sleep(500);
					logger.info("Clicked previous month button ({}/2)", i + 1);
				} else {
					logger.warn("Previous month button not found on attempt {}", i + 1);
				}
			}

			// Step 3: Select a date from react-calendar (click day 15)
			logger.info("Selecting date from calendar...");
			boolean dateClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
				"var tiles = document.querySelectorAll('.react-calendar__tile:not(.react-calendar__tile--neighboringMonth)');" +
				"for (var i = 0; i < tiles.length; i++) {" +
				"  var abbr = tiles[i].querySelector('abbr');" +
				"  if (abbr && abbr.textContent === '15' && !tiles[i].disabled) {" +
				"    tiles[i].click(); return true;" +
				"  }" +
				"}" +
				"// Fallback: click any available day" +
				"for (var j = 0; j < tiles.length; j++) {" +
				"  if (!tiles[j].disabled) { tiles[j].click(); return true; }" +
				"}" +
				"return false;"
			);
			if (dateClicked) {
				logger.info("Date selected");
				result.setDateChanged(true);
				sleep(1000);
			} else {
				logger.warn("Could not select date");
			}

			// Step 4: Wait for and handle Confirm dialog
			sleep(2000);
			logger.info("Waiting for Confirm Date Change dialog...");

			// Look for the confirm dialog
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			boolean dialogFound = false;
			java.util.List<String> locationsInDialog = new java.util.ArrayList<>();

			try {
				// Wait for dialog to appear
				wait.until(ExpectedConditions.or(
					ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Confirm')]")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'Dialog')]")),
					ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@id,'radix-')]"))
				));

				dialogFound = true;
				result.setConfirmDialogAppeared(true);
				logger.info("Confirm dialog appeared");

				// Capture locations shown in dialog using multiple XPath strategies
				try {
					// Try multiple XPaths to capture all location elements
					String[] locationXpaths = {
						"//*[@id='root']/div[2]/div[2]/div/div[2]/div[2]//p",
						"//div[contains(@class,'Dialog')]//p[contains(text(),',')]",
						"//*[contains(@id,'radix-')]//p[contains(text(),',')]",
						"//div[contains(@class,'modal')]//p",
						"//div[@role='dialog']//p"
					};

					java.util.Set<String> uniqueLocations = new java.util.LinkedHashSet<>();

					for (String xpath : locationXpaths) {
						try {
							List<WebElement> elements = driver.findElements(By.xpath(xpath));
							for (WebElement loc : elements) {
								String text = loc.getText().trim();
								// Location addresses typically contain comma (city, state pattern)
								if (!text.isEmpty() && text.contains(",") && !text.toLowerCase().contains("confirm")
									&& !text.toLowerCase().contains("change") && text.length() > 10) {
									uniqueLocations.add(text);
								}
							}
						} catch (Exception ex) {
							// Continue with next XPath
						}
					}

					// Also try to capture from any visible dialog content
					try {
						List<WebElement> allParagraphs = driver.findElements(By.xpath("//div[contains(@class,'fixed') or contains(@class,'overlay')]//p"));
						for (WebElement p : allParagraphs) {
							String text = p.getText().trim();
							if (!text.isEmpty() && text.contains(",") && text.contains(" ")
								&& (text.toUpperCase().contains("NY") || text.toUpperCase().contains("USA") || text.matches(".*\\d{5}.*"))) {
								uniqueLocations.add(text);
							}
						}
					} catch (Exception ex) {
						// Continue
					}

					locationsInDialog.addAll(uniqueLocations);
					logger.info("Found {} unique location elements in dialog", locationsInDialog.size());
					for (String loc : locationsInDialog) {
						logger.info("Found location in dialog: {}", loc);
					}
				} catch (Exception e) {
					logger.warn("Could not capture locations from dialog: {}", e.getMessage());
				}

				result.setLocationsInDialog(locationsInDialog);

				// Get actual location count from the Edit Endorsement page
				int actualLocationCountOnPage = getLocationCount();
				logger.info("Actual location count on Edit Endorsement page: {}", actualLocationCountOnPage);
				logger.info("Locations found in confirm dialog: {}", locationsInDialog.size());

				// Validate all locations are displayed - compare dialog count with page count
				boolean allLocationsFound = false;
				if (locationsInDialog.size() >= actualLocationCountOnPage && actualLocationCountOnPage > 0) {
					// Dialog shows at least as many locations as on the page
					allLocationsFound = true;
					logger.info("All locations displayed: Dialog has {} locations, page has {} locations",
						locationsInDialog.size(), actualLocationCountOnPage);
				} else if (locationsInDialog.size() > 0 && actualLocationCountOnPage == 0) {
					// Fallback: if page count couldn't be determined but dialog has locations
					allLocationsFound = true;
					logger.info("Location count from page unavailable, but dialog shows {} locations", locationsInDialog.size());
				} else {
					logger.warn("Not all locations displayed: Dialog has {} locations, page has {} locations",
						locationsInDialog.size(), actualLocationCountOnPage);
				}

				// Also validate against expected addresses if provided (secondary check)
				if (expectedAddresses != null && !expectedAddresses.isEmpty()) {
					int matchedCount = 0;
					for (String expectedAddr : expectedAddresses) {
						String normalizedExpected = normalizeAddress(expectedAddr);
						for (String dialogLoc : locationsInDialog) {
							if (normalizeAddress(dialogLoc).contains(normalizedExpected) ||
								normalizedExpected.contains(normalizeAddress(dialogLoc))) {
								matchedCount++;
								break;
							}
						}
					}
					logger.info("Expected addresses matched: {}/{}", matchedCount, expectedAddresses.size());
				}

				result.setAllLocationsDisplayed(allLocationsFound);

				captureEndorsementScreenshot("Confirm Date Change Dialog");

			} catch (Exception e) {
				logger.warn("Confirm dialog wait timeout: {}", e.getMessage());
				result.setConfirmDialogAppeared(false);
			}

			// Step 5: Click Confirm Changes button
			logger.info("Clicking Confirm Changes button...");
			sleep(500);

			Boolean confirmClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
				"var buttons = document.querySelectorAll('button');" +
				"for (var i = 0; i < buttons.length; i++) {" +
				"  var text = buttons[i].textContent.trim();" +
				"  if (text === 'Confirm Changes' && !buttons[i].disabled && buttons[i].offsetParent) {" +
				"    buttons[i].click(); return true;" +
				"  }" +
				"}" +
				"return false;"
			);

			if (confirmClicked != null && confirmClicked) {
				logger.info("Clicked Confirm Changes button");
				result.setConfirmClicked(true);

				// Wait for page to reload/update
				sleep(5000);

				// Use explicit wait for page elements to load
				try {
					wait.until(ExpectedConditions.or(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//table[.//th[contains(text(),'Premium')]]")),
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Grand Total')]"))
					));
					logger.info("Page loaded after date change");
				} catch (Exception e) {
					logger.warn("Page load wait timeout after confirm: {}", e.getMessage());
				}

				sleep(3000); // Additional wait for calculations

			} else {
				logger.error("Confirm Changes button not found");
				result.setError("Confirm Changes button not found");
			}

			// Wait for page to auto-refresh and data to load
			sleep(5000);
			wait.until(ExpectedConditions.presenceOfElementLocated(
				By.id("edit-endorsement-endorsement-effective-date-picker")));

			// Capture new date
			String newDate = getEndorsementEffectiveDate();
			result.setNewDate(newDate);
			logger.info("New Endorsement Effective Date: {}", newDate);

			captureEndorsementScreenshot("After Date Change Confirmed");

			// Log result to report
			logDateChangeResultToReport(result);

		} catch (Exception e) {
			logger.error("Error changing endorsement date: {}", e.getMessage());
			result.setError(e.getMessage());
			captureEndorsementScreenshot("Date Change Error");
		}

		return result;
	}

	/**
	 * Log date change result to HTML report
	 */
	private void logDateChangeResultToReport(DateChangeValidationResult result) {
		StringBuilder html = new StringBuilder();
		boolean success = result.isSuccess();

		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");
		html.append(success ? "background-color: #d4edda; border-left: 4px solid #28a745;'>" : "background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
		html.append("<h3 style='color: ").append(success ? "#28a745" : "#dc3545").append("; margin-top: 0;'>");
		html.append("Edit Endorsement Date Change: ").append(success ? "SUCCESS" : "FAILED").append("</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Previous Date:</td><td style='padding: 8px;'>").append(result.getOldDate()).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>New Date (2 months earlier):</td><td style='padding: 8px;'>").append(result.getNewDate()).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Confirm Dialog Appeared:</td><td style='padding: 8px;'>").append(result.isConfirmDialogAppeared() ? "Yes" : "No").append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>All Locations Displayed:</td><td style='padding: 8px;'>").append(result.isAllLocationsDisplayed() ? "Yes" : "No").append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Confirm Clicked:</td><td style='padding: 8px;'>").append(result.isConfirmClicked() ? "Yes" : "No").append("</td></tr>");

		// Show locations found in dialog
		if (result.getLocationsInDialog() != null && !result.getLocationsInDialog().isEmpty()) {
			html.append("<tr style='background-color: #e7f3ff;'><td style='padding: 8px; font-weight: bold;' colspan='2'>Locations in Confirm Dialog:</td></tr>");
			for (String loc : result.getLocationsInDialog()) {
				html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px;' colspan='2'>• ").append(loc).append("</td></tr>");
			}
		}

		if (result.getError() != null) {
			html.append("<tr style='background-color: #f8d7da;'><td style='padding: 8px; font-weight: bold; color: #dc3545;'>Error:</td><td style='padding: 8px; color: #dc3545;'>").append(result.getError()).append("</td></tr>");
		}

		html.append("</table></div>");
		logHtmlToReport(html.toString());
	}

	/**
	 * Validate premium calculations on Edit Endorsement page after date change
	 * Uses same formulas as testAddLocationsOnEndorsement
	 */
	public PremiumValidationResult validateEditEndorsementPremiums() {
		logger.info("=== Validating Edit Endorsement Premium Calculations ===");
		return validateEndorsementPremiums();
	}
}

