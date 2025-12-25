package com.automation.tests;

import com.automation.base.DriverManager;
import com.automation.listeners.TestListener;
import com.automation.pages.CreateQuotePage;
import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.pages.ResetPage;
import com.automation.utils.ConfigReader;
import com.automation.utils.ExcelReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Listeners(TestListener.class)
public class CreateQuoteTest {

	protected static final Logger logger = LogManager.getLogger(CreateQuoteTest.class);
	protected static ConfigReader config = ConfigReader.getInstance();

	private LoginPage loginPage;
	private ResetPage resetPage;
	private HomePage homePage;
	private CreateQuotePage createQuotePage;
	private WebDriver driver;

	private static final String ADMIN_EMAIL = "admin@cpiai.com";
	private static final String ADMIN_PASSWORD = "Admin@123";
	private static final String BASE_URL = "https://cpiai-dev.attri.ai/";

	// ==================== Setup ====================

	@BeforeClass(alwaysRun = true)
	public void initDriver() {
		driver = DriverManager.getDriver();
		TestListener.setDriver(driver);

		loginPage = new LoginPage(driver);
		resetPage = new ResetPage(driver);
		homePage = new HomePage(driver);
		createQuotePage = new CreateQuotePage(driver);

		driver.get(config.getProperty("base.url", BASE_URL));
		sleep(2000);
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		// DriverManager.quitDriver();
	}

	// ==================== Tests ====================

	@Test(priority = 1, groups = { "PositiveTestFlow" })
	@Description("Login with valid admin credentials")
	public void testLoginWithValidCredentials() {
		loginPage.waitForPageLoad();
		loginPage.captureScreenshotToReport("Login Page - Before Login");

		loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
		sleep(3000);

		if (driver.getCurrentUrl().contains("/reset_password")) {
			assertThat(resetPage.isSkipButtonDisplayed()).isTrue();
			resetPage.clickSkipButton();
			sleep(2000);
		}

		loginPage.captureScreenshotToReport("Login - Successfully Logged In");

		DriverManager.setLoggedIn(true);
		DriverManager.setOnHomePage(true);
	}

	@Test(priority = 2, groups = { "PositiveTestFlow" }, dependsOnMethods = "testLoginWithValidCredentials")
	@Description("Navigate to Create Quote page")
	public void testNavigateToCreateQuotePage() {
		// Use full URL with driver.get() for reliable navigation
		String quoteUrl = config.getProperty("base.url", BASE_URL) + "new_quote";
		logger.info("Navigating to: {}", quoteUrl);
		driver.get(quoteUrl);
		sleep(5000);

		String currentUrl = driver.getCurrentUrl();
		logger.info("Final URL: {}", currentUrl);

		// If redirected to login, try navigation again
		if (currentUrl.contains("/login")) {
			// Try JavaScript navigation as backup
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.location.href='/new_quote'");
			sleep(5000);
			currentUrl = driver.getCurrentUrl();
			logger.info("After retry URL: {}", currentUrl);
		}

		boolean onCreateQuotePage = currentUrl.contains("/new_quote");

		if (!onCreateQuotePage) {
			createQuotePage.captureScreenshot("Navigation Failed");
		}

		assertThat(onCreateQuotePage).isTrue();

		createQuotePage = new CreateQuotePage(driver);
		createQuotePage.captureScreenshot("Create Quote Page Loaded");
	}

	// ==================== Validation Tests ====================

	@Test(priority = 10, dependsOnMethods = "testLoginWithValidCredentials")
	@Description("Validate form with empty data - submit should be disabled")
	public void testWithEmptyData() {
		logger.info("Starting empty data validation test");

		navigateToCreateQuotePage();

		createQuotePage = new CreateQuotePage(driver);

		boolean validationPassed = createQuotePage.validateInvalidDataAndCaptureScreenshots();

		logger.info("Empty data validation result: {}", validationPassed ? "PASSED" : "FAILED");
		assertThat(validationPassed).as("Submit button should be disabled when form has empty data").isTrue();
	}

