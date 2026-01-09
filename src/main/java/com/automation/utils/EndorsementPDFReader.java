package com.automation.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reading and parsing Endorsement PDF files
 */
public class EndorsementPDFReader {
    private static final Logger logger = LoggerFactory.getLogger(EndorsementPDFReader.class);

    private PDDocument document;
    private String pdfFilePath;

    // Parsed data structures
    private EndorsementSummary summary;
    private List<LocationTableEntry> locationTable;
    private List<LocationDetailPage> locationDetailPages;

    public EndorsementPDFReader(String pdfFilePath) throws IOException {
        this.pdfFilePath = pdfFilePath;
        File pdfFile = new File(pdfFilePath);
        if (!pdfFile.exists()) {
            throw new IOException("PDF file not found: " + pdfFilePath);
        }
        this.document = Loader.loadPDF(pdfFile);
        logger.info("Loaded PDF with {} pages: {}", document.getNumberOfPages(), pdfFilePath);
    }

    /**
     * Parse all pages of the endorsement PDF
     */
    public void parseAllPages() throws IOException {
        parseSummaryPage();
        parseLocationTable();
        parseLocationDetailPages();
    }

    /**
     * Parse Page 1 - Summary page with header fields and premium totals
     */
    public EndorsementSummary parseSummaryPage() throws IOException {
        summary = new EndorsementSummary();
        String pageText = getPageText(1);
        logger.debug("Page 1 text:\n{}", pageText);

        // Parse header fields
        summary.policyId = extractValue(pageText, "Policy ID\\s*:\\s*([A-Z0-9]+)");
        summary.effectiveDateOfEndorsement = extractValue(pageText, "Effective Date of Endorsement:\\s*(\\d{2}/\\d{2}/\\d{4})");
        summary.effectiveDate = extractValue(pageText, "Effective Date:\\s*(\\d{2}/\\d{2}/\\d{4})");
        summary.expirationDate = extractValue(pageText, "Expiration Date:\\s*(\\d{2}/\\d{2}/\\d{4})");
        summary.namedInsured = extractValue(pageText, "Named Insured:\\s*([^\\n]+)");
        summary.agentName = extractValue(pageText, "Agent Name:\\s*([^\\n]+)");
        summary.insuringCompany = extractValue(pageText, "Insuring Company:\\s*([^\\n]+)");

        // Parse Coverage Limits
        summary.coverageADwelling = extractCurrencyValue(pageText, "Coverage A – Dwelling:\\s*\\$([\\d,]+\\.?\\d*)");
        summary.totalInsuredValue = extractCurrencyValue(pageText, "Total Insured Value \\(TIV\\):\\s*\\$([\\d,]+\\.?\\d*)");
        summary.totalPropertiesCovered = extractIntValue(pageText, "Total Properties Covered – (\\d+)");

        // Parse Premium Preview values
        summary.propertyPremiumCovA = extractPremiumValue(pageText, "Coverage A – Dwelling", 0);
        summary.propertyPremiumCovB = extractPremiumValue(pageText, "Coverage B – Other Structures", 0);
        summary.propertyPremiumCovC = extractPremiumValue(pageText, "Coverage C – Business Personal Property", 0);
        summary.propertyPremiumCovD = extractPremiumValue(pageText, "Coverage D – Loss of Rents", 0);
        summary.waterSewerBackupPremium = extractPremiumValue(pageText, "Water & Sewer Backup", 0);
        summary.policyFee = extractPremiumValue(pageText, "Policy Fee", 0);
        summary.surplusLinesTax = extractPremiumValue(pageText, "Surplus Lines Tax", 0);
        summary.stampingFee = extractPremiumValue(pageText, "Stamping Fee", 0);
        summary.fireTax = extractPremiumValue(pageText, "Fire Tax", 0);
        summary.municipalTax = extractPremiumValue(pageText, "Municipal Tax", 0);

        // Parse GL Premium
        summary.glPremiumCovA = extractPremiumValue(pageText, "Coverage A – Dwelling", 1);

        // Parse Total Premium Preview
        Pattern totalPattern = Pattern.compile("Total Premium Preview\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})");
        Matcher totalMatcher = totalPattern.matcher(pageText);
        if (totalMatcher.find()) {
            summary.totalPropertyPremium = parseCurrency(totalMatcher.group(1));
            summary.totalGLPremium = parseCurrency(totalMatcher.group(2));
            summary.grandTotal = parseCurrency(totalMatcher.group(3));
        }

        logger.info("Parsed Summary - Policy ID: {}, Effective Date: {}, Grand Total: ${}",
            summary.policyId, summary.effectiveDate, summary.grandTotal);

        return summary;
    }

