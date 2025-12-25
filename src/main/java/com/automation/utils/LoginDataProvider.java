package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Login Data Provider - Fetches login credentials from Excel file
 * Provides methods to get credentials for different test scenarios
 */
public class LoginDataProvider {

    private static final Logger logger = LogManager.getLogger(LoginDataProvider.class);
    private static final String EXCEL_PATH = "src/test/resources/testdata/TestData.xlsx";
    private static final String LOGIN_SHEET = "LoginData";
    private static final String USER_SHEET = "UserData";

    private final ExcelReader excelReader;

    /**
     * Constructor - Initialize with default Excel path
     */
    public LoginDataProvider() {
        this.excelReader = new ExcelReader(EXCEL_PATH);
        logger.info("LoginDataProvider initialized with Excel: {}", EXCEL_PATH);
    }

    /**
     * Constructor - Initialize with custom Excel path
     */
    public LoginDataProvider(String excelPath) {
        this.excelReader = new ExcelReader(excelPath);
        logger.info("LoginDataProvider initialized with Excel: {}", excelPath);
    }

    // ==================== Get Valid Credentials ====================

    /**
     * Get valid admin credentials from Excel
     * @return Map with Email and Password
     */
    public Map<String, String> getValidAdminCredentials() {
        logger.info("Fetching valid admin credentials from Excel");
        return getCredentialsByTestCase("TC_Login_001");
    }

    /**
     * Get credentials by test case name
     * @param testCaseName Test case identifier (e.g., TC_Login_001)
     * @return Map with all columns for that test case
     */
    public Map<String, String> getCredentialsByTestCase(String testCaseName) {
        logger.info("Fetching credentials for test case: {}", testCaseName);
        List<Map<String, String>> allData = excelReader.getSheetDataAsMap(LOGIN_SHEET);

        for (Map<String, String> row : allData) {
            if (testCaseName.equals(row.get("TestCaseName"))) {
                logger.debug("Found credentials for: {}", testCaseName);
                return row;
            }
        }

        logger.warn("No credentials found for test case: {}", testCaseName);
        return Map.of();
    }

    /**
     * Get email for valid login
     * @return Valid email address
     */
    public String getValidEmail() {
        Map<String, String> credentials = getValidAdminCredentials();
        return credentials.getOrDefault("Email", "");
    }

    /**
     * Get password for valid login
     * @return Valid password
     */
    public String getValidPassword() {
        Map<String, String> credentials = getValidAdminCredentials();
        return credentials.getOrDefault("Password", "");
    }

    // ==================== Get Credentials by User Type ====================

    /**
     * Get credentials by user type from UserData sheet
     * @param userType Type of user (Admin, User, Guest)
     * @return Map with user data
     */
    public Map<String, String> getCredentialsByUserType(String userType) {
        logger.info("Fetching credentials for user type: {}", userType);
        List<Map<String, String>> allData = excelReader.getSheetDataAsMap(USER_SHEET);

        for (Map<String, String> row : allData) {
            if (userType.equalsIgnoreCase(row.get("UserType"))) {
                logger.debug("Found credentials for user type: {}", userType);
                return row;
            }
        }

        logger.warn("No credentials found for user type: {}", userType);
        return Map.of();
    }

    /**
     * Get admin user email
     * @return Admin email
     */
    public String getAdminEmail() {
        Map<String, String> userData = getCredentialsByUserType("Admin");
        return userData.getOrDefault("Email", "");
    }

    /**
     * Get admin user password
     * @return Admin password
     */
    public String getAdminPassword() {
        Map<String, String> userData = getCredentialsByUserType("Admin");
        return userData.getOrDefault("Password", "");
    }

    // ==================== Get All Test Data ====================

    /**
     * Get all login test data
     * @return List of all login test cases
     */
    public List<Map<String, String>> getAllLoginTestData() {
        logger.info("Fetching all login test data");
        return excelReader.getSheetDataAsMap(LOGIN_SHEET);
    }

    /**
     * Get all executable login test data (Execute = Yes)
     * @return List of executable test cases
     */
    public List<Map<String, String>> getExecutableLoginTestData() {
        logger.info("Fetching executable login test data");
        return excelReader.getFilteredData(LOGIN_SHEET, "Execute", "Yes");
    }

    /**
     * Get login data as 2D array for TestNG DataProvider
     * @return 2D Object array for DataProvider
     */
    public Object[][] getLoginDataForTestNG() {
        logger.info("Fetching login data for TestNG DataProvider");
        return excelReader.getSheetData(LOGIN_SHEET);
    }

    /**
     * Get executable login data as 2D array
     * @return 2D Object array with only executable tests
     */
    public Object[][] getExecutableLoginDataForTestNG() {
        logger.info("Fetching executable login data for TestNG");
        return excelReader.getExecutableTestData(LOGIN_SHEET);
    }

    // ==================== Get Specific Credentials ====================

    /**
     * Get credentials for invalid password test
     * @return Map with invalid password credentials
     */
    public Map<String, String> getInvalidPasswordCredentials() {
        return getCredentialsByTestCase("TC_Login_002");
    }

    /**
     * Get credentials for invalid email test
     * @return Map with invalid email credentials
     */
    public Map<String, String> getInvalidEmailCredentials() {
        return getCredentialsByTestCase("TC_Login_003");
    }

    /**
     * Get credentials for empty email test
     * @return Map with empty email credentials
     */
    public Map<String, String> getEmptyEmailCredentials() {
        return getCredentialsByTestCase("TC_Login_004");
    }

    /**
     * Get credentials for empty password test
     * @return Map with empty password credentials
     */
    public Map<String, String> getEmptyPasswordCredentials() {
        return getCredentialsByTestCase("TC_Login_005");
    }

    // ==================== Utility Methods ====================

    /**
     * Get row count in LoginData sheet
     * @return Number of test cases
     */
    public int getLoginTestCaseCount() {
        return excelReader.getRowCount(LOGIN_SHEET) - 1; // Exclude header
    }

    /**
     * Close Excel reader
     */
    public void close() {
        excelReader.close();
    }
}