	@Test(priority = 11, dependsOnMethods = "testLoginWithValidCredentials")
	@Description("Validate submit button remains disabled until all data is entered")
	public void testSubmitDisabledUntilComplete() {
		logger.info("Starting submit button disabled until complete validation test");

		navigateToCreateQuotePage();

		createQuotePage = new CreateQuotePage(driver);

		boolean validationPassed = createQuotePage.validateSubmitDisabledUntilComplete();

		logger.info("Submit disabled until complete validation result: {}", validationPassed ? "PASSED" : "FAILED");
		assertThat(validationPassed).as("Submit button should remain disabled until all required fields are filled")
				.isTrue();
	}

	// ==================== Reset Form Test ====================

	@Test(priority = 15, dependsOnMethods = "testSubmitDisabledUntilComplete")
	@Description("Reset form data and verify previously entered data is removed")
	public void testResetFormData() {
		logger.info("=== Starting Reset Form Data Test ===");

		// Form should have data from previous test, click reset
		boolean resetSuccess = createQuotePage.resetFormAndVerify();

		logger.info("Reset form result: {}", resetSuccess ? "PASSED" : "FAILED");
		assertThat(resetSuccess).as("Form data should be cleared after clicking Reset button").isTrue();

		logger.info("=== Reset Form Data Test Complete ===");
	}

	// ==================== Final Form Fill Test (Runs Last) ====================

