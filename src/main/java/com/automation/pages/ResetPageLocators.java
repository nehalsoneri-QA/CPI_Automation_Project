package com.automation.pages;

import com.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Reset Password Page Locators and Basic Methods
 * Contains all element locators and low-level interaction methods
 */
public class ResetPageLocators extends BasePage {

    // ==================== Web Element Locators (@FindBy) ====================

    @FindBy(xpath = "//a[@class='text-sm text-blue-600 hover:text-blue-700']")
    protected WebElement skipButton;

    // ==================== By Locators ====================

    protected final By skipButtonLocator = By.xpath("//a[@class='text-sm text-blue-600 hover:text-blue-700']");

    // ==================== Constructor ====================

    public ResetPageLocators(WebDriver driver) {
        super(driver);
        logger.info("ResetPageLocators initialized");
    }

    // ==================== Basic Click Methods ====================

    // Click skip button
    public void clickSkip() {
        logger.debug("Clicking skip button");
        click(skipButton);
    }

    // ==================== Basic Validation Methods ====================

    // Check if skip button is displayed
    public boolean isSkipButtonDisplayed() {
        return isDisplayed(skipButtonLocator);
    }

    // Check if on reset password page
    public boolean isOnResetPasswordPage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/reset_password");
    }

    // ==================== Basic Wait Methods ====================

    // Wait for skip button to be visible
    public void waitForSkipButton() {
        waitForVisibility(skipButtonLocator);
    }
}
