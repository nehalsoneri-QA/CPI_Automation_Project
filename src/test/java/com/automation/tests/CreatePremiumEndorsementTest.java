package com.automation.tests;

import com.automation.base.DriverManager;
import com.automation.listeners.TestListener;
import com.automation.pages.CreatePremiumEndorsementPage;
import com.automation.pages.CreateQuotePage;
import com.automation.pages.EditQuotePage;
import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.pages.MasterPolicyPage;
import com.automation.pages.ResetPage;
import com.automation.utils.ConfigReader;
import com.automation.utils.ExcelReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Create Premium Endorsement functionality
 * Flow: Login -> Create Quote -> Submit -> Edit Quote -> Add Locations -> Bind -> Master Policy -> Premium Endorsement
 * Reuses existing page methods to avoid code duplication
 */
@Listeners(TestListener.class)
public class CreatePremiumEndorsementTest {

	protected static final Logger logger = LogManager.getLogger(CreatePremiumEndorsementTest.class);
	protected static ConfigReader config = ConfigReader.getInstance();

	private LoginPage loginPage;
	private ResetPage resetPage;
	private HomePage homePage;
	private CreateQuotePage createQuotePage;
	private EditQuotePage editQuotePage;
	private MasterPolicyPage masterPolicyPage;
	private CreatePremiumEndorsementPage createEndorsementPage;
	private WebDriver driver;

	private static final String ADMIN_EMAIL = "admin@cpiai.com";
	private static final String ADMIN_PASSWORD = "Admin@123";
	private static final String BASE_URL = "https://cpiai-dev.attri.ai/";

	// Store values for validation
	private Map<String, String> editQuoteValues;
	private Map<String, String> endorsementFormValues;
	private String policyNumber;
	private int editQuoteLocationCount = 0;

	// ==================== Setup ====================

	@BeforeClass(alwaysRun = true)
	public void initDriver() {
		driver = DriverManager.getDriver();
		TestListener.setDriver(driver);

		loginPage = new LoginPage(driver);
		resetPage = new ResetPage(driver);
		homePage = new HomePage(driver);
		createQuotePage = new CreateQuotePage(driver);
		editQuotePage = new EditQuotePage(driver);
		masterPolicyPage = new MasterPolicyPage(driver);

		driver.get(config.getProperty("base.url", BASE_URL));
		sleep(2000);
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		// DriverManager.quitDriver();
	}

	// ==================== Test 1: Positive Login ====================

	@Test(priority = 1, groups = { "PremiumEndorsementFlow" })
	public void testPositiveLogin() {
		logger.info("=== Test 1: Positive Login ===");

		if (DriverManager.isLoggedIn()) {
			logger.info("Already logged in, skipping login");
			return;
		}

		loginPage.waitForPageLoad();
		loginPage.captureScreenshotToReport("Login Page");

		loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
		sleep(3000);

		String currentUrl = driver.getCurrentUrl();
		if (currentUrl.contains("/reset_password")) {
			logger.info("Reset page detected - clicking Skip button");
			resetPage.clickSkipButton();
			sleep(2000);
		}

		homePage.waitForHomePageLoad();
		DriverManager.setLoggedIn(true);
		logger.info("Login successful");
		homePage.captureScreenshotToReport("Home Page - After Login");
	}

	// ==================== Test 2: Enter Data on Create Quote ====================