	@Test(priority = 20, groups = { "PositiveTestFlow" }, dependsOnMethods = { "testLoginWithValidCredentials" })
	@Description("Fill Create Quote form with valid data and add locations with different payment plans and property types")
	public void testFillQuoteFormWithValidDataFinal() {
		// Read all config from Excel CreateQuoteConfig sheet
		ExcelReader configReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");

		// Print all config values for debugging
		configReader.printAllConfig();

		String locationMethod = configReader.getLocationMethod();
		String glAmount = configReader.getGLAmount();
		String wsAmount = configReader.getWSAmount();
		String uploadPath = configReader.getUploadPath();
		String carrier = configReader.getCarrier();
		String agent = configReader.getAgent();
		String insured = configReader.getInsured();
		String state = configReader.getState();
		String animalLiability = configReader.getAnimalLiability();
		String policyFee = configReader.getPolicyFee();

		logger.info("=== FINAL CONFIG VALUES BEING USED ===");
		logger.info("Location Method: {}", locationMethod);
		logger.info("Agent: {}", agent != null ? agent : "First Available");
		logger.info("Insured: {}", insured != null ? insured : "First Available");
		logger.info("Carrier: {}", carrier);
		logger.info("State: {}", state);
		logger.info("GL: {}, WS: {}, Policy Fee: {}", glAmount, wsAmount, policyFee);
		logger.info("Animal Liability: {}", animalLiability);

		// Get all location data from Excel for manual add mode
		List<Map<String, String>> allLocations = getLocationDataFromExcel();
		int expectedLocationCount = allLocations.size();

		logger.info("=== FINAL TEST: Fill Create Quote form (Method: {}) ===", locationMethod);

		navigateToCreateQuotePage();

		createQuotePage = new CreateQuotePage(driver);
		createQuotePage.waitForPageReady();

		// Select Agent (from config or first available)
		if (agent != null && !agent.isEmpty()) {
			createQuotePage.selectAgentByName(agent);
		} else {
			createQuotePage.selectFirstAvailableAgent();
		}
		sleep(1500);

		// Select Insured (from config or first available)
		if (insured != null && !insured.isEmpty()) {
			createQuotePage.selectInsuredByName(insured);
		} else {
			createQuotePage.selectFirstAvailableInsured();
		}
		sleep(1500);

		// Select Carrier (from config or first available)
		if (carrier != null && !carrier.isEmpty()) {
			createQuotePage.selectCarrierByName(carrier);
		} else {
			createQuotePage.selectFirstAvailableCarrier();
		}
		sleep(1500);

		// Set GL, WS, Policy Fee from Excel values
		createQuotePage.setGeneralLiabilityAmount(glAmount);
		sleep(500);
		createQuotePage.setWaterSewerBackupAmount(wsAmount);
		sleep(500);
		createQuotePage.setPolicyFee(policyFee);
		sleep(500);

		// Set Animal Liability from config
		if ("Yes".equalsIgnoreCase(animalLiability)) {
			createQuotePage.selectAnimalLiabilityYes();
		} else {
			createQuotePage.selectAnimalLiabilityNo();
		}
		sleep(500);

		// Select State from config
		createQuotePage.selectState(state);
		sleep(1000);

		int locationsAdded = 0;

		// Check LocationMethod from CreateQuoteConfig sheet
		// Supports: "Upload", "upload", "Upload File", "upload file" for upload mode
		// Supports: "Add Manually", "add manually", "Manual" for manual mode
		String locationMethodLower = locationMethod.trim().toLowerCase();
		boolean isUploadMode = locationMethodLower.equals("upload") ||
							   locationMethodLower.equals("upload file") ||
							   locationMethodLower.contains("upload");

		if (isUploadMode) {
			// ==================== UPLOAD FILE MODE ====================
			logger.info("=== UPLOAD FILE MODE: Uploading locations from file ===");
			logger.info("Location Method detected: '{}' -> Upload Mode", locationMethod);

			if (uploadPath == null || uploadPath.trim().isEmpty()) {
				logger.error("Upload path is empty in CreateQuoteConfig sheet (B5)");
				assertThat(false)
						.as("Upload path must be specified in CreateQuoteConfig sheet B5 when using Upload File mode")
						.isTrue();
			}

			logger.info("Upload Path from Excel: {}", uploadPath);
			createQuotePage.captureScreenshot("Before File Upload");

			// Click Upload button and upload the file
			boolean uploadSuccess = createQuotePage.clickUploadButtonAndUploadFile(uploadPath);

			if (uploadSuccess) {
				logger.info("File uploaded successfully: {}", uploadPath);
				createQuotePage.captureScreenshot("After File Upload");
				sleep(5000); // Wait for file processing and locations to load
			} else {
				logger.error("File upload failed: {}", uploadPath);
				createQuotePage.captureScreenshot("File Upload Failed");
				assertThat(uploadSuccess).as("File upload should be successful").isTrue();
			}

			// Get location count from frontend after upload
			int frontendLocationCount = createQuotePage.getLocationCount();
			logger.info("Frontend location count after upload: {}", frontendLocationCount);

			// Store location data from Excel for Display Computation and PDF validation
			logger.info("=== STORING EXCEL LOCATION DATA FOR VALIDATION ===");
			createQuotePage.storeLocationDataFromExcel(allLocations, glAmount, wsAmount);

			// Update stored locations with actual values from frontend table
			createQuotePage.updateStoredLocationsFromFrontendTable();

			// Verify locations from file are uploaded - compare with expected count from CreateQuote sheet
			logger.info("=== VERIFYING UPLOADED LOCATIONS ===");
			logger.info("Expected locations (from CreateQuote sheet): {}", expectedLocationCount);
			logger.info("Frontend location count after upload: {}", frontendLocationCount);

			createQuotePage.captureScreenshot("Final - " + frontendLocationCount + " of " + expectedLocationCount + " Locations After Upload");

			// Assert that locations were loaded
			assertThat(frontendLocationCount).as("Locations should be loaded from uploaded file").isGreaterThan(0);

			// Assert that frontend count matches expected count from Excel CreateQuote sheet
			assertThat(frontendLocationCount)
					.as("Uploaded location count should match expected count from CreateQuote sheet")
					.isEqualTo(expectedLocationCount);

			logger.info("=== UPLOAD FILE MODE COMPLETE - {} LOCATIONS UPLOADED AND VERIFIED ===", frontendLocationCount);

		} else {
			// ==================== ADD MANUALLY MODE ====================
			logger.info("=== ADD MANUALLY MODE: Adding {} locations from CreateQuote sheet ===", expectedLocationCount);

			for (int i = 0; i < allLocations.size(); i++) {
				try {
					Map<String, String> location = allLocations.get(i);
					String locationName = location.getOrDefault("LocationName", "Location" + (i + 1));
					String address = location.getOrDefault("Address", "");

					// Log all location data from Excel
					logger.info("=== Location {} Data from Excel ===", i + 1);
					logger.info("  Address: {}", address);
					logger.info("  SqFt: {}", location.getOrDefault("SqFt", "1200"));
					logger.info("  CoverageA: {}", location.getOrDefault("CoverageA", "50000"));
					logger.info("  CoverageB: {}", location.getOrDefault("CoverageB", "5000"));
					logger.info("  CoverageC: {}", location.getOrDefault("CoverageC", "5000"));
					logger.info("  LossOfRents: {}", location.getOrDefault("LossOfRents", "5000"));
					logger.info("  SuggestedRate: {}", location.getOrDefault("SuggestedRate", "0.5"));

					// Get PropertyType (RS = Residential, RM = Multi-Family)
					String propertyType = location.getOrDefault("PropertyType", "RS");
					String units = location.getOrDefault("Units", "1");
					logger.info("  PropertyType: {}, Units: {}", propertyType, units);

					// Check for both "PaymentPlan" and "Payment Plan" column names
					String paymentPlan = location.get("PaymentPlan");
					if (paymentPlan == null || paymentPlan.trim().isEmpty()) {
						paymentPlan = location.get("Payment Plan");
					}
					if (paymentPlan == null || paymentPlan.trim().isEmpty()) {
						paymentPlan = "Paid In Full"; // Default
					}
					logger.info("  PaymentPlan: {}", paymentPlan);

					// Prepare mortgagee data if present (for Escrow payment plan)
					Map<String, String> mortgageeData = null;
					String mortgageeName = location.get("MortgageeName");
					String mortgageeAddress = location.get("MortgageeAddress");
					if (mortgageeName != null && !mortgageeName.isEmpty()) {
						mortgageeData = new java.util.HashMap<>();
						mortgageeData.put("MortgageeName", mortgageeName);
						mortgageeData.put("MortgageeAddress", mortgageeAddress);
						logger.info("  Mortgagee: {} - {}", mortgageeName, mortgageeAddress);
					}

					logger.info("=== Adding Location {}: {} ===", i + 1, locationName);

					if (createQuotePage.addLocationAndVerify(location, paymentPlan, mortgageeData)) {
						locationsAdded++;
						logger.info("Location {} ({}) added successfully", i + 1, locationName);
					} else {
						logger.warn("Location {} ({}) may not have been added properly", i + 1, locationName);
					}
					sleep(1500);

				} catch (Exception e) {
					logger.error("Error adding location {}: {}", i + 1, e.getMessage());
					createQuotePage.captureScreenshot("Error Adding Location " + (i + 1));
					// Continue with next location instead of failing
				}
			}

			// Screenshot after adding all locations
			createQuotePage.captureScreenshot("After Adding All Locations");

			// CRITICAL VALIDATION: Verify location count on frontend matches Excel rows
			logger.info("=== VALIDATING LOCATION COUNT ===");
			int frontendLocationCount = createQuotePage.getLocationCount();

			logger.info("Expected locations (from Excel): {}", expectedLocationCount);
			logger.info("Locations added successfully: {}", locationsAdded);
			logger.info("Frontend location count: {}", frontendLocationCount);

			// Take final screenshot
			createQuotePage.captureScreenshot(
					"Final - " + frontendLocationCount + " of " + expectedLocationCount + " Locations");

			// Assert that all locations were added
			assertThat(locationsAdded).as("All locations from Excel should be added successfully (added count)")
					.isEqualTo(expectedLocationCount);

			// Assert that frontend shows correct count
			assertThat(frontendLocationCount).as("Frontend location count should match Excel row count")
					.isEqualTo(expectedLocationCount);

			logger.info("=== ADD MANUALLY MODE COMPLETE - {} LOCATIONS ADDED AND VERIFIED ===", frontendLocationCount);
		}

		// ==================== RE-SELECT CARRIER AFTER LOCATIONS ARE ADDED ====================
		// Application may reset carrier after file upload or location additions
		// Re-select to ensure correct carrier from Excel config is used for PDF validation
		logger.info("=== RE-SELECTING CARRIER AFTER LOCATIONS ADDED ===");
		if (carrier != null && !carrier.isEmpty()) {
			sleep(1000);
			createQuotePage.selectCarrierByName(carrier);
			sleep(1500);
			logger.info("Carrier re-selected to: {}", carrier);
		}

		configReader.close();
	}

