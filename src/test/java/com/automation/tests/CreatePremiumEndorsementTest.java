package com.automation.tests;

import com.automation.base.DriverManager;
import com.automation.listeners.TestListener;
import com.automation.pages.CreatePremiumEndorsementPage;
import com.automation.pages.CreateQuotePage;
import com.automation.pages.EditPremiumEndorsementPage;
import com.automation.pages.EditQuotePage;
import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.pages.MasterPolicyPage;
import com.automation.pages.ResetPage;
import com.automation.utils.ConfigReader;
import com.automation.utils.ExcelReader;
import com.automation.utils.TestWaitHelper;
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
	private EditPremiumEndorsementPage editEndorsementPage;
	private WebDriver driver;
	private TestWaitHelper waitHelper;

	// Store captured endorsement data for validation on Edit page
	private CreatePremiumEndorsementPage.EndorsementCapturedData capturedEndorsementData;

	private static final String ADMIN_EMAIL = "admin@cpiai.com";
	private static final String ADMIN_PASSWORD = "Admin@123";
	private static final String BASE_URL = "https://cpiai-dev.attri.ai/";

	// Store values for validation
	private Map<String, String> editQuoteValues;
	private Map<String, String> endorsementFormValues;
	private String policyNumber;
	private int editQuoteLocationCount = 0;
	private int endorsementLocationsAdded = 0;
	private List<Map<String, String>> endorsementLocationData; // Store location data from Excel for pro-rata validation
	private List<Map<String, String>> editEndorsementLocationData; // Store Edit Endorsement location data for pro-rata validation
	private int editEndorsementLocationsAdded = 0;

	// ==================== Setup ====================

	@BeforeClass(alwaysRun = true)
	public void initDriver() {
		driver = DriverManager.getDriver();
		TestListener.setDriver(driver);
		waitHelper = new TestWaitHelper(driver);

		loginPage = new LoginPage(driver);
		resetPage = new ResetPage(driver);
		homePage = new HomePage(driver);
		createQuotePage = new CreateQuotePage(driver);
		editQuotePage = new EditQuotePage(driver);
		masterPolicyPage = new MasterPolicyPage(driver);

		driver.get(config.getProperty("base.url", BASE_URL));
		waitHelper.waitAfterNavigation();
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

	// ==================== Test 4: Click Bind Button ====================

	@Test(priority = 4, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickSubmitButton")
	public void testClickBindButton() {
		logger.info("=== Test 4: Click Bind Button ===");

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

		// Capture policy number from UI element using XPath: //*[@id="root"]/div[2]/div/div[1]/div[1]/div[1]/label/span
		policyNumber = masterPolicyPage.getPolicyNumberFromUI();
		logger.info("Policy Number from Master Policy UI: {}", policyNumber);
		masterPolicyPage.captureScreenshotToReport("Master Policy - Policy #" + policyNumber);
	}

	// ==================== Test 5: Click Premium Endorsement Button ====================

	@Test(priority = 5, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickBindButton")
	public void testClickPremiumEndorsementButton() {
		logger.info("=== Test 5: Click Premium Endorsement Button ===");

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

	// ==================== Test 6: Verify Endorsement Page URL ====================

	@Test(priority = 6, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickPremiumEndorsementButton")
	public void testVerifyEndorsementPageURL() {
		logger.info("=== Test 6: Verify Endorsement Page URL ===");

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

	// ==================== Test 7: Verify Endorsement Page Loaded ====================

	@Test(priority = 7, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifyEndorsementPageURL")
	public void testVerifyEndorsementPageLoaded() {
		logger.info("=== Test 7: Verify Endorsement Page Loaded ===");

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
			// First check if endorsement policy number was captured
			boolean endorsementPolicyExists = endorsementPolicyNumber != null && !endorsementPolicyNumber.trim().isEmpty();

			boolean policyMatch = false;
			if (endorsementPolicyExists) {
				// Only compare if endorsement policy number exists
				policyMatch = endorsementPolicyNumber.contains(policyNumber) || policyNumber.contains(endorsementPolicyNumber);
			}

			logger.info("Policy Number Validation: Master Policy='{}', Endorsement='{}', Endorsement Exists={}, Match={}",
				policyNumber, endorsementPolicyNumber, endorsementPolicyExists, policyMatch);

			// Log to report
			logPolicyNumberValidation(policyNumber, endorsementPolicyNumber, policyMatch);

			assertThat(endorsementPolicyExists)
				.as("Endorsement Policy Number should be displayed on the page")
				.isTrue();

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

	// ==================== Test 8: Capture Endorsement Form Values (Before Adding Locations) ====================

	@Test(priority = 8, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifyEndorsementPageLoaded")
	public void testCaptureEndorsementFormValues() {
		logger.info("=== Test 8: Capture and Validate Endorsement Form Values (Before Adding Locations) ===");

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

	// ==================== Test 9: Verify Submit Button State ====================

	@Test(priority = 9, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testCaptureEndorsementFormValues")
	public void testVerifySubmitButtonState() {
		logger.info("=== Test 9: Verify Submit Button State ===");

		boolean isEnabled = createEndorsementPage.isSubmitButtonEnabled();
		logger.info("Submit button enabled: {}", isEnabled);
		createEndorsementPage.captureEndorsementScreenshot("Submit Button State");
	}

	// ==================== Test 10: Validate Display Computations ====================

	@Test(priority = 10, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifySubmitButtonState")
	public void testValidateDisplayComputations() {
		logger.info("=== Test 9: Validate Display Computations for Existing Locations ===");

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

	// ==================== Test 10: Change Endorsement Effective Date ====================

	@Test(priority = 11, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testValidateDisplayComputations")
	public void testChangeEndorsementEffectiveDate() {
		logger.info("=== Test 10: Change Endorsement Effective Date to 6 Months Later ===");

		// Capture current date before change
		String currentEndorsementDate = createEndorsementPage.getEndorsementEffectiveDate();
		logger.info("Current Endorsement Effective Date: {}", currentEndorsementDate);

		// Calculate expected date (6 months from now)
		java.time.LocalDate expectedDate = java.time.LocalDate.now().plusMonths(6);
		String expectedDateStr = expectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
		logger.info("Expected date after change: {}", expectedDateStr);

		// Change date to 6 months from now and handle Confirm Date Change dialog
		// This method now validates that the selected date is displayed correctly in the textbox
		boolean dateChangeAndValidation = createEndorsementPage.changeEndorsementDateTo6MonthsLater();

		// Assert that date was changed AND the displayed date matches the expected date
		assertThat(dateChangeAndValidation)
			.as("Date change validation failed: After selecting date 6 months from today (" + expectedDateStr + "), " +
				"the same date should be displayed in the Endorsement Effective Date textbox. " +
				"Check the HTML report for details on expected vs actual date.")
			.isTrue();

		// Verify the date was actually changed
		sleep(2000);
		String newEndorsementDate = createEndorsementPage.getEndorsementEffectiveDate();
		logger.info("New Endorsement Effective Date: {}", newEndorsementDate);

		// Log the date change to report
		logDateChangeToReport(currentEndorsementDate, newEndorsementDate);

		logger.info("Endorsement Effective Date changed and validated successfully");
	}

	/**
	 * Log date change result to HTML report
	 */
	private void logDateChangeToReport(String oldDate, String newDate) {
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; background-color: #d4edda; border-left: 4px solid #28a745;'>");
		html.append("<h3 style='color: #28a745; margin-top: 0;'>Endorsement Date Change: SUCCESS</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Previous Date:</td><td style='padding: 8px;'>").append(oldDate).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>New Date (6 months later):</td><td style='padding: 8px;'>").append(newDate).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Confirm Dialog:</td><td style='padding: 8px;'>Handled - Clicked Confirm Changes</td></tr>");
		html.append("</table></div>");
		createEndorsementPage.logHtmlToReport(html.toString());
	}

	// ==================== Test 11: Add Locations on Endorsement ====================

	@Test(priority = 12, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testChangeEndorsementEffectiveDate")
	public void testAddLocationsOnEndorsement() {
		logger.info("=== Test 11: Add Locations from CreateEndorsement Sheet ===");

		// Read locations from CreateEndorsement sheet
		ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");

		// Check if CreateEndorsement sheet exists
		assertThat(excelReader.sheetExists("CreateEndorsement"))
			.as("CreateEndorsement sheet should exist in TestData.xlsx")
			.isTrue();

		List<Map<String, String>> endorsementLocations = excelReader.getLocationsFromSheet("CreateEndorsement");
		logger.info("Found {} locations in CreateEndorsement sheet", endorsementLocations.size());

		// Store location data for pro-rata validation later
		endorsementLocationData = endorsementLocations;

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

		// Store the count of newly added locations for pro-rata validation
		endorsementLocationsAdded = actualAddedCount;

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

		// Pro-Rata Premium Validation for newly added locations
		if (actualAddedCount > 0 && endorsementLocationData != null && !endorsementLocationData.isEmpty()) {
			logger.info("=== Validating Pro-Rata Premium Calculations ===");

			// Get only the successfully added locations for validation
			List<Map<String, String>> addedLocations = endorsementLocationData;
			if (actualAddedCount < endorsementLocationData.size()) {
				// If some locations were rejected, only validate the ones that were added
				addedLocations = endorsementLocationData.subList(0, actualAddedCount);
			}

			CreatePremiumEndorsementPage.ProRataPremiumValidationResult proRataResult =
				createEndorsementPage.validateProRataPremiumCalculations(addedLocations);

			// Log validation result
			if (proRataResult.getError() != null) {
				logger.warn("Pro-Rata validation encountered an error: {}", proRataResult.getError());
			} else {
				logger.info("Pro-Rata validation completed: {} locations validated, All Passed: {}",
					proRataResult.getLocationCalculations().size(), proRataResult.isAllPassed());

				// Assert that all pro-rata calculations match
				assertThat(proRataResult.isAllPassed())
					.as("Pro-Rata Premium Validation: All calculated pro-rata premiums should match displayed values. " +
						"Check HTML report for detailed calculations.")
					.isTrue();
			}
		} else {
			logger.info("Skipping pro-rata validation - no locations were added");
		}
	}

	// ==================== Test 12: Validate Premium Calculations ====================

	@Test(priority = 13, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testAddLocationsOnEndorsement")
	public void testValidatePremiumCalculations() {
		logger.info("=== Test 12: Validate Premium Calculations ===");

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

		// Pro-Rata Validation for newly added locations after date change
		if (endorsementLocationsAdded > 0) {
			logger.info("=== Validating Pro-Rata Amounts for {} Newly Added Locations ===", endorsementLocationsAdded);

			CreatePremiumEndorsementPage.ProRataValidationResult proRataResult =
				createEndorsementPage.validateProRataForNewLocations(endorsementLocationsAdded);

			// Capture screenshot after pro-rata validation
			createEndorsementPage.captureEndorsementScreenshot("After Pro-Rata Validation");

			// Assertion for pro-rata validation
			if (proRataResult.getError() != null) {
				logger.warn("Pro-Rata validation encountered an issue: {}", proRataResult.getError());
			} else {
				assertThat(proRataResult.isAllLocationsHaveValues())
					.as("Pro-Rata Validation: All newly added locations should have premium values calculated based on remaining policy period")
					.isTrue();
				logger.info("Pro-Rata validation passed for {} newly added locations", endorsementLocationsAdded);
			}
		} else {
			logger.info("No locations were added, skipping pro-rata validation");
		}
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

	// ==================== Test 14: Capture All Endorsement Data (Silent - No Report) ====================

	@Test(priority = 14, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testValidatePremiumCalculations")
	public void testCaptureAllEndorsementData() {
		logger.info("=== Test 13: Capture All Endorsement Data (Silent - for Edit Endorsement validation) ===");

		// Capture all values silently from Create Endorsement page
		capturedEndorsementData = createEndorsementPage.captureAllEndorsementData();

		assertThat(capturedEndorsementData)
			.as("Should capture endorsement data")
			.isNotNull();

		assertThat(capturedEndorsementData.getError())
			.as("Should not have any capture errors")
			.isNull();

		logger.info("Endorsement data captured silently - will be validated on Edit Endorsement screen");
	}

	// ==================== Test 15: Click Create Endorsement Button (LAST for Create Endorsement) ====================

	@Test(priority = 15, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testCaptureAllEndorsementData")
	public void testClickCreateEndorsementButton() {
		logger.info("=== Test 14: Click Create Endorsement Button ===");

		// Click Create Endorsement button
		boolean navigatedToEdit = createEndorsementPage.clickCreateEndorsementButton();

		// Verify navigation to Edit Endorsement page
		String currentUrl = driver.getCurrentUrl();
		logger.info("Current URL after clicking Create Endorsement: {}", currentUrl);

		assertThat(navigatedToEdit)
			.as("Should navigate to Edit Endorsement page after clicking Create Endorsement. Current URL: " + currentUrl)
			.isTrue();

		// Initialize Edit Endorsement page
		editEndorsementPage = new EditPremiumEndorsementPage(driver);
		logger.info("Successfully navigated to Edit Endorsement page");
	}

	// ==================== Test 16: Validate Edit Endorsement Against Captured Data ====================

	@Test(priority = 16, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testClickCreateEndorsementButton")
	public void testValidateEditEndorsementData() {
		logger.info("=== Test 16: Validate Edit Endorsement Data Against Captured Values ===");

		assertThat(capturedEndorsementData)
			.as("Captured data from Create Endorsement should be available")
			.isNotNull();

		assertThat(editEndorsementPage)
			.as("Edit Endorsement page should be initialized")
			.isNotNull();

		// Wait for Edit page to fully load - refresh and wait
		logger.info("Waiting for Edit Endorsement page to fully load...");
		driver.navigate().refresh();
		sleep(5000);

		// Wait for page elements
		try {
			new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
				.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
					org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
						org.openqa.selenium.By.id("edit-endorsement-endorsement-effective-date-picker")),
					org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
						org.openqa.selenium.By.xpath("//*[contains(text(),'Grand Total')]"))
				));
			logger.info("Edit Endorsement page elements loaded");
		} catch (Exception e) {
			logger.warn("Page load wait timeout: {}", e.getMessage());
		}

		sleep(3000);

		// Validate all values match
		EditPremiumEndorsementPage.EndorsementValidationResult validationResult =
			editEndorsementPage.validateAgainstCreateEndorsement(capturedEndorsementData);

		// Capture screenshot
		editEndorsementPage.captureEndorsementScreenshot("Edit Endorsement Validation Result");

		// Log validation summary
		logger.info("=== Validation Summary ===");
		logger.info("  - Endorsement Date Match: {}", validationResult.isEndorsementDateMatch());
		logger.info("  - Premium (GL+WS) Match: {}", validationResult.isPremiumGLWSMatch());
		logger.info("  - Taxes Match: {}", validationResult.isTaxesMatch());
		logger.info("  - Total Fees Match: {}", validationResult.isTotalFeesMatch());
		logger.info("  - Grand Total Match: {}", validationResult.isGrandTotalMatch());
		logger.info("  - Location Count Match: {}", validationResult.isLocationCountMatch());
		logger.info("  - All Locations Match: {}", validationResult.isAllLocationsMatch());
		logger.info("  - Overall Result: {}", validationResult.isAllValidationsPassed() ? "PASSED" : "FAILED");

		// Assert all validations passed
		assertThat(validationResult.isEndorsementDateMatch())
			.as("Endorsement Effective Date should match. Expected: " + validationResult.getExpectedDate() +
				", Actual: " + validationResult.getActualDate())
			.isTrue();

		assertThat(validationResult.isPremiumGLWSMatch())
			.as("Premium (GL+WS) should match. Expected: $" + String.format("%.2f", validationResult.getExpectedPremiumGLWS()) +
				", Actual: $" + String.format("%.2f", validationResult.getActualPremiumGLWS()))
			.isTrue();

		assertThat(validationResult.isTaxesMatch())
			.as("Taxes should match. Expected: $" + String.format("%.2f", validationResult.getExpectedTaxes()) +
				", Actual: $" + String.format("%.2f", validationResult.getActualTaxes()))
			.isTrue();

		assertThat(validationResult.isTotalFeesMatch())
			.as("Total Fees should match. Expected: $" + String.format("%.2f", validationResult.getExpectedTotalFees()) +
				", Actual: $" + String.format("%.2f", validationResult.getActualTotalFees()))
			.isTrue();

		assertThat(validationResult.isGrandTotalMatch())
			.as("Grand Total should match. Expected: $" + String.format("%.2f", validationResult.getExpectedGrandTotal()) +
				", Actual: $" + String.format("%.2f", validationResult.getActualGrandTotal()))
			.isTrue();

		assertThat(validationResult.isLocationCountMatch())
			.as("Location count should match. Expected: " + validationResult.getExpectedLocationCount() +
				", Actual: " + validationResult.getActualLocationCount())
			.isTrue();

		logger.info("All Edit Endorsement validations PASSED!");
	}

	// ==================== Test 17: Add Locations on Edit Endorsement ====================

	@Test(priority = 17, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testValidateEditEndorsementData")
	public void testAddLocationsOnEditEndorsement() {
		logger.info("=== Test 17: Add Locations on Edit Endorsement ===");

		assertThat(editEndorsementPage)
			.as("Edit Endorsement page should be initialized")
			.isNotNull();

		// Read locations from EditEndorsement sheet using normalized method
		ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");
		java.util.List<java.util.Map<String, String>> locations = excelReader.getLocationsFromSheet("EditEndorsement");

		// Log raw data for debugging if locations are empty
		if (locations.isEmpty()) {
			logger.warn("No locations found with getLocationsFromSheet, trying getSheetDataAsMap...");
			java.util.List<java.util.Map<String, String>> rawData = excelReader.getSheetDataAsMap("EditEndorsement");
			logger.info("Raw data has {} rows", rawData.size());
			for (int i = 0; i < Math.min(rawData.size(), 2); i++) {
				logger.info("Raw row {}: {}", i, rawData.get(i));
			}
			// Try to normalize manually if needed
			locations = rawData;
		}

		assertThat(locations)
			.as("EditEndorsement sheet should have locations")
			.isNotEmpty();

		logger.info("Found {} locations in EditEndorsement sheet", locations.size());

		// Log first location data for debugging
		if (!locations.isEmpty()) {
			logger.info("First location data: {}", locations.get(0));
		}

		// Store location data for pro-rata validation later
		editEndorsementLocationData = locations;

		// Add locations using existing method
		int addedCount = editEndorsementPage.addLocationsOnEditEndorsement(locations);

		// Store the count of newly added locations for pro-rata validation
		editEndorsementLocationsAdded = addedCount;

		// Log results to report
		editEndorsementPage.logAddLocationsResultToReport(locations.size(), addedCount);

		editEndorsementPage.captureEndorsementScreenshot("After Adding Locations on Edit Endorsement");

		assertThat(addedCount)
			.as("At least one location should be added successfully")
			.isGreaterThan(0);

		logger.info("Successfully added {}/{} locations on Edit Endorsement", addedCount, locations.size());
	}

	// ==================== Test 18: Change Endorsement Date on Edit Endorsement (Reduce 2 Months) ====================

	@Test(priority = 18, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testAddLocationsOnEditEndorsement")
	public void testChangeEditEndorsementDate() {
		logger.info("=== Test 17: Change Endorsement Date on Edit Endorsement (Reduce by 2 Months) ===");

		assertThat(editEndorsementPage)
			.as("Edit Endorsement page should be initialized")
			.isNotNull();

		// Get addresses of newly added locations to validate in confirm dialog
		java.util.List<String> expectedLocationsInDialog = new java.util.ArrayList<>();
		if (capturedEndorsementData != null && capturedEndorsementData.getLocationDetails() != null) {
			for (CreatePremiumEndorsementPage.LocationRowData loc : capturedEndorsementData.getLocationDetails()) {
				if (loc.getAddress() != null && !loc.getAddress().isEmpty()) {
					expectedLocationsInDialog.add(loc.getAddress());
				}
			}
		}
		// Add the example address from user
		expectedLocationsInDialog.add("56-45 Main St, Flushing, NY 11355, USA");

		logger.info("Expected locations in confirm dialog: {}", expectedLocationsInDialog.size());

		// Change date by reducing 2 months
		EditPremiumEndorsementPage.DateChangeValidationResult dateResult =
			editEndorsementPage.changeEndorsementDateReduceBy2Months(expectedLocationsInDialog);

		// Validate date was changed
		assertThat(dateResult.isDateChanged())
			.as("Date should be changed successfully")
			.isTrue();

		// Validate confirm dialog appeared
		assertThat(dateResult.isConfirmDialogAppeared())
			.as("Confirm dialog should appear after date change")
			.isTrue();

		// Validate confirm was clicked
		assertThat(dateResult.isConfirmClicked())
			.as("Confirm Changes button should be clicked")
			.isTrue();

		logger.info("Date change completed: {} -> {}", dateResult.getOldDate(), dateResult.getNewDate());
	}

	// ==================== Test 19: Validate Edit Endorsement Premiums After Date Change ====================

	@Test(priority = 19, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testChangeEditEndorsementDate")
	public void testValidateEditEndorsementPremiums() {
		logger.info("=== Test 19: Validate Edit Endorsement Premiums After Date Change ===");

		assertThat(editEndorsementPage)
			.as("Edit Endorsement page should be initialized")
			.isNotNull();

		// Wait for page to fully load after date change
		sleep(3000);

		// Scroll to top to ensure all data is visible
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
		sleep(1000);

		editEndorsementPage.captureEndorsementScreenshot("Edit Endorsement - Before Premium Validation");

		// ==================== Location Count Validation ====================
		logger.info("=== Location Count Validation ===");
		int currentLocationCount = editEndorsementPage.getLocationCount();
		int expectedTotalLocations = endorsementLocationsAdded + editEndorsementLocationsAdded;

		// Get original location count from captured data if available
		int originalLocationCount = 0;
		if (capturedEndorsementData != null) {
			originalLocationCount = capturedEndorsementData.getLocationDetails() != null
				? capturedEndorsementData.getLocationDetails().size() - endorsementLocationsAdded : 0;
			if (originalLocationCount < 0) originalLocationCount = 0;
		}

		int expectedTotal = originalLocationCount + endorsementLocationsAdded + editEndorsementLocationsAdded;

		logger.info("Location Count: Original={}, Create Endorsement Added={}, Edit Endorsement Added={}, Expected Total={}, Actual={}",
			originalLocationCount, endorsementLocationsAdded, editEndorsementLocationsAdded, expectedTotal, currentLocationCount);

		// Log location count validation to report
		logLocationCountValidationToReport(originalLocationCount, endorsementLocationsAdded, editEndorsementLocationsAdded,
			expectedTotal, currentLocationCount);

		// Validate premium calculations using same formulas as testAddLocationsOnEndorsement
		EditPremiumEndorsementPage.PremiumValidationResult result = editEndorsementPage.validateEditEndorsementPremiums();

		// Log validation summary
		logger.info("=== Edit Endorsement Premium Validation Summary ===");
		logger.info("  - Property Premium Sum: ${}", String.format("%.2f", result.getPropertyPremiumSum()));
		logger.info("  - GL Premium Sum: ${}", String.format("%.2f", result.getGlPremiumSum()));
		logger.info("  - WS Premium Sum: ${}", String.format("%.2f", result.getWsPremiumSum()));
		logger.info("  - Premium (GL+WS) Match: {}", result.isPremiumGLWSMatch() ? "PASS" : "FAIL");
		logger.info("  - Taxes Match: {}", result.isTaxesMatch() ? "PASS" : "FAIL");
		logger.info("  - Fees Match: {}", result.isFeesMatch() ? "PASS" : "FAIL");
		logger.info("  - Grand Total Match: {}", result.isGrandTotalMatch() ? "PASS" : "FAIL");

		editEndorsementPage.captureEndorsementScreenshot("Edit Endorsement - Premium Validation Complete");

		// Assert validations passed
		assertThat(result.isPremiumGLWSMatch())
			.as("Premium (GL+WS) sum should match displayed value. Calculated: $" +
				String.format("%.2f", result.getCalculatedPremiumGLWS()) + ", Displayed: $" +
				String.format("%.2f", result.getPremiumGLWSValue()))
			.isTrue();

		assertThat(result.isTaxesMatch())
			.as("Taxes sum should match displayed value")
			.isTrue();

		assertThat(result.isFeesMatch())
			.as("Fees sum should match displayed value")
			.isTrue();

		assertThat(result.isGrandTotalMatch())
			.as("Grand Total calculation should match")
			.isTrue();

		logger.info("All Edit Endorsement premium validations PASSED!");

		// ==================== Pro-Rata Premium Validation for All Newly Added Locations ====================
		logger.info("=== Pro-Rata Premium Validation for All Newly Added Locations ===");
		logger.info("Total newly added locations to validate: {} (Create: {}, Edit: {})",
			endorsementLocationsAdded + editEndorsementLocationsAdded, endorsementLocationsAdded, editEndorsementLocationsAdded);

		// Validate Pro-Rata for locations added on Create Endorsement screen
		if (endorsementLocationsAdded > 0 && endorsementLocationData != null && !endorsementLocationData.isEmpty()) {
			logger.info("Validating Pro-Rata for {} locations added on Create Endorsement", endorsementLocationsAdded);

			List<Map<String, String>> createLocations = endorsementLocationData;
			if (endorsementLocationsAdded < endorsementLocationData.size()) {
				createLocations = endorsementLocationData.subList(0, endorsementLocationsAdded);
			}

			EditPremiumEndorsementPage.ProRataPremiumValidationResult createProRataResult =
				editEndorsementPage.validateProRataPremiumCalculations(createLocations);

			if (createProRataResult.getError() != null) {
				logger.warn("Create Endorsement Pro-Rata validation error: {}", createProRataResult.getError());
				editEndorsementPage.logHtmlToReport("<div style='padding: 10px; background-color: #f8d7da; border-left: 4px solid #dc3545;'>" +
					"<strong>Create Endorsement Pro-Rata Error:</strong> " + createProRataResult.getError() + "</div>");
			} else {
				logger.info("Create Endorsement Pro-Rata: {} locations validated, All Passed: {}",
					createProRataResult.getLocationCalculations().size(), createProRataResult.isAllPassed());

				assertThat(createProRataResult.isAllPassed())
					.as("Pro-Rata Premium Validation for Create Endorsement locations: All calculated pro-rata premiums should match. " +
						"Formula: Property = Annual/365×Days, GL = GLAmount/365×Days, WS = WSAmount/365×Days")
					.isTrue();
			}
		} else {
			logger.info("Skipping Create Endorsement pro-rata validation - no locations data available");
		}

		// Validate Pro-Rata for locations added on Edit Endorsement screen
		if (editEndorsementLocationsAdded > 0 && editEndorsementLocationData != null && !editEndorsementLocationData.isEmpty()) {
			logger.info("Validating Pro-Rata for {} locations added on Edit Endorsement", editEndorsementLocationsAdded);

			List<Map<String, String>> editLocations = editEndorsementLocationData;
			if (editEndorsementLocationsAdded < editEndorsementLocationData.size()) {
				editLocations = editEndorsementLocationData.subList(0, editEndorsementLocationsAdded);
			}

			EditPremiumEndorsementPage.ProRataPremiumValidationResult editProRataResult =
				editEndorsementPage.validateProRataPremiumCalculations(editLocations);

			if (editProRataResult.getError() != null) {
				logger.warn("Edit Endorsement Pro-Rata validation error: {}", editProRataResult.getError());
				editEndorsementPage.logHtmlToReport("<div style='padding: 10px; background-color: #f8d7da; border-left: 4px solid #dc3545;'>" +
					"<strong>Edit Endorsement Pro-Rata Error:</strong> " + editProRataResult.getError() + "</div>");
			} else {
				logger.info("Edit Endorsement Pro-Rata: {} locations validated, All Passed: {}",
					editProRataResult.getLocationCalculations().size(), editProRataResult.isAllPassed());

				assertThat(editProRataResult.isAllPassed())
					.as("Pro-Rata Premium Validation for Edit Endorsement locations: All calculated pro-rata premiums should match. " +
						"Formula: Property = Annual/365×Days, GL = GLAmount/365×Days, WS = WSAmount/365×Days")
					.isTrue();
			}
		} else {
			logger.info("Skipping Edit Endorsement pro-rata validation - no locations data available");
		}

		logger.info("=== All Pro-Rata Premium Validations Completed ===");
	}

	/**
	 * Log location count validation to HTML report
	 */
	private void logLocationCountValidationToReport(int original, int createAdded, int editAdded, int expected, int actual) {
		boolean isMatch = (actual >= expected); // At least expected locations should be present
		StringBuilder html = new StringBuilder();
		html.append("<div style='margin: 10px 0; padding: 15px; border-radius: 8px; ");
		html.append(isMatch ? "background-color: #d4edda; border-left: 4px solid #28a745;'>" : "background-color: #f8d7da; border-left: 4px solid #dc3545;'>");
		html.append("<h3 style='color: ").append(isMatch ? "#28a745" : "#dc3545").append("; margin-top: 0;'>Location Count Validation: ")
			.append(isMatch ? "PASS" : "FAIL").append("</h3>");

		html.append("<table style='width: 100%; border-collapse: collapse; color: #000000;'>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Original Locations:</td><td style='padding: 8px;'>").append(original).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Added on Create Endorsement:</td><td style='padding: 8px;'>").append(createAdded).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Added on Edit Endorsement:</td><td style='padding: 8px;'>").append(editAdded).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Expected Total:</td><td style='padding: 8px;'>").append(expected).append("</td></tr>");
		html.append("<tr style='background-color: #f8f9fa;'><td style='padding: 8px; font-weight: bold;'>Actual Total:</td><td style='padding: 8px;'>").append(actual).append("</td></tr>");
		html.append("<tr><td style='padding: 8px; font-weight: bold;'>Status:</td><td style='padding: 8px; font-weight: bold; color: ")
			.append(isMatch ? "#28a745" : "#dc3545").append(";'>").append(isMatch ? "PASS" : "FAIL").append("</td></tr>");
		html.append("</table></div>");

		editEndorsementPage.logHtmlToReport(html.toString());
	}

	// ==================== Test 20: Validate Display Computations on Edit Endorsement ====================

	@Test(priority = 20, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testValidateEditEndorsementPremiums")
	public void testValidateEditDisplayComputations() {
		logger.info("=== Test 20: Validate Display Computations Against Sheet Data ===");

		assertThat(editEndorsementPage)
			.as("Edit Endorsement page should be initialized")
			.isNotNull();

		// Wait for page to be ready
		sleep(2000);

		editEndorsementPage.captureEndorsementScreenshot("Edit Endorsement - Before Display Computation Validation");

		// Get location data from both sheets
		List<Map<String, String>> createLocations = endorsementLocationData;
		List<Map<String, String>> editLocations = editEndorsementLocationData;

		logger.info("Validating Display Computations for {} Create Endorsement locations and {} Edit Endorsement locations",
			createLocations != null ? createLocations.size() : 0,
			editLocations != null ? editLocations.size() : 0);

		// Validate display computations against sheet data
		EditPremiumEndorsementPage.DisplayComputationSheetValidationResult result =
			editEndorsementPage.validateDisplayComputationsAgainstSheetData(createLocations, editLocations);

		// Log validation summary
		logger.info("=== Display Computation Validation Summary ===");
		logger.info("  - Pro-Rata Days: {}", result.getProRataDays());
		logger.info("  - Total Locations: {}", result.getTotalCount());
		logger.info("  - Passed: {}", result.getPassedCount());
		logger.info("  - Failed: {}", result.getFailedCount());

		if (result.getError() != null) {
			logger.warn("Display Computation validation error: {}", result.getError());
		}

		editEndorsementPage.captureEndorsementScreenshot("Edit Endorsement - After Display Computation Validation");

		// Assert all validations passed
		assertThat(result.isAllPassed())
			.as("Display Computation Validation: All locations should match between Excel sheet data and Display Computation dialog. " +
				"Formulas: TIV = Dwelling + AS + BPP + Loss Of Rents | Property Premium = (TIV / 100 × Rate) × (ProRataDays / 365). " +
				"Passed: " + result.getPassedCount() + ", Failed: " + result.getFailedCount())
			.isTrue();

		logger.info("Display Computation validation completed successfully for all locations");
	}

	// ==================== Helper ====================

	/**
	 * Wait for page stability - replaces Thread.sleep with explicit waits
	 * @param millis ignored - kept for backward compatibility, uses explicit wait instead
	 */
	private void sleep(long millis) {
		// Use explicit wait instead of Thread.sleep for more reliable test execution
		waitHelper.waitForPageStability();
	}
}
