package com.automation.listeners;

import com.automation.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Retry Listener - Implements retry logic for failed tests
 * Automatically retries failed tests based on configuration
 */
public class RetryListener implements IAnnotationTransformer {

    private static final Logger logger = LogManager.getLogger(RetryListener.class);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }

    /**
     * Retry Analyzer - Determines if a test should be retried
     */
    public static class RetryAnalyzer implements IRetryAnalyzer {

        private int retryCount = 0;
        private static final int MAX_RETRY_COUNT;
        private static final boolean RETRY_ENABLED;

        static {
            ConfigReader config = ConfigReader.getInstance();
            MAX_RETRY_COUNT = config.getIntProperty("retry.count", 2);
            RETRY_ENABLED = config.getBooleanProperty("retry.enabled", true);
        }

        @Override
        public boolean retry(ITestResult result) {
            if (!RETRY_ENABLED) {
                return false;
            }

            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                logger.warn("Retrying test '{}' - Attempt {} of {}",
                    result.getName(), retryCount, MAX_RETRY_COUNT);
                return true;
            }

            logger.error("Test '{}' failed after {} retry attempts",
                result.getName(), MAX_RETRY_COUNT);
            return false;
        }
    }
}
