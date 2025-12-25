package com.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber Test Runner with TestNG Integration
 * Executes Cucumber feature files using TestNG framework
 * Supports parallel execution and various reporting options
 */
@CucumberOptions(
    // Feature files location
    features = "src/test/resources/features",

    // Step definitions and hooks packages
    glue = {
        "com.automation.stepdefinitions",
        "com.automation.hooks"
    },

    // Tags to include/exclude
    // Use: @Smoke, @Regression, @Login, @Positive, @Negative, @AIHealing
    // Exclude with ~: ~@Skip, ~@WIP
    tags = "@Smoke or @Regression",

    // Plugins for reporting
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber-html-report.html",
        "json:target/cucumber-reports/cucumber-report.json",
        "junit:target/cucumber-reports/cucumber-junit-report.xml",
        "rerun:target/cucumber-reports/rerun.txt",
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
        "timeline:target/cucumber-reports/timeline"
    },

    // Dry run to check if all steps have definitions
    dryRun = false,

    // Strict mode - fail if there are undefined or pending steps
    // strict = true, // Deprecated in newer versions

    // Show snippets for missing step definitions
    snippets = CucumberOptions.SnippetType.CAMELCASE,

    // Monochrome output for cleaner console logs
    monochrome = true,

    // Publish report to Cucumber Cloud (optional)
    publish = false
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    /**
     * Override scenarios DataProvider for parallel execution
     * Set parallel = true for parallel scenario execution
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
