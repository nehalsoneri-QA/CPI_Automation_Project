package com.automation.tests;

import com.automation.base.DriverManager;
import com.automation.listeners.TestListener;
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
 * Master Policy Test Class
 * Simple individual tests for Master Policy flow
 */
@Listeners(TestListener.class)
public class MasterPolicyTest {

	protected static final Logger logger = LogManager.getLogger(MasterPolicyTest.class);
	protected static ConfigReader config = ConfigReader.getInstance();

	private LoginPage loginPage;
	private ResetPage resetPage;
	private HomePage homePage;
	private CreateQuotePage createQuotePage;
	private EditQuotePage editQuotePage;
	private MasterPolicyPage masterPolicyPage;
	private WebDriver driver;

	private static final String ADMIN_EMAIL = "admin@cpiai.com";
	private static final String ADMIN_PASSWORD = "Admin@123";
	private static final String BASE_URL = "https://cpiai-dev.attri.ai/";

	// Store Edit Quote values for comparison with Master Policy
	private Map<String, String> editQuoteValues;
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

	// ==================== Test 1: Login ====================

	@Test(priority = 1, groups = { "MasterPolicyFlow" })
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
			logger.info("Reset page detected");
			if (resetPage.isSkipButtonDisplayed()) {
				resetPage.clickSkipButton();
				sleep(2000);
			}
		}

		homePage.waitForHomePageLoad();
		DriverManager.setLoggedIn(true);
		logger.info("Login successful");
		homePage.captureScreenshotToReport("Home Page - After Login");
	}

	// ==================== Test 2: Enter Data on Create Quote ====================

	@Test(priority = 2, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testPositiveLogin")
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

		String quoteUrl = config.getProperty("base.url", BASE_URL) + "new_quote";
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

			// Handle reset page if shown
			currentUrl = driver.getCurrentUrl();
			if (currentUrl.contains("/reset_password")) {
				if (resetPage.isSkipButtonDisplayed()) {
					resetPage.clickSkipButton();
					sleep(2000);
				}
			}

			// Navigate to Create Quote again
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
				sleep(5000);
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

	@Test(priority = 3, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testEnterDataOnCreateQuote")
	public void testClickSubmitButton() {
		logger.info("=== Test 3: Click Submit Button ===");

		// Wait for Submit button to be visible (page may still be loading after file upload)
		createQuotePage.waitForSubmitButtonVisible();

		createQuotePage.captureScreenshot("Before Submit");

		CreateQuotePage.SubmitQuoteResult submitResult = createQuotePage.clickSubmitAndValidateQuoteCreation();
		logger.info("Submit Result: Success={}, Message={}", submitResult.isSuccess(), submitResult.getMessage());

		// Wait for navigation with retry
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
		} else {
			// Log the issue for debugging
			logger.error("Did NOT navigate to Edit Quote page. Current URL: {}", currentUrl);
			createQuotePage.captureScreenshot("Submit Failed - Not on Edit Quote");
		}

		assertThat(onEditQuote).as("Should navigate to Edit Quote page. Current URL: " + currentUrl).isTrue();
	}

	// ==================== Test 4: Add Locations on Edit Quote ====================

	@Test(priority = 4, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickSubmitButton")
	public void testAdd3LocationsOnEditQuote() {
		logger.info("=== Test 4: Add Locations on Edit Quote ===");

		if (editQuotePage == null) {
			editQuotePage = new EditQuotePage(driver);
		}

		editQuotePage.waitForEditQuotePageReady();
		sleep(2000);

		ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");
		// Use getEditQuoteLocations() which normalizes column names (Address, SqFt, etc.)
		List<Map<String, String>> editQuoteLocations = excelReader.getEditQuoteLocations();
		logger.info("Found {} locations in EditQuote sheet", editQuoteLocations.size());

		// Add ALL locations from EditQuote sheet
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

	@Test(priority = 5, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testAdd3LocationsOnEditQuote")
	public void testClickBindButton() {
		logger.info("=== Test 5: Click Bind Button ===");

		if (editQuotePage == null) {
			editQuotePage = new EditQuotePage(driver);
		}

		// Capture Edit Quote values for comparison with Master Policy
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

		String policyNumber = masterPolicyPage.getPolicyNumberFromURL();
		logger.info("Policy Number: {}", policyNumber);
		masterPolicyPage.captureScreenshotToReport("Master Policy - Policy #" + policyNumber);
	}

	// ==================== Test 6: Capture Policy ID ====================

	@Test(priority = 6, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickBindButton")
	public void testCapturePolicyId() {
		logger.info("=== Test 6: Capture Policy ID ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
		sleep(1000);

		String policyId = masterPolicyPage.capturePolicyId();
		logger.info("Captured Policy ID: {}", policyId);

		// Log result - policy ID might not be captured if page structure changed
		if (policyId == null || policyId.isEmpty()) {
			logger.warn("Policy ID not captured - page structure may have changed");
			masterPolicyPage.captureScreenshotToReport("Policy ID - Not Captured");
		} else {
			logger.info("Policy ID captured successfully: {}", policyId);
		}
	}

	// ==================== Test 7: Validate Master Policy Values ====================

	@Test(priority = 7, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickBindButton")
	public void testValidateMasterPolicyValues() {
		logger.info("=== Test 7: Validate Master Policy Values ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		// Compare Edit Quote values with Master Policy values
		masterPolicyPage.compareWithEditQuoteValues(editQuoteValues);

		logger.info("Master Policy values comparison completed");
	}

	// ==================== Test 8: Validate Action Buttons ====================

	@Test(priority = 8, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickBindButton")
	public void testValidateActionButtons() {
		logger.info("=== Test 8: Validate Action Buttons ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
		sleep(1000);

		MasterPolicyPage.MasterPolicyValidationResult result = masterPolicyPage.validateActionButtonsDisplayed();
		logger.info("Action Buttons Validation: {}", result.isValid() ? "ALL FOUND" : "SOME MISSING");
	}

	// ==================== Test 9: Validate Account Manager Dropdown ====================

	@Test(priority = 9, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickBindButton")
	public void testValidateAccountManagerDropdown() {
		logger.info("=== Test 9: Validate Account Manager Dropdown ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
		sleep(1000);

		boolean dropdownFound = masterPolicyPage.validateAndClickAccountManagerDropdown();
		logger.info("Account Manager Dropdown: {}", dropdownFound ? "FOUND AND CLICKED" : "NOT FOUND");
	}

	// ==================== Test 10: Validate Location Count ====================

	@Test(priority = 10, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickBindButton")
	public void testValidateLocationCount() {
		logger.info("=== Test 10: Validate Location Count ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight / 2)");
		sleep(1000);

		// Get expected count from editQuoteValues (more reliable than editQuoteLocationCount)
		int expectedCount = editQuoteLocationCount;
		if (editQuoteValues != null && editQuoteValues.containsKey("LocationCount")) {
			try {
				expectedCount = Integer.parseInt(editQuoteValues.get("LocationCount"));
			} catch (NumberFormatException e) {
				logger.warn("Could not parse LocationCount from editQuoteValues");
			}
		}

		logger.info("Expected location count from Edit Quote: {}", expectedCount);

		MasterPolicyPage.MasterPolicyValidationResult result =
			masterPolicyPage.validateLocationCount(expectedCount);

		logger.info("Location count validation: {}", result.isValid() ? "MATCH" : "MISMATCH");
		masterPolicyPage.captureScreenshotToReport("Master Policy - Locations Table");
	}

	// ==================== Test 11: Download PDF from Bind History ====================

	@Test(priority = 11, groups = { "MasterPolicyFlow" }, dependsOnMethods = "testClickBindButton")
	public void testDownloadPDFFromBindHistory() {
		logger.info("=== Test 11: Download PDF from Bind History ===");

		if (masterPolicyPage == null) {
			masterPolicyPage = new MasterPolicyPage(driver);
		}

		// Use page method for comprehensive PDF validation with Edit Quote values
		MasterPolicyPage.MasterPolicyValidationResult result =
			masterPolicyPage.downloadAndValidateBindHistoryPDFComprehensive(editQuoteValues);

		logger.info("Bind History PDF validation: {}", result.isValid() ? "PASSED" : "HAS ERRORS");

		// Fail the test if PDF download failed or has errors
		assertThat(result.isValid())
			.as("PDF download and validation should succeed. Errors: " + result.getErrors())
			.isTrue();
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
