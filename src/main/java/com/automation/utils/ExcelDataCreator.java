package com.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Excel Data Creator - Creates test data Excel files
 * Run this class to generate the TestData.xlsx file
 */
public class ExcelDataCreator {

    public static void main(String[] args) {
        createTestDataExcel();
    }

    /**
     * Create TestData.xlsx with Login sheet
     */
    public static void createTestDataExcel() {
        String filePath = "src/test/resources/testdata/TestData.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {

            // Create LoginData sheet
            createLoginDataSheet(workbook);

            // Create UserData sheet (additional test data)
            createUserDataSheet(workbook);

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                System.out.println("Excel file created successfully at: " + filePath);
            }

        } catch (IOException e) {
            System.err.println("Error creating Excel file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create LoginData sheet with credentials
     */
    private static void createLoginDataSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("LoginData");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"TestCaseName", "Description", "Email", "Password", "ExpectedResult", "Execute"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        Object[][] loginData = {
            {"TC_Login_001", "Valid Admin Login", "admin@cpiai.com", "Admin@123", "success", "Yes"},
            {"TC_Login_002", "Invalid Password", "admin@cpiai.com", "WrongPassword", "failure", "Yes"},
            {"TC_Login_003", "Invalid Email", "invalid@cpiai.com", "Admin@123", "failure", "Yes"},
            {"TC_Login_004", "Empty Email", "", "Admin@123", "failure", "Yes"},
            {"TC_Login_005", "Empty Password", "admin@cpiai.com", "", "failure", "Yes"},
            {"TC_Login_006", "Empty Credentials", "", "", "failure", "Yes"},
            {"TC_Login_007", "Invalid Email Format", "admincpiai.com", "Admin@123", "failure", "Yes"},
            {"TC_Login_008", "SQL Injection Test", "admin@cpiai.com' OR '1'='1", "Admin@123", "failure", "Yes"},
            {"TC_Login_009", "XSS Test", "<script>alert('xss')</script>", "Admin@123", "failure", "Yes"},
            {"TC_Login_010", "Special Characters", "admin@cpiai.com", "Admin@123!@#$%", "failure", "No"}
        };

        for (int i = 0; i < loginData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < loginData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(loginData[i][j].toString());
                cell.setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create UserData sheet for additional test scenarios
     */
    private static void createUserDataSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("UserData");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"UserType", "Email", "Password", "Role", "Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        Object[][] userData = {
            {"Admin", "admin@cpiai.com", "Admin@123", "Administrator", "Active"},
            {"User", "user@cpiai.com", "User@123", "Standard User", "Active"},
            {"Guest", "guest@cpiai.com", "Guest@123", "Guest", "Active"}
        };

        for (int i = 0; i < userData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < userData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(userData[i][j].toString());
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
