package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CreateQuoteLocators extends BasePage {

	// ==================== Page Header Elements ====================

	@FindBy(xpath = "//h1[contains(text(),'Create New Quote')]")
	protected WebElement pageTitle;

	@FindBy(id = "new-quote-reset-form-button")
	protected WebElement resetFormButton;

	// ==================== Agent Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Select Agent')]/following::button[1]")
	protected WebElement agentDropdown;

	@FindBy(xpath = "//label[contains(text(),'Commission')]/following-sibling::input")
	protected WebElement agentCommissionInput;

	@FindBy(xpath = "//label[contains(text(),'Renewal Commission')]/following-sibling::input")
	protected WebElement agentRenewalCommissionInput;

	// ==================== Insured Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Insured')]/following::button[1]")
	protected WebElement insuredDropdown;

	// ==================== User Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Select User')]/following::button[1]")
	protected WebElement userDropdown;

	protected final By userDropdownLocator = By.xpath("//label[contains(text(),'Select User')]/following::button[1]");

	// ==================== Carrier Section Locators ====================

	@FindBy(id = "new-quote-carrier-select")
	protected WebElement carrierDropdown;

	// ==================== General Liability Section Locators ====================

	@FindBy(id = "new-quote-general-liability-yes-radio")
	protected WebElement generalLiabilityYesRadio;

	@FindBy(id = "new-quote-general-liability-no-radio")
	protected WebElement generalLiabilityNoRadio;

	@FindBy(id = "new-quote-general-liability-amount-input")
	protected WebElement generalLiabilityAmountInput;

	@FindBy(id = "new-quote-general-liability-apply-button")
	protected WebElement generalLiabilityApplyButton;

	// ==================== Water & Sewer Backup Section Locators================
	
	@FindBy(id = "new-quote-water-sewer-backup-yes-radio")
	protected WebElement waterSewerBackupYesRadio;

	@FindBy(id = "new-quote-water-sewer-backup-no-radio")
	protected WebElement waterSewerBackupNoRadio;

	@FindBy(id = "new-quote-water-sewer-backup-amount-input")
	protected WebElement waterSewerBackupAmountInput;

	@FindBy(id = "new-quote-water-sewer-backup-apply-button")
	protected WebElement waterSewerBackupApplyButton;

	// ==================== Animal Liability Section Locators ====================

	@FindBy(id = "new-quote-animal-liability-no-radio")
	protected WebElement animalLiabilityNoRadio;

	@FindBy(id = "new-quote-animal-liability-no-only-radio")
	protected WebElement animalLiabilityNoOnlyRadio;

	@FindBy(id = "new-quote-animal-liability-yes-radio")
	protected WebElement animalLiabilityYesRadio;

	// ==================== Suggested State Section Locators ====================

	@FindBy(xpath = "//label[contains(text(),'Suggested States')]/following::button[1]")
	protected WebElement suggestedStateDropdown;

	// ==================== Action Buttons Section Locators ====================

	@FindBy(id = "new-quote-submit-button")
	public WebElement submitQuoteButton;

	// ==================== Location Section Locators ====================

	@FindBy(xpath = "//button[contains(text(),'New Location')]")
	protected WebElement newLocationButton;

	@FindBy(xpath = "//h1[contains(text(),'Locations Details')]")
	protected WebElement locationsDetailsHeader;

	// ==================== By Locators (for explicit waits) ====================

	protected final By pageTitleLocator = By.xpath(
			"//h1[contains(text(),'Create New Quote') or contains(text(),'New Quote') or contains(text(),'Create Quote')]");
	protected final By agentDropdownLocator = By.xpath(
			"//label[contains(text(),'Agent')]/following-sibling::*//button | //label[contains(text(),'Agent')]/following::button[1] | //*[contains(@id,'agent') and contains(@id,'select')]");
	protected final By insuredDropdownLocator = By.xpath(
			"//label[contains(text(),'Insured')]/following-sibling::*//button | //label[contains(text(),'Insured')]/following::button[1] | //*[contains(@id,'insured') or contains(@id,'investor')]");
	protected final By submitButtonLocator = By.xpath(
			"//button[contains(text(),'Submit') or contains(text(),'Create Quote') or contains(text(),'Save')] | //*[contains(@id,'submit')]");
	protected final By newLocationButtonLocator = By.xpath("//button[contains(text(),'New Location')]");

	// SearchableDropdown specific locators
	protected final By dropdownSearchInputLocator = By.xpath("//input[@placeholder]");
	protected final By dropdownOptionLocator = By.xpath("//div[@role='option']");

	// ==================== Constructor ====================

	public CreateQuoteLocators(WebDriver driver) {
		super(driver);
		logger.info("CreateQuoteLocators initialized");
	}

	// ==================== Agent Section Methods ====================

	public void clickAgentDropdown() {
		WebElement dropdown = findDropdownByLabel("Agent");
		if (dropdown != null) {
			click(dropdown);
		} else {
			throw new RuntimeException("Could not find Agent dropdown");
		}
	}

	public void selectAgentByName(String agentName) {
		clickAgentDropdown();
		sleep(1000);
		selectDropdownOption(agentName);
	}

	public void selectFirstAvailableAgent() {
		clickAgentDropdown();
		sleep(1000);
		selectFirstDropdownOption();
	}

	protected WebElement findDropdownByLabel(String labelText) {

		// Strategy 1: Find by label followed by button/div
		String[] xpaths = { "//label[contains(text(),'" + labelText + "')]/following-sibling::*//button",
				"//label[contains(text(),'" + labelText + "')]/following::button[1]",
				"//label[contains(text(),'" + labelText + "')]/..//button",
				"//label[contains(text(),'" + labelText + "')]/parent::*//button",
				"//*[contains(text(),'" + labelText + "')]/following::button[1]",
				"//button[contains(@aria-label,'" + labelText + "')]",
				"//*[contains(@id,'" + labelText.toLowerCase() + "')]//button",
				"//*[contains(@class,'" + labelText.toLowerCase() + "')]//button" };

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					if (el.isDisplayed()) {
						return el;
					}
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}

		return null;
	}

	protected void selectDropdownOption(String optionText) {
		sleep(800);
		logger.info("Selecting dropdown option: {}", optionText);

		// Try exact and partial match with case-insensitive search
		String lowerOption = optionText.toLowerCase().trim();

		// Strategy 1: Try exact/contains match with original text
		String[] xpaths = {
				"//div[@role='option' and contains(.,'" + optionText + "')]",
				"//li[@role='option' and contains(.,'" + optionText + "')]",
				"//div[contains(@class,'option') and contains(.,'" + optionText + "')]",
				"//*[@data-value and contains(.,'" + optionText + "')]",
				"//div[contains(@class,'item') and contains(.,'" + optionText + "')]"
		};

		for (String xpath : xpaths) {
			try {
				WebElement option = driver.findElement(By.xpath(xpath));
				if (option.isDisplayed()) {
					click(option);
					logger.info("Selected option: {}", optionText);
					sleep(500);
					return;
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}

		// Strategy 2: Find all visible options and match by text (case-insensitive, partial match)
		// More comprehensive list of option XPaths
		String[] optionXpaths = {
				"//div[@role='option']",
				"//li[@role='option']",
				"//div[contains(@class,'option')]",
				"//div[contains(@class,'item') and not(contains(@class,'disabled'))]",
				"//*[@role='listbox']//*[@role='option']",
				"//div[contains(@class,'select')]//div[contains(@class,'option')]",
				"//ul//li[not(contains(@class,'disabled'))]",
				"//*[contains(@class,'dropdown')]//div[@role='option']",
				"//*[contains(@class,'menu-content')]//*[@role='option']",
				"//*[contains(@class,'popover')]//*[@role='option']"
		};

		for (String xpath : optionXpaths) {
			try {
				java.util.List<WebElement> options = driver.findElements(By.xpath(xpath));
				logger.debug("Found {} options with xpath: {}", options.size(), xpath);
				for (WebElement opt : options) {
					try {
						if (opt.isDisplayed()) {
							String text = opt.getText().toLowerCase().trim();
							// Check if option text contains search term OR search term contains option text
							// This handles both "Arch" matching "Arch Specialty Insurance"
							// and "New York" matching "NY" or vice versa
							if (text.contains(lowerOption) || lowerOption.contains(text) ||
								text.startsWith(lowerOption) || lowerOption.startsWith(text)) {
								click(opt);
								logger.info("Selected option by partial match: '{}' (searched for '{}')", opt.getText(), optionText);
								sleep(500);
								return;
							}
						}
					} catch (Exception e) {
						// Element might have become stale, continue
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		// Strategy 3: Try clicking directly on visible text matching the option
		try {
			// Use XPath text() function for more precise matching
			String[] textXpaths = {
				"//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lowerOption + "')]",
				"//*[starts-with(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lowerOption + "')]"
			};
			for (String xpath : textXpaths) {
				try {
					java.util.List<WebElement> elements = driver.findElements(By.xpath(xpath));
					for (WebElement el : elements) {
						if (el.isDisplayed() && el.isEnabled()) {
							String tagName = el.getTagName().toLowerCase();
							// Avoid clicking on input, label, or container elements
							if (!tagName.equals("input") && !tagName.equals("label") && !tagName.equals("form")) {
								click(el);
								logger.info("Selected option by text xpath: '{}' (searched for '{}')", el.getText(), optionText);
								sleep(500);
								return;
							}
						}
					}
				} catch (Exception e) {
					// Continue
				}
			}
		} catch (Exception e) {
			// Continue
		}

		// Strategy 4: If not found, select first available option
		logger.warn("Option '{}' not found, selecting first available option", optionText);
		selectFirstDropdownOption();
	}

	protected void selectFirstDropdownOption() {
		sleep(500);

		// Prioritize options within popover/listbox containers to avoid header elements
		String[] xpaths = {
				// Options within popover/listbox containers (highest priority)
				"(//div[contains(@class,'popover')]//div[@role='option'])[1]",
				"(//div[contains(@class,'listbox')]//div[@role='option'])[1]",
				"(//div[@role='listbox']//div[@role='option'])[1]",
				"(//ul[contains(@class,'menu')]//li[@role='option' or @role='menuitem'])[1]",
				// Options NOT in header/nav
				"(//div[@role='option' and not(ancestor::header) and not(ancestor::nav)])[1]",
				"(//li[@role='option' and not(ancestor::header) and not(ancestor::nav)])[1]",
				"(//div[contains(@class,'option') and not(contains(@class,'disabled')) and not(ancestor::header)])[1]",
				"(//div[contains(@class,'item') and not(contains(@class,'disabled')) and not(ancestor::header)])[1]",
				"(//*[@data-value and not(ancestor::header)])[1]"
		};

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> options = driver.findElements(By.xpath(xpath));
				for (WebElement opt : options) {
					if (opt.isDisplayed()) {
						click(opt);
						sleep(500);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}

		// Last resort: click any visible non-header element in a popup/dropdown container
		try {
			WebElement option = driver.findElement(By.xpath(
					"(//div[contains(@class,'popover') or contains(@class,'dropdown') or contains(@class,'menu')]//div[string-length(text()) > 0])[1]"));
			if (option.isDisplayed()) {
				click(option);
				sleep(500);
				return;
			}
		} catch (Exception e) {
		}

		throw new RuntimeException("Could not find any dropdown options to select");
	}

	protected WebElement findInputField(String labelText, String defaultValue) {

		String[] xpaths = { "//label[contains(text(),'" + labelText + "')]/..//input[@type='number']",
				"//label[contains(text(),'" + labelText + "')]/following::input[@type='number'][1]",
				"//label[contains(text(),'" + labelText + "')]/..//input[not(@type='radio') and not(@type='checkbox')]",
				"//*[contains(text(),'" + labelText + "')]/following::input[@type='number'][1]",
				"//div[contains(@class,'" + labelText.toLowerCase().replace(" ", "-") + "')]//input" };

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					if (el.isDisplayed()) {
						return el;
					}
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}

		return null;
	}

	protected void clickApplyButtonIfExists(String labelText) {
		try {
			String[] xpaths = { "//label[contains(text(),'" + labelText + "')]/..//button[contains(text(),'Apply')]",
					"//label[contains(text(),'" + labelText + "')]/following::button[contains(text(),'Apply')][1]",
					"//*[contains(text(),'" + labelText + "')]/following::button[contains(text(),'Apply')][1]" };

			for (String xpath : xpaths) {
				java.util.List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				for (WebElement btn : buttons) {
					if (btn.isDisplayed() && btn.isEnabled()) {
						click(btn);
						sleep(500);
						return;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	// ==================== Insured Section Methods ====================

	public void clickInsuredDropdown() {
		WebElement dropdown = findDropdownByLabel("Insured");
		if (dropdown != null) {
			click(dropdown);
		} else {
			throw new RuntimeException("Could not find Insured dropdown");
		}
	}

	public void selectInsuredByName(String insuredName) {
		clickInsuredDropdown();
		sleep(1000);
		selectDropdownOption(insuredName);
	}

	public void selectFirstAvailableInsured() {
		clickInsuredDropdown();
		sleep(1000);
		selectFirstDropdownOption();
	}

	// ==================== User Section Methods ====================

	/**
	 * Click on Select User dropdown and capture screenshot
	 * This dropdown is located before WS (Water & Sewer) fields
	 */
	public void clickSelectUserDropdown() {
		logger.info("Clicking Select User dropdown");

		// Scroll to make sure the dropdown is visible
		scrollIntoView(userDropdown);
		sleep(500);

		// Wait for dropdown to be clickable
		waitForClickable(userDropdown);
		click(userDropdown);
		sleep(1000);

		logger.info("Select User dropdown clicked");
	}

	/**
	 * Check if Select User dropdown is displayed
	 */
	public boolean isSelectUserDropdownDisplayed() {
		return isDisplayed(userDropdownLocator);
	}

	// ==================== Carrier Section Methods ====================

	public void clickCarrierDropdown() {
		WebElement dropdown = findDropdownByLabel("Carrier");
		if (dropdown != null) {
			click(dropdown);
		} else {
			throw new RuntimeException("Could not find Carrier dropdown");
		}
	}

	public void selectFirstAvailableCarrier() {
		clickCarrierDropdown();
		sleep(1000);
		selectFirstDropdownOption();
	}

	public void selectCarrierByName(String carrierName) {
		logger.info("Selecting carrier: {}", carrierName);

		// First, try using the specific carrier dropdown ID
		try {
			WebElement carrierDropdown = driver.findElement(By.id("new-quote-carrier-select"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", carrierDropdown);
			sleep(300);
			click(carrierDropdown);
			sleep(1500);
			logger.info("Clicked carrier dropdown by ID");
		} catch (Exception e) {
			logger.warn("Could not find carrier by ID, using label-based click");
			clickCarrierDropdown();
			sleep(1500);
		}

		// Try exact/partial match with the carrier name - be more specific
		String lowerCarrier = carrierName.toLowerCase().trim();

		// Build XPaths for finding carrier option - prioritize exact "Insurance" match for carriers
		String[] xpaths = {
			// Carrier-specific: Look for options containing both the carrier name AND "Insurance" or "Specialty"
			"//div[@role='option' and (contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lowerCarrier + "') and (contains(., 'Insurance') or contains(., 'Specialty')))]",
			"//li[@role='option' and (contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lowerCarrier + "') and (contains(., 'Insurance') or contains(., 'Specialty')))]",
			"//*[@role='option' and (contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lowerCarrier + "') and (contains(., 'Insurance') or contains(., 'Specialty')))]",
			// Fallback: just the carrier name but still require role='option'
			"//div[@role='option' and contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lowerCarrier + "')]",
			"//li[@role='option' and contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lowerCarrier + "')]",
			"//*[@role='option' and contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lowerCarrier + "')]"
		};

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> options = driver.findElements(By.xpath(xpath));
				for (WebElement option : options) {
					if (option.isDisplayed()) {
						String optionText = option.getText().trim();
						// Skip options that are clearly not carriers (like "Add Insured")
						if (optionText.toLowerCase().contains("add ") ||
							optionText.toLowerCase().contains("create ") ||
							optionText.toLowerCase().contains("new ")) {
							logger.debug("Skipping non-carrier option: {}", optionText);
							continue;
						}
						// Prefer options that contain "Insurance" or "Specialty" (carrier names)
						if (optionText.toLowerCase().contains("insurance") ||
							optionText.toLowerCase().contains("specialty") ||
							optionText.toLowerCase().startsWith(lowerCarrier)) {
							logger.info("Found matching carrier option: {}", optionText);
							click(option);
							sleep(500);
							logger.info("Selected carrier: {}", optionText);
							return;
						}
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}

		// Fallback: Find all options and match manually - but be more strict
		logger.warn("No carrier option found with xpath match, trying manual search for: {}", carrierName);
		try {
			java.util.List<WebElement> allOptions = driver.findElements(By.xpath("//*[@role='option']"));
			for (WebElement option : allOptions) {
				if (!option.isDisplayed()) continue;
				String optionText = option.getText().trim();
				String optionTextLower = optionText.toLowerCase();
				// Skip non-carrier options
				if (optionTextLower.contains("add ") || optionTextLower.contains("create ") || optionTextLower.contains("new ")) {
					continue;
				}
				// Must contain "insurance" or "specialty" to be a valid carrier
				if (optionTextLower.contains(lowerCarrier) &&
					(optionTextLower.contains("insurance") || optionTextLower.contains("specialty"))) {
					logger.info("Found carrier via manual search: {}", optionText);
					click(option);
					sleep(500);
					return;
				}
			}
		} catch (Exception e) {
			logger.error("Failed to find carrier option: {}", e.getMessage());
		}

		// Last resort: close dropdown and try one more time with direct ID approach
		logger.warn("Could not select carrier '{}', attempting final retry", carrierName);
		try {
			// Press Escape to close any open dropdown
			driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
			sleep(500);

			// Try clicking by ID again
			WebElement carrierBtn = driver.findElement(By.id("new-quote-carrier-select"));
			click(carrierBtn);
			sleep(1500);

			// Look for the option more specifically
			String exactXpath = "//*[@role='option' and contains(., '" + carrierName + "') and contains(., 'Specialty')]";
			java.util.List<WebElement> exactOptions = driver.findElements(By.xpath(exactXpath));
			for (WebElement opt : exactOptions) {
				if (opt.isDisplayed()) {
					logger.info("Found carrier on retry: {}", opt.getText().trim());
					click(opt);
					sleep(500);
					return;
				}
			}
		} catch (Exception e) {
			logger.error("Final retry failed for carrier selection: {}", e.getMessage());
		}

		logger.error("FAILED to select carrier: {}. Carrier dropdown may need manual verification.", carrierName);
	}

	/**
	 * Get the currently selected carrier name
	 */
	public String getSelectedCarrier() {
		try {
			WebElement dropdown = findDropdownByLabel("Carrier");
			if (dropdown != null) {
				String text = dropdown.getText().trim();
				if (!text.isEmpty() && !text.equalsIgnoreCase("Select")) {
					return text;
				}
			}
		} catch (Exception e) {
		}
		return "";
	}

	// ==================== General Liability Section Methods ====================

	public void selectGeneralLiabilityYes() {
		clickRadioButton("General Liability", "Yes", generalLiabilityYesRadio);
	}

	public void selectGeneralLiabilityNo() {
		clickRadioButton("General Liability", "No", generalLiabilityNoRadio);
	}

	/**
	 * Click radio button with scroll and fallback strategies
	 */
	private void clickRadioButton(String sectionName, String optionName, WebElement defaultElement) {
		String[] xpaths = {
			"//label[contains(text(),'" + sectionName + "')]/following::button[contains(text(),'" + optionName + "')][1]",
			"//label[contains(text(),'" + sectionName + "')]/following::*[@role='radio' and contains(.,'" + optionName + "')][1]",
			"//*[contains(text(),'" + sectionName + "')]/following::button[contains(text(),'" + optionName + "')][1]",
			"//button[contains(@id,'" + sectionName.toLowerCase().replace(" ", "-") + "') and contains(@id,'" + optionName.toLowerCase() + "')]",
			"//*[contains(@id,'" + sectionName.toLowerCase().replace(" ", "-") + "-" + optionName.toLowerCase() + "')]"
		};

		// Try default element first with scroll
		try {
			scrollIntoView(defaultElement);
			sleep(300);
			if (defaultElement.isDisplayed()) {
				click(defaultElement);
				return;
			}
		} catch (Exception e) {
		}

		// Try alternative xpaths
		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> elements = driver.findElements(By.xpath(xpath));
				for (WebElement el : elements) {
					if (el.isDisplayed()) {
						scrollIntoView(el);
						sleep(200);
						click(el);
						return;
					}
				}
			} catch (Exception e) {
				// Continue
			}
		}

		// Last resort: JavaScript click on default element
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", defaultElement);
			sleep(300);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", defaultElement);
		} catch (Exception e) {
		}
	}

	public void enterGeneralLiabilityAmount(String amount) {
		enterAmountWithJavaScript(generalLiabilityAmountInput, amount, "General Liability");
	}

	/**
	 * Get current GL amount from frontend
	 */
	public String getGeneralLiabilityAmount() {
		try {
			// Try using xpath to find the GL input field
			String[] xpaths = {
				"//input[@id='new-quote-general-liability-amount-input']",
				"//label[contains(text(),'General Liability')]/following::input[1]",
				"//input[contains(@id,'general-liability') and contains(@id,'amount')]"
			};

			for (String xpath : xpaths) {
				try {
					WebElement input = driver.findElement(By.xpath(xpath));
					if (input != null && input.isDisplayed()) {
						String value = input.getAttribute("value");
						if (value != null && !value.isEmpty()) {
							return value.trim();
						}
					}
				} catch (Exception ex) {
					// Continue
				}
			}
			return "0";
		} catch (Exception e) {
			return "0";
		}
	}

	/**
	 * Enter amount using JavaScript for reliability - handles element not interactable issues
	 */
	private void enterAmountWithJavaScript(WebElement element, String amount, String fieldName) {
		try {
			// Scroll element into view
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
			sleep(500);

			// Wait for element to be visible
			try {
				new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
					.until(d -> element.isDisplayed() && element.isEnabled());
			} catch (Exception e) {
				// Continue anyway
			}

			// Clear using JavaScript
			((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", element);
			sleep(200);

			// Try normal sendKeys first
			try {
				element.sendKeys(amount);
			} catch (Exception e) {
				// Fallback: Set value via JavaScript
				((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", element, amount);
				// Trigger input event for React
				((JavascriptExecutor) driver).executeScript(
					"arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
					"arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", element);
			}
		} catch (Exception e) {
			// Last resort: try finding by ID and using JS
			try {
				String id = element.getAttribute("id");
				if (id != null && !id.isEmpty()) {
					((JavascriptExecutor) driver).executeScript(
						"var el = document.getElementById('" + id + "');" +
						"if(el) { el.value = '" + amount + "'; " +
						"el.dispatchEvent(new Event('input', { bubbles: true })); " +
						"el.dispatchEvent(new Event('change', { bubbles: true })); }");
				}
			} catch (Exception ex) {
			}
		}
	}

	public void clickGeneralLiabilityApply() {
		// Try flexible locators for Apply button
		String[] xpaths = { "//button[contains(text(),'Apply')]",
				"//button[@type='button' and contains(@class,'apply')]",
				"//label[contains(text(),'General Liability')]/following::button[contains(text(),'Apply')][1]",
				"//*[contains(@id,'apply')]//button",
				"//div[contains(@class,'general-liability')]//button[contains(text(),'Apply')]" };

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				if (!buttons.isEmpty()) {
					WebElement btn = buttons.get(0);
					if (btn.isDisplayed() && btn.isEnabled()) {
						click(btn);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}

		// Try original locator as last resort
		try {
			if (generalLiabilityApplyButton != null && generalLiabilityApplyButton.isDisplayed()) {
				waitForClickable(generalLiabilityApplyButton);
				click(generalLiabilityApplyButton);
				return;
			}
		} catch (Exception e) {
		}

	}

	public void setGeneralLiabilityAmount150() {
		logger.info("Setting General Liability amount to 150");
		selectGeneralLiabilityYes();
		sleep(1500);
		enterAmountInField("General Liability", "150");
		clickApplyButtonIfExists("General Liability");
	}

	public void setGeneralLiabilityAmount(String amount) {
		logger.info("Setting General Liability amount to: {}", amount);
		selectGeneralLiabilityYes();
		sleep(1500);
		// Use specific ID for GL input to avoid confusion with WS field
		enterAmountInFieldById("new-quote-general-liability-amount-input", amount, "General Liability");
		clickApplyButtonIfExists("General Liability");
	}

	/**
	 * Enter amount in a field with scroll and wait
	 */
	private void enterAmountInField(String sectionName, String amount) {
		String[] xpaths = {
			"//label[contains(text(),'" + sectionName + "')]/following::input[@type='number'][1]",
			"//label[contains(text(),'" + sectionName + "')]/..//input[@type='number']",
			"//*[contains(text(),'" + sectionName + "')]/following::input[@type='number'][1]",
			"//input[contains(@id,'" + sectionName.toLowerCase().replace(" ", "-") + "') and contains(@id,'amount')]",
			"//input[contains(@id,'" + sectionName.toLowerCase().replace(" ", "-") + "') and @type='number']"
		};

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> inputs = driver.findElements(By.xpath(xpath));
				for (WebElement input : inputs) {
					if (input.isDisplayed()) {
						// Scroll into view
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({block: 'center'});", input);
						sleep(300);

						// Wait for interactable
						try {
							new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
								.until(d -> input.isEnabled());
						} catch (Exception e) {
							// Continue anyway
						}

						// Click to focus
						try {
							input.click();
						} catch (Exception e) {
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
						}
						sleep(200);

						// Clear using Ctrl+A and Delete
						input.sendKeys(Keys.CONTROL + "a");
						sleep(100);
						input.sendKeys(Keys.DELETE);
						sleep(100);

						// Also clear using JavaScript with React events
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].value = '';" +
							"arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
							"arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input);
						sleep(200);

						// Enter the new value
						input.sendKeys(amount);
						sleep(100);

						// Trigger React events to ensure value is registered
						((JavascriptExecutor) driver).executeScript(
							"arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
							"arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
							"arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));", input);

						logger.info("Entered {} in {} field", amount, sectionName);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next xpath
			}
		}
	}

	/**
	 * Enter amount in a field using specific element ID
	 * This is more reliable than label-based search to avoid field confusion
	 * @param elementId the HTML id of the input element
	 * @param amount the amount to enter
	 * @param fieldName for logging purposes
	 */
	private void enterAmountInFieldById(String elementId, String amount, String fieldName) {
		logger.info("Entering {} in {} field (ID: {})", amount, fieldName, elementId);
		try {
			WebElement input = driver.findElement(By.id(elementId));

			// Scroll into view
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block: 'center'});", input);
			sleep(300);

			// Wait for interactable
			try {
				new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
					.until(d -> input.isEnabled());
			} catch (Exception e) {
				// Continue anyway
			}

			// Click to focus
			try {
				input.click();
			} catch (Exception e) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
			}
			sleep(200);

			// Clear using Ctrl+A and Delete
			input.sendKeys(Keys.CONTROL + "a");
			sleep(100);
			input.sendKeys(Keys.DELETE);
			sleep(100);

			// Also clear using JavaScript with React events
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].value = '';" +
				"arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
				"arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input);
			sleep(200);

			// Enter the new value
			input.sendKeys(amount);
			sleep(100);

			// Trigger React events to ensure value is registered
			((JavascriptExecutor) driver).executeScript(
				"arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
				"arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
				"arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));", input);

			logger.info("Successfully entered {} in {} field", amount, fieldName);
		} catch (Exception e) {
			logger.error("Failed to enter amount in {} field by ID: {}", fieldName, e.getMessage());
			// Fallback to label-based method
			enterAmountInField(fieldName, amount);
		}
	}

	// ==================== Water & Sewer Backup Section Methods
	// ====================

	public void selectWaterSewerBackupYes() {
		clickRadioButton("Water", "Yes", waterSewerBackupYesRadio);
	}

	public void selectWaterSewerBackupNo() {
		clickRadioButton("Water", "No", waterSewerBackupNoRadio);
	}

	public void enterWaterSewerBackupAmount(String amount) {
		enterAmountWithJavaScript(waterSewerBackupAmountInput, amount, "Water & Sewer Backup");
	}

	public void clickWaterSewerBackupApply() {
		// Try flexible locators for Apply button
		String[] xpaths = {
				"//label[contains(text(),'Water') and contains(text(),'Sewer')]/following::button[contains(text(),'Apply')][1]",
				"//button[contains(text(),'Apply')]", "//button[@type='button' and contains(@class,'apply')]",
				"//*[contains(@id,'water-sewer')]//button[contains(text(),'Apply')]",
				"//div[contains(@class,'water-sewer')]//button[contains(text(),'Apply')]" };

		for (String xpath : xpaths) {
			try {
				java.util.List<WebElement> buttons = driver.findElements(By.xpath(xpath));
				if (!buttons.isEmpty()) {
					WebElement btn = buttons.get(0);
					if (btn.isDisplayed() && btn.isEnabled()) {
						click(btn);
						return;
					}
				}
			} catch (Exception e) {
				// Continue to next strategy
			}
		}

		// Try original locator as last resort
		try {
			if (waterSewerBackupApplyButton != null && waterSewerBackupApplyButton.isDisplayed()) {
				waitForClickable(waterSewerBackupApplyButton);
				click(waterSewerBackupApplyButton);
				return;
			}
		} catch (Exception e) {
		}

	}

	/**
	 * Set Water & Sewer Backup amount to 100 (default value)
	 */
	public void setWaterSewerBackupAmount100() {
		logger.info("Setting Water & Sewer Backup amount to 100");
		selectWaterSewerBackupYes();
		sleep(1500);
		enterAmountInField("Water", "100");
		clickApplyButtonIfExists("Water");
	}

	/**
	 * Set Water & Sewer Backup to a specific amount
	 */
	public void setWaterSewerBackupAmount(String amount) {
		logger.info("Setting Water & Sewer Backup amount to: {}", amount);
		selectWaterSewerBackupYes();
		sleep(1500);
		// Use specific ID for WS input to avoid confusion with GL field
		enterAmountInFieldById("new-quote-water-sewer-backup-amount-input", amount, "Water & Sewer Backup");
		clickApplyButtonIfExists("Water");
	}

	// ==================== Animal Liability Section Methods ====================

	public void selectAnimalLiabilityNo() {
		try {
			clickRadioButton("Animal Liability", "No", animalLiabilityNoRadio);
		} catch (Exception e) {
			clickRadioButton("Animal Liability", "No", animalLiabilityNoOnlyRadio);
		}
	}

	public void selectAnimalLiabilityYes() {
		clickRadioButton("Animal Liability", "Yes", animalLiabilityYesRadio);
	}

	// ==================== Policy Fee Section Methods ====================

	/**
	 * Set Policy Fee toggle based on Yes/No value from Excel
	 * If "Yes" - turn toggle ON
	 * If "No" - turn toggle OFF (or leave off)
	 */
	public void setPolicyFee(String value) {
		if (value == null || value.trim().isEmpty()) {
			logger.info("Skipping Policy Fee - not configured");
			return;
		}

		String trimmedValue = value.trim().toLowerCase();
		boolean shouldBeOn = "yes".equals(trimmedValue) || "true".equals(trimmedValue) || "on".equals(trimmedValue);

		logger.info("Setting Policy Fee toggle to: {} (value from Excel: {})", shouldBeOn ? "ON" : "OFF", value);

		try {
			// Try to find toggle/switch element for Policy Fee
			WebElement toggle = null;

			// Try multiple XPath patterns to find the toggle
			String[] toggleXpaths = {
					"//label[contains(text(),'Policy Fee')]/following-sibling::*//input[@type='checkbox']",
					"//label[contains(text(),'Policy Fee')]/parent::*//input[@type='checkbox']",
					"//div[contains(text(),'Policy Fee')]/following-sibling::*//input[@type='checkbox']",
					"//span[contains(text(),'Policy Fee')]/ancestor::div[1]//input[@type='checkbox']",
					"//*[contains(text(),'Policy Fee')]/ancestor::div[1]//button[contains(@class,'switch') or contains(@role,'switch')]",
					"//*[contains(text(),'Policy Fee')]/following::input[@type='checkbox'][1]",
					"//*[contains(text(),'Policy Fee')]/following::*[contains(@class,'toggle') or contains(@class,'switch')][1]"
			};

			for (String xpath : toggleXpaths) {
				try {
					toggle = driver.findElement(By.xpath(xpath));
					if (toggle != null) {
						logger.info("Found Policy Fee toggle with XPath: {}", xpath);
						break;
					}
				} catch (Exception ignored) {
				}
			}

			if (toggle != null) {
				// Check current state
				boolean isCurrentlyOn = false;
				try {
					isCurrentlyOn = toggle.isSelected() ||
							"true".equals(toggle.getAttribute("checked")) ||
							"true".equals(toggle.getAttribute("aria-checked")) ||
							toggle.getAttribute("class").contains("checked") ||
							toggle.getAttribute("class").contains("active");
				} catch (Exception e) {
					logger.debug("Could not determine toggle state: {}", e.getMessage());
				}

				// Toggle if needed
				if (shouldBeOn && !isCurrentlyOn) {
					logger.info("Clicking Policy Fee toggle to turn ON");
					click(toggle);
				} else if (!shouldBeOn && isCurrentlyOn) {
					logger.info("Clicking Policy Fee toggle to turn OFF");
					click(toggle);
				} else {
					logger.info("Policy Fee toggle already in correct state: {}", shouldBeOn ? "ON" : "OFF");
				}
			} else {
				// Fallback: Try to find as radio button
				logger.info("Toggle not found, trying radio button approach for Policy Fee");
				if (shouldBeOn) {
					try {
						WebElement yesRadio = driver.findElement(
								By.xpath("//*[contains(text(),'Policy Fee')]/following::*[contains(text(),'Yes') and (@type='radio' or @role='radio' or ancestor::label)][1]"));
						click(yesRadio);
						logger.info("Selected 'Yes' radio for Policy Fee");
					} catch (Exception e) {
						logger.warn("Could not find Policy Fee Yes radio: {}", e.getMessage());
					}
				} else {
					try {
						WebElement noRadio = driver.findElement(
								By.xpath("//*[contains(text(),'Policy Fee')]/following::*[contains(text(),'No') and (@type='radio' or @role='radio' or ancestor::label)][1]"));
						click(noRadio);
						logger.info("Selected 'No' radio for Policy Fee");
					} catch (Exception e) {
						logger.warn("Could not find Policy Fee No radio: {}", e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Could not set Policy Fee toggle: {}", e.getMessage());
		}
	}

	// ==================== Suggested State Section Methods ====================

	public void clickSuggestedStateDropdown() {
		WebElement dropdown = findDropdownByLabel("Suggested States");
		if (dropdown != null) {
			click(dropdown);
		} else {
			waitForClickable(suggestedStateDropdown);
			click(suggestedStateDropdown);
		}
	}

	public void selectSuggestedState(String state) {
		clickSuggestedStateDropdown();
		sleep(1000);

		// Try to find and use search input if available
		try {
			WebElement searchInput = driver.findElement(By.xpath(
					"//input[contains(@placeholder,'state') or contains(@placeholder,'State') or contains(@placeholder,'Search')]"));
			type(searchInput, state);
			sleep(1000);
		} catch (Exception e) {
		}

		// Select the state option
		selectDropdownOption(state);
	}

	/**
	 * Select Texas as the suggested state XPath for Texas option:
	 * //div[@role='option' and contains(.,'Texas')]
	 */
	public void selectTexasState() {
		logger.info("Selecting Texas as suggested state");
		selectSuggestedState("Texas");
		captureScreenshotToReport("Selected Texas State");
	}

	// ==================== Action Buttons Methods ====================

	public void clickResetForm() {
		waitForClickable(resetFormButton);
		click(resetFormButton);
	}

	public String getSubmitButtonText() {
		return getText(submitQuoteButton);
	}

	public WebElement getSubmitQuoteButton() {
		return submitQuoteButton;
	}

	// ==================== Location Section Methods ====================

	public void clickNewLocationButton() {
		logger.info("Clicking New Location button");

		// Use specific XPath directly for speed
		String specificXpath = "//*[@id='root']/div[2]/div[3]/div[1]/div/button[2]";

		try {
			WebElement button = driver.findElement(By.xpath(specificXpath));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
			sleep(300);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
			logger.info("Clicked New Location button");
			return;
		} catch (Exception e) {
		}

		// Fallback xpaths
		String[] xpaths = {
				"//button[contains(text(),'New Location')]",
				"//button[contains(.,'New Location')]" };

		for (String xpath : xpaths) {
			try {
				WebElement button = driver.findElement(By.xpath(xpath));
				if (button.isDisplayed()) {
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
					logger.info("Clicked New Location button via fallback");
					return;
				}
			} catch (Exception e) {
				// Continue
			}
		}

		throw new RuntimeException("New Location button not found");
	}

	// Note: Common methods (sleep, getCurrentUrl, clearAndType) and page-specific
	// boolean/wait methods are in CreateQuotePage.java
}