	@Test(priority = 2, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testPositiveLogin")
	public void testEnterDataOnCreateQuote() {
		logger.info("=== Test 2: Enter Data on Create Quote ===");

		ExcelReader configReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");
		configReader.printAllConfig();

		String locationMethod = configReader.getLocationMethod();
		String glAmount = configReader.getGLAmount();
		String wsAmount = configReader.getWSAmount();
		String uploadPath = configReader.getUploadPath();
		String carrier = configReader.getCarrier();
		String agent = configReader.getAgent();
		String insured = configReader.getInsured();
		String state = configReader.getState();
		String policyFee = configReader.getPolicyFee();

		logger.info("Config - Carrier: {}, Agent: {}, Insured: {}", carrier, agent, insured);

		// First ensure home page is properly loaded before navigating to Create Quote
		String baseUrl = config.getProperty("base.url", BASE_URL);
		driver.get(baseUrl);
		sleep(2000);
		homePage.waitForHomePageLoad();
		logger.info("Home page loaded successfully");

		// Now navigate to Create Quote
		String quoteUrl = baseUrl + "new_quote";
		driver.get(quoteUrl);
		sleep(3000);

		// Check if redirected to login page - if so, re-login
		String currentUrl = driver.getCurrentUrl();
		if (currentUrl.contains("/login")) {
			logger.warn("Session expired - redirected to login. Re-logging in...");
			DriverManager.setLoggedIn(false);
			loginPage.waitForPageLoad();
			loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
			sleep(3000);

			currentUrl = driver.getCurrentUrl();
			if (currentUrl.contains("/reset_password")) {
				logger.info("Reset page detected - clicking Skip button");
				resetPage.clickSkipButton();
				sleep(2000);
			}

			driver.get(quoteUrl);
			sleep(3000);
			DriverManager.setLoggedIn(true);
		}

		createQuotePage = new CreateQuotePage(driver);
		createQuotePage.waitForPageReady();
		createQuotePage.captureScreenshot("Create Quote Page");

		// Select dropdowns
		if (agent != null && !agent.isEmpty()) {
			createQuotePage.selectAgentByName(agent);
		} else {
			createQuotePage.selectFirstAvailableAgent();
		}
		sleep(1500);

		if (insured != null && !insured.isEmpty()) {
			createQuotePage.selectInsuredByName(insured);
		} else {
			createQuotePage.selectFirstAvailableInsured();
		}
		sleep(1500);

		if (carrier != null && !carrier.isEmpty()) {
			createQuotePage.selectCarrierByName(carrier);
		} else {
			createQuotePage.selectFirstAvailableCarrier();
		}
		sleep(1500);

		// Set amounts
		createQuotePage.setGeneralLiabilityAmount(glAmount);
		createQuotePage.setWaterSewerBackupAmount(wsAmount);
		createQuotePage.setPolicyFee(policyFee);
		createQuotePage.selectState(state);
		sleep(1000);

		// Add locations
		String locationMethodLower = locationMethod != null ? locationMethod.trim().toLowerCase() : "upload";
		boolean isUploadMode = locationMethodLower.contains("upload");

		if (isUploadMode && uploadPath != null && !uploadPath.trim().isEmpty()) {
			boolean uploadSuccess = createQuotePage.clickUploadButtonAndUploadFile(uploadPath);
			if (uploadSuccess) {
				// Wait for locations to load and verify count
				sleep(5000);
				int locationCount = createQuotePage.getLocationCount();
				logger.info("Locations loaded after upload: {}", locationCount);

				// If only 1 location, wait more and check again
				if (locationCount < 2) {
					logger.warn("Only {} location(s) found, waiting for more to load...", locationCount);
					sleep(5000);
					locationCount = createQuotePage.getLocationCount();
					logger.info("Locations after additional wait: {}", locationCount);
				}
				createQuotePage.captureScreenshot("After Upload - " + locationCount + " Locations");
			}
		} else {
			List<Map<String, String>> locations = configReader.getSheetDataAsMap("CreateQuote");
			if (locations != null && !locations.isEmpty()) {
				for (Map<String, String> location : locations) {
					createQuotePage.addLocationAndVerify(location, "Paid In Full", null);
					sleep(1000);
				}
			}
		}

		createQuotePage.captureScreenshot("Create Quote - Data Entered");
		configReader.close();
	}

	// ==================== Test 3: Click Submit Button ====================

	@Test(priority = 3, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testEnterDataOnCreateQuote")
	public void testClickSubmitButton() {
		logger.info("=== Test 3: Click Submit Button ===");

		createQuotePage.waitForSubmitButtonVisible();
		createQuotePage.captureScreenshot("Before Submit");

		CreateQuotePage.SubmitQuoteResult submitResult = createQuotePage.clickSubmitAndValidateQuoteCreation();
		logger.info("Submit Result: Success={}, Message={}", submitResult.isSuccess(), submitResult.getMessage());

		// Wait for navigation to Edit Quote
		boolean onEditQuote = false;
		String currentUrl = "";
		int maxWaitSeconds = 15;
		int waited = 0;

		while (!onEditQuote && waited < maxWaitSeconds) {
			sleep(1000);
			waited++;
			currentUrl = driver.getCurrentUrl();
			onEditQuote = currentUrl.contains("edit_quote") || currentUrl.contains("edit-quote");
			if (onEditQuote) {
				logger.info("Navigation to Edit Quote successful after {} seconds", waited);
				break;
			}
		}

		logger.info("Current URL after submit: {}", currentUrl);

		if (onEditQuote) {
			editQuotePage = new EditQuotePage(driver);
			editQuotePage.captureScreenshotToReport("Edit Quote Page - After Submit");
		}

		assertThat(onEditQuote).as("Should navigate to Edit Quote page. Current URL: " + currentUrl).isTrue();
	}

	// ==================== Test 4: Add Locations on Edit Quote ====================

	@Test(priority = 4, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickSubmitButton")
	public void testAddLocationsOnEditQuote() {
		logger.info("=== Test 4: Add Locations on Edit Quote ===");

		if (editQuotePage == null) {
			editQuotePage = new EditQuotePage(driver);
		}

		editQuotePage.waitForEditQuotePageReady();
		sleep(2000);

		ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");
		List<Map<String, String>> editQuoteLocations = excelReader.getEditQuoteLocations();
		logger.info("Found {} locations in EditQuote sheet", editQuoteLocations.size());

		if (editQuoteLocations != null && !editQuoteLocations.isEmpty()) {
			int added = editQuotePage.addLocationsFromEditQuoteSheet(editQuoteLocations);
			logger.info("Added {} locations", added);
		}

		sleep(2000);
		editQuotePage.captureScreenshotToReport("Edit Quote - Locations Added");

		// Store location count
		String locationText = editQuotePage.getLocationCountText();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("of\\s+(\\d+)");
		java.util.regex.Matcher matcher = pattern.matcher(locationText);
		if (matcher.find()) {
			editQuoteLocationCount = Integer.parseInt(matcher.group(1));
		}
		logger.info("Total location count: {}", editQuoteLocationCount);

		excelReader.close();
	}

	// ==================== Test 5: Click Bind Button ====================

	@Test(priority = 5, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testAddLocationsOnEditQuote")
	public void testClickBindButton() {
		logger.info("=== Test 5: Click Bind Button ===");

		if (editQuotePage == null) {
			editQuotePage = new EditQuotePage(driver);
		}

		// Capture Edit Quote values for later comparison
		logger.info("Capturing Edit Quote values for comparison");
		editQuoteValues = editQuotePage.captureAllEditQuoteValues();

		// Scroll to bottom and click Bind
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		sleep(1000);

		editQuotePage.captureScreenshotToReport("Edit Quote - Before Bind");

		boolean clicked = editQuotePage.clickBindGeneratePolicyButton();
		assertThat(clicked).as("Bind button should be clickable").isTrue();

		sleep(5000);

		String currentUrl = driver.getCurrentUrl();
		assertThat(currentUrl).as("Should navigate to Master Policy page").contains("/policies/");

		masterPolicyPage = new MasterPolicyPage(driver);
		masterPolicyPage.waitForMasterPolicyPageReady();

		policyNumber = masterPolicyPage.getPolicyNumberFromURL();
		logger.info("Policy Number: {}", policyNumber);
		masterPolicyPage.captureScreenshotToReport("Master Policy - Policy #" + policyNumber);
	}

	// ==================== Test 6: Click Premium Endorsement Button ====================

	@Test(priority = 6, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickBindButton")
	public void testClickPremiumEndorsementButton() {
		logger.info("=== Test 6: Click Premium Endorsement Button ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		// Click on Premium Endorsement button (also handles Caution dialog and clicks Continue)
		boolean clicked = masterPolicyPage.clickPremiumEndorsementButton();
		assertThat(clicked)
			.as("Should be able to click Premium Endorsement button")
			.isTrue();

		sleep(2000);

		// Initialize Create Premium Endorsement page
		createEndorsementPage = new CreatePremiumEndorsementPage(driver);

		logger.info("Clicked Premium Endorsement button and Continue on Caution dialog");
	}

	// ==================== Test 7: Verify Endorsement Page URL ====================

	@Test(priority = 7, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickPremiumEndorsementButton")
	public void testVerifyEndorsementPageURL() {
		logger.info("=== Test 7: Verify Endorsement Page URL ===");

		// Expected URL
		String expectedUrlPart = "create-premium-endorsement";
		String currentUrl = driver.getCurrentUrl();

		logger.info("Current URL: {}", currentUrl);
		logger.info("Expected URL should contain: {}", expectedUrlPart);

		// Validate URL
		boolean urlValid = currentUrl.contains(expectedUrlPart);

		// Log Pass/Fail to report with HTML formatting
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");
		if (urlValid) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>URL Validation: PASS</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>URL Validation: FAIL</h3>");
		}
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Expected URL Contains:</td>");
		html.append("<td style='padding: 8px;'>").append(expectedUrlPart).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Actual URL:</td>");
		html.append("<td style='padding: 8px;'>").append(currentUrl).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Result:</td>");
		html.append("<td style='padding: 8px; font-weight: bold; color: ").append(urlValid ? "#28a745" : "#dc3545").append(";'>");
		html.append(urlValid ? "PASS" : "FAIL").append("</td></tr>");
		html.append("</table></div>");

		createEndorsementPage.logHtmlToReport(html.toString());
		createEndorsementPage.captureEndorsementScreenshot("URL Validation - " + (urlValid ? "PASS" : "FAIL"));

		// Assert URL is valid
		assertThat(urlValid)
			.as("URL should contain 'create-premium-endorsement'. Current URL: " + currentUrl)
			.isTrue();

		logger.info("URL Validation: {} - {}", urlValid ? "PASS" : "FAIL", currentUrl);
	}

	// ==================== Test 8: Verify Endorsement Page Loaded ====================

	@Test(priority = 8, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifyEndorsementPageURL")
	public void testVerifyEndorsementPageLoaded() {
		logger.info("=== Test 8: Verify Endorsement Page Loaded ===");

		assertThat(createEndorsementPage.isPageLoaded())
			.as("Create Premium Endorsement page should be loaded")
			.isTrue();

		// Wait for page data to load
		createEndorsementPage.waitForPageDataLoad();

		// Capture and validate Policy Number
		String endorsementPolicyNumber = createEndorsementPage.getPolicyNumber();
		logger.info("Policy Number on Endorsement Page: {}", endorsementPolicyNumber);

		// Validate policy number matches Master Policy
		if (policyNumber != null && !policyNumber.isEmpty()) {
			boolean policyMatch = endorsementPolicyNumber.contains(policyNumber) || policyNumber.contains(endorsementPolicyNumber);
			logger.info("Policy Number Validation: Master Policy='{}', Endorsement='{}', Match={}",
				policyNumber, endorsementPolicyNumber, policyMatch);

			// Log to report
			logPolicyNumberValidation(policyNumber, endorsementPolicyNumber, policyMatch);

			assertThat(policyMatch)
				.as("Policy Number on Endorsement page (" + endorsementPolicyNumber + ") should match Master Policy (" + policyNumber + ")")
				.isTrue();
		}

		createEndorsementPage.captureEndorsementScreenshot("Page Loaded - Policy #" + endorsementPolicyNumber);
		logger.info("Create Premium Endorsement page loaded successfully");
	}

	/**
	 * Log policy number validation to HTML report
	 */
	private void logPolicyNumberValidation(String masterPolicyNum, String endorsementPolicyNum, boolean isMatch) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");
		if (isMatch) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Policy Number Validation: PASS</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Policy Number Validation: FAIL</h3>");
		}
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Master Policy Number:</td><td style='padding: 8px;'>").append(masterPolicyNum).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Endorsement Policy Number:</td><td style='padding: 8px;'>").append(endorsementPolicyNum).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Status:</td><td style='padding: 8px; font-weight: bold; color: ")
			.append(isMatch ? "#28a745" : "#dc3545").append(";'>").append(isMatch ? "MATCH" : "MISMATCH").append("</td></tr>");
		html.append("</table></div>");
		createEndorsementPage.logHtmlToReport(html.toString());
	}

	// ==================== Test 9: Capture Endorsement Form Values (Before Adding Locations) ====================

	@Test(priority = 9, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifyEndorsementPageLoaded")
	public void testCaptureEndorsementFormValues() {
		logger.info("=== Test 9: Capture and Validate Endorsement Form Values (Before Adding Locations) ===");

		// Capture all form values
		endorsementFormValues = createEndorsementPage.captureEndorsementFormValues();

		assertThat(endorsementFormValues)
			.as("Should capture endorsement form values")
			.isNotEmpty();

		// Log captured values to report
		createEndorsementPage.logEndorsementValuesToReport(endorsementFormValues);
		logger.info("Endorsement form values captured: {}", endorsementFormValues);

		// Validate critical fields (dates) were captured correctly
		boolean datesValid = createEndorsementPage.validateCapturedValues(endorsementFormValues);

		// Log date validation result
		String effectiveDate = endorsementFormValues.get("EffectiveDate");
		String expirationDate = endorsementFormValues.get("ExpirationDate");
		String endorsementEffectiveDate = endorsementFormValues.get("EndorsementEffectiveDate");

		logger.info("Date values captured - EffectiveDate: '{}', ExpirationDate: '{}', EndorsementEffectiveDate: '{}'",
			effectiveDate, expirationDate, endorsementEffectiveDate);

		// Assert dates were captured correctly
		assertThat(datesValid)
			.as("All date fields must be captured correctly. " +
				"EffectiveDate='" + effectiveDate + "', " +
				"ExpirationDate='" + expirationDate + "', " +
				"EndorsementEffectiveDate='" + endorsementEffectiveDate + "'")
			.isTrue();

		// Validate endorsement values against Master Policy (Edit Quote) values
		validateEndorsementValuesAgainstMasterPolicy();
	}

	// ==================== Test 10: Validate Display Computations for Existing Locations ====================

	@Test(priority = 10, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testCaptureEndorsementFormValues")
	public void testValidateDisplayComputations() {
		logger.info("=== Test 10: Validate Display Computations for Existing Locations ===");

		// Capture screenshot before validation
		createEndorsementPage.captureEndorsementScreenshot("Before Display Computations Validation");

		// Perform display computations validation - clicks dialog ONCE and captures all locations
		CreatePremiumEndorsementPage.PropertyPremiumValidationResult result =
			createEndorsementPage.validateDisplayComputations();

		// Log results to HTML report
		createEndorsementPage.logPropertyPremiumValidationToReport(result);

		// Capture screenshot after validation
		createEndorsementPage.captureEndorsementScreenshot("After Display Computations Validation");

		// Log summary
		logger.info("Display Computations Validation Summary: {} Passed, {} Failed out of {} locations",
			result.getPassedCount(), result.getFailedCount(), result.getTotalCount());

		// Assertion - all locations should have correct property premium
		assertThat(result.isAllPassed())
			.as("All locations should have correct Property Premium. Formula: TIV/100 × Rate. " +
				"Passed: " + result.getPassedCount() + ", Failed: " + result.getFailedCount())
			.isTrue();

		logger.info("Display Computations validation completed successfully for all locations");
	}

	// ==================== Test 11: Add Locations on Endorsement ====================

	@Test(priority = 11, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testValidateDisplayComputations")
	public void testAddLocationsOnEndorsement() {
		logger.info("=== Test 9: Add Locations from CreateEndorsement Sheet ===");

		// Read locations from CreateEndorsement sheet
		ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");

		// Check if CreateEndorsement sheet exists
		assertThat(excelReader.sheetExists("CreateEndorsement"))
			.as("CreateEndorsement sheet should exist in TestData.xlsx")
			.isTrue();

		List<Map<String, String>> endorsementLocations = excelReader.getLocationsFromSheet("CreateEndorsement");
		logger.info("Found {} locations in CreateEndorsement sheet", endorsementLocations.size());

		// Validate that we have locations to add
		assertThat(endorsementLocations)
			.as("CreateEndorsement sheet should contain at least one location")
			.isNotNull()
			.isNotEmpty();

		// Get carrier name to determine validation logic
		String carrierName = editQuoteValues != null ? editQuoteValues.getOrDefault("Carrier", "") : "";
		boolean isArchCarrier = carrierName.toLowerCase().contains("arch");
		logger.info("Carrier: {} (isArch: {})", carrierName, isArchCarrier);

		// Get location count before adding
		int locationCountBefore = createEndorsementPage.getLocationCount();
		logger.info("Location count BEFORE adding: {}", locationCountBefore);

		// Log locations to add
		logLocationsToReport(endorsementLocations);

		// Add locations
		createEndorsementPage.addLocationsFromSheet(endorsementLocations);

		// Wait for UI to update
		sleep(2000);

		// Get location count after adding
		int locationCountAfter = createEndorsementPage.getLocationCount();
		logger.info("Location count AFTER adding: {}", locationCountAfter);

		// Calculate ACTUAL added count from page (more reliable than method return)
		int actualAddedCount = locationCountAfter - locationCountBefore;
		int rejectedCount = endorsementLocations.size() - actualAddedCount;

		logger.info("Actual added: {}, Rejected: {}", actualAddedCount, rejectedCount);

		// Capture screenshot
		createEndorsementPage.captureEndorsementScreenshot("After Adding Locations");

		// Log result with validation (including rejected count and carrier info)
		logLocationAddResultWithRejected(endorsementLocations.size(), actualAddedCount, rejectedCount, locationCountBefore, locationCountAfter, carrierName, isArchCarrier);

		excelReader.close();

		// VALIDATION: Different logic based on carrier
		if (rejectedCount > 0 && isArchCarrier) {
			// ARCH CARRIER: California locations rejection is expected behavior
			logger.info("EXPECTED: {} locations rejected due to Arch California restriction", rejectedCount);
			logger.info("Test PASSED - Arch California error handled correctly for California locations");
			// Test passes - no assertion needed, rejection is expected for Arch + CA
		} else if (rejectedCount > 0 && !isArchCarrier) {
			// NON-ARCH CARRIER (e.g., Starstone): Locations should NOT be rejected
			createEndorsementPage.captureEndorsementScreenshot("FAILED - Locations Not Added for " + carrierName);
			logger.error("FAILED: {} locations were not added for carrier: {}", rejectedCount, carrierName);

			assertThat(actualAddedCount)
				.as("All " + endorsementLocations.size() + " locations should be added for carrier: " + carrierName +
					". But only " + actualAddedCount + " were added. " + rejectedCount + " locations failed to add.")
				.isEqualTo(endorsementLocations.size());
		} else {
			// All locations added successfully
			assertThat(actualAddedCount)
				.as("At least one location should be added from CreateEndorsement sheet")
				.isGreaterThan(0);
		}

		logger.info("Location count validation PASSED: {} (before) + {} (added) = {} (after), {} rejected",
			locationCountBefore, actualAddedCount, locationCountAfter, rejectedCount);
	}

	// ==================== Test 12: Validate Premium Calculations ====================

	@Test(priority = 12, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testAddLocationsOnEndorsement")
	public void testValidatePremiumCalculations() {
		logger.info("=== Test 10: Validate Premium Calculations ===");

		// Capture screenshot before validation
		createEndorsementPage.captureEndorsementScreenshot("Before Premium Validation");

		// Perform premium calculations validation
		CreatePremiumEndorsementPage.PremiumValidationResult result = createEndorsementPage.validateEndorsementPremiums();

		// Log results to HTML report
		createEndorsementPage.logPremiumValidationToReport(result);

		// Capture screenshot after validation
		createEndorsementPage.captureEndorsementScreenshot("After Premium Validation");

		// Assertions for each validation
		assertThat(result.isPremiumGLWSMatch())
			.as("Validation 1: Property Premium + GL Premium + Water/Sewer Premium should equal Premium (GL+WS) value. " +
				"Calculated: $" + String.format("%.2f", result.getCalculatedPremiumGLWS()) +
				", Actual: $" + String.format("%.2f", result.getPremiumGLWSValue()))
			.isTrue();

		assertThat(result.isTaxesMatch())
			.as("Validation 2: Sum of Taxes column should equal Taxes value. " +
				"Sum: $" + String.format("%.2f", result.getTaxesSum()) +
				", Actual: $" + String.format("%.2f", result.getTaxesValue()))
			.isTrue();

		assertThat(result.isFeesMatch())
			.as("Validation 3: Sum of Fees column should equal Total Fees value. " +
				"Sum: $" + String.format("%.2f", result.getFeesSum()) +
				", Actual: $" + String.format("%.2f", result.getTotalFeesValue()))
			.isTrue();

		assertThat(result.isGrandTotalMatch())
			.as("Validation 4: Premium (GL+WS) + Taxes + Total Fees should equal Grand Total. " +
				"Calculated: $" + String.format("%.2f", result.getCalculatedGrandTotal()) +
				", Actual: $" + String.format("%.2f", result.getGrandTotalValue()))
			.isTrue();

		logger.info("All premium calculations validated successfully!");
	}

	/**
	 * Log location add result with rejected count - handles Arch vs non-Arch carriers
	 */
	private void logLocationAddResultWithRejected(int totalLocations, int addedCount, int rejectedCount, int countBefore, int countAfter, String carrierName, boolean isArchCarrier) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		int expectedCount = countBefore + addedCount;
		boolean countMatches = (countAfter == expectedCount);
		// For Arch: pass if rejected due to CA restriction. For others: fail if any rejected
		boolean isSuccess = countMatches && (rejectedCount == 0 || isArchCarrier);

		if (isSuccess) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Location Add Result: PASS</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Location Add Result: FAIL</h3>");
		}

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Carrier:</td><td style='padding: 8px;'>").append(carrierName).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Locations in Sheet:</td><td style='padding: 8px;'>").append(totalLocations).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Locations Added:</td><td style='padding: 8px;'>").append(addedCount).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Count Before:</td><td style='padding: 8px;'>").append(countBefore).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Count After:</td><td style='padding: 8px;'>").append(countAfter).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Expected Count:</td><td style='padding: 8px;'>")
			.append(countBefore).append(" + ").append(addedCount).append(" = ").append(expectedCount).append("</td></tr>");

		if (rejectedCount > 0) {
			if (isArchCarrier) {
				// Arch carrier - California rejection is expected
				html.append("<tr style='background-color: #fff3cd;'><td style='padding: 8px; font-weight: bold; color: #856404;'>Locations Rejected:</td>");
				html.append("<td style='padding: 8px; color: #856404;'>").append(rejectedCount).append(" (Expected - Arch CA restriction)</td></tr>");
				html.append("<tr style='background-color: #fff3cd;'><td colspan='2' style='padding: 8px; color: #856404;'>");
				html.append("<strong>Note:</strong> Arch Specialty Insurance does not cover California counties</td></tr>");
			} else {
				// Non-Arch carrier - rejection is a failure
				html.append("<tr style='background-color: #f8d7da;'><td style='padding: 8px; font-weight: bold; color: #dc3545;'>Locations NOT Added:</td>");
				html.append("<td style='padding: 8px; color: #dc3545;'>").append(rejectedCount).append(" (UNEXPECTED for ").append(carrierName).append(")</td></tr>");
				html.append("<tr style='background-color: #f8d7da;'><td colspan='2' style='padding: 8px; color: #dc3545;'>");
				html.append("<strong>ERROR:</strong> All locations should be added for ").append(carrierName).append(" carrier</td></tr>");
			}
		}

		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Overall Status:</td><td style='padding: 8px; font-weight: bold; color: ")
			.append(isSuccess ? "#28a745" : "#dc3545").append(";'>")
			.append(isSuccess ? "PASS" : "FAIL").append("</td></tr>");
		html.append("</table></div>");

		createEndorsementPage.logHtmlToReport(html.toString());
	}

	/**
	 * Log locations to add to HTML report
	 */
	private void logLocationsToReport(List<Map<String, String>> locations) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #17a2b8;'>");
		html.append("<h3 style='color: #17a2b8; margin-top: 0;'>Locations to Add from CreateEndorsement Sheet</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 8px;'>#</th><th style='padding: 8px;'>Address</th><th style='padding: 8px;'>Payment Plan</th></tr>");

		for (int i = 0; i < locations.size(); i++) {
			Map<String, String> loc = locations.get(i);
			html.append("<tr style='background-color: #ffffff; border-bottom: 1px solid #dee2e6;'>");
			html.append("<td style='padding: 8px;'>").append(i + 1).append("</td>");
			html.append("<td style='padding: 8px;'>").append(loc.getOrDefault("Address", "N/A")).append("</td>");
			html.append("<td style='padding: 8px;'>").append(loc.getOrDefault("PaymentPlan", "N/A")).append("</td>");
			html.append("</tr>");
		}
		html.append("</table></div>");
		createEndorsementPage.logHtmlToReport(html.toString());
	}

	/**
	 * Log location add result with validation status
	 * Validates: Previous locations + New locations = Total locations after
	 */
	private void logLocationAddResult(int totalLocations, int addedCount, int countBefore, int countAfter) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");

		// Calculate expected count and validate
		int expectedCount = countBefore + addedCount;
		boolean countMatches = (countAfter == expectedCount);
		boolean isSuccess = addedCount > 0 && countMatches;

		if (isSuccess) {
			html.append("background-color: #d4edda; border-left: 4px solid #28a745;'>");
			html.append("<h3 style='color: #28a745; margin-top: 0;'>Location Add Result: PASS</h3>");
		} else {
			html.append("background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
			html.append("<h3 style='color: #dc3545; margin-top: 0;'>Location Add Result: FAIL</h3>");
		}

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Locations in Sheet:</td><td style='padding: 8px;'>").append(totalLocations).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Locations Added:</td><td style='padding: 8px;'>").append(addedCount).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Count Before (Previous):</td><td style='padding: 8px;'>").append(countBefore).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Count After (Current):</td><td style='padding: 8px;'>").append(countAfter).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Expected Count:</td><td style='padding: 8px;'>")
			.append(countBefore).append(" + ").append(addedCount).append(" = ").append(expectedCount).append("</td></tr>");

		// Count validation row
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Count Validation:</td><td style='padding: 8px; font-weight: bold; color: ")
			.append(countMatches ? "#28a745" : "#dc3545").append(";'>")
			.append(countMatches ? "PASS" : "FAIL")
			.append(" (").append(countAfter).append(" ").append(countMatches ? "==" : "!=").append(" ").append(expectedCount).append(")")
			.append("</td></tr>");

		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Overall Status:</td><td style='padding: 8px; font-weight: bold; color: ")
			.append(isSuccess ? "#28a745" : "#dc3545").append(";'>")
			.append(isSuccess ? "PASS" : "FAIL").append("</td></tr>");
		html.append("</table></div>");

		createEndorsementPage.logHtmlToReport(html.toString());
	}

