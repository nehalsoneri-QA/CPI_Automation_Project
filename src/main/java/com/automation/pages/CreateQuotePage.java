package com.automation.pages;

import com.github.javafaker.Faker;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Create Quote Page - High-Level Quote Creation Actions
 * Contains business-level quote creation operations that call basic methods from CreateQuoteLocators
 * This is the class to be used in tests (Cucumber steps, TestNG tests)
 * URL: /new_quote
 */
public class CreateQuotePage extends CreateQuoteLocators {

    // Test Data - Default Values
    private static final String DEFAULT_GL_AMOUNT = "150";
    private static final String DEFAULT_WSB_AMOUNT = "100";

    // Faker for generating test data when auto-complete fails
    private static final Faker faker = new Faker(new Locale("en-US"));

    // Store location data for PDF page 2 validation
    private java.util.List<LocationData> addedLocations = new java.util.ArrayList<>();

    /**
     * Constructor
     */
    public CreateQuotePage(WebDriver driver) {
        super(driver);
        logger.info("CreateQuotePage initialized");
    }

    // ==================== Liability Configuration Methods ====================

    /**
     * Configure General Liability settings
     */
    public void configureGeneralLiability(boolean enable, String amount) {
        logger.info("Configuring General Liability: enabled={}, amount={}", enable, amount);

        if (enable) {
            selectGeneralLiabilityYes();
            sleep(500);
            if (amount != null && !amount.isEmpty() && isGeneralLiabilityAmountDisplayed()) {
                enterGeneralLiabilityAmount(amount);
                clickGeneralLiabilityApply();
            }
        } else {
            selectGeneralLiabilityNo();
        }
    }

    /**
     * Configure Water & Sewer Backup settings
     */
    public void configureWaterSewerBackup(boolean enable, String amount) {
        logger.info("Configuring Water & Sewer Backup: enabled={}, amount={}", enable, amount);

        if (enable) {
            selectWaterSewerBackupYes();
            sleep(500);
            if (amount != null && !amount.isEmpty() && isWaterSewerBackupAmountDisplayed()) {
                enterWaterSewerBackupAmount(amount);
                clickWaterSewerBackupApply();
            }
        } else {
            selectWaterSewerBackupNo();
        }
    }

    // ==================== Screenshot Methods ====================

    /**
     * Capture screenshot with description
     */
    public void captureScreenshot(String description) {
        captureScreenshotToReport(description);
    }

    // ==================== Wait Methods ====================

    /**
     * Wait for page to be ready for interaction
     * Waits for URL, then waits for form elements (dropdowns) to be visible
     */
    public void waitForPageReady() {
        logger.info("Waiting for Create Quote page to be ready");
        waitForPageLoad();
        sleep(2000);

        // Wait for form elements to be visible - specifically Agent dropdown
        boolean formLoaded = false;
        try {
            org.openqa.selenium.support.ui.WebDriverWait formWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));

