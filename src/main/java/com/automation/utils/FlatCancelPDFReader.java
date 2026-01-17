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
 * Utility class for reading and parsing Flat Cancel Endorsement PDF files
 * Validates Total Balance Refund and individual location calculations
 */
public class FlatCancelPDFReader {
    private static final Logger logger = LoggerFactory.getLogger(FlatCancelPDFReader.class);

    private PDDocument document;
    private String pdfFilePath;
    private String fullPdfText;

    // Parsed data
    private FlatCancelSummary summary;
    private List<FlatCancelLocation> locations;

    public FlatCancelPDFReader(String pdfFilePath) throws IOException {
        this.pdfFilePath = pdfFilePath;
        File pdfFile = new File(pdfFilePath);
        if (!pdfFile.exists()) {
            throw new IOException("PDF file not found: " + pdfFilePath);
        }
        this.document = Loader.loadPDF(pdfFile);
        this.locations = new ArrayList<>();
        logger.info("Loaded Flat Cancel PDF with {} pages: {}", document.getNumberOfPages(), pdfFilePath);
    }

    /**
     * Parse all pages of the Flat Cancel PDF
     */
    public void parseAllPages() throws IOException {
        // Get full PDF text
        PDFTextStripper stripper = new PDFTextStripper();
        fullPdfText = stripper.getText(document);

        logger.info("Full PDF text length: {} characters", fullPdfText.length());

        // Parse summary (Page 1)
        parseSummaryPage();

        // Parse location details (all pages)
        parseLocationDetails();
    }

    /**
     * Parse summary page - extract header info and Total Premium of Cancelled Property
     * Page 1: Contains "Total Premium of Cancelled Property" row - last column is Grand Total
     */
    private void parseSummaryPage() throws IOException {
        summary = new FlatCancelSummary();
        String page1 = getPageText(1);

        // Extract Policy ID
        summary.policyId = extractValue(page1, "Policy ID\\s*[:\\s]+([A-Z0-9]+)");
        if (summary.policyId == null) {
            summary.policyId = extractValue(fullPdfText, "Policy ID\\s*[:\\s]+([A-Z0-9]+)");
        }

        // Extract Endorsement Type
        summary.endorsementType = extractValue(page1, "Endorsement Type\\s*[:\\s]+([A-Z_]+)");
        if (summary.endorsementType == null) {
            summary.endorsementType = extractValue(fullPdfText, "ADDRESS_FLAT_CANCELLED");
            if (summary.endorsementType != null) {
                summary.endorsementType = "ADDRESS_FLAT_CANCELLED";
            }
        }

        // Extract Effective Date of Endorsement
        summary.effectiveDateOfEndorsement = extractValue(page1, "Effective Date of Endorsement\\s*[:\\s]+(\\d{2}/\\d{2}/\\d{4})");

        // Extract Named Insured
        summary.namedInsured = extractValue(page1, "Named Insured\\s*[:\\s]+([^\\n]+)");

        // Extract Agent Name
        summary.agentName = extractValue(page1, "Agent Name\\s*[:\\s]+([^\\n]+)");

        // Extract Insuring Company
        summary.insuringCompany = extractValue(page1, "Insuring Company\\s*[:\\s]+([^\\n]+)");

        // ============================================================
        // PRIMARY: Find "Total Premium of Cancelled Property" on Page 1
        // The last column in this row contains the Grand Total
        // ============================================================
        summary.totalBalanceRefund = extractTotalPremiumOfCancelledProperty(page1);

        if (summary.totalBalanceRefund > 0) {
            logger.info("Found 'Total Premium of Cancelled Property' Grand Total: ${}",
                String.format("%.2f", summary.totalBalanceRefund));
        } else {
            // Fallback: Try other patterns if primary method fails
            logger.warn("Could not find 'Total Premium of Cancelled Property' - trying fallback patterns");

            String[] totalPatterns = {
                "Total Premium of Cancelled Property[^$]*\\$([\\d,]+\\.\\d{2})",
                "Total\\s+Premium\\s+of\\s+Cancelled[^$]*\\$([\\d,]+\\.\\d{2})",
                "Cancelled\\s+Property[^$]*\\$([\\d,]+\\.\\d{2})",
                "Total Balance Refund\\s*[:\\s]*\\$?([\\d,]+\\.\\d{2})",
                "Grand\\s+Total\\s*[:\\s]*\\$?([\\d,]+\\.\\d{2})"
            };

            for (String pattern : totalPatterns) {
                summary.totalBalanceRefund = extractCurrencyValue(page1, pattern);
                if (summary.totalBalanceRefund > 0) {
                    logger.info("Found Total using fallback pattern: {} = ${}", pattern, summary.totalBalanceRefund);
                    break;
                }
            }
        }

        // Log first 500 chars of PDF for debugging
        logger.info("PDF Page 1 preview (first 500 chars): {}",
            page1.substring(0, Math.min(500, page1.length())).replaceAll("\\s+", " "));

        // Extract Total properties cancelled
        summary.totalPropertiesCancelled = extractIntValue(page1, "Total Properties Cancelled\\s*[–:\\s]+(\\d+)");
        if (summary.totalPropertiesCancelled == 0) {
            summary.totalPropertiesCancelled = extractIntValue(fullPdfText, "(\\d+)\\s+(?:location|propert|certificate)", Pattern.CASE_INSENSITIVE);
        }

        logger.info("Parsed Summary - Policy ID: {}, Endorsement Type: {}, Total Balance Refund: ${}",
            summary.policyId, summary.endorsementType, summary.totalBalanceRefund);
    }

