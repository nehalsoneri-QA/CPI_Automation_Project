package com.automation.tests;

import java.io.FileOutputStream;
import java.time.Duration;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InvoiceNegativeDueVerification {

    private By policyButtons = By.xpath("//table//tr/td[1]//button");

    WebDriver driver;
    WebDriverWait wait;

    Workbook workbook;
    Sheet sheet;
    int rowCount = 1;

    public static void main(String[] args) throws Exception {
        System.out.println("[MAIN] Execution started");

        InvoiceNegativeDueVerification test = new InvoiceNegativeDueVerification();
        test.setup();
        test.initializeExcel();
        test.login("admin@cpiai.com", "Admin@123");
        test.navigateToInvoicePage();
        test.applyDateFilter("06/01/2025", "11/30/2025");
        test.selectRecordsPerPage("250");
        test.processAllPages();
        test.saveExcel();
        test.tearDown();

        System.out.println("[MAIN] Execution completed");
    }

    public void setup() {
        System.out.println("[SETUP] Launching Chrome browser");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
        driver.get("https://cpiai-dev.attri.ai/login");
        System.out.println("[SETUP] Navigated to login page");
    }

    // ---------- EXCEL INITIALIZATION ----------
    public void initializeExcel() {
        System.out.println("[EXCEL] Initializing Excel workbook");
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Negative Due Invoices");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Policy Number");
        header.createCell(1).setCellValue("Invoice ID");
        header.createCell(2).setCellValue("Due Amount");
        System.out.println("[EXCEL] Header created");
    }

    public void login(String username, String password) throws InterruptedException {
        System.out.println("[LOGIN] Entering credentials");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email"))).sendKeys(username);
        driver.findElement(By.id("login-password")).sendKeys(password);
        driver.findElement(By.xpath("//button[@id='login-submit-btn']")).click();
        Thread.sleep(2000);

        System.out.println("[LOGIN] Skipping reset password popup");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reset-password-skip-link"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[@class='text-4xl font-bold tracking-tight']")));
        System.out.println("[LOGIN] Login successful, dashboard loaded");
    }

    // ---------------- NAVIGATION ----------------
    public void navigateToInvoicePage() throws InterruptedException {
        System.out.println("[NAVIGATION] Navigating to Invoice page");
        Thread.sleep(2000);
        driver.findElement(By.id("desktop-nav-invoices")).click();
        Thread.sleep(5000);
        System.out.println("[NAVIGATION] Invoice page opened");
    }

    // ---------------- DATE FILTER ----------------
    public void applyDateFilter(String fromDate, String toDate) throws InterruptedException {
        System.out.println("[FILTER] Applying date filter from " + fromDate + " to " + toDate);

        WebElement fromDateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id='root']/div[2]/div[1]/div/div[1]/div[1]/div/div/input")));
        fromDateInput.click();
        fromDateInput.sendKeys(Keys.CONTROL, "a");
        fromDateInput.sendKeys(Keys.DELETE);
        Thread.sleep(2000);
        fromDateInput.sendKeys(fromDate);
        fromDateInput.sendKeys(Keys.ENTER);

        WebElement toDateInput = driver.findElement(
                By.xpath("//*[@id='root']/div[2]/div[1]/div/div[1]/div[2]/div/div/input"));
        toDateInput.click();
        toDateInput.sendKeys(Keys.CONTROL, "a");
        toDateInput.sendKeys(Keys.DELETE);
        Thread.sleep(2000);
        toDateInput.sendKeys(toDate);
        toDateInput.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr")));
        Thread.sleep(5000);
        System.out.println("[FILTER] Date filter applied successfully");
    }

    // ---------------- PAGINATION SIZE ----------------
    public void selectRecordsPerPage(String value) throws InterruptedException {
        System.out.println("[PAGINATION] Setting records per page to " + value);

        By pageSizeButton = By.id("invoice-list-page-size-selector");
        wait.until(ExpectedConditions.elementToBeClickable(pageSizeButton)).click();

        By option = By.xpath("//span[text()='" + 250 + "']");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr")));
        Thread.sleep(7000);
        System.out.println("[PAGINATION] Records per page updated");
    }

    // ---------------- PROCESS ALL PAGES ----------------
    public void processAllPages() throws InterruptedException {
        boolean hasNextPage = true;
        int page = 1;

        while (hasNextPage) {
            System.out.println("[PAGINATION] Processing page number: " + page);
            verifyNegativeDueAmountsOnCurrentPage();
            hasNextPage = goToNextPageIfExists();
            page++;
        }
        System.out.println("[PAGINATION] No more pages to process");
    }

    // ---------------- CORE LOGIC (ONE PAGE) ----------------
    public void verifyNegativeDueAmountsOnCurrentPage() throws InterruptedException {

        waitForLoaderToDisappear();

        int policyCount = wait.until(ExpectedConditions
                .numberOfElementsToBeMoreThan(policyButtons, 0))
                .size();

        System.out.println("[POLICY] Policies found on page: " + policyCount);

        for (int i = 0; i < policyCount; i++) {

            System.out.println("[POLICY] Opening policy index: " + i);
            waitForLoaderToDisappear();

            List<WebElement> policies = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(policyButtons));

            WebElement policyButton = policies.get(i);
            String policyNumber = policyButton.getText().trim();
            System.out.println("[POLICY] Policy number: " + policyNumber);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", policyButton);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", policyButton);

            waitForLoaderToDisappear();

            System.out.println("[INVOICE] Fetching invoices for policy: " + policyNumber);

            List<WebElement> invoices = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.xpath("//table//tr/td//table//tbody//tr")));
            if (invoices.isEmpty()) {
                System.out.println("⚠ No invoices for policy: " + policyNumber);
                continue;   // move to next policy safely
            }
            System.out.println("[INVOICE] Invoices found: " + invoices.size());
            

            for (WebElement invoice : invoices) {

                String invoiceId = invoice.findElement(By.xpath("./td[2]")).getText().trim();

                String dueAmountText = invoice.findElement(By.xpath("./td[6]")).getText()
                        .replace("$", "")
                        .replace(",", "")
                        .trim();

                double dueAmount = Double.parseDouble(dueAmountText);

                System.out.println("[INVOICE] InvoiceId=" + invoiceId + ", DueAmount=" + dueAmount);

                if (dueAmount < 0) {
                    System.out.println("[EXCEL] Writing negative due invoice to Excel");
                    writeToExcel(policyNumber, invoiceId, dueAmount);
                }
            }

            waitForLoaderToDisappear();

            policies = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(policyButtons));

            WebElement collapseButton = policies.get(i);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", collapseButton);

            waitForLoaderToDisappear();
            System.out.println("[POLICY] Policy collapsed: " + policyNumber);
        }
    }

    public void waitForLoaderToDisappear() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'bg-opacity-75')]")));
        } catch (Exception e) {
            System.out.println("[LOADER] Loader wait skipped or failed");
        }
    }

    // ---------------- NEXT PAGE ----------------
    public boolean goToNextPageIfExists() throws InterruptedException {
        WebElement nextButton = driver.findElement(
                By.id("invoice-list-pagination-next"));

        if (nextButton.isEnabled()) {
            System.out.println("[PAGINATION] Navigating to next page");
            nextButton.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr")));
            Thread.sleep(5000);
            return true;
        }

        System.out.println("[PAGINATION] Next page button disabled");
        return false;
    }

    // ---------------- EXCEL WRITE ----------------
    public void writeToExcel(String policy, String invoiceId, double dueAmount) {
        Row row = sheet.createRow(rowCount++);
        row.createCell(0).setCellValue(policy);
        row.createCell(1).setCellValue(invoiceId);
        row.createCell(2).setCellValue(dueAmount);
    }

    // ---------------- SAVE FILE ----------------
    public void saveExcel() throws Exception {
        System.out.println("[EXCEL] Saving Excel file");
        FileOutputStream fos = new FileOutputStream("Negative_Due_Invoices.xlsx");
        workbook.write(fos);
        fos.close();
        workbook.close();
        System.out.println("[EXCEL] Excel file created successfully");
    }

    // ---------------- CLEANUP ----------------
    public void tearDown() {
        System.out.println("[CLEANUP] Closing browser");
        driver.quit();
    }
}
