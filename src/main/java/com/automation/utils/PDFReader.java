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
 * Utility class for reading and parsing PDF files
 */
public class PDFReader {
    private static final Logger logger = LoggerFactory.getLogger(PDFReader.class);

    /**
     * Read entire PDF content as text
     * @param filePath Path to the PDF file
     * @return PDF content as string
     */
    public static String readPDF(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.error("PDF file not found: {}", filePath);
                return "";
            }

            PDDocument document = Loader.loadPDF(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            logger.info("Successfully read PDF: {} ({} characters)", filePath, text.length());
            return text;
        } catch (IOException e) {
            logger.error("Error reading PDF: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Extract MASTER POLICY STATEMENT section from PDF
     * @param pdfContent Full PDF content
     * @return Content of MASTER POLICY STATEMENT section
     */
    public static String extractMasterPolicySection(String pdfContent) {
        // Find the MASTER POLICY STATEMENT section
        int startIndex = pdfContent.indexOf("MASTER POLICY STATEMENT");
        if (startIndex == -1) {
            logger.warn("MASTER POLICY STATEMENT section not found in PDF");
            return "";
        }

        // Extract from MASTER POLICY STATEMENT to end (or next major section)
        String section = pdfContent.substring(startIndex);
        logger.info("Extracted MASTER POLICY STATEMENT section ({} characters)", section.length());
        return section;
    }

    /**
     * Data class to hold certificate data from PDF
     */
    public static class PDFCertificateData {
        public String certNumber;
        public String propertyAddress;
        public String total;
        public double totalValue;

        @Override
        public String toString() {
            return String.format("CERT#: %s | Address: %s | Total: %s", certNumber, propertyAddress, total);
        }
    }

    /**
     * Parse certificate data from MASTER POLICY STATEMENT section
     * Extracts CERT#, PROPERTY ADDRESS, and TOTAL columns
     * Only keeps the FIRST occurrence of each certificate (from the main policy section)
     * @param masterPolicySection The MASTER POLICY STATEMENT section text
     * @return Map of certificate number to PDFCertificateData
     */
    public static Map<String, PDFCertificateData> parseCertificateData(String masterPolicySection) {
        Map<String, PDFCertificateData> certificateMap = new HashMap<>();

        // Split into lines
        String[] lines = masterPolicySection.split("\n");

        // Pattern to match certificate lines (starts with BP, E, F or I followed by space and ARCH/STAR...)
        // BP = BANK PAYMENT, E = ESCROW, F = FULL PAY, I = INSTALLMENTS
        // Carriers: ARCH = Arch Insurance, STAR = StarStone Insurance
        // Format: F ARCH73261 604 N Neal St, Commerce, TX 75428, USA $572.01 $15.00 $0.00 $28.70 $0.00 $615.71
        Pattern certPattern = Pattern.compile("^(BP|[EFI])\\s+((ARCH|STAR)\\d+)\\s+(.+?)\\s+(\\$[\\d,]+\\.\\d{2})\\s*$");

        // Alternative pattern for multi-line addresses
        Pattern certStartPattern = Pattern.compile("^(BP|[EFI])\\s+((ARCH|STAR)\\d+)\\s+(.+)");
        Pattern totalPattern = Pattern.compile("\\$(\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s*$");

        StringBuilder currentAddress = new StringBuilder();
        String currentCert = null;
        List<String> currentLineData = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Skip empty lines and header lines
            if (line.isEmpty() || line.contains("CERT#") || line.contains("PROPERTY ADDRESS")) {
                continue;
            }

            // Check if line starts with BP, E, F or I followed by ARCH (certificate line)
            Matcher startMatcher = certStartPattern.matcher(line);
            if (startMatcher.find()) {
                // Save previous certificate if exists
                if (currentCert != null && !currentLineData.isEmpty()) {
                    PDFCertificateData data = extractCertificateFromLines(currentCert, currentLineData);
                    // Only add if certificate doesn't already exist (keep first occurrence only)
                    if (data != null && !certificateMap.containsKey(data.certNumber)) {
                        certificateMap.put(data.certNumber, data);
                    }
                }

                // Start new certificate - group(2) is the ARCH number since group(1) is payment type
                currentCert = startMatcher.group(2);
                currentLineData = new ArrayList<>();
                currentLineData.add(line);
            } else if (currentCert != null) {
                // Continue adding lines for current certificate (multi-line address)
                currentLineData.add(line);

                // Check if this line contains the TOTAL (ends with dollar amount pattern)
                if (line.matches(".*\\$[\\d,]+\\.\\d{2}\\s*$")) {
                    // This might be the last line with totals
                    PDFCertificateData data = extractCertificateFromLines(currentCert, currentLineData);
                    // Only add if certificate doesn't already exist (keep first occurrence only)
                    if (data != null && !certificateMap.containsKey(data.certNumber)) {
                        certificateMap.put(data.certNumber, data);
                    }
                    currentCert = null;
                    currentLineData = new ArrayList<>();
                }
            }
        }

        // Handle last certificate
        if (currentCert != null && !currentLineData.isEmpty()) {
            PDFCertificateData data = extractCertificateFromLines(currentCert, currentLineData);
            // Only add if certificate doesn't already exist (keep first occurrence only)
            if (data != null && !certificateMap.containsKey(data.certNumber)) {
                certificateMap.put(data.certNumber, data);
            }
        }

        logger.info("Parsed {} certificates from PDF", certificateMap.size());
        return certificateMap;
    }

    /**
     * Extract certificate data from collected lines
     */
    private static PDFCertificateData extractCertificateFromLines(String certNumber, List<String> lines) {
        if (lines.isEmpty()) return null;

        PDFCertificateData data = new PDFCertificateData();
        data.certNumber = certNumber;

        // Combine all lines
        String combinedLine = String.join(" ", lines);

        // Extract dollar amounts from the line (PREMIUM, SEWER, GL, TAXES, FEES, TOTAL)
        Pattern dollarPattern = Pattern.compile("\\$([\\d,]+\\.\\d{2})");
        Matcher dollarMatcher = dollarPattern.matcher(combinedLine);

        List<String> amounts = new ArrayList<>();
        while (dollarMatcher.find()) {
            amounts.add("$" + dollarMatcher.group(1));
        }

        // The last amount should be TOTAL
        if (!amounts.isEmpty()) {
            data.total = amounts.get(amounts.size() - 1);
            try {
                data.totalValue = Double.parseDouble(data.total.replace("$", "").replace(",", ""));
            } catch (NumberFormatException e) {
                data.totalValue = 0;
            }
        }

        // Extract address (text between cert number and first dollar amount)
        int certIndex = combinedLine.indexOf(certNumber);
        int firstDollarIndex = combinedLine.indexOf("$");
        if (certIndex != -1 && firstDollarIndex != -1 && firstDollarIndex > certIndex) {
            String addressPart = combinedLine.substring(certIndex + certNumber.length(), firstDollarIndex).trim();
            // Clean up the address
            data.propertyAddress = addressPart.replaceAll("\\s+", " ").trim();
        }

        logger.debug("Extracted certificate: {}", data);
        return data;
    }

    /**
     * Find the most recent PDF file in a directory
     * @param directoryPath Directory to search
     * @param fileNameContains Optional filter for file name
     * @return Path to the most recent PDF file, or null if not found
     */
    public static String findLatestPDF(String directoryPath, String fileNameContains) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("Directory not found: {}", directoryPath);
            return null;
        }

