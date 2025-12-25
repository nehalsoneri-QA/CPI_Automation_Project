package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Home Page Locators and Basic Methods
 * Contains all element locators and low-level interaction methods for the Home/Dashboard page
 */
public class HomePageLocators extends BasePage {

    // ==================== Welcome Section Locators ====================

    @FindBy(xpath = "//h1[@class='text-4xl font-bold tracking-tight']")
    protected WebElement welcomeText;

    @FindBy(xpath = "//p[@class='text-[#002A67] font-medium']")
    protected WebElement dateTimeDisplay;

    @FindBy(xpath = "//span[normalize-space()='Admin User']")
    protected WebElement loggedInUserLabel;

    // ==================== Top Navigation Bar Locators (11 Links) ====================

    @FindBy(id = "desktop-nav-quotes")
    protected WebElement quotesLink;

    @FindBy(id = "desktop-nav-policies")
    protected WebElement managePolicyLink;

    @FindBy(id = "desktop-nav-invoices")
    protected WebElement invoicesLink;

    @FindBy(id = "desktop-nav-transactions")
    protected WebElement transactionsLink;

    @FindBy(id = "desktop-nav-refunds")
    protected WebElement refundsLink;

    @FindBy(id = "desktop-nav-agents")
    protected WebElement agentsLink;

    @FindBy(id = "desktop-nav-users")
    protected WebElement usersLink;

    @FindBy(id = "desktop-nav-carrier")
    protected WebElement carrierLink;

    @FindBy(id = "desktop-nav-insured")
    protected WebElement insuredLink;

    @FindBy(id = "desktop-nav-reports")
    protected WebElement reportsLink;

    @FindBy(id = "desktop-nav-analytics")
    protected WebElement analyticsLink;

    // ==================== Dashboard Tiles/Cards Locators (9 Buttons) ====================

    @FindBy(id = "btn-new_quote")
    protected WebElement createQuoteBtn;

    @FindBy(id = "btn-quotes")
    protected WebElement viewQuotesBtn;

    @FindBy(id = "btn-policies")
    protected WebElement managePoliciesBtn;

    @FindBy(id = "btn-users")
    protected WebElement manageUsersBtn;

    @FindBy(id = "btn-agents")
    protected WebElement manageAgentsBtn;

    @FindBy(id = "btn-carrier")
    protected WebElement viewCarriersBtn;

    @FindBy(id = "btn-invoices")
    protected WebElement invoicesTileBtn;

    @FindBy(id = "btn-transactions")
    protected WebElement transactionsTileBtn;

    @FindBy(id = "btn-refunds")
    protected WebElement refundsTileBtn;

    // ==================== Constructor ====================

    public HomePageLocators(WebDriver driver) {
        super(driver);
        logger.info("HomePageLocators initialized");
    }

    // ==================== Welcome Section Methods ====================

    // Get welcome text
    public String getWelcomeText() {
        waitForVisibility(welcomeText);
        return getText(welcomeText);
    }

    // Check if welcome text is displayed
    public boolean isWelcomeTextDisplayed() {
        return isDisplayed(welcomeText);
    }

    // Get date time display text
    public String getDateTimeText() {
        waitForVisibility(dateTimeDisplay);
        return getText(dateTimeDisplay);
    }

    // Check if date time is displayed
    public boolean isDateTimeDisplayed() {
        return isDisplayed(dateTimeDisplay);
    }

    // Get logged in user label text
    public String getLoggedInUserText() {
        waitForVisibility(loggedInUserLabel);
        return getText(loggedInUserLabel);
    }

    // Check if logged in user label is displayed
    public boolean isLoggedInUserDisplayed() {
        return isDisplayed(loggedInUserLabel);
    }

    // ==================== Navigation Bar Methods ====================

    // Click Quotes link
    public void clickQuotes() {
        logger.debug("Clicking Quotes link");
        waitForClickable(quotesLink);
        click(quotesLink);
    }

    // Click Manage Policy link
    public void clickManagePolicy() {
        logger.debug("Clicking Manage Policy link");
        waitForClickable(managePolicyLink);
        click(managePolicyLink);
    }