	@Test(priority = 21, groups = {
			"PositiveTestFlow" }, dependsOnMethods = "testFillQuoteFormWithValidDataFinal", alwaysRun = true)
	@Description("Validate premium calculations - sum of columns should match displayed totals")
	public void testValidatePremiumCalculations() {
		logger.info("=== Starting Premium Calculation Validation ===");

		// Ensure we're on the quote page with locations
		if (createQuotePage == null) {
			createQuotePage = new CreateQuotePage(driver);
		}

		sleep(2000); // Wait for page to settle

		boolean calculationsValid = createQuotePage.validatePremiumCalculations();

		assertThat(calculationsValid)
				.as("Premium calculations should match: Premium(GL+WS), Taxes, Fees, and Grand Total").isTrue();

		logger.info("=== Premium Calculation Validation Complete ===");
	}

	@Test(priority = 22, groups = {
			"PositiveTestFlow" }, dependsOnMethods = "testValidatePremiumCalculations", alwaysRun = true)
	@Description("Validate GL calculation - RM: GL × Units, RS: no multiplication")
	public void testValidateGLCalculation() {
		logger.info("=== Starting GL Calculation Validation ===");

		if (createQuotePage == null) {
			createQuotePage = new CreateQuotePage(driver);
		}

		sleep(1000);

		// Get current GL amount from frontend
		String glAmountStr = createQuotePage.getGeneralLiabilityAmount();
		double glAmount = Double.parseDouble(glAmountStr.isEmpty() ? "0" : glAmountStr);
		logger.info("GL Amount from frontend: {}", glAmount);

		// Get carrier from Excel config
		ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");
		String carrier = excelReader.getCarrier();

		// Get full location data from Excel (Address, PropertyType, Units)
		List<Map<String, String>> locations = excelReader.getAllLocationData();
		excelReader.close();

		logger.info("GL Amount: {}, Carrier: {}, Locations: {}", glAmount, carrier, locations.size());

		// Pass full location data for validation by address
		boolean glValid = createQuotePage.validateGLCalculation(glAmount, locations, carrier);

		assertThat(glValid)
				.as("GL calculation should be correct based on PropertyType (RM: GL × Units, RS: no multiplication)")
				.isTrue();

		logger.info("=== GL Calculation Validation Complete ===");
	}

