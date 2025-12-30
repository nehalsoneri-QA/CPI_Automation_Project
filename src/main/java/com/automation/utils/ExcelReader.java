package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel Reader Utility
 * Provides methods to read and write test data from Excel files
 * Supports both .xlsx and .xls formats
 */
public class ExcelReader {

    private static final Logger logger = LogManager.getLogger(ExcelReader.class);
    private final String filePath;
    private Workbook workbook;

    /**
     * Constructor - Initialize with Excel file path
     */
    public ExcelReader(String filePath) {
        this.filePath = filePath;
        loadWorkbook();
    }

    /**
     * Load workbook from file
     */
    private void loadWorkbook() {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = new XSSFWorkbook(fis);
            logger.info("Excel workbook loaded: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to load Excel file: {}", filePath, e);
            throw new RuntimeException("Failed to load Excel file: " + filePath, e);
        }
    }

    /**
     * Get sheet by name
     */
    public Sheet getSheet(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            logger.error("Sheet not found: {}", sheetName);
            throw new RuntimeException("Sheet not found: " + sheetName);
        }
        return sheet;
    }

    /**
     * Get sheet by index
     */
    public Sheet getSheet(int index) {
        return workbook.getSheetAt(index);
    }

    /**
     * Get row count for a sheet
     */
    public int getRowCount(String sheetName) {
        Sheet sheet = getSheet(sheetName);
        return sheet.getLastRowNum() + 1;
    }

    /**
     * Get column count for a row
     */
    public int getColumnCount(String sheetName, int rowNum) {
        Sheet sheet = getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        return row != null ? row.getLastCellNum() : 0;
    }

    /**
     * Get cell value as string
     */
    public String getCellData(String sheetName, int rowNum, int colNum) {
        Sheet sheet = getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(colNum);
        return getCellValueAsString(cell);
    }

    /**
     * Get cell value by column name
     */
    public String getCellData(String sheetName, String columnName, int rowNum) {
        int colNum = getColumnIndex(sheetName, columnName);
        return getCellData(sheetName, rowNum, colNum);
    }

    /**
     * Get column index by column name
     */
    private int getColumnIndex(String sheetName, String columnName) {
        Sheet sheet = getSheet(sheetName);
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new RuntimeException("Header row not found in sheet: " + sheetName);
        }

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && getCellValueAsString(cell).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new RuntimeException("Column not found: " + columnName);
    }

    /**
     * Convert cell value to string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Avoid scientific notation for large numbers
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Get all data from sheet as 2D array (for TestNG DataProvider)
     */
    public Object[][] getSheetData(String sheetName) {
        Sheet sheet = getSheet(sheetName);
        int rowCount = sheet.getLastRowNum();
        int colCount = sheet.getRow(0).getLastCellNum();

        Object[][] data = new Object[rowCount][colCount];

        for (int i = 1; i <= rowCount; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < colCount; j++) {
                if (row != null) {
                    data[i - 1][j] = getCellValueAsString(row.getCell(j));
                } else {
                    data[i - 1][j] = "";
                }
            }
        }
        logger.info("Loaded {} rows of data from sheet: {}", rowCount, sheetName);
        return data;
    }

    /**
     * Get sheet data as List of Maps (column name -> value)
     * Skips empty rows (rows where all cells are empty)
     */
    public List<Map<String, String>> getSheetDataAsMap(String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        Sheet sheet = getSheet(sheetName);

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return dataList;
        }

        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            headers.add(getCellValueAsString(headerRow.getCell(i)));
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && !isRowEmpty(row, headers.size())) {
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    rowData.put(headers.get(j), getCellValueAsString(row.getCell(j)));
                }
                dataList.add(rowData);
            }
        }
        logger.info("Loaded {} rows as map from sheet: {}", dataList.size(), sheetName);
        return dataList;
    }

    /**
     * Check if a row is empty (all cells are empty or blank)
     */
    private boolean isRowEmpty(Row row, int columnCount) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < columnCount; i++) {
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);
            if (value != null && !value.trim().isEmpty()) {
                return false; // Found non-empty cell, row is not empty
            }
        }
        return true; // All cells are empty
    }

    /**
     * Get data filtered by column value
     */
    public List<Map<String, String>> getFilteredData(String sheetName, String columnName, String filterValue) {
        List<Map<String, String>> allData = getSheetDataAsMap(sheetName);
        List<Map<String, String>> filteredData = new ArrayList<>();

        for (Map<String, String> row : allData) {
            if (filterValue.equalsIgnoreCase(row.get(columnName))) {
                filteredData.add(row);
            }
        }
        logger.info("Filtered {} rows where {}={}", filteredData.size(), columnName, filterValue);
        return filteredData;
    }

    /**
     * Get test data for specific test case
     */
    public Map<String, String> getTestData(String sheetName, String testCaseName) {
        List<Map<String, String>> allData = getSheetDataAsMap(sheetName);
        for (Map<String, String> row : allData) {
            if (testCaseName.equalsIgnoreCase(row.get("TestCaseName")) ||
                testCaseName.equalsIgnoreCase(row.get("TestCase"))) {
                return row;
            }
        }
        logger.warn("Test case not found: {}", testCaseName);
        return new HashMap<>();
    }

    /**
     * Write data to cell
     */
    public void setCellData(String sheetName, int rowNum, int colNum, String value) {
        Sheet sheet = getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        Cell cell = row.getCell(colNum);
        if (cell == null) {
            cell = row.createCell(colNum);
        }
        cell.setCellValue(value);
        saveWorkbook();
        logger.info("Set cell [{},{}] = {}", rowNum, colNum, value);
    }

    /**
     * Write data to cell by column name
     */
    public void setCellData(String sheetName, String columnName, int rowNum, String value) {
        int colNum = getColumnIndex(sheetName, columnName);
        setCellData(sheetName, rowNum, colNum, value);
    }

    /**
     * Save workbook to file
     */
    private void saveWorkbook() {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
            logger.debug("Workbook saved: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to save workbook: {}", filePath, e);
        }
    }

    /**
     * Get all sheet names
     */
    public List<String> getSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        return sheetNames;
    }

    /**
     * Check if sheet exists
     */
    public boolean sheetExists(String sheetName) {
        return workbook.getSheet(sheetName) != null;
    }

    /**
     * Close workbook
     */
    public void close() {
        try {
            if (workbook != null) {
                workbook.close();
                logger.info("Workbook closed: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to close workbook", e);
        }
    }

    /**
     * Create a new sheet with headers and data
     */
    public void createSheet(String sheetName, String[] headers, String[][] data) {
        if (sheetExists(sheetName)) {
            logger.info("Sheet already exists: {}", sheetName);
            return;
        }

        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Create data rows
        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }

        saveWorkbook();
        logger.info("Created new sheet: {} with {} rows", sheetName, data.length);
    }

    /**
     * Create CreateQuote sheet with location test data
     * Includes all necessary fields: Physical Address, Coverage details, Payment Plan, Mortgagee info
     */
    public void createCreateQuoteSheet() {
        // Delete existing sheet if present
        if (sheetExists("CreateQuote")) {
            int index = workbook.getSheetIndex("CreateQuote");
            workbook.removeSheetAt(index);
            logger.info("Removed existing CreateQuote sheet");
        }

        String[] headers = {
            "LocationName",      // Identifier for the location
            "Address",           // Physical address (with auto-complete format)
            "SqFt",              // Square footage
            "CoverageA",         // Coverage A amount
            "CoverageB",         // Coverage B amount
            "CoverageC",         // Coverage C amount
            "LossOfRents",       // Loss of Rents amount
            "SuggestedRate",     // Suggested rate
            "PropertyType",      // RS = Residential, RM = Multi-Family (Residential Multi)
            "Units",             // Number of units (required when PropertyType is RM)
            "PaymentPlan",       // Payment Plan: Paid In Full, Installments, Escrow, Bank
            "MortgageeName",     // Mortgagee name (for Escrow)
            "MortgageeAddress"   // Mortgagee address (for Escrow)
        };

        String[][] data = {
            {"Location1", "123 William St, New York, NY 10038, USA", "1200", "50000", "5000", "5000", "5000", "0.5", "RM", "2", "Installments", "", ""},
            {"Location2", "456 Broadway, New York, NY 10012, USA", "1500", "60000", "6000", "6000", "6000", "0.5", "RM", "3", "Bank", "", ""},
            {"Location3", "789 Park Ave, New York, NY 10021, USA", "1800", "70000", "7000", "7000", "7000", "0.5", "RM", "4", "Paid In Full", "", ""},
            {"Location4", "321 5th Ave, New York, NY 10016, USA", "2000", "80000", "8000", "8000", "8000", "0.5", "RM", "1", "Escrow", "First National Bank", "100 Wall Street, New York, NY 10005, USA"}
        };

        createSheet("CreateQuote", headers, data);
        logger.info("Created CreateQuote sheet with {} locations", data.length);
    }

    public String getGLAmount() {
        return getConfigValue("GL", "150");
    }

    public String getWSAmount() {
        return getConfigValue("WS", "100");
    }

    public String getCarrier() {
        return getConfigValue("Carrier", "Starstone");
    }

    public String getLocationMethod() {
        // Try both variations: with and without space
        String value = getConfigValue("LocationMethod", null);
        if (value == null) {
            value = getConfigValue("Location Method", "Add Manually");
        }
        return value != null ? value : "Add Manually";
    }

    public String getUploadPath() {
        // Try both variations: with and without space
        String value = getConfigValue("UploadPath", null);
        if (value == null) {
            value = getConfigValue("Upload Path", null);
        }
        return value != null ? value : "";
    }

    // Common method to get any config value from CreateQuoteConfig sheet
    public String getConfig(String key) {
        return getConfigValue(key, null);
    }

    public String getConfig(String key, String defaultValue) {
        return getConfigValue(key, defaultValue);
    }

    public String getAgent() {
        return getConfigValue("Agent", null);
    }

    public String getInsured() {
        return getConfigValue("Insured", null);
    }

    public String getState() {
        return getConfigValue("State", "Texas");
    }

    public String getAnimalLiability() {
        // Try both variations: with and without space
        String value = getConfigValue("AnimalLiability", null);
        if (value == null) {
            value = getConfigValue("Animal Liability", null);
        }
        return value != null ? value : "No";
    }

    public String getPolicyFee() {
        // Try both variations: with and without space
        String value = getConfigValue("PolicyFee", null);
        if (value == null) {
            value = getConfigValue("Policy Fee", null);
        }
        return value != null ? value : "No";
    }

    // ==================== Edit Quote Config Methods ====================

    /**
     * Get config value from EditQuoteConfig sheet
     */
    public String getEditQuoteConfig(String key) {
        return getEditQuoteConfigValue(key, null);
    }

    public String getEditQuoteConfig(String key, String defaultValue) {
        return getEditQuoteConfigValue(key, defaultValue);
    }

    private String getEditQuoteConfigValue(String key, String defaultValue) {
        // Try EditQuoteConfig sheet first
        String value = getConfigFromSheet("EditQuoteConfig", key);
        if (value != null) {
            logger.info("EditQuote Config '{}' = '{}' (from EditQuoteConfig)", key, value);
            return value;
        }

        // Fallback to EditQuote sheet
        value = getConfigFromSheet("EditQuote", key);
        if (value != null) {
            logger.info("EditQuote Config '{}' = '{}' (from EditQuote)", key, value);
            return value;
        }

        logger.info("EditQuote Config '{}' not found, using default: '{}'", key, defaultValue);
        return defaultValue;
    }

    public String getEditQuoteGL() {
        return getEditQuoteConfigValue("GL", "150");
    }

    public String getEditQuoteWS() {
        return getEditQuoteConfigValue("WS", "100");
    }

    public String getEditQuoteCarrier() {
        return getEditQuoteConfigValue("Carrier", null);
    }

    public String getEditQuoteAgent() {
        return getEditQuoteConfigValue("Agent", null);
    }

    public String getEditQuoteInsured() {
        return getEditQuoteConfigValue("Insured", null);
    }

    public String getEditQuoteState() {
        return getEditQuoteConfigValue("State", null);
    }

    public String getEditQuotePolicyFee() {
        String value = getEditQuoteConfigValue("PolicyFee", null);
        if (value == null) {
            value = getEditQuoteConfigValue("Policy Fee", null);
        }
        return value != null ? value : "No";
    }

    /**
     * Get locations from a specific sheet
     * Works like getAllLocationData() but for any sheet name
     */
    public List<Map<String, String>> getLocationsFromSheet(String sheetName) {
        if (!sheetExists(sheetName)) {
            logger.warn("Sheet '{}' does not exist, returning empty list", sheetName);
            return new ArrayList<>();
        }

        // Get data using flexible header detection
        List<Map<String, String>> allRows = getSheetDataAsMapFlexible(sheetName);

        // Filter and normalize location data
        List<Map<String, String>> validLocations = new ArrayList<>();
        for (Map<String, String> row : allRows) {
            // Normalize the row with standard column names
            Map<String, String> normalizedRow = normalizeLocationRow(row);

            String address = normalizedRow.get("Address");
            if (address != null && !address.trim().isEmpty() &&
                    !address.toLowerCase().contains("required") &&
                    !address.toLowerCase().equals("address")) {
                validLocations.add(normalizedRow);
                logger.debug("Valid location found in {}: Address={}", sheetName, address);
            }
        }

        logger.info("Retrieved {} valid locations from {} sheet", validLocations.size(), sheetName);
        return validLocations;
    }

    /**
     * Get locations from EditQuote sheet
     */
    public List<Map<String, String>> getEditQuoteLocations() {
        return getLocationsFromSheet("EditQuote");
    }

    /**
     * Print all config values from EditQuoteConfig sheet for debugging
     */
    public void printEditQuoteConfig() {
        logger.info("=== All Config Values from EditQuoteConfig ===");
        if (sheetExists("EditQuoteConfig")) {
            try {
                Sheet sheet = getSheet("EditQuoteConfig");
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        String key = getCellValueAsString(row.getCell(0));
                        String value = getCellValueAsString(row.getCell(1));
                        if (key != null && !key.trim().isEmpty()) {
                            logger.info("  {} = {}", key, value);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error reading EditQuoteConfig: {}", e.getMessage());
            }
        } else {
            logger.warn("EditQuoteConfig sheet does not exist");
        }
    }

    /**
     * Print all config values from CreateQuoteConfig sheet for debugging
     */
    public void printAllConfig() {
        logger.info("=== All Config Values from CreateQuoteConfig ===");
        if (sheetExists("CreateQuoteConfig")) {
            try {
                Sheet sheet = getSheet("CreateQuoteConfig");
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        String key = getCellValueAsString(row.getCell(0));
                        String value = getCellValueAsString(row.getCell(1));
                        if (key != null && !key.trim().isEmpty()) {
                            logger.info("  {} = {}", key, value);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error reading CreateQuoteConfig: {}", e.getMessage());
            }
        } else {
            logger.warn("CreateQuoteConfig sheet does not exist");
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        // Try CreateQuoteConfig sheet first
        String value = getConfigFromSheet("CreateQuoteConfig", key);
        if (value != null) {
            logger.info("Config '{}' = '{}' (from CreateQuoteConfig)", key, value);
            return value;
        }

        // Fallback to CreateQuote sheet
        value = getConfigFromSheet("CreateQuote", key);
        if (value != null) {
            logger.info("Config '{}' = '{}' (from CreateQuote)", key, value);
            return value;
        }

        logger.info("Config '{}' not found, using default: '{}'", key, defaultValue);
        return defaultValue;
    }

    private String getConfigFromSheet(String sheetName, String key) {
        if (!sheetExists(sheetName)) {
            logger.debug("Sheet '{}' does not exist", sheetName);
            return null;
        }

        try {
            Sheet sheet = getSheet(sheetName);
            // Normalize key: trim, lowercase, remove extra spaces
            String keyNormalized = key.trim().toLowerCase().replaceAll("\\s+", " ");

            // Key-Value format (Column A = Key, Column B = Value)
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell firstCell = row.getCell(0);
                    String cellValue = getCellValueAsString(firstCell);
                    if (cellValue != null) {
                        // Normalize cell value the same way
                        String cellNormalized = cellValue.trim().toLowerCase().replaceAll("\\s+", " ");

                        // Try exact match first
                        if (cellNormalized.equals(keyNormalized)) {
                            Cell valueCell = row.getCell(1);
                            String value = getCellValueAsString(valueCell);
                            if (value != null && !value.trim().isEmpty()) {
                                return value.trim();
                            }
                        }

                        // Also try without spaces (e.g., "PolicyFee" matches "Policy Fee")
                        String keyNoSpaces = keyNormalized.replace(" ", "");
                        String cellNoSpaces = cellNormalized.replace(" ", "");
                        if (cellNoSpaces.equals(keyNoSpaces)) {
                            Cell valueCell = row.getCell(1);
                            String value = getCellValueAsString(valueCell);
                            if (value != null && !value.trim().isEmpty()) {
                                return value.trim();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Continue
        }
        return null;
    }

    /**
     * Get all location data from CreateQuote sheet
     * Returns only locations with valid data (Address is required)
     * Normalizes column names to standard format
     * Handles complex Excel structure with multiple header rows
     */
    public List<Map<String, String>> getAllLocationData() {
        if (!sheetExists("CreateQuote")) {
            createCreateQuoteSheet();
        }

        // Try standard approach first
        List<Map<String, String>> allRows = getSheetDataAsMapFlexible("CreateQuote");

        // Filter and normalize location data
        List<Map<String, String>> validLocations = new ArrayList<>();
        for (Map<String, String> row : allRows) {
            // Normalize the row with standard column names
            Map<String, String> normalizedRow = normalizeLocationRow(row);

            String address = normalizedRow.get("Address");
            if (address != null && !address.trim().isEmpty() &&
                    !address.toLowerCase().contains("required") &&
                    !address.toLowerCase().equals("address")) {
                validLocations.add(normalizedRow);
                logger.debug("Valid location found: Address={}", address);
            }
        }

        logger.info("Retrieved {} valid locations from CreateQuote sheet", validLocations.size());
        return validLocations;
    }

    /**
     * Get sheet data as List of Maps with flexible header detection
     * Handles sheets where header row may not be row 0
     * Looks for a row with recognizable column names (address, coverage_a, etc.)
     */
    public List<Map<String, String>> getSheetDataAsMapFlexible(String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        Sheet sheet = getSheet(sheetName);

        // Find the header row - look for a row containing column names like "address" or "coverage_a"
        int headerRowIndex = -1;
        List<String> headers = new ArrayList<>();

        for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 5); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                boolean looksLikeHeader = false;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String cellValue = getCellValueAsString(row.getCell(j)).toLowerCase().trim();
                    // Check if this row looks like a header row (has short column names)
                    if (cellValue.equals("address") || cellValue.equals("coverage_a") ||
                            cellValue.equals("payment_plan") || cellValue.equals("units") ||
                            cellValue.equals("city") || cellValue.equals("county")) {
                        looksLikeHeader = true;
                        break;
                    }
                }

                if (looksLikeHeader) {
                    headerRowIndex = i;
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        headers.add(getCellValueAsString(row.getCell(j)));
                    }
                    logger.info("Found header row at index {} with {} columns", headerRowIndex, headers.size());
                    break;
                }
            }
        }

        // If no header found with short names, fall back to row 0
        if (headerRowIndex == -1) {
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                headerRowIndex = 0;
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    headers.add(getCellValueAsString(headerRow.getCell(j)));
                }
            }
        }

        if (headers.isEmpty()) {
            return dataList;
        }

        // Read data rows starting from headerRowIndex + 1
        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && !isRowEmpty(row, headers.size())) {
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    if (header != null && !header.trim().isEmpty()) {
                        rowData.put(header, getCellValueAsString(row.getCell(j)));
                    }
                }
                dataList.add(rowData);
            }
        }

        logger.info("Loaded {} rows as map from sheet: {} (header at row {})", dataList.size(), sheetName, headerRowIndex);
        return dataList;
    }

    /**
     * Normalize location row - map various column names to standard names
     * Handles both long descriptive headers and short underscore names from Excel
     */
    private Map<String, String> normalizeLocationRow(Map<String, String> row) {
        Map<String, String> normalized = new LinkedHashMap<>();

        // Copy all original values
        normalized.putAll(row);

        // Map common column variations to standard names
        // Format: standardName, followed by all possible variations (both long and short)
        mapColumn(row, normalized, "Address", "address", "Insured Property Address", "Property Address", "Address");
        mapColumn(row, normalized, "County", "county", "Insured Property County", "County");
        mapColumn(row, normalized, "CoverageA", "coverage_a", "Dewelling Coverage", "Dwelling Coverage", "Dwelling", "Coverage A", "CoverageA");
        mapColumn(row, normalized, "CoverageB", "coverage_b", "Coverage B amount", "Coverage B", "CoverageB");
        mapColumn(row, normalized, "CoverageC", "coverage_c", "Coverage C Amount", "Coverage C", "CoverageC");
        mapColumn(row, normalized, "LossOfRents", "loss_of_rent", "Loss of Rent Percent", "Loss of Rents", "LossOfRents", "Loss Of Rent");
        mapColumn(row, normalized, "SuggestedRate", "recommended_rate", "Recommended Rate", "Suggested Rate", "Rate", "SuggestedRate");
        mapColumn(row, normalized, "Units", "units", "# of Units", "Units", "Number of Units");
        mapColumn(row, normalized, "PropertyType", "family_dwelling", "Family Dwelling", "Property Type", "PropertyType");
        mapColumn(row, normalized, "PaymentPlan", "payment_plan", "payment_terms", "Payment Plan", "Payment Terms", "PaymentPlan");
        mapColumn(row, normalized, "SqFt", "square_ft", "Total Square FT", "Square Feet", "Sq Ft", "SqFt");
        mapColumn(row, normalized, "City", "city", "Insured Property City", "City");
        mapColumn(row, normalized, "State", "state", "state_abb", "Insured Property State", "State");
        mapColumn(row, normalized, "ZipCode", "postal_code", "Insured Property Zip Code", "Zip Code", "ZipCode", "Zip");
        mapColumn(row, normalized, "YearBuilt", "year_built", "Year Built", "YearBuilt");
        mapColumn(row, normalized, "RoofYear", "roof_replaced_year", "Roof Replacement Year", "Roof Year", "RoofYear");
        mapColumn(row, normalized, "RentalType", "rental_type", "Rental Type", "RentalType");
        mapColumn(row, normalized, "Stories", "stories", "# of Stories", "Stories", "Number of Stories");
        mapColumn(row, normalized, "RCV_ACV", "rcv_acv", "RCV ACV", "RCV_ACV");
        mapColumn(row, normalized, "ExcludeWind", "excluding_wind", "Exclude Wind", "ExcludeWind");
        mapColumn(row, normalized, "Downpayment", "downpay_percentage", "Downpayment", "Down Payment");
        mapColumn(row, normalized, "AOP", "aop", "AOP");
        mapColumn(row, normalized, "WindDeductible", "wind_deductible_amt", "Wind Deductible Amount", "WindDeductible");
        mapColumn(row, normalized, "Municipality", "municipality", "Insured Property Municipality", "Municipality");
        mapColumn(row, normalized, "MortgageeName", "mortgagee1_name", "First Lien Mortgaee Name", "Mortgagee Name", "MortgageeName");
        mapColumn(row, normalized, "MortgageeAddress", "mortgagee1_address1", "First Lien Mortgagee Address", "Mortgagee Address", "MortgageeAddress");
        mapColumn(row, normalized, "MortgageeCity", "mortgagee1_city", "First Lien Mortgagee City", "MortgageeCity");
        mapColumn(row, normalized, "MortgageeState", "mortgagee1_state", "First Lien Mortgagee State", "MortgageeState");
        mapColumn(row, normalized, "MortgageeZip", "mortgagee1_postal_code", "First Lien Mortgagee Zip Code", "MortgageeZip");
        mapColumn(row, normalized, "AutoCalculateCoverageA", "auto_calculate_coverage_a", "Do you want to calculate coverage A automatically", "AutoCalculateCoverageA");
        mapColumn(row, normalized, "SquareFtPrice", "square_ft_price", "SquareFtPrice");

        return normalized;
    }

    /**
     * Map column value from various possible names to standard name
     */
    private void mapColumn(Map<String, String> source, Map<String, String> target, String standardName, String... possibleNames) {
        for (String name : possibleNames) {
            // Try exact match first
            if (source.containsKey(name)) {
                String value = source.get(name);
                if (value != null && !value.trim().isEmpty()) {
                    target.put(standardName, value.trim());
                    return;
                }
            }
            // Try case-insensitive match
            for (Map.Entry<String, String> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getKey().trim().toLowerCase().contains(name.toLowerCase())) {
                    String value = entry.getValue();
                    if (value != null && !value.trim().isEmpty()) {
                        target.put(standardName, value.trim());
                        return;
                    }
                }
            }
        }
    }

    /**
     * Get data as Object[][] for TestNG DataProvider with specific columns
     */
    public Object[][] getDataProviderData(String sheetName, String... columns) {
        List<Map<String, String>> allData = getSheetDataAsMap(sheetName);
        Object[][] data = new Object[allData.size()][columns.length];

        for (int i = 0; i < allData.size(); i++) {
            Map<String, String> row = allData.get(i);
            for (int j = 0; j < columns.length; j++) {
                data[i][j] = row.getOrDefault(columns[j], "");
            }
        }
        return data;
    }

    /**
     * Get data filtered by "Execute" column (for selective test execution)
     */
    public Object[][] getExecutableTestData(String sheetName) {
        List<Map<String, String>> filteredData = getFilteredData(sheetName, "Execute", "Yes");
        if (filteredData.isEmpty()) {
            return new Object[0][0];
        }

        Set<String> keys = filteredData.get(0).keySet();
        Object[][] data = new Object[filteredData.size()][keys.size()];

        for (int i = 0; i < filteredData.size(); i++) {
            Map<String, String> row = filteredData.get(i);
            int j = 0;
            for (String key : keys) {
                data[i][j++] = row.get(key);
            }
        }
        return data;
    }

    /**
     * Main method to print Excel sheet contents for debugging
     */
    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "src/test/resources/testdata/TestData.xlsx";
        System.out.println("Reading Excel file: " + filePath);

        try {
            ExcelReader reader = new ExcelReader(filePath);

            // Print all sheet names
            System.out.println("\n=== SHEET NAMES ===");
            for (int i = 0; i < reader.workbook.getNumberOfSheets(); i++) {
                System.out.println("  " + reader.workbook.getSheetName(i));
            }

            // Print CreateQuoteConfig sheet
            System.out.println("\n=== CreateQuoteConfig SHEET ===");
            if (reader.sheetExists("CreateQuoteConfig")) {
                org.apache.poi.ss.usermodel.Sheet configSheet = reader.getSheet("CreateQuoteConfig");
                for (int i = 0; i <= configSheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = configSheet.getRow(i);
                    if (row != null) {
                        StringBuilder sb = new StringBuilder("Row " + i + ": ");
                        for (int j = 0; j < row.getLastCellNum(); j++) {
                            String val = reader.getCellValueAsString(row.getCell(j));
                            sb.append("[").append(val).append("] ");
                        }
                        System.out.println(sb.toString());
                    }
                }
            } else {
                System.out.println("  Sheet does not exist!");
            }

            // Print CreateQuote sheet headers and first few rows
            System.out.println("\n=== CreateQuote SHEET (RAW) ===");
            if (reader.sheetExists("CreateQuote")) {
                org.apache.poi.ss.usermodel.Sheet quoteSheet = reader.getSheet("CreateQuote");
                for (int i = 0; i <= Math.min(quoteSheet.getLastRowNum(), 5); i++) {
                    org.apache.poi.ss.usermodel.Row row = quoteSheet.getRow(i);
                    if (row != null) {
                        StringBuilder sb = new StringBuilder("Row " + i + ": ");
                        for (int j = 0; j < Math.min(row.getLastCellNum(), 10); j++) {
                            String val = reader.getCellValueAsString(row.getCell(j));
                            sb.append("[").append(val.length() > 20 ? val.substring(0, 20) + "..." : val).append("] ");
                        }
                        System.out.println(sb.toString());
                    }
                }
            }

            // Test config value reading
            System.out.println("\n=== CONFIG VALUES ===");
            System.out.println("  GL: " + reader.getGLAmount());
            System.out.println("  WS: " + reader.getWSAmount());
            System.out.println("  Carrier: " + reader.getCarrier());
            System.out.println("  Agent: " + reader.getAgent());
            System.out.println("  Insured: " + reader.getInsured());
            System.out.println("  State: " + reader.getState());
            System.out.println("  Policy Fee: " + reader.getPolicyFee());
            System.out.println("  Animal Liability: " + reader.getAnimalLiability());
            System.out.println("  Location Method: " + reader.getLocationMethod());

            // Test location data parsing
            System.out.println("\n=== PARSED LOCATIONS ===");
            java.util.List<java.util.Map<String, String>> locations = reader.getAllLocationData();
            System.out.println("Total locations found: " + locations.size());
            for (int i = 0; i < Math.min(locations.size(), 3); i++) {
                java.util.Map<String, String> loc = locations.get(i);
                System.out.println("\nLocation " + (i + 1) + ":");
                System.out.println("  Address: " + loc.getOrDefault("Address", "N/A"));
                System.out.println("  Municipality: " + loc.getOrDefault("Municipality", "N/A"));
                System.out.println("  CoverageA: " + loc.getOrDefault("CoverageA", "N/A"));
                System.out.println("  CoverageB: " + loc.getOrDefault("CoverageB", "N/A"));
                System.out.println("  CoverageC: " + loc.getOrDefault("CoverageC", "N/A"));
                System.out.println("  LossOfRents: " + loc.getOrDefault("LossOfRents", "N/A"));
                System.out.println("  SuggestedRate: " + loc.getOrDefault("SuggestedRate", "N/A"));
                System.out.println("  PropertyType: " + loc.getOrDefault("PropertyType", "N/A"));
                System.out.println("  Units: " + loc.getOrDefault("Units", "N/A"));
                System.out.println("  PaymentPlan: " + loc.getOrDefault("PaymentPlan", "N/A"));
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
