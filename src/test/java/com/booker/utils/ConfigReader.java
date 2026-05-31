package com.booker.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader — Centralized Configuration Loader
 *
 * Reads all values from src/test/resources/config.properties.
 * Uses a static initializer to load properties ONCE at class-load time,
 * ensuring no repeated I/O during test execution.
 *
 * Design Decisions:
 * - Static methods with no instance state = thread-safe reads.
 * - Fails fast with a descriptive RuntimeException if config is missing or broken.
 *   This surfaces misconfigurations immediately, rather than failing mid-test with
 *   a NullPointerException.
 * - getIntProperty() convenience method avoids Integer.parseInt() boilerplate in tests.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
public final class ConfigReader {

    private static final Logger log        = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES;

    // ── Static Initializer — loads config once at class-load time ─────────────
    static {
        PROPERTIES = new Properties();
        try (InputStream inputStream = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (inputStream == null) {
                throw new ExceptionInInitializerError(
                    "[ConfigReader] FATAL: '" + CONFIG_FILE + "' not found on classpath. " +
                    "Ensure it exists at src/test/resources/" + CONFIG_FILE
                );
            }

            PROPERTIES.load(inputStream);
            log.info("[ConfigReader] Successfully loaded '{}' with {} properties.", CONFIG_FILE, PROPERTIES.size());

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                "[ConfigReader] FATAL: Failed to load '" + CONFIG_FILE + "': " + e.getMessage()
            );
        }
    }

    // ── Private constructor: utility class ────────────────────────────────────
    private ConfigReader() {
        throw new UnsupportedOperationException("ConfigReader is a utility class.");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Retrieves a property value by key.
     *
     * @param key the property key (e.g., "base.url")
     * @return the property value as String
     * @throws RuntimeException if the key is not found or the value is blank
     */
    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                "[ConfigReader] Property key '" + key + "' not found in " + CONFIG_FILE + ". " +
                "Please verify the key exists and has a non-empty value."
            );
        }
        return value.trim();
    }

    /**
     * Retrieves a property value as an integer.
     * Useful for status codes, timeouts, or numeric thresholds.
     *
     * @param key the property key
     * @return the value parsed as int
     * @throws RuntimeException if the key is missing or the value is not a valid integer
     */
    public static int getIntProperty(String key) {
        String rawValue = getProperty(key);
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                "[ConfigReader] Property '" + key + "' has value '" + rawValue + "' " +
                "which cannot be parsed as an integer."
            );
        }
    }

    /**
     * Retrieves a property value as a boolean.
     *
     * @param key the property key
     * @return true if value is "true" (case-insensitive), false otherwise
     */
    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }
}