	@Test(priority = 23, groups = {
			"PositiveTestFlow" }, dependsOnMethods = "testValidateGLCalculation", alwaysRun = true)
	@Description("Validate Display Computation - TIV and Property Premium calculations for each location")
	public void testValidateDisplayComputation() {
		logger.info("=== Starting Display Computation Validation ===");

		if (createQuotePage == null) {
			createQuotePage = new CreateQuotePage(driver);
		}

		sleep(2000);

		// Click Display Computation button and validate all locations
		CreateQuotePage.DisplayComputationResult result = createQuotePage.validateDisplayComputation();

		// Assert validation passed
		assertThat(result.isValid())
				.as("Display Computation validation should pass - TIV = D+AS+BPP+LOR, Property Premium = (TIV/100)*Rate")
				.isTrue();

		logger.info("=== Display Computation Validation Complete ===");
	}

	@Test(priority = 24, groups = {
			"PositiveTestFlow" }, dependsOnMethods = "testValidateDisplayComputation", alwaysRun = true)
	@Description("Download PDF and validate Summary page - Carrier, Insured, Dates, Premiums, Grand Total")
	public void testDownloadAndValidatePDF() {
		logger.info("=== Starting PDF Download and Validation Test ===");

		if (createQuotePage == null) {
			createQuotePage = new CreateQuotePage(driver);
		}

		sleep(2000);

		// Update stored locations with Display Computation taxes for accurate Full Term calculation
		// Full Term = Property Premium + GL Premium + WS Premium + Taxes
		createQuotePage.updateStoredLocationsFromDisplayComputation();
		sleep(1000);

		// Download and validate PDF
		CreateQuotePage.PDFValidationResult result = createQuotePage.downloadAndValidatePDF();

		// Log validation report
		logger.info("PDF Validation Report:\n{}", result.getReport());

		// Assert validation passed
		assertThat(result.isValid()).as("PDF validation should pass - all values should match quote screen").isTrue();

		logger.info("=== PDF Download and Validation Test Complete ===");
	}

