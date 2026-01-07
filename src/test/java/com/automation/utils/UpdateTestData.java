package com.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Utility to update test data in Excel file
 * Updates UploadPath in CreateQuoteConfig sheet
 */
public class UpdateTestData {

    public static void main(String[] args) {
        String filePath = "src/test/resources/testdata/TestData.xlsx";
        String configSheetName = "CreateQuoteConfig";

        // New upload path for LocationsCounty file
        String newUploadPath = "C:\\Users\\HP\\Downloads\\CPI_Final\\src\\test\\resources\\testdata\\LocationsCounty.xlsx";

        Workbook workbook = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            workbook = new XSSFWorkbook(fis);
            fis.close(); // Close input stream before writing

            System.out.println("=== Updating UploadPath in " + configSheetName + " ===");
            System.out.println("New path: " + newUploadPath);

            // List all sheets
            System.out.println("\nAvailable sheets:");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                System.out.println("  " + i + ": " + workbook.getSheetName(i));
            }

            Sheet sheet = workbook.getSheet(configSheetName);
            if (sheet == null) {
                System.out.println("\nSheet not found: " + configSheetName);
                System.out.println("Trying to find config sheet...");

                // Try to find a sheet with "Config" in the name
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    String sheetName = workbook.getSheetName(i);
                    if (sheetName.toLowerCase().contains("config")) {
                        sheet = workbook.getSheetAt(i);
                        configSheetName = sheetName;
                        System.out.println("Found config sheet: " + sheetName);
                        break;
                    }
                }
            }

            if (sheet == null) {
                System.out.println("ERROR: Could not find config sheet!");
                return;
            }

            // Search for UploadPath in column A and update column B
            boolean found = false;
            System.out.println("\n=== Searching for UploadPath in sheet: " + configSheetName + " ===");

            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Cell keyCell = row.getCell(0); // Column A - key name
                if (keyCell == null) continue;

                String keyValue = getCellValueAsString(keyCell).trim();

                // Check if this is the UploadPath row
                if (keyValue.equalsIgnoreCase("UploadPath") ||
                    keyValue.equalsIgnoreCase("Upload Path") ||
                    keyValue.toLowerCase().contains("uploadpath")) {

                    System.out.println("Found '" + keyValue + "' at row " + (r + 1));

                    // Get or create value cell in column B
                    Cell valueCell = row.getCell(1);
                    if (valueCell == null) {
                        valueCell = row.createCell(1);
                    }

                    String oldValue = getCellValueAsString(valueCell);
                    System.out.println("Old value: " + oldValue);

                    // Update the value
                    valueCell.setCellValue(newUploadPath);
                    System.out.println("New value: " + newUploadPath);

                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("\nUploadPath not found in config. Listing all config values:");
                for (int r = 0; r <= Math.min(20, sheet.getLastRowNum()); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    Cell keyCell = row.getCell(0);
                    Cell valueCell = row.getCell(1);

                    String key = keyCell != null ? getCellValueAsString(keyCell) : "";
                    String value = valueCell != null ? getCellValueAsString(valueCell) : "";

                    if (!key.isEmpty()) {
                        System.out.println("  Row " + (r + 1) + ": " + key + " = " + value);
                    }
                }
            }

            // Save the workbook
            if (found) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                    System.out.println("\n=== Excel file updated successfully! ===");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}
