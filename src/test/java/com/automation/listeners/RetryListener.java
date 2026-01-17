package com.automation.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Retry Listener - DISABLED
 * Retry functionality has been completely disabled to prevent duplicate test execution.
 * Tests will run exactly once, regardless of pass/fail status.
 */
public class RetryListener implements IAnnotationTransformer {

    private static final Logger logger = LogManager.getLogger(RetryListener.class);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        // DISABLED: Do not set retry analyzer - tests should run only once
        // annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }

    /**
     * Retry Analyzer - DISABLED
     * Always returns false to prevent any retry behavior
     */
    public static class RetryAnalyzer implements IRetryAnalyzer {

        @Override
        public boolean retry(ITestResult result) {
            // DISABLED: Never retry - always return false
            return false;
        }
    }
}