    /**
     * Parse Page 2 - Location table with all locations
     */
    public List<LocationTableEntry> parseLocationTable() throws IOException {
        locationTable = new ArrayList<>();
        String pageText = getPageText(2);
        logger.debug("Page 2 text:\n{}", pageText);

        // Parse location entries from table - addresses may span multiple lines
        // Pattern: ARCH##### Address (multi-line) $Dwelling $Structures $PersonalProp $Rents $TIV $FullTerm $Change
        Pattern locationPattern = Pattern.compile(
            "(ARCH\\d+)\\s+([\\s\\S]+?)\\s*\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})"
        );

        Matcher matcher = locationPattern.matcher(pageText);
        while (matcher.find()) {
            LocationTableEntry entry = new LocationTableEntry();
            entry.certId = matcher.group(1);
            // Normalize address by replacing newlines with space
            entry.address = matcher.group(2).replaceAll("\\s+", " ").trim();
            entry.dwelling = parseCurrency(matcher.group(3));
            entry.structures = parseCurrency(matcher.group(4));
            entry.personalProp = parseCurrency(matcher.group(5));
            entry.rents = parseCurrency(matcher.group(6));
            entry.tiv = parseCurrency(matcher.group(7));
            entry.fullTerm = parseCurrency(matcher.group(8));
            entry.change = parseCurrency(matcher.group(9));

            locationTable.add(entry);
            logger.info("Parsed location: {} - {} TIV: ${}", entry.certId, entry.address, entry.tiv);
        }

        logger.info("Parsed {} locations from Page 2", locationTable.size());
        return locationTable;
    }

    /**
     * Parse Pages 3+ - Individual location detail pages
     */
    public List<LocationDetailPage> parseLocationDetailPages() throws IOException {
        locationDetailPages = new ArrayList<>();
        int totalPages = document.getNumberOfPages();

        for (int pageNum = 3; pageNum <= totalPages; pageNum++) {
            String pageText = getPageText(pageNum);
            LocationDetailPage detail = parseLocationDetailPage(pageText, pageNum);
            if (detail != null && detail.certId != null) {
                locationDetailPages.add(detail);
                logger.info("Parsed location detail page {}: {} - {}", pageNum, detail.certId, detail.propertyAddress);
            }
        }

        logger.info("Parsed {} location detail pages", locationDetailPages.size());
        return locationDetailPages;
    }

    /**
     * Parse a single location detail page
     */
    private LocationDetailPage parseLocationDetailPage(String pageText, int pageNum) {
        LocationDetailPage detail = new LocationDetailPage();
        detail.pageNumber = pageNum;

        // Parse header fields
        detail.policyId = extractValue(pageText, "Policy ID\\s*:\\s*([A-Z0-9]+)");
        detail.certId = extractValue(pageText, "Cert ID\\s*:\\s*(ARCH\\d+)");
        detail.propertyAddress = extractValue(pageText, "Property Address:\\s*([^\\n]+?)\\s*Effective Date:");
        if (detail.propertyAddress == null) {
            detail.propertyAddress = extractValue(pageText, "Property Address:\\s*([^\\n]+)");
        }
        detail.effectiveDate = extractValue(pageText, "Effective Date:\\s*(\\d{2}/\\d{2}/\\d{4})");
        detail.expirationDate = extractValue(pageText, "Expiration Date:\\s*(\\d{2}/\\d{2}/\\d{4})");

        // Parse Coverage Limits
        detail.coverageADwelling = extractCurrencyValue(pageText, "Coverage A – Dwelling:\\s*\\$([\\d,]+\\.?\\d*)");
        detail.coverageBStructures = extractCurrencyValue(pageText, "Coverage B – Other Structures:\\s*\\$([\\d,]+\\.?\\d*)");
        detail.coverageCBPP = extractCurrencyValue(pageText, "Coverage C – Business Personal Property:\\s*\\$([\\d,]+\\.?\\d*)");
        detail.coverageDRents = extractCurrencyValue(pageText, "Coverage D – Loss of Rents Limit:\\s*\\$([\\d,]+\\.?\\d*)");
        detail.totalInsuredValue = extractCurrencyValue(pageText, "Total Insured Value \\(TIV\\):\\s*\\$([\\d,]+\\.?\\d*)");

        // Parse Premium Preview values
        detail.premiumCovA = extractPremiumValue(pageText, "Coverage A – Dwelling", 0);
        detail.premiumCovB = extractPremiumValue(pageText, "Coverage B – Other Structures", 0);
        detail.premiumCovC = extractPremiumValue(pageText, "Coverage C – Business Personal Property", 0);
        detail.premiumCovD = extractPremiumValue(pageText, "Coverage D – Loss of Rents", 0);
        detail.waterSewerBackup = extractPremiumValue(pageText, "Water & Sewer Backup", 0);
        detail.policyFee = extractPremiumValue(pageText, "Policy Fee", 0);
        detail.surplusLinesTax = extractPremiumValue(pageText, "Surplus Lines Tax", 0);
        detail.stampingFee = extractPremiumValue(pageText, "Stamping Fee", 0);
        detail.fireTax = extractPremiumValue(pageText, "Fire Tax", 0);
        detail.municipalTax = extractPremiumValue(pageText, "Municipal Tax", 0);

        // Parse GL Premium
        detail.glPremiumCovA = extractPremiumValue(pageText, "Coverage A – Dwelling", 1);

        // Parse Total Premium Preview
        Pattern totalPattern = Pattern.compile("Premium Preview\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})\\s+\\$([\\d,]+\\.\\d{2})");
        Matcher totalMatcher = totalPattern.matcher(pageText);
        if (totalMatcher.find()) {
            detail.totalPropertyPremium = parseCurrency(totalMatcher.group(1));
            detail.totalGLPremium = parseCurrency(totalMatcher.group(2));
            detail.totalPremium = parseCurrency(totalMatcher.group(3));
        }

        return detail;
    }