    // Click Invoices link
    public void clickInvoices() {
        logger.debug("Clicking Invoices link");
        waitForClickable(invoicesLink);
        click(invoicesLink);
    }

    // Click Transactions link
    public void clickTransactions() {
        logger.debug("Clicking Transactions link");
        waitForClickable(transactionsLink);
        click(transactionsLink);
    }

    // Click Refunds link
    public void clickRefunds() {
        logger.debug("Clicking Refunds link");
        waitForClickable(refundsLink);
        click(refundsLink);
    }

    // Click Agents link
    public void clickAgents() {
        logger.debug("Clicking Agents link");
        waitForClickable(agentsLink);
        click(agentsLink);
    }

    // Click Users link
    public void clickUsers() {
        logger.debug("Clicking Users link");
        waitForClickable(usersLink);
        click(usersLink);
    }

    // Click Carrier link
    public void clickCarrier() {
        logger.debug("Clicking Carrier link");
        waitForClickable(carrierLink);
        click(carrierLink);
    }

    // Click Insured link
    public void clickInsured() {
        logger.debug("Clicking Insured link");
        waitForClickable(insuredLink);
        click(insuredLink);
    }

    // Click Reports link
    public void clickReports() {
        logger.debug("Clicking Reports link");
        waitForClickable(reportsLink);
        click(reportsLink);
    }

    // Click Analytics link
    public void clickAnalytics() {
        logger.debug("Clicking Analytics link");
        waitForClickable(analyticsLink);
        click(analyticsLink);
    }

    // ==================== Navigation Link Visibility Methods ====================

    public boolean isQuotesLinkDisplayed() {
        return isDisplayed(quotesLink);
    }

    public boolean isManagePolicyLinkDisplayed() {
        return isDisplayed(managePolicyLink);
    }

    public boolean isInvoicesLinkDisplayed() {
        return isDisplayed(invoicesLink);
    }

    public boolean isTransactionsLinkDisplayed() {
        return isDisplayed(transactionsLink);
    }

    public boolean isRefundsLinkDisplayed() {
        return isDisplayed(refundsLink);
    }

    public boolean isAgentsLinkDisplayed() {
        return isDisplayed(agentsLink);
    }

    public boolean isUsersLinkDisplayed() {
        return isDisplayed(usersLink);
    }

    public boolean isCarrierLinkDisplayed() {
        return isDisplayed(carrierLink);
    }

    public boolean isInsuredLinkDisplayed() {
        return isDisplayed(insuredLink);
    }

    public boolean isReportsLinkDisplayed() {
        return isDisplayed(reportsLink);
    }

    public boolean isAnalyticsLinkDisplayed() {
        return isDisplayed(analyticsLink);
    }

    // ==================== Navigation Link Clickable Methods ====================

    public boolean isQuotesLinkClickable() {
        return isClickable(quotesLink);
    }

    public boolean isManagePolicyLinkClickable() {
        return isClickable(managePolicyLink);
    }

    public boolean isInvoicesLinkClickable() {
        return isClickable(invoicesLink);
    }

    public boolean isTransactionsLinkClickable() {
        return isClickable(transactionsLink);
    }

    public boolean isRefundsLinkClickable() {
        return isClickable(refundsLink);
    }

    public boolean isAgentsLinkClickable() {
        return isClickable(agentsLink);
    }

    public boolean isUsersLinkClickable() {
        return isClickable(usersLink);
    }

    public boolean isCarrierLinkClickable() {
        return isClickable(carrierLink);
    }

    public boolean isInsuredLinkClickable() {
        return isClickable(insuredLink);
    }

    public boolean isReportsLinkClickable() {
        return isClickable(reportsLink);
    }

    public boolean isAnalyticsLinkClickable() {
        return isClickable(analyticsLink);
    }

    // ==================== Dashboard Tile Click Methods ====================

    // Click Create Quote tile
    public void clickCreateQuoteTile() {
        logger.debug("Clicking Create Quote tile");
        waitForClickable(createQuoteBtn);
        click(createQuoteBtn);
    }

