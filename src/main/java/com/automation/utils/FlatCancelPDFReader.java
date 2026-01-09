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
     * Parse summary page - extract header info and Total Balance Refund
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

        // Extract Total Balance Refund - this is the key value to validate
        summary.totalBalanceRefund = extractCurrencyValue(fullPdfText, "Total Balance Refund\\s*[:\\s]*\\$?([\\d,]+\\.\\d{2})");

        // Alternative patterns for Total Balance Refund
        if (summary.totalBalanceRefund == 0) {
            summary.totalBalanceRefund = extractCurrencyValue(fullPdfText, "Total\\s+Balance\\s+Refund\\s*\\$?([\\d,]+\\.\\d{2})");
        }
        if (summary.totalBalanceRefund == 0) {
            summary.totalBalanceRefund = extractCurrencyValue(fullPdfText, "Balance\\s+Refund\\s*\\$?([\\d,]+\\.\\d{2})");
        }

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
     */
    private FlatCancelLocation parseLocationForCertificate(String certId) {
        FlatCancelLocation location = new FlatCancelLocation();
        location.certId = certId;

        // Find the section containing this certificate
        int certIndex = fullPdfText.indexOf(certId);
        if (certIndex == -1) {
            return null;
        }

        // Get context around certificate (next 500 chars)
        int endIndex = Math.min(certIndex + 1000, fullPdfText.length());
        String certSection = fullPdfText.substring(certIndex, endIndex);

        // Extract Property Address
        location.propertyAddress = extractValue(certSection, "Property Address\\s*[:\\s]+([^\\n]+)");
        if (location.propertyAddress == null) {
            // Try to get address from nearby text (typically after cert ID)
            Pattern addrPattern = Pattern.compile(certId + "\\s+([^$\\n]+?(?:St|Ave|Rd|Dr|Blvd|Ct|Ln|Way|Circle|Cir)[^\\n]*)", Pattern.CASE_INSENSITIVE);
            Matcher addrMatcher = addrPattern.matcher(fullPdfText);
            if (addrMatcher.find()) {
                location.propertyAddress = addrMatcher.group(1).trim();
            }
        }

        // Extract premium values - look for dollar amounts after certificate
        List<Double> dollarAmounts = extractDollarAmounts(certSection);

        if (dollarAmounts.size() >= 5) {
            // Typical order: Property Premium, GL Premium, Water/Sewer, Taxes, Fees, Total
            location.propertyPremium = dollarAmounts.get(0);
            location.glPremium = dollarAmounts.get(1);
            location.waterSewerPremium = dollarAmounts.size() > 2 ? dollarAmounts.get(2) : 0;
            location.taxes = dollarAmounts.size() > 3 ? dollarAmounts.get(3) : 0;
            location.fees = dollarAmounts.size() > 4 ? dollarAmounts.get(4) : 0;
            location.total = dollarAmounts.get(dollarAmounts.size() - 1);
        } else if (!dollarAmounts.isEmpty()) {
            // Just get the last value as total
            location.total = dollarAmounts.get(dollarAmounts.size() - 1);
        }

        location.calculateSum();

        logger.debug("Parsed location: {} - Address: {} - Total: ${}",
            certId, location.propertyAddress, location.total);

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
     * Calculate totals from all locations
     */
    private void calculateTotals() {
        double totalPropertyPremium = 0;
        double totalGLPremium = 0;
        double totalWaterSewer = 0;
        double totalTaxes = 0;
        double totalFees = 0;
        double grandTotal = 0;

        for (FlatCancelLocation loc : locations) {
            totalPropertyPremium += loc.propertyPremium;
            totalGLPremium += loc.glPremium;
            totalWaterSewer += loc.waterSewerPremium;
            totalTaxes += loc.taxes;
            totalFees += loc.fees;
            grandTotal += loc.total;
        }

        summary.calculatedPropertyPremium = totalPropertyPremium;
        summary.calculatedGLPremium = totalGLPremium;
        summary.calculatedWaterSewer = totalWaterSewer;
        summary.calculatedTaxes = totalTaxes;
        summary.calculatedFees = totalFees;
        summary.calculatedGrandTotal = grandTotal;

        logger.info("Calculated totals - Property: ${}, GL: ${}, Grand Total: ${}",
            String.format("%.2f", totalPropertyPremium),
            String.format("%.2f", totalGLPremium),
            String.format("%.2f", grandTotal));
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
     * Validate PDF Total Balance Refund against expected calculation
     * @param expectedTotal Expected total from Edit Flat Cancel screen
     * @return ValidationResult with pass/fail and details
     */
    public FlatCancelValidationResult validateTotalBalanceRefund(double expectedTotal) {
        FlatCancelValidationResult result = new FlatCancelValidationResult();

        double pdfTotal = summary.totalBalanceRefund;
        double difference = Math.abs(pdfTotal - expectedTotal);
        boolean matches = difference < 1.0; // Allow $1 tolerance for rounding

        result.totalBalanceRefundMatch = matches;
        result.expectedTotal = expectedTotal;
        result.pdfTotal = pdfTotal;
        result.difference = difference;

        logger.info("Total Balance Refund validation - Expected: ${}, PDF: ${}, Match: {}",
            String.format("%.2f", expectedTotal),
            String.format("%.2f", pdfTotal),
            matches);

        return result;
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