	@Test(priority = 25, groups = {
			"PositiveTestFlow" }, alwaysRun = true)
	@Description("Submit Quote and validate 'Quote created successfully' toast message - Runs even if previous tests fail")
	public void testSubmitQuoteAndValidate() {
		logger.info("=== Starting Quote Submission Test ===");

		if (createQuotePage == null) {
			createQuotePage = new CreateQuotePage(driver);
		}

		sleep(2000);

		// Click Submit and validate success toast
		CreateQuotePage.SubmitQuoteResult result = createQuotePage.clickSubmitAndValidateQuoteCreation();

		// Log result
		logger.info("Submit Quote Result: Success={}, Message={}, Toast={}",
				result.isSuccess(), result.getMessage(), result.getToastMessage());

		// Assert submission was successful
		assertThat(result.isSuccess())
				.as("Quote submission should be successful with 'Quote created successfully' toast")
				.isTrue();

		logger.info("=== Quote Submission Test Complete - {} ===",
				result.isSuccess() ? "SUCCESS" : "FAILED");
	}

	/**
	 * Get all location data from Excel TestData.xlsx - CreateQuote sheet Flexible:
	 * Adds ALL locations present in Excel (user can add/remove as needed)
	 */
	private List<Map<String, String>> getLocationDataFromExcel() {
		try {
			ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/TestData.xlsx");

			// Get all locations from Excel
			List<Map<String, String>> locations = excelReader.getAllLocationData();

			// If no locations found, create default sheet
			if (locations.isEmpty()) {
				logger.info("No locations found, creating default sheet with 4 locations");
				excelReader.createCreateQuoteSheet();
				locations = excelReader.getAllLocationData();
			}

			logger.info("========== Loaded {} locations from Excel ==========", locations.size());
			for (int i = 0; i < locations.size(); i++) {
				Map<String, String> loc = locations.get(i);
				// Check both column name variations
				String paymentPlan = loc.get("PaymentPlan");
				if (paymentPlan == null || paymentPlan.trim().isEmpty()) {
					paymentPlan = loc.get("Payment Plan");
				}
				logger.info("Location {}: Name={}, Address={}, PaymentPlan={}", i + 1,
						loc.getOrDefault("LocationName", "N/A"), loc.getOrDefault("Address", "N/A"),
						paymentPlan != null ? paymentPlan : "N/A");
			}

			// Log all column names found for debugging
			if (!locations.isEmpty()) {
				logger.info("Excel columns found: {}", locations.get(0).keySet());
			}

			excelReader.close();
			return locations;
		} catch (Exception e) {
			return getDefaultLocations();
		}
	}

	/**
	 * Get default locations if Excel fails
	 */
	private List<Map<String, String>> getDefaultLocations() {
		List<Map<String, String>> locations = new java.util.ArrayList<>();

		Map<String, String> loc1 = new java.util.HashMap<>();
		loc1.put("LocationName", "Location1");
		loc1.put("Address", "123 William St, New York, NY 10038, USA");
		loc1.put("SqFt", "1200");
		loc1.put("CoverageA", "50000");
		loc1.put("CoverageB", "5000");
		loc1.put("CoverageC", "5000");
		loc1.put("LossOfRents", "5000");
		loc1.put("SuggestedRate", "0.5");
		loc1.put("PaymentPlan", "Paid In Full");
		locations.add(loc1);

		return locations;
	}

	// ==================== Utilities ====================

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Navigate to Create Quote page with retry logic
	 */
	private void navigateToCreateQuotePage() {
		String quoteUrl = config.getProperty("base.url", BASE_URL) + "new_quote";
		logger.info("Navigating to: {}", quoteUrl);
		driver.get(quoteUrl);
		sleep(5000);

		String currentUrl = driver.getCurrentUrl();

		// If redirected to login, session may have expired - try JS navigation
		if (currentUrl.contains("/login")) {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.location.href='/new_quote'");
			sleep(5000);
		}

		logger.info("Current URL after navigation: {}", driver.getCurrentUrl());
	}

	// ==================== Custom Annotation ====================

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Description {
		String value();
	}
}