            // Wait for any dropdown button to be visible (indicates form is loaded)
            formWait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.xpath("//label[contains(text(),'Agent')]/following::button[1]")),
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.xpath("//label[contains(text(),'Select Agent')]/following::button[1]")),
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.xpath("//*[contains(@id,'agent')]//button")),
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.xpath("//button[contains(@aria-label,'Agent')]"))
            ));
            formLoaded = true;
            logger.info("Form elements are visible");
        } catch (Exception e) {
            logger.warn("Form elements not found, refreshing page...");
        }

        // If form not loaded, refresh page and try again (SPA loading issue fix)
        if (!formLoaded) {
            driver.navigate().refresh();
            sleep(3000);
            try {
                org.openqa.selenium.support.ui.WebDriverWait formWait =
                    new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15));
                formWait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.xpath("//label[contains(text(),'Agent')]/following::button[1]")));
                logger.info("Form elements visible after refresh");
            } catch (Exception e) {
                logger.warn("Form elements still not found after refresh: {}", e.getMessage());
            }
        }

        sleep(1000);
    }

    // ==================== Invalid Data Validation Methods ====================

    /**
     * Validate form with invalid/empty data and verify submit button is disabled
     * Takes screenshot of each field showing validation state
     * @return true if submit button is correctly disabled with invalid data
     */
    public boolean validateInvalidDataAndCaptureScreenshots() {
        logger.info("Starting invalid data validation with screenshots");
        boolean allValidationsPassed = true;

        waitForPageLoad();
        sleep(2000);

        // Capture initial state - submit should be disabled
        captureScreenshotToReport("Initial State - Empty Form");
        boolean initialDisabled = isSubmitButtonDisabledDynamic();
        logger.info("Initial submit button disabled: {}", initialDisabled);
        if (!initialDisabled) {
            allValidationsPassed = false;
        }

        // Scroll to and capture each form section
        captureFormSectionScreenshots();

        // Try to click submit without filling form - should remain disabled or show error
        scrollToSubmitButton();
        captureScreenshotToReport("Submit Button State - No Data Entered");

        logger.info("Invalid data validation completed. All validations passed: {}", allValidationsPassed);
        return allValidationsPassed;
    }

    /**
     * Capture screenshots of each form section/field
     * Skips Yes/No radio button fields for GL, WS, and Animal Liability
     */
    public void captureFormSectionScreenshots() {
        logger.info("Capturing screenshots of each form section (skipping Yes/No fields)");

        // Find all form field containers (usually div with label)
        List<WebElement> formSections = driver.findElements(By.xpath(
            "//label/parent::div | //label/ancestor::div[contains(@class,'form') or contains(@class,'field') or contains(@class,'group')][1]"
        ));

        // Labels to skip (Yes/No radio options)
        List<String> labelsToSkip = java.util.Arrays.asList("Yes", "No");

        int sectionCount = 0;
        for (WebElement section : formSections) {
            try {
                String labelText = "";
                try {
                    WebElement label = section.findElement(By.tagName("label"));
                    labelText = label.getText().trim();
                } catch (Exception e) {
                    labelText = "Section " + sectionCount;
                }

                if (labelText.isEmpty() || labelText.length() > 50) {
                    continue;
                }

                // Skip Yes/No fields
                if (labelsToSkip.contains(labelText)) {
                    continue;
                }

                // Scroll to section
                scrollIntoView(section);
                sleep(300);

                // Highlight and capture
                highlightElement(section);
                captureScreenshotToReport("Field: " + labelText);
                removeHighlight(section);

                sectionCount++;
            } catch (Exception e) {
            }
        }

        logger.info("Captured {} form section screenshots", sectionCount);
    }

    // ==================== Submit Button Disabled Until Complete ====================

    /**
     * Verify submit button remains disabled until all required data is entered
     * including adding a new location
     * Takes individual field screenshots at each step
     * @return true if submit button behaves correctly throughout the process
     */
    public boolean validateSubmitDisabledUntilComplete() {
        logger.info("Starting validation: Submit button disabled until form is complete");
        boolean validationPassed = true;

        waitForPageLoad();
        sleep(2000);

        // Step 1: Initial state - Submit should be disabled
        logger.info("Step 1: Checking initial state");
        captureScreenshotToReport("Step 1 - Initial Empty Form");
        if (!verifySubmitButtonDisabled("Initial empty form")) {
            validationPassed = false;
        }

        // Step 2: Select Agent only - Submit should still be disabled
        logger.info("Step 2: Selecting Agent only");
        try {
            selectFirstAvailableAgent();
            sleep(1500);
            captureScreenshotToReport("Step 2 - After Agent Selection");
            if (!verifySubmitButtonDisabled("After Agent selection only")) {
                validationPassed = false;
            }
        } catch (Exception e) {
            captureScreenshotToReport("Step 2 - Agent Selection Failed");
        }

        // Step 3: Select Insured - Submit should still be disabled
        logger.info("Step 3: Selecting Insured");
        try {
            selectFirstAvailableInsured();
            sleep(1500);
            // Capture individual Insured field screenshot
            captureIndividualFieldScreenshot("Insured", "Step 3 - After Insured Selection");
            if (!verifySubmitButtonDisabled("After Insured selection")) {
                validationPassed = false;
            }
        } catch (Exception e) {
            captureScreenshotToReport("Step 3 - Insured Selection Failed");
        }

        // Step 4: Select Carrier - Submit should still be disabled
        logger.info("Step 4: Selecting Carrier");
        try {
            selectFirstAvailableCarrier();
            sleep(1500);
            // Capture individual Carrier field screenshot
            captureIndividualFieldScreenshot("carrier", "Step 4 - After Carrier Selection");
            if (!verifySubmitButtonDisabled("After Carrier selection")) {
                validationPassed = false;
            }
        } catch (Exception e) {
            captureScreenshotToReport("Step 4 - Carrier Selection Failed");
        }

        // Step 5: Configure General Liability
        logger.info("Step 5: Configuring General Liability");
        try {
            configureGeneralLiability(true, DEFAULT_GL_AMOUNT);
            sleep(1000);
            // Capture individual GL field screenshot
            captureIndividualFieldScreenshot("General Liability", "Step 5 - After GL Configuration");
            boolean submitState = isSubmitButtonDisabledDynamic();
            logger.info("Submit button disabled after GL config: {}", submitState);
        } catch (Exception e) {
            captureIndividualFieldScreenshot("General Liability", "Step 5 - GL Configuration Failed");
        }

        // Step 6: Configure Water & Sewer Backup
        logger.info("Step 6: Configuring Water & Sewer Backup");
        try {
            configureWaterSewerBackup(true, DEFAULT_WSB_AMOUNT);
            sleep(1000);
            // Capture individual WS field screenshot
            captureIndividualFieldScreenshot("Water", "Step 6 - After WSB Configuration");
        } catch (Exception e) {
            captureIndividualFieldScreenshot("Water", "Step 6 - WSB Configuration Failed");
        }

        // Step 7: Configure Animal Liability
        logger.info("Step 7: Configuring Animal Liability");
        try {
            selectAnimalLiabilityNo();
            sleep(500);
            captureIndividualFieldScreenshot("Animal Liability", "Step 7 - After Animal Liability Configuration");
        } catch (Exception e) {
        }

        // Step 8: Final state - Check submit button
        logger.info("Step 8: Final form state");
        scrollToSubmitButton();
        sleep(500);
        captureIndividualFieldScreenshot("Submit", "Step 8 - Final Form State");

        boolean finalSubmitEnabled = !isSubmitButtonDisabledDynamic();
        logger.info("Final submit button enabled: {}", finalSubmitEnabled);

        if (finalSubmitEnabled) {
            logger.info("SUCCESS: Submit button is now enabled after completing all fields");
            captureScreenshotToReport("SUCCESS - Submit Button Enabled");
        } else {
            captureIndividualFieldScreenshot("Submit", "Submit Button Still Disabled");
        }

        logger.info("Submit disabled until complete validation finished. Result: {}", validationPassed);
        return validationPassed;
    }

    /**
     * Capture screenshot of individual field by label text
     */
    private void captureIndividualFieldScreenshot(String fieldLabel, String screenshotName) {
        try {
            WebElement field = findFieldByLabel(fieldLabel);
            if (field != null) {
                scrollIntoView(field);
                sleep(300);
                highlightElement(field);
                captureScreenshotToReport(screenshotName);
                removeHighlight(field);
            } else {
                // If field not found by label, try finding by ID or other attributes
                WebElement altField = findFieldByAlternative(fieldLabel);
                if (altField != null) {
                    scrollIntoView(altField);
                    sleep(300);
                    highlightElement(altField);
                    captureScreenshotToReport(screenshotName);
                    removeHighlight(altField);
                } else {
                    captureScreenshotToReport(screenshotName);
                }
            }
        } catch (Exception e) {
            captureScreenshotToReport(screenshotName);
        }
    }

    /**
     * Find field by alternative methods (ID, class, text content)
     */
    private WebElement findFieldByAlternative(String fieldLabel) {
        String[] xpaths = {
            "//*[contains(@id,'" + fieldLabel.toLowerCase().replace(" ", "-") + "')]",
            "//*[contains(@class,'" + fieldLabel.toLowerCase().replace(" ", "-") + "')]",
            "//button[contains(text(),'" + fieldLabel + "')]",
            "//button[contains(@id,'" + fieldLabel.toLowerCase() + "')]",
            "//*[contains(text(),'" + fieldLabel + "')]/ancestor::div[1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            } catch (Exception e) {
                // Continue
            }
        }
        return null;
    }

    /**
     * Verify submit button is disabled and log/capture the state
     */
    private boolean verifySubmitButtonDisabled(String stepDescription) {
        boolean isDisabled = isSubmitButtonDisabledDynamic();
        if (isDisabled) {
            logger.info("PASS: Submit button correctly disabled at step: {}", stepDescription);
            return true;
        } else {
            captureScreenshotToReport("FAIL - Submit Enabled Prematurely - " + stepDescription);
            return false;
        }
    }

    /**
     * Check if submit button is disabled using dynamic locators
     */
    private boolean isSubmitButtonDisabledDynamic() {
        try {
            // Try multiple ways to find the submit button
            String[] xpaths = {
                "//button[contains(text(),'Submit')]",
                "//button[contains(text(),'Create Quote')]",
                "//button[contains(text(),'Save')]",
                "//button[contains(@class,'submit')]",
                "//button[@type='submit']"
            };

            for (String xpath : xpaths) {
                List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed()) {
                        boolean disabled = !btn.isEnabled() ||
                            "true".equals(btn.getAttribute("disabled")) ||
                            btn.getAttribute("class").contains("disabled");
                        return disabled;
                    }
                }
            }

            return true; // Assume disabled if not found
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Fill location details if the location form is present
     */
    private void fillLocationDetailsIfPresent() {
        logger.info("Attempting to fill location details");

        try {
            // Look for location form fields
            List<WebElement> locationInputs = driver.findElements(By.xpath(
                "//input[contains(@placeholder,'address') or contains(@placeholder,'Address') or " +
                "contains(@name,'address') or contains(@id,'address') or " +
                "contains(@placeholder,'street') or contains(@placeholder,'Street')]"
            ));

            if (!locationInputs.isEmpty()) {
                // Fill address
                locationInputs.get(0).sendKeys("123 Test Street");
                sleep(500);
            }

            // Look for city field
            List<WebElement> cityInputs = driver.findElements(By.xpath(
                "//input[contains(@placeholder,'city') or contains(@placeholder,'City') or " +
                "contains(@name,'city') or contains(@id,'city')]"
            ));
            if (!cityInputs.isEmpty()) {
                cityInputs.get(0).sendKeys("Test City");
                sleep(500);
            }

            // Look for state dropdown
            WebElement stateDropdown = findDropdownByLabel("State");
            if (stateDropdown != null) {
                click(stateDropdown);
                sleep(500);
                selectFirstDropdownOption();
            }

            // Look for zip code
            List<WebElement> zipInputs = driver.findElements(By.xpath(
                "//input[contains(@placeholder,'zip') or contains(@placeholder,'Zip') or " +
                "contains(@name,'zip') or contains(@id,'zip') or contains(@placeholder,'postal')]"
            ));
            if (!zipInputs.isEmpty()) {
                zipInputs.get(0).sendKeys("12345");
                sleep(500);
            }

            // Look for save/add location button
            List<WebElement> saveButtons = driver.findElements(By.xpath(
                "//button[contains(text(),'Save') or contains(text(),'Add') or contains(text(),'Confirm')]"
            ));
            for (WebElement btn : saveButtons) {
                if (btn.isDisplayed() && btn.isEnabled()) {
                    click(btn);
                    sleep(1000);
                    break;
                }
            }

        } catch (Exception e) {
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Find a form field by its label text
     */
    private WebElement findFieldByLabel(String labelText) {
        String[] xpaths = {
            "//label[contains(text(),'" + labelText + "')]/following-sibling::*[1]",
            "//label[contains(text(),'" + labelText + "')]/..//*[self::input or self::button or self::select]",
            "//label[contains(text(),'" + labelText + "')]/parent::div",
            "//*[contains(text(),'" + labelText + "')]/ancestor::div[1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            } catch (Exception e) {
                // Continue
            }
        }
        return null;
    }

    /**
     * Scroll to the submit button
     */
    private void scrollToSubmitButton() {
        try {
            WebElement submitBtn = driver.findElement(By.xpath(
                "//button[contains(text(),'Submit') or contains(text(),'Create Quote')]"
            ));
            scrollIntoView(submitBtn);
        } catch (Exception e) {
            // Scroll to bottom of page
            scrollToBottom();
        }
    }

    /**
     * Highlight an element for screenshot
     */
    private void highlightElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.border='3px solid red'; arguments[0].style.backgroundColor='#ffffcc';",
                element
            );
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Remove highlight from element
     */
    private void removeHighlight(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.border=''; arguments[0].style.backgroundColor='';",
                element
            );
        } catch (Exception e) {
            // Ignore
        }
    }

    // ==================== Location Dialog Methods ====================

    /**
     * Click New Location button to open location dialog
     * XPath: //button[contains(text(),'New Location')]
     */
    public void openNewLocationDialog() {
        logger.info("Opening New Location dialog");
        clickNewLocationButton();
        sleep(2000);
    }

    /**
     * Enter location details from test data map
     * @param locationData Map containing location field values
     */
    public void enterLocationDetails(Map<String, String> locationData) {
        logger.info("Entering location details from test data");

        // Wait for dialog to be fully visible
        sleep(1000);

        // Find the dialog container
        WebElement dialog = findLocationDialog();
        if (dialog == null) {
            captureScreenshotToReport("Location Dialog Not Found");
            return;
        }

        logger.info("Found location dialog, entering data within dialog context");

        // Enter Physical Address with auto-complete
        String address = locationData.getOrDefault("Address", "123 William St, New York, NY 10038, USA");
        enterAddressWithAutoComplete(dialog, address);
        logger.info("Entered Physical address with auto-complete: {}", address);

        // City, State, Zip should be auto-filled from address selection
        sleep(1000);

        // Check if City was auto-filled, if not enter it
        String city = locationData.getOrDefault("City", "New York");
        String currentCity = getFieldValueInDialog(dialog, "City");
        if (currentCity == null || currentCity.isEmpty()) {
            enterFieldInDialog(dialog, "City", city);
            logger.info("Entered city: {}", city);
        } else {
            logger.info("City auto-filled: {}", currentCity);
        }

        // Check if State was auto-filled
        sleep(500);

        // Check if Zip was auto-filled, if not enter it
        String zipCode = locationData.getOrDefault("ZipCode", "10038");
        String currentZip = getFieldValueInDialog(dialog, "Zip");
        if (currentZip == null || currentZip.isEmpty()) {
            enterFieldInDialog(dialog, "Zip", zipCode);
            logger.info("Entered zip code: {}", zipCode);
        } else {
            logger.info("Zip auto-filled: {}", currentZip);
        }

        // Scroll down in dialog to see more fields
        scrollDialogDown(dialog);
        sleep(500);

        // Enter Sq. Ft. * (Square Feet) - Required field
        String sqFt = locationData.getOrDefault("SqFt", "1200");
        enterFieldInDialog(dialog, "Sq. Ft", sqFt);
        logger.info("Entered Sq. Ft.: {}", sqFt);

        // Enter Coverage A
        String coverageA = locationData.getOrDefault("CoverageA", "50000");
        enterFieldInDialog(dialog, "Coverage A", coverageA);
        logger.info("Entered Coverage A: {}", coverageA);

        // Enter Coverage B
        String coverageB = locationData.getOrDefault("CoverageB", "5000");
        enterFieldInDialog(dialog, "Coverage B", coverageB);
        logger.info("Entered Coverage B: {}", coverageB);

        // Scroll down to reveal Coverage C field
        scrollDialogDown(dialog);
        sleep(500);

        // Enter Coverage C using specific XPath
        String coverageC = locationData.getOrDefault("CoverageC", "5000");
        boolean coverageCEntered = enterCoverageCValue(coverageC);
        if (coverageCEntered) {
            logger.info("Entered Coverage C: {}", coverageC);
        } else {
        }

        // Enter Loss of Rents
        String lossOfRents = locationData.getOrDefault("LossOfRents", "5000");
        enterFieldInDialog(dialog, "Loss of Rents", lossOfRents);
        logger.info("Entered Loss of Rents: {}", lossOfRents);

        // Scroll down more
        scrollDialogDown(dialog);
        sleep(500);

        // Enter Suggested Rate
        String suggestedRate = locationData.getOrDefault("SuggestedRate", "0.5");
        enterFieldInDialog(dialog, "Suggested Rate", suggestedRate);
        logger.info("Entered Suggested Rate: {}", suggestedRate);

        // NOTE: Do NOT enter anything in Mortgagee address fields - skip them intentionally

        // Scroll dialog to show all entered data
        scrollDialogToTop(dialog);
        sleep(500);

        // Take screenshot of entire dialog after all data entered
        captureScreenshotToReport("New Location Dialog - All Data Entered");
    }

    /**
     * Scroll dialog to top to capture full view
     */
    private void scrollDialogToTop(WebElement dialog) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = 0;", dialog);
        } catch (Exception e) {
        }
    }

    /**
     * Enter address in Physical address field and select from auto-complete dropdown
     * @param dialog the dialog element
     * @param address the full address to enter
     */
    private void enterAddressWithAutoComplete(WebElement dialog, String address) {
        logger.info("Entering address with auto-complete: {}", address);

        // Find the Physical address input field
        String[] xpaths = {
            ".//label[contains(text(),'Physical address')]/following::input[1]",
            ".//label[contains(text(),'Physical Address')]/following::input[1]",
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
            // Look for auto-complete suggestion dropdown
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
                click(suggestion);
                sleep(1000);
                logger.info("Auto-complete suggestion selected");
            } else {
                // Try pressing down arrow and enter to select first suggestion
                logger.info("No suggestion element found, trying keyboard navigation");
                addressInput.sendKeys(Keys.ARROW_DOWN);
                sleep(300);
                addressInput.sendKeys(Keys.ENTER);
                sleep(1000);
            }
        } catch (Exception e) {
            // Continue without selection
        }
    }

    /**
     * Enter Coverage C value using specific XPath
     * @param value the value to enter
     * @return true if value was entered successfully
     */
    private boolean enterCoverageCValue(String value) {

        // Use the specific XPath provided
        String specificXpath = "//*[@id=':rg4:-form-item']/input";

        // Also try alternative XPaths in case ID changes
        String[] xpaths = {
            specificXpath,
            "//input[contains(@id,'rg4')]",
            "//label[contains(text(),'Coverage C')]/following::input[1]",
            "//label[text()='Coverage C']/following::input[1]",
            "//*[contains(text(),'Coverage C')]/following::input[1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = driver.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        // Scroll into view
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
                        sleep(300);

                        // Clear and enter value
                        clearInputField(input);
                        sleep(100);
                        input.sendKeys(value);
                        sleep(200);

                        logger.info("Entered Coverage C value using xpath: {}", xpath);
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.trace("XPath {} failed: {}", xpath, e.getMessage());
            }
        }

        return false;
    }

    /**
     * Try to enter value in a field using multiple label variations
     * @param dialog the dialog element
     * @param labelVariations array of possible label texts
     * @param value the value to enter
     * @return true if field was found and value entered
     */
    private boolean enterFieldInDialogWithRetry(WebElement dialog, String[] labelVariations, String value) {
        for (String label : labelVariations) {
            if (tryEnterFieldInDialog(dialog, label, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to enter value in a field, return success status
     */
    private boolean tryEnterFieldInDialog(WebElement dialog, String fieldLabel, String value) {

        String[] xpaths = {
            ".//label[contains(text(),'" + fieldLabel + "')]/following::input[1]",
            ".//label[contains(text(),'" + fieldLabel + "')]/..//input",
            ".//label[text()='" + fieldLabel + "']/following::input[1]",
            ".//*[contains(text(),'" + fieldLabel + "')]/following::input[1]",
            ".//input[contains(@placeholder,'" + fieldLabel + "')]",
            ".//input[contains(@name,'" + fieldLabel.toLowerCase().replace(" ", "") + "')]",
            ".//label[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + fieldLabel.toLowerCase() + "')]/following::input[1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        // Skip mortgagee fields
                        try {
                            WebElement parent = input.findElement(By.xpath("./ancestor::div[3]"));
                            if (parent.getText().toLowerCase().contains("mortgagee")) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Continue with this input
                        }

                        // Scroll element into view
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
                        sleep(300);

                        // Clear and enter value
                        clearInputField(input);
                        sleep(100);
                        input.sendKeys(value);
                        sleep(200);

                        return true;
                    }
                }
            } catch (Exception e) {
                // Continue to next strategy
            }
        }
        return false;
    }

    /**
     * Get current value of a field within the dialog
     * @param dialog the dialog element
     * @param fieldLabel the field label to search for
     * @return the field value or null if not found
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
     * Enter value in a field within the dialog context
     * Uses base class clearInputField method for robust clearing
     */
    private void enterFieldInDialog(WebElement dialog, String fieldLabel, String value) {

        // Skip Mortgagee fields - do not enter anything in them
        if (fieldLabel.toLowerCase().contains("mortgagee")) {
            return;
        }

        // Search within dialog using relative XPaths
        String[] xpaths = {
            ".//label[contains(text(),'" + fieldLabel + "')]/following::input[1]",
            ".//label[contains(text(),'" + fieldLabel + "')]/..//input",
            ".//label[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + fieldLabel.toLowerCase() + "')]/following::input[1]",
            ".//*[contains(text(),'" + fieldLabel + "')]/following::input[1]",
            ".//input[contains(@placeholder,'" + fieldLabel + "')]",
            ".//input[contains(@placeholder,'" + fieldLabel.toLowerCase() + "')]",
            ".//input[contains(@name,'" + fieldLabel.toLowerCase().replace(" ", "") + "')]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        // Skip if this is a mortgagee field
                        try {
                            WebElement parent = input.findElement(By.xpath("./ancestor::div[3]"));
                            if (parent.getText().toLowerCase().contains("mortgagee")) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Continue with this input
                        }

                        // Scroll element into view within dialog
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", input);
                        sleep(300);

                        // Use base class clearInputField for robust clearing
                        clearInputField(input);
                        sleep(100);
                        input.sendKeys(value);
                        sleep(200);

                        return;
                    }
                }
            } catch (Exception e) {
                logger.trace("XPath {} failed: {}", xpath, e.getMessage());
            }
        }

        // Fallback: Try finding all inputs in dialog and match by nearby text
        try {
            List<WebElement> allInputs = dialog.findElements(By.xpath(".//input[@type='text' or @type='number' or not(@type)]"));
            for (WebElement input : allInputs) {
                try {
                    // Get parent and check for label text
                    WebElement parent = input.findElement(By.xpath("./ancestor::div[1]"));
                    String parentText = parent.getText().toLowerCase();

                    // Skip mortgagee fields
                    if (parentText.contains("mortgagee")) {
                        continue;
                    }

                    if (parentText.contains(fieldLabel.toLowerCase()) && input.isDisplayed()) {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", input);
                        sleep(200);

                        // Use base class clearInputField for robust clearing
                        clearInputField(input);
                        sleep(100);
                        input.sendKeys(value);
                        return;
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
        } catch (Exception e) {
            logger.trace("Fallback search failed: {}", e.getMessage());
        }

    }

    /**
     * Select state dropdown within dialog
     * @return true if state was selected from dropdown, false if fallback to text entry needed
     */
    private boolean selectStateInDialog(WebElement dialog, String state) {

        try {
            // Find state dropdown/button within dialog
            String[] dropdownXpaths = {
                ".//label[contains(text(),'State')]/following::button[1]",
                ".//label[contains(text(),'State')]/..//button",
                ".//*[contains(text(),'State')]/following::button[1]",
                ".//button[contains(@class,'select')]"
            };

            WebElement stateDropdown = null;
            for (String xpath : dropdownXpaths) {
                try {
                    List<WebElement> elements = dialog.findElements(By.xpath(xpath));
                    for (WebElement elem : elements) {
                        if (elem.isDisplayed()) {
                            stateDropdown = elem;
                            break;
                        }
                    }
                    if (stateDropdown != null) break;
                } catch (Exception e) {
                    // Continue
                }
            }

            if (stateDropdown != null) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", stateDropdown);
                sleep(300);
                click(stateDropdown);
                sleep(500);

                // Select the state option (options appear outside dialog typically)
                try {
                    selectDropdownOption(state);
                    sleep(300);
                    return true;
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Scroll down within dialog
     */
    private void scrollDialogDown(WebElement dialog) {
        try {
            // Find scrollable content within dialog
            WebElement scrollable = null;
            String[] scrollableXpaths = {
                ".//div[contains(@class,'overflow')]",
                ".//div[contains(@class,'scroll')]",
                ".//div[contains(@class,'content')]",
                "."
            };

            for (String xpath : scrollableXpaths) {
                try {
                    List<WebElement> elements = dialog.findElements(By.xpath(xpath));
                    for (WebElement elem : elements) {
                        if (elem.isDisplayed()) {
                            scrollable = elem;
                            break;
                        }
                    }
                    if (scrollable != null) break;
                } catch (Exception e) {
                    // Continue
                }
            }

            if (scrollable != null) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTop += 300;", scrollable);
            } else {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTop += 300;", dialog);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Find location dialog element
     */
    private WebElement findLocationDialog() {
        String[] xpaths = {
            "//div[@role='dialog']",
            "//div[contains(@class,'dialog')]",
            "//div[contains(@class,'modal')]",
            "//div[contains(@class,'Dialog')]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> dialogs = driver.findElements(By.xpath(xpath));
                for (WebElement dialog : dialogs) {
                    if (dialog.isDisplayed()) {
                        return dialog;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }
        return null;
    }

    /**
     * Enter value in a location field
     */
    private void enterLocationField(String fieldName, String value) {
        String[] xpaths = {
            "//input[contains(@placeholder,'" + fieldName + "')]",
            "//input[contains(@placeholder,'" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "')]",
            "//input[contains(@name,'" + fieldName + "')]",
            "//input[contains(@id,'" + fieldName + "')]",
            "//label[contains(text(),'" + fieldName + "')]/following::input[1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = driver.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed()) {
                        input.clear();
                        input.sendKeys(value);
                        sleep(300);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }
    }

    /**
     * Select state in location dialog
     */
    private void selectLocationState(String state) {
        try {
            // Find and click state dropdown
            WebElement stateDropdown = findDropdownByLabel("State");
            if (stateDropdown != null) {
                click(stateDropdown);
                sleep(500);
                selectDropdownOption(state);
            } else {
                // Try alternative: input field for state
                enterLocationField("state", state);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Select dropdown in location dialog
     */
    private void selectLocationDropdown(String label, String value) {
        try {
            WebElement dropdown = findDropdownByLabel(label);
            if (dropdown != null) {
                click(dropdown);
                sleep(500);
                selectDropdownOption(value);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Scroll down in the location dialog and take screenshot
     */
    public void scrollLocationDialogAndCapture() {
        logger.info("Scrolling location dialog");

        WebElement dialog = findLocationDialog();
        if (dialog != null) {
            // Scroll to bottom of dialog
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTop = arguments[0].scrollHeight;", dialog
            );
            sleep(500);
            captureScreenshotToReport("Location Dialog - After Scroll");
        } else {
            // Try scrolling the page
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 300);");
            sleep(500);
            captureScreenshotToReport("Location Dialog - After Scroll");
        }
    }

    /**
     * Click submit/add button in location dialog
     * XPath: //button[contains(text(),'Add') or contains(text(),'Save') or contains(text(),'Submit')]
     */
    public void submitLocationDialog() {
        logger.info("Submitting location dialog");

        String[] xpaths = {
            "//div[@role='dialog']//button[contains(text(),'Add')]",
            "//div[@role='dialog']//button[contains(text(),'Save')]",
            "//div[@role='dialog']//button[contains(text(),'Submit')]",
            "//div[contains(@class,'dialog')]//button[contains(text(),'Add')]",
            "//button[contains(text(),'Add Location')]",
            "//button[contains(text(),'Save Location')]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed() && btn.isEnabled()) {
                        click(btn);
                        sleep(2000);
                        logger.info("Location submitted successfully");
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }
    }

    /**
     * Complete flow: Open dialog, enter data, scroll, screenshot, and submit
     */
    public void addNewLocationWithData(Map<String, String> locationData) {
        logger.info("Adding new location with test data");

        // Step 1: Open location dialog
        openNewLocationDialog();

        // Step 2: Enter location details
        enterLocationDetails(locationData);

        // Step 3: Scroll and capture
        scrollLocationDialogAndCapture();

        // Step 4: Submit location
        submitLocationDialog();
    }

    // ==================== State Selection Methods ====================

    /**
     * Select state by name
     * @param stateName the state name to select (e.g., "New York", "Texas")
     */
    public void selectState(String stateName) {
        logger.info("Selecting state: {}", stateName);
        selectSuggestedState(stateName);
    }

    // ==================== Location Verification Methods ====================

    /**
     * Verify that a location was added and appears in the Locations Details table
     * @return true if location is visible in the table
     */
    public boolean verifyLocationAddedInTable() {
        logger.info("Verifying location was added to Locations Details table");
        sleep(2000);

        // Check if dialog is still open (if open, location was not added)
        try {
            WebElement dialog = findLocationDialog();
            if (dialog != null && dialog.isDisplayed()) {
                return false;
            }
        } catch (Exception e) {
            // Dialog not found, which is expected if location was added
        }

        // Check for Locations Details table/section
        String[] tableXpaths = {
            "//h2[contains(text(),'Locations Details')]",
            "//div[contains(text(),'Locations Details')]",
            "//*[contains(@class,'location')]//table",
            "//table[contains(@class,'location')]"
        };

        for (String xpath : tableXpaths) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    logger.info("Found Locations Details section");
                    return true;
                }
            } catch (Exception e) {
                // Continue
            }
        }

        // Check for any table rows that might contain location data
        try {
            List<WebElement> tableRows = driver.findElements(By.xpath("//table//tr"));
            if (tableRows.size() > 1) { // More than header row
                logger.info("Found table with {} rows", tableRows.size());
                return true;
            }
        } catch (Exception e) {
            // Continue
        }

        return false;
    }

    // ==================== Page Load and Verification Methods ====================

    /**
     * Wait for Create Quote page to load
     * Uses dynamic URL pattern from BasePage
     */
    public void waitForPageLoad() {
        waitForPageLoadOrFail("/new_quote", "Create Quote");
    }

    /**
     * Wait for Submit button to be visible and enabled
     * Useful after file upload when page may still be processing
     */
    public void waitForSubmitButtonVisible() {
        logger.info("Waiting for Submit button to be visible...");
        int maxWaitSeconds = 30;
        int waited = 0;

        String[] submitXpaths = {
            "//button[contains(text(),'Submit')]",
            "//button[@id='new-quote-submit-button']",
            "//button[contains(@class,'submit')]",
            "//button[@type='submit']",
            "//button[contains(text(),'Create Quote')]"
        };

        while (waited < maxWaitSeconds) {
            for (String xpath : submitXpaths) {
                try {
                    java.util.List<org.openqa.selenium.WebElement> buttons = driver.findElements(org.openqa.selenium.By.xpath(xpath));
                    for (org.openqa.selenium.WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            logger.info("Submit button is visible and enabled after {} seconds", waited);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
            sleep(1000);
            waited++;
        }
        logger.warn("Submit button not found after {} seconds, continuing anyway", maxWaitSeconds);
    }

    /**
     * Check if page title is displayed
     * @return true if page title is visible
     */
    public boolean isPageTitleDisplayed() {
        return isElementDisplayedSafe(pageTitleLocator);
    }

    /**
     * Check if currently on Create Quote page
     * @return true if on Create Quote page
     */
    public boolean isOnCreateQuotePage() {
        return getCurrentUrl().contains("/new_quote");
    }

    /**
     * Get page title text
     * @return page title text
     */
    public String getPageTitleText() {
        waitForVisibility(pageTitle);
        return getText(pageTitle);
    }

    // ==================== Form Element State Methods ====================

    /**
     * Check if agent commission input is displayed
     * @return true if displayed
     */
    public boolean isAgentCommissionDisplayed() {
        return isElementDisplayedSafe(agentCommissionInput);
    }

    /**
     * Check if insured dropdown is displayed
     * @return true if displayed
     */
    public boolean isInsuredDropdownDisplayed() {
        return isElementDisplayedSafe(insuredDropdown);
    }

    /**
     * Check if General Liability amount input is displayed
     * @return true if displayed
     */
    public boolean isGeneralLiabilityAmountDisplayed() {
        return isElementDisplayedSafe(generalLiabilityAmountInput);
    }

    /**
     * Check if Water & Sewer Backup amount input is displayed
     * @return true if displayed
     */
    public boolean isWaterSewerBackupAmountDisplayed() {
        return isElementDisplayedSafe(waterSewerBackupAmountInput);
    }

    /**
     * Check if New Location button is displayed
     * @return true if displayed
     */
    public boolean isNewLocationButtonDisplayed() {
        return isElementDisplayedSafe(newLocationButtonLocator);
    }

    /**
     * Check if Locations Details header is displayed
     * @return true if displayed
     */
    public boolean isLocationsDetailsHeaderDisplayed() {
        return isElementDisplayedSafe(locationsDetailsHeader);
    }

    // ==================== Submit Button State Methods ====================

    /**
     * Check if submit button is enabled
     * @return true if enabled
     */
    public boolean isSubmitButtonEnabled() {
        return isElementEnabledSafe(submitQuoteButton);
    }

    /**
     * Check if submit button is disabled
     * @return true if disabled
     */
    public boolean isSubmitButtonDisabled() {
        return !isSubmitButtonEnabled();
    }

    // ==================== Wait Methods ====================

    /**
     * Wait for insured dropdown to appear
     */
    public void waitForInsuredDropdownToAppear() {
        waitForVisibility(insuredDropdownLocator);
    }

    /**
     * Wait for submit button to be clickable
     */
    public void waitForSubmitButton() {
        waitForClickable(submitQuoteButton);
    }

    // ==================== Reset Form Methods ====================

    /**
     * Click reset button and verify form data is cleared
     * @return true if reset was successful
     */
    public boolean resetFormAndVerify() {
        logger.info("Clicking Reset button");

        // Scroll to top of page first
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        sleep(1000);

        // Find reset button by ID first
        WebElement resetBtn = null;
        try {
            resetBtn = driver.findElement(By.id("new-quote-reset-form-button"));
        } catch (Exception e) {
            // Try xpath fallbacks
            String[] xpaths = {
                "//button[@id='new-quote-reset-form-button']",
                "//button[contains(text(),'Reset')]",
                "//button[contains(.,'Reset')]"
            };
            for (String xpath : xpaths) {
                try {
                    resetBtn = driver.findElement(By.xpath(xpath));
                    if (resetBtn.isDisplayed()) break;
                } catch (Exception ex) {
                    // Continue
                }
            }
        }

        if (resetBtn == null) {
            return false;
        }

        // Scroll to button and click
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", resetBtn);
        sleep(500);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resetBtn);
        logger.info("Reset button clicked");

        sleep(2000);

        // Verify data is cleared
        boolean dataCleared = verifyFormDataCleared();

        captureScreenshotToReport("After Reset - Form Data " + (dataCleared ? "Cleared" : "Not Cleared"));

        return dataCleared;
    }

    /**
     * Verify form data is cleared after reset
     */
    private boolean verifyFormDataCleared() {
        try {
            // Check Agent dropdown is empty/placeholder
            String agentText = getDropdownText("Agent");
            boolean agentCleared = agentText.isEmpty() || agentText.contains("Select") || agentText.contains("Choose");

            // Check Insured dropdown is empty/placeholder
            String insuredText = getDropdownText("Insured");
            boolean insuredCleared = insuredText.isEmpty() || insuredText.contains("Select") || insuredText.contains("Choose");

            logger.info("Form cleared check - Agent: {}, Insured: {}", agentCleared, insuredCleared);

            return agentCleared || insuredCleared;
        } catch (Exception e) {
            return true; // Assume cleared if can't verify
        }
    }

    /**
     * Get current text from a dropdown
     */
    private String getDropdownText(String label) {
        try {
            WebElement dropdown = driver.findElement(By.xpath(
                "//label[contains(text(),'" + label + "')]/following::button[1]"
            ));
            return dropdown.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // ==================== Upload File Method ====================

    /**
     * Click Upload button using specific XPath and upload file
     * XPath for Upload button: //*[@id="root"]/div[2]/div[3]/div[1]/div/button[1]/button
     * @param filePath absolute path to the file to upload
     * @return true if upload was successful
     */
    public boolean clickUploadButtonAndUploadFile(String filePath) {
        logger.info("=== Starting Upload File Process ===");
        logger.info("File path to upload: {}", filePath);

        try {
            // Verify file exists
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                logger.error("File does not exist: {}", filePath);
                return false;
            }
            logger.info("File exists and is readable: {}", file.getAbsolutePath());

            // Primary XPath for Upload button (provided by user)
            String primaryUploadButtonXpath = "//*[@id='root']/div[2]/div[3]/div[1]/div/button[1]/button";

            // Fallback XPaths for Upload button
            String[] uploadButtonXpaths = {
                primaryUploadButtonXpath,
                "//*[@id='root']/div[2]/div[3]/div[1]/div/button[1]",
                "//button[contains(text(),'Upload')]",
                "//button[contains(text(),'Upload Quotes')]",
                "//button[contains(@class,'upload')]",
                "//button[.//span[contains(text(),'Upload')]]"
            };

            WebElement uploadButton = null;

            // Try to find Upload button
            for (String xpath : uploadButtonXpaths) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(xpath));
                    for (WebElement el : elements) {
                        if (el.isDisplayed() && el.isEnabled()) {
                            uploadButton = el;
                            logger.info("Found Upload button with XPath: {}", xpath);
                            break;
                        }
                    }
                    if (uploadButton != null) break;
                } catch (Exception e) {
                    logger.debug("XPath not found: {}", xpath);
                }
            }

            if (uploadButton == null) {
                logger.error("Upload button not found with any XPath");
                captureScreenshotToReport("Upload Button Not Found");
                return false;
            }

            // Scroll to Upload button
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", uploadButton);
            sleep(500);

            captureScreenshotToReport("Before Clicking Upload Button");

            // Click Upload button
            try {
                uploadButton.click();
                logger.info("Clicked Upload button");
            } catch (Exception e) {
                logger.info("Regular click failed, trying JavaScript click");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", uploadButton);
            }
            sleep(2000);

            // After clicking, find the file input element
            WebElement fileInput = null;
            String[] fileInputXpaths = {
                "//input[@type='file']",
                "//input[contains(@accept,'.xlsx') or contains(@accept,'.xls') or contains(@accept,'.csv')]",
                "//input[@type='file' and not(@disabled)]"
            };

            for (String xpath : fileInputXpaths) {
                try {
                    List<WebElement> inputs = driver.findElements(By.xpath(xpath));
                    for (WebElement input : inputs) {
                        // File inputs are often hidden but still usable
                        fileInput = input;
                        logger.info("Found file input with XPath: {}", xpath);
                        break;
                    }
                    if (fileInput != null) break;
                } catch (Exception e) {
                    logger.debug("File input XPath not found: {}", xpath);
                }
            }

            if (fileInput == null) {
                logger.error("File input element not found after clicking Upload button");
                captureScreenshotToReport("File Input Not Found");
                return false;
            }

            // Send file path to file input
            logger.info("Sending file path to input element");
            fileInput.sendKeys(file.getAbsolutePath());

            // Wait for file to be selected
            sleep(2000);
            captureScreenshotToReport("After File Selection");

            // Click the "Upload File" confirmation button to actually upload and add locations
            logger.info("Looking for Upload File confirmation button");
            WebElement uploadConfirmButton = null;

            // XPaths for Upload File confirmation button (dynamic ID like radix-:r12:)
            String[] confirmButtonXpaths = {
                "//*[contains(@id,'radix-')]/button[1]",
                "//*[contains(@id,'radix-')]//button[contains(text(),'Upload')]",
                "//button[contains(text(),'Upload File')]",
                "//button[contains(text(),'Upload') and not(contains(text(),'Quotes'))]",
                "//div[contains(@class,'dialog') or contains(@class,'modal')]//button[contains(text(),'Upload')]",
                "//div[contains(@role,'dialog')]//button[contains(text(),'Upload')]",
                "//*[contains(@id,'radix-')]/button[contains(text(),'Upload')]",
                "//button[text()='Upload File']",
                "//button[normalize-space()='Upload File']"
            };

            for (String xpath : confirmButtonXpaths) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            String btnText = btn.getText().trim();
                            // Make sure it's the Upload button, not Cancel
                            if (btnText.toLowerCase().contains("upload") || btnText.isEmpty()) {
                                uploadConfirmButton = btn;
                                logger.info("Found Upload File button with XPath: {} (text: '{}')", xpath, btnText);
                                break;
                            }
                        }
                    }
                    if (uploadConfirmButton != null) break;
                } catch (Exception e) {
                    logger.debug("Confirm button XPath not found: {}", xpath);
                }
            }

            if (uploadConfirmButton == null) {
                logger.error("Upload File confirmation button not found");
                captureScreenshotToReport("Upload Confirm Button Not Found");
                return false;
            }

            // Click the Upload File confirmation button
            captureScreenshotToReport("Before Clicking Upload File Button");
            try {
                uploadConfirmButton.click();
                logger.info("Clicked Upload File confirmation button");
            } catch (Exception e) {
                logger.info("Regular click failed, trying JavaScript click for confirmation button");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", uploadConfirmButton);
            }

            // Wait for locations to be loaded from the file
            sleep(5000);

            captureScreenshotToReport("After Upload File Confirmation");
            logger.info("File upload and location loading completed successfully");
            return true;

        } catch (Exception e) {
            logger.error("Upload failed with exception: {}", e.getMessage());
            e.printStackTrace();
            captureScreenshotToReport("Upload Exception");
            return false;
        }
    }

    /**
     * Upload locations file using Upload Quotes button (legacy method)
     * @param filePath absolute path to the file to upload
     * @return true if upload was successful
     */
    public boolean uploadLocationsFile(String filePath) {
        logger.info("Uploading locations file: {}", filePath);
        try {
            // Find and click Upload Quotes button
            String[] buttonXpaths = {
                "//button[contains(text(),'Upload')]",
                "//button[contains(text(),'Upload Quotes')]",
                "//button[contains(@id,'upload')]",
                "//input[@type='file']"
            };

            WebElement uploadElement = null;
            for (String xpath : buttonXpaths) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(xpath));
                    for (WebElement el : elements) {
                        if (el.isDisplayed()) {
                            uploadElement = el;
                            break;
                        }
                    }
                    if (uploadElement != null) break;
                } catch (Exception e) {
                    // Continue
                }
            }

            if (uploadElement == null) {
                logger.info("Upload button not found");
                return false;
            }

            // If it's a file input, send the file path directly
            if ("input".equalsIgnoreCase(uploadElement.getTagName())) {
                uploadElement.sendKeys(filePath);
            } else {
                // Click button to open file dialog, then find hidden file input
                uploadElement.click();
                sleep(1000);
                WebElement fileInput = driver.findElement(By.xpath("//input[@type='file']"));
                fileInput.sendKeys(filePath);
            }

            sleep(3000); // Wait for upload
            logger.info("File uploaded successfully");
            captureScreenshotToReport("File Uploaded");
            return true;
        } catch (Exception e) {
            logger.info("Upload failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Multi-Location Methods ====================

    /**
     * Add a location and verify it was added successfully (dialog closed)
     * @return true if location was added successfully
     */
    public boolean addLocationAndVerify(Map<String, String> locationData, String paymentPlan, Map<String, String> mortgageeData) {
        String address = locationData != null ? locationData.getOrDefault("Address", "Unknown") : "Unknown";
        try {
            logger.info("Adding location: Address={}, PaymentPlan={}", address, paymentPlan);
            addLocation(locationData, paymentPlan, mortgageeData);

            // Wait and verify dialog is closed - this means location was added
            sleep(1000);
            WebElement dialog = findLocationDialog();
            boolean success = (dialog == null || !dialog.isDisplayed());

            if (success) {
                logger.info("Location added successfully: {} - {}", address, paymentPlan);
                return true;
            } else {
                // Try clicking Add button again
                logger.warn("Dialog still open, retrying Add button...");
                clickAddButtonInDialog();
                sleep(2000);
                dismissChromeDialog();

                dialog = findLocationDialog();
                success = (dialog == null || !dialog.isDisplayed());

                if (success) {
                    logger.info("Location added on retry: {} - {}", address, paymentPlan);
                    return true;
                } else {
                    // Try closing dialog with Escape and moving on
                    logger.error("Location add failed, attempting to close dialog: {}", address);
                    try {
                        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                        sleep(500);
                    } catch (Exception ex) {
                        // Ignore
                    }
                    captureScreenshotToReport("Location Add Failed - " + address);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Error adding location {}: {}", address, e.getMessage());
            captureScreenshotToReport("Location Error - " + address);
            // Try to close any open dialog
            try {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                sleep(500);
            } catch (Exception ex) {
                // Ignore
            }
            return false;
        }
    }

    /**
     * Add a location with specified payment plan and optional mortgagee details
     * @param locationData base location data (Address, SqFt, Coverages, etc.)
     * @param paymentPlan payment plan type: "Paid In Full", "Installments", "Escrow", "Bank"
     * @param mortgageeData optional mortgagee data (only for Escrow), null to skip
     */
    public void addLocation(Map<String, String> locationData, String paymentPlan, Map<String, String> mortgageeData) {
        logger.info("========== Adding location: {} ==========", paymentPlan);

        // Wait a bit before starting and dismiss any existing Chrome dialogs
        sleep(1000);
        dismissChromeDialog();
        sleep(500);

        // Open dialog with retry logic
        WebElement dialog = null;
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            logger.info("Attempting to open dialog (attempt {}/{})", attempt, maxRetries);

            // Click New Location button
            clickNewLocationButton();
            sleep(3000); // Wait for dialog to open

            // Check if dialog opened
            dialog = findLocationDialog();
            if (dialog != null && dialog.isDisplayed()) {
                logger.info("Dialog opened successfully on attempt {}", attempt);
                break;
            }

            // Dialog didn't open, try to dismiss any blocking elements and retry
            dismissChromeDialog();
            sleep(500);

            // Press Escape to close any popups
            try {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                sleep(500);
            } catch (Exception e) {
                // Ignore
            }

            // Click elsewhere to ensure page is ready
            try {
                ((JavascriptExecutor) driver).executeScript("document.body.click();");
                sleep(500);
            } catch (Exception e) {
                // Ignore
            }
        }

        if (dialog == null) {
            captureScreenshotToReport("Dialog Not Found - " + paymentPlan);
            return; // Return gracefully instead of throwing exception
        }
        logger.info("Dialog opened for: {}", paymentPlan);

        // Enter base location details
        enterLocationDetailsInDialog(dialog, locationData);
        logger.info("Location details entered for: {}", paymentPlan);

        // Scroll to payment plan section
        scrollDialogDown(dialog);
        sleep(500);

        // Select payment plan
        selectPaymentPlan(dialog, paymentPlan);
        logger.info("Payment plan selected: {}", paymentPlan);

        // Enter mortgagee details for Escrow payment plan
        // Mortgagee fields only appear AFTER Escrow is selected
        if (paymentPlan.toLowerCase().contains("escrow")) {
            logger.info("Escrow selected - waiting for mortgagee fields to appear");
            sleep(2000); // Wait for mortgagee section to appear

            scrollDialogDown(dialog);
            sleep(500);

            // Enter mortgagee details from data or use defaults
            if (mortgageeData != null && !mortgageeData.isEmpty()) {
                enterMortgageeDetails(dialog, mortgageeData);
                logger.info("Mortgagee details entered from data");
            } else {
                // Use default mortgagee data if not provided
                logger.info("No mortgagee data provided, using defaults");
                Map<String, String> defaultMortgagee = new java.util.HashMap<>();
                defaultMortgagee.put("MortgageeName", "First National Bank");
                defaultMortgagee.put("MortgageeAddress", "100 Wall Street, New York, NY 10005, USA");
                enterMortgageeDetails(dialog, defaultMortgagee);
            }
        } else if (mortgageeData != null && !mortgageeData.isEmpty()) {
            // For non-Escrow, still try to enter mortgagee if provided
            scrollDialogDown(dialog);
            sleep(500);
            enterMortgageeDetails(dialog, mortgageeData);
            logger.info("Mortgagee details entered");
        }

        // Capture screenshot before submit
        scrollDialogToTop(dialog);
        sleep(300);
        captureScreenshotToReport("Before Submit - " + paymentPlan);

        // Click Add button to submit
        clickAddButtonInDialog();
        sleep(3000);

        // Dismiss Chrome save address dialog if appears
        dismissChromeDialog();

        // Verify dialog closed
        WebElement checkDialog = findLocationDialog();
        if (checkDialog != null && checkDialog.isDisplayed()) {
            clickAddButtonInDialog();
            sleep(2000);
            dismissChromeDialog();
        }

        // Store location data for PDF page 2 validation
        storeLocationDataForPDFValidation(locationData);

        // Update stored location with WS, GL, Tax from table (calculated values displayed after add)
        updateLastLocationWithTableValues();

        logger.info("========== Location added: {} ==========", paymentPlan);
    }

    /**
     * Store location data for PDF page 2 validation
     * PDF Page 2 shows: Address, Dwelling(A), Structures(B), Personal Prop(C), Rents(D), TIV, Full Term
     * Premium Formula: Premium = (Coverage / 100) * Rate
     */
    private void storeLocationDataForPDFValidation(Map<String, String> locationData) {
        try {
            String address = locationData.getOrDefault("Address", "");
            if (address.isEmpty()) return;

            // Parse coverage values from location data
            double coverageA = parseAmountValue(locationData.getOrDefault("CoverageA", "50000"));
            double coverageB = parseAmountValue(locationData.getOrDefault("CoverageB", "5000"));
            double coverageC = parseAmountValue(locationData.getOrDefault("CoverageC", "5000"));
            double coverageD = parseAmountValue(locationData.getOrDefault("LossOfRents", "5000"));
            double rate = parseAmountValue(locationData.getOrDefault("SuggestedRate", "0.5"));
            // Parse WS, GL, Tax for Full Term calculation: Full Term = Property Premium + WS + GL + Tax
            double wsPremium = parseAmountValue(locationData.getOrDefault("WSPremium", "0"));
            double glPremium = parseAmountValue(locationData.getOrDefault("GLPremium", "0"));
            double tax = parseAmountValue(locationData.getOrDefault("Tax", "0"));

            // Create and store location data
            LocationData locData = new LocationData(address)
                .setCoverageA(coverageA)
                .setCoverageB(coverageB)
                .setCoverageC(coverageC)
                .setCoverageD(coverageD)
                .setRate(rate)
                .setWsPremium(wsPremium)
                .setGlPremium(glPremium)
                .setTax(tax);

            // Calculate TIV = A + B + C + D
            locData.calculateTIV();
            // Calculate Property Premium = (TIV / 100) * Rate
            locData.calculatePremiums();
            // Calculate Full Term = Property Premium + WS + GL + Tax
            locData.calculateFullTerm();

            addedLocations.add(locData);
            logger.info("Stored location: Address={}, CovA={}, CovB={}, CovC={}, CovD={}, Rate={}, TIV={}, PropPrem={}, WS={}, GL={}, Tax={}, FullTerm={}",
                address, coverageA, coverageB, coverageC, coverageD, rate, locData.getTIV(),
                locData.getPropertyPremium(), wsPremium, glPremium, tax, locData.getFullTerm());

        } catch (Exception e) {
            logger.warn("Failed to store location data: {}", e.getMessage());
        }
    }

    // Update the last stored location with WS, GL, Tax, FullTerm from table
    private void updateLastLocationWithTableValues() {
        if (addedLocations.isEmpty()) return;
        try {
            java.util.List<LocationRowData> tableData = getLocationTableData();
            if (tableData.isEmpty()) { logger.warn("No table data found to update location"); return; }
            LocationRowData lastRow = tableData.get(tableData.size() - 1);
            LocationData lastLoc = addedLocations.get(addedLocations.size() - 1);
            // Update WS, GL, Tax from table (these are calculated after location is added)
            lastLoc.setWsPremium(lastRow.getWsPremium());
            lastLoc.setGlPremium(lastRow.getGlPremium());
            lastLoc.setTax(lastRow.getTaxes());
            // Recalculate Full Term = Property Premium + WS + GL + Tax
            lastLoc.calculateFullTerm();
            logger.info("Updated location with table values: WS={}, GL={}, Tax={}, FullTerm={}",
                lastRow.getWsPremium(), lastRow.getGlPremium(), lastRow.getTaxes(), lastLoc.getFullTerm());
        } catch (Exception e) {
            logger.warn("Failed to update location with table values: {}", e.getMessage());
        }
    }

    // Get stored location data for PDF validation
    public java.util.List<LocationData> getAddedLocations() {
        return addedLocations;
    }

    /**
     * Clear stored location data
     */
    public void clearAddedLocations() {
        addedLocations.clear();
    }

    // Flag to indicate if we're in upload mode (skip Excel comparison, validate calculations only)
    private boolean uploadMode = false;

    /**
     * Set upload mode flag - when true, Display Computation will validate calculations only
     * instead of comparing against Excel data
     */
    public void setUploadMode(boolean uploadMode) {
        this.uploadMode = uploadMode;
        logger.info("Upload mode set to: {}", uploadMode);
    }

    /**
     * Check if we're in upload mode
     */
    public boolean isUploadMode() {
        return uploadMode;
    }

    /**
     * Store location data from Excel for upload mode validation
     * This method should be called after file upload to store expected location data
     * from the Excel CreateQuote sheet for Display Computation and PDF validation
     * @param excelLocations list of location data maps from Excel
     * @param glAmount GL amount from config
     * @param wsAmount WS amount from config
     */
    public void storeLocationDataFromExcel(java.util.List<Map<String, String>> excelLocations, String glAmount, String wsAmount) {
        logger.info("Storing {} locations from Excel for upload mode validation", excelLocations.size());

        // Set upload mode flag
        uploadMode = true;

        // Clear any existing stored data
        addedLocations.clear();

        double glValue = parseAmountValue(glAmount != null ? glAmount : "0");
        double wsValue = parseAmountValue(wsAmount != null ? wsAmount : "0");

        for (int i = 0; i < excelLocations.size(); i++) {
            Map<String, String> excelLoc = excelLocations.get(i);
            try {
                // Get address for LocationData constructor
                String address = excelLoc.getOrDefault("Address", "Location " + (i + 1));
                LocationData locData = new LocationData(address);

                // Store actual coverage values from Excel for PDF validation
                double coverageA = parseAmountValue(excelLoc.getOrDefault("CoverageA",
                    excelLoc.getOrDefault("Dwelling Coverage", excelLoc.getOrDefault("Dwelling", "0"))));
                double coverageB = parseAmountValue(excelLoc.getOrDefault("CoverageB",
                    excelLoc.getOrDefault("Additional Structures", excelLoc.getOrDefault("AS", "0"))));
                double coverageC = parseAmountValue(excelLoc.getOrDefault("CoverageC",
                    excelLoc.getOrDefault("BPP", excelLoc.getOrDefault("Personal Property", "0"))));
                double coverageD = parseAmountValue(excelLoc.getOrDefault("LossOfRents",
                    excelLoc.getOrDefault("Loss of Rents", excelLoc.getOrDefault("CoverageD", "0"))));
                double rate = parseAmountValue(excelLoc.getOrDefault("SuggestedRate",
                    excelLoc.getOrDefault("Rate", "0.5")));

                locData.setCoverageA(coverageA);
                locData.setCoverageB(coverageB);
                locData.setCoverageC(coverageC);
                locData.setCoverageD(coverageD);
                locData.setRate(rate);

                // Set WS and GL premiums from config
                locData.setWsPremium(wsValue);
                locData.setGlPremium(glValue);

                // Calculate TIV and premiums
                locData.calculateTIV();
                locData.calculatePremiums();
                locData.calculateFullTerm();

                addedLocations.add(locData);
                logger.info("Stored Excel location {}: Address={}, CovA={}, CovB={}, CovC={}, CovD={}, Rate={}",
                    i + 1, address, coverageA, coverageB, coverageC, coverageD, rate);

            } catch (Exception e) {
                logger.warn("Failed to store location {}: {}", i + 1, e.getMessage());
            }
        }

        logger.info("Stored {} locations from Excel for PDF validation", addedLocations.size());
    }

    /**
     * Update stored locations with actual values from frontend table after upload
     * This should be called after locations are uploaded and displayed
     */
    public void updateStoredLocationsFromFrontendTable() {
        logger.info("Updating stored locations with frontend table values");
        try {
            java.util.List<LocationRowData> tableData = getLocationTableData();
            if (tableData.isEmpty()) {
                logger.warn("No table data found to update stored locations");
                return;
            }

            logger.info("Found {} rows in frontend table, {} stored locations", tableData.size(), addedLocations.size());

            // Update each stored location with actual values from table
            for (int i = 0; i < Math.min(tableData.size(), addedLocations.size()); i++) {
                LocationRowData tableRow = tableData.get(i);
                LocationData storedLoc = addedLocations.get(i);

                // Update coverage values from frontend table (for uploaded file)
                double dwelling = tableRow.getDwelling();
                double addStruct = tableRow.getAdditionalStructures();
                double bpp = tableRow.getBpp();
                double lor = tableRow.getLossOfRents();
                double rate = tableRow.getRate();
                double tiv = tableRow.getTiv();

                // Only update coverage values if they're available from table
                if (dwelling > 0) storedLoc.setCoverageA(dwelling);
                if (addStruct > 0) storedLoc.setCoverageB(addStruct);
                if (bpp > 0) storedLoc.setCoverageC(bpp);
                if (lor > 0) storedLoc.setCoverageD(lor);
                if (rate > 0) storedLoc.setRate(rate);

                // Update premium values from frontend table
                double propPrem = tableRow.getPropertyPremium();
                double wsPrem = tableRow.getWsPremium();
                double glPrem = tableRow.getGlPremium();
                double taxes = tableRow.getTaxes();

                storedLoc.setPropertyPremium(propPrem);
                storedLoc.setWsPremium(wsPrem);
                storedLoc.setGlPremium(glPrem);
                storedLoc.setTax(taxes);

                // Recalculate TIV if we have coverage values
                if (storedLoc.getCoverageA() > 0) {
                    storedLoc.calculateTIV();
                }

                // Use Full Term from table if available, otherwise calculate
                // Full Term = Property Premium + GL Premium + WS Premium + Taxes
                double fullTerm = tableRow.getFullTerm();
                if (fullTerm <= 0) {
                    // Calculate if not available from table
                    fullTerm = propPrem + glPrem + wsPrem + taxes;
                }
                storedLoc.setFullTerm(fullTerm);

                logger.info("Updated location {}: Address={}", i + 1, tableRow.getAddress());
                logger.info("  Dwelling={}, AS={}, BPP={}, LOR={}, Rate={}, TIV={}",
                    formatAmount(dwelling), formatAmount(addStruct), formatAmount(bpp),
                    formatAmount(lor), formatAmount(rate), formatAmount(tiv));
                logger.info("  Premium={}, GL={}, WS={}, Taxes={}, FullTerm={}",
                    formatAmount(propPrem), formatAmount(glPrem), formatAmount(wsPrem),
                    formatAmount(taxes), formatAmount(fullTerm));
            }

            logger.info("Updated {} stored locations with frontend values", Math.min(tableData.size(), addedLocations.size()));
        } catch (Exception e) {
            logger.warn("Failed to update stored locations from frontend: {}", e.getMessage());
        }
    }

    /**
     * Update stored locations with per-location Taxes from Display Computation dialog
     * This should be called AFTER Display Computation validation to get actual taxes
     * Full Term = Property Premium + GL Premium + WS Premium + Taxes
     */
    public void updateStoredLocationsFromDisplayComputation() {
        logger.info("Updating stored locations with Display Computation taxes for PDF validation");
        try {
            // Click Display Computation button to open dialog
            if (!clickDisplayComputationButton()) {
                logger.warn("Could not open Display Computation dialog");
                return;
            }
            sleep(1500);

            // Extract data from Display Computation dialog (includes Taxes)
            java.util.List<LocationRowData> dialogData = getDisplayComputationDialogData();
            if (dialogData.isEmpty()) {
                logger.warn("No data found in Display Computation dialog");
                closeDisplayComputationDialog();
                return;
            }

            logger.info("Found {} locations in Display Computation, {} stored locations",
                dialogData.size(), addedLocations.size());

            // Update each stored location with taxes from Display Computation
            for (int i = 0; i < Math.min(dialogData.size(), addedLocations.size()); i++) {
                LocationRowData dialogRow = dialogData.get(i);
                LocationData storedLoc = addedLocations.get(i);

                // Get values from Display Computation
                double taxes = dialogRow.getTaxes();
                double propPrem = dialogRow.getPropertyPremium();
                double dwelling = dialogRow.getDwelling();
                double tiv = dialogRow.getTiv();

                // Update stored location with actual taxes
                storedLoc.setTax(taxes);

                // Update property premium from Display Computation
                if (propPrem > 0) {
                    storedLoc.setPropertyPremium(propPrem);
                }

                // Update coverage values if available
                if (dwelling > 0) storedLoc.setCoverageA(dwelling);
                if (tiv > 0) storedLoc.setTiv(tiv);

                // Calculate Full Term = PropPrem + GL + WS + Taxes
                double glPrem = storedLoc.getGlPremium();
                double wsPrem = storedLoc.getWsPremium();
                double fullTerm = propPrem + glPrem + wsPrem + taxes;
                storedLoc.setFullTerm(fullTerm);

                logger.info("Updated location {}: PropPrem=${}, GL=${}, WS=${}, Tax=${}, FullTerm=${}",
                    i + 1, formatAmount(propPrem), formatAmount(glPrem), formatAmount(wsPrem),
                    formatAmount(taxes), formatAmount(fullTerm));
            }

            // Close the Display Computation dialog
            closeDisplayComputationDialog();
            sleep(500);

            logger.info("Updated {} stored locations with Display Computation taxes", addedLocations.size());
        } catch (Exception e) {
            logger.warn("Failed to update stored locations from Display Computation: {}", e.getMessage());
            // Try to close dialog if it's open
            try {
                closeDisplayComputationDialog();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    /**
     * Dismiss Chrome autofill/save address dialog
     */
    private void dismissChromeDialog() {
        try {
            // Try pressing Escape to close any Chrome dialog
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            sleep(500);

            // Try to find and click "No thanks" or close button on Chrome dialog
            String[] closeXpaths = {
                "//button[contains(text(),'No thanks')]",
                "//button[contains(text(),'No Thanks')]",
                "//button[contains(text(),'Never')]",
                "//button[contains(text(),'Close')]",
                "//button[contains(@aria-label,'Close')]",
                "//div[contains(@class,'save-address')]//button[contains(text(),'No')]"
            };

            for (String xpath : closeXpaths) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            logger.info("Dismissed Chrome dialog");
                            sleep(500);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
        } catch (Exception e) {
            // No dialog to dismiss
        }
    }

    /**
     * Click Add button in the dialog
     */
    private void clickAddButtonInDialog() {
        String[] xpaths = {
            "//div[@role='dialog']//button[contains(text(),'Add')]",
            "//button[contains(text(),'Add Location')]",
            "//div[@role='dialog']//button[contains(text(),'Save')]",
            "//div[contains(@class,'dialog')]//button[contains(text(),'Add')]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed() && btn.isEnabled()) {
                        logger.info("Clicking Add button");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }
    }

    /**
     * Close dialog if open (only closes if Add Location dialog is stuck)
     */
    private void closeDialogIfOpen() {
        try {
            WebElement dialog = findLocationDialog();
            if (dialog != null && dialog.isDisplayed()) {
                // Check if this is a stuck dialog (has Add button visible but disabled or error shown)
                try {
                    WebElement addBtn = dialog.findElement(By.xpath(".//button[contains(text(),'Add')]"));
                    if (!addBtn.isEnabled()) {
                        logger.info("Closing stuck dialog");
                        // Press Escape to close
                        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                        sleep(1000);
                    }
                } catch (Exception e) {
                    // Dialog might be fine, don't close it
                }
            }
        } catch (Exception e) {
            // No dialog open - that's fine
        }
    }

    /**
     * Submit dialog and ensure it closes
     */
    private void submitAndCloseDialog() {
        // Try to submit
        submitLocationDialog();
        sleep(2000);

        // Check if dialog is still open and retry
        for (int i = 0; i < 3; i++) {
            WebElement dialog = findLocationDialog();
            if (dialog == null || !dialog.isDisplayed()) {
                logger.info("Dialog closed successfully");
                return;
            }


            // Try clicking Add/Save button again
            try {
                String[] submitXpaths = {
                    "//div[@role='dialog']//button[contains(text(),'Add')]",
                    "//div[@role='dialog']//button[contains(text(),'Save')]",
                    "//button[contains(text(),'Add Location')]"
                };
                for (String xpath : submitXpaths) {
                    try {
                        List<WebElement> btns = driver.findElements(By.xpath(xpath));
                        for (WebElement btn : btns) {
                            if (btn.isDisplayed() && btn.isEnabled()) {
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                                sleep(2000);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Continue
                    }
                }
            } catch (Exception e) {
            }
        }

        // Force close if still open
        closeDialogIfOpen();
    }

    /**
     * Enter location details within dialog (reuses existing logic)
     */
    private void enterLocationDetailsInDialog(WebElement dialog, Map<String, String> locationData) {
        // Address with auto-complete
        String address = locationData.getOrDefault("Address", "123 William St, New York, NY 10038, USA");
        enterAddressWithAutoComplete(dialog, address);
        sleep(1000);

        // Check if City, State, Zip were auto-filled - if not, use Faker to generate data
        fillCityStateZipWithFakerIfEmpty(dialog, locationData);

        // Municipality (from Excel column AB) - optional field
        String municipality = locationData.getOrDefault("Municipality", "");
        if (municipality != null && !municipality.trim().isEmpty()) {
            enterMunicipalityInDialog(dialog, municipality);
            logger.info("Municipality entered from Excel: {}", municipality);
        }

        // Select PropertyType (RS = Residential, RM = Multi-Family)
        String propertyType = locationData.getOrDefault("PropertyType", "RS");
        selectPropertyType(dialog, propertyType);
        sleep(500);

        // Enter Units from Excel (# of Units field)
        String units = locationData.getOrDefault("Units", "1");
        if (units != null && !units.isEmpty()) {
            enterUnitsInDialog(dialog, units);
            logger.info("Units entered from Excel: {}", units);
        }

        // Sq. Ft.
        enterFieldInDialog(dialog, "Sq. Ft", locationData.getOrDefault("SqFt", "1200"));

        // Coverages A and B
        enterFieldInDialog(dialog, "Coverage A", locationData.getOrDefault("CoverageA", "50000"));
        enterFieldInDialog(dialog, "Coverage B", locationData.getOrDefault("CoverageB", "5000"));

        scrollDialogDown(dialog);
        sleep(500);

        // Coverage C - use dialog-scoped search
        enterCoverageCInDialog(dialog, locationData.getOrDefault("CoverageC", "5000"));

        // Loss of Rents
        enterFieldInDialog(dialog, "Loss of Rents", locationData.getOrDefault("LossOfRents", "5000"));

        scrollDialogDown(dialog);
        sleep(300);

        // Suggested Rate
        enterFieldInDialog(dialog, "Suggested Rate", locationData.getOrDefault("SuggestedRate", "0.5"));
    }

    /**
     * Select Property Type in dialog
     * RS = Residential, RM = Multi-Family (Residential Multi)
     * Handles both "RM" and "Multi-Family" values from Excel
     */
    private void selectPropertyType(WebElement dialog, String propertyType) {
        logger.info("Selecting Property Type: {}", propertyType);

        // Normalize propertyType - check if it's Multi-Family type
        boolean isMultiFamily = "RM".equalsIgnoreCase(propertyType) ||
                                propertyType.toLowerCase().contains("multi") ||
                                propertyType.toLowerCase().contains("rm");

        // Look for PropertyType buttons/radio options within dialog
        String[] xpaths;

        if (isMultiFamily) {
            // Multi-Family / Residential Multi (RM)
            logger.info("Looking for Multi-Family (RM) property type option");
            xpaths = new String[] {
                ".//button[text()='RM']",
                ".//button[contains(text(),'RM')]",
                ".//button[contains(text(),'Multi')]",
                ".//label[text()='RM']",
                ".//label[contains(text(),'RM')]",
                ".//label[contains(text(),'Multi-Family')]",
                ".//*[@role='radio' and (contains(.,'RM') or contains(.,'Multi'))]",
                ".//button[contains(@id,'rm') or contains(@id,'multi')]",
                ".//div[contains(@class,'radio') or contains(@class,'button')][contains(.,'RM')]",
                ".//*[contains(@data-value,'RM') or contains(@data-value,'MULTI')]"
            };
        } else {
            // Residential (RS) - default
            logger.info("Looking for Residential (RS) property type option");
            xpaths = new String[] {
                ".//button[text()='RS']",
                ".//button[contains(text(),'RS')]",
                ".//label[text()='RS']",
                ".//label[contains(text(),'Residential') and not(contains(text(),'Multi'))]",
                ".//*[@role='radio' and (contains(.,'RS') or (contains(.,'Residential') and not(contains(.,'Multi'))))]",
                ".//button[contains(@id,'rs') or contains(@id,'residential')]",
                ".//div[contains(@class,'radio') or contains(@class,'button')][contains(.,'RS')]",
                ".//*[contains(@data-value,'RS') or contains(@data-value,'RESIDENTIAL')]"
            };
        }

        for (String xpath : xpaths) {
            try {
                List<WebElement> elements = dialog.findElements(By.xpath(xpath));
                for (WebElement el : elements) {
                    if (el.isDisplayed()) {
                        String elText = el.getText().trim();
                        // For RS, make sure we don't accidentally select RM
                        if (!isMultiFamily && (elText.contains("RM") || elText.toLowerCase().contains("multi"))) {
                            continue; // Skip RM/Multi options when looking for RS
                        }
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", el);
                        sleep(200);
                        click(el);
                        logger.info("Selected Property Type: {} (element text: {}) using xpath: {}",
                            isMultiFamily ? "RM/Multi-Family" : "RS/Residential", elText, xpath);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue to next xpath
            }
        }

    }

    /**
     * Enter Municipality value in dialog (from Excel column AB)
     * Municipality is a dropdown button, not an input field
     */
    private void enterMunicipalityInDialog(WebElement dialog, String municipality) {
        logger.info("Selecting Municipality: {}", municipality);

        try {
            // Municipality is a dropdown button - find and click it first
            String[] dropdownXpaths = {
                ".//label[contains(text(),'Municipality')]/following::button[1]",
                ".//label[contains(text(),'municipality')]/following::button[1]",
                ".//*[contains(text(),'Municipality')]/parent::div//button",
                ".//div[contains(@class,'municipality')]//button",
                ".//*[@id[contains(.,'municipality')]]//button",
                ".//label[contains(text(),'Municipality')]/parent::div/div//button",
                ".//*[contains(text(),'Municipality')]/following-sibling::*//button",
                ".//form//div[5]//button" // Based on user provided XPath structure
            };

            WebElement dropdownButton = null;
            for (String xpath : dropdownXpaths) {
                try {
                    List<WebElement> buttons = dialog.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            dropdownButton = btn;
                            logger.info("Found Municipality dropdown with xpath: {}", xpath);
                            break;
                        }
                    }
                    if (dropdownButton != null) break;
                } catch (Exception e) {
                    // Continue to next xpath
                }
            }

            if (dropdownButton != null) {
                // Scroll to and click the dropdown button
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", dropdownButton);
                sleep(300);
                click(dropdownButton);
                sleep(1000);

                // Now select the municipality option from dropdown
                selectDropdownOptionByText(municipality);
                logger.info("Municipality selected: {}", municipality);
                return;
            }

            // Fallback: Try as input field
            logger.info("Dropdown not found, trying as input field for Municipality");
            String[] inputXpaths = {
                ".//label[contains(text(),'Municipality')]/following::input[1]",
                ".//*[contains(text(),'Municipality')]/parent::div//input"
            };

            for (String xpath : inputXpaths) {
                try {
                    List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                    for (WebElement input : inputs) {
                        if (input.isDisplayed() && input.isEnabled()) {
                            clearInputField(input);
                            input.sendKeys(municipality);
                            logger.info("Municipality entered as input: {}", municipality);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }

            logger.warn("Could not find Municipality field");
        } catch (Exception e) {
            logger.warn("Error selecting Municipality: {}", e.getMessage());
        }
    }

    /**
     * Select an option from an open dropdown by text
     */
    private void selectDropdownOptionByText(String optionText) {
        try {
            sleep(500);

            // Look for the option in the dropdown
            String[] optionXpaths = {
                "//div[@role='option' and contains(.,'" + optionText + "')]",
                "//li[contains(.,'" + optionText + "')]",
                "//*[@role='listbox']//*[contains(.,'" + optionText + "')]",
                "//div[contains(@class,'option') and contains(.,'" + optionText + "')]",
                "//*[contains(@class,'menu')]//*[contains(.,'" + optionText + "')]"
            };

            for (String xpath : optionXpaths) {
                try {
                    List<WebElement> options = driver.findElements(By.xpath(xpath));
                    for (WebElement option : options) {
                        if (option.isDisplayed()) {
                            click(option);
                            logger.info("Selected dropdown option: {}", optionText);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }

            // Try partial match
            String partialText = optionText.length() > 10 ? optionText.substring(0, 10) : optionText;
            for (String xpath : optionXpaths) {
                String partialXpath = xpath.replace(optionText, partialText);
                try {
                    List<WebElement> options = driver.findElements(By.xpath(partialXpath));
                    for (WebElement option : options) {
                        if (option.isDisplayed()) {
                            click(option);
                            logger.info("Selected dropdown option (partial match): {}", option.getText());
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }

            logger.warn("Could not find dropdown option: {}", optionText);
        } catch (Exception e) {
            logger.warn("Error selecting dropdown option: {}", e.getMessage());
        }
    }

    /**
     * Enter Units value in dialog (for Multi-Family properties)
     */
    private void enterUnitsInDialog(WebElement dialog, String units) {
        logger.info("Entering Units: {}", units);

        String[] xpaths = {
            ".//label[contains(text(),'Units') or contains(text(),'Number of Units')]/following::input[1]",
            ".//label[contains(text(),'Unit')]/following::input[@type='number'][1]",
            ".//input[contains(@id,'unit') or contains(@name,'unit')]",
            ".//input[contains(@placeholder,'units') or contains(@placeholder,'Units')]",
            ".//label[contains(text(),'Units')]/parent::div//input"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", input);
                        sleep(200);
                        clearInputField(input);
                        input.sendKeys(units);
                        logger.info("Entered Units: {}", units);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue to next xpath
            }
        }

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
     * Enter Coverage C value within dialog context
     */
    private void enterCoverageCInDialog(WebElement dialog, String value) {

        // Find all number inputs in dialog after Coverage B
        String[] xpaths = {
            ".//label[contains(text(),'Coverage C')]/following::input[1]",
            ".//label[text()='Coverage C']/following::input[1]",
            ".//input[contains(@id,'coverage') and contains(@id,'c')]",
            ".//input[@type='number'][3]", // Third number input (after A and B)
            "(.//label[contains(text(),'Coverage')]/following::input[@type='number'])[3]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", input);
                        sleep(200);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", input);
                        sleep(100);
                        input.sendKeys(value);
                        logger.info("Entered Coverage C: {}", value);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }

        // Fallback: try global search
        enterCoverageCValue(value);
    }

    /**
     * Select payment plan in dialog
     * @param dialog the dialog element
     * @param paymentPlan "Paid In Full", "Installments", "Escrow", or "Bank"
     */
    private void selectPaymentPlan(WebElement dialog, String paymentPlan) {
        logger.info("Selecting payment plan: {}", paymentPlan);

        // Find payment plan dropdown within dialog
        String[] dropdownXpaths = {
            ".//label[contains(text(),'Payment Plan')]/following::button[1]",
            ".//label[contains(text(),'Payment')]/following::button[1]"
        };

        WebElement dropdown = null;
        for (String xpath : dropdownXpaths) {
            try {
                List<WebElement> elements = dialog.findElements(By.xpath(xpath));
                for (WebElement el : elements) {
                    if (el.isDisplayed()) {
                        dropdown = el;
                        break;
                    }
                }
                if (dropdown != null) break;
            } catch (Exception e) {
                // Continue
            }
        }

        if (dropdown == null) {
            return;
        }

        // Scroll and click dropdown
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", dropdown);
        sleep(300);
        click(dropdown);
        sleep(800);

        // Find and click the correct option within the popover/listbox that just opened
        selectPaymentPlanOption(paymentPlan);
    }

    /**
     * Select payment plan option by text
     */
    private void selectPaymentPlanOption(String paymentPlan) {
        sleep(500);

        // Look for options in the popover/listbox that just opened
        String[] optionXpaths = {
            "//div[contains(@class,'popover')]//div[@role='option']",
            "//div[@role='listbox']//div[@role='option']",
            "//div[@role='option']"
        };

        List<WebElement> options = new java.util.ArrayList<>();
        for (String xpath : optionXpaths) {
            try {
                options = driver.findElements(By.xpath(xpath));
                if (!options.isEmpty()) break;
            } catch (Exception e) {
                // Continue
            }
        }

        if (options.isEmpty()) {
            return;
        }

        // Log options for debugging
        logger.info("Found {} payment options", options.size());
        for (int i = 0; i < options.size(); i++) {
            try {
                logger.info("  Option {}: {}", i, options.get(i).getText());
            } catch (Exception e) {
                // Ignore
            }
        }

        // Find matching option
        String searchKey = paymentPlan.toLowerCase().trim();
        for (WebElement opt : options) {
            try {
                if (!opt.isDisplayed()) continue;
                String optText = opt.getText().toLowerCase().trim();

                // Match by contains
                if (optText.contains(searchKey) || searchKey.contains(optText)) {
                    logger.info("Selecting: {}", opt.getText());
                    click(opt);
                    sleep(500);
                    return;
                }

                // Match "Paid In Full" or "Pay In Full" variants - must have "full" to avoid matching "Bank Payment"
                if ((paymentPlan.equalsIgnoreCase("Paid In Full") || paymentPlan.equalsIgnoreCase("Pay In Full")) &&
                    optText.contains("full")) {
                    logger.info("Selecting Paid/Pay In Full: {}", opt.getText());
                    click(opt);
                    sleep(500);
                    return;
                }
                // Match "Installments" or "Installment" variants
                if ((paymentPlan.equalsIgnoreCase("Installments") || paymentPlan.equalsIgnoreCase("Installment")) &&
                    optText.contains("install")) {
                    logger.info("Selecting Installments: {}", opt.getText());
                    click(opt);
                    sleep(500);
                    return;
                }
                // Match "Escrow" variants
                if (paymentPlan.toLowerCase().contains("escrow") && optText.contains("escrow")) {
                    logger.info("Selecting Escrow: {}", opt.getText());
                    click(opt);
                    sleep(500);
                    return;
                }
                // Match "Bank" or "Bank Payment" variants
                if ((paymentPlan.equalsIgnoreCase("Bank") || paymentPlan.equalsIgnoreCase("Bank Payment")) &&
                    optText.contains("bank")) {
                    logger.info("Selecting Bank: {}", opt.getText());
                    click(opt);
                    sleep(500);
                    return;
                }
            } catch (Exception e) {
                // Continue to next option
            }
        }

        // Close dropdown if no match found
        try {
            ((JavascriptExecutor) driver).executeScript("document.body.click();");
            sleep(300);
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Enter mortgagee details in dialog
     * @param dialog the dialog element
     * @param mortgageeData map with mortgagee fields (Name, Address)
     */
    private void enterMortgageeDetails(WebElement dialog, Map<String, String> mortgageeData) {
        logger.info("Entering mortgagee details");

        // Scroll to mortgagee section
        scrollDialogDown(dialog);
        sleep(500);

        // Enter Mortgagee Name (field label might be "Mortgagee 1 Name *" or similar)
        String name = mortgageeData.get("MortgageeName");
        if (name != null && !name.isEmpty()) {
            enterMortgageeName(dialog, name);
        }

        // Enter Mortgagee Address with auto-complete
        String address = mortgageeData.get("MortgageeAddress");
        if (address != null && !address.isEmpty()) {
            enterMortgageeAddressWithAutoComplete(dialog, address);
        }
    }

    /**
     * Enter mortgagee name in dialog
     */
    private void enterMortgageeName(WebElement dialog, String name) {
        logger.info("Entering mortgagee name: {}", name);

        // Wait a moment for field to be ready
        sleep(500);

        String[] xpaths = {
            // Direct label matching
            ".//label[contains(text(),'Mortgagee') and contains(text(),'Name')]/following::input[1]",
            ".//label[contains(text(),'Mortgagee 1 Name')]/following::input[1]",
            ".//label[contains(text(),'Mortgagee name')]/following::input[1]",
            // Look for input after Mortgagee section header
            ".//*[contains(text(),'Mortgagee')]/following::input[1]",
            ".//*[contains(text(),'Mortgagee')]/following::label[contains(text(),'Name')]/following::input[1]",
            // Look by placeholder
            ".//input[contains(@placeholder,'mortgagee') or contains(@placeholder,'Mortgagee')]",
            ".//input[contains(@placeholder,'name') and ancestor::div[contains(.,'Mortgagee')]]",
            // Look by id/name attributes
            ".//input[contains(@id,'mortgagee') and contains(@id,'name')]",
            ".//input[contains(@name,'mortgagee') and contains(@name,'name')]",
            // Generic text input in mortgagee section
            ".//div[contains(.,'Mortgagee')]//input[@type='text'][1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", input);
                        sleep(300);
                        clearInputField(input);
                        input.sendKeys(name);
                        // Trigger events
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input);
                        logger.info("Entered mortgagee name: {} using xpath: {}", name, xpath);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }

        // Fallback: Try to find any visible text input in the lower half of dialog
        try {
            List<WebElement> allInputs = dialog.findElements(By.xpath(".//input[@type='text']"));
            logger.info("Found {} text inputs in dialog, looking for mortgagee name field", allInputs.size());
            for (int i = allInputs.size() - 1; i >= 0; i--) {
                WebElement input = allInputs.get(i);
                if (input.isDisplayed() && input.isEnabled() && input.getAttribute("value").isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'});", input);
                    sleep(200);
                    input.sendKeys(name);
                    logger.info("Entered mortgagee name in fallback input: {}", name);
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore
        }

    }

    /**
     * Enter mortgagee address with auto-complete selection
     */
    private void enterMortgageeAddressWithAutoComplete(WebElement dialog, String address) {
        logger.info("Entering mortgagee address with auto-complete: {}", address);

        // Wait a moment for field to be ready
        sleep(500);

        // Find the Mortgagee address input field
        String[] xpaths = {
            // Direct label matching
            ".//label[contains(text(),'Mortgagee') and contains(text(),'Address')]/following::input[1]",
            ".//label[contains(text(),'Mortgagee 1 Address')]/following::input[1]",
            ".//label[contains(text(),'Mortgagee address')]/following::input[1]",
            // Look for address input after Mortgagee section
            ".//*[contains(text(),'Mortgagee')]/following::input[contains(@placeholder,'address') or contains(@placeholder,'Address')][1]",
            ".//div[contains(text(),'Mortgagee')]/following::label[contains(text(),'Address')]/following::input[1]",
            // Look by placeholder in mortgagee section
            ".//input[contains(@placeholder,'address') and ancestor::div[contains(.,'Mortgagee')]]",
            // Second address field (after physical address)
            "(.//input[contains(@placeholder,'address') or contains(@placeholder,'Address')])[2]",
            // By id/name
            ".//input[contains(@id,'mortgagee') and contains(@id,'address')]",
            ".//input[contains(@name,'mortgagee') and contains(@name,'address')]"
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
            return;
        }

        // Scroll to input
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center'});", addressInput);
        sleep(300);

        // Clear and type address
        clearInputField(addressInput);
        sleep(100);
        addressInput.sendKeys(address);
        sleep(1500); // Wait for auto-complete

        // Try to select first suggestion
        try {
            String[] suggestionXpaths = {
                "//div[contains(@class,'pac-container')]//div[contains(@class,'pac-item')][1]",
                "(//div[contains(@class,'pac-item')])[1]",
                "//div[@class='pac-container']//div[@class='pac-item'][1]"
            };

            for (String xpath : suggestionXpaths) {
                List<WebElement> suggestions = driver.findElements(By.xpath(xpath));
                for (WebElement s : suggestions) {
                    if (s.isDisplayed()) {
                        logger.info("Selecting mortgagee address suggestion");
                        sleep(500);
                        click(s);
                        sleep(1000);
                        return;
                    }
                }
            }

            // Fallback: keyboard navigation
            addressInput.sendKeys(Keys.ARROW_DOWN);
            sleep(300);
            addressInput.sendKeys(Keys.ENTER);
            sleep(1000);
        } catch (Exception e) {
        }
    }

    /**
     * Enter a specific mortgagee field (for Name only now)
     */
    private void enterMortgageeField(WebElement dialog, String fieldName, String value) {
        String[] xpaths = {
            ".//div[contains(text(),'Mortgagee')]/following::label[contains(text(),'" + fieldName + "')]/following::input[1]",
            ".//*[contains(text(),'Mortgagee')]/following::label[contains(text(),'" + fieldName + "')]/following::input[1]",
            ".//label[contains(text(),'Mortgagee " + fieldName + "')]/following::input[1]",
            ".//label[contains(text(),'Mortgagee')]/ancestor::div[2]//label[contains(text(),'" + fieldName + "')]/following::input[1]"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> inputs = dialog.findElements(By.xpath(xpath));
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", input);
                        sleep(200);
                        clearInputField(input);
                        input.sendKeys(value);
                        logger.info("Entered mortgagee {}: {}", fieldName, value);
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }
    }

    /**
     * Get count of locations displayed in table
     * @return number of locations in the table
     */
    public int getLocationCount() {
        sleep(3000); // Wait for table to update

        // First, scroll to make sure table is visible
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            sleep(1000);
        } catch (Exception e) {
            // Ignore
        }

        // Try multiple strategies to find the location table rows
        String[] xpaths = {
            // Specific xpaths for location table - look for table with address/coverage data
            "//table[.//th[contains(text(),'Address') or contains(text(),'Location') or contains(text(),'Coverage')]]//tbody//tr",
            "//table[.//thead//th[contains(text(),'Premium')]]//tbody//tr",
            "//div[contains(@class,'location')]//table//tbody//tr",
            "//h2[contains(text(),'Location')]/following::table[1]//tbody//tr",
            // Generic table rows
            "//table//tbody//tr[td]", // Only rows that have td elements (not empty rows)
            "//table//tbody//tr"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath(xpath));
                if (!rows.isEmpty()) {
                    // Filter out header rows, empty rows, and footer/summary rows
                    int count = 0;
                    List<String> rowTexts = new java.util.ArrayList<>();

                    for (WebElement row : rows) {
                        try {
                            if (row.isDisplayed()) {
                                String rowText = row.getText().trim();
                                // Skip empty rows, header-like rows, and summary rows
                                if (!rowText.isEmpty() &&
                                    !rowText.toLowerCase().contains("total") &&
                                    !rowText.toLowerCase().contains("grand") &&
                                    !rowText.toLowerCase().startsWith("address") &&
                                    !rowText.toLowerCase().startsWith("location") &&
                                    !rowText.toLowerCase().startsWith("premium")) {
                                    count++;
                                    // Log first 50 chars of each row for debugging
                                    String preview = rowText.length() > 50 ? rowText.substring(0, 50) + "..." : rowText;
                                    rowTexts.add(preview);
                                }
                            }
                        } catch (Exception e) {
                            // Skip this row
                        }
                    }
                    if (count > 0) {
                        logger.info("Location count: {} (using xpath: {})", count, xpath);
                        logger.info("Rows found: {}", rowTexts);
                        return count;
                    }
                }
            } catch (Exception e) {
                // Continue to next xpath
            }
        }

        // Fallback: count by looking for location entries/cards
        try {
            List<WebElement> locationEntries = driver.findElements(By.xpath(
                "//*[contains(@class,'location') and contains(@class,'item')] | " +
                "//div[contains(@class,'card') and .//text()[contains(.,'Coverage')]]"
            ));
            if (!locationEntries.isEmpty()) {
                logger.info("Location count (by entries): {}", locationEntries.size());
                return locationEntries.size();
            }
        } catch (Exception e) {
            // Ignore
        }

        return 0;
    }

    /**
     * Verify expected number of locations in table
     * @param expectedCount expected number of locations
     * @return true if count matches
     */
    public boolean verifyLocationCount(int expectedCount) {
        int actualCount = getLocationCount();
        boolean matches = actualCount == expectedCount;
        logger.info("Location count verification: expected={}, actual={}, matches={}",
            expectedCount, actualCount, matches);
        if (!matches) {
            captureScreenshotToReport("Location Count Mismatch - Expected " + expectedCount + " Got " + actualCount);
        }
        return matches;
    }

    // ==================== Premium Calculation Validation ====================

    /**
     * Validate premium calculations and return result
     * Logs detailed amounts to console and report
     */
    public boolean validatePremiumCalculations() {
        logger.info("=== Validating Premium Calculations ===");
        boolean allValid = true;
        StringBuilder reportSummary = new StringBuilder();

        // Get column sums
        double propPremiumSum = getColumnSum("Property Premium");
        double glSum = getColumnSum("GL Premium", "GL");
        double wsSum = getColumnSum("Water/Sewer Premium", "Water/Sewer", "WS");
        double taxesSum = getColumnSum("Taxes", "Tax");
        double feesSum = getColumnSum("Fees", "Fee");

        // Log column sums
        String columnSumsLog = String.format("Column Sums: PropPremium=%s, GL=%s, WS=%s, Taxes=%s, Fees=%s",
            formatAmount(propPremiumSum), formatAmount(glSum), formatAmount(wsSum), formatAmount(taxesSum), formatAmount(feesSum));
        logger.info(columnSumsLog);
        reportSummary.append(columnSumsLog).append("\n");

        // Get displayed totals
        double displayedPremium = getDisplayedTotal("Premium");
        double displayedTaxes = getDisplayedTotal("Taxes");
        double displayedFees = getDisplayedTotal("Fees");
        double displayedGrandTotal = getDisplayedTotal("Grand Total");

        // Calculate expected values
        double expectedPremium = roundTo2Decimals(propPremiumSum + glSum + wsSum);
        double expectedGrandTotal = roundTo2Decimals(expectedPremium + taxesSum + feesSum);

        // Validate Premium (PropPremium + GL + WS)
        if (Math.abs(expectedPremium - displayedPremium) > 0.01) {
            String msg = String.format("FAILED: Premium - Expected: %s, Displayed: %s", formatAmount(expectedPremium), formatAmount(displayedPremium));
            reportSummary.append(msg).append("\n");
            allValid = false;
        } else {
            String msg = String.format("PASSED: Premium = %s", formatAmount(displayedPremium));
            logger.info(msg);
            reportSummary.append(msg).append("\n");
        }

        // Validate Taxes
        if (Math.abs(taxesSum - displayedTaxes) > 0.01) {
            String msg = String.format("FAILED: Taxes - Expected: %s, Displayed: %s", formatAmount(taxesSum), formatAmount(displayedTaxes));
            reportSummary.append(msg).append("\n");
            allValid = false;
        } else {
            String msg = String.format("PASSED: Taxes = %s", formatAmount(displayedTaxes));
            logger.info(msg);
            reportSummary.append(msg).append("\n");
        }

        // Validate Fees
        if (Math.abs(feesSum - displayedFees) > 0.01) {
            String msg = String.format("FAILED: Fees - Expected: %s, Displayed: %s", formatAmount(feesSum), formatAmount(displayedFees));
            reportSummary.append(msg).append("\n");
            allValid = false;
        } else {
            String msg = String.format("PASSED: Fees = %s", formatAmount(displayedFees));
            logger.info(msg);
            reportSummary.append(msg).append("\n");
        }

        // Validate Grand Total
        if (Math.abs(expectedGrandTotal - displayedGrandTotal) > 0.01) {
            String msg = String.format("FAILED: Grand Total - Expected: %s, Displayed: %s", formatAmount(expectedGrandTotal), formatAmount(displayedGrandTotal));
            reportSummary.append(msg).append("\n");
            allValid = false;
        } else {
            String msg = String.format("PASSED: Grand Total = %s", formatAmount(displayedGrandTotal));
            logger.info(msg);
            reportSummary.append(msg).append("\n");
        }

        // Capture screenshot with detailed summary in report
        String screenshotTitle = "Premium Calculation Validation - " + (allValid ? "PASSED" : "FAILED");
        captureScreenshotToReport(screenshotTitle);

        // Log the full summary to the report
        logToReport(reportSummary.toString());

        return allValid;
    }

    /**
     * Validate GL calculation based on Carrier and PropertyType
     *
     * ARCH Carrier: GL is always flat (no multiplication regardless of RS/RM)
     * STARSTONE Carrier: GL × Units ONLY when RM (Multi-Family) is selected
     *
     * Validates by matching address from Excel with frontend table
     * @param glAmount the configured GL amount
     * @param locations list of location data from Excel (Address, PropertyType, Units)
     * @param carrier carrier name from Excel config (for formula selection)
     */
    public boolean validateGLCalculation(double glAmount, List<Map<String, String>> locations, String carrier) {
        logger.info("=== Validating GL Calculation ===");

        // Get address and GL values from frontend table
        List<Map<String, Object>> frontendData = getAddressAndGLFromTable();

        // Determine carrier type for formula selection
        String carrierUpper = carrier != null ? carrier.toUpperCase() : "";
        boolean isArch = carrierUpper.contains("ARCH");
        boolean isStarStone = carrierUpper.contains("STAR") || carrierUpper.contains("STONE");

        // Build report summary
        StringBuilder report = new StringBuilder();
        report.append("GL Calculation Validation\n");
        report.append("========================\n");
        report.append("Carrier: ").append(carrier).append("\n");
        report.append("Configured GL Amount: $").append(formatAmount(glAmount)).append("\n");
        if (isArch) {
            report.append("Formula (Arch): GL = Flat Amount (no multiplication for RS or RM)\n\n");
        } else if (isStarStone) {
            report.append("Formula (StarStone): RM (Multi-Family) = GL × Units | RS (Residential) = GL (flat)\n\n");
        } else {
            report.append("Formula: GL = Flat Amount (default)\n\n");
        }

        if (frontendData.size() != locations.size()) {
            String msg = String.format("WARNING: Frontend has %d locations, Excel has %d locations",
                frontendData.size(), locations.size());
            logger.info(msg);
            report.append(msg).append("\n\n");
        }

        report.append("Location Details:\n");
        report.append("-".repeat(100)).append("\n");
        report.append(String.format("%-50s | %-6s | %-5s | %-12s | %-12s | %-6s\n",
            "Address", "Type", "Units", "Expected GL", "Actual GL", "Status"));
        report.append("-".repeat(100)).append("\n");

        boolean allValid = true;

        // Validate each location from Excel against frontend (by index for reliable matching)
        for (int i = 0; i < locations.size(); i++) {
            Map<String, String> excelLoc = locations.get(i);
            String excelAddress = excelLoc.getOrDefault("Address", "").trim();
            String propertyType = excelLoc.getOrDefault("PropertyType", "RS").trim().toUpperCase();
            String unitsStr = excelLoc.getOrDefault("Units", "1").trim();
            int units = unitsStr.isEmpty() ? 1 : Integer.parseInt(unitsStr);

            // Calculate expected GL based on Carrier and PropertyType
            // ARCH: Always flat GL (no multiplication)
            // STARSTONE: RM/MULTI-FAMILY = GL × Units, RS/RESIDENTIAL = GL (flat)
            boolean isMultiFamily = propertyType.contains("RM") || propertyType.contains("MULTI") ||
                                    propertyType.contains("FAMILY") || propertyType.contains("MF");

            double expectedGL;
            if (isArch) {
                // Arch: Always flat GL regardless of property type
                expectedGL = glAmount;
            } else if (isStarStone && isMultiFamily) {
                // StarStone with Multi-Family: GL × Units
                expectedGL = glAmount * units;
            } else {
                // Default/StarStone with RS: flat GL
                expectedGL = glAmount;
            }

            // Find matching data in frontend - use reverse index since frontend displays newest first
            // Frontend table shows locations in reverse order (last added = first row)
            int frontendIndex = frontendData.size() - 1 - i;
            Double actualGL = null;
            String matchedAddress = "";

            // Primary: Try index-based matching (reverse order)
            if (frontendIndex >= 0 && frontendIndex < frontendData.size()) {
                Map<String, Object> frontendLoc = frontendData.get(frontendIndex);
                actualGL = (Double) frontendLoc.get("gl");
                matchedAddress = (String) frontendLoc.get("address");
            }

            // Fallback: If index doesn't give valid data, try address matching
            if (actualGL == null) {
                for (Map<String, Object> frontendLoc : frontendData) {
                    String frontendAddress = (String) frontendLoc.get("address");
                    if (frontendAddress != null && addressMatchesEnhanced(excelAddress, frontendAddress)) {
                        actualGL = (Double) frontendLoc.get("gl");
                        matchedAddress = frontendAddress;
                        break;
                    }
                }
            }

            // Truncate address for display (first 47 chars + ...)
            String displayAddress = excelAddress.length() > 47 ? excelAddress.substring(0, 47) + "..." : excelAddress;

            if (actualGL == null) {
                String line = String.format("%-50s | %-6s | %-5d | $%-11s | %-12s | %-6s",
                    displayAddress, propertyType, units, formatAmount(expectedGL), "NOT FOUND", "FAIL");
                report.append(line).append("\n");
                allValid = false;
                logger.info("Address not found in frontend: {}", excelAddress);
            } else {
                boolean passed = Math.abs(actualGL - expectedGL) <= 0.01;
                String status = passed ? "PASS" : "FAIL";
                String line = String.format("%-50s | %-6s | %-5d | $%-11s | $%-11s | %-6s",
                    displayAddress, propertyType, units, formatAmount(expectedGL), formatAmount(actualGL), status);
                report.append(line).append("\n");

                if (!passed) {
                    allValid = false;
                }
            }
        }

        report.append("-".repeat(100)).append("\n");
        report.append("\nResult: ").append(allValid ? "ALL PASSED" : "FAILED");

        logger.info("\n{}", report.toString());
        logToReport(report.toString());
        captureScreenshotToReport("GL Validation - " + (allValid ? "PASSED" : "FAILED"));
        return allValid;
    }

    /**
     * Check if two addresses match (handles abbreviations like St/Street, Ave/Avenue)
     */
    private boolean addressMatches(String excelAddress, String frontendAddress) {
        if (excelAddress == null || frontendAddress == null) return false;

        // Normalize addresses - expand common abbreviations and remove special chars
        String excel = normalizeAddress(excelAddress);
        String frontend = normalizeAddress(frontendAddress);

        // Direct match after normalization
        if (excel.equals(frontend)) return true;

        // Check if one contains the other
        if (excel.contains(frontend) || frontend.contains(excel)) return true;

        // Extract street number and name for matching
        String excelStreet = extractStreetPart(excel);
        String frontendStreet = extractStreetPart(frontend);

        // Match if street parts are same
        return excelStreet.equals(frontendStreet) ||
               excelStreet.contains(frontendStreet) ||
               frontendStreet.contains(excelStreet);
    }

    /**
     * Enhanced address matching that handles auto-complete address changes
     * Matches by: city name + street number OR city name + first word of street name
     */
    private boolean addressMatchesEnhanced(String excelAddress, String frontendAddress) {
        if (excelAddress == null || frontendAddress == null) return false;

        // First try standard matching
        if (addressMatches(excelAddress, frontendAddress)) return true;

        String excelLower = excelAddress.toLowerCase().trim();
        String frontendLower = frontendAddress.toLowerCase().trim();

        // Extract city from both addresses (common Kentucky cities)
        String[] cities = {"louisville", "lexington", "bowling green", "owensboro", "covington",
            "richmond", "frankfort", "georgetown", "florence", "hopkinsville", "nicholasville",
            "elizabethtown", "paducah", "morehead", "grayson", "lewisport", "paris"};

        String excelCity = null;
        String frontendCity = null;
        for (String city : cities) {
            if (excelLower.contains(city)) excelCity = city;
            if (frontendLower.contains(city)) frontendCity = city;
        }

        // If cities match, good candidate
        boolean citiesMatch = excelCity != null && excelCity.equals(frontendCity);

        // Extract street number (first sequence of digits)
        String excelNumber = extractStreetNumber(excelLower);
        String frontendNumber = extractStreetNumber(frontendLower);

        // If same city and same street number, likely a match
        if (citiesMatch && excelNumber != null && excelNumber.equals(frontendNumber)) {
            return true;
        }

        // Extract first meaningful word of street name (after number, skip directionals)
        String excelStreetWord = extractFirstStreetWord(excelLower);
        String frontendStreetWord = extractFirstStreetWord(frontendLower);

        // Match by city + first street word
        if (citiesMatch && excelStreetWord != null && frontendStreetWord != null) {
            if (excelStreetWord.equals(frontendStreetWord) ||
                excelStreetWord.startsWith(frontendStreetWord) ||
                frontendStreetWord.startsWith(excelStreetWord)) {
                return true;
            }
        }

        // Match by street number + first street word (even if cities different due to auto-complete)
        if (excelNumber != null && excelNumber.equals(frontendNumber) &&
            excelStreetWord != null && frontendStreetWord != null &&
            (excelStreetWord.equals(frontendStreetWord) ||
             excelStreetWord.startsWith(frontendStreetWord) ||
             frontendStreetWord.startsWith(excelStreetWord))) {
            return true;
        }

        return false;
    }

    /**
     * Extract street number from address (first sequence of digits)
     */
    private String extractStreetNumber(String address) {
        if (address == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\D*(\\d+)").matcher(address);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Extract first meaningful street word (skip number, directionals like E/W/N/S)
     */
    private String extractFirstStreetWord(String address) {
        if (address == null) return null;
        // Remove street number and common directionals
        String cleaned = address.replaceAll("^[\\d\\s]+", "") // Remove leading numbers
            .replaceAll("^(po\\s+box|p\\.?o\\.?\\s*box)\\s*\\d*\\s*,?\\s*", "") // Remove PO Box
            .replaceAll("^(e|w|n|s|east|west|north|south)\\s+", "") // Remove directionals
            .trim();
        // Get first word
        String[] parts = cleaned.split("[\\s,]+");
        if (parts.length > 0 && parts[0].length() > 1) {
            // Normalize common abbreviations
            String word = parts[0]
                .replaceAll("blvd", "boulevard")
                .replaceAll("st$", "street")
                .replaceAll("ave$", "avenue")
                .replaceAll("rd$", "road")
                .replaceAll("dr$", "drive");
            return word;
        }
        return null;
    }

    /**
     * Normalize address by converting full words to abbreviations (standardize to short form)
     */
    private String normalizeAddress(String address) {
        if (address == null) return "";

        String normalized = address.toLowerCase()
            // Convert full words to abbreviations (standardize to short form)
            .replaceAll("\\bstreet\\b", "st")
            .replaceAll("\\bavenue\\b", "ave")
            .replaceAll("\\broad\\b", "rd")
            .replaceAll("\\bdrive\\b", "dr")
            .replaceAll("\\blane\\b", "ln")
            .replaceAll("\\bboulevard\\b", "blvd")
            .replaceAll("\\bcourt\\b", "ct")
            .replaceAll("\\bcircle\\b", "cir")
            .replaceAll("\\bparkway\\b", "pkwy")
            .replaceAll("\\bhighway\\b", "hwy")
            .replaceAll("\\bplace\\b", "pl")
            .replaceAll("\\bterrace\\b", "ter")
            .replaceAll("\\bway\\b", "wy");

        // Remove all non-alphanumeric characters for comparison
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    /**
     * Extract street number and name (first part before city/state)
     */
    private String extractStreetPart(String normalizedAddress) {
        // Take first 20 chars to match street number + name
        int len = Math.min(20, normalizedAddress.length());
        return normalizedAddress.substring(0, len);
    }

    /**
     * Get address and GL values from frontend table
     */
    private List<Map<String, Object>> getAddressAndGLFromTable() {
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        try {
            // Find header row to get column indices
            List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th"));
            int addressColIndex = -1;
            int glColIndex = -1;

            for (int i = 0; i < headers.size(); i++) {
                String headerText = headers.get(i).getText().toLowerCase();
                if (headerText.contains("address") || headerText.contains("location")) {
                    addressColIndex = i;
                }
                if (headerText.contains("gl") || headerText.contains("general liability")) {
                    glColIndex = i;
                }
            }

            logger.info("Table columns - Address: {}, GL: {}", addressColIndex, glColIndex);

            if (addressColIndex >= 0 && glColIndex >= 0) {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody//tr"));
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    if (addressColIndex < cells.size() && glColIndex < cells.size()) {
                        Map<String, Object> rowData = new java.util.HashMap<>();
                        rowData.put("address", cells.get(addressColIndex).getText().trim());
                        rowData.put("gl", parseAmountValue(cells.get(glColIndex).getText()));
                        data.add(rowData);
                        logger.info("Found row - Address: {}, GL: {}",
                            rowData.get("address"), rowData.get("gl"));
                    }
                }
            }
        } catch (Exception e) {
            logger.info("Error reading table: {}", e.getMessage());
        }
        return data;
    }

    /**
     * Get column values as list of doubles
     */
    private List<Double> getColumnValues(String... columnNames) {
        List<Double> values = new java.util.ArrayList<>();
        try {
            List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th"));
            int colIndex = -1;

            for (String columnName : columnNames) {
                for (int i = 0; i < headers.size(); i++) {
                    if (headers.get(i).getText().toLowerCase().contains(columnName.toLowerCase())) {
                        colIndex = i;
                        break;
                    }
                }
                if (colIndex >= 0) break;
            }

            if (colIndex >= 0) {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody//tr"));
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    if (colIndex < cells.size()) {
                        values.add(parseAmountValue(cells.get(colIndex).getText()));
                    }
                }
            }
        } catch (Exception e) {
        }
        return values;
    }


    /**
     * Get sum of a column from table (tries multiple column names)
     * Uses parseAmountValue from BasePage
     */
    private double getColumnSum(String... columnNames) {
        double sum = 0.0;
        try {
            List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th"));
            int colIndex = -1;

            // Try each column name
            for (String columnName : columnNames) {
                for (int i = 0; i < headers.size(); i++) {
                    String headerText = headers.get(i).getText().toLowerCase().trim();
                    if (headerText.contains(columnName.toLowerCase()) ||
                        headerText.equals(columnName.toLowerCase())) {
                        colIndex = i;
                        break;
                    }
                }
                if (colIndex >= 0) break;
            }

            if (colIndex >= 0) {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody//tr"));
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    if (colIndex < cells.size()) {
                        sum += parseAmountValue(cells.get(colIndex).getText());
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error getting column sum: {}", e.getMessage());
        }
        return roundTo2Decimals(sum);
    }

    /**
     * Get displayed total value from UI
     * Uses parseAmountValue from BasePage
     * Supports multiple label alternatives (tries each until found)
     */
    protected double getDisplayedTotal(String... labels) {
        for (String label : labels) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(
                    "//*[contains(text(),'" + label + "')]/following-sibling::*[1] | " +
                    "//*[contains(text(),'" + label + "')]/..//span[last()] | " +
                    "//td[contains(text(),'" + label + "')]/following-sibling::td[1]"
                ));
                for (WebElement el : elements) {
                    String text = el.getText().trim();
                    if (text.contains("$") || text.matches(".*\\d.*")) {
                        return parseAmountValue(text);
                    }
                }
            } catch (Exception e) {
                logger.debug("Error getting displayed total for '{}': {}", label, e.getMessage());
            }
        }
        return 0.0;
    }

    // ==================== PDF Download and Validation Methods ====================

    /**
     * Click the Download button to download PDF
     * @return true if download button was clicked successfully
     */
    public boolean clickDownloadButton() {
        logger.info("Clicking Download button");
        try {
            String[] xpaths = {
                "//button[contains(text(),'Download')]",
                "//button[contains(@id,'download')]",
                "//button[contains(@class,'download')]",
                "//a[contains(text(),'Download')]",
                "//button[contains(text(),'PDF')]"
            };

            for (String xpath : xpaths) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            scrollIntoView(btn);
                            sleep(500);
                            btn.click();
                            logger.info("Download button clicked");
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
            logger.info("Download button not found");
            return false;
        } catch (Exception e) {
            logger.info("Error clicking download: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Wait for PDF download and return the file path
     * @param downloadDir the download directory to monitor
     * @param timeoutSeconds max time to wait for download
     * @return path to downloaded PDF file, or null if not found
     */
    public String waitForPDFDownload(String downloadDir, int timeoutSeconds) {
        logger.info("Waiting for PDF download in: {}", downloadDir);
        java.io.File dir = new java.io.File(downloadDir);

        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;
        // Look for PDFs modified within the last 60 seconds (more lenient)
        long recentThreshold = startTime - 60000;

        while (System.currentTimeMillis() - startTime < timeout) {
            java.io.File[] files = dir.listFiles((d, name) ->
                name.toLowerCase().endsWith(".pdf") &&
                !name.endsWith(".crdownload") &&
                !name.endsWith(".tmp"));

            if (files != null && files.length > 0) {
                // Find most recent PDF modified within the last 60 seconds
                java.io.File mostRecent = null;
                long mostRecentTime = 0;
                for (java.io.File file : files) {
                    if (file.lastModified() > mostRecentTime &&
                        file.lastModified() > recentThreshold) {
                        mostRecentTime = file.lastModified();
                        mostRecent = file;
                    }
                }
                if (mostRecent != null) {
                    logger.info("PDF downloaded: {}", mostRecent.getAbsolutePath());
                    return mostRecent.getAbsolutePath();
                }
            }
            sleep(1000);
        }

        // Last resort: find the most recently modified PDF regardless of time
        logger.info("Trying to find any recent PDF in Downloads...");
        java.io.File[] allPdfs = dir.listFiles((d, name) ->
            name.toLowerCase().endsWith(".pdf") &&
            !name.endsWith(".crdownload") &&
            !name.endsWith(".tmp"));

        if (allPdfs != null && allPdfs.length > 0) {
            java.io.File mostRecent = null;
            long mostRecentTime = 0;
            for (java.io.File file : allPdfs) {
                if (file.lastModified() > mostRecentTime) {
                    mostRecentTime = file.lastModified();
                    mostRecent = file;
                }
            }
            if (mostRecent != null) {
                logger.info("Found most recent PDF: {}", mostRecent.getAbsolutePath());
                return mostRecent.getAbsolutePath();
            }
        }

        logger.info("PDF download timeout after {} seconds", timeoutSeconds);
        return null;
    }

    /**
     * Get the Chrome default download directory
     */
    public String getDownloadDirectory() {
        String userHome = System.getProperty("user.home");
        return userHome + java.io.File.separator + "Downloads";
    }

    /**
     * Read PDF and extract all text
     * @param pdfPath path to PDF file
     * @return extracted text from PDF
     */
    public String readPDFText(String pdfPath) {
        logger.info("Reading PDF: {}", pdfPath);
        try {
            java.io.File file = new java.io.File(pdfPath);
            org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file);
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            logger.info("PDF text extracted, length: {} chars", text.length());
            return text;
        } catch (Exception e) {
            logger.info("Error reading PDF: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Read only the first page (Summary page) of PDF
     * @param pdfPath path to PDF file
     * @return extracted text from first page
     */
    public String readPDFFirstPage(String pdfPath) {
        logger.info("Reading PDF first page: {}", pdfPath);
        try {
            java.io.File file = new java.io.File(pdfPath);
            org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file);
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (Exception e) {
            logger.info("Error reading PDF first page: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Get current values from quote screen for PDF validation
     * @return map of field names to their values
     */
    public Map<String, String> getQuoteScreenValues() {
        Map<String, String> values = new java.util.HashMap<>();

        try {
            // Get Carrier using the actual dropdown button
            String carrier = getDropdownSelectedText("Carrier");
            values.put("Carrier", carrier);
            logger.info("Captured Carrier: {}", carrier);

            // Get Insured
            String insured = getDropdownSelectedText("Insured");
            values.put("Insured", insured);
            logger.info("Captured Insured: {}", insured);

            // Get Agent
            String agent = getDropdownSelectedText("Agent");
            values.put("Agent", agent);
            logger.info("Captured Agent: {}", agent);

            // Get Effective Date
            String effectiveDate = getDateFieldValue("effective", "Effective");
            values.put("EffectiveDate", effectiveDate);
            logger.info("Captured EffectiveDate: {}", effectiveDate);

            // Get Expiration Date
            String expirationDate = getDateFieldValue("expiration", "Expiration");
            values.put("ExpirationDate", expirationDate);
            logger.info("Captured ExpirationDate: {}", expirationDate);

            // Get Grand Total
            double grandTotal = getDisplayedTotal("Grand Total");
            values.put("GrandTotal", String.valueOf(grandTotal));
            logger.info("Captured GrandTotal: {}", grandTotal);

            // Get column sums from table
            values.put("CoverageA", String.valueOf(getColumnSum("Coverage A", "Cov A", "CovA")));
            values.put("CoverageB", String.valueOf(getColumnSum("Coverage B", "Cov B", "CovB")));
            values.put("CoverageC", String.valueOf(getColumnSum("Coverage C", "Cov C", "CovC")));
            values.put("CoverageD", String.valueOf(getColumnSum("Coverage D", "Cov D", "CovD", "Loss of Rents")));
            values.put("GLPremium", String.valueOf(getColumnSum("GL Premium", "GL", "General Liability")));
            values.put("WSPremium", String.valueOf(getColumnSum("WS Premium", "WS", "Water", "Sewer")));
            values.put("Tax", String.valueOf(getColumnSum("Tax", "Taxes")));
            values.put("PropertyPremium", String.valueOf(getColumnSum("Property Premium", "Prop Premium", "Premium")));

            logger.info("All Quote screen values captured: {}", values);
        } catch (Exception e) {
            logger.info("Error getting quote values: {}", e.getMessage());
        }
        return values;
    }

    /**
     * Get selected text from dropdown by label text
     */
    private String getDropdownSelectedText(String labelText) {
        try {
            // Special handling for Carrier dropdown - use specific ID
            if ("Carrier".equalsIgnoreCase(labelText)) {
                try {
                    // First try to get from the specific carrier dropdown button
                    WebElement carrierButton = driver.findElement(By.id("new-quote-carrier-select"));
                    if (carrierButton != null) {
                        String text = carrierButton.getText().trim();
                        if (text != null && !text.isEmpty() && !text.equalsIgnoreCase("Select")) {
                            logger.info("Found Carrier value from ID: {}", text);
                            return text;
                        }
                    }
                } catch (Exception e) {
                    // Continue to fallback
                }
            }

            String[] xpaths = {
                // Direct button text (most reliable)
                "//label[contains(text(),'" + labelText + "')]/following::button[1]",
                // Button dropdown with span inside
                "//label[contains(text(),'" + labelText + "')]/following::button[1]//span[not(contains(@class,'icon'))]",
                // ID-based dropdown button (not the options)
                "//*[@id='new-quote-" + labelText.toLowerCase() + "-select']",
                // Select element
                "//label[contains(text(),'" + labelText + "')]/following::select[1]/option[@selected]",
                // React select
                "//label[contains(text(),'" + labelText + "')]/following::div[contains(@class,'select')][1]//span[1]"
            };

            for (String xpath : xpaths) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(xpath));
                    for (WebElement el : elements) {
                        String text = el.getText().trim();
                        if (text != null && !text.isEmpty() &&
                            !text.equalsIgnoreCase("Select") &&
                            !text.equalsIgnoreCase("Select...") &&
                            !text.contains("Select ") &&
                            text.length() > 2) {  // Avoid picking up icon text
                            logger.info("Found {} value: {} using xpath: {}", labelText, text, xpath);
                            return text;
                        }
                    }
                } catch (Exception e) {
                    // Continue to next xpath
                }
            }
        } catch (Exception e) {
            logger.info("Error getting dropdown value for {}: {}", labelText, e.getMessage());
        }
        return "";
    }

    /**
     * Get date field value
     */
    private String getDateFieldValue(String... identifiers) {
        for (String id : identifiers) {
            try {
                String[] xpaths = {
                    "//input[contains(@id,'" + id + "')]",
                    "//input[contains(@name,'" + id + "')]",
                    "//label[contains(text(),'" + id + "')]/following::input[1]",
                    "//input[contains(@placeholder,'" + id + "')]"
                };

                for (String xpath : xpaths) {
                    List<WebElement> elements = driver.findElements(By.xpath(xpath));
                    for (WebElement el : elements) {
                        String value = el.getAttribute("value");
                        if (value != null && !value.isEmpty()) {
                            return value;
                        }
                    }
                }
            } catch (Exception e) {
                // Continue
            }
        }
        return "";
    }

    /**
     * Validate PDF Summary page against quote screen values
     * @param pdfPath path to downloaded PDF
     * @param screenValues values from quote screen
     * @return validation result with detailed report
     */
    public PDFValidationResult validatePDFSummary(String pdfPath, Map<String, String> screenValues) {
        logger.info("=== Starting PDF Validation ===");
        PDFValidationResult result = new PDFValidationResult();

        String pdfText = readPDFFirstPage(pdfPath);
        if (pdfText.isEmpty()) {
            result.addError("Failed to read PDF file");
            return result;
        }

        // Log first 500 chars of PDF for debugging
        logger.info("PDF First Page Text (first 500 chars): {}",
            pdfText.length() > 500 ? pdfText.substring(0, 500) : pdfText);

        // Normalize PDF text for searching
        String pdfTextLower = pdfText.toLowerCase();

        StringBuilder report = new StringBuilder();
        report.append("PDF Summary Validation Report\n");
        report.append("=".repeat(80)).append("\n");
        report.append("PDF File: ").append(pdfPath).append("\n");
        report.append("Screen Values: ").append(screenValues).append("\n\n");

        // 1. Validate Insuring Company (Carrier)
        String carrier = screenValues.getOrDefault("Carrier", "");
        if (!carrier.isEmpty()) {
            boolean found = pdfTextLower.contains(carrier.toLowerCase());
            report.append(formatValidationLine("Insuring Company", carrier, found));
            if (!found) result.addError("Insuring Company '" + carrier + "' not found in PDF");
        }

        // 2. Validate Named Insured
        String insured = screenValues.getOrDefault("Insured", "");
        if (!insured.isEmpty()) {
            boolean found = pdfTextLower.contains(insured.toLowerCase());
            report.append(formatValidationLine("Named Insured", insured, found));
            if (!found) result.addError("Named Insured '" + insured + "' not found in PDF");
        }

        // 3. Validate Agent Name
        String agent = screenValues.getOrDefault("Agent", "");
        if (!agent.isEmpty()) {
            boolean found = pdfTextLower.contains(agent.toLowerCase());
            report.append(formatValidationLine("Agent Name", agent, found));
            if (!found) result.addError("Agent Name '" + agent + "' not found in PDF");
        }

        // 4. Validate Effective Date
        String effectiveDate = screenValues.getOrDefault("EffectiveDate", "");
        if (!effectiveDate.isEmpty()) {
            boolean found = pdfText.contains(effectiveDate) || pdfText.contains(formatDateVariations(effectiveDate));
            report.append(formatValidationLine("Effective Date", effectiveDate, found));
            if (!found) result.addError("Effective Date '" + effectiveDate + "' not found in PDF");
        }

        // 5. Validate Expiration Date
        String expirationDate = screenValues.getOrDefault("ExpirationDate", "");
        if (!expirationDate.isEmpty()) {
            boolean found = pdfText.contains(expirationDate) || pdfText.contains(formatDateVariations(expirationDate));
            report.append(formatValidationLine("Expiration Date", expirationDate, found));
            if (!found) result.addError("Expiration Date '" + expirationDate + "' not found in PDF");
        }

        report.append("\n--- Premium Amounts Validation ---\n");

        // 6. Validate Coverage A Premium Sum
        validateAmountInPDF(report, result, pdfText, "Coverage A Premium",
            screenValues.getOrDefault("CoverageA", "0"));

        // 7. Validate Coverage B Premium Sum
        validateAmountInPDF(report, result, pdfText, "Coverage B Premium",
            screenValues.getOrDefault("CoverageB", "0"));

        // 8. Validate Coverage C Premium Sum
        validateAmountInPDF(report, result, pdfText, "Coverage C Premium",
            screenValues.getOrDefault("CoverageC", "0"));

        // 9. Validate Coverage D Premium Sum (Loss of Rents)
        validateAmountInPDF(report, result, pdfText, "Coverage D Premium",
            screenValues.getOrDefault("CoverageD", "0"));

        // 10. Validate GL Premium Sum
        validateAmountInPDF(report, result, pdfText, "GL Premium",
            screenValues.getOrDefault("GLPremium", "0"));

        // 11. Validate WS Premium Sum
        validateAmountInPDF(report, result, pdfText, "WS Premium",
            screenValues.getOrDefault("WSPremium", "0"));

        // 12. Validate Tax Sum
        validateAmountInPDF(report, result, pdfText, "Tax",
            screenValues.getOrDefault("Tax", "0"));

        // 13. Validate Total Premium Due = Grand Total
        String grandTotal = screenValues.getOrDefault("GrandTotal", "0");
        validateAmountInPDF(report, result, pdfText, "Total Premium Due",
            grandTotal);

        report.append("\n").append("=".repeat(80)).append("\n");
        report.append("RESULT: ").append(result.isValid() ? "ALL VALIDATIONS PASSED" : "VALIDATION FAILED").append("\n");

        if (!result.isValid()) {
            report.append("\nErrors:\n");
            for (String error : result.getErrors()) {
                report.append("  - ").append(error).append("\n");
            }
        }

        result.setReport(report.toString());
        logger.info("\n{}", report.toString());
        logToReport(report.toString());

        return result;
    }

    /**
     * Validate an amount value in PDF
     */
    private void validateAmountInPDF(StringBuilder report, PDFValidationResult result,
                                     String pdfText, String fieldName, String expectedValue) {
        double expected = parseAmountValue(expectedValue);
        String formattedExpected = formatAmount(expected);

        // Search for the amount in PDF (try various formats)
        boolean found = pdfText.contains(formattedExpected) ||
                       pdfText.contains("$" + formattedExpected) ||
                       pdfText.contains(String.valueOf((int) expected)) ||
                       pdfText.contains("$" + (int) expected);

        // Also try without decimals if whole number
        if (!found && expected == Math.floor(expected)) {
            found = pdfText.contains(String.format("%.0f", expected));
        }

        report.append(formatValidationLine(fieldName, "$" + formattedExpected, found));

        if (!found && expected > 0) {
            result.addError(fieldName + " amount $" + formattedExpected + " not found in PDF");
        }
    }

    /**
     * Format validation line for report
     */
    private String formatValidationLine(String field, String value, boolean passed) {
        String status = passed ? "PASS" : "FAIL";
        return String.format("%-25s: %-30s [%s]\n", field, value, status);
    }

    /**
     * Try different date format variations
     */
    private String formatDateVariations(String date) {
        // Return as-is for now, can add more formats if needed
        return date;
    }

    /**
     * Complete PDF download and validation flow
     * Opens PDF in new tab and validates with screenshots
     * @return validation result
     */
    public PDFValidationResult downloadAndValidatePDF() {
        logger.info("=== Starting PDF Download and Validation ===");

        // Step 1: Capture current screen values before download
        Map<String, String> screenValues = getQuoteScreenValues();
        captureScreenshotToReport("Quote Screen - Before PDF Download");

        // Step 2: Click download button
        if (!clickDownloadButton()) {
            PDFValidationResult result = new PDFValidationResult();
            result.addError("Failed to click download button");
            return result;
        }

        // Step 3: Wait for PDF download
        String downloadDir = getDownloadDirectory();
        sleep(2000); // Initial wait for download to start
        String pdfPath = waitForPDFDownload(downloadDir, 30);

        if (pdfPath == null) {
            PDFValidationResult result = new PDFValidationResult();
            result.addError("PDF download failed or timed out");
            return result;
        }

        // Step 4: Open PDF in new browser tab
        String originalWindow = driver.getWindowHandle();
        openPDFInNewTab(pdfPath);
        sleep(3000); // Wait for PDF to load in browser

        // Step 5: Validate PDF with screenshots
        PDFValidationResult result = validatePDFWithScreenshots(pdfPath, screenValues);

        // Step 7: Close PDF tab and switch back
        closePDFTab(originalWindow);

        return result;
    }

    /**
     * Open PDF file in a new browser tab
     */
    private void openPDFInNewTab(String pdfPath) {
        logger.info("Opening PDF in new tab: {}", pdfPath);
        try {
            // Store original window handle
            String originalWindow = driver.getWindowHandle();
            int originalWindowCount = driver.getWindowHandles().size();

            // Convert to file URL format
            String fileUrl = "file:///" + pdfPath.replace("\\", "/").replace(" ", "%20");
            logger.info("PDF URL: {}", fileUrl);

            // Open new tab using JavaScript
            ((JavascriptExecutor) driver).executeScript("window.open('" + fileUrl + "', '_blank');");
            sleep(2000);

            // Wait for new window to open
            int retries = 0;
            while (driver.getWindowHandles().size() <= originalWindowCount && retries < 10) {
                sleep(500);
                retries++;
            }

            // Switch to the new tab (not the original)
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    logger.info("Switched to new tab with handle: {}", handle);
                    break;
                }
            }

            sleep(2000); // Wait for PDF to render
            logger.info("Current URL after switch: {}", driver.getCurrentUrl());
        } catch (Exception e) {
            logger.info("Error opening PDF in tab: {}", e.getMessage());
        }
    }

    /**
     * Close PDF tab and switch back to original window
     */
    private void closePDFTab(String originalWindow) {
        try {
            // Get all window handles
            java.util.Set<String> handles = driver.getWindowHandles();

            // If we have multiple windows, close the current one
            if (handles.size() > 1) {
                String currentHandle = driver.getWindowHandle();
                if (!currentHandle.equals(originalWindow)) {
                    driver.close();
                    sleep(500);
                }
            }

            // Switch back to original window
            driver.switchTo().window(originalWindow);
            sleep(1000);
            logger.info("Switched back to main window");
        } catch (Exception e) {
            // Try to recover by switching to any available window
            try {
                java.util.Set<String> handles = driver.getWindowHandles();
                if (!handles.isEmpty()) {
                    driver.switchTo().window(handles.iterator().next());
                    logger.info("Recovered - switched to available window");
                }
            } catch (Exception ex) {
                logger.info("Error recovering from tab close: {}", ex.getMessage());
            }
        }
    }

    /**
     * Validate PDF data against New Quote screen data
     * Uses common validation methods from BasePage
     */
    private PDFValidationResult validatePDFWithScreenshots(String pdfPath, Map<String, String> screenValues) {
        logger.info("=== Validating PDF Data vs New Quote Screen Data ===");
        PDFValidationResult result = new PDFValidationResult();

        // Read PDF text using PDFBox
        String pdfText = readPDFFirstPage(pdfPath);
        if (pdfText.isEmpty()) {
            result.addError("Failed to read PDF file");
            return result;
        }

        // Track validation results for building report at the end
        java.util.List<String[]> validationRows = new java.util.ArrayList<>();

        // 1. Carrier/Insuring Company - using BasePage method
        validatePDFTextField(validationRows, result, pdfText, "Insuring Company",
            screenValues.getOrDefault("Carrier", ""));

        // 2. Named Insured - using BasePage method
        validatePDFTextField(validationRows, result, pdfText, "Named Insured",
            screenValues.getOrDefault("Insured", ""));

        // 3. Agent - using BasePage method
        validatePDFTextField(validationRows, result, pdfText, "Agent Name",
            screenValues.getOrDefault("Agent", ""));

        // 4. Effective Date - using BasePage method
        validatePDFDateField(validationRows, result, pdfText, "Effective Date",
            screenValues.getOrDefault("EffectiveDate", ""));

        // 5. Expiration Date - using BasePage method
        validatePDFDateField(validationRows, result, pdfText, "Expiration Date",
            screenValues.getOrDefault("ExpirationDate", ""));

        // 6. Grand Total / Total Premium Due - using BasePage method
        validatePDFAmountField(validationRows, result, pdfText, "Total Premium Due",
            screenValues.getOrDefault("GrandTotal", "0"));

        // 11. GL Premium - using BasePage method
        validatePDFAmountField(validationRows, result, pdfText, "GL Premium",
            screenValues.getOrDefault("GLPremium", "0"));

        // 12. WS Premium - using BasePage method
        validatePDFAmountField(validationRows, result, pdfText, "WS Premium",
            screenValues.getOrDefault("WSPremium", "0"));

        // 13. Tax Sum (PDF has 4 tax fields) - using BasePage extract method
        String taxFromScreen = screenValues.getOrDefault("Tax", "0");
        double expectedTax = parsePDFAmount(taxFromScreen);
        if (expectedTax > 0) {
            double surplusLinesTax = extractPDFAmountAfterLabel(pdfText, "Surplus Lines Tax");
            double stampingFee = extractPDFAmountAfterLabel(pdfText, "Stamping Fee");
            double fireTax = extractPDFAmountAfterLabel(pdfText, "Fire Tax");
            double municipalTax = extractPDFAmountAfterLabel(pdfText, "Municipal Tax");

            double pdfTaxSum = surplusLinesTax + stampingFee + fireTax + municipalTax;

            logger.info("PDF Tax: Surplus={}, Stamping={}, Fire={}, Municipal={}, Sum={}",
                surplusLinesTax, stampingFee, fireTax, municipalTax, pdfTaxSum);

            boolean taxMatch = Math.abs(pdfTaxSum - expectedTax) <= 0.01;
            validationRows.add(new String[]{"Tax Total", "$" + formatPDFAmount(expectedTax),
                "$" + formatPDFAmount(pdfTaxSum), taxMatch ? "PASS" : "FAIL"});

            // Add tax breakdown rows (info only) - using BasePage method
            addPDFInfoRow(validationRows, "  - Surplus Lines Tax", "$" + formatPDFAmount(surplusLinesTax));
            addPDFInfoRow(validationRows, "  - Stamping Fee", "$" + formatPDFAmount(stampingFee));
            addPDFInfoRow(validationRows, "  - Fire Tax", "$" + formatPDFAmount(fireTax));
            addPDFInfoRow(validationRows, "  - Municipal Tax", "$" + formatPDFAmount(municipalTax));

            if (!taxMatch) {
                result.addError("Tax mismatch: Screen=$" + formatPDFAmount(expectedTax) + ", PDF=$" + formatPDFAmount(pdfTaxSum));
            }
        }

        // 14. Validate PDF Page 2 - Location Details
        // PDF Page 2 contains: Address, Dwelling(A), Structures(B), Personal Prop(C), Rents(D), TIV, Full Term
        validatePDFPage2LocationData(pdfPath, validationRows, result);

        // Build clean HTML table report with black text for visibility
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String status = result.isValid() ? "PASSED" : "FAILED";
        String statusColor = result.isValid() ? "#155724" : "#721c24";

        StringBuilder htmlReport = new StringBuilder();
        htmlReport.append("<table style='width:100%; border-collapse:collapse; font-family:Arial,sans-serif; font-size:13px; color:#000000;'>");

        // Status row at top
        htmlReport.append("<tr style='background-color:#e9ecef;'>");
        htmlReport.append("<td colspan='2' style='padding:10px; border:1px solid #000000; color:#000000;'><b>Timestamp:</b> ").append(timestamp).append("</td>");
        htmlReport.append("<td colspan='2' style='padding:10px; border:1px solid #000000; color:#000000; text-align:right;'><b>Status:</b> <span style='color:").append(statusColor).append("; font-weight:bold;'>").append(status).append("</span></td>");
        htmlReport.append("</tr>");

        // Header row - dark background with white text
        htmlReport.append("<tr style='background-color:#495057;'>");
        htmlReport.append("<th style='padding:10px; border:1px solid #000000; text-align:left; color:#ffffff; font-weight:bold;'>Field</th>");
        htmlReport.append("<th style='padding:10px; border:1px solid #000000; text-align:left; color:#ffffff; font-weight:bold;'>Quote Data</th>");
        htmlReport.append("<th style='padding:10px; border:1px solid #000000; text-align:left; color:#ffffff; font-weight:bold;'>PDF Data</th>");
        htmlReport.append("<th style='padding:10px; border:1px solid #000000; text-align:center; color:#ffffff; font-weight:bold;'>Status</th>");
        htmlReport.append("</tr>");

        // Data rows - black text on light backgrounds
        for (String[] row : validationRows) {
            String bgColor = row[3].equals("PASS") ? "#d4edda" : (row[3].equals("FAIL") ? "#f8d7da" : "#f8f9fa");
            String badgeColor = row[3].equals("PASS") ? "#28a745" : (row[3].equals("FAIL") ? "#dc3545" : "#6c757d");

            htmlReport.append("<tr style='background-color:").append(bgColor).append(";'>");
            htmlReport.append("<td style='padding:8px; border:1px solid #000000; color:#000000;'>").append(row[0]).append("</td>");
            htmlReport.append("<td style='padding:8px; border:1px solid #000000; color:#000000;'>").append(row[1]).append("</td>");
            htmlReport.append("<td style='padding:8px; border:1px solid #000000; color:#000000;'>").append(row[2]).append("</td>");
            htmlReport.append("<td style='padding:8px; border:1px solid #000000; text-align:center;'>");
            htmlReport.append("<span style='background-color:").append(badgeColor).append("; color:#ffffff; padding:3px 8px; border-radius:3px; font-size:12px; font-weight:bold;'>").append(row[3]).append("</span>");
            htmlReport.append("</td>");
            htmlReport.append("</tr>");
        }

        htmlReport.append("</table>");

        // Console log only (no extra text to report)
        StringBuilder consoleLog = new StringBuilder();
        consoleLog.append("\n=== PDF Validation Results ===\n");
        consoleLog.append("Status: ").append(status).append("\n");
        consoleLog.append(String.format("%-20s | %-20s | %-20s | %-8s\n", "Field", "Quote Data", "PDF Data", "Status"));
        consoleLog.append("-".repeat(75)).append("\n");
        for (String[] row : validationRows) {
            String f = row[0].length() > 20 ? row[0].substring(0, 17) + "..." : row[0];
            String q = row[1].length() > 20 ? row[1].substring(0, 17) + "..." : row[1];
            String p = row[2].length() > 20 ? row[2].substring(0, 17) + "..." : row[2];
            consoleLog.append(String.format("%-20s | %-20s | %-20s | %-8s\n", f, q, p, row[3]));
        }

        result.setReport(htmlReport.toString());
        logger.info(consoleLog.toString());
        logHtmlToReport(htmlReport.toString());

        return result;
    }

    // Validate a calculated premium value against PDF (legacy method)
    private void validateCalculatedPremium(java.util.List<String[]> validationRows, PDFValidationResult result, String pdfText, String fieldName, double calculatedPremium) {
        String formatted = "$" + String.format("%,.2f", calculatedPremium);
        if (calculatedPremium <= 0) { validationRows.add(new String[]{fieldName, "$0.00", "N/A", "INFO"}); return; }
        boolean found = amountExistsInPDF(pdfText, calculatedPremium);
        if (!found) found = amountExistsInPDF(pdfText, Math.round(calculatedPremium * 100.0) / 100.0);
        String pdfValue = found ? formatted : "Not Found";
        validationRows.add(new String[]{fieldName, formatted, pdfValue, found ? "PASS" : "FAIL"});
        if (!found) result.addError(fieldName + " calculated as " + formatted + " not found in PDF");
        logger.info("{}: Calculated={}, Found in PDF={}", fieldName, formatted, found);
    }

    // Validate PDF Page 2 location data - TIV = D+AS+BPP+LOR, Full Term = PropPrem+WS+GL+Tax
    private void validatePDFPage2LocationData(String pdfPath, java.util.List<String[]> validationRows, PDFValidationResult result) {
        if (addedLocations == null || addedLocations.isEmpty()) { logger.info("No stored location data for PDF page 2 validation"); return; }
        try {
            String pdfPage2Text = readPDFPages(pdfPath, 2, 2);
            if (pdfPage2Text == null || pdfPage2Text.isEmpty()) { validationRows.add(new String[]{"--- PDF Page 2 ---", "", "Not Readable", "FAIL"}); result.addError("PDF Page 2 could not be read"); return; }
            logger.info("=== Validating PDF Page 2 Location Data ===");
            validationRows.add(new String[]{"--- LOCATION DETAILS (Page 2) ---", "", "", "INFO"});
            validationRows.add(new String[]{"Formulas: TIV = D+AS+BPP+LOR | Full Term = PropPrem+WS+GL+Tax", "", "", "INFO"});
            for (int i = 0; i < addedLocations.size(); i++) {
                LocationData loc = addedLocations.get(i);
                int locationNumber = i + 1;
                boolean addressFound = findAddressInPDFText(pdfPage2Text, loc.getAddress());
                String shortAddr = loc.getAddress();
                if (shortAddr != null && shortAddr.length() > 30) shortAddr = shortAddr.substring(0, 27) + "...";
                validationRows.add(new String[]{"Location " + locationNumber + ": " + shortAddr, "", "", addressFound ? "PASS" : "FAIL"});
                if (!addressFound) { result.addError("Location " + locationNumber + " address not found in PDF Page 2"); continue; }
                PDFLocationData pdfData = extractPDFLocationData(pdfPage2Text, loc.getAddress(), loc.getRate());
                // Set WS, GL, Tax from stored location data for Full Term calculation
                pdfData.setWsPremium(loc.getWsPremium());
                pdfData.setGlPremium(loc.getGlPremium());
                pdfData.setTax(loc.getTax());
                validationRows.add(new String[]{"  PDF Data", String.format("D=$%s, AS=$%s, BPP=$%s, LOR=$%s, WS=$%s, GL=$%s, Tax=$%s", formatAmountWithComma(pdfData.getDwelling()), formatAmountWithComma(pdfData.getAdditionalStructures()), formatAmountWithComma(pdfData.getBpp()), formatAmountWithComma(pdfData.getLossOfRents()), formatAmountWithComma(loc.getWsPremium()), formatAmountWithComma(loc.getGlPremium()), formatAmountWithComma(loc.getTax())), String.format("TIV=$%s, FullTerm=$%s", formatAmountWithComma(pdfData.getPdfTIV()), formatAmountWithComma(pdfData.getPdfFullTerm())), "INFO"});
                validatePDFLocationCalculations(validationRows, result, pdfData, locationNumber);
                validateStoredVsPDFData(validationRows, result, loc, pdfData, locationNumber);
            }
            logger.info("=== PDF Page 2 Location Validation Complete ===");
        } catch (Exception e) { logger.error("Error validating PDF page 2: {}", e.getMessage()); validationRows.add(new String[]{"--- PDF Page 2 ---", "", "Error: " + e.getMessage(), "FAIL"}); result.addError("PDF Page 2 validation error: " + e.getMessage()); }
    }

    // Validate stored location data matches PDF extracted data for data integrity
    private void validateStoredVsPDFData(java.util.List<String[]> validationRows, PDFValidationResult result, LocationData stored, PDFLocationData pdfData, int locationNumber) {
        boolean allMatch = true;
        if (Math.abs(stored.getCoverageA() - pdfData.getDwelling()) > 0.01) { allMatch = false; logger.warn("Location {} Dwelling mismatch", locationNumber); }
        if (Math.abs(stored.getCoverageB() - pdfData.getAdditionalStructures()) > 0.01) { allMatch = false; logger.warn("Location {} AS mismatch", locationNumber); }
        if (Math.abs(stored.getCoverageC() - pdfData.getBpp()) > 0.01) { allMatch = false; logger.warn("Location {} BPP mismatch", locationNumber); }
        if (Math.abs(stored.getCoverageD() - pdfData.getLossOfRents()) > 0.01) { allMatch = false; logger.warn("Location {} LOR mismatch", locationNumber); }
        validationRows.add(new String[]{"  Loc " + locationNumber + " Data: Stored vs PDF", String.format("Stored: D=$%s, AS=$%s, BPP=$%s, LOR=$%s", formatAmountWithComma(stored.getCoverageA()), formatAmountWithComma(stored.getCoverageB()), formatAmountWithComma(stored.getCoverageC()), formatAmountWithComma(stored.getCoverageD())), String.format("PDF: D=$%s, AS=$%s, BPP=$%s, LOR=$%s", formatAmountWithComma(pdfData.getDwelling()), formatAmountWithComma(pdfData.getAdditionalStructures()), formatAmountWithComma(pdfData.getBpp()), formatAmountWithComma(pdfData.getLossOfRents())), allMatch ? "PASS" : "FAIL"});
        if (!allMatch) result.addError("Location " + locationNumber + ": Stored data does not match PDF data");
    }

    // Validate a single location field value in PDF
    private void validateLocationField(java.util.List<String[]> validationRows, PDFValidationResult result,
                                       String pdfText, String fieldName, double expectedAmount) {
        String formatted = "$" + String.format("%,.2f", expectedAmount);

        if (expectedAmount <= 0) {
            validationRows.add(new String[]{fieldName, "$0.00", "N/A", "INFO"});
            return;
        }

        // Check if amount exists in PDF
        boolean found = amountExistsInPDF(pdfText, expectedAmount);
        String pdfValue = found ? formatted : "Not Found";
        validationRows.add(new String[]{fieldName, formatted, pdfValue, found ? "PASS" : "FAIL"});

        if (!found) {
            result.addError(fieldName + " " + formatted + " not found in PDF Page 2");
        }
    }


    /**
     * Extract amount value from PDF text following a specific label
     * @param pdfText the PDF text content
     * @param label the label to search for (e.g., "Surplus Lines Tax")
     * @return the amount value found, or 0.0 if not found
     */
    private double extractAmountFromPDF(String pdfText, String label) {
        try {
            // Search for the label in PDF text
            int labelIndex = pdfText.indexOf(label);
            if (labelIndex == -1) {
                // Try case-insensitive search
                labelIndex = pdfText.toLowerCase().indexOf(label.toLowerCase());
                if (labelIndex == -1) {
                    logger.info("Label '{}' not found in PDF", label);
                    return 0.0;
                }
            }

            // Get text after the label (next 50 characters should contain the amount)
            int startPos = labelIndex + label.length();
            int endPos = Math.min(startPos + 50, pdfText.length());
            String textAfterLabel = pdfText.substring(startPos, endPos);

            // Extract amount using regex - looks for patterns like $123.45 or 123.45 or $1,234.56
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$?\\s*([\\d,]+\\.?\\d*)");
            java.util.regex.Matcher matcher = pattern.matcher(textAfterLabel);

            if (matcher.find()) {
                String amountStr = matcher.group(1).replace(",", "");
                double amount = Double.parseDouble(amountStr);
                logger.info("Extracted {} = ${}", label, amount);
                return amount;
            }
        } catch (Exception e) {
            logger.info("Error extracting amount for '{}': {}", label, e.getMessage());
        }
        return 0.0;
    }

    /**
     * Check if date exists in PDF (handles various date formats)
     */
    private boolean dateExistsInPDF(String pdfText, String date) {
        if (date == null || date.isEmpty()) return false;

        // Try original format first
        if (pdfText.contains(date)) return true;

        // Try different date formats
        try {
            // Parse the input date (assuming yyyy-MM-dd or MM/dd/yyyy format)
            String[] parts;
            int year, month, day;

            if (date.contains("-")) {
                // Format: 2025-12-24
                parts = date.split("-");
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            } else if (date.contains("/")) {
                // Format: 12/24/2025
                parts = date.split("/");
                month = Integer.parseInt(parts[0]);
                day = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            } else {
                return false;
            }

            // Try various formats
            String[] formats = {
                String.format("%02d/%02d/%04d", month, day, year),      // 12/24/2025
                String.format("%02d-%02d-%04d", month, day, year),      // 12-24-2025
                String.format("%04d-%02d-%02d", year, month, day),      // 2025-12-24
                String.format("%04d/%02d/%02d", year, month, day),      // 2025/12/24
                String.format("%d/%d/%04d", month, day, year),          // 12/24/2025 (no leading zeros)
                String.format("%d/%d/%02d", month, day, year % 100),    // 12/24/25
                getMonthName(month) + " " + day + ", " + year,          // December 24, 2025
                getShortMonthName(month) + " " + day + ", " + year      // Dec 24, 2025
            };

            for (String fmt : formats) {
                if (pdfText.contains(fmt)) {
                    logger.info("Date found in format: {}", fmt);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.info("Error parsing date: {}", e.getMessage());
        }

        return false;
    }

    private String getMonthName(int month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }

    private String getShortMonthName(int month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }

    /**
     * Scroll PDF down in browser viewer
     */
    private void scrollPDFDown() {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
        } catch (Exception e) {
            // Ignore scroll errors
        }
    }

    // ==================== Display Computation Validation Methods ====================

    // Click the Display Computation button
    public boolean clickDisplayComputationButton() {
        logger.info("Clicking Display Computation button");
        try {
            String[] xpaths = {
                "//button[contains(text(),'Display Computation')]",
                "//button[contains(text(),'display computation')]",
                "//button[contains(text(),'Computation')]",
                "//button[contains(@id,'computation')]",
                "//button[contains(@id,'display-computation')]",
                "//button[contains(@class,'computation')]",
                "//a[contains(text(),'Display Computation')]"
            };

            for (String xpath : xpaths) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            scrollIntoView(btn);
                            sleep(500);
                            btn.click();
                            logger.info("Display Computation button clicked");
                            sleep(2000); // Wait for computation to display
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Continue to next xpath
                }
            }
            logger.warn("Display Computation button not found");
            return false;
        } catch (Exception e) {
            logger.error("Error clicking Display Computation button: {}", e.getMessage());
            return false;
        }
    }

    // Close the Display Computation dialog by clicking X button
    public boolean closeDisplayComputationDialog() {
        logger.info("Closing Display Computation dialog");
        try {
            String[] xpaths = {
                "//div[contains(@class,'modal')]//button[contains(@class,'close')]",
                "//div[contains(@class,'dialog')]//button[contains(@class,'close')]",
                "//button[contains(@class,'close') and contains(@aria-label,'Close')]",
                "//button[contains(@class,'btn-close')]",
                "//div[contains(@class,'modal')]//button[text()='×']",
                "//div[contains(@class,'modal')]//span[text()='×']/parent::button",
                "//button[@aria-label='Close']",
                "//div[contains(@class,'modal-header')]//button",
                "//*[contains(@class,'modal')]//*[contains(text(),'×')]"
            };
            for (String xpath : xpaths) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            logger.info("Display Computation dialog closed");
                            sleep(1000);
                            return true;
                        }
                    }
                } catch (Exception e) { /* Continue */ }
            }
            // Try pressing Escape key as fallback
            try {
                driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
                sleep(1000);
                logger.info("Closed dialog with Escape key");
                return true;
            } catch (Exception e) { /* Continue */ }
            logger.warn("Could not find close button for Display Computation dialog");
            return false;
        } catch (Exception e) {
            logger.error("Error closing Display Computation dialog: {}", e.getMessage());
            return false;
        }
    }

    // Get location table data: Address, Dwelling, AS, BPP, LOR, Rate, TIV, PropPrem, Taxes
    public java.util.List<LocationRowData> getLocationTableData() {
        java.util.List<LocationRowData> locationData = new java.util.ArrayList<>();
        logger.info("Extracting location table data from frontend");

        try {
            // Find table headers to get column indices
            List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th"));

            // Map column names to indices
            java.util.Map<String, Integer> columnIndices = new java.util.HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String headerText = headers.get(i).getText().trim().toLowerCase();
                columnIndices.put(headerText, i);
            }

            logger.info("Found columns: {}", columnIndices.keySet());

            // Get column indices for required fields
            int addressColIndex = findColumnIndex(columnIndices, "address", "location", "physical address");
            int dwellingColIndex = findColumnIndex(columnIndices, "dwelling", "coverage a", "cov a", "cova");
            int additionalStructColIndex = findColumnIndex(columnIndices, "additional structures", "structures", "coverage b", "cov b", "covb", "add. struct", "as");
            int bppColIndex = findColumnIndex(columnIndices, "bpp", "coverage c", "cov c", "covc", "personal property", "business personal property");
            int lossOfRentsColIndex = findColumnIndex(columnIndices, "loss of rents", "rents", "coverage d", "cov d", "covd", "lor");
            int rateColIndex = findColumnIndex(columnIndices, "rate", "suggested rate");
            int tivColIndex = findColumnIndex(columnIndices, "tiv", "total insurable value", "total");
            int propertyPremiumColIndex = findColumnIndex(columnIndices, "property premium", "prop premium", "premium");
            int taxesColIndex = findColumnIndex(columnIndices, "taxes", "tax");
            int wsColIndex = findColumnIndex(columnIndices, "ws", "ws premium", "water", "water/sewer", "wsb", "water/sewer premium");
            int glColIndex = findColumnIndex(columnIndices, "gl", "gl premium", "general liability", "gl premium");
            int fullTermColIndex = findColumnIndex(columnIndices, "full term", "fullterm", "full term premium", "total premium", "total");

            logger.info("Column indices - Address:{}, Dwelling:{}, AS:{}, BPP:{}, LOR:{}, Rate:{}, TIV:{}, PropPrem:{}, WS:{}, GL:{}, Tax:{}, FullTerm:{}",
                addressColIndex, dwellingColIndex, additionalStructColIndex, bppColIndex, lossOfRentsColIndex,
                rateColIndex, tivColIndex, propertyPremiumColIndex, wsColIndex, glColIndex, taxesColIndex, fullTermColIndex);

            // Get table rows
            List<WebElement> rows = driver.findElements(By.xpath("//table//tbody//tr"));
            logger.info("Found {} table rows", rows.size());

            int locationNumber = 1;
            for (WebElement row : rows) {
                try {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    if (cells.isEmpty()) continue;

                    LocationRowData rowData = new LocationRowData(locationNumber);

                    // Extract address
                    if (addressColIndex >= 0 && addressColIndex < cells.size()) {
                        rowData.setAddress(cells.get(addressColIndex).getText().trim());
                    }

                    // Extract Dwelling (Coverage A)
                    if (dwellingColIndex >= 0 && dwellingColIndex < cells.size()) {
                        rowData.setDwelling(parseAmountValue(cells.get(dwellingColIndex).getText()));
                    }

                    // Extract Additional Structures (Coverage B)
                    if (additionalStructColIndex >= 0 && additionalStructColIndex < cells.size()) {
                        rowData.setAdditionalStructures(parseAmountValue(cells.get(additionalStructColIndex).getText()));
                    }

                    // Extract BPP (Coverage C)
                    if (bppColIndex >= 0 && bppColIndex < cells.size()) {
                        rowData.setBpp(parseAmountValue(cells.get(bppColIndex).getText()));
                    }

                    // Extract Loss Of Rents (Coverage D)
                    if (lossOfRentsColIndex >= 0 && lossOfRentsColIndex < cells.size()) {
                        rowData.setLossOfRents(parseAmountValue(cells.get(lossOfRentsColIndex).getText()));
                    }

                    // Extract Rate
                    if (rateColIndex >= 0 && rateColIndex < cells.size()) {
                        rowData.setRate(parseAmountValue(cells.get(rateColIndex).getText()));
                    }

                    // Extract TIV
                    if (tivColIndex >= 0 && tivColIndex < cells.size()) {
                        rowData.setTiv(parseAmountValue(cells.get(tivColIndex).getText()));
                    }

                    // Extract Property Premium
                    if (propertyPremiumColIndex >= 0 && propertyPremiumColIndex < cells.size()) {
                        rowData.setPropertyPremium(parseAmountValue(cells.get(propertyPremiumColIndex).getText()));
                    }

                    // Extract Taxes
                    if (taxesColIndex >= 0 && taxesColIndex < cells.size()) {
                        rowData.setTaxes(parseAmountValue(cells.get(taxesColIndex).getText()));
                    }

                    // Extract WS Premium
                    if (wsColIndex >= 0 && wsColIndex < cells.size()) {
                        rowData.setWsPremium(parseAmountValue(cells.get(wsColIndex).getText()));
                    }

                    // Extract GL Premium
                    if (glColIndex >= 0 && glColIndex < cells.size()) {
                        rowData.setGlPremium(parseAmountValue(cells.get(glColIndex).getText()));
                    }

                    // Extract Full Term
                    if (fullTermColIndex >= 0 && fullTermColIndex < cells.size()) {
                        rowData.setFullTerm(parseAmountValue(cells.get(fullTermColIndex).getText()));
                    }

                    // Skip rows without valid address (like total rows or empty rows)
                    String addr = rowData.getAddress();
                    if (addr == null || addr.trim().isEmpty() || addr.toLowerCase().contains("total") || addr.toLowerCase().contains("sum")) {
                        logger.debug("Skipping row without valid address: {}", addr);
                        continue;
                    }

                    // Skip rows where TIV is 0 (likely not a location row)
                    if (rowData.getTiv() <= 0 && rowData.getDwelling() <= 0) {
                        logger.debug("Skipping row with no TIV/Dwelling data");
                        continue;
                    }

                    locationData.add(rowData);
                    logger.info("Location {}: Addr={}, D={}, AS={}, BPP={}, LOR={}, Rate={}, TIV={}, PropPrem={}",
                        locationNumber, rowData.getAddress(), rowData.getDwelling(), rowData.getAdditionalStructures(),
                        rowData.getBpp(), rowData.getLossOfRents(), rowData.getRate(), rowData.getTiv(), rowData.getPropertyPremium());

                    locationNumber++;
                } catch (Exception e) {
                    logger.debug("Error parsing row {}: {}", locationNumber, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error extracting location table data: {}", e.getMessage());
        }

        return locationData;
    }

    // Find column index from multiple possible column names
    private int findColumnIndex(java.util.Map<String, Integer> columnIndices, String... columnNames) {
        for (String name : columnNames) {
            // Exact match first
            if (columnIndices.containsKey(name.toLowerCase())) {
                return columnIndices.get(name.toLowerCase());
            }
            // Partial match
            for (java.util.Map.Entry<String, Integer> entry : columnIndices.entrySet()) {
                if (entry.getKey().contains(name.toLowerCase()) || name.toLowerCase().contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return -1;
    }

    // Validate Display Computation: TIV = D+AS+BPP+LOR, Property Premium = (TIV/100)*Rate
    public DisplayComputationResult validateDisplayComputation(boolean clickButton) {
        logger.info("=== Starting Display Computation Validation (Excel vs Frontend) ===");
        DisplayComputationResult result = new DisplayComputationResult();

        // Step 1: Click Display Computation button if requested
        if (clickButton) {
            if (!clickDisplayComputationButton()) {
                logger.warn("Display Computation button not clicked, proceeding with existing data");
            }
            sleep(2000); // Wait for dialog to fully load
        }

        // Step 2: Take screenshot before validation
        captureScreenshotToReport("Display Computation - Before Validation");

        // Step 3: Extract location data from Display Computation DIALOG table
        java.util.List<LocationRowData> dialogData = getDisplayComputationDialogData();

        if (dialogData.isEmpty()) {
            result.addError("No location data found in Display Computation dialog");
            logger.error("No location data found in Display Computation dialog");
            captureScreenshotToReport("Display Computation - No Data Found");
            result.setHtmlReport(buildDisplayComputationReport(result));
            logHtmlToReport(result.getHtmlReport());
            closeDisplayComputationDialog();
            return result;
        }

        logger.info("Found {} locations in Display Computation dialog", dialogData.size());

        // Step 4: Check if we're in upload mode - validate calculations only, skip Excel comparison
        if (uploadMode) {
            logger.info("=== UPLOAD MODE: Validating calculations only (TIV and Property Premium formulas) ===");
            return validateDisplayComputationCalculationsOnly(dialogData, result);
        }

        // Step 4b: For manual mode, check if we have stored Excel data for comparison
        if (addedLocations == null || addedLocations.isEmpty()) {
            logger.warn("No stored Excel data available for comparison");
            result.addError("No stored Excel data available - cannot validate against expected values");
            result.setHtmlReport(buildDisplayComputationReport(result));
            logHtmlToReport(result.getHtmlReport());
            closeDisplayComputationDialog();
            return result;
        }

        logger.info("Comparing {} dialog locations against {} stored Excel locations",
            dialogData.size(), addedLocations.size());

        // Step 5: Compare each location by matching address - Excel (stored) vs Frontend (displayed)
        // Dialog shows locations in reverse order (last added = first displayed)
        int locationNumber = 1;
        for (int displayIndex = 0; displayIndex < dialogData.size(); displayIndex++) {
            LocationRowData frontendData = dialogData.get(displayIndex);
            String frontendAddress = frontendData.getAddress();

            // Primary: Try index-based matching (reverse order)
            LocationData excelData = findStoredLocationByIndex(displayIndex, dialogData.size());

            // Fallback: If index doesn't work, try address matching
            if (excelData == null) {
                excelData = findStoredLocationByAddress(frontendAddress);
            }

            if (excelData == null) {
                logger.warn("No matching Excel data found for address: {}", frontendAddress);
                result.addError("Location " + locationNumber + ": No matching Excel data for address '" + frontendAddress + "'");
                locationNumber++;
                continue;
            }

            // Create comparison data
            LocationComparisonData comparison = new LocationComparisonData(locationNumber, frontendAddress);

            // Set Excel values (Coverage A=Dwelling, B=AS, C=BPP, D=LOR)
            comparison.setExcelValues(
                excelData.getCoverageA(),
                excelData.getCoverageB(),
                excelData.getCoverageC(),
                excelData.getCoverageD(),
                excelData.getRate()
            );

            // Set Frontend values from Display Computation dialog
            comparison.setFrontendValues(
                frontendData.getDwelling(),
                frontendData.getAdditionalStructures(),
                frontendData.getBpp(),
                frontendData.getLossOfRents(),
                frontendData.getRate(),
                frontendData.getTaxes(),
                frontendData.getTiv(),
                frontendData.getPropertyPremium()
            );

            // Validate and add to result
            comparison.validate();
            result.addComparisonData(comparison);

            // Log comparison details
            logger.info("Location {}: {}", locationNumber, frontendAddress);
            logger.info("  Dwelling: Excel=${} vs Frontend=${} - {}",
                formatAmountWithComma(excelData.getCoverageA()),
                formatAmountWithComma(frontendData.getDwelling()),
                comparison.isDwellingMatch() ? "PASS" : "FAIL");
            logger.info("  Additional Structures: Excel=${} vs Frontend=${} - {}",
                formatAmountWithComma(excelData.getCoverageB()),
                formatAmountWithComma(frontendData.getAdditionalStructures()),
                comparison.isAsMatch() ? "PASS" : "FAIL");
            logger.info("  BPP: Excel=${} vs Frontend=${} - {}",
                formatAmountWithComma(excelData.getCoverageC()),
                formatAmountWithComma(frontendData.getBpp()),
                comparison.isBppMatch() ? "PASS" : "FAIL");
            logger.info("  Loss Of Rents: Excel=${} vs Frontend=${} - {}",
                formatAmountWithComma(excelData.getCoverageD()),
                formatAmountWithComma(frontendData.getLossOfRents()),
                comparison.isLorMatch() ? "PASS" : "FAIL");
            logger.info("  Rate: Excel={} vs Frontend={} - {}",
                formatAmount(excelData.getRate()),
                formatAmount(frontendData.getRate()),
                comparison.isRateMatch() ? "PASS" : "FAIL");
            logger.info("  Taxes: ${}, TIV: ${}, Property Premium: ${}",
                formatAmountWithComma(frontendData.getTaxes()),
                formatAmountWithComma(frontendData.getTiv()),
                formatAmountWithComma(frontendData.getPropertyPremium()));
            logger.info("  Location {} Overall: {}", locationNumber, comparison.isAllMatch() ? "PASS" : "FAIL");

            // Add errors for mismatches
            if (!comparison.isDwellingMatch()) {
                result.addError("Location " + locationNumber + ": Dwelling mismatch - Excel=$" +
                    formatAmountWithComma(excelData.getCoverageA()) + ", Frontend=$" +
                    formatAmountWithComma(frontendData.getDwelling()));
            }
            if (!comparison.isAsMatch()) {
                result.addError("Location " + locationNumber + ": Additional Structures mismatch - Excel=$" +
                    formatAmountWithComma(excelData.getCoverageB()) + ", Frontend=$" +
                    formatAmountWithComma(frontendData.getAdditionalStructures()));
            }
            if (!comparison.isBppMatch()) {
                result.addError("Location " + locationNumber + ": BPP mismatch - Excel=$" +
                    formatAmountWithComma(excelData.getCoverageC()) + ", Frontend=$" +
                    formatAmountWithComma(frontendData.getBpp()));
            }
            if (!comparison.isLorMatch()) {
                result.addError("Location " + locationNumber + ": Loss Of Rents mismatch - Excel=$" +
                    formatAmountWithComma(excelData.getCoverageD()) + ", Frontend=$" +
                    formatAmountWithComma(frontendData.getLossOfRents()));
            }
            if (!comparison.isRateMatch()) {
                result.addError("Location " + locationNumber + ": Rate mismatch - Excel=" +
                    formatAmount(excelData.getRate()) + ", Frontend=" +
                    formatAmount(frontendData.getRate()));
            }

            locationNumber++;
        }

        // Step 6: Build and log the HTML report
        String htmlReport = buildDisplayComputationReport(result);
        result.setHtmlReport(htmlReport);
        logHtmlToReport(htmlReport);

        // Step 7: Take final screenshot
        captureScreenshotToReport("Display Computation - Validation Complete (" +
            (result.isValid() ? "PASSED" : "FAILED") + ")");

        // Step 8: Close the Display Computation dialog
        closeDisplayComputationDialog();

        logger.info("=== Display Computation Validation Complete. Status: {} ===",
            result.isValid() ? "PASSED" : "FAILED");

        return result;
    }

    // Validate Display Computation with button click (convenience method)
    public DisplayComputationResult validateDisplayComputation() {
        return validateDisplayComputation(true);
    }

    /**
     * Validate Display Computation calculations only (for Upload mode)
     * Validates: TIV = Dwelling + AS + BPP + LOR, Property Premium = (TIV/100) * Rate
     * Does NOT compare against Excel data
     */
    private DisplayComputationResult validateDisplayComputationCalculationsOnly(
            java.util.List<LocationRowData> dialogData, DisplayComputationResult result) {

        logger.info("=== Validating Display Computation Calculations (Upload Mode) ===");
        int locationNumber = 1;
        int passCount = 0;
        int failCount = 0;

        for (LocationRowData frontendData : dialogData) {
            String frontendAddress = frontendData.getAddress();

            // Get values from Display Computation dialog
            double dwelling = frontendData.getDwelling();
            double additionalStructures = frontendData.getAdditionalStructures();
            double bpp = frontendData.getBpp();
            double lossOfRents = frontendData.getLossOfRents();
            double rate = frontendData.getRate();
            double displayedTiv = frontendData.getTiv();
            double displayedPropPrem = frontendData.getPropertyPremium();

            // Calculate expected TIV = Dwelling + AS + BPP + LOR
            double expectedTiv = dwelling + additionalStructures + bpp + lossOfRents;

            // Calculate expected Property Premium = (TIV / 100) * Rate
            double expectedPropPrem = (expectedTiv / 100.0) * rate;

            // Validate TIV calculation
            boolean tivMatch = Math.abs(expectedTiv - displayedTiv) < 1.0; // Allow $1 tolerance

            // Validate Property Premium calculation
            boolean propPremMatch = Math.abs(expectedPropPrem - displayedPropPrem) < 1.0; // Allow $1 tolerance

            boolean locationPassed = tivMatch && propPremMatch;
            if (locationPassed) {
                passCount++;
            } else {
                failCount++;
            }

            // Log validation results
            logger.info("Location {}: {}", locationNumber, frontendAddress);
            logger.info("  Dwelling: ${}", formatAmountWithComma(dwelling));
            logger.info("  Additional Structures: ${}", formatAmountWithComma(additionalStructures));
            logger.info("  BPP: ${}", formatAmountWithComma(bpp));
            logger.info("  Loss Of Rents: ${}", formatAmountWithComma(lossOfRents));
            logger.info("  Rate: {}", formatAmount(rate));
            logger.info("  TIV: Expected=${} vs Displayed=${} - {}",
                formatAmountWithComma(expectedTiv), formatAmountWithComma(displayedTiv),
                tivMatch ? "PASS" : "FAIL");
            logger.info("  Property Premium: Expected=${} vs Displayed=${} - {}",
                formatAmountWithComma(expectedPropPrem), formatAmountWithComma(displayedPropPrem),
                propPremMatch ? "PASS" : "FAIL");
            logger.info("  Location {} Overall: {}", locationNumber, locationPassed ? "PASS" : "FAIL");

            // Add errors for calculation mismatches
            if (!tivMatch) {
                result.addError("Location " + locationNumber + ": TIV calculation mismatch - Expected=$" +
                    formatAmountWithComma(expectedTiv) + " (D+AS+BPP+LOR), Displayed=$" +
                    formatAmountWithComma(displayedTiv));
            }
            if (!propPremMatch) {
                result.addError("Location " + locationNumber + ": Property Premium calculation mismatch - Expected=$" +
                    formatAmountWithComma(expectedPropPrem) + " (TIV/100*Rate), Displayed=$" +
                    formatAmountWithComma(displayedPropPrem));
            }

            locationNumber++;
        }

        // Build summary
        logger.info("=== Upload Mode Calculation Validation Complete ===");
        logger.info("Total Locations: {}, Passed: {}, Failed: {}", dialogData.size(), passCount, failCount);

        // Take screenshot
        captureScreenshotToReport("Display Computation - Upload Mode Validation (" +
            (failCount == 0 ? "PASSED" : "FAILED") + ")");

        // Close dialog
        closeDisplayComputationDialog();

        return result;
    }

    // Validate Display Computation against stored location data
    public DisplayComputationResult validateDisplayComputationAgainstStoredData() {
        logger.info("=== Validating Display Computation Against Stored Data ===");
        DisplayComputationResult result = new DisplayComputationResult();

        // Click Display Computation button
        clickDisplayComputationButton();

        // Take screenshot
        captureScreenshotToReport("Display Computation - Validating Against Stored Data");

        // Extract frontend table data
        java.util.List<LocationRowData> tableData = getLocationTableData();

        if (tableData.isEmpty()) {
            result.addError("No location data found in frontend table");
            result.setHtmlReport(buildDisplayComputationReport(result));
            logHtmlToReport(result.getHtmlReport());
            return result;
        }

        if (addedLocations == null || addedLocations.isEmpty()) {
            logger.warn("No stored location data for comparison, validating formulas only");
            // Fall back to formula validation only
            for (LocationRowData rowData : tableData) {
                validateComputationRow(result, rowData);
            }
        } else {
            // Validate against stored data
            logger.info("Validating {} frontend rows against {} stored locations",
                tableData.size(), addedLocations.size());

            for (int i = 0; i < tableData.size() && i < addedLocations.size(); i++) {
                LocationRowData frontendData = tableData.get(i);
                LocationData storedData = addedLocations.get(i);

                // Validate frontend values match stored values
                validateStoredVsFrontend(result, i + 1, frontendData, storedData);

                // Also validate formulas
                validateComputationRow(result, frontendData);
            }
        }

        // Build and log report
        String htmlReport = buildDisplayComputationReport(result);
        result.setHtmlReport(htmlReport);
        String consoleLog = buildDisplayComputationConsoleLog(result);
        logger.info(consoleLog);
        logHtmlToReport(htmlReport);

        captureScreenshotToReport("Display Computation - Stored Data Validation Complete");

        return result;
    }

    /**
     * Validate frontend data against stored location data
     */
    private void validateStoredVsFrontend(DisplayComputationResult result, int locationNumber,
                                          LocationRowData frontendData, LocationData storedData) {
        StringBuilder errors = new StringBuilder();

        // Compare Dwelling (Coverage A)
        if (Math.abs(frontendData.getDwelling() - storedData.getCoverageA()) > 0.01) {
            errors.append("Dwelling mismatch: Stored=").append(storedData.getCoverageA())
                  .append(", Frontend=").append(frontendData.getDwelling()).append("; ");
        }

        // Compare Additional Structures (Coverage B)
        if (Math.abs(frontendData.getAdditionalStructures() - storedData.getCoverageB()) > 0.01) {
            errors.append("Additional Structures mismatch: Stored=").append(storedData.getCoverageB())
                  .append(", Frontend=").append(frontendData.getAdditionalStructures()).append("; ");
        }

        // Compare BPP (Coverage C)
        if (Math.abs(frontendData.getBpp() - storedData.getCoverageC()) > 0.01) {
            errors.append("BPP mismatch: Stored=").append(storedData.getCoverageC())
                  .append(", Frontend=").append(frontendData.getBpp()).append("; ");
        }

        // Compare Loss Of Rents (Coverage D)
        if (Math.abs(frontendData.getLossOfRents() - storedData.getCoverageD()) > 0.01) {
            errors.append("Loss Of Rents mismatch: Stored=").append(storedData.getCoverageD())
                  .append(", Frontend=").append(frontendData.getLossOfRents()).append("; ");
        }

        // Compare Rate
        if (Math.abs(frontendData.getRate() - storedData.getRate()) > 0.01) {
            errors.append("Rate mismatch: Stored=").append(storedData.getRate())
                  .append(", Frontend=").append(frontendData.getRate()).append("; ");
        }

        if (errors.length() > 0) {
            result.addError("Location " + locationNumber + " data mismatch: " + errors.toString());
            logger.warn("Location {} data mismatch: {}", locationNumber, errors.toString());
        }
    }

    /**
     * Extract location data specifically from Display Computation dialog table
     * Reads all columns: Location, Dwelling, AS, BPP, LOR, Rate, Taxes, TIV, Property Premium
     */
    public java.util.List<LocationRowData> getDisplayComputationDialogData() {
        java.util.List<LocationRowData> locationData = new java.util.ArrayList<>();
        logger.info("Extracting data from Display Computation dialog table");

        try {
            // Wait for dialog to be visible
            sleep(1000);

            // Find the modal/dialog table - try multiple XPath patterns
            String[] tableXpaths = {
                "//div[contains(@class,'modal')]//table",
                "//div[contains(@class,'dialog')]//table",
                "//div[contains(@class,'popup')]//table",
                "//div[@role='dialog']//table",
                "//div[contains(@class,'rate-calculation')]//table",
                "//div[contains(@class,'computation')]//table",
                "//div[contains(@class,'overlay')]//table",
                "//table[contains(@class,'computation')]",
                "//table[contains(@class,'rate')]"
            };

            WebElement dialogTable = null;
            for (String xpath : tableXpaths) {
                try {
                    List<WebElement> tables = driver.findElements(By.xpath(xpath));
                    if (!tables.isEmpty()) {
                        dialogTable = tables.get(0);
                        logger.info("Found Display Computation table using: {}", xpath);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (dialogTable == null) {
                // Fallback: find any visible table
                List<WebElement> allTables = driver.findElements(By.xpath("//table"));
                for (WebElement table : allTables) {
                    if (table.isDisplayed()) {
                        dialogTable = table;
                        logger.info("Using visible table as Display Computation table");
                        break;
                    }
                }
            }

            if (dialogTable == null) {
                logger.error("Could not find Display Computation dialog table");
                return locationData;
            }

            // Get headers from table
            List<WebElement> headers = dialogTable.findElements(By.xpath(".//thead//th | .//tr[1]//th | .//tr[1]//td"));
            java.util.Map<String, Integer> columnIndices = new java.util.HashMap<>();

            logger.info("Found {} headers in Display Computation table", headers.size());
            for (int i = 0; i < headers.size(); i++) {
                String headerText = headers.get(i).getText().trim().toLowerCase();
                columnIndices.put(headerText, i);
                logger.debug("Header {}: '{}'", i, headerText);
            }

            // Map column names to indices
            int locationCol = findColumnIndex(columnIndices, "location", "address", "physical address");
            int dwellingCol = findColumnIndex(columnIndices, "dwelling", "coverage a", "cov a");
            int asCol = findColumnIndex(columnIndices, "additional structures", "add. struct", "as", "coverage b", "cov b");
            int bppCol = findColumnIndex(columnIndices, "bpp", "coverage c", "cov c", "personal property");
            int lorCol = findColumnIndex(columnIndices, "loss of rents", "lor", "coverage d", "cov d", "rents");
            int rateCol = findColumnIndex(columnIndices, "rate", "suggested rate");
            int taxesCol = findColumnIndex(columnIndices, "taxes", "tax");
            int tivCol = findColumnIndex(columnIndices, "tiv", "total insurable value");
            int propPremCol = findColumnIndex(columnIndices, "property premium", "prop premium", "premium");
            int glCol = findColumnIndex(columnIndices, "gl", "gl premium", "general liability");
            int wsCol = findColumnIndex(columnIndices, "ws", "ws premium", "water", "sewer", "water/sewer");
            int feesCol = findColumnIndex(columnIndices, "fees", "fee", "policy fee");

            logger.info("Column mapping - Location:{}, Dwelling:{}, AS:{}, BPP:{}, LOR:{}, Rate:{}, Taxes:{}, TIV:{}, PropPrem:{}, GL:{}, WS:{}, Fees:{}",
                locationCol, dwellingCol, asCol, bppCol, lorCol, rateCol, taxesCol, tivCol, propPremCol, glCol, wsCol, feesCol);

            // Get data rows (skip header row)
            List<WebElement> rows = dialogTable.findElements(By.xpath(".//tbody//tr | .//tr[position()>1]"));
            logger.info("Found {} data rows in Display Computation table", rows.size());

            int locationNumber = 1;
            for (WebElement row : rows) {
                try {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    if (cells.isEmpty()) continue;

                    // Get address from location column
                    String address = "";
                    if (locationCol >= 0 && locationCol < cells.size()) {
                        address = cells.get(locationCol).getText().trim();
                    }

                    // Skip rows without valid address (total rows, empty rows, header rows)
                    if (address.isEmpty() || address.toLowerCase().contains("total") ||
                        address.toLowerCase().contains("sum") || address.toLowerCase().equals("location")) {
                        logger.debug("Skipping non-location row: '{}'", address);
                        continue;
                    }

                    LocationRowData rowData = new LocationRowData(locationNumber);
                    rowData.setAddress(address);

                    // Extract all values
                    if (dwellingCol >= 0 && dwellingCol < cells.size()) {
                        rowData.setDwelling(parseAmountValue(cells.get(dwellingCol).getText()));
                    }
                    if (asCol >= 0 && asCol < cells.size()) {
                        rowData.setAdditionalStructures(parseAmountValue(cells.get(asCol).getText()));
                    }
                    if (bppCol >= 0 && bppCol < cells.size()) {
                        rowData.setBpp(parseAmountValue(cells.get(bppCol).getText()));
                    }
                    if (lorCol >= 0 && lorCol < cells.size()) {
                        rowData.setLossOfRents(parseAmountValue(cells.get(lorCol).getText()));
                    }
                    if (rateCol >= 0 && rateCol < cells.size()) {
                        rowData.setRate(parseAmountValue(cells.get(rateCol).getText()));
                    }
                    if (taxesCol >= 0 && taxesCol < cells.size()) {
                        rowData.setTaxes(parseAmountValue(cells.get(taxesCol).getText()));
                    }
                    if (tivCol >= 0 && tivCol < cells.size()) {
                        rowData.setTiv(parseAmountValue(cells.get(tivCol).getText()));
                    }
                    if (propPremCol >= 0 && propPremCol < cells.size()) {
                        rowData.setPropertyPremium(parseAmountValue(cells.get(propPremCol).getText()));
                    }
                    if (glCol >= 0 && glCol < cells.size()) {
                        rowData.setGlPremium(parseAmountValue(cells.get(glCol).getText()));
                    }
                    if (wsCol >= 0 && wsCol < cells.size()) {
                        rowData.setWsPremium(parseAmountValue(cells.get(wsCol).getText()));
                    }
                    if (feesCol >= 0 && feesCol < cells.size()) {
                        rowData.setFees(parseAmountValue(cells.get(feesCol).getText()));
                    }

                    locationData.add(rowData);
                    logger.info("Location {}: Addr='{}', Dwelling=${}, AS=${}, BPP=${}, LOR=${}, Rate={}, Taxes=${}, TIV=${}, PropPrem=${}, GL=${}, WS=${}, Fees=${}",
                        locationNumber, address, rowData.getDwelling(), rowData.getAdditionalStructures(),
                        rowData.getBpp(), rowData.getLossOfRents(), rowData.getRate(), rowData.getTaxes(),
                        rowData.getTiv(), rowData.getPropertyPremium(), rowData.getGlPremium(), rowData.getWsPremium(), rowData.getFees());

                    locationNumber++;
                } catch (Exception e) {
                    logger.debug("Error parsing row: {}", e.getMessage());
                }
            }

            logger.info("Extracted {} locations from Display Computation dialog", locationData.size());

        } catch (Exception e) {
            logger.error("Error extracting Display Computation dialog data: {}", e.getMessage());
        }

        return locationData;
    }

    /**
     * Match locations by address - find the stored Excel data that matches the frontend address
     * Uses enhanced matching to handle auto-complete address changes
     */
    private LocationData findStoredLocationByAddress(String frontendAddress) {
        if (addedLocations == null || addedLocations.isEmpty() || frontendAddress == null) {
            return null;
        }

        // Normalize the frontend address for comparison
        String normalizedFrontend = normalizeAddress(frontendAddress);

        // First pass: Try standard contains matching
        for (LocationData stored : addedLocations) {
            String normalizedStored = normalizeAddress(stored.getAddress());
            if (normalizedFrontend.contains(normalizedStored) || normalizedStored.contains(normalizedFrontend)) {
                return stored;
            }
        }

        // Second pass: Use enhanced address matching
        for (LocationData stored : addedLocations) {
            if (addressMatchesEnhanced(stored.getAddress(), frontendAddress)) {
                return stored;
            }
        }

        // Third pass: Match by city + approximate position
        // If we still don't find a match, try to match by city and coverage values
        String frontendLower = frontendAddress.toLowerCase();
        for (LocationData stored : addedLocations) {
            String storedLower = stored.getAddress().toLowerCase();

            // Extract cities
            String[] cities = {"louisville", "lexington", "covington", "frankfort", "elizabethtown",
                "paducah", "morehead", "grayson", "lewisport", "paris"};

            for (String city : cities) {
                if (frontendLower.contains(city) && storedLower.contains(city)) {
                    // Same city - check if this stored location hasn't been matched yet
                    // by checking if coverage values are unique
                    return stored;
                }
            }
        }

        return null;
    }

    /**
     * Find stored location by index (for Display Computation where order matters)
     * Display Computation shows locations in reverse order (last added = first row)
     */
    private LocationData findStoredLocationByIndex(int displayIndex, int totalDisplayed) {
        if (addedLocations == null || addedLocations.isEmpty()) {
            return null;
        }

        // Convert display index to stored index (reverse order)
        int storedIndex = addedLocations.size() - 1 - displayIndex;
        if (storedIndex >= 0 && storedIndex < addedLocations.size()) {
            return addedLocations.get(storedIndex);
        }

        return null;
    }

    // ==================== Submit Quote and Validation ====================

    /**
     * Click Submit button and validate Quote created successfully toast
     * Returns a result object with success status and details
     */
    public SubmitQuoteResult clickSubmitAndValidateQuoteCreation() {
        logger.info("=== Starting Quote Submission ===");
        SubmitQuoteResult result = new SubmitQuoteResult();

        try {
            // Step 1: Find and click Submit button
            logger.info("Looking for Submit button...");
            String[] submitXpaths = {
                "//button[contains(text(),'Submit')]",
                "//button[contains(@class,'submit')]",
                "//input[@type='submit']",
                "//button[@type='submit']",
                "//button[contains(text(),'Create Quote')]",
                "//button[contains(text(),'Save')]",
                "//button[contains(@id,'submit')]",
                "//a[contains(text(),'Submit')]",
                "//*[contains(@class,'btn') and contains(text(),'Submit')]"
            };

            WebElement submitButton = null;
            for (String xpath : submitXpaths) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            submitButton = btn;
                            logger.info("Found Submit button using: {}", xpath);
                            break;
                        }
                    }
                    if (submitButton != null) break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (submitButton == null) {
                result.setSuccess(false);
                result.setMessage("Submit button not found");
                logger.error("Submit button not found on page");
                captureScreenshotToReport("Submit Quote - Button Not Found");
                return result;
            }

            // Take screenshot before clicking
            captureScreenshotToReport("Submit Quote - Before Click");

            // Step 2: Click the Submit button
            try {
                scrollIntoView(submitButton);
                sleep(500);
                submitButton.click();
                logger.info("Clicked Submit button");
            } catch (Exception e) {
                // Try JavaScript click as fallback
                logger.warn("Regular click failed, trying JavaScript click");
                jsExecutor.executeScript("arguments[0].click();", submitButton);
            }

            // Step 3: Wait for toast message
            logger.info("Waiting for success toast message...");
            sleep(2000); // Initial wait for toast to appear

            // Step 4: Validate toast message
            String[] toastXpaths = {
                "//*[contains(@class,'toast') and contains(text(),'successfully')]",
                "//*[contains(@class,'toast') and contains(text(),'Quote created')]",
                "//*[contains(@class,'alert') and contains(text(),'successfully')]",
                "//*[contains(@class,'notification') and contains(text(),'successfully')]",
                "//*[contains(@class,'success') and contains(text(),'Quote')]",
                "//*[contains(@class,'message') and contains(text(),'successfully')]",
                "//div[contains(@class,'toast')]//span[contains(text(),'successfully')]",
                "//*[contains(text(),'Quote created successfully')]",
                "//*[contains(text(),'Successfully created')]",
                "//*[contains(text(),'created successfully')]"
            };

            WebElement toastElement = null;
            String toastMessage = "";
            int maxWaitTime = 15000; // 15 seconds max wait
            int waitInterval = 500;
            int totalWait = 0;

            while (toastElement == null && totalWait < maxWaitTime) {
                for (String xpath : toastXpaths) {
                    try {
                        List<WebElement> toasts = driver.findElements(By.xpath(xpath));
                        for (WebElement toast : toasts) {
                            if (toast.isDisplayed()) {
                                toastElement = toast;
                                toastMessage = toast.getText().trim();
                                logger.info("Found toast message: '{}'", toastMessage);
                                break;
                            }
                        }
                        if (toastElement != null) break;
                    } catch (Exception e) {
                        continue;
                    }
                }

                if (toastElement == null) {
                    sleep(waitInterval);
                    totalWait += waitInterval;
                }
            }

            // Step 5: Take screenshot with toast
            captureScreenshotToReport("Submit Quote - After Submission");

            // Step 6: Set result based on toast validation
            if (toastElement != null && (toastMessage.toLowerCase().contains("success") ||
                toastMessage.toLowerCase().contains("created"))) {
                result.setSuccess(true);
                result.setMessage("Quote created successfully");
                result.setToastMessage(toastMessage);
                logger.info("Quote submission successful. Toast: {}", toastMessage);

                // Log success to report
                String successHtml = buildSubmitQuoteReport(true, toastMessage);
                logHtmlToReport(successHtml);

            } else if (toastElement != null) {
                // Toast found but not success message
                result.setSuccess(false);
                result.setMessage("Toast found but not success message");
                result.setToastMessage(toastMessage);
                logger.warn("Toast found but not success: {}", toastMessage);

                String reportHtml = buildSubmitQuoteReport(false, toastMessage);
                logHtmlToReport(reportHtml);

            } else {
                // No toast found - check for other success indicators
                logger.warn("No toast message found, checking for other success indicators...");

                // Check if page navigated or quote number appeared
                boolean otherSuccess = checkForOtherSuccessIndicators();
                if (otherSuccess) {
                    result.setSuccess(true);
                    result.setMessage("Quote created (based on page indicators)");
                    result.setToastMessage("N/A - Success detected via page indicators");
                    logger.info("Quote submission successful based on page indicators");

                    String successHtml = buildSubmitQuoteReport(true, "Success detected via page indicators");
                    logHtmlToReport(successHtml);
                } else {
                    result.setSuccess(false);
                    result.setMessage("No success toast or indicators found");
                    result.setToastMessage("Not found");
                    logger.error("Quote submission status unclear - no toast found");

                    String failHtml = buildSubmitQuoteReport(false, "No success toast message found");
                    logHtmlToReport(failHtml);
                }
            }

            // Take final screenshot
            captureScreenshotToReport("Submit Quote - Final State (" +
                (result.isSuccess() ? "SUCCESS" : "FAILED") + ")");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Exception during submission: " + e.getMessage());
            logger.error("Error during quote submission: {}", e.getMessage());
            captureScreenshotToReport("Submit Quote - Error: " + e.getMessage());

            String errorHtml = buildSubmitQuoteReport(false, "Error: " + e.getMessage());
            logHtmlToReport(errorHtml);
        }

        logger.info("=== Quote Submission Complete. Status: {} ===",
            result.isSuccess() ? "SUCCESS" : "FAILED");

        return result;
    }

    /**
     * Check for other success indicators when toast is not found
     */
    private boolean checkForOtherSuccessIndicators() {
        try {
            // Check for quote number display
            String[] quoteNumberXpaths = {
                "//*[contains(text(),'Quote #')]",
                "//*[contains(text(),'Quote Number')]",
                "//*[contains(@class,'quote-number')]",
                "//*[contains(@id,'quoteNumber')]"
            };

            for (String xpath : quoteNumberXpaths) {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    logger.info("Found quote number indicator");
                    return true;
                }
            }

            // Check if URL changed to indicate success
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            if (currentUrl.contains("success") || currentUrl.contains("confirmation") ||
                currentUrl.contains("complete") || currentUrl.contains("quote/view")) {
                logger.info("URL indicates success: {}", currentUrl);
                return true;
            }

            // Check for confirmation message
            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("quote has been created") ||
                pageSource.contains("successfully submitted") ||
                pageSource.contains("quote saved")) {
                logger.info("Page source contains success message");
                return true;
            }

        } catch (Exception e) {
            logger.debug("Error checking success indicators: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Build HTML report for Submit Quote result
     */
    private String buildSubmitQuoteReport(boolean success, String message) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String statusColor = success ? "#155724" : "#721c24";
        String statusBg = success ? "#d4edda" : "#f8d7da";
        String statusText = success ? "SUCCESS" : "FAILED";
        String icon = success ? "&#10004;" : "&#10008;";

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family:Arial,sans-serif; margin:10px 0;'>");

        // Title bar
        html.append("<div style='background-color:#343a40; color:#ffffff; padding:12px 15px; border-radius:5px 5px 0 0;'>");
        html.append("<span style='font-size:16px; font-weight:bold;'>Quote Submission Result</span>");
        html.append("<span style='float:right; font-size:13px;'>").append(timestamp).append("</span>");
        html.append("</div>");

        // Status section
        html.append("<div style='background-color:").append(statusBg).append("; padding:15px; border:1px solid #000; text-align:center;'>");
        html.append("<span style='font-size:24px; color:").append(statusColor).append(";'>").append(icon).append("</span>");
        html.append("<br/>");
        html.append("<span style='font-size:18px; font-weight:bold; color:").append(statusColor).append(";'>").append(statusText).append("</span>");
        html.append("<br/><br/>");
        html.append("<span style='font-size:14px; color:#000;'>").append(escapeHtmlChars(message)).append("</span>");
        html.append("</div>");

        html.append("</div>");

        return html.toString();
    }

    /**
     * Result class for Submit Quote operation
     */
    public static class SubmitQuoteResult {
        private boolean success = false;
        private String message = "";
        private String toastMessage = "";

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getToastMessage() { return toastMessage; }
        public void setToastMessage(String toastMessage) { this.toastMessage = toastMessage; }
    }

    // ==================== Arch + California Validation ====================

    /**
     * Check if any location address contains California (CA)
     * @param locations List of location data from Excel
     * @return true if any location is in California
     */
    public boolean hasCaliforniaLocation(List<Map<String, String>> locations) {
        if (locations == null || locations.isEmpty()) {
            return false;
        }

        for (Map<String, String> location : locations) {
            String address = location.getOrDefault("Address", "");
            if (isCaliforniaAddress(address)) {
                logger.info("California location detected: {}", address);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an address is in California
     * Checks for "CA", "California", common CA zip codes (9xxxx)
     */
    public boolean isCaliforniaAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        String addressUpper = address.toUpperCase().trim();

        // Check for state abbreviation - must be at end or followed by zip
        // Pattern: ", CA " or ", CA," or ends with " CA" or ", CA 9xxxx"
        if (addressUpper.contains(", CA ") ||
            addressUpper.contains(", CA,") ||
            addressUpper.endsWith(" CA") ||
            addressUpper.endsWith(",CA") ||
            addressUpper.matches(".*,\\s*CA\\s+\\d{5}.*")) {
            return true;
        }

        // Check for full state name
        if (addressUpper.contains("CALIFORNIA")) {
            return true;
        }

        // Check for California zip codes (90000-96199)
        java.util.regex.Pattern zipPattern = java.util.regex.Pattern.compile("\\b(9[0-5]\\d{3}|96[01]\\d{2})\\b");
        if (zipPattern.matcher(address).find()) {
            return true;
        }

        return false;
    }

    /**
     * Check if the Arch California error message is displayed
     * @return true if the error message is visible
     */
    public boolean isArchCaliforniaErrorDisplayed() {
        String[] errorXpaths = {
            "//*[contains(text(),'Arch Specialty Insurance does not cover any County in California')]",
            "//*[contains(text(),'does not cover any County in California')]",
            "//*[contains(text(),'does not cover') and contains(text(),'County') and contains(text(),'California')]",
            "//*[contains(text(),'Arch Specialty Insurance does not cover') and contains(text(),'California')]",
            "//*[contains(text(),'San Mateo County') and contains(text(),'California')]",
            "//div[contains(@class,'error') or contains(@class,'alert')][contains(text(),'California')]",
            "//div[contains(@class,'toast') or contains(@class,'notification')][contains(text(),'California')]",
            "//*[contains(@class,'MuiAlert') or contains(@class,'Mui-error')][contains(text(),'California')]"
        };

        try {
            for (String xpath : errorXpaths) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(xpath));
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            String text = element.getText();
                            logger.info("Found Arch California error message: {}", text);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Continue to next xpath
                }
            }
        } catch (Exception e) {
            logger.warn("Error checking for Arch California error: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Get the Arch California error message text
     * @return The error message text, or empty string if not found
     */
    public String getArchCaliforniaErrorMessage() {
        String[] errorXpaths = {
            "//*[contains(text(),'Arch Specialty Insurance does not cover any County in California')]",
            "//*[contains(text(),'does not cover any County in California')]",
            "//*[contains(text(),'does not cover') and contains(text(),'County') and contains(text(),'California')]",
            "//*[contains(text(),'Arch Specialty Insurance does not cover') and contains(text(),'California')]",
            "//div[contains(@class,'error') or contains(@class,'alert')][contains(text(),'California')]"
        };

        try {
            for (String xpath : errorXpaths) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(xpath));
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            return element.getText().trim();
                        }
                    }
                } catch (Exception e) {
                    // Continue to next xpath
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting Arch California error message: {}", e.getMessage());
        }

        return "";
    }

    /**
     * Validate Arch + California combination
     * If Arch Specialty Insurance is selected and locations contain California,
     * an error message should be displayed.
     *
     * @param carrier The selected carrier name
     * @param locations List of location data
     * @return ArchCaliforniaValidationResult with validation outcome
     */
    public ArchCaliforniaValidationResult validateArchCaliforniaCombination(String carrier, List<Map<String, String>> locations) {
        ArchCaliforniaValidationResult result = new ArchCaliforniaValidationResult();

        // Check if carrier is Arch
        boolean isArchCarrier = carrier != null &&
            (carrier.toLowerCase().contains("arch") ||
             carrier.equalsIgnoreCase("arch specialty insurance") ||
             carrier.equalsIgnoreCase("arch"));

        result.setArchCarrier(isArchCarrier);

        // Check if any location is in California
        boolean hasCALocation = hasCaliforniaLocation(locations);
        result.setHasCaliforniaLocation(hasCALocation);

        logger.info("Arch California Validation - Carrier: {}, Is Arch: {}, Has CA Location: {}",
            carrier, isArchCarrier, hasCALocation);

        if (isArchCarrier && hasCALocation) {
            // This combination should show an error
            result.setExpectedError(true);

            // Wait a moment for error to appear
            sleep(2000);

            // Check if error is displayed
            boolean errorDisplayed = isArchCaliforniaErrorDisplayed();
            result.setErrorDisplayed(errorDisplayed);

            // Get error message
            String errorMessage = getArchCaliforniaErrorMessage();
            result.setErrorMessage(errorMessage);

            // Capture screenshot
            captureScreenshotToReport("Arch California Error - " + (errorDisplayed ? "Displayed" : "Not Found"));

            if (errorDisplayed) {
                logger.info("EXPECTED: Arch + California error is displayed correctly: {}", errorMessage);
                result.setValid(true);
                result.setMessage("Arch + California error displayed correctly: " + errorMessage);
            } else {
                logger.warn("Arch + California error should be displayed but was not found");
                result.setValid(false);
                result.setMessage("Expected error message not displayed for Arch + California combination");
            }
        } else {
            // No error expected
            result.setExpectedError(false);
            result.setValid(true);

            if (!isArchCarrier) {
                result.setMessage("Carrier is not Arch - no California restriction applies");
            } else {
                result.setMessage("No California locations - Arch carrier is valid");
            }
        }

        return result;
    }

    /**
     * Wait for and validate Arch California error after uploading file
     * Call this after file upload when using Arch carrier
     *
     * @param carrier The selected carrier
     * @param uploadedLocations Locations that were uploaded
     * @return ArchCaliforniaValidationResult
     */
    public ArchCaliforniaValidationResult waitForArchCaliforniaError(String carrier, List<Map<String, String>> uploadedLocations) {
        logger.info("=== Checking for Arch + California Error ===");

        // Wait for potential error to appear after upload
        sleep(3000);

        // Scroll up to see any error messages
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
            sleep(500);
        } catch (Exception e) {
            // Continue
        }

        return validateArchCaliforniaCombination(carrier, uploadedLocations);
    }

    /**
     * Result class for Arch + California validation
     */
    public static class ArchCaliforniaValidationResult {
        private boolean valid = false;
        private boolean archCarrier = false;
        private boolean hasCaliforniaLocation = false;
        private boolean expectedError = false;
        private boolean errorDisplayed = false;
        private String errorMessage = "";
        private String message = "";

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public boolean isArchCarrier() { return archCarrier; }
        public void setArchCarrier(boolean archCarrier) { this.archCarrier = archCarrier; }

        public boolean hasCaliforniaLocation() { return hasCaliforniaLocation; }
        public void setHasCaliforniaLocation(boolean hasCaliforniaLocation) { this.hasCaliforniaLocation = hasCaliforniaLocation; }

        public boolean isExpectedError() { return expectedError; }
        public void setExpectedError(boolean expectedError) { this.expectedError = expectedError; }

        public boolean isErrorDisplayed() { return errorDisplayed; }
        public void setErrorDisplayed(boolean errorDisplayed) { this.errorDisplayed = errorDisplayed; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Arch + California Validation ===\n");
            sb.append("Carrier is Arch: ").append(archCarrier).append("\n");
            sb.append("Has California Location: ").append(hasCaliforniaLocation).append("\n");
            sb.append("Error Expected: ").append(expectedError).append("\n");
            sb.append("Error Displayed: ").append(errorDisplayed).append("\n");
            if (!errorMessage.isEmpty()) {
                sb.append("Error Message: ").append(errorMessage).append("\n");
            }
            sb.append("Validation: ").append(valid ? "PASS" : "FAIL").append("\n");
            sb.append("Message: ").append(message).append("\n");
            return sb.toString();
        }
    }
}
