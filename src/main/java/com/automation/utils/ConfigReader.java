package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration Reader - Singleton pattern
 * Reads properties from config files and system properties
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static ConfigReader instance;
    private final Properties properties;
    private static final String DEFAULT_CONFIG_PATH = "src/test/resources/config/config.properties";

    /**
     * Private constructor for Singleton pattern
     */
    private ConfigReader() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Get singleton instance
     */
    public static synchronized ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    /**
     * Load properties from config file
     */
    private void loadProperties() {
        String configPath = System.getProperty("config.path", DEFAULT_CONFIG_PATH);
        logger.info("Loading configuration from: {}", configPath);

        try (InputStream input = new FileInputStream(configPath)) {
            properties.load(input);
            logger.info("Configuration loaded successfully. Properties count: {}", properties.size());
        } catch (IOException e) {
            logger.error("Failed to load configuration file: {}", configPath, e);
            loadDefaultProperties();
        }

        // Override with system properties
        properties.stringPropertyNames().forEach(key -> {
            String systemValue = System.getProperty(key);
            if (systemValue != null) {
                logger.info("Overriding property '{}' with system property value", key);
                properties.setProperty(key, systemValue);
            }
        });

        // Load environment-specific properties if environment is set
        String environment = getProperty("environment", "qa");
        loadEnvironmentProperties(environment);
    }

    /**
     * Load environment-specific properties
     */
    private void loadEnvironmentProperties(String environment) {
        String envConfigPath = "src/test/resources/config/" + environment + ".properties";
        try (InputStream input = new FileInputStream(envConfigPath)) {
            Properties envProperties = new Properties();
            envProperties.load(input);
            properties.putAll(envProperties);
            logger.info("Loaded {} environment properties", environment);
        } catch (IOException e) {
            logger.warn("Environment-specific config not found: {}", envConfigPath);
        }
    }

    /**
     * Load default properties if config file not found
     */
    private void loadDefaultProperties() {
        logger.warn("Loading default properties");
        properties.setProperty("browser", "chrome");
        properties.setProperty("base.url", "https://example.com");
        properties.setProperty("implicit.wait", "10");
        properties.setProperty("explicit.wait", "20");
        properties.setProperty("page.load.timeout", "30");
        properties.setProperty("headless", "false");
        properties.setProperty("healenium.enabled", "false");
    }

    /**
     * Get property value
     */
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property '{}' not found", key);
        }
        return value;
    }

    /**
     * Get property value with default
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get property as integer
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for property '{}': {}", key, value);
            }
        }
        return defaultValue;
    }

    /**
     * Get property as boolean
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Get property as long
     */
    public long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for property '{}': {}", key, value);
            }
        }
        return defaultValue;
    }

    /**
     * Set property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Check if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Get all properties
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }

    /**
     * Reload properties
     */
    public void reloadProperties() {
        properties.clear();
        loadProperties();
    }
}
