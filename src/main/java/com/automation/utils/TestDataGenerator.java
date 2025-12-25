package com.automation.utils;

import com.github.javafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

/**
 * Test Data Generator - Generates random test data using JavaFaker
 * Useful for creating realistic test data dynamically
 */
public class TestDataGenerator {

    private static final Logger logger = LogManager.getLogger(TestDataGenerator.class);
    private final Faker faker;
    private final Random random;

    /**
     * Constructor with default locale (US)
     */
    public TestDataGenerator() {
        this(Locale.US);
    }

    /**
     * Constructor with custom locale
     */
    public TestDataGenerator(Locale locale) {
        this.faker = new Faker(locale);
        this.random = new Random();
    }

    // ==================== User Data ====================

    /**
     * Generate random first name
     */
    public String firstName() {
        return faker.name().firstName();
    }

    /**
     * Generate random last name
     */
    public String lastName() {
        return faker.name().lastName();
    }

    /**
     * Generate random full name
     */
    public String fullName() {
        return faker.name().fullName();
    }

    /**
     * Generate random username
     */
    public String username() {
        return faker.name().username();
    }

    /**
     * Generate random email
     */
    public String email() {
        return faker.internet().emailAddress();
    }

    /**
     * Generate email with specific domain
     */
    public String email(String domain) {
        return faker.name().username().toLowerCase() + "@" + domain;
    }

    /**
     * Generate random password
     */
    public String password() {
        return faker.internet().password(8, 16, true, true, true);
    }

    /**
     * Generate strong password with requirements
     */
    public String strongPassword() {
        return faker.internet().password(12, 20, true, true, true);
    }

    // ==================== Address Data ====================

    /**
     * Generate random street address
     */
    public String streetAddress() {
        return faker.address().streetAddress();
    }

    /**
     * Generate random city
     */
    public String city() {
        return faker.address().city();
    }

    /**
     * Generate random state
     */
    public String state() {
        return faker.address().state();
    }

    /**
     * Generate random zip code
     */
    public String zipCode() {
        return faker.address().zipCode();
    }

    /**
     * Generate random country
     */
    public String country() {
        return faker.address().country();
    }

    /**
     * Generate full address
     */
    public String fullAddress() {
        return faker.address().fullAddress();
    }

    // ==================== Phone Data ====================

    /**
     * Generate random phone number
     */
    public String phoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }

    /**
     * Generate cell phone number
     */
    public String cellPhone() {
        return faker.phoneNumber().cellPhone();
    }

    // ==================== Company Data ====================

    /**
     * Generate random company name
     */
    public String companyName() {
        return faker.company().name();
    }

    /**
     * Generate random job title
     */
    public String jobTitle() {
        return faker.job().title();
    }

    // ==================== Date Data ====================

    /**
     * Generate random past date
     */
    public String pastDate(int daysBack) {
        LocalDate date = LocalDate.now().minusDays(random.nextInt(daysBack));
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Generate random future date
     */
    public String futureDate(int daysAhead) {
        LocalDate date = LocalDate.now().plusDays(random.nextInt(daysAhead));
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Generate random date between range
     */
    public String dateBetween(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        long daysBetween = end.toEpochDay() - start.toEpochDay();
        LocalDate randomDate = start.plusDays(random.nextInt((int) daysBetween));
        return randomDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // ==================== Financial Data ====================

    /**
     * Generate random credit card number
     */
    public String creditCardNumber() {
        return faker.finance().creditCard();
    }

    /**
     * Generate random IBAN
     */
    public String iban() {
        return faker.finance().iban();
    }

    // ==================== Lorem Ipsum ====================

    /**
     * Generate random sentence
     */
    public String sentence() {
        return faker.lorem().sentence();
    }

    /**
     * Generate random paragraph
     */
    public String paragraph() {
        return faker.lorem().paragraph();
    }

    /**
     * Generate random text with specified word count
     */
    public String words(int count) {
        return String.join(" ", faker.lorem().words(count));
    }

    // ==================== Numeric Data ====================

    /**
     * Generate random number in range
     */
    public int numberBetween(int min, int max) {
        return faker.number().numberBetween(min, max);
    }

    /**
     * Generate random double in range
     */
    public double doubleBetween(double min, double max) {
        return faker.number().randomDouble(2, (long) min, (long) max);
    }

    /**
     * Generate random digits
     */
    public String digits(int count) {
        return faker.number().digits(count);
    }

    // ==================== ID Generation ====================

    /**
     * Generate UUID
     */
    public String uuid() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Generate random ID with prefix
     */
    public String idWithPrefix(String prefix) {
        return prefix + "_" + faker.number().digits(8);
    }

    // ==================== Web Data ====================

    /**
     * Generate random URL
     */
    public String url() {
        return faker.internet().url();
    }

    /**
     * Generate random domain name
     */
    public String domainName() {
        return faker.internet().domainName();
    }

    /**
     * Generate random IP address
     */
    public String ipAddress() {
        return faker.internet().ipV4Address();
    }

    // ==================== Custom Patterns ====================

    /**
     * Generate data matching regex pattern
     */
    public String regexify(String pattern) {
        return faker.regexify(pattern);
    }

    /**
     * Generate data matching numerify pattern (# = digit)
     */
    public String numerify(String pattern) {
        return faker.numerify(pattern);
    }

    /**
     * Generate data matching letterify pattern (? = letter)
     */
    public String letterify(String pattern) {
        return faker.letterify(pattern);
    }

    /**
     * Generate data matching bothify pattern (# = digit, ? = letter)
     */
    public String bothify(String pattern) {
        return faker.bothify(pattern);
    }
}
