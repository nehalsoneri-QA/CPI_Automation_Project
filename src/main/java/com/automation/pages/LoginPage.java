package com.automation.pages;

import com.github.javafaker.Faker;
import org.openqa.selenium.WebDriver;

/**
 * Login Page - High-Level Login Actions Contains business-level login operations that
 * call basic methods from LoginPageLocators This is the class to be used in tests (Cucumber steps, TestNG tests)
 */
public class LoginPage extends LoginPageLocators {

	private final Faker faker;

	/**
	 * Constructor
	 */
	public LoginPage(WebDriver driver) {
		super(driver);
		this.faker = new Faker();
		logger.info("LoginPage initialized");
	}

	// ==================== High-Level Login Actions ====================

	// Perform login with admin credentials from Excel (TestData.xlsx)
	public void login() {
		String email = getAdminEmail();
		String password = getAdminPassword();
		logger.info("Performing login with admin credentials from Excel - Email: {}", email);
		waitForPageLoad();
		inputUsername(email);
		inputPassword(password);
		clickLogin();
	}

	// Perform complete login with username and password
	public void login(String username, String password) {
		logger.info("Performing login for user: {}", username);
		waitForPageLoad();
		inputUsername(username);
		inputPassword(password);
		clickLogin();
	}

	/**
	 * Perform login with invalid credentials using Faker-generated data This method
	 * generates random invalid credentials for negative testing
	 * 
	 * @return Error message displayed after failed login attempt
	 */
	public String loginWithInvalidCredentials() {
		String invalidEmail = faker.internet().emailAddress();
		String invalidPassword = faker.internet().password(8, 16, true, true, true);

		logger.info("Performing negative login test with Faker-generated credentials");
		logger.info("Invalid Email: {}", invalidEmail);

		waitForPageLoad();
		inputUsername(invalidEmail);
		inputPassword(invalidPassword);
		clickLogin();

		try {
			waitForErrorMessage();
			String errorMsg = getErrorMessage();
			logger.info("Negative login test completed - Error message: {}", errorMsg);
			return errorMsg;
		} catch (Exception e) {
			logger.warn("No error message found after invalid login attempt");
			return "";
		}
	}

	/**
	 * Generate invalid credentials using Faker
	 * 
	 * @return Array containing [invalidEmail, invalidPassword]
	 */
	public String[] generateInvalidCredentials() {
		String invalidEmail = faker.internet().emailAddress();
		String invalidPassword = faker.internet().password(8, 16, true, true, true);
		logger.info("Generated invalid credentials - Email: {}", invalidEmail);
		return new String[] { invalidEmail, invalidPassword };
	}

}