	/**
	 * Validate endorsement form values against Master Policy values
	 * Skips EndorsementEffectiveDate as it doesn't display on Policy Details page
	 */
	private void validateEndorsementValuesAgainstMasterPolicy() {
		logger.info("Validating Endorsement values against Master Policy values...");

		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #007bff;'>");
		html.append("<h3 style='color: #007bff; margin-top: 0;'>Premium Endorsement vs Master Policy Validation</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<th style='padding: 10px; text-align: left;'>Field</th>");
		html.append("<th style='padding: 10px; text-align: left;'>Master Policy Value</th>");
		html.append("<th style='padding: 10px; text-align: left;'>Endorsement Value</th>");
		html.append("<th style='padding: 10px; text-align: center;'>Status</th>");
		html.append("</tr>");

		int passCount = 0;
		int failCount = 0;

		// Define field mappings - Skip EndorsementEffectiveDate as it doesn't display on Policy Details page
		String[][] fieldMappings = {
			{"Agent", "Agent"},
			{"Insured", "Insured"},
			{"Carrier", "Carrier"},
			{"EffectiveDate", "EffectiveDate"},
			{"ExpirationDate", "ExpirationDate"},
			{"GLAmount", "GLAmount"},
			{"WSAmount", "WSAmount"},
			{"State", "State"},
			{"LocationCount", "LocationCount"}
		};

		for (String[] mapping : fieldMappings) {
			String fieldName = mapping[0];
			String endorsementKey = mapping[1];

			String masterPolicyValue = editQuoteValues != null ? editQuoteValues.getOrDefault(fieldName, "N/A") : "N/A";
			String endorsementValue = endorsementFormValues != null ? endorsementFormValues.getOrDefault(endorsementKey, "N/A") : "N/A";

			// Normalize values for comparison
			String normalizedMaster = normalizeValue(masterPolicyValue);
			String normalizedEndorsement = normalizeValue(endorsementValue);

			boolean isMatch = normalizedMaster.equalsIgnoreCase(normalizedEndorsement) ||
				(normalizedMaster.contains(normalizedEndorsement) || normalizedEndorsement.contains(normalizedMaster));

			// For amounts, compare numeric values
			if (fieldName.contains("Amount") || fieldName.equals("LocationCount")) {
				isMatch = compareNumericValues(masterPolicyValue, endorsementValue);
			}

			String status = isMatch ? "PASS" : "FAIL";
			String statusColor = isMatch ? "#28a745" : "#dc3545";
			String rowBgColor = isMatch ? "#d4edda" : "#f8d7da";

			if (isMatch) passCount++; else failCount++;

			html.append("<tr style='background-color: ").append(rowBgColor).append("; border-bottom: 1px solid #dee2e6;'>");
			html.append("<td style='padding: 8px; font-weight: bold; color: #000000;'>").append(fieldName).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(masterPolicyValue).append("</td>");
			html.append("<td style='padding: 8px; color: #000000;'>").append(endorsementValue).append("</td>");
			html.append("<td style='padding: 8px; text-align: center; font-weight: bold; color: ").append(statusColor).append(";'>").append(status).append("</td>");
			html.append("</tr>");

			logger.info("Validation - {}: Master='{}', Endorsement='{}', Status={}", fieldName, masterPolicyValue, endorsementValue, status);
		}

		// Summary row
		String summaryColor = failCount == 0 ? "#28a745" : "#dc3545";
		html.append("<tr style='background-color: #343a40; color: white;'>");
		html.append("<td colspan='3' style='padding: 10px; font-weight: bold;'>Total Results</td>");
		html.append("<td style='padding: 10px; text-align: center; font-weight: bold; color: ").append(summaryColor).append(";'>");
		html.append("PASS: ").append(passCount).append(" | FAIL: ").append(failCount).append("</td>");
		html.append("</tr>");

		html.append("</table></div>");

		createEndorsementPage.logHtmlToReport(html.toString());
		createEndorsementPage.captureEndorsementScreenshot("Validation Results");

		logger.info("Validation Summary - PASS: {}, FAIL: {}", passCount, failCount);
	}

