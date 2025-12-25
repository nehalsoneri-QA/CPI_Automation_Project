package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Data Provider Utility - Centralized data providers for TestNG tests
 * Supports Excel, JSON, and programmatic test data
 */
public class DataProviderUtil {

    private static final Logger logger = LogManager.getLogger(DataProviderUtil.class);
    private static final String DEFAULT_EXCEL_PATH = "src/test/resources/testdata/TestData.xlsx";

    /**
     * Generic Excel data provider - uses method name to find sheet
     */
    @DataProvider(name = "excelData")
    public static Object[][] getExcelData(Method method) {
        String methodName = method.getName();
        String sheetName = getSheetNameFromMethod(methodName);

        logger.info("Loading test data from Excel sheet: {}", sheetName);

        try {
            ExcelReader reader = new ExcelReader(DEFAULT_EXCEL_PATH);
            return reader.getSheetData(sheetName);
        } catch (Exception e) {
            logger.error("Failed to load Excel data for: {}", sheetName, e);
            return new Object[0][0];
        }
    }

    /**
     * Login test data provider
     */
    @DataProvider(name = "loginData")
    public static Object[][] getLoginData() {
        return getExcelDataFromSheet("LoginData");
    }

    /**
     * Login data with execution filter
     */
    @DataProvider(name = "loginDataFiltered")
    public static Object[][] getLoginDataFiltered() {
        try {
            ExcelReader reader = new ExcelReader(DEFAULT_EXCEL_PATH);
            return reader.getExecutableTestData("LoginData");
        } catch (Exception e) {
            logger.error("Failed to load filtered login data", e);
            return new Object[0][0];
        }
    }

    /**
     * User credentials data provider
     */
    @DataProvider(name = "userCredentials", parallel = true)
    public static Object[][] getUserCredentials() {
        return new Object[][]{
            {"admin@example.com", "Admin@123", "Admin"},
            {"user@example.com", "User@123", "Standard User"},
            {"test@example.com", "Test@123", "Test User"}
        };
    }

    /**
     * Invalid credentials data provider
     */
    @DataProvider(name = "invalidCredentials")
    public static Object[][] getInvalidCredentials() {
        return new Object[][]{
            {"", "password", "Username is required"},
            {"user@example.com", "", "Password is required"},
            {"", "", "Username is required"},
            {"invalid@email", "password", "Invalid email format"},
            {"user@example.com", "short", "Password too short"},
            {"<script>alert('xss')</script>", "password", "Invalid username"}
        };
    }

    /**
     * Browser data provider for cross-browser testing
     */
    @DataProvider(name = "browsers", parallel = true)
    public static Object[][] getBrowsers() {
        return new Object[][]{
            {"chrome"},
            {"firefox"},
            {"edge"}
        };
    }

    /**
     * Environment data provider
     */
    @DataProvider(name = "environments")
    public static Object[][] getEnvironments() {
        return new Object[][]{
            {"qa", "https://qa.example.com"},
            {"staging", "https://staging.example.com"},
            {"prod", "https://www.example.com"}
        };
    }

    /**
     * Get data from specific Excel sheet
     */
    private static Object[][] getExcelDataFromSheet(String sheetName) {
        try {
            ExcelReader reader = new ExcelReader(DEFAULT_EXCEL_PATH);
            return reader.getSheetData(sheetName);
        } catch (Exception e) {
            logger.error("Failed to load data from sheet: {}", sheetName, e);
            return new Object[0][0];
        }
    }

    /**
     * Map method name to sheet name
     */
    private static String getSheetNameFromMethod(String methodName) {
        if (methodName.toLowerCase().contains("login")) {
            return "LoginData";
        } else if (methodName.toLowerCase().contains("register")) {
            return "RegistrationData";
        } else if (methodName.toLowerCase().contains("search")) {
            return "SearchData";
        }
        return "TestData";
    }

    /**
     * Get Excel data as List of Maps
     */
    public static List<Map<String, String>> getExcelDataAsMaps(String sheetName) {
        try {
            ExcelReader reader = new ExcelReader(DEFAULT_EXCEL_PATH);
            return reader.getSheetDataAsMap(sheetName);
        } catch (Exception e) {
            logger.error("Failed to load Excel data as maps", e);
            return List.of();
        }
    }
}
