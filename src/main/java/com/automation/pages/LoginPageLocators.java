package com.automation.pages;

import com.automation.base.BasePage;
import com.automation.utils.LoginDataProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Login Page Locators and Basic Methods
 * Contains all element locators and low-level interaction methods
 * This class should NOT be used directly in tests - use LoginPage.java instead
 */
public class LoginPageLocators extends BasePage {

    // ==================== Excel Data Provider ====================

    protected final LoginDataProvider loginDataProvider;

    // ==================== Web Element Locators (@FindBy) ====================

    @FindBy(name = "email")
    protected WebElement usernameInput;

    @FindBy(name = "password")
    protected WebElement passwordInput;

    @FindBy(xpath = "//button[@type='submit']")
    protected WebElement loginButton;

    @FindBy(xpath = "//div[@class='text-sm font-semibold [&+div]:text-xs']")
    protected WebElement errorMessage;

    @FindBy(xpath = "//h1[@class='text-4xl font-bold tracking-tight']")
    protected WebElement successMessage;

    // ==================== By Locators (for explicit waits and AI healing) ====================

    protected final By errorMessageLocator = By.xpath("//div[@class='text-sm font-semibold [&+div]:text-xs']");
    protected final By successMessageLocator = By.xpath("//h1[@class='text-4xl font-bold tracking-tight']");

    // ==================== Constructor ====================

    public LoginPageLocators(WebDriver driver) {
        super(driver);
        this.loginDataProvider = new LoginDataProvider();
        logger.info("LoginPageLocators initialized");
    }

    // ==================== Excel Data Methods ====================

    // Get admin email from Excel (TestData.xlsx - UserData sheet)
    public String getAdminEmail() {
        return loginDataProvider.getAdminEmail();
    }

    // Get admin password from Excel (TestData.xlsx - UserData sheet)
    public String getAdminPassword() {
        return loginDataProvider.getAdminPassword();
    }

    // ==================== Basic Input Methods ====================

    // Enter text in username field
    public void inputUsername(String username) {
        logger.debug("Entering username: {}", username);
        type(usernameInput, username);
    }

    // Enter text in password field
    public void inputPassword(String password) {
        logger.debug("Entering password");
        type(passwordInput, password);
    }

    // ==================== Basic Click Methods ====================

    // Click login button
    public void clickLogin() {
        logger.debug("Clicking login button");
        click(loginButton);
    }

    // ==================== Basic Get Methods ====================

    // Get username field value
    public String getUsernameValue() {
        return usernameInput.getAttribute("value");
    }

    // Get password field value
    public String getPasswordValue() {
        return passwordInput.getAttribute("value");
    }

    // Get error message text
    public String getErrorMessageText() {
        if (isDisplayed(errorMessageLocator)) {
            return getText(errorMessage);
        }
        return "";
    }

    // Get error message text (alias for getErrorMessageText)
    public String getErrorMessage() {
        return getErrorMessageText();
    }

    // Get success message text
    public String getSuccessMessageText() {
        if (isDisplayed(successMessageLocator)) {
            return getText(successMessage);
        }
        return "";
    }

    // Get current page title - uses inherited getPageTitle() from BasePage
    public String getLoginPageTitle() {
        return getPageTitle();
    }

    // Get current URL - uses inherited getCurrentUrl() from BasePage
    public String getCurrentPageUrl() {
        return getCurrentUrl();
    }

    // ==================== Basic Validation Methods ====================

    // Check if login button is enabled
    public boolean isLoginButtonEnabled() {
        return loginButton.isEnabled();
    }
    
    // Check if error message is displayed
    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessageLocator);
    }

    // Check if success message is displayed
    public boolean isSuccessDisplayed() {
        return isDisplayed(successMessageLocator);
    }

    // ==================== Basic Wait Methods ====================

    // Wait for username field to be visible
    public void waitForUsernameField() {
        waitForVisibility(usernameInput);
    }

    // Wait for password field to be visible
    public void waitForPasswordField() {
        waitForVisibility(passwordInput);
    }

    // Wait for login button to be clickable
    public void waitForLoginButton() {
        waitForClickable(loginButton);
    }

    // Wait for error message to appear
    public void waitForError() {
        waitForVisibility(errorMessageLocator);
    }

    // Wait for error message to appear (alias for waitForError)
    public void waitForErrorMessage() {
        waitForError();
    }

    // Wait for login page to fully load
    public void waitForPageLoad() {
        logger.debug("Waiting for login page to load");
        waitForUsernameField();
        waitForPasswordField();
        waitForLoginButton();
    }

    // Wait for success message to appear
    public void waitForSuccess() {
        waitForVisibility(successMessageLocator);
    }

    // Focus on username field
    public void focusOnUsername() {
        usernameInput.click();
    }

    // Focus on password field
    public void focusOnPassword() {
        passwordInput.click();
    }
}
