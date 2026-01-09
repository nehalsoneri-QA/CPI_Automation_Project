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
			// Primary XPath for Create New Premium Endorsement page
			// XPath: //*[@id="root"]/div[2]/div[1]/h1/span
			String primaryXpath = "//*[@id='root']/div[2]/div[1]/h1/span";

			try {
				WebElement policyElement = driver.findElement(By.xpath(primaryXpath));
				String policyNumber = policyElement.getText().trim();

				if (policyNumber != null && !policyNumber.isEmpty()) {
					logger.info("Policy Number from Create Endorsement UI: {}", policyNumber);
					return policyNumber;
				}
			} catch (Exception e) {
				logger.warn("Error getting policy number from primary XPath: {}", e.getMessage());
			}

			// Fallback: Try from URL /create-premium-endorsement/{policyNumber}
			String url = driver.getCurrentUrl();
			logger.info("Current URL for policy number extraction: {}", url);

			if (url.contains("create-premium-endorsement/")) {
				String[] parts = url.split("create-premium-endorsement/");
				if (parts.length > 1) {
					String policyNum = parts[1].split("[?#/]")[0].trim();
					if (!policyNum.isEmpty() && policyNum.matches("\\d+")) {
						logger.info("Policy Number from URL: {}", policyNum);
						return policyNum;
					}
				}
			}

			// Also try edit-endorsement URL pattern
			if (url.contains("edit-endorsement/")) {
				String[] parts = url.split("edit-endorsement/");
				if (parts.length > 1) {
					String policyNum = parts[1].split("[?#/]")[0].trim();
					if (!policyNum.isEmpty() && policyNum.matches("\\d+")) {
						logger.info("Policy Number from edit-endorsement URL: {}", policyNum);
						return policyNum;
					}
				}
			}

			// Additional fallback XPaths
			String[] fallbackXpaths = {
				"//h1/span",
				"//h1[contains(text(),'Policy')]/span",
				"//*[contains(text(),'Policy #')]/following::span[1]",
				"//span[contains(@class,'policy-number')]"
			};

			for (String xpath : fallbackXpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement el : elements) {
						String text = el.getText().trim();
						if (text != null && !text.isEmpty() && text.matches("\\d+")) {
							logger.info("Policy Number from fallback XPath '{}': {}", xpath, text);
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
		logger.warn("Policy number not found from UI or URL");
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
	 * Get endorsement effective date value (handles react-date-picker component)
	 * The react-date-picker has a hidden input with full ISO date (YYYY-MM-DD)
	 */
	public String getEndorsementEffectiveDate() {
		try {
			// Primary approach: Get date from react-date-picker hidden input (first input has full ISO date)
			try {
				WebElement dateContainer = driver.findElement(By.xpath("//*[@id='create-endorsement-endorsement-effective-date-picker']/div/div"));
				if (dateContainer != null) {
					List<WebElement> inputs = dateContainer.findElements(By.xpath(".//input"));
					if (inputs.size() > 0) {
						// The first input in react-date-picker contains the full ISO date (YYYY-MM-DD)
						String firstInputValue = inputs.get(0).getAttribute("value");
						logger.info("Create endorsement date - First input value: '{}'", firstInputValue);

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

			// Fallback to standard approach
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
					By.id("create-endorsement-endorsement-effective-date-picker")));
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
			// User provided xpath: //*[@id='create-endorsement-endorsement-effective-date-picker']/div/button/svg
			WebElement calendarButton = null;
			String[] calendarButtonXpaths = {
				"//*[@id='create-endorsement-endorsement-effective-date-picker']/div/button/svg",
				"//*[@id='create-endorsement-endorsement-effective-date-picker']/div/button",
				"//*[@id='create-endorsement-endorsement-effective-date-picker']//button[.//svg]",
				"//*[@id='create-endorsement-endorsement-effective-date-picker']//button",
				"//*[@id='create-endorsement-endorsement-effective-date-picker']//svg",
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
				"//*[@id='create-endorsement-endorsement-effective-date-picker']//input",
				"//*[@id='create-endorsement-endorsement-effective-date-picker']/div/input",
				"//div[contains(@id,'endorsement-effective-date')]//input",
				"//*[@id='create-endorsement-endorsement-effective-date-picker']//input[@type='text']"
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
			String disabledValue = getDateFromDisabledField(endorsementEffectiveDatePicker, "create-endorsement-endorsement-effective-date-picker");
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
		html.append("<p style='color: #000000; font-size: 12px; font-weight: bold; margin-top: 10px;'>");
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
			"//*[@id='create-endorsement-endorsement-effective-date-picker']/span/div/div/div[1]/button[4]",
			"//*[@id='create-endorsement-endorsement-effective-date-picker']//button[normalize-space()='>']",
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
				By.xpath("//*[@id='create-endorsement-endorsement-effective-date-picker']//button"));
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
			double excelGLAmount = getDisabledInputValue("create-endorsement-general-liability-amount-input");
			double excelWSAmount = getDisabledInputValue("create-endorsement-water-sewer-backup-amount-input");
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
	 * Get value from disabled input field by ID
	 * Disabled fields require getAttribute("value") instead of getText()
	 */
	private double getDisabledInputValue(String elementId) {
		try {
			// Try multiple ways to find and get the value
			String[] xpaths = {
				"//*[@id='" + elementId + "']",
				"//input[@id='" + elementId + "']",
				"//*[contains(@id,'" + elementId.replace("create-endorsement-", "") + "')]"
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
		String streetTarget = extractStreetPortion(targetAddress);
		logger.info("Searching for normalized address: '{}', street portion: '{}'", normalizedTarget, streetTarget);

		// First pass: Try exact/full address matching
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

		// Second pass: Try lenient street-portion matching (handles Faker city/state/zip differences)
		if (streetTarget != null && streetTarget.length() > 5) {
			logger.info("Full address match failed, trying street-portion matching: '{}'", streetTarget);
			for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
				WebElement row = rows.get(rowIdx);
				try {
					List<WebElement> cells = row.findElements(By.tagName("td"));

					// Try address column first
					if (addressColIndex >= 0 && addressColIndex < cells.size()) {
						String cellAddress = cells.get(addressColIndex).getText().trim();
						String cellStreet = extractStreetPortion(cellAddress);

						if (cellStreet.contains(streetTarget) || streetTarget.contains(cellStreet)) {
							logger.info("Row {}: Street-portion match found - '{}' matches '{}' (street: '{}')",
								rowIdx, cellAddress, targetAddress, streetTarget);
							return row;
						}
					}

					// Fallback: search all cells
					for (int c = 0; c < cells.size(); c++) {
						String cellText = cells.get(c).getText().trim();
						if (cellText.length() > 10 && !cellText.startsWith("$")) {
							String cellStreet = extractStreetPortion(cellText);
							if (cellStreet.contains(streetTarget) || streetTarget.contains(cellStreet)) {
								logger.info("Row {}: Street-portion match found in cell {} - '{}' matches '{}' (street: '{}')",
									rowIdx, c, cellText, targetAddress, streetTarget);
								return row;
							}
						}
					}
				} catch (Exception e) {
					logger.warn("Error checking row {} for street match: {}", rowIdx, e.getMessage());
				}
			}
		}

		logger.warn("No row found matching address: '{}' (tried full and street-portion matching)", targetAddress);
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
	 * Extract just the street portion from an address (street number + street name)
	 * This allows matching addresses even when city/state/zip differ
	 * Example: "56-45 Main St, Flushing, NY 11355" -> "56-45 main st"
	 */
	private String extractStreetPortion(String address) {
		if (address == null || address.isEmpty()) return "";

		String normalized = normalizeAddress(address);

		// Split by common delimiters (comma, state abbreviation patterns)
		// Take the first part which is typically the street address
		String[] parts = normalized.split(",");
		if (parts.length > 0) {
			String streetPart = parts[0].trim();
			// Further clean - remove any trailing city name if present
			// Pattern: street ends with st/ave/dr/rd/blvd/ln etc.
			if (streetPart.matches(".*\\b(st|ave|dr|rd|blvd|ln|court|ct|way|circle|cir|place|pl)\\b.*")) {
				// Extract up to and including the street type
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
					"^(.+?\\b(?:st|ave|dr|rd|blvd|ln|court|ct|way|circle|cir|place|pl)\\b)"
				);
				java.util.regex.Matcher matcher = pattern.matcher(streetPart);
				if (matcher.find()) {
					return matcher.group(1).trim();
				}
			}
			return streetPart;
		}
		return normalized;
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

	// ==================== Endorsement Data Capture and Create Endorsement ====================

	/**
	 * Data holder class for all captured endorsement values
	 */
	public static class EndorsementCapturedData {
		private String endorsementEffectiveDate;
		private double premiumGLWS;
		private double taxes;
		private double totalFees;
		private double grandTotal;
		private java.util.List<LocationRowData> locationDetails;
		private int locationCount;
		private String error;

		// Getters and Setters
		public String getEndorsementEffectiveDate() { return endorsementEffectiveDate; }
		public void setEndorsementEffectiveDate(String v) { this.endorsementEffectiveDate = v; }
		public double getPremiumGLWS() { return premiumGLWS; }
		public void setPremiumGLWS(double v) { this.premiumGLWS = v; }
		public double getTaxes() { return taxes; }
		public void setTaxes(double v) { this.taxes = v; }
		public double getTotalFees() { return totalFees; }
		public void setTotalFees(double v) { this.totalFees = v; }
		public double getGrandTotal() { return grandTotal; }
		public void setGrandTotal(double v) { this.grandTotal = v; }
		public java.util.List<LocationRowData> getLocationDetails() { return locationDetails; }
		public void setLocationDetails(java.util.List<LocationRowData> v) { this.locationDetails = v; }
		public int getLocationCount() { return locationCount; }
		public void setLocationCount(int v) { this.locationCount = v; }
		public String getError() { return error; }
		public void setError(String v) { this.error = v; }
	}

	/**
	 * Location row data holder for table validation
	 */
	public static class LocationRowData {
		private int rowIndex;
		private String address;
		private double propertyPremium;
		private double glPremium;
		private double wsPremium;
		private double taxes;
		private double fees;
		private double total;

		// Getters and Setters
		public int getRowIndex() { return rowIndex; }
		public void setRowIndex(int v) { this.rowIndex = v; }
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
		public double getFees() { return fees; }
		public void setFees(double v) { this.fees = v; }
		public double getTotal() { return total; }
		public void setTotal(double v) { this.total = v; }
	}

	/**
	 * Capture all endorsement data from the page for validation on Edit Endorsement screen
	 * Captures: Effective Date of Endorsement, Locations Details table, Premium (GL+WS), Taxes, Total Fees, Grand Total
	 */
	public EndorsementCapturedData captureAllEndorsementData() {
		EndorsementCapturedData data = new EndorsementCapturedData();
		logger.info("=== Capturing All Endorsement Data ===");

		try {
			// 1. Capture Effective Date of Endorsement
			String effectiveDate = getEndorsementEffectiveDate();
			data.setEndorsementEffectiveDate(effectiveDate);
			logger.info("Captured Endorsement Effective Date: {}", effectiveDate);

			// 2. Capture Locations Details table
			java.util.List<LocationRowData> locations = captureLocationTableData();
			data.setLocationDetails(locations);
			data.setLocationCount(locations.size());
			logger.info("Captured {} locations from table", locations.size());

			// 3. Capture Premium (GL + WS)
			double premiumGLWS = getSummaryValueByLabel("Premium", "Premium (GL", "GL+WS");
			data.setPremiumGLWS(premiumGLWS);
			logger.info("Captured Premium (GL+WS): ${}", String.format("%.2f", premiumGLWS));

			// 4. Capture Taxes
			double taxes = getSummaryValueByLabel("Taxes", "Tax");
			data.setTaxes(taxes);
			logger.info("Captured Taxes: ${}", String.format("%.2f", taxes));

			// 5. Capture Total Fees
			double totalFees = getSummaryValueByLabel("Total Fees", "Fees");
			data.setTotalFees(totalFees);
			logger.info("Captured Total Fees: ${}", String.format("%.2f", totalFees));

			// 6. Capture Grand Total
			double grandTotal = getSummaryValueByLabel("Grand Total", "Total");
			data.setGrandTotal(grandTotal);
			logger.info("Captured Grand Total: ${}", String.format("%.2f", grandTotal));

			// Data captured silently - will be validated on Edit Endorsement screen

		} catch (Exception e) {
			logger.error("Error capturing endorsement data: {}", e.getMessage());
			data.setError(e.getMessage());
		}

		return data;
	}

	/**
	 * Capture location table data with all column values
	 */
	public java.util.List<LocationRowData> captureLocationTableData() {
		java.util.List<LocationRowData> locations = new java.util.ArrayList<>();

		try {
			WebElement table = findLocationTable();
			if (table == null) {
				logger.warn("Location table not found");
				return locations;
			}

			// Get headers to find column indices
			List<WebElement> headers = table.findElements(By.xpath(".//thead//th | .//tr[1]//th"));
			Map<String, Integer> columnIndices = new HashMap<>();

			for (int i = 0; i < headers.size(); i++) {
				String headerText = headers.get(i).getText().trim().toLowerCase();
				if (headerText.contains("address")) columnIndices.put("address", i);
				else if (headerText.contains("property") && headerText.contains("premium")) columnIndices.put("propertyPremium", i);
				else if (headerText.contains("gl") && headerText.contains("premium")) columnIndices.put("glPremium", i);
				else if (headerText.contains("water") || headerText.contains("sewer") || headerText.contains("w/s")) columnIndices.put("wsPremium", i);
				else if (headerText.contains("tax")) columnIndices.put("taxes", i);
				else if (headerText.contains("fee")) columnIndices.put("fees", i);
				else if (headerText.contains("total") && !headerText.contains("grand")) columnIndices.put("total", i);
			}

			logger.info("Column indices found: {}", columnIndices);

			// Get all data rows
			List<WebElement> rows = table.findElements(By.xpath(".//tbody//tr"));
			for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
				WebElement row = rows.get(rowIdx);
				List<WebElement> cells = row.findElements(By.xpath(".//td"));

				LocationRowData locData = new LocationRowData();
				locData.setRowIndex(rowIdx + 1);

				// Extract values from each column
				if (columnIndices.containsKey("address") && cells.size() > columnIndices.get("address")) {
					locData.setAddress(cells.get(columnIndices.get("address")).getText().trim());
				}
				if (columnIndices.containsKey("propertyPremium") && cells.size() > columnIndices.get("propertyPremium")) {
					locData.setPropertyPremium(parseAmount(cells.get(columnIndices.get("propertyPremium")).getText()));
				}
				if (columnIndices.containsKey("glPremium") && cells.size() > columnIndices.get("glPremium")) {
					locData.setGlPremium(parseAmount(cells.get(columnIndices.get("glPremium")).getText()));
				}
				if (columnIndices.containsKey("wsPremium") && cells.size() > columnIndices.get("wsPremium")) {
					locData.setWsPremium(parseAmount(cells.get(columnIndices.get("wsPremium")).getText()));
				}
				if (columnIndices.containsKey("taxes") && cells.size() > columnIndices.get("taxes")) {
					locData.setTaxes(parseAmount(cells.get(columnIndices.get("taxes")).getText()));
				}
				if (columnIndices.containsKey("fees") && cells.size() > columnIndices.get("fees")) {
					locData.setFees(parseAmount(cells.get(columnIndices.get("fees")).getText()));
				}
				if (columnIndices.containsKey("total") && cells.size() > columnIndices.get("total")) {
					locData.setTotal(parseAmount(cells.get(columnIndices.get("total")).getText()));
				}

				locations.add(locData);
				logger.debug("Captured location {}: {}", rowIdx + 1, locData.getAddress());
			}

		} catch (Exception e) {
			logger.error("Error capturing location table data: {}", e.getMessage());
		}

		return locations;
	}

	/**
	 * Click Create Endorsement button
	 * @return true if clicked successfully and navigation to edit endorsement page occurred
	 */
	public boolean clickCreateEndorsementButton() {
		logger.info("=== Clicking Create Endorsement Button ===");

		try {
			// Wait for any loading to complete
			sleep(2000);

			// Scroll to bottom to make button visible
			((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
			sleep(1000);

			WebElement createButton = null;

			// PRIORITY 1: Try exact ID first (most reliable)
			try {
				createButton = driver.findElement(By.id("create-endorsement-submit-button"));
				if (createButton != null && createButton.isDisplayed()) {
					logger.info("Found Create Endorsement button by ID: create-endorsement-submit-button");
				} else {
					createButton = null;
				}
			} catch (Exception e) {
				logger.info("Button not found by ID, trying other methods...");
			}

			// PRIORITY 2: Try locator-defined button
			if (createButton == null) {
				try {
					if (submitEndorsementButton != null && submitEndorsementButton.isDisplayed()) {
						createButton = submitEndorsementButton;
						logger.info("Using locator-defined submit button");
					}
				} catch (Exception e) {
					// Continue
				}
			}

			// PRIORITY 3: Try multiple XPaths for the Create Endorsement button
			if (createButton == null) {
				String[] buttonXpaths = {
					"//*[@id='create-endorsement-submit-button']",
					"//button[contains(text(),'Create Endorsement')]",
					"//button[normalize-space()='Create Endorsement']",
					"//*[@id='root']//button[contains(text(),'Create Endorsement')]",
					"//button[contains(@class,'bg-blue') and contains(text(),'Create')]",
					"//div[contains(@class,'flex')]//button[contains(text(),'Create')]",
					"//button[@type='submit' and contains(text(),'Create')]",
					"//button[contains(@id,'submit')]"
				};

				for (String xpath : buttonXpaths) {
					try {
						List<WebElement> buttons = driver.findElements(By.xpath(xpath));
						logger.info("XPath '{}' found {} buttons", xpath, buttons.size());
						for (WebElement btn : buttons) {
							String btnText = btn.getText().trim();
							boolean isDisplayed = false;
							boolean isEnabled = false;
							try {
								isDisplayed = btn.isDisplayed();
								isEnabled = btn.isEnabled();
							} catch (Exception e) {
								continue;
							}

							logger.info("  Button: text='{}', displayed={}, enabled={}", btnText, isDisplayed, isEnabled);

							// Accept button if displayed and enabled (no text check for ID-based XPaths)
							if (isDisplayed && isEnabled) {
								createButton = btn;
								logger.info("Found Create Endorsement button with XPath: {} (text: '{}')", xpath, btnText);
								break;
							}
						}
						if (createButton != null) break;
					} catch (Exception e) {
						// Continue
					}
				}
			}

			// PRIORITY 4: Last resort - find any button with "Create" text at bottom of page
			if (createButton == null) {
				logger.info("Trying last resort: finding any Create button");
				List<WebElement> allButtons = driver.findElements(By.tagName("button"));
				for (WebElement btn : allButtons) {
					try {
						String btnText = btn.getText().trim();
						if (btnText.toLowerCase().contains("create endorsement") && btn.isDisplayed() && btn.isEnabled()) {
							createButton = btn;
							logger.info("Found button via tag search: '{}'", btnText);
							break;
						}
					} catch (Exception e) {
						// Continue
					}
				}
			}

			if (createButton == null) {
				logger.error("Create Endorsement button not found after all attempts");
				captureEndorsementScreenshot("Create Endorsement Button Not Found");
				return false;
			}

			// Scroll to button
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", createButton);
			sleep(500);

			captureEndorsementScreenshot("Before Clicking Create Endorsement");

			// Click the button using JavaScript (more reliable)
			logger.info("Clicking Create Endorsement button using JavaScript...");
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", createButton);
			logger.info("Clicked Create Endorsement button");

			// Wait for navigation to edit endorsement page
			sleep(5000);

			// Wait for URL to change
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
			try {
				wait.until(d -> {
					String url = d.getCurrentUrl();
					return url.contains("edit-premium-endorsement") || url.contains("edit_premium_endorsement") || url.contains("edit-endorsement");
				});
			} catch (Exception e) {
				logger.warn("Timeout waiting for URL change: {}", e.getMessage());
			}

			String currentUrl = driver.getCurrentUrl();
			boolean onEditEndorsement = currentUrl.contains("edit-premium-endorsement") ||
				currentUrl.contains("edit_premium_endorsement") || currentUrl.contains("edit-endorsement");

			if (onEditEndorsement) {
				logger.info("Successfully navigated to Edit Endorsement page: {}", currentUrl);
				captureEndorsementScreenshot("After Create Endorsement - Edit Page");
			} else {
				logger.warn("May not be on Edit Endorsement page. Current URL: {}", currentUrl);
				captureEndorsementScreenshot("After Create Endorsement Click - URL Check");
			}

			return onEditEndorsement;

		} catch (Exception e) {
			logger.error("Error clicking Create Endorsement button: {}", e.getMessage());
			captureEndorsementScreenshot("Create Endorsement Click Error");
			return false;
		}
	}

	/**
	 * Log captured data to HTML report
	 */
	private void logCapturedDataToReport(EndorsementCapturedData data) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #e7f3ff; border-radius: 8px; border-left: 4px solid #007bff;'>");
		html.append("<h3 style='color: #007bff; margin-top: 0;'>Captured Endorsement Data (Before Create)</h3>");

		// Summary values
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; margin-bottom: 15px;'>");
		html.append("<tr style='background-color: #007bff; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left;'>Value</th>");
		html.append("</tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Endorsement Effective Date</td><td style='padding: 8px;'>").append(data.getEndorsementEffectiveDate()).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Premium (GL+WS)</td><td style='padding: 8px;'>$").append(String.format("%.2f", data.getPremiumGLWS())).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Taxes</td><td style='padding: 8px;'>$").append(String.format("%.2f", data.getTaxes())).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Total Fees</td><td style='padding: 8px;'>$").append(String.format("%.2f", data.getTotalFees())).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Grand Total</td><td style='padding: 8px; font-weight: bold; color: #28a745;'>$").append(String.format("%.2f", data.getGrandTotal())).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Location Count</td><td style='padding: 8px;'>").append(data.getLocationCount()).append("</td></tr>");
		html.append("</table>");

		// Location details table
		if (data.getLocationDetails() != null && !data.getLocationDetails().isEmpty()) {
			html.append("<h4 style='color: #007bff;'>Location Details</h4>");
			html.append("<table style='width: 100%; border-collapse: collapse; color: #000000; font-size: 12px;'>");
			html.append("<tr style='background-color: #343a40; color: white;'>");
			html.append("<th style='padding: 6px;'>#</th>");
			html.append("<th style='padding: 6px;'>Address</th>");
			html.append("<th style='padding: 6px;'>Property</th>");
			html.append("<th style='padding: 6px;'>GL</th>");
			html.append("<th style='padding: 6px;'>W/S</th>");
			html.append("<th style='padding: 6px;'>Taxes</th>");
			html.append("<th style='padding: 6px;'>Fees</th>");
			html.append("</tr>");

			for (LocationRowData loc : data.getLocationDetails()) {
				html.append("<tr style='border-bottom: 1px solid #dee2e6;'>");
				html.append("<td style='padding: 6px;'>").append(loc.getRowIndex()).append("</td>");
				html.append("<td style='padding: 6px;'>").append(loc.getAddress() != null ? loc.getAddress() : "").append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getPropertyPremium())).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getGlPremium())).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getWsPremium())).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getTaxes())).append("</td>");
				html.append("<td style='padding: 6px;'>$").append(String.format("%.2f", loc.getFees())).append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
		}

		html.append("</div>");
		logHtmlToReport(html.toString());
	}
}