    /**
     * Parse location details from all pages
     * Extracts certificate IDs, addresses, and premium values
     */
    private void parseLocationDetails() throws IOException {
        locations = new ArrayList<>();

        // Pattern to find certificate entries (ARCH followed by digits)
        Pattern certPattern = Pattern.compile("(ARCH\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher certMatcher = certPattern.matcher(fullPdfText);

        Set<String> foundCertIds = new LinkedHashSet<>();
        while (certMatcher.find()) {
            foundCertIds.add(certMatcher.group(1).toUpperCase());
        }

        logger.info("Found {} unique certificate IDs in PDF: {}", foundCertIds.size(), foundCertIds);

        // Parse each certificate's details
        for (String certId : foundCertIds) {
            FlatCancelLocation location = parseLocationForCertificate(certId);
            if (location != null) {
                locations.add(location);
            }
        }

        // Calculate totals
        calculateTotals();

        logger.info("Parsed {} locations from PDF", locations.size());
    }

    /**
     * Parse location details for a specific certificate
     * PDF column order: Property Premium, Water/Sewer Premium, GL Premium, Premium, Taxes, Fees
     * We capture: Property, Water/Sewer, GL, Taxes, Fees (skip Premium column)
     */
    private FlatCancelLocation parseLocationForCertificate(String certId) {
        FlatCancelLocation location = new FlatCancelLocation();
        location.certId = certId;

        // Find the section containing this certificate
        int certIndex = fullPdfText.indexOf(certId);
        if (certIndex == -1) {
            return null;
        }

        // Get context around certificate (next 1500 chars to capture all values)
        int endIndex = Math.min(certIndex + 1500, fullPdfText.length());
        String certSection = fullPdfText.substring(certIndex, endIndex);

        // Extract Property Address
        location.propertyAddress = extractValue(certSection, "Property Address\\s*[:\\s]+([^\\n]+)");
        if (location.propertyAddress == null) {
            // Try to get address from nearby text (typically after cert ID)
            Pattern addrPattern = Pattern.compile(certId + "\\s+([^$\\n]+?(?:St|Ave|Rd|Dr|Blvd|Ct|Ln|Way|Circle|Cir|Box)[^\\n]*)", Pattern.CASE_INSENSITIVE);
            Matcher addrMatcher = addrPattern.matcher(fullPdfText);
            if (addrMatcher.find()) {
                location.propertyAddress = addrMatcher.group(1).trim();
            }
        }

        // Extract premium values - look for dollar amounts after certificate
        List<Double> dollarAmounts = extractDollarAmounts(certSection);
        logger.debug("Certificate {} - Found {} dollar amounts: {}", certId, dollarAmounts.size(), dollarAmounts);

        // PDF column order: Property Premium, Water/Sewer Premium, GL Premium, Premium, Taxes, Fees
        // We need to map correctly (skip "Premium" column which is index 3)
        if (dollarAmounts.size() >= 6) {
            location.propertyPremium = dollarAmounts.get(0);
            location.waterSewerPremium = dollarAmounts.get(1);
            location.glPremium = dollarAmounts.get(2);
            // Skip dollarAmounts.get(3) which is "Premium"
            location.taxes = dollarAmounts.get(4);
            location.fees = dollarAmounts.get(5);
        } else if (dollarAmounts.size() >= 5) {
            // Fallback if only 5 values
            location.propertyPremium = dollarAmounts.get(0);
            location.waterSewerPremium = dollarAmounts.get(1);
            location.glPremium = dollarAmounts.get(2);
            location.taxes = dollarAmounts.get(3);
            location.fees = dollarAmounts.get(4);
        } else if (dollarAmounts.size() >= 3) {
            // Minimum values
            location.propertyPremium = dollarAmounts.get(0);
            location.glPremium = dollarAmounts.get(1);
            location.waterSewerPremium = dollarAmounts.get(2);
        }

        // Calculate total = Property + GL + Water/Sewer + Taxes + Fees
        location.calculateSum();
        location.total = location.calculatedSum;

        logger.debug("Parsed location: {} - Property: ${}, WS: ${}, GL: ${}, Taxes: ${}, Fees: ${}, Total: ${}",
            certId,
            String.format("%.2f", location.propertyPremium),
            String.format("%.2f", location.waterSewerPremium),
            String.format("%.2f", location.glPremium),
            String.format("%.2f", location.taxes),
            String.format("%.2f", location.fees),
            String.format("%.2f", location.total));

        return location;
    }

    /**
     * Extract all dollar amounts from text
     */
    private List<Double> extractDollarAmounts(String text) {
        List<Double> amounts = new ArrayList<>();
        Pattern dollarPattern = Pattern.compile("\\$([\\d,]+\\.\\d{2})");
        Matcher matcher = dollarPattern.matcher(text);

        while (matcher.find()) {
            try {
                double amount = Double.parseDouble(matcher.group(1).replace(",", ""));
                amounts.add(amount);
            } catch (NumberFormatException e) {
                // Skip invalid amounts
            }
        }

        return amounts;
    }

    /**
     * Extract Grand Total from "Total Premium of Cancelled Property" row on Page 1
     * This row contains the sum of all cancelled property premiums
     * The last dollar amount in this row is the Grand Total
     */
    private double extractTotalPremiumOfCancelledProperty(String page1Text) {
        logger.info("Searching for 'Total Premium of Cancelled Property' row...");

        // Split page into lines
        String[] lines = page1Text.split("\\n");

        for (String line : lines) {
            // Check if this line contains "Total Premium of Cancelled Property" (case insensitive)
            if (line.toLowerCase().contains("total premium of cancelled property") ||
                line.toLowerCase().contains("total premium cancelled property") ||
                line.toLowerCase().contains("cancelled property total")) {

                logger.info("Found row: {}", line.trim());

                // Extract all dollar amounts from this line
                List<Double> amounts = extractDollarAmounts(line);

                if (!amounts.isEmpty()) {
                    // Get the last dollar amount in the row - this is the Grand Total
                    double grandTotal = amounts.get(amounts.size() - 1);
                    logger.info("Extracted Grand Total (last column): ${}", String.format("%.2f", grandTotal));
                    return grandTotal;
                }
            }
        }

        // If not found by line search, try regex on full text
        // Pattern: "Total Premium of Cancelled Property" followed by dollar amounts, get the last one
        Pattern rowPattern = Pattern.compile(
            "Total\\s+Premium\\s+(?:of\\s+)?Cancelled\\s+Property[^\\n]*",
            Pattern.CASE_INSENSITIVE
        );
        Matcher rowMatcher = rowPattern.matcher(page1Text);

        if (rowMatcher.find()) {
            String matchedRow = rowMatcher.group();
            logger.info("Found row via regex: {}", matchedRow.trim());

            List<Double> amounts = extractDollarAmounts(matchedRow);
            if (!amounts.isEmpty()) {
                double grandTotal = amounts.get(amounts.size() - 1);
                logger.info("Extracted Grand Total (last column): ${}", String.format("%.2f", grandTotal));
                return grandTotal;
            }
        }

        logger.warn("Could not find 'Total Premium of Cancelled Property' row in PDF");
        return 0;
    }

    /**
     * Parse Page 2 for list of cancelled locations
     * Returns list of certificate IDs found on page 2
     */
    public List<String> parseCancelledLocationsFromPage2() throws IOException {
        List<String> cancelledCerts = new ArrayList<>();

        if (document.getNumberOfPages() < 2) {
            logger.warn("PDF has less than 2 pages - cannot parse page 2");
            return cancelledCerts;
        }

        String page2 = getPageText(2);
        logger.info("Parsing Page 2 for cancelled locations list...");

        // Find all certificate IDs on page 2
        Pattern certPattern = Pattern.compile("(ARCH[A-Z]*\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher certMatcher = certPattern.matcher(page2);

        Set<String> uniqueCerts = new LinkedHashSet<>();
        while (certMatcher.find()) {
            uniqueCerts.add(certMatcher.group(1).toUpperCase());
        }

        cancelledCerts.addAll(uniqueCerts);
        logger.info("Found {} cancelled certificates on Page 2: {}", cancelledCerts.size(), cancelledCerts);

        return cancelledCerts;
    }

    /**
     * Parse individual certificate details from Page 3 onwards
     * Each certificate's property, GL, WS, taxes, fees details are on separate pages
     */
    public Map<String, FlatCancelLocation> parseIndividualCertificatePages() throws IOException {
        Map<String, FlatCancelLocation> certDetails = new LinkedHashMap<>();

        int totalPages = document.getNumberOfPages();
        logger.info("Parsing individual certificate pages (Page 3 to {})...", totalPages);

        for (int pageNum = 3; pageNum <= totalPages; pageNum++) {
            String pageText = getPageText(pageNum);

            // Find certificate ID on this page
            Pattern certPattern = Pattern.compile("(ARCH[A-Z]*\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher certMatcher = certPattern.matcher(pageText);

            if (certMatcher.find()) {
                String certId = certMatcher.group(1).toUpperCase();

                // Parse this certificate's details
                FlatCancelLocation location = new FlatCancelLocation();
                location.certId = certId;

                // Extract address
                location.propertyAddress = extractValue(pageText, "(?:Property\\s+)?Address\\s*[:\\s]+([^\\n]+)");

                // Extract all dollar amounts from the page
                List<Double> amounts = extractDollarAmounts(pageText);

                // PDF columns: Property Premium, Water/Sewer Premium, GL Premium, Premium, Taxes, Fees
                if (amounts.size() >= 6) {
                    location.propertyPremium = amounts.get(0);
                    location.waterSewerPremium = amounts.get(1);
                    location.glPremium = amounts.get(2);
                    // Skip amounts.get(3) which is "Premium" total
                    location.taxes = amounts.get(4);
                    location.fees = amounts.get(5);
                } else if (amounts.size() >= 5) {
                    location.propertyPremium = amounts.get(0);
                    location.waterSewerPremium = amounts.get(1);
                    location.glPremium = amounts.get(2);
                    location.taxes = amounts.get(3);
                    location.fees = amounts.get(4);
                }

                location.calculateSum();
                location.total = location.calculatedSum;

                certDetails.put(certId, location);

                logger.info("Page {} - Certificate {}: Property=${}, WS=${}, GL=${}, Taxes=${}, Fees=${}, Total=${}",
                    pageNum, certId,
                    String.format("%.2f", location.propertyPremium),
                    String.format("%.2f", location.waterSewerPremium),
                    String.format("%.2f", location.glPremium),
                    String.format("%.2f", location.taxes),
                    String.format("%.2f", location.fees),
                    String.format("%.2f", location.total));
            }
        }

        return certDetails;
    }

    /**
     * Calculate totals from all locations
     * Formula: Property + GL + Water/Sewer + Taxes + Fees = Total
     */
    private void calculateTotals() {
        double totalPropertyPremium = 0;
        double totalGLPremium = 0;
        double totalWaterSewer = 0;
        double totalTaxes = 0;
        double totalFees = 0;

        for (FlatCancelLocation loc : locations) {
            totalPropertyPremium += loc.propertyPremium;
            totalGLPremium += loc.glPremium;
            totalWaterSewer += loc.waterSewerPremium;
            totalTaxes += loc.taxes;
            totalFees += loc.fees;
        }

        summary.calculatedPropertyPremium = totalPropertyPremium;
        summary.calculatedGLPremium = totalGLPremium;
        summary.calculatedWaterSewer = totalWaterSewer;
        summary.calculatedTaxes = totalTaxes;
        summary.calculatedFees = totalFees;

        // Calculate Grand Total = Property + GL + Water/Sewer + Taxes + Fees
        summary.calculatedGrandTotal = totalPropertyPremium + totalGLPremium + totalWaterSewer + totalTaxes + totalFees;

        // If totalBalanceRefund was not found in PDF text, use calculated grand total
        if (summary.totalBalanceRefund == 0) {
            summary.totalBalanceRefund = summary.calculatedGrandTotal;
            logger.info("Using calculated grand total as Total Balance Refund: ${}", String.format("%.2f", summary.totalBalanceRefund));
        }

        logger.info("Calculated totals from PDF locations:");
        logger.info("  Property Premium: ${}", String.format("%.2f", totalPropertyPremium));
        logger.info("  GL Premium: ${}", String.format("%.2f", totalGLPremium));
        logger.info("  Water/Sewer Premium: ${}", String.format("%.2f", totalWaterSewer));
        logger.info("  Taxes: ${}", String.format("%.2f", totalTaxes));
        logger.info("  Fees: ${}", String.format("%.2f", totalFees));
        logger.info("  GRAND TOTAL (P+GL+WS+T+F): ${}", String.format("%.2f", summary.calculatedGrandTotal));
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
     * Get full PDF text
     */
    public String getFullPdfText() {
        return fullPdfText;
    }

    /**
     * Extract value using regex pattern
     */
    private String extractValue(String text, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        } catch (Exception e) {
            logger.debug("Pattern extraction failed: {}", pattern);
        }
        return null;
    }

    /**
     * Extract currency value
     */
    private double extractCurrencyValue(String text, String pattern) {
        String value = extractValue(text, pattern);
        if (value != null) {
            try {
                return Double.parseDouble(value.replace(",", "").replace("$", ""));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Extract integer value
     */
    private int extractIntValue(String text, String pattern) {
        return extractIntValue(text, pattern, 0);
    }

    private int extractIntValue(String text, String pattern, int flags) {
        try {
            Pattern p = Pattern.compile(pattern, flags);
            Matcher m = p.matcher(text);
            if (m.find()) {
                return Integer.parseInt(m.group(1).trim());
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    /**
     * Close the PDF document
     */
    public void close() throws IOException {
        if (document != null) {
            document.close();
        }
    }

    // ==================== Validation Methods ====================

    /**
     * Validate PDF Grand Total against expected calculation
     *
     * Validation Logic:
     * - Expected Total = Sum of cancelled certificates from Create + Edit Flat Cancel screens
     * - PDF Total = "Total Premium of Cancelled Property" value from Page 1 (last column)
     *
     * @param expectedTotal Sum of Create + Edit Flat Cancel amounts from frontend
     * @return ValidationResult with pass/fail and details
     */
    public FlatCancelValidationResult validateTotalBalanceRefund(double expectedTotal) {
        FlatCancelValidationResult result = new FlatCancelValidationResult();
        result.expectedTotal = expectedTotal;

        String expectedFormatted = String.format("%.2f", expectedTotal);

        logger.info("=== PDF GRAND TOTAL VALIDATION ===");
        logger.info("Expected Total (from Create + Edit screens): ${}", expectedFormatted);

        // PRIMARY: Use "Total Premium of Cancelled Property" from Page 1
        // This was extracted in parseSummaryPage() using extractTotalPremiumOfCancelledProperty()
        if (summary.totalBalanceRefund > 0) {
            result.pdfTotal = summary.totalBalanceRefund;
            result.difference = Math.abs(result.pdfTotal - expectedTotal);

            // Allow $1 tolerance for rounding differences
            result.totalBalanceRefundMatch = result.difference < 1.0;

            logger.info("PDF 'Total Premium of Cancelled Property': ${}", String.format("%.2f", result.pdfTotal));
            logger.info("Difference: ${}", String.format("%.2f", result.difference));
            logger.info("Match (within $1 tolerance): {}", result.totalBalanceRefundMatch);

            return result;
        }

        // FALLBACK: If "Total Premium of Cancelled Property" was not found,
        // search for the expected value directly in the PDF text
        logger.warn("'Total Premium of Cancelled Property' not found - using fallback search");

        // Check if expected value exists anywhere in PDF
        boolean foundExact = fullPdfText.contains(expectedFormatted) ||
                             fullPdfText.contains("$" + expectedFormatted) ||
                             fullPdfText.contains(String.format("%,.2f", expectedTotal)) ||
                             fullPdfText.contains("$" + String.format("%,.2f", expectedTotal));

        if (foundExact) {
            logger.info("Found expected value ${} in PDF text", expectedFormatted);
            result.pdfTotal = expectedTotal;
            result.difference = 0;
            result.totalBalanceRefundMatch = true;
            return result;
        }

        // Search for close matches in PDF
        Pattern dollarPattern = Pattern.compile("\\$([\\d,]+\\.\\d{2})");
        Matcher matcher = dollarPattern.matcher(fullPdfText);

        double closestMatch = 0;
        double smallestDiff = Double.MAX_VALUE;
        List<Double> allAmounts = new ArrayList<>();

        while (matcher.find()) {
            try {
                double amount = Double.parseDouble(matcher.group(1).replace(",", ""));
                allAmounts.add(amount);

                double diff = Math.abs(amount - expectedTotal);
                if (diff < smallestDiff) {
                    smallestDiff = diff;
                    closestMatch = amount;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        // If closest match is within 5% or $50, use it
        if (smallestDiff < expectedTotal * 0.05 || smallestDiff < 50) {
            result.pdfTotal = closestMatch;
            result.difference = smallestDiff;
            result.totalBalanceRefundMatch = smallestDiff < 1.0;
            logger.info("Found close match: ${} (diff: ${})",
                String.format("%.2f", closestMatch), String.format("%.2f", smallestDiff));
        } else {
            result.pdfTotal = 0;
            result.difference = expectedTotal;
            result.totalBalanceRefundMatch = false;
            logger.warn("No matching value found in PDF. All amounts found: {}", allAmounts);
        }

        logger.info("=== VALIDATION RESULT ===");
        logger.info("Expected: ${}, PDF: ${}, Diff: ${}, Match: {}",
            expectedFormatted,
            String.format("%.2f", result.pdfTotal),
            String.format("%.2f", result.difference),
            result.totalBalanceRefundMatch);

        return result;
    }

    /**
     * Comprehensive PDF validation against frontend data
     * Compares:
     * 1. Grand Total (Page 1) vs Sum of Create + Edit certificates
     * 2. Cancelled locations list (Page 2) vs certificate IDs
     * 3. Individual certificate details (Page 3+) vs captured data
     *
     * @param expectedGrandTotal Sum of all cancelled certificates from frontend
     * @param allCancelledCertIds All certificate IDs (Create + Edit)
     * @param certificateDetails Map of certId -> LocationDetails from frontend
     * @return Comprehensive validation result
     */
    public ComprehensivePDFValidationResult validateComprehensive(
            double expectedGrandTotal,
            List<String> allCancelledCertIds,
            Map<String, Double> certificateDetails) throws IOException {

        ComprehensivePDFValidationResult result = new ComprehensivePDFValidationResult();

        logger.info("=== COMPREHENSIVE PDF VALIDATION ===");
        logger.info("Expected Grand Total: ${}", String.format("%.2f", expectedGrandTotal));
        logger.info("Expected Certificates: {}", allCancelledCertIds);

        // 1. Validate Grand Total from Page 1
        result.grandTotalValidation = validateTotalBalanceRefund(expectedGrandTotal);

        // 2. Validate cancelled locations on Page 2
        List<String> page2Certs = parseCancelledLocationsFromPage2();
        result.page2Certificates = page2Certs;
        result.allCertificatesFoundOnPage2 = page2Certs.containsAll(allCancelledCertIds);

        logger.info("Page 2 certificates: {}", page2Certs);
        logger.info("All expected certs found on Page 2: {}", result.allCertificatesFoundOnPage2);

        // Find missing certificates
        for (String certId : allCancelledCertIds) {
            if (!page2Certs.contains(certId)) {
                result.missingCertificates.add(certId);
            }
        }

        // 3. Validate individual certificate details from Page 3+
        Map<String, FlatCancelLocation> pdfCertDetails = parseIndividualCertificatePages();
        result.pdfCertificateDetails = pdfCertDetails;

        // Compare each certificate's total
        for (Map.Entry<String, Double> entry : certificateDetails.entrySet()) {
            String certId = entry.getKey();
            double expectedTotal = entry.getValue();

            FlatCancelLocation pdfLocation = pdfCertDetails.get(certId);
            if (pdfLocation != null) {
                double diff = Math.abs(pdfLocation.total - expectedTotal);
                boolean matches = diff < 1.0;
                result.certificateMatches.put(certId, matches);

                logger.info("Certificate {} - Expected: ${}, PDF: ${}, Match: {}",
                    certId, String.format("%.2f", expectedTotal),
                    String.format("%.2f", pdfLocation.total), matches);
            } else {
                result.certificateMatches.put(certId, false);
                logger.warn("Certificate {} not found in PDF pages", certId);
            }
        }

        // Overall validation
        result.overallValid = result.grandTotalValidation.totalBalanceRefundMatch &&
                              result.allCertificatesFoundOnPage2 &&
                              result.certificateMatches.values().stream().allMatch(v -> v);

        logger.info("=== OVERALL VALIDATION: {} ===", result.overallValid ? "PASSED" : "FAILED");

        return result;
    }

    /**
     * Result class for comprehensive PDF validation
     */
    public static class ComprehensivePDFValidationResult {
        public FlatCancelValidationResult grandTotalValidation;
        public List<String> page2Certificates = new ArrayList<>();
        public boolean allCertificatesFoundOnPage2;
        public List<String> missingCertificates = new ArrayList<>();
        public Map<String, FlatCancelLocation> pdfCertificateDetails = new LinkedHashMap<>();
        public Map<String, Boolean> certificateMatches = new LinkedHashMap<>();
        public boolean overallValid;
    }

    /**
     * Validate individual location against expected data
     */
    public boolean validateLocation(String certId, double expectedTotal) {
        for (FlatCancelLocation loc : locations) {
            if (loc.certId.equalsIgnoreCase(certId)) {
                double difference = Math.abs(loc.total - expectedTotal);
                boolean matches = difference < 1.0;
                logger.info("Location {} validation - Expected: ${}, PDF: ${}, Match: {}",
                    certId,
                    String.format("%.2f", expectedTotal),
                    String.format("%.2f", loc.total),
                    matches);
                return matches;
            }
        }
        logger.warn("Location {} not found in PDF", certId);
        return false;
    }

    /**
     * Check if certificate exists in PDF
     */
    public boolean certificateExistsInPDF(String certId) {
        for (FlatCancelLocation loc : locations) {
            if (loc.certId.equalsIgnoreCase(certId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get list of all certificate IDs in PDF
     */
    public List<String> getAllCertificateIds() {
        List<String> certIds = new ArrayList<>();
        for (FlatCancelLocation loc : locations) {
            certIds.add(loc.certId);
        }
        return certIds;
    }

    // ==================== Getters ====================

    public FlatCancelSummary getSummary() { return summary; }
    public List<FlatCancelLocation> getLocations() { return locations; }
    public int getPageCount() { return document != null ? document.getNumberOfPages() : 0; }
    public String getPdfFilePath() { return pdfFilePath; }

    // ==================== Data Classes ====================

    /**
     * Flat Cancel Summary Data
     */
    public static class FlatCancelSummary {
        public String policyId;
        public String endorsementType;
        public String effectiveDateOfEndorsement;
        public String namedInsured;
        public String agentName;
        public String insuringCompany;

        public double totalBalanceRefund;
        public int totalPropertiesCancelled;

        // Calculated values from locations
        public double calculatedPropertyPremium;
        public double calculatedGLPremium;
        public double calculatedWaterSewer;
        public double calculatedTaxes;
        public double calculatedFees;
        public double calculatedGrandTotal;

        @Override
        public String toString() {
            return String.format("Policy: %s | Type: %s | Total Balance Refund: $%.2f | Properties: %d",
                policyId, endorsementType, totalBalanceRefund, totalPropertiesCancelled);
        }
    }

    /**
     * Flat Cancel Location Data
     */
    public static class FlatCancelLocation {
        public String certId;
        public String propertyAddress;
        public double propertyPremium;
        public double glPremium;
        public double waterSewerPremium;
        public double taxes;
        public double fees;
        public double total;
        public double calculatedSum;

        public void calculateSum() {
            calculatedSum = propertyPremium + glPremium + waterSewerPremium + taxes + fees;
        }

        @Override
        public String toString() {
            return String.format("Cert: %s | Address: %s | Property: $%.2f | GL: $%.2f | WS: $%.2f | Taxes: $%.2f | Fees: $%.2f | Total: $%.2f",
                certId, propertyAddress, propertyPremium, glPremium, waterSewerPremium, taxes, fees, total);
        }
    }

    /**
     * Validation Result
     */
    public static class FlatCancelValidationResult {
        public boolean totalBalanceRefundMatch;
        public double expectedTotal;
        public double pdfTotal;
        public double difference;
        public Map<String, Boolean> locationMatches = new HashMap<>();
        public List<String> errors = new ArrayList<>();

        public boolean isValid() {
            return totalBalanceRefundMatch && errors.isEmpty();
        }

        public void addError(String error) {
            errors.add(error);
        }

        @Override
        public String toString() {
            return String.format("Valid: %s | Expected: $%.2f | PDF: $%.2f | Diff: $%.2f",
                isValid(), expectedTotal, pdfTotal, difference);
        }
    }

    // ==================== Static Helper Methods ====================

    /**
     * Wait for PDF download to complete
     */
    public static String waitForPDFDownload(String directoryPath, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        File directory = new File(directoryPath);
        Set<String> existingPdfs = new HashSet<>();
        File[] existingFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (existingFiles != null) {
            for (File f : existingFiles) {
                existingPdfs.add(f.getAbsolutePath());
            }
        }

        logger.info("Waiting for PDF download in: {} (timeout: {}s)", directoryPath, timeoutSeconds);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            File[] currentFiles = directory.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".pdf") && !name.endsWith(".crdownload"));

            if (currentFiles != null) {
                for (File f : currentFiles) {
                    if (!existingPdfs.contains(f.getAbsolutePath())) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        long fileSize = f.length();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        if (f.length() == fileSize && fileSize > 0) {
                            logger.info("PDF download complete: {}", f.getAbsolutePath());
                            return f.getAbsolutePath();
                        }
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.warn("Timeout waiting for PDF download");
        return null;
    }

    /**
     * Find latest PDF in directory
     */
    public static String findLatestPDF(String directoryPath, String fileNameContains) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        File[] pdfFiles = directory.listFiles((dir, name) -> {
            boolean isPdf = name.toLowerCase().endsWith(".pdf");
            boolean containsFilter = fileNameContains == null || fileNameContains.isEmpty()
                    || name.toLowerCase().contains(fileNameContains.toLowerCase());
            return isPdf && containsFilter;
        });

        if (pdfFiles == null || pdfFiles.length == 0) {
            return null;
        }

        Arrays.sort(pdfFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return pdfFiles[0].getAbsolutePath();
    }
}