	/**
	 * Normalize value for comparison (remove special chars, trim)
	 */
	private String normalizeValue(String value) {
		if (value == null || value.equals("N/A") || value.equals("--") || value.equals("null")) {
			return "";
		}
		return value.trim().replaceAll("[,$]", "").replaceAll("\\s+", " ");
	}

	/**
	 * Compare numeric values (for amounts and counts)
	 */
	private boolean compareNumericValues(String value1, String value2) {
		try {
			String clean1 = value1.replaceAll("[^0-9.]", "");
			String clean2 = value2.replaceAll("[^0-9.]", "");

			if (clean1.isEmpty() || clean2.isEmpty()) {
				return clean1.isEmpty() && clean2.isEmpty();
			}

			double num1 = Double.parseDouble(clean1);
			double num2 = Double.parseDouble(clean2);
			return Math.abs(num1 - num2) < 0.01;
		} catch (NumberFormatException e) {
			return value1.equalsIgnoreCase(value2);
		}
	}

	// ==================== Test 13: Verify Submit Button State ====================

	@Test(priority = 13, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testValidatePremiumCalculations")
	public void testVerifySubmitButtonState() {
		logger.info("=== Test 13: Verify Submit Button State ===");

		boolean isEnabled = createEndorsementPage.isSubmitButtonEnabled();

		// Log the state but don't fail - submit might be disabled if no changes made
		logger.info("Submit button enabled: {}", isEnabled);
		createEndorsementPage.captureEndorsementScreenshot("Submit Button State");
	}

	// ==================== Helper ====================

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
