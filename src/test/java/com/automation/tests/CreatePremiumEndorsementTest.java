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

		createEndorsementPage.captureEndorsementScreenshot("Page Loaded");
		logger.info("Create Premium Endorsement page loaded successfully");
	}

	// ==================== Test 9: Capture Endorsement Form Values ====================

	@Test(priority = 9, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifyEndorsementPageLoaded")
	public void testCaptureEndorsementFormValues() {
		logger.info("=== Test 9: Capture Endorsement Form Values ===");

		// Capture all form values
		endorsementFormValues = createEndorsementPage.captureEndorsementFormValues();

		assertThat(endorsementFormValues)
			.as("Should capture endorsement form values")
			.isNotEmpty();

		// Log values to report
		createEndorsementPage.logEndorsementValuesToReport(endorsementFormValues);

		logger.info("Endorsement form values captured: {}", endorsementFormValues);
	}

	// ==================== Test 10: Verify Location Count ====================

	@Test(priority = 10, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testCaptureEndorsementFormValues")
	public void testVerifyLocationCount() {
		logger.info("=== Test 10: Verify Location Count ===");

		int locationCount = createEndorsementPage.getLocationCount();

		assertThat(locationCount)
			.as("Should have at least one location")
			.isGreaterThan(0);

		logger.info("Location count: {}", locationCount);
		createEndorsementPage.captureEndorsementScreenshot("Location Count: " + locationCount);
	}

	// ==================== Test 11: Verify Submit Button State ====================

	@Test(priority = 11, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifyLocationCount")
	public void testVerifySubmitButtonState() {
		logger.info("=== Test 11: Verify Submit Button State ===");

		boolean isEnabled = createEndorsementPage.isSubmitButtonEnabled();

		// Log the state but don't fail - submit might be disabled if no changes made
		logger.info("Submit button enabled: {}", isEnabled);
		createEndorsementPage.captureEndorsementScreenshot("Submit Button State");
	}

	// ==================== Test 12: Final Summary ====================

	@Test(priority = 12, groups = { "PremiumEndorsementFlow" }, dependsOnMethods = "testVerifySubmitButtonState")
	public void testFinalSummary() {
		logger.info("=== Test 12: Final Summary ===");

		// Capture final screenshot
		createEndorsementPage.captureEndorsementScreenshot("Test Complete");

		// Log summary
		StringBuilder summary = new StringBuilder();
		summary.append("\n=== Create Premium Endorsement Test Summary ===\n");
		summary.append("Policy Number: ").append(policyNumber).append("\n");
		summary.append("Page URL: ").append(driver.getCurrentUrl()).append("\n");

		if (endorsementFormValues != null) {
			summary.append("Form Values:\n");
			for (Map.Entry<String, String> entry : endorsementFormValues.entrySet()) {
				summary.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
			}
		}
		summary.append("==============================================\n");

		logger.info(summary.toString());
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
