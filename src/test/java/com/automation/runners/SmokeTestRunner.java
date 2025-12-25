package com.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Smoke Test Runner - Runs only smoke test scenarios
 * Use this runner for quick sanity checks
 */
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.automation.stepdefinitions", "com.automation.hooks"},
    tags = "@Smoke",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/smoke-report.html",
        "json:target/cucumber-reports/smoke-report.json",
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
    },
    monochrome = true
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