        File[] pdfFiles = directory.listFiles((dir, name) -> {
            boolean isPdf = name.toLowerCase().endsWith(".pdf");
            boolean containsFilter = fileNameContains == null || fileNameContains.isEmpty()
                    || name.contains(fileNameContains);
            return isPdf && containsFilter;
        });

        if (pdfFiles == null || pdfFiles.length == 0) {
            logger.warn("No PDF files found in: {}", directoryPath);
            return null;
        }

        // Sort by last modified (newest first)
        Arrays.sort(pdfFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        String latestPdf = pdfFiles[0].getAbsolutePath();
        logger.info("Found latest PDF: {}", latestPdf);
        return latestPdf;
    }

    /**
     * Main method for debugging PDF parsing
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: PDFReader <pdf-file-path>");
            return;
        }

        String pdfPath = args[0];
        System.out.println("Reading PDF: " + pdfPath);

        String content = readPDF(pdfPath);
        System.out.println("\n=== PDF Content (" + content.length() + " chars) ===\n");

        // Print first 3000 chars to see format
        System.out.println(content.substring(0, Math.min(3000, content.length())));

        System.out.println("\n=== Extracting MASTER POLICY STATEMENT ===\n");
        String masterSection = extractMasterPolicySection(content);
        System.out.println("Section length: " + masterSection.length());

        if (!masterSection.isEmpty()) {
            System.out.println("\n=== First 2000 chars of MASTER POLICY STATEMENT ===\n");
            System.out.println(masterSection.substring(0, Math.min(2000, masterSection.length())));
        }

        System.out.println("\n=== Parsing Certificates ===\n");
        Map<String, PDFCertificateData> certs = parseCertificateData(masterSection);
        System.out.println("Found " + certs.size() + " certificates");

        for (PDFCertificateData cert : certs.values()) {
            System.out.println("  " + cert);
        }
    }

    /**
     * Wait for PDF download to complete
     * @param directoryPath Directory where PDF will be downloaded
     * @param timeoutSeconds Maximum time to wait
     * @return Path to downloaded PDF, or null if timeout
     */
    public static String waitForPDFDownload(String directoryPath, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        // Get list of existing PDFs before download
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
                        // New PDF found - wait a bit more to ensure download is complete
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        // Verify file is not still being written
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
}
