package com.automation.pages;

import org.openqa.selenium.WebDriver;

/**
 * Reset Password Page - High-Level Actions
 * Contains business-level operations that call basic methods from ResetPageLocators
 */
public class ResetPage extends ResetPageLocators {

    // Constructor
    public ResetPage(WebDriver driver) {
        super(driver);
        logger.info("ResetPage initialized");
    }

    // ==================== High-Level Actions ====================

    // Click skip button to skip reset password
    public void clickSkipButton() {
        logger.info("Clicking skip button on reset password page");
        waitForSkipButton();
        clickSkip();
    }

    // Handle reset password page - click skip if on reset page
    public boolean handleResetPasswordPage() {
        if (isOnResetPasswordPage()) {
            logger.info("Reset password page detected, clicking skip button");
            clickSkipButton();
            return true;
        }
        logger.info("Not on reset password page");
        return false;
    }

    // Check if reset password page is displayed
    public boolean isResetPasswordPageDisplayed() {
        return isOnResetPasswordPage() && isSkipButtonDisplayed();
    }
}