    // Click View Quotes tile
    public void clickViewQuotesTile() {
        logger.debug("Clicking View Quotes tile");
        waitForClickable(viewQuotesBtn);
        click(viewQuotesBtn);
    }

    // Click Manage Policies tile
    public void clickManagePoliciesTile() {
        logger.debug("Clicking Manage Policies tile");
        waitForClickable(managePoliciesBtn);
        click(managePoliciesBtn);
    }

    // Click Manage Users tile
    public void clickManageUsersTile() {
        logger.debug("Clicking Manage Users tile");
        waitForClickable(manageUsersBtn);
        click(manageUsersBtn);
    }

    // Click Manage Agents tile
    public void clickManageAgentsTile() {
        logger.debug("Clicking Manage Agents tile");
        waitForClickable(manageAgentsBtn);
        click(manageAgentsBtn);
    }

    // Click View Carriers tile
    public void clickViewCarriersTile() {
        logger.debug("Clicking View Carriers tile");
        waitForClickable(viewCarriersBtn);
        click(viewCarriersBtn);
    }

    // Click Invoices tile
    public void clickInvoicesTile() {
        logger.debug("Clicking Invoices tile");
        waitForClickable(invoicesTileBtn);
        click(invoicesTileBtn);
    }

    // Click Transactions tile
    public void clickTransactionsTile() {
        logger.debug("Clicking Transactions tile");
        waitForClickable(transactionsTileBtn);
        click(transactionsTileBtn);
    }

    // Click Refunds tile
    public void clickRefundsTile() {
        logger.debug("Clicking Refunds tile");
        waitForClickable(refundsTileBtn);
        click(refundsTileBtn);
    }

    // ==================== Dashboard Tile Visibility Methods ====================

    public boolean isCreateQuoteTileDisplayed() {
        return isDisplayed(createQuoteBtn);
    }

    public boolean isViewQuotesTileDisplayed() {
        return isDisplayed(viewQuotesBtn);
    }

    public boolean isManagePoliciesTileDisplayed() {
        return isDisplayed(managePoliciesBtn);
    }

    public boolean isManageUsersTileDisplayed() {
        return isDisplayed(manageUsersBtn);
    }

    public boolean isManageAgentsTileDisplayed() {
        return isDisplayed(manageAgentsBtn);
    }

    public boolean isViewCarriersTileDisplayed() {
        return isDisplayed(viewCarriersBtn);
    }

    public boolean isInvoicesTileDisplayed() {
        return isDisplayed(invoicesTileBtn);
    }

    public boolean isTransactionsTileDisplayed() {
        return isDisplayed(transactionsTileBtn);
    }

    public boolean isRefundsTileDisplayed() {
        return isDisplayed(refundsTileBtn);
    }

    // ==================== Dashboard Tile Enabled Methods ====================

    public boolean isCreateQuoteTileEnabled() {
        return createQuoteBtn.isEnabled();
    }

    public boolean isViewQuotesTileEnabled() {
        return viewQuotesBtn.isEnabled();
    }

    public boolean isManagePoliciesTileEnabled() {
        return managePoliciesBtn.isEnabled();
    }

    public boolean isManageUsersTileEnabled() {
        return manageUsersBtn.isEnabled();
    }

    public boolean isManageAgentsTileEnabled() {
        return manageAgentsBtn.isEnabled();
    }

    public boolean isViewCarriersTileEnabled() {
        return viewCarriersBtn.isEnabled();
    }

    public boolean isInvoicesTileEnabled() {
        return invoicesTileBtn.isEnabled();
    }

    public boolean isTransactionsTileEnabled() {
        return transactionsTileBtn.isEnabled();
    }

    public boolean isRefundsTileEnabled() {
        return refundsTileBtn.isEnabled();
    }

    // Note: Page URL methods (getCurrentUrl, getPageTitle, navigateBack, waitForUrlContains) are inherited from BasePage

    // ==================== Helper Methods ====================

    // Check if element is clickable
    protected boolean isClickable(WebElement element) {
        try {
            waitForClickable(element);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
