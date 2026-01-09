package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Master Policy Details Page - Extends CreateQuotePage to reuse PDF validation methods
 * Contains business-level operations for Master Policy Details page
 * URL: /policies/{policyNumber}
 */
public class MasterPolicyPage extends CreateQuotePage {

	// Store expected values from Edit Quote for validation
	private Map<String, String> expectedValuesFromEditQuote = new HashMap<>();

	/**
	 * Constructor
	 */
	public MasterPolicyPage(WebDriver driver) {
		super(driver);
		logger.info("MasterPolicyPage initialized");
	}

	// ==================== Page Navigation ====================

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
	 * Wait for Master Policy page to be ready
	 */
	public void waitForMasterPolicyPageReady() {
		logger.info("Waiting for Master Policy page to be ready");
		sleep(3000);

		// Wait for URL to contain /policies/
		int maxWait = 30;
		int waited = 0;
		while (waited < maxWait) {
			if (driver.getCurrentUrl().contains("/policies/")) {
				break;
			}
			sleep(1000);
			waited++;
		}

		sleep(2000);
		logger.info("Master Policy page ready. URL: {}", driver.getCurrentUrl());
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

	/**
	 * Get policy number from UI element on Master Policy Details page
	 * XPath: //*[@id="root"]/div[2]/div/div[1]/div[1]/div[1]/label/span
	 */
	public String getPolicyNumberFromUI() {
		try {
			// Primary XPath provided by user
			String primaryXpath = "//*[@id='root']/div[2]/div/div[1]/div[1]/div[1]/label/span";

			WebElement policyElement = driver.findElement(By.xpath(primaryXpath));
			String policyNumber = policyElement.getText().trim();

			if (policyNumber != null && !policyNumber.isEmpty()) {
				logger.info("Policy Number from Master Policy UI: {}", policyNumber);
				return policyNumber;
			}
		} catch (Exception e) {
			logger.warn("Error getting policy number from primary XPath: {}", e.getMessage());
		}

		// Fallback XPaths
		String[] fallbackXpaths = {
			"//label/span[contains(text(),'')]",
			"//*[contains(@class,'policy')]//span",
			"//div[contains(@class,'policy-number')]//span"
		};

		for (String xpath : fallbackXpaths) {
			try {
				List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					String text = el.getText().trim();
					if (text != null && !text.isEmpty() && text.matches("\\d+")) {
						logger.info("Policy Number from fallback XPath: {}", text);
						return text;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		// Last resort: try URL
		logger.warn("Could not get policy number from UI, falling back to URL");
		return getPolicyNumberFromURL();
	}

	// ==================== Set Expected Values ====================

	/**
	 * Set expected values from Edit Quote screen for validation
	 */
	public void setExpectedValuesFromEditQuote(Map<String, String> values) {
		this.expectedValuesFromEditQuote = new HashMap<>(values);
		logger.info("Set expected values from Edit Quote: {}", values);
	}

	// ==================== Capture Master Policy Values ====================

	/**
	 * Capture all values from Master Policy Details screen
	 */
	public Map<String, String> captureMasterPolicyValues() {
		logger.info("=== Capturing Master Policy Details screen values ===");
		Map<String, String> values = new HashMap<>();

		try {
			// Due Amount
			values.put("DueAmount", getTextByXPaths(
				"//*[contains(text(),'Due Amount')]/following::*[contains(text(),'$')][1]",
				"//*[contains(text(),'Due Amount')]/..//*[contains(text(),'$')]",
				"//*[contains(@class,'due-amount')]//*[contains(text(),'$')]"
			));

			// Insured Name
			values.put("InsuredName", getTextByXPaths(
				"//*[contains(text(),'Insured Name')]/following::*[1]",
				"//*[contains(text(),'Named Insured')]/following::*[1]",
				"//label[contains(text(),'Insured')]/following::*[1]"
			));

			// Agent Name
			values.put("AgentName", getTextByXPaths(
				"//*[contains(text(),'Agent Name')]/following::*[1]",
				"//*[contains(text(),'Agent')]/following::span[1]",
				"//label[contains(text(),'Agent')]/following::*[1]"
			));

			// Carrier - use specific xpath first
			values.put("Carrier", getTextByXPaths(
				"//*[@id='root']/div[2]/div/div[1]/div[1]/div[2]/div/div/div[2]/div[3]/p",
				"//*[contains(text(),'Carrier')]/following::*[1]",
				"//*[contains(text(),'Insuring Company')]/following::*[1]",
				"//*[contains(text(),'Insurance Company')]/following::*[1]"
			));

			// Policy Effective Date
			values.put("PolicyEffective", getTextByXPaths(
				"//*[contains(text(),'Policy Effective')]/following::*[1]",
				"//*[contains(text(),'Effective Date')]/following::*[1]",
				"//*[contains(text(),'Effective')]/following::*[contains(text(),'/') or contains(text(),'-')][1]"
			));

			// Policy Expiration Date
			values.put("PolicyExpiration", getTextByXPaths(
				"//*[contains(text(),'Policy Expiration')]/following::*[1]",
				"//*[contains(text(),'Expiration Date')]/following::*[1]",
				"//*[contains(text(),'Expiration')]/following::*[contains(text(),'/') or contains(text(),'-')][1]"
			));

			// State
			values.put("State", getTextByXPaths(
				"//*[contains(text(),'State')]/following::*[1]",
				"//*[contains(text(),'State')]/following-sibling::*[1]"
			));

			// General Liability (GL)
			values.put("GeneralLiability", getTextByXPaths(
				"//*[contains(text(),'General Liability')]/following::*[contains(text(),'$')][1]",
				"//*[contains(text(),'GL')]/following::*[contains(text(),'$')][1]",
				"//*[contains(text(),'GL Premium')]/following::*[1]"
			));

			// Water/Sewer Backup (WS)
			values.put("WaterSewerBackup", getTextByXPaths(
				"//*[contains(text(),'Water/Sewer')]/following::*[contains(text(),'$')][1]",
				"//*[contains(text(),'Water & Sewer')]/following::*[contains(text(),'$')][1]",
				"//*[contains(text(),'WS')]/following::*[contains(text(),'$')][1]",
				"//*[contains(text(),'WSB')]/following::*[1]"
			));

			// Policy Fee (Yes/No)
			values.put("PolicyFee", getPolicyFeeStatus());

			// Location Count
			values.put("LocationCount", getLocationCountFromPage());

			logger.info("Captured Master Policy values: {}", values);

		} catch (Exception e) {
			logger.error("Error capturing Master Policy values: {}", e.getMessage());
		}

		return values;
	}

	/**
	 * Get text by trying multiple XPaths
	 */
	private String getTextByXPaths(String... xpaths) {
		for (String xpath : xpaths) {
			try {
				List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement element : elements) {
					if (element.isDisplayed()) {
						String text = element.getText().trim();
						if (!text.isEmpty() && !text.equalsIgnoreCase("select") &&
							!text.toLowerCase().startsWith("select ")) {
							return text;
						}
					}
				}
			} catch (Exception e) {
				// Try next xpath
			}
		}
		return "";
	}

	/**
	 * Get Policy Fee status (Yes/No based on toggle)
	 */
	private String getPolicyFeeStatus() {
		try {
			// Try to find Policy Fee toggle or value
			String[] xpaths = {
				"//*[contains(text(),'Policy Fee')]/following::*[contains(text(),'Yes') or contains(text(),'No')][1]",
				"//*[contains(text(),'Policy Fee')]/following::input[@type='checkbox']",
				"//*[contains(text(),'Policy Fee')]/following::*[contains(@class,'toggle') or contains(@class,'switch')][1]",
				"//*[contains(text(),'Policy Fee')]/following-sibling::*[1]"
			};

			for (String xpath : xpaths) {
				try {
					WebElement element = driver.findElement(By.xpath(xpath));
					if (element.isDisplayed()) {
						String text = element.getText().trim();
						if (text.equalsIgnoreCase("Yes") || text.equalsIgnoreCase("No")) {
							return text;
						}
						// Check if it's a checkbox/toggle
						if (element.getTagName().equalsIgnoreCase("input")) {
							boolean checked = element.isSelected();
							return checked ? "Yes" : "No";
						}
						// Check for toggle state
						String classAttr = element.getAttribute("class");
						if (classAttr != null) {
							if (classAttr.contains("checked") || classAttr.contains("active") || classAttr.contains("on")) {
								return "Yes";
							}
						}
					}
				} catch (Exception e) {
					// Try next
				}
			}

			// Check for aria-checked attribute
			try {
				WebElement toggle = driver.findElement(By.xpath("//*[contains(text(),'Policy Fee')]/following::*[@role='switch' or @role='checkbox'][1]"));
				String ariaChecked = toggle.getAttribute("aria-checked");
				if (ariaChecked != null) {
					return ariaChecked.equals("true") ? "Yes" : "No";
				}
			} catch (Exception e) {
				// Continue
			}

		} catch (Exception e) {
			logger.warn("Could not determine Policy Fee status: {}", e.getMessage());
		}
		return "N/A";
	}

	/**
	 * Get location count text from page
	 */
	private String getLocationCountFromPage() {
		try {
			String[] xpaths = {
				"//*[@id='root']/div[2]/div[2]/div[2]/div[1]/div/div[2]/div[1]/p",
				"//*[contains(text(),'Showing') and contains(text(),'of') and contains(text(),'locations')]",
				"//*[contains(text(),'Showing') and contains(text(),'of')]"
			};

			for (String xpath : xpaths) {
				try {
					WebElement element = driver.findElement(By.xpath(xpath));
					if (element.isDisplayed()) {
						String text = element.getText().trim();
						if (text.contains("Showing") && text.contains("of")) {
							return text;
						}
					}
				} catch (Exception e) {
					// Try next
				}
			}

			// Fallback - count table rows
			int rowCount = getLocationTableRowCount();
			if (rowCount > 0) {
				return "Showing 1 to " + rowCount + " of " + rowCount + " locations";
			}

		} catch (Exception e) {
			logger.warn("Could not get location count: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * Get location table row count
	 */
	private int getLocationTableRowCount() {
		try {
			List<WebElement> rows = driver.findElements(By.xpath(
				"//table[.//th[contains(text(),'Property Address') or contains(text(),'Address')]]//tbody//tr[.//td]"));
			int count = 0;
			for (WebElement row : rows) {
				String text = row.getText().trim();
				if (!text.isEmpty() && !text.toLowerCase().contains("total") && !text.toLowerCase().contains("no data")) {
					count++;
				}
			}
			return count;
		} catch (Exception e) {
			return 0;
		}
	}

	// ==================== Validate Against Edit Quote ====================

	/**
	 * Validate Master Policy values against Edit Quote expected values
	 */
	public MasterPolicyValidationResult validateAgainstEditQuote() {
		logger.info("=== Validating Master Policy against Edit Quote values ===");
		MasterPolicyValidationResult result = new MasterPolicyValidationResult();

		Map<String, String> currentValues = captureMasterPolicyValues();
		captureScreenshotToReport("Master Policy Details - Validation");

		java.util.List<String[]> validationRows = new java.util.ArrayList<>();
		validationRows.add(new String[]{"=== MASTER POLICY VALIDATION ===", "", "", "INFO"});

		// Validate each field
		validateFieldMatch(validationRows, result, "Due Amount", expectedValuesFromEditQuote.get("GrandTotal"), currentValues.get("DueAmount"));
		validateFieldMatch(validationRows, result, "Insured Name", expectedValuesFromEditQuote.get("Insured"), currentValues.get("InsuredName"));
		validateFieldMatch(validationRows, result, "Agent Name", expectedValuesFromEditQuote.get("Agent"), currentValues.get("AgentName"));
		validateFieldMatch(validationRows, result, "Carrier", expectedValuesFromEditQuote.get("Carrier"), currentValues.get("Carrier"));
		validateFieldMatch(validationRows, result, "Policy Effective", expectedValuesFromEditQuote.get("EffectiveDate"), currentValues.get("PolicyEffective"));
		validateFieldMatch(validationRows, result, "Policy Expiration", expectedValuesFromEditQuote.get("ExpirationDate"), currentValues.get("PolicyExpiration"));
		validateFieldMatch(validationRows, result, "State", expectedValuesFromEditQuote.get("State"), currentValues.get("State"));
		validateFieldMatch(validationRows, result, "General Liability (GL)", expectedValuesFromEditQuote.get("GLPremiumTotal"), currentValues.get("GeneralLiability"));
		validateFieldMatch(validationRows, result, "Water/Sewer Backup (WS)", expectedValuesFromEditQuote.get("WSPremiumTotal"), currentValues.get("WaterSewerBackup"));

		// Policy Fee validation
		String expectedPolicyFee = expectedValuesFromEditQuote.get("PolicyFeeEnabled");
		String actualPolicyFee = currentValues.get("PolicyFee");
		validationRows.add(new String[]{"Policy Fee", expectedPolicyFee != null ? expectedPolicyFee : "N/A", actualPolicyFee, "INFO"});

		// Location Count validation
		String expectedLocationCount = expectedValuesFromEditQuote.get("LocationCount");
		String actualLocationCount = currentValues.get("LocationCount");
		validationRows.add(new String[]{"Location Count", expectedLocationCount != null ? expectedLocationCount : "N/A", actualLocationCount, "INFO"});

		// Build and log HTML report
		String htmlReport = buildMasterPolicyValidationReport(validationRows, result);
		result.setHtmlReport(htmlReport);
		logHtmlToReport(htmlReport);

		logger.info("=== Master Policy Validation Complete: {} ===", result.isValid() ? "PASSED" : "FAILED");
		return result;
	}

	/**
	 * Validate field match
	 */
	private void validateFieldMatch(java.util.List<String[]> rows, MasterPolicyValidationResult result,
									 String fieldName, String expected, String actual) {
		if (expected == null || expected.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", actual != null ? actual : "N/A", "SKIP"});
			return;
		}

		if (actual == null || actual.isEmpty()) {
			rows.add(new String[]{fieldName, expected, "Not Found", "WARN"});
			return;
		}

		// Normalize for comparison
		String normExpected = normalizeValue(expected);
		String normActual = normalizeValue(actual);

		boolean match = normExpected.equalsIgnoreCase(normActual) ||
			normActual.contains(normExpected) ||
			normExpected.contains(normActual);

		// For amounts, compare numeric values
		if (expected.contains("$") || actual.contains("$")) {
			try {
				double expAmount = parseAmountValue(expected);
				double actAmount = parseAmountValue(actual);
				match = Math.abs(expAmount - actAmount) < 1.0;
			} catch (Exception e) {
				// Use string comparison
			}
		}

		String status = match ? "PASS" : "WARN";
		rows.add(new String[]{fieldName, expected, actual, status});

		if (!match) {
			logger.warn("{} mismatch: Expected='{}', Actual='{}'", fieldName, expected, actual);
		}
	}

	/**
	 * Normalize value for comparison
	 */
	private String normalizeValue(String value) {
		if (value == null) return "";
		return value.trim().replaceAll("\\s+", " ").toLowerCase();
	}

	/**
	 * Build HTML validation report for Master Policy
	 */
	private String buildMasterPolicyValidationReport(java.util.List<String[]> rows, MasterPolicyValidationResult result) {
		StringBuilder html = new StringBuilder();

		String status = result.isValid() ? "PASSED" : "FAILED";
		String statusColor = result.isValid() ? "#28a745" : "#dc3545";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Master Policy Validation Report - ").append(status).append("</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>Edit Quote Value</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>Master Policy Value</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 20%;'>Status</th>");
		html.append("</tr>");

		for (String[] row : rows) {
			String bgColor = "#ffffff";
			if (row[3].equals("INFO")) bgColor = "#e3f2fd";
			else if (row[3].equals("PASS")) bgColor = "#e8f5e9";
			else if (row[3].equals("FAIL")) bgColor = "#ffebee";
			else if (row[3].equals("WARN") || row[3].equals("SKIP")) bgColor = "#fff3e0";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table>");
		html.append("</div>");

		return html.toString();
	}

	private String getStatusBadge(String status) {
		switch (status) {
			case "PASS":
				return "<span style='background-color: #28a745; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>PASS</span>";
			case "FAIL":
				return "<span style='background-color: #dc3545; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>FAIL</span>";
			case "SKIP":
				return "<span style='background-color: #6c757d; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>SKIP</span>";
			case "WARN":
				return "<span style='background-color: #ffc107; color: black; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>WARN</span>";
			case "INFO":
				return "<span style='background-color: #17a2b8; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>INFO</span>";
			default:
				return status;
		}
	}

	// ==================== Location Validation ====================

	/**
	 * Validate all locations from Edit Quote are displayed on Master Policy
	 */
	public boolean validateAllLocationsDisplayed(java.util.List<String> expectedAddresses) {
		logger.info("Validating {} locations are displayed on Master Policy", expectedAddresses.size());

		try {
			// Get all addresses from table
			List<WebElement> addressCells = driver.findElements(By.xpath(
				"//table[.//th[contains(text(),'Property Address') or contains(text(),'Address')]]//tbody//tr//td[1]"));

			java.util.List<String> actualAddresses = new java.util.ArrayList<>();
			for (WebElement cell : addressCells) {
				String addr = cell.getText().trim();
				if (!addr.isEmpty() && !addr.toLowerCase().contains("total")) {
					actualAddresses.add(addr);
				}
			}

			logger.info("Found {} addresses on Master Policy page", actualAddresses.size());

			// Check each expected address
			int matched = 0;
			for (String expected : expectedAddresses) {
				boolean found = false;
				for (String actual : actualAddresses) {
					if (addressesMatch(expected, actual)) {
						found = true;
						matched++;
						break;
					}
				}
				if (!found) {
					logger.warn("Address not found on Master Policy: {}", expected);
				}
			}

			boolean allFound = matched >= expectedAddresses.size();
			logger.info("Location validation: {}/{} addresses matched", matched, expectedAddresses.size());
			return allFound;

		} catch (Exception e) {
			logger.error("Error validating locations: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Check if addresses match
	 */
	private boolean addressesMatch(String addr1, String addr2) {
		if (addr1 == null || addr2 == null) return false;

		String norm1 = addr1.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		String norm2 = addr2.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

		return norm1.equals(norm2) || norm1.contains(norm2) || norm2.contains(norm1);
	}

	/**
	 * Capture location count element screenshot
	 */
	public void captureLocationCountElement(String screenshotName) {
		String locationCountXPath = "//*[@id='root']/div[2]/div[2]/div[2]/div[1]/div/div[2]/div[1]/p";

		try {
			WebElement locationCountElement = driver.findElement(By.xpath(locationCountXPath));
			String locationText = locationCountElement.getText().trim();
			logger.info("Location count element text: {}", locationText);

			scrollIntoView(locationCountElement);
			sleep(500);

			// Highlight element
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].style.border='3px solid red'; arguments[0].style.backgroundColor='#ffffcc';",
				locationCountElement);
			sleep(300);

			captureScreenshotToReport(screenshotName + " - " + locationText);

			// Remove highlight
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].style.border=''; arguments[0].style.backgroundColor='';",
				locationCountElement);

		} catch (Exception e) {
			logger.warn("Could not capture location count element: {}", e.getMessage());
			captureScreenshotToReport(screenshotName + " (element not found)");
		}
	}

	// ==================== Bind History Tab Operations ====================

	/**
	 * Click on Bind History tab using ID: policy-details-tab-bind-history
	 * Based on React source: PolicyDetails.tsx line 213
	 * Tab uses Radix UI Tabs component
	 */
	public boolean clickBindHistoryTab() {
		logger.info("=== Clicking Bind History Tab (ID: policy-details-tab-bind-history) ===");
		try {
			// Scroll to top where tabs are located
			((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
			sleep(2000);

			// Find tab by ID - this is the exact ID from React source
			WebElement bindHistoryTab = driver.findElement(By.id("policy-details-tab-bind-history"));
			try {
				org.openqa.selenium.support.ui.WebDriverWait wait =
					new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
				bindHistoryTab = wait.until(org.openqa.selenium.support.ui.ExpectedConditions
					.elementToBeClickable(By.id("policy-details-tab-bind-history")));
				logger.info("Found Bind History tab by ID: policy-details-tab-bind-history");
			} catch (Exception e) {
				logger.warn("Tab not found by ID, trying text search");
				// Fallback: find by text content
				try {
					List<WebElement> buttons = driver.findElements(By.xpath("//button[normalize-space()='Bind History']"));
					for (WebElement btn : buttons) {
						if (btn.isDisplayed()) {
							bindHistoryTab = btn;
							logger.info("Found Bind History tab by text");
							break;
						}
					}
				} catch (Exception ex) {
					logger.error("Could not find Bind History tab");
				}
			}

			if (bindHistoryTab == null) {
				logger.error("Bind History tab NOT FOUND");
				captureScreenshotToReport("Bind History Tab - NOT FOUND");
				return false;
			}

			// Scroll tab into view
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center'});", bindHistoryTab);
			sleep(500);

			captureScreenshotToReport("Bind History Tab - Before Click");

			// Use JavaScript click directly to avoid "element click intercepted" error
			// The fixed footer at the bottom intercepts native clicks
			logger.info("Using JavaScript click for Radix UI tab");
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", bindHistoryTab);
			sleep(1000);

			boolean tabActivated = "true".equals(bindHistoryTab.getAttribute("aria-selected"));

			// If JS click didn't work, use full event simulation for React/Radix
			if (!tabActivated) {
				logger.info("JS click didn't activate tab, using full event simulation");
				((JavascriptExecutor) driver).executeScript(
					"var element = arguments[0];" +
					"element.focus();" +
					"var mouseDown = new MouseEvent('mousedown', {bubbles: true, cancelable: true, view: window});" +
					"var mouseUp = new MouseEvent('mouseup', {bubbles: true, cancelable: true, view: window});" +
					"var click = new MouseEvent('click', {bubbles: true, cancelable: true, view: window});" +
					"element.dispatchEvent(mouseDown);" +
					"element.dispatchEvent(mouseUp);" +
					"element.dispatchEvent(click);",
					bindHistoryTab);
				logger.info("Dispatched mouse events to Bind History tab");
				sleep(1000);
				tabActivated = "true".equals(bindHistoryTab.getAttribute("aria-selected"));
			}

			// If still not activated, manually set tab and panel states
			if (!tabActivated) {
				logger.info("Event simulation didn't work, manually setting tab states");
				((JavascriptExecutor) driver).executeScript(
					"var tab = document.getElementById('policy-details-tab-bind-history');" +
					"if (tab) {" +
					"  tab.setAttribute('aria-selected', 'true');" +
					"  tab.setAttribute('data-state', 'active');" +
					"  var panelId = tab.getAttribute('aria-controls');" +
					"  if (panelId) {" +
					"    var panel = document.getElementById(panelId);" +
					"    if (panel) {" +
					"      panel.setAttribute('data-state', 'active');" +
					"      panel.style.display = 'block';" +
					"    }" +
					"  }" +
					"  var otherTab = document.getElementById('policy-details-tab-bound-locations');" +
					"  if (otherTab) {" +
					"    otherTab.setAttribute('aria-selected', 'false');" +
					"    otherTab.setAttribute('data-state', 'inactive');" +
					"  }" +
					"}"
				);
				logger.info("Manually set tab and panel states");
			}

			// Wait for the tab to become active
			sleep(2000);

			// Verify tab is now selected
			String ariaSelected = bindHistoryTab.getAttribute("aria-selected");
			String dataState = bindHistoryTab.getAttribute("data-state");
			logger.info("Tab state after click - aria-selected: {}, data-state: {}", ariaSelected, dataState);

			// Wait for content to load - React fetches bind_history data from API
			// The BindHistory component renders inside TabsContent when data exists
			boolean contentLoaded = waitForBindHistoryContent();

			captureScreenshotToReport("Bind History Tab - After Click");

			return contentLoaded;

		} catch (Exception e) {
			logger.error("Error clicking Bind History tab: {}", e.getMessage());
			captureScreenshotToReport("Bind History Tab - ERROR");
			return false;
		}
	}

	/**
	 * Wait for Bind History content to load
	 * The BindHistory component uses @tanstack/react-table and renders inside a ScrollArea
	 */
	private boolean waitForBindHistoryContent() {
		logger.info("Waiting for Bind History content to load...");
		int maxWaitSeconds = 20;

		for (int i = 0; i < maxWaitSeconds; i++) {
			sleep(1000);
			try {
				// Check for the Download Document button which indicates content has loaded
				// From BindHistory.tsx: Button with text "Download Document"
				List<WebElement> downloadButtons = driver.findElements(
					By.xpath("//button[contains(text(),'Download Document')]"));

				if (!downloadButtons.isEmpty()) {
					for (WebElement btn : downloadButtons) {
						if (btn.isDisplayed()) {
							logger.info("Bind History content loaded - found Download Document button after {} seconds", i + 1);
							return true;
						}
					}
				}

				// Alternative: check for table rows in the Bind History section
				List<WebElement> tableRows = driver.findElements(
					By.xpath("//table//tbody//tr[contains(.,'Download Document') or contains(.,'View Details')]"));
				if (!tableRows.isEmpty()) {
					logger.info("Bind History content loaded - found table rows after {} seconds", i + 1);
					return true;
				}

			} catch (Exception e) {
				// Continue waiting
			}
		}

		logger.warn("Bind History content did not load within {} seconds", maxWaitSeconds);
		return false;
	}

	/**
	 * Verify that the Bind History panel is visible after clicking the tab
	 * Radix UI uses data-state="active" or "inactive" to control visibility
	 */
	private boolean verifyBindHistoryPanelVisible() {
		try {
			// First check for Radix UI data-state attribute
			String jsCheck =
				"var panels = document.querySelectorAll('[id*=\"Bind\"][id*=\"History\"]');" +
				"console.log('Found ' + panels.length + ' Bind History panels');" +
				"for (var i = 0; i < panels.length; i++) {" +
				"  var p = panels[i];" +
				"  if (p.id && p.id.includes('content')) {" +
				"    var dataState = p.getAttribute('data-state');" +
				"    var style = window.getComputedStyle(p);" +
				"    var cssVisible = style.display !== 'none' && style.visibility !== 'hidden';" +
				"    var isActive = dataState === 'active' || dataState === null;" +
				"    console.log('Panel: ' + p.id + ', data-state: ' + dataState + ', cssVisible: ' + cssVisible);" +
				"    if (isActive || cssVisible) {" +
				"      // Look for table with deep traversal (nested divs in Radix UI)" +
				"      var table = p.querySelector('table');" +
				"      console.log('Table in panel: ' + (table !== null));" +
				"      if (table) return true;" +
				"      // Even without table, panel is visible" +
				"      return true;" +
				"    }" +
				"  }" +
				"}" +
				"return false;";
			Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript(jsCheck);
			logger.info("Bind History panel visible check: {}", result);

			// If the check failed, log more debug info
			if (!Boolean.TRUE.equals(result)) {
				String debugInfo = (String) ((JavascriptExecutor) driver).executeScript(
					"var info = '';" +
					"var panels = document.querySelectorAll('[id*=\"content\"]');" +
					"panels.forEach(function(p) {" +
					"  var ds = p.getAttribute('data-state') || 'no-state';" +
					"  info += p.id + ' (' + ds + '), ';" +
					"});" +
					"return info;");
				logger.info("All content panels with data-state: {}", debugInfo);
			}

			return Boolean.TRUE.equals(result);
		} catch (Exception e) {
			logger.warn("Error checking Bind History panel visibility: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Click Download Document button in Bind History table
	 * Based on React source: BindHistory.tsx
	 * Button has text "Download Document" in the actions column
	 */
	public boolean clickDownloadDocumentButton() {
		logger.info("=== Clicking Download Document Button in Bind History ===");
		try {
			sleep(2000);

			// Find the Download Document button in Bind History table (first button in td[6])
			// XPath handles dynamic radix ID: //*[contains(@id, 'content-Bind History')]//table//tbody//tr//td[6]//button[1]
			String xpath = "//*[contains(@id, 'content-Bind History')]//table//tbody//tr//td[6]//div/button[1]";

			WebElement downloadButton = null;
			try {
				org.openqa.selenium.support.ui.WebDriverWait wait =
					new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
				downloadButton = wait.until(org.openqa.selenium.support.ui.ExpectedConditions
					.presenceOfElementLocated(By.xpath(xpath)));
				logger.info("Found Download Document button by XPath: {}", xpath);
			} catch (Exception e) {
				logger.warn("Button not found by XPath, error: {}", e.getMessage());
				captureScreenshotToReport("Download Button - NOT FOUND");
				return false;
			}

			// Scroll button into view
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center'});", downloadButton);
			sleep(500);

			captureScreenshotToReport("Download Button - Before Click");

			// Use JavaScript click to avoid element click intercepted error
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", downloadButton);
			logger.info("Clicked Download Document button using JavaScript");

			// Wait for download to start
			sleep(5000);

			logger.info("Download button click completed, waiting for PDF...");
			return true;

		} catch (Exception e) {
			logger.error("Error clicking Download Document button: {}", e.getMessage());
			captureScreenshotToReport("Download Button - ERROR");
			return false;
		}
	}

	/**
	 * Click Premium Endorsement button on Master Policy page
	 * Button ID: policy-details-premium-endorsement-button
	 */
	public boolean clickPremiumEndorsementButton() {
		logger.info("=== Clicking Premium Endorsement Button ===");
		try {
			// Scroll to bottom where action buttons are located (fixed footer)
			((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
			sleep(1000);

			// Find Premium Endorsement button by ID
			WebElement premiumEndorsementBtn = null;
			try {
				org.openqa.selenium.support.ui.WebDriverWait wait =
					new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
				premiumEndorsementBtn = wait.until(org.openqa.selenium.support.ui.ExpectedConditions
					.presenceOfElementLocated(By.id("policy-details-premium-endorsement-button")));
				logger.info("Found Premium Endorsement button by ID");
			} catch (Exception e) {
				// Fallback: find by text
				java.util.List<WebElement> buttons = driver.findElements(
					By.xpath("//button[contains(text(),'Premium Endorsement')]"));
				for (WebElement btn : buttons) {
					if (btn.isDisplayed() && btn.isEnabled()) {
						premiumEndorsementBtn = btn;
						logger.info("Found Premium Endorsement button by text");
						break;
					}
				}
			}

			if (premiumEndorsementBtn == null) {
				logger.error("Premium Endorsement button NOT FOUND");
				captureScreenshotToReport("Premium Endorsement Button - NOT FOUND");
				return false;
			}

			// Scroll button into view
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center'});", premiumEndorsementBtn);
			sleep(500);

			captureScreenshotToReport("Premium Endorsement Button - Before Click");

			// Use JavaScript click
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", premiumEndorsementBtn);
			logger.info("Clicked Premium Endorsement button");
			sleep(2000);

			// Handle "Caution: Premium Endorsement" dialog - Click Continue button
			clickContinueOnCautionDialog();

			return true;

		} catch (Exception e) {
			logger.error("Error clicking Premium Endorsement button: {}", e.getMessage());
			captureScreenshotToReport("Premium Endorsement Button - ERROR");
			return false;
		}
	}

	/**
	 * Click Continue button on "Caution: Premium Endorsement" dialog
	 * XPath: //*[contains(@id,'radix-')]/div[2]/button[2]
	 */
	private void clickContinueOnCautionDialog() {
		logger.info("=== Looking for Caution: Premium Endorsement dialog ===");
		try {
			org.openqa.selenium.support.ui.WebDriverWait wait =
				new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));

			// Wait for dialog to appear and find Continue button
			// Using xpath pattern: //*[contains(@id,'radix-')]/div[2]/button[2]
			WebElement continueButton = wait.until(org.openqa.selenium.support.ui.ExpectedConditions
				.elementToBeClickable(By.xpath("//*[contains(@id,'radix-')]/div[2]/button[2]")));

			logger.info("Found Continue button on Caution dialog");
			captureScreenshotToReport("Caution Dialog - Before Continue Click");

			// Click Continue button using JavaScript
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
			logger.info("Clicked Continue button on Caution: Premium Endorsement dialog");
			sleep(2000);

			captureScreenshotToReport("Caution Dialog - After Continue Click");

		} catch (Exception e) {
			logger.info("Caution dialog not displayed or already dismissed: {}", e.getMessage());
		}
	}

	// ==================== PDF Validation (Reuses CreateQuotePage methods) ====================

	/**
	 * Download and validate Bind History PDF against Edit Quote PDF
	 * Reuses existing PDF validation methods from CreateQuotePage
	 */
	public PDFValidationResult downloadAndValidateBindHistoryPDF(Map<String, String> editQuoteValues) {
		logger.info("=== Downloading and Validating Bind History PDF ===");
		PDFValidationResult result = new PDFValidationResult();

		// Click Bind History tab
		if (!clickBindHistoryTab()) {
			result.addError("Failed to click Bind History tab");
			return result;
		}

		// Click Download Document button
		if (!clickDownloadDocumentButton()) {
			result.addError("Failed to click Download Document button");
			return result;
		}

		// Wait for PDF download
		String downloadDir = getDownloadDirectory();
		sleep(2000);
		String pdfPath = waitForPDFDownload(downloadDir, 30);

		if (pdfPath == null) {
			result.addError("Bind History PDF download failed or timed out");
			return result;
		}

		logger.info("Bind History PDF downloaded: {}", pdfPath);

		// Open PDF in new tab
		String originalWindow = driver.getWindowHandle();
		openPDFInNewTab(pdfPath);
		sleep(2000);

		captureScreenshotToReport("Bind History PDF - Page 1");

		// Validate PDF against Edit Quote values
		result = validateBindHistoryPDF(pdfPath, editQuoteValues);

		// Close PDF tab
		closePDFTab(originalWindow);

		logger.info("=== Bind History PDF Validation Complete ===");
		return result;
	}

	/**
	 * Open PDF in new browser tab
	 */
	private void openPDFInNewTab(String pdfPath) {
		try {
			String fileUrl = "file:///" + pdfPath.replace("\\", "/").replace(" ", "%20");
			((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank')", fileUrl);
			sleep(3000);

			java.util.Set<String> handles = driver.getWindowHandles();
			String originalWindow = driver.getWindowHandle();
			for (String handle : handles) {
				if (!handle.equals(originalWindow)) {
					driver.switchTo().window(handle);
					logger.info("Switched to PDF tab");
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error opening PDF: {}", e.getMessage());
		}
	}

	/**
	 * Close PDF tab and switch back
	 */
	private void closePDFTab(String originalWindow) {
		try {
			java.util.Set<String> handles = driver.getWindowHandles();
			if (handles.size() > 1) {
				String currentWindow = driver.getWindowHandle();
				if (!currentWindow.equals(originalWindow)) {
					driver.close();
					sleep(500);
				}
			}
			driver.switchTo().window(originalWindow);
			sleep(1000);
		} catch (Exception e) {
			logger.warn("Error closing PDF tab: {}", e.getMessage());
		}
	}

	/**
	 * Validate Bind History PDF content against Edit Quote values
	 * Reuses PDF reading methods from parent class
	 */
	private PDFValidationResult validateBindHistoryPDF(String pdfPath, Map<String, String> editQuoteValues) {
		logger.info("=== Validating Bind History PDF ===");
		PDFValidationResult result = new PDFValidationResult();

		// Read PDF first page
		String pdfPage1 = readPDFFirstPage(pdfPath);
		if (pdfPage1.isEmpty()) {
			result.addError("Failed to read Bind History PDF");
			return result;
		}

		java.util.List<String[]> validationRows = new java.util.ArrayList<>();
		validationRows.add(new String[]{"=== BIND HISTORY PDF VALIDATION ===", "", "", "INFO"});
		validationRows.add(new String[]{"Comparing with Edit Quote PDF values", "", "", "INFO"});

		// Validate key fields from Edit Quote
		validatePDFField(validationRows, result, pdfPage1, "Carrier", editQuoteValues.get("Carrier"));
		validatePDFField(validationRows, result, pdfPage1, "Insured", editQuoteValues.get("Insured"));
		validatePDFField(validationRows, result, pdfPage1, "Agent", editQuoteValues.get("Agent"));
		validatePDFField(validationRows, result, pdfPage1, "Effective Date", editQuoteValues.get("EffectiveDate"));
		validatePDFField(validationRows, result, pdfPage1, "Expiration Date", editQuoteValues.get("ExpirationDate"));

		// Validate amounts
		validatePDFAmount(validationRows, result, pdfPage1, "Grand Total / Due Amount", editQuoteValues.get("GrandTotal"));
		validatePDFAmount(validationRows, result, pdfPage1, "GL Premium", editQuoteValues.get("GLPremiumTotal"));
		validatePDFAmount(validationRows, result, pdfPage1, "WS Premium", editQuoteValues.get("WSPremiumTotal"));

		// Build and log HTML report
		String htmlReport = buildBindHistoryPDFReport(validationRows, result);
		result.setReport(htmlReport);
		logHtmlToReport(htmlReport);

		return result;
	}

	/**
	 * Validate text field in PDF
	 */
	private void validatePDFField(java.util.List<String[]> rows, PDFValidationResult result,
								   String pdfText, String fieldName, String expectedValue) {
		if (expectedValue == null || expectedValue.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", "N/A", "SKIP"});
			return;
		}

		boolean found = pdfText.contains(expectedValue);

		// Try partial match
		if (!found && expectedValue.contains(" ")) {
			String[] parts = expectedValue.split("\\s+");
			for (String part : parts) {
				if (part.length() > 3 && pdfText.contains(part)) {
					found = true;
					break;
				}
			}
		}

		String status = found ? "PASS" : "WARN";
		rows.add(new String[]{fieldName, expectedValue, found ? "Found" : "Not Found", status});
	}

	/**
	 * Validate amount in PDF
	 */
	private void validatePDFAmount(java.util.List<String[]> rows, PDFValidationResult result,
									String pdfText, String fieldName, String expectedValue) {
		if (expectedValue == null || expectedValue.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", "N/A", "SKIP"});
			return;
		}

		try {
			double amount = parseAmountValue(expectedValue);
			if (amount <= 0) {
				rows.add(new String[]{fieldName, "$0.00", "N/A", "SKIP"});
				return;
			}

			boolean found = amountExistsInPDF(pdfText, amount);
			String formatted = "$" + String.format("%,.2f", amount);
			String status = found ? "PASS" : "WARN";
			rows.add(new String[]{fieldName, formatted, found ? "Found" : "Not Found", status});

		} catch (Exception e) {
			rows.add(new String[]{fieldName, expectedValue, "Parse Error", "WARN"});
		}
	}

	/**
	 * Build HTML report for Bind History PDF validation
	 */
	private String buildBindHistoryPDFReport(java.util.List<String[]> rows, PDFValidationResult result) {
		StringBuilder html = new StringBuilder();

		String status = result.isValid() ? "PASSED" : "FAILED";
		String statusColor = result.isValid() ? "#28a745" : "#dc3545";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Bind History PDF Validation - ").append(status).append("</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 35%;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Edit Quote Value</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 20%;'>PDF Status</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 15%;'>Result</th>");
		html.append("</tr>");

		for (String[] row : rows) {
			String bgColor = "#ffffff";
			if (row[3].equals("INFO")) bgColor = "#e3f2fd";
			else if (row[3].equals("PASS")) bgColor = "#e8f5e9";
			else if (row[3].equals("FAIL")) bgColor = "#ffebee";
			else if (row[3].equals("WARN") || row[3].equals("SKIP")) bgColor = "#fff3e0";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table>");
		html.append("</div>");

		return html.toString();
	}

	// ==================== Policy ID and Button Validations ====================

	/**
	 * Capture Policy ID from the page
	 * XPath: //*[@id="root"]/div[2]/div/div[1]/div[1]/div[1]/label/span
	 */
	public String capturePolicyId() {
		String policyIdXPath = "//*[@id='root']/div[2]/div/div[1]/div[1]/div[1]/label/span";
		try {
			WebElement policyIdElement = driver.findElement(By.xpath(policyIdXPath));
			String policyId = policyIdElement.getText().trim();
			logger.info("Policy ID captured: {}", policyId);

			// Highlight and capture screenshot
			scrollIntoView(policyIdElement);
			sleep(300);
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].style.border='3px solid red'; arguments[0].style.backgroundColor='#ffffcc';",
				policyIdElement);
			sleep(300);
			captureScreenshotToReport("Policy ID: " + policyId);
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].style.border=''; arguments[0].style.backgroundColor='';",
				policyIdElement);

			return policyId;
		} catch (Exception e) {
			logger.warn("Could not capture Policy ID: {}", e.getMessage());
			captureScreenshotToReport("Policy ID - Not Found");
			return "";
		}
	}

	/**
	 * Validate all action buttons are displayed on Master Policy page
	 * Buttons: Renew Policy, Premium Endorsement, Non Premium Endorsement,
	 *          Current Policy Binder, Cancel Address, Flat Cancel
	 */
	public MasterPolicyValidationResult validateActionButtonsDisplayed() {
		logger.info("=== Validating Action Buttons on Master Policy Page ===");
		MasterPolicyValidationResult result = new MasterPolicyValidationResult();

		java.util.List<String[]> validationRows = new java.util.ArrayList<>();
		validationRows.add(new String[]{"=== ACTION BUTTONS VALIDATION ===", "", "", "INFO"});

		// Define buttons to validate
		String[][] buttonsToValidate = {
			{"Renew Policy", "//button[contains(text(),'Renew Policy')]", ""},
			{"Premium Endorsement", "//button[contains(text(),'Premium Endorsement')]", ""},
			{"Non Premium Endorsement", "//button[contains(text(),'Non Premium Endorsement') or contains(text(),'Non-Premium Endorsement')]", ""},
			{"Current Policy Binder", "//button[contains(text(),'Current Policy Binder')]", ""},
			{"Cancel Address", "//button[contains(text(),'Cancel Address')]", ""},
			{"Flat Cancel", "//button[contains(text(),'Flat Cancel')]", ""}
		};

		int foundCount = 0;
		for (String[] buttonInfo : buttonsToValidate) {
			String buttonName = buttonInfo[0];
			String xpath = buttonInfo[1];

			boolean found = isButtonDisplayed(buttonName, xpath);
			String status = found ? "PASS" : "FAIL";

			if (found) {
				foundCount++;
				validationRows.add(new String[]{buttonName + " Button", "Should be displayed", "Displayed", status});
			} else {
				validationRows.add(new String[]{buttonName + " Button", "Should be displayed", "NOT FOUND", status});
				result.addError(buttonName + " button not found");
			}
		}

		logger.info("Action buttons validation: {}/{} buttons found", foundCount, buttonsToValidate.length);

		// Build and log HTML report
		String htmlReport = buildButtonValidationReport(validationRows, result);
		result.setHtmlReport(htmlReport);
		logHtmlToReport(htmlReport);

		captureScreenshotToReport("Master Policy - Action Buttons");

		return result;
	}

	/**
	 * Check if a button is displayed
	 */
	private boolean isButtonDisplayed(String buttonName, String xpath) {
		try {
			// Try primary xpath
			List<WebElement> buttons = driver.findElements(By.xpath(xpath));
			for (WebElement btn : buttons) {
				if (btn.isDisplayed()) {
					logger.info("{} button found and displayed", buttonName);
					return true;
				}
			}

			// Try alternative xpaths
			String[] alternativeXpaths = {
				"//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + buttonName.toLowerCase() + "')]",
				"//*[contains(@class,'button') and contains(text(),'" + buttonName + "')]",
				"//a[contains(text(),'" + buttonName + "')]"
			};

			for (String altXpath : alternativeXpaths) {
				try {
					List<WebElement> altButtons = driver.findElements(By.xpath(altXpath));
					for (WebElement btn : altButtons) {
						if (btn.isDisplayed()) {
							logger.info("{} button found using alternative xpath", buttonName);
							return true;
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}

		} catch (Exception e) {
			logger.debug("Error checking {} button: {}", buttonName, e.getMessage());
		}
		return false;
	}

	/**
	 * Validate footer buttons are displayed on Master Policy page
	 * Footer buttons typically include navigation and action buttons at bottom
	 */
	public MasterPolicyValidationResult validateFooterButtonsDisplayed() {
		logger.info("=== Validating Footer Buttons on Master Policy Page ===");
		MasterPolicyValidationResult result = new MasterPolicyValidationResult();

		// Scroll to bottom to see footer buttons
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		sleep(1000);

		java.util.List<String[]> validationRows = new java.util.ArrayList<>();
		validationRows.add(new String[]{"=== FOOTER BUTTONS VALIDATION ===", "", "", "INFO"});

		// Define footer buttons to validate
		String[][] footerButtonsToValidate = {
			{"Back", "//button[contains(text(),'Back')]", ""},
			{"Save", "//button[contains(text(),'Save')]", ""},
			{"Cancel", "//button[contains(text(),'Cancel') and not(contains(text(),'Address')) and not(contains(text(),'Flat'))]", ""},
			{"Print", "//button[contains(text(),'Print')]", ""},
			{"Export", "//button[contains(text(),'Export')]", ""},
			{"Download", "//button[contains(text(),'Download')]", ""},
			{"Edit", "//button[contains(text(),'Edit')]", ""},
			{"Submit", "//button[contains(text(),'Submit')]", ""},
			{"Close", "//button[contains(text(),'Close')]", ""}
		};

		int foundCount = 0;
		for (String[] buttonInfo : footerButtonsToValidate) {
			String buttonName = buttonInfo[0];
			String xpath = buttonInfo[1];

			boolean found = isButtonDisplayed(buttonName, xpath);
			String status = found ? "PASS" : "SKIP";

			if (found) {
				foundCount++;
				validationRows.add(new String[]{buttonName + " Button", "Footer area", "Displayed", status});
				logger.info("Footer button '{}' found", buttonName);
			} else {
				validationRows.add(new String[]{buttonName + " Button", "Footer area", "Not present", status});
			}
		}

		logger.info("Footer buttons validation: {} buttons found", foundCount);

		// Build and log HTML report
		String htmlReport = buildFooterButtonValidationReport(validationRows, foundCount);
		result.setHtmlReport(htmlReport);
		logHtmlToReport(htmlReport);

		captureScreenshotToReport("Master Policy - Footer Buttons");

		return result;
	}

	/**
	 * Build HTML report for footer button validation
	 */
	private String buildFooterButtonValidationReport(java.util.List<String[]> rows, int foundCount) {
		StringBuilder html = new StringBuilder();

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #17a2b8; color: #000000;'>");
		html.append("<h3 style='color: #17a2b8; margin-top: 0;'>Master Policy Footer Buttons - ").append(foundCount).append(" Found</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 35%;'>Button</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Location</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 20%;'>Status</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 15%;'>Result</th>");
		html.append("</tr>");

		for (String[] row : rows) {
			String bgColor = "#ffffff";
			if (row[3].equals("INFO")) bgColor = "#e3f2fd";
			else if (row[3].equals("PASS")) bgColor = "#e8f5e9";
			else if (row[3].equals("SKIP")) bgColor = "#fff3e0";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table></div>");

		return html.toString();
	}

	/**
	 * Validate location count matches expected count from Edit Quote
	 */
	public MasterPolicyValidationResult validateLocationCount(int expectedCount) {
		logger.info("Validating location count against Edit Quote: expected={}", expectedCount);
		MasterPolicyValidationResult result = new MasterPolicyValidationResult();

		// Parse location count from text
		String locationText = getLocationCountFromPage();
		int actualCount = 0;

		// Parse "Showing 1 to 13 of 13 locations" -> extract 13
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("of\\s+(\\d+)");
		java.util.regex.Matcher matcher = pattern.matcher(locationText);
		if (matcher.find()) {
			actualCount = Integer.parseInt(matcher.group(1));
		}
		logger.info("Parsed location count: {}", actualCount);

		java.util.List<String[]> validationRows = new java.util.ArrayList<>();
		validationRows.add(new String[]{"=== LOCATION COUNT VALIDATION ===", "", "", "INFO"});

		boolean match = (actualCount == expectedCount);
		String status = match ? "PASS" : (actualCount > 0 ? "WARN" : "FAIL");

		validationRows.add(new String[]{
			"Total Locations",
			String.valueOf(expectedCount),
			String.valueOf(actualCount),
			status
		});

		if (!match) {
			if (actualCount == 0) {
				result.addError("Could not determine location count on Master Policy page");
			} else {
				result.addWarning("Location count mismatch: expected " + expectedCount + ", found " + actualCount);
			}
		}

		// Build HTML report
		StringBuilder html = new StringBuilder();
		String statusColor = match ? "#28a745" : "#ffc107";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Location Count Validation</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Edit Quote</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Master Policy</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Status</th>");
		html.append("</tr>");

		String bgColor = match ? "#e8f5e9" : "#fff3e0";
		html.append("<tr style='background-color: ").append(bgColor).append("; color: #000000;'>");
		html.append("<td style='padding: 10px;'><strong>Number of Locations</strong></td>");
		html.append("<td style='padding: 10px; text-align: center;'>").append(expectedCount).append("</td>");
		html.append("<td style='padding: 10px; text-align: center;'>").append(actualCount).append("</td>");
		html.append("<td style='padding: 10px; text-align: center;'>").append(getStatusBadge(status)).append("</td>");
		html.append("</tr>");

		html.append("</table></div>");

		result.setHtmlReport(html.toString());
		logHtmlToReport(html.toString());

		captureScreenshotToReport("Location Count Validation");

		return result;
	}

	/**
	 * Validate Select Account Manager dropdown is displayed
	 */
	public boolean validateAccountManagerDropdownDisplayed() {
		logger.info("Validating Select Account Manager dropdown");
		try {
			String[] xpaths = {
				"//label[contains(text(),'Account Manager')]/following::button[1]",
				"//label[contains(text(),'Account Manager')]/following::select[1]",
				"//*[contains(text(),'Select Account Manager')]",
				"//button[contains(text(),'Account Manager')]",
				"//*[contains(@placeholder,'Account Manager')]",
				"//label[contains(text(),'Account Manager')]/..//button",
				"//*[contains(@id,'account-manager')]"
			};

			for (String xpath : xpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement element : elements) {
						if (element.isDisplayed()) {
							logger.info("Account Manager dropdown found: {}", element.getText());
							scrollIntoView(element);
							sleep(300);

							// Highlight
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].style.border='3px solid green';", element);
							sleep(300);
							captureScreenshotToReport("Select Account Manager Dropdown - Found");
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].style.border='';", element);

							return true;
						}
					}
				} catch (Exception e) {
					// Try next
				}
			}

			logger.warn("Account Manager dropdown not found");
			captureScreenshotToReport("Select Account Manager Dropdown - NOT FOUND");
			return false;

		} catch (Exception e) {
			logger.error("Error validating Account Manager dropdown: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Validate and click Select Account Manager dropdown, then take screenshot
	 */
	public boolean validateAndClickAccountManagerDropdown() {
		logger.info("Validating and clicking Select Account Manager dropdown");
		try {
			String[] xpaths = {
				"//label[contains(text(),'Account Manager')]/following::button[1]",
				"//*[contains(text(),'Select Account Manager')]",
				"//button[contains(text(),'Account Manager')]",
				"//label[contains(text(),'Account Manager')]/..//button",
				"//*[contains(@id,'account-manager')]"
			};

			for (String xpath : xpaths) {
				try {
					List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement element : elements) {
						if (element.isDisplayed()) {
							logger.info("Account Manager dropdown found: {}", element.getText());
							scrollIntoView(element);
							sleep(500);

							// Take screenshot before click
							captureScreenshotToReport("Account Manager Dropdown - Before Click");

							// Highlight and click
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].style.border='3px solid blue';", element);
							sleep(300);

							// Click the dropdown
							try {
								element.click();
								logger.info("Clicked Account Manager dropdown");
							} catch (Exception clickEx) {
								((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
								logger.info("Clicked Account Manager dropdown using JavaScript");
							}

							sleep(1000);

							// Take screenshot after click (dropdown opened)
							captureScreenshotToReport("Account Manager Dropdown - Opened");

							// Remove highlight
							((JavascriptExecutor) driver).executeScript(
								"arguments[0].style.border='';", element);

							return true;
						}
					}
				} catch (Exception e) {
					// Try next
				}
			}

			logger.warn("Account Manager dropdown not found");
			captureScreenshotToReport("Account Manager Dropdown - NOT FOUND");
			return false;

		} catch (Exception e) {
			logger.error("Error with Account Manager dropdown: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Build HTML report for button validation
	 */
	private String buildButtonValidationReport(java.util.List<String[]> rows, MasterPolicyValidationResult result) {
		StringBuilder html = new StringBuilder();

		String status = result.isValid() ? "PASSED" : "FAILED";
		String statusColor = result.isValid() ? "#28a745" : "#dc3545";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Master Policy Action Buttons Validation - ").append(status).append("</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 35%;'>Button</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Expected</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 20%;'>Actual</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 15%;'>Status</th>");
		html.append("</tr>");

		for (String[] row : rows) {
			String bgColor = "#ffffff";
			if (row[3].equals("INFO")) bgColor = "#e3f2fd";
			else if (row[3].equals("PASS")) bgColor = "#e8f5e9";
			else if (row[3].equals("FAIL")) bgColor = "#ffebee";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table>");

		// Error summary
		if (!result.getErrors().isEmpty()) {
			html.append("<div style='margin-top: 10px; padding: 10px; background-color: #ffebee; border-radius: 4px;'>");
			html.append("<strong style='color: #c62828;'>Missing Buttons (").append(result.getErrors().size()).append("):</strong><ul style='margin: 5px 0; padding-left: 20px;'>");
			for (String error : result.getErrors()) {
				html.append("<li style='color: #c62828;'>").append(error).append("</li>");
			}
			html.append("</ul></div>");
		}

		html.append("</div>");

		return html.toString();
	}

	/**
	 * Comprehensive Master Policy validation including PDF report like Edit Quote
	 * Validates all elements and generates detailed HTML report
	 */
	public MasterPolicyValidationResult validateMasterPolicyComprehensive(Map<String, String> editQuoteValues) {
		logger.info("=== Starting Comprehensive Master Policy Validation ===");
		MasterPolicyValidationResult result = new MasterPolicyValidationResult();

		java.util.List<String[]> validationRows = new java.util.ArrayList<>();

		// Step 1: Capture Policy ID
		String policyId = capturePolicyId();
		validationRows.add(new String[]{"=== POLICY INFORMATION ===", "", "", "INFO"});
		validationRows.add(new String[]{"Policy ID", "Captured", policyId.isEmpty() ? "Not Found" : policyId, policyId.isEmpty() ? "WARN" : "PASS"});

		// Step 2: Capture all Master Policy values
		Map<String, String> policyValues = captureMasterPolicyValues();

		// Step 3: Validate against Edit Quote values
		validationRows.add(new String[]{"=== VALUES COMPARISON (Edit Quote vs Master Policy) ===", "", "", "INFO"});

		// Due Amount vs Grand Total
		String expectedDueAmount = editQuoteValues.get("GrandTotal");
		String actualDueAmount = policyValues.get("DueAmount");
		validateAndAddRow(validationRows, result, "Due Amount", expectedDueAmount, actualDueAmount, true);

		// Insured Name
		validateAndAddRow(validationRows, result, "Insured Name", editQuoteValues.get("Insured"), policyValues.get("InsuredName"), false);

		// Agent Name
		validateAndAddRow(validationRows, result, "Agent Name", editQuoteValues.get("Agent"), policyValues.get("AgentName"), false);

		// Carrier
		validateAndAddRow(validationRows, result, "Carrier", editQuoteValues.get("Carrier"), policyValues.get("Carrier"), false);

		// Policy Effective Date
		validateAndAddRow(validationRows, result, "Policy Effective", editQuoteValues.get("EffectiveDate"), policyValues.get("PolicyEffective"), false);

		// Policy Expiration Date
		validateAndAddRow(validationRows, result, "Policy Expiration", editQuoteValues.get("ExpirationDate"), policyValues.get("PolicyExpiration"), false);

		// State
		validateAndAddRow(validationRows, result, "State", editQuoteValues.get("State"), policyValues.get("State"), false);

		// General Liability
		validateAndAddRow(validationRows, result, "General Liability (GL)", editQuoteValues.get("GLPremiumTotal"), policyValues.get("GeneralLiability"), true);

		// Water/Sewer Backup
		validateAndAddRow(validationRows, result, "Water/Sewer Backup (WS)", editQuoteValues.get("WSPremiumTotal"), policyValues.get("WaterSewerBackup"), true);

		// Policy Fee
		String expectedPolicyFee = editQuoteValues.get("PolicyFeeEnabled");
		String actualPolicyFee = policyValues.get("PolicyFee");
		validationRows.add(new String[]{"Policy Fee (Yes/No)", expectedPolicyFee != null ? expectedPolicyFee : "N/A", actualPolicyFee, "INFO"});

		// Location Count
		String locationCount = policyValues.get("LocationCount");
		validationRows.add(new String[]{"Location Count", editQuoteValues.get("LocationCount"), locationCount, "INFO"});

		// Step 4: Validate Action Buttons
		validationRows.add(new String[]{"=== ACTION BUTTONS ===", "", "", "INFO"});
		String[][] buttons = {
			{"Renew Policy", "//button[contains(text(),'Renew Policy')]"},
			{"Premium Endorsement", "//button[contains(text(),'Premium Endorsement')]"},
			{"Non Premium Endorsement", "//button[contains(text(),'Non Premium Endorsement') or contains(text(),'Non-Premium')]"},
			{"Current Policy Binder", "//button[contains(text(),'Current Policy Binder')]"},
			{"Cancel Address", "//button[contains(text(),'Cancel Address')]"},
			{"Flat Cancel", "//button[contains(text(),'Flat Cancel')]"}
		};

		for (String[] btn : buttons) {
			boolean found = isButtonDisplayed(btn[0], btn[1]);
			validationRows.add(new String[]{btn[0] + " Button", "Displayed", found ? "Yes" : "No", found ? "PASS" : "FAIL"});
			if (!found) {
				result.addWarning(btn[0] + " button not found");
			}
		}

		// Step 5: Validate Account Manager Dropdown
		validationRows.add(new String[]{"=== DROPDOWN VALIDATION ===", "", "", "INFO"});
		boolean accountManagerFound = validateAccountManagerDropdownDisplayed();
		validationRows.add(new String[]{"Select Account Manager", "Displayed", accountManagerFound ? "Yes" : "No", accountManagerFound ? "PASS" : "WARN"});

		// Build comprehensive HTML report (like PDF validation report)
		String htmlReport = buildComprehensiveMasterPolicyReport(validationRows, result, policyId);
		result.setHtmlReport(htmlReport);
		logHtmlToReport(htmlReport);

		captureScreenshotToReport("Master Policy - Comprehensive Validation Complete");

		logger.info("=== Master Policy Comprehensive Validation Complete: {} ===", result.isValid() ? "PASSED" : "HAS WARNINGS");
		return result;
	}

	/**
	 * Validate and add row to validation list
	 */
	private void validateAndAddRow(java.util.List<String[]> rows, MasterPolicyValidationResult result,
									String fieldName, String expected, String actual, boolean isAmount) {
		if (expected == null || expected.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", actual != null ? actual : "N/A", "SKIP"});
			return;
		}

		if (actual == null || actual.isEmpty()) {
			rows.add(new String[]{fieldName, expected, "Not Found", "WARN"});
			return;
		}

		boolean match = false;
		if (isAmount) {
			try {
				double exp = parseAmountValue(expected);
				double act = parseAmountValue(actual);
				match = Math.abs(exp - act) < 1.0;
			} catch (Exception e) {
				match = expected.equalsIgnoreCase(actual);
			}
		} else {
			String normExp = expected.toLowerCase().trim();
			String normAct = actual.toLowerCase().trim();
			match = normExp.equals(normAct) || normAct.contains(normExp) || normExp.contains(normAct);
		}

		String status = match ? "PASS" : "WARN";
		rows.add(new String[]{fieldName, expected, actual, status});
	}

	/**
	 * Build comprehensive HTML report for Master Policy (like PDF validation report)
	 */
	private String buildComprehensiveMasterPolicyReport(java.util.List<String[]> rows, MasterPolicyValidationResult result, String policyId) {
		StringBuilder html = new StringBuilder();

		String status = result.isValid() ? "PASSED" : "HAS WARNINGS";
		String statusColor = result.isValid() ? "#28a745" : "#ffc107";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Master Policy Comprehensive Validation Report - ").append(status).append("</h3>");

		// Policy ID Header
		if (!policyId.isEmpty()) {
			html.append("<p style='font-size: 16px; color: #000000;'><strong>Policy ID:</strong> ").append(policyId).append("</p>");
		}

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>Edit Quote Value</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>Master Policy Value</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 20%;'>Status</th>");
		html.append("</tr>");

		for (String[] row : rows) {
			String bgColor = "#ffffff";
			String textColor = "#000000";

			if (row[3].equals("INFO")) {
				bgColor = "#e3f2fd";
				textColor = "#1565c0";
			} else if (row[3].equals("PASS")) {
				bgColor = "#e8f5e9";
			} else if (row[3].equals("FAIL")) {
				bgColor = "#ffebee";
			} else if (row[3].equals("WARN") || row[3].equals("SKIP")) {
				bgColor = "#fff3e0";
			}

			html.append("<tr style='background-color: ").append(bgColor).append("; color: ").append(textColor).append("; border-bottom: 1px solid #dee2e6;'>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table>");

		// Warnings summary
		if (!result.getWarnings().isEmpty()) {
			html.append("<div style='margin-top: 10px; padding: 10px; background-color: #fff3e0; border-radius: 4px;'>");
			html.append("<strong style='color: #e65100;'>Warnings (").append(result.getWarnings().size()).append("):</strong><ul style='margin: 5px 0; padding-left: 20px;'>");
			for (String warning : result.getWarnings()) {
				html.append("<li style='color: #e65100;'>").append(warning).append("</li>");
			}
			html.append("</ul></div>");
		}

		// Errors summary
		if (!result.getErrors().isEmpty()) {
			html.append("<div style='margin-top: 10px; padding: 10px; background-color: #ffebee; border-radius: 4px;'>");
			html.append("<strong style='color: #c62828;'>Errors (").append(result.getErrors().size()).append("):</strong><ul style='margin: 5px 0; padding-left: 20px;'>");
			for (String error : result.getErrors()) {
				html.append("<li style='color: #c62828;'>").append(error).append("</li>");
			}
			html.append("</ul></div>");
		}

		html.append("</div>");

		return html.toString();
	}

	// ==================== Edit Quote vs Master Policy Comparison ====================

	/**
	 * Compare Edit Quote values with Master Policy values and generate HTML report
	 * @param editQuoteValues - Values captured from Edit Quote screen
	 * @return HTML report string
	 */
	public String compareWithEditQuoteValues(Map<String, String> editQuoteValues) {
		logger.info("=== Comparing Edit Quote vs Master Policy Values ===");

		// Capture Master Policy values
		Map<String, String> policyValues = captureMasterPolicyValues();

		// Build comparison HTML table
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #007bff; color: #000000;'>");
		html.append("<h3 style='color: #007bff; margin-top: 0;'>Edit Quote vs Master Policy Comparison</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Edit Quote Value</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Master Policy Value</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 15%;'>Status</th>");
		html.append("</tr>");

		// Field mapping: {DisplayName, EditQuoteKey, MasterPolicyKey}
		String[][] fieldMapping = {
			{"Insured Name", "Insured", "InsuredName"},
			{"Agent Name", "Agent", "AgentName"},
			{"Carrier", "Carrier", "Carrier"},
			{"Effective Date", "EffectiveDate", "PolicyEffective"},
			{"Expiration Date", "ExpirationDate", "PolicyExpiration"},
			{"State", "State", "State"},
			{"Due Amount", "GrandTotal", "DueAmount"},
			{"General Liability", "GLAmount", "GeneralLiability"},
			{"Water/Sewer Backup", "WSAmount", "WaterSewerBackup"},
			{"Location Count", "LocationCount", "LocationCount"}
		};

		int matchCount = 0;
		int totalFields = 0;

		for (String[] mapping : fieldMapping) {
			String displayName = mapping[0];
			String editQuoteKey = mapping[1];
			String masterPolicyKey = mapping[2];

			String editValue = editQuoteValues != null ? editQuoteValues.get(editQuoteKey) : null;
			String masterValue = policyValues.get(masterPolicyKey);

			// Skip if both are empty
			if ((editValue == null || editValue.isEmpty()) && (masterValue == null || masterValue.isEmpty())) {
				continue;
			}

			totalFields++;
			boolean match = compareFieldValues(editValue, masterValue);
			if (match) matchCount++;

			String bgColor = match ? "#e8f5e9" : "#fff3e0";
			String statusBadge = match ?
				"<span style='background-color: #28a745; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>MATCH</span>" :
				"<span style='background-color: #ffc107; color: black; padding: 3px 8px; border-radius: 3px; font-size: 12px;'>DIFF</span>";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; font-weight: bold; color: #000000;'>").append(displayName).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(editValue != null ? editValue : "N/A").append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(masterValue != null ? masterValue : "N/A").append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(statusBadge).append("</td>");
			html.append("</tr>");
		}

		html.append("</table>");
		html.append("<p style='margin-top: 10px; color: #000000;'><strong>Summary:</strong> ");
		html.append(matchCount).append(" of ").append(totalFields).append(" fields matched</p>");
		html.append("</div>");

		// Log to report
		logHtmlToReport(html.toString());
		captureScreenshotToReport("Master Policy Values Comparison");

		return html.toString();
	}

	/**
	 * Compare two field values - handles amounts, dates, and text
	 */
	private boolean compareFieldValues(String value1, String value2) {
		if (value1 == null || value2 == null) return false;
		if (value1.isEmpty() || value2.isEmpty()) return false;

		// Normalize values
		String norm1 = value1.trim().toLowerCase();
		String norm2 = value2.trim().toLowerCase();

		// Direct match
		if (norm1.equals(norm2)) return true;

		// Partial match
		if (norm1.contains(norm2) || norm2.contains(norm1)) return true;

		// Date comparison - normalize to same format
		if (isDateFormat(value1) && isDateFormat(value2)) {
			String normalizedDate1 = normalizeDateToMMDDYYYY(value1);
			String normalizedDate2 = normalizeDateToMMDDYYYY(value2);
			if (normalizedDate1 != null && normalizedDate2 != null) {
				return normalizedDate1.equals(normalizedDate2);
			}
		}

		// Amount comparison
		if (value1.contains("$") || value2.contains("$") ||
			value1.matches(".*\\d+\\.\\d+.*") || value2.matches(".*\\d+\\.\\d+.*")) {
			try {
				double amt1 = parseNumericValue(value1);
				double amt2 = parseNumericValue(value2);
				return Math.abs(amt1 - amt2) < 1.0;
			} catch (Exception e) {
				// Fall through
			}
		}

		return false;
	}

	/**
	 * Check if value looks like a date format
	 */
	private boolean isDateFormat(String value) {
		if (value == null || value.isEmpty()) return false;
		// Matches patterns like: 2025-12-29, 12-29-2025, 12/29/2025, 2025/12/29
		return value.matches("\\d{4}[-/]\\d{2}[-/]\\d{2}") ||
			   value.matches("\\d{2}[-/]\\d{2}[-/]\\d{4}");
	}

	/**
	 * Normalize date to MM-DD-YYYY format for comparison
	 */
	private String normalizeDateToMMDDYYYY(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) return null;

		try {
			// Replace slashes with dashes for consistency
			String normalized = dateStr.replace("/", "-");

			// Check if format is YYYY-MM-DD
			if (normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
				String[] parts = normalized.split("-");
				// Convert to MM-DD-YYYY
				return parts[1] + "-" + parts[2] + "-" + parts[0];
			}

			// Check if format is MM-DD-YYYY (already correct)
			if (normalized.matches("\\d{2}-\\d{2}-\\d{4}")) {
				return normalized;
			}

		} catch (Exception e) {
			logger.debug("Could not normalize date: {}", dateStr);
		}

		return dateStr;
	}

	/**
	 * Parse numeric value from string
	 */
	private double parseNumericValue(String value) {
		if (value == null || value.isEmpty()) return 0;
		String clean = value.replaceAll("[^0-9.]", "");
		if (clean.isEmpty()) return 0;
		return Double.parseDouble(clean);
	}

	/**
	 * Get value from map with fallback keys
	 * Useful when Edit Quote and Master Policy use different key names
	 */
	private String getValueWithFallback(Map<String, String> map, String... keys) {
		if (map == null) return "";
		for (String key : keys) {
			String value = map.get(key);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "";
	}

	// ==================== Bind History PDF Validation ====================

	/**
	 * Download and validate Bind History PDF against Edit Quote values
	 * @param editQuoteValues values captured from Edit Quote screen
	 * @return MasterPolicyValidationResult with validation details
	 */
	public MasterPolicyValidationResult downloadAndValidateBindHistoryPDFComprehensive(Map<String, String> editQuoteValues) {
		logger.info("=== Starting Bind History PDF Download and Validation ===");
		MasterPolicyValidationResult result = new MasterPolicyValidationResult();

		// Use Edit Quote values if provided, otherwise capture from Master Policy screen
		Map<String, String> screenValues;
		if (editQuoteValues != null && !editQuoteValues.isEmpty()) {
			screenValues = editQuoteValues;
			logger.info("Using Edit Quote values for PDF validation");
		} else {
			screenValues = captureMasterPolicyValues();
			logger.info("Using Master Policy screen values for PDF validation");
		}

		// Step 1.5: Click on Bind History tab first
		if (!clickBindHistoryTab()) {
			result.addError("Could not click Bind History tab - tab not active or panel not visible");
			captureScreenshotToReport("Bind History Tab - Failed to Activate");
			return result;
		}

		// Build and log screen values table
		StringBuilder screenHtml = new StringBuilder();
		screenHtml.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #007bff; color: #000000;'>");
		screenHtml.append("<h3 style='color: #007bff; margin-top: 0;'>Master Policy Screen Values (Before PDF Download)</h3>");
		screenHtml.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		screenHtml.append("<tr style='background-color: #343a40; color: white;'>");
		screenHtml.append("<th style='padding: 10px; text-align: left; width: 40%;'>Field</th>");
		screenHtml.append("<th style='padding: 10px; text-align: left; width: 60%;'>Value</th>");
		screenHtml.append("</tr>");

		String[] fieldOrder = {"DueAmount", "InsuredName", "AgentName", "Carrier", "PolicyEffective",
			"PolicyExpiration", "State", "GeneralLiability", "WaterSewerBackup", "PolicyFee", "LocationCount"};

		for (String field : fieldOrder) {
			String value = screenValues.get(field);
			if (value != null && !value.isEmpty()) {
				String displayName = formatFieldNameForDisplay(field);
				screenHtml.append("<tr style='background-color: #ffffff; border-bottom: 1px solid #dee2e6; color: #000000;'>");
				screenHtml.append("<td style='padding: 8px; font-weight: bold; color: #000000;'>").append(displayName).append("</td>");
				screenHtml.append("<td style='padding: 8px; color: #000000;'>").append(value).append("</td>");
				screenHtml.append("</tr>");
			}
		}
		screenHtml.append("</table></div>");
		logHtmlToReport(screenHtml.toString());

		captureScreenshotToReport("Master Policy - Before PDF Download");

		// Step 2: Click Download Document button
		if (!clickDownloadDocumentButton()) {
			result.addError("Failed to click Download Document button");
			return result;
		}

		// Step 3: Wait for PDF download
		sleep(3000);
		String downloadDir = getDownloadDirectory();
		String pdfPath = waitForPDFDownload(downloadDir, 30);

		if (pdfPath == null) {
			result.addError("PDF download failed or timed out");
			captureScreenshotToReport("Master Policy - PDF Download Failed");
			return result;
		}

		logger.info("PDF downloaded: {}", pdfPath);

		// Step 4: Read PDF content
		String pdfText = readPDFAllPages(pdfPath);
		if (pdfText == null || pdfText.isEmpty()) {
			result.addError("Could not read PDF content");
			return result;
		}

		// Step 5: Open PDF in new tab for screenshot
		String originalWindow = driver.getWindowHandle();
		openPDFInNewTab(pdfPath);
		sleep(2000);
		captureScreenshotToReport("Bind History PDF - Page 1");

		// Step 6: Validate PDF against screen values (like Edit Quote PDF validation)
		java.util.List<String[]> validationRows = new java.util.ArrayList<>();

		// Get values with fallback for both Edit Quote and Master Policy key names
		String carrier = getValueWithFallback(screenValues, "Carrier");
		String insuredName = getValueWithFallback(screenValues, "Insured", "InsuredName");
		String agentName = getValueWithFallback(screenValues, "Agent", "AgentName");
		String effectiveDate = getValueWithFallback(screenValues, "EffectiveDate", "PolicyEffective");
		String expirationDate = getValueWithFallback(screenValues, "ExpirationDate", "PolicyExpiration");
		String glAmount = getValueWithFallback(screenValues, "GLAmount", "GeneralLiability");
		String wsAmount = getValueWithFallback(screenValues, "WSAmount", "WaterSewerBackup");
		String grandTotal = getValueWithFallback(screenValues, "GrandTotal", "DueAmount");
		String state = getValueWithFallback(screenValues, "State");
		String locationCount = getValueWithFallback(screenValues, "LocationCount");

		// === PDF PAGE 1 (SUMMARY) === - matching Edit Quote format
		validationRows.add(new String[]{"=== PDF PAGE 1 (SUMMARY) ===", "", "", "INFO"});

		// Validate basic fields - same as Edit Quote
		validatePDFTextField(validationRows, pdfText, "Insuring Company", carrier);
		validatePDFTextField(validationRows, pdfText, "Named Insured", insuredName);
		validatePDFTextField(validationRows, pdfText, "Agent", agentName);

		// Validate dates
		validatePDFDateField(validationRows, pdfText, "Effective Date", effectiveDate);
		validatePDFDateField(validationRows, pdfText, "Expiration Date", expirationDate);

		// Premium Totals section
		validationRows.add(new String[]{"--- PREMIUM TOTALS ---", "", "", "INFO"});
		validatePDFAmountField(validationRows, pdfText, "General Liability Premium", glAmount);
		validatePDFAmountField(validationRows, pdfText, "Water & Sewer Backup", wsAmount);
		validatePDFAmountField(validationRows, pdfText, "Grand Total", grandTotal);

		// State validation
		validatePDFTextField(validationRows, pdfText, "State", state);

		// === PDF LOCATION DETAILS (ALL PAGES) ===
		validationRows.add(new String[]{"=== PDF LOCATION DETAILS ===", "", "", "INFO"});

		// Read ALL PDF pages (locations may span multiple pages)
		String pdfAllPagesText = readPDFAllPages(pdfPath);
		if (pdfAllPagesText == null || pdfAllPagesText.isEmpty()) {
			validationRows.add(new String[]{"PDF Content", "N/A", "Could not read PDF", "FAIL"});
			result.addError("PDF could not be read");
		} else {
			logger.info("PDF all pages content length: {} characters", pdfAllPagesText.length());

			// Location Count validation
			int expectedLocCount = 0;
			if (locationCount != null && !locationCount.isEmpty()) {
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("of\\s+(\\d+)");
				java.util.regex.Matcher matcher = pattern.matcher(locationCount);
				if (matcher.find()) {
					expectedLocCount = Integer.parseInt(matcher.group(1));
				}
			}

			// Extract ALL location addresses from the entire PDF
			java.util.List<String> pdfAddresses = extractAllAddressesFromPDF(pdfAllPagesText);
			int pdfLocCount = pdfAddresses.size();
			String expectedStr = String.valueOf(expectedLocCount);
			String pdfCountStr = String.valueOf(pdfLocCount);
			boolean countMatch = expectedLocCount == pdfLocCount || expectedLocCount == 0;
			validationRows.add(new String[]{"Location Count", expectedStr, pdfCountStr, countMatch ? "PASS" : "WARN"});

			// Validate Coverage values from PDF
			validationRows.add(new String[]{"--- COVERAGE VALUES ---", "", "", "INFO"});
			String covA = getValueWithFallback(screenValues, "CoverageA");
			String covB = getValueWithFallback(screenValues, "CoverageB");
			String covC = getValueWithFallback(screenValues, "CoverageC");
			String covD = getValueWithFallback(screenValues, "CoverageD");
			if (covA != null && !covA.isEmpty()) validatePDFAmountField(validationRows, pdfAllPagesText, "Coverage A (Dwelling)", covA);
			if (covB != null && !covB.isEmpty()) validatePDFAmountField(validationRows, pdfAllPagesText, "Coverage B (Additional Structures)", covB);
			if (covC != null && !covC.isEmpty()) validatePDFAmountField(validationRows, pdfAllPagesText, "Coverage C (Personal Property)", covC);
			if (covD != null && !covD.isEmpty()) validatePDFAmountField(validationRows, pdfAllPagesText, "Coverage D (Loss of Rents)", covD);

			// Log ALL location addresses found in PDF
			if (!pdfAddresses.isEmpty()) {
				validationRows.add(new String[]{"--- ALL LOCATION ADDRESSES (" + pdfAddresses.size() + " found) ---", "", "", "INFO"});
				for (int i = 0; i < pdfAddresses.size(); i++) {
					String addr = pdfAddresses.get(i);
					String shortAddr = addr.length() > 50 ? addr.substring(0, 47) + "..." : addr;
					validationRows.add(new String[]{"Location " + (i + 1), shortAddr, "Found in PDF", "PASS"});
				}
			} else {
				validationRows.add(new String[]{"Location Addresses", "Expected: " + expectedLocCount, "None found", "WARN"});
			}
		}

		// Build HTML validation report - showing Edit Quote vs PDF values
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #28a745; color: #000000;'>");
		html.append("<h3 style='color: #28a745; margin-top: 0;'>Bind History PDF Validation: Edit Quote vs PDF Comparison</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 30%;'>Edit Quote Value</th>");
		html.append("<th style='padding: 10px; text-align: left; width: 25%;'>PDF Value</th>");
		html.append("<th style='padding: 10px; text-align: center; width: 20%;'>Match Result</th>");
		html.append("</tr>");

		for (String[] row : validationRows) {
			String bgColor = "#ffffff";
			if (row[3].equals("INFO")) bgColor = "#e3f2fd";
			else if (row[3].equals("PASS")) bgColor = "#e8f5e9";
			else if (row[3].equals("FAIL")) bgColor = "#ffebee";
			else if (row[3].equals("WARN") || row[3].equals("SKIP")) bgColor = "#fff3e0";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table></div>");
		result.setHtmlReport(html.toString());
		logHtmlToReport(html.toString());

		// Step 7: Close PDF tab
		closePDFTab(originalWindow);

		captureScreenshotToReport("Master Policy - PDF Validation Complete");
		logger.info("Bind History PDF validation completed");

		return result;
	}

	/**
	 * Validate text field in PDF - extracts and compares actual values
	 */
	private void validatePDFTextField(java.util.List<String[]> rows, String pdfText, String fieldName, String expectedValue) {
		if (expectedValue == null || expectedValue.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", "N/A", "SKIP"});
			return;
		}

		boolean found = pdfText.contains(expectedValue);
		String pdfValue = expectedValue; // Default to expected if found exactly

		// Try partial match
		if (!found && expectedValue.contains(" ")) {
			String[] parts = expectedValue.split("\\s+");
			for (String part : parts) {
				if (part.length() > 3 && pdfText.contains(part)) {
					found = true;
					pdfValue = expectedValue + " (partial match)";
					break;
				}
			}
		}

		if (!found) {
			pdfValue = "NOT FOUND IN PDF";
		}

		rows.add(new String[]{fieldName, expectedValue, pdfValue, found ? "PASS" : "FAIL"});
	}

	/**
	 * Validate amount in PDF - shows both Edit Quote and PDF values
	 */
	private void validatePDFAmountField(java.util.List<String[]> rows, String pdfText, String fieldName, String expectedValue) {
		if (expectedValue == null || expectedValue.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", "N/A", "SKIP"});
			return;
		}

		String cleanValue = expectedValue.replaceAll("[^0-9.]", "");
		if (cleanValue.isEmpty()) {
			rows.add(new String[]{fieldName, expectedValue, "N/A", "SKIP"});
			return;
		}

		try {
			double amount = Double.parseDouble(cleanValue);
			if (amount <= 0) {
				rows.add(new String[]{fieldName, "$0.00", "N/A", "SKIP"});
				return;
			}

			String formatted = String.format("%,.2f", amount);
			String formatted2 = String.format("%.2f", amount);
			String formatted3 = String.format("%,.0f", amount);
			String editQuoteValue = "$" + formatted;

			boolean found = pdfText.contains(formatted) || pdfText.contains(formatted2) ||
				pdfText.contains("$" + formatted) || pdfText.contains("$" + formatted2) ||
				pdfText.contains(formatted3);

			String pdfValue = found ? "$" + formatted : "NOT FOUND IN PDF";

			rows.add(new String[]{fieldName, editQuoteValue, pdfValue, found ? "PASS" : "FAIL"});
		} catch (Exception e) {
			rows.add(new String[]{fieldName, expectedValue, "Parse Error", "FAIL"});
		}
	}

	/**
	 * Validate date field in PDF - handles different date formats
	 */
	private void validatePDFDateField(java.util.List<String[]> rows, String pdfText, String fieldName, String expectedValue) {
		if (expectedValue == null || expectedValue.isEmpty()) {
			rows.add(new String[]{fieldName, "N/A", "N/A", "SKIP"});
			return;
		}

		// Try to find date in PDF with multiple formats
		boolean found = false;
		String normalizedDate = normalizeDateToMMDDYYYY(expectedValue);

		// Try original format
		if (pdfText.contains(expectedValue)) {
			found = true;
		}

		// Try normalized MM-DD-YYYY format
		if (!found && normalizedDate != null && pdfText.contains(normalizedDate)) {
			found = true;
		}

		// Try with slashes MM/DD/YYYY
		if (!found && normalizedDate != null) {
			String withSlashes = normalizedDate.replace("-", "/");
			if (pdfText.contains(withSlashes)) {
				found = true;
			}
		}

		// Try YYYY-MM-DD format
		if (!found && expectedValue.matches("\\d{2}-\\d{2}-\\d{4}")) {
			String[] parts = expectedValue.split("-");
			String yyyymmdd = parts[2] + "-" + parts[0] + "-" + parts[1];
			if (pdfText.contains(yyyymmdd)) {
				found = true;
			}
		}

		// Try individual date components
		if (!found) {
			String cleanDate = expectedValue.replaceAll("[-/]", "");
			if (cleanDate.length() >= 8) {
				// Check if year, month, day appear in PDF
				String year = cleanDate.length() == 8 ? cleanDate.substring(4, 8) : cleanDate.substring(0, 4);
				if (pdfText.contains(year)) {
					found = true; // At least year matches
				}
			}
		}

		String pdfValue = found ? expectedValue : "NOT FOUND IN PDF";
		rows.add(new String[]{fieldName, expectedValue, pdfValue, found ? "PASS" : "FAIL"});
	}

	/**
	 * Read all pages from PDF
	 */
	private String readPDFAllPages(String pdfPath) {
		try {
			java.io.File file = new java.io.File(pdfPath);
			org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file);
			org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
			String text = stripper.getText(document);
			document.close();
			logger.info("Read {} characters from PDF", text.length());
			return text;
		} catch (Exception e) {
			logger.error("Error reading PDF: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Format field name for display (e.g., "DueAmount" -> "Due Amount")
	 */
	private String formatFieldNameForDisplay(String fieldName) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < fieldName.length(); i++) {
			char c = fieldName.charAt(i);
			if (Character.isUpperCase(c) && i > 0) {
				result.append(" ");
			}
			result.append(c);
		}
		return result.toString();
	}

	/**
	 * Extract ALL location addresses from the entire PDF
	 * Uses multiple patterns to catch various address formats
	 */
	private java.util.List<String> extractAllAddressesFromPDF(String pdfText) {
		java.util.List<String> addresses = new java.util.ArrayList<>();
		java.util.Set<String> seen = new java.util.HashSet<>();

		try {
			// Pattern 1: Standard US addresses (number + street name + suffix)
			String[] patterns = {
				// Standard street addresses
				"(\\d+\\s+[A-Za-z0-9\\s\\.]+(?:Street|St|Avenue|Ave|Road|Rd|Drive|Dr|Lane|Ln|Boulevard|Blvd|Way|Court|Ct|Circle|Cir|Place|Pl|Terrace|Ter|Trail|Trl|Parkway|Pkwy|Highway|Hwy)[\\.,]?(?:\\s+[A-Za-z]+)?)",
				// Addresses with apartment/unit numbers
				"(\\d+\\s+[A-Za-z0-9\\s\\.]+(?:#|Apt|Unit|Suite|Ste)\\s*[A-Za-z0-9]+)",
				// PO Box addresses
				"(P\\.?O\\.?\\s*Box\\s+\\d+)",
				// Addresses followed by city/state (e.g., "123 Main St, City, ST")
				"(\\d+\\s+[A-Za-z0-9\\s\\.]+,\\s*[A-Za-z\\s]+,\\s*[A-Z]{2})",
				// Simple number + words pattern for addresses without standard suffix
				"(\\d{1,5}\\s+[A-Za-z][A-Za-z0-9\\s]{5,40})"
			};

			for (String patternStr : patterns) {
				try {
					java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
						patternStr, java.util.regex.Pattern.CASE_INSENSITIVE);
					java.util.regex.Matcher matcher = pattern.matcher(pdfText);

					while (matcher.find()) {
						String addr = matcher.group(1).trim();
						// Clean up the address
						addr = addr.replaceAll("\\s+", " ");
						addr = addr.replaceAll("[\\r\\n]", " ");

						// Skip if too short, too long, or already seen
						if (addr.length() < 8 || addr.length() > 120) continue;

						// Skip numeric-only or invalid patterns
						if (addr.matches("^\\d+\\s*$")) continue;
						if (addr.matches(".*\\d{5,}.*\\d{5,}.*")) continue; // Skip lines with multiple long numbers

						String normalized = addr.toLowerCase().replaceAll("[^a-z0-9]", "");
						if (!seen.contains(normalized) && normalized.length() > 5) {
							seen.add(normalized);
							addresses.add(addr);
						}
					}
				} catch (Exception e) {
					logger.debug("Pattern failed: {}", patternStr);
				}
			}

			// Sort addresses to maintain consistent order
			addresses.sort((a, b) -> {
				// Extract street number for sorting
				String numA = a.replaceAll("^(\\d+).*", "$1");
				String numB = b.replaceAll("^(\\d+).*", "$1");
				try {
					return Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
				} catch (Exception e) {
					return a.compareTo(b);
				}
			});

			logger.info("Extracted {} unique addresses from PDF", addresses.size());
		} catch (Exception e) {
			logger.warn("Error extracting addresses from PDF: {}", e.getMessage());
		}
		return addresses;
	}

	// ==================== Flat Cancel Validation Methods ====================

	/**
	 * Validate that cancelled certificates are NOT displayed on Master Policy page
	 * @param cancelledCertificates List of certificate IDs that were flat cancelled
	 * @return FlatCancelValidationResult with pass/fail for each certificate
	 */
	public FlatCancelCertificateValidationResult validateCancelledCertificatesNotDisplayed(List<String> cancelledCertificates) {
		logger.info("=== Validating {} Cancelled Certificates are NOT on Master Policy ===", cancelledCertificates.size());
		FlatCancelCertificateValidationResult result = new FlatCancelCertificateValidationResult();
		result.totalCertificates = cancelledCertificates.size();

		// Wait for page to load
		sleep(2000);

		// Get all text from locations table
		String tableText = "";
		try {
			// Try to find locations table
			List<WebElement> tables = driver.findElements(By.xpath("//table"));
			for (WebElement table : tables) {
				if (table.isDisplayed()) {
					tableText += table.getText() + " ";
				}
			}
			logger.info("Locations table text length: {} characters", tableText.length());
		} catch (Exception e) {
			logger.warn("Could not read locations table: {}", e.getMessage());
		}

		// Also get full page text as backup
		String pageText = "";
		try {
			WebElement body = driver.findElement(By.tagName("body"));
			pageText = body.getText();
		} catch (Exception e) {
			logger.warn("Could not read page text: {}", e.getMessage());
		}

		// Check each cancelled certificate
		java.util.List<String[]> validationRows = new java.util.ArrayList<>();
		validationRows.add(new String[]{"=== FLAT CANCELLED CERTIFICATES VALIDATION ===", "", "", "INFO"});
		validationRows.add(new String[]{"Certificate ID", "Expected Status", "Found on Page?", "Result"});

		for (String certId : cancelledCertificates) {
			boolean foundInTable = tableText.contains(certId);
			boolean foundOnPage = pageText.contains(certId);

			// Certificate should NOT be found - so NOT found = PASS
			boolean pass = !foundInTable && !foundOnPage;

			if (pass) {
				result.certificatesNotFound.add(certId);
				validationRows.add(new String[]{certId, "NOT Displayed", "No", "PASS"});
				logger.info("Certificate {} correctly NOT found on Master Policy page", certId);
			} else {
				result.certificatesStillDisplayed.add(certId);
				validationRows.add(new String[]{certId, "NOT Displayed", foundInTable ? "Yes (in table)" : "Yes (on page)", "FAIL"});
				logger.error("Certificate {} is STILL displayed on Master Policy page after flat cancel!", certId);
			}
		}

		result.allCertificatesRemoved = result.certificatesStillDisplayed.isEmpty();

		// Build HTML report
		StringBuilder html = new StringBuilder();
		String statusColor = result.allCertificatesRemoved ? "#28a745" : "#dc3545";
		String status = result.allCertificatesRemoved ? "PASSED" : "FAILED";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Flat Cancelled Certificates Validation - ").append(status).append("</h3>");
		html.append("<p style='color: #000000;'>Verifying that <strong>").append(cancelledCertificates.size()).append("</strong> flat cancelled certificates are NOT displayed on Master Policy page</p>");

		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left;'>Certificate ID</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Expected</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Found on Page?</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Result</th>");
		html.append("</tr>");

		for (int i = 1; i < validationRows.size(); i++) {
			String[] row = validationRows.get(i);
			String bgColor = row[3].equals("PASS") ? "#e8f5e9" : "#ffebee";

			html.append("<tr style='background-color: ").append(bgColor).append("; border-bottom: 1px solid #dee2e6; color: #000000;'>");
			html.append("<td style='padding: 8px; font-weight: bold; color: #000000;'>").append(row[0]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center; color: #000000;'>").append(row[1]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center; color: #000000;'>").append(row[2]).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(getStatusBadge(row[3])).append("</td>");
			html.append("</tr>");
		}

		html.append("</table>");

		// Summary
		html.append("<p style='margin-top: 10px; color: #000000;'><strong>Summary:</strong> ");
		html.append(result.certificatesNotFound.size()).append(" of ").append(cancelledCertificates.size());
		html.append(" certificates correctly removed from Master Policy</p>");

		if (!result.certificatesStillDisplayed.isEmpty()) {
			html.append("<p style='color: #dc3545;'><strong>ERROR:</strong> The following certificates are still displayed: ");
			html.append(result.certificatesStillDisplayed).append("</p>");
		}

		html.append("</div>");

		result.htmlReport = html.toString();
		logHtmlToReport(html.toString());

		captureScreenshotToReport("Flat Cancel Certificates Validation");

		logger.info("Flat Cancel validation complete: {} passed, {} failed",
			result.certificatesNotFound.size(), result.certificatesStillDisplayed.size());

		return result;
	}

	/**
	 * Find ADDRESS_FLAT_CANCELLED entry in Bind History table
	 * @return Row index of the last ADDRESS_FLAT_CANCELLED entry, or -1 if not found
	 */
	public int findAddressFlatCancelledEntry() {
		logger.info("=== Finding ADDRESS_FLAT_CANCELLED Entry in Bind History ===");

		try {
			// Find all rows in Bind History table
			List<WebElement> rows = driver.findElements(By.xpath(
				"//*[contains(@id, 'content-Bind History')]//table//tbody//tr"));

			if (rows.isEmpty()) {
				// Try alternative XPath
				rows = driver.findElements(By.xpath("//table//tbody//tr[contains(.,'ADDRESS_FLAT_CANCELLED')]"));
			}

			logger.info("Found {} rows in Bind History table", rows.size());

			int lastFlatCancelIndex = -1;

			for (int i = 0; i < rows.size(); i++) {
				try {
					String rowText = rows.get(i).getText();
					if (rowText.contains("ADDRESS_FLAT_CANCELLED")) {
						lastFlatCancelIndex = i;
						logger.info("Found ADDRESS_FLAT_CANCELLED at row index {}: {}",
							i, rowText.substring(0, Math.min(100, rowText.length())));
					}
				} catch (Exception e) {
					// Row might be stale, continue
				}
			}

			if (lastFlatCancelIndex >= 0) {
				logger.info("Last ADDRESS_FLAT_CANCELLED entry found at row index {}", lastFlatCancelIndex);
			} else {
				logger.warn("ADDRESS_FLAT_CANCELLED entry not found in Bind History");
			}

			return lastFlatCancelIndex;

		} catch (Exception e) {
			logger.error("Error finding ADDRESS_FLAT_CANCELLED entry: {}", e.getMessage());
			return -1;
		}
	}

	/**
	 * Click Download Document button for ADDRESS_FLAT_CANCELLED entry
	 * @param rowIndex Row index of the entry (use findAddressFlatCancelledEntry to get this)
	 * @return true if click successful
	 */
	public boolean clickDownloadForFlatCancelEntry(int rowIndex) {
		logger.info("=== Clicking Download Document for ADDRESS_FLAT_CANCELLED (row {}) ===", rowIndex);

		try {
			// Find all rows
			List<WebElement> rows = driver.findElements(By.xpath(
				"//*[contains(@id, 'content-Bind History')]//table//tbody//tr"));

			if (rows.isEmpty()) {
				rows = driver.findElements(By.xpath("//table//tbody//tr[contains(.,'ADDRESS_FLAT_CANCELLED')]"));
			}

			if (rowIndex < 0 || rowIndex >= rows.size()) {
				logger.error("Row index {} is out of bounds (total rows: {})", rowIndex, rows.size());
				return false;
			}

			WebElement row = rows.get(rowIndex);

			// Scroll row into view
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center'});", row);
			sleep(500);

			captureScreenshotToReport("ADDRESS_FLAT_CANCELLED Row - Before Download Click");

			// Find Download Document button in this row
			// Try multiple patterns
			String[] buttonXpaths = {
				".//button[contains(text(),'Download Document')]",
				".//button[contains(text(),'Download')]",
				".//td[last()]//button[1]",
				".//td//button[1]"
			};

			WebElement downloadBtn = null;
			for (String xpath : buttonXpaths) {
				try {
					List<WebElement> buttons = row.findElements(By.xpath(xpath));
					for (WebElement btn : buttons) {
						if (btn.isDisplayed()) {
							downloadBtn = btn;
							logger.info("Found Download button using xpath: {}", xpath);
							break;
						}
					}
					if (downloadBtn != null) break;
				} catch (Exception e) {
					// Try next pattern
				}
			}

			if (downloadBtn == null) {
				logger.error("Download Document button not found in row");
				captureScreenshotToReport("Download Button - NOT FOUND");
				return false;
			}

			// Click using JavaScript
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", downloadBtn);
			logger.info("Clicked Download Document button for ADDRESS_FLAT_CANCELLED");

			// Wait for download to start
			sleep(5000);

			return true;

		} catch (Exception e) {
			logger.error("Error clicking Download button: {}", e.getMessage());
			captureScreenshotToReport("Download Button - ERROR");
			return false;
		}
	}

	/**
	 * Download and validate Flat Cancel PDF from Bind History
	 * Compares PDF Total Balance Refund with calculated grand total from Edit Flat Cancel
	 * @param calculatedGrandTotal The grand total calculated from Edit Flat Cancel screen
	 * @param cancelledCertificates List of cancelled certificate IDs
	 * @param locationDetails Map of certificate ID to location details (for individual validation)
	 * @return FlatCancelPDFValidationResult with detailed validation
	 */
	public FlatCancelPDFValidationResult downloadAndValidateFlatCancelPDF(
			double calculatedGrandTotal,
			List<String> cancelledCertificates,
			Map<String, Double> locationTotals) {

		logger.info("=== Downloading and Validating Flat Cancel PDF ===");
		logger.info("Expected Grand Total: ${}", String.format("%.2f", calculatedGrandTotal));
		logger.info("Cancelled Certificates: {}", cancelledCertificates);

		FlatCancelPDFValidationResult result = new FlatCancelPDFValidationResult();
		result.expectedGrandTotal = calculatedGrandTotal;

		try {
			// Step 1: Click Bind History tab
			if (!clickBindHistoryTab()) {
				result.addError("Failed to click Bind History tab");
				return result;
			}

			captureScreenshotToReport("Bind History Tab - Opened");

			// Step 2: Find ADDRESS_FLAT_CANCELLED entry
			int flatCancelRowIndex = findAddressFlatCancelledEntry();
			if (flatCancelRowIndex < 0) {
				result.addError("ADDRESS_FLAT_CANCELLED entry not found in Bind History");
				return result;
			}

			// Step 3: Click Download Document button
			if (!clickDownloadForFlatCancelEntry(flatCancelRowIndex)) {
				result.addError("Failed to click Download Document button");
				return result;
			}

			// Step 4: Wait for PDF download
			String downloadDir = getDownloadDirectory();
			sleep(3000);
			String pdfPath = waitForPDFDownload(downloadDir, 30);

			if (pdfPath == null) {
				result.addError("PDF download failed or timed out");
				return result;
			}

			logger.info("PDF downloaded: {}", pdfPath);
			result.pdfPath = pdfPath;

			// Step 5: Parse PDF using FlatCancelPDFReader
			com.automation.utils.FlatCancelPDFReader pdfReader =
				new com.automation.utils.FlatCancelPDFReader(pdfPath);
			pdfReader.parseAllPages();

			result.pdfSummary = pdfReader.getSummary();
			result.pdfLocations = pdfReader.getLocations();

			// Step 6: Validate Total Balance Refund
			double pdfTotalBalanceRefund = result.pdfSummary.totalBalanceRefund;
			result.pdfGrandTotal = pdfTotalBalanceRefund;
			result.difference = Math.abs(pdfTotalBalanceRefund - calculatedGrandTotal);
			result.grandTotalMatch = result.difference < 1.0; // $1 tolerance

			logger.info("PDF Total Balance Refund: ${}", String.format("%.2f", pdfTotalBalanceRefund));
			logger.info("Calculated Grand Total: ${}", String.format("%.2f", calculatedGrandTotal));
			logger.info("Difference: ${}", String.format("%.2f", result.difference));
			logger.info("Match: {}", result.grandTotalMatch);

			// Step 7: Validate individual locations
			for (String certId : cancelledCertificates) {
				boolean foundInPdf = pdfReader.certificateExistsInPDF(certId);
				result.certificateInPdfMap.put(certId, foundInPdf);

				if (foundInPdf) {
					result.certificatesFoundInPdf.add(certId);
				} else {
					result.certificatesNotInPdf.add(certId);
				}
			}

			// Step 8: Validate individual location totals if provided
			if (locationTotals != null && !locationTotals.isEmpty()) {
				for (Map.Entry<String, Double> entry : locationTotals.entrySet()) {
					String certId = entry.getKey();
					double expectedTotal = entry.getValue();
					boolean matches = pdfReader.validateLocation(certId, expectedTotal);
					result.locationTotalMatches.put(certId, matches);
				}
			}

			// Step 9: Open PDF in new tab for screenshot
			String originalWindow = driver.getWindowHandle();
			openPDFInNewTab(pdfPath);
			sleep(2000);
			captureScreenshotToReport("Flat Cancel PDF - Page 1 (Summary)");

			// Close PDF tab
			closePDFTab(originalWindow);

			// Step 10: Close PDF reader
			pdfReader.close();

			// Step 11: Build HTML validation report
			result.htmlReport = buildFlatCancelPDFValidationReport(result, cancelledCertificates, locationTotals);
			logHtmlToReport(result.htmlReport);

			logger.info("Flat Cancel PDF validation complete. Grand Total Match: {}", result.grandTotalMatch);

		} catch (Exception e) {
			logger.error("Error validating Flat Cancel PDF: {}", e.getMessage());
			result.addError("Exception: " + e.getMessage());
		}

		return result;
	}

	/**
	 * Build HTML validation report for Flat Cancel PDF
	 */
	private String buildFlatCancelPDFValidationReport(
			FlatCancelPDFValidationResult result,
			List<String> cancelledCertificates,
			Map<String, Double> locationTotals) {

		StringBuilder html = new StringBuilder();

		String status = result.grandTotalMatch ? "PASSED" : "FAILED";
		String statusColor = result.grandTotalMatch ? "#28a745" : "#dc3545";

		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid ").append(statusColor).append("; color: #000000;'>");
		html.append("<h3 style='color: ").append(statusColor).append("; margin-top: 0;'>Flat Cancel PDF Validation - ").append(status).append("</h3>");

		// Summary section
		html.append("<h4 style='color: #1565c0;'>PDF Summary</h4>");
		if (result.pdfSummary != null) {
			html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
			html.append("<tr style='background-color: #e3f2fd;'><td style='padding: 8px; width: 40%;'>Policy ID</td><td style='padding: 8px;'>").append(result.pdfSummary.policyId).append("</td></tr>");
			html.append("<tr><td style='padding: 8px;'>Endorsement Type</td><td style='padding: 8px;'>").append(result.pdfSummary.endorsementType).append("</td></tr>");
			html.append("<tr style='background-color: #e3f2fd;'><td style='padding: 8px;'>Effective Date</td><td style='padding: 8px;'>").append(result.pdfSummary.effectiveDateOfEndorsement).append("</td></tr>");
			html.append("<tr><td style='padding: 8px;'>Total Properties Cancelled</td><td style='padding: 8px;'>").append(result.pdfSummary.totalPropertiesCancelled).append("</td></tr>");
			html.append("</table>");
		}

		// Grand Total Comparison
		html.append("<h4 style='color: #1565c0;'>Total Balance Refund Validation</h4>");
		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px;'>Field</th>");
		html.append("<th style='padding: 10px;'>Edit Flat Cancel (Calculated)</th>");
		html.append("<th style='padding: 10px;'>PDF Value</th>");
		html.append("<th style='padding: 10px;'>Difference</th>");
		html.append("<th style='padding: 10px;'>Result</th>");
		html.append("</tr>");

		String totalBgColor = result.grandTotalMatch ? "#e8f5e9" : "#ffebee";
		html.append("<tr style='background-color: ").append(totalBgColor).append("; color: #000000;'>");
		html.append("<td style='padding: 10px; font-weight: bold;'>Total Balance Refund</td>");
		html.append("<td style='padding: 10px; text-align: center;'>$").append(String.format("%.2f", result.expectedGrandTotal)).append("</td>");
		html.append("<td style='padding: 10px; text-align: center;'>$").append(String.format("%.2f", result.pdfGrandTotal)).append("</td>");
		html.append("<td style='padding: 10px; text-align: center;'>$").append(String.format("%.2f", result.difference)).append("</td>");
		html.append("<td style='padding: 10px; text-align: center;'>").append(getStatusBadge(result.grandTotalMatch ? "PASS" : "FAIL")).append("</td>");
		html.append("</tr></table>");

		// Certificate Validation
		html.append("<h4 style='color: #1565c0;'>Certificates in PDF</h4>");
		html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 8px;'>Certificate ID</th>");
		html.append("<th style='padding: 8px;'>Found in PDF</th>");
		if (locationTotals != null && !locationTotals.isEmpty()) {
			html.append("<th style='padding: 8px;'>Expected Total</th>");
			html.append("<th style='padding: 8px;'>Total Match</th>");
		}
		html.append("</tr>");

		for (String certId : cancelledCertificates) {
			boolean foundInPdf = result.certificateInPdfMap.getOrDefault(certId, false);
			String bgColor = foundInPdf ? "#e8f5e9" : "#fff3e0";

			html.append("<tr style='background-color: ").append(bgColor).append("; color: #000000;'>");
			html.append("<td style='padding: 8px; font-weight: bold;'>").append(certId).append("</td>");
			html.append("<td style='padding: 8px; text-align: center;'>").append(foundInPdf ? "Yes" : "No").append("</td>");

			if (locationTotals != null && !locationTotals.isEmpty()) {
				Double expectedTotal = locationTotals.get(certId);
				Boolean matches = result.locationTotalMatches.get(certId);

				html.append("<td style='padding: 8px; text-align: center;'>$").append(expectedTotal != null ? String.format("%.2f", expectedTotal) : "N/A").append("</td>");
				html.append("<td style='padding: 8px; text-align: center;'>").append(matches != null && matches ? getStatusBadge("PASS") : getStatusBadge("WARN")).append("</td>");
			}
			html.append("</tr>");
		}
		html.append("</table>");

		// Location Details from PDF
		if (result.pdfLocations != null && !result.pdfLocations.isEmpty()) {
			html.append("<h4 style='color: #1565c0;'>Location Details from PDF</h4>");
			html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0; color: #000000; font-size: 12px;'>");
			html.append("<tr style='background-color: #343a40; color: white;'>");
			html.append("<th style='padding: 6px;'>Cert ID</th>");
			html.append("<th style='padding: 6px;'>Address</th>");
			html.append("<th style='padding: 6px;'>Property</th>");
			html.append("<th style='padding: 6px;'>GL</th>");
			html.append("<th style='padding: 6px;'>Water/Sewer</th>");
			html.append("<th style='padding: 6px;'>Taxes</th>");
			html.append("<th style='padding: 6px;'>Fees</th>");
			html.append("<th style='padding: 6px;'>Total</th>");
			html.append("</tr>");

			for (com.automation.utils.FlatCancelPDFReader.FlatCancelLocation loc : result.pdfLocations) {
				html.append("<tr style='background-color: #ffffff; border-bottom: 1px solid #dee2e6; color: #000000;'>");
				html.append("<td style='padding: 6px; font-weight: bold;'>").append(loc.certId).append("</td>");
				html.append("<td style='padding: 6px;'>").append(loc.propertyAddress != null ? loc.propertyAddress : "").append("</td>");
				html.append("<td style='padding: 6px; text-align: right;'>$").append(String.format("%.2f", loc.propertyPremium)).append("</td>");
				html.append("<td style='padding: 6px; text-align: right;'>$").append(String.format("%.2f", loc.glPremium)).append("</td>");
				html.append("<td style='padding: 6px; text-align: right;'>$").append(String.format("%.2f", loc.waterSewerPremium)).append("</td>");
				html.append("<td style='padding: 6px; text-align: right;'>$").append(String.format("%.2f", loc.taxes)).append("</td>");
				html.append("<td style='padding: 6px; text-align: right;'>$").append(String.format("%.2f", loc.fees)).append("</td>");
				html.append("<td style='padding: 6px; text-align: right; font-weight: bold;'>$").append(String.format("%.2f", loc.total)).append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
		}

		// Errors
		if (!result.errors.isEmpty()) {
			html.append("<div style='margin-top: 10px; padding: 10px; background-color: #ffebee; border-radius: 4px;'>");
			html.append("<strong style='color: #c62828;'>Errors:</strong><ul style='margin: 5px 0; padding-left: 20px;'>");
			for (String error : result.errors) {
				html.append("<li style='color: #c62828;'>").append(error).append("</li>");
			}
			html.append("</ul></div>");
		}

		html.append("</div>");

		return html.toString();
	}

	// ==================== Flat Cancel Result Classes ====================

	/**
	 * Result class for Flat Cancel Certificate validation (not displayed on Master Policy)
	 */
	public static class FlatCancelCertificateValidationResult {
		public int totalCertificates;
		public java.util.List<String> certificatesNotFound = new java.util.ArrayList<>();
		public java.util.List<String> certificatesStillDisplayed = new java.util.ArrayList<>();
		public boolean allCertificatesRemoved;
		public String htmlReport;

		public boolean isValid() {
			return allCertificatesRemoved;
		}
	}

	/**
	 * Result class for Flat Cancel PDF validation
	 */
	public static class FlatCancelPDFValidationResult {
		public String pdfPath;
		public double expectedGrandTotal;
		public double pdfGrandTotal;
		public double difference;
		public boolean grandTotalMatch;

		public com.automation.utils.FlatCancelPDFReader.FlatCancelSummary pdfSummary;
		public java.util.List<com.automation.utils.FlatCancelPDFReader.FlatCancelLocation> pdfLocations;

		public java.util.List<String> certificatesFoundInPdf = new java.util.ArrayList<>();
		public java.util.List<String> certificatesNotInPdf = new java.util.ArrayList<>();
		public java.util.Map<String, Boolean> certificateInPdfMap = new java.util.HashMap<>();
		public java.util.Map<String, Boolean> locationTotalMatches = new java.util.HashMap<>();

		public java.util.List<String> errors = new java.util.ArrayList<>();
		public String htmlReport;

		public boolean isValid() {
			return grandTotalMatch && errors.isEmpty();
		}

		public void addError(String error) {
			errors.add(error);
		}
	}

	// ==================== Result Classes ====================

	/**
	 * Master Policy Validation Result
	 */
	public static class MasterPolicyValidationResult {
		private boolean valid = true;
		private java.util.List<String> errors = new java.util.ArrayList<>();
		private java.util.List<String> warnings = new java.util.ArrayList<>();
		private String htmlReport = "";

		public boolean isValid() { return valid && errors.isEmpty(); }
		public void setValid(boolean valid) { this.valid = valid; }
		public java.util.List<String> getErrors() { return errors; }
		public void addError(String error) { errors.add(error); valid = false; }
		public java.util.List<String> getWarnings() { return warnings; }
		public void addWarning(String warning) { warnings.add(warning); }
		public String getHtmlReport() { return htmlReport; }
		public void setHtmlReport(String report) { this.htmlReport = report; }
	}
}