    /**
     * Get text content of a specific page
     */
    public String getPageText(int pageNumber) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNumber);
        stripper.setEndPage(pageNumber);
        return stripper.getText(document);
    }

    /**
     * Extract value using regex pattern
     */
    private String extractValue(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     * Extract currency value using regex pattern
     */
    private double extractCurrencyValue(String text, String pattern) {
        String value = extractValue(text, pattern);
        if (value != null) {
            return parseCurrency(value);
        }
        return 0.0;
    }

    /**
     * Extract integer value using regex pattern
     */
    private int extractIntValue(String text, String pattern) {
        String value = extractValue(text, pattern);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Extract premium value from premium table
     * @param columnIndex 0 for Property Premium, 1 for GL Premium
     */
    private double extractPremiumValue(String text, String rowLabel, int columnIndex) {
        // Pattern to match: Label $value1 $value2
        Pattern pattern = Pattern.compile(rowLabel + "\\s+\\$([\\d,]+\\.\\d{2})(?:\\s+\\$([\\d,]+\\.\\d{2}))?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(columnIndex + 1);
            if (value != null) {
                return parseCurrency(value);
            }
        }
        return 0.0;
    }

    /**
     * Parse currency string to double
     */
    private double parseCurrency(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(",", "").replace("$", ""));
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse currency: {}", value);
            return 0.0;
        }
    }

    /**
     * Close the PDF document
     */
    public void close() throws IOException {
        if (document != null) {
            document.close();
        }
    }

    // Getters
    public EndorsementSummary getSummary() { return summary; }
    public List<LocationTableEntry> getLocationTable() { return locationTable; }
    public List<LocationDetailPage> getLocationDetailPages() { return locationDetailPages; }
    public int getPageCount() { return document != null ? document.getNumberOfPages() : 0; }

    // ==================== Data Classes ====================

    /**
     * Endorsement Summary (Page 1)
     */
    public static class EndorsementSummary {
        public String policyId;
        public String effectiveDateOfEndorsement;
        public String effectiveDate;
        public String expirationDate;
        public String namedInsured;
        public String agentName;
        public String insuringCompany;

        // Coverage Limits
        public double coverageADwelling;
        public double totalInsuredValue;
        public int totalPropertiesCovered;

        // Property Premium Preview
        public double propertyPremiumCovA;
        public double propertyPremiumCovB;
        public double propertyPremiumCovC;
        public double propertyPremiumCovD;
        public double waterSewerBackupPremium;
        public double policyFee;
        public double surplusLinesTax;
        public double stampingFee;
        public double fireTax;
        public double municipalTax;

        // GL Premium
        public double glPremiumCovA;

        // Totals
        public double totalPropertyPremium;
        public double totalGLPremium;
        public double grandTotal;

        public double calculatePropertyPremiumSum() {
            return propertyPremiumCovA + propertyPremiumCovB + propertyPremiumCovC + propertyPremiumCovD;
        }

        public double calculateTaxesSum() {
            return surplusLinesTax + stampingFee + fireTax + municipalTax;
        }
    }

    /**
     * Location Table Entry (Page 2)
     */
    public static class LocationTableEntry {
        public String certId;
        public String address;
        public double dwelling;
        public double structures;
        public double personalProp;
        public double rents;
        public double tiv;
        public double fullTerm;
        public double change;

        public double calculateTIV() {
            return dwelling + structures + personalProp + rents;
        }
    }

    /**
     * Location Detail Page (Pages 3+)
     */
    public static class LocationDetailPage {
        public int pageNumber;
        public String policyId;
        public String certId;
        public String propertyAddress;
        public String effectiveDate;
        public String expirationDate;

        // Coverage Limits
        public double coverageADwelling;
        public double coverageBStructures;
        public double coverageCBPP;
        public double coverageDRents;
        public double totalInsuredValue;

        // Premium Preview
        public double premiumCovA;
        public double premiumCovB;
        public double premiumCovC;
        public double premiumCovD;
        public double waterSewerBackup;
        public double policyFee;
        public double surplusLinesTax;
        public double stampingFee;
        public double fireTax;
        public double municipalTax;

        // GL Premium
        public double glPremiumCovA;

        // Totals
        public double totalPropertyPremium;
        public double totalGLPremium;
        public double totalPremium;

        public double calculatePropertyPremiumSum() {
            return premiumCovA + premiumCovB + premiumCovC + premiumCovD;
        }

        public double calculateTaxesSum() {
            return surplusLinesTax + stampingFee + fireTax + municipalTax;
        }

        public double calculateTIV() {
            return coverageADwelling + coverageBStructures + coverageCBPP + coverageDRents;
        }
    }
}
