package com.booker.utils;

import com.booker.models.BookingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * TestDataLoader — JSON Test Data File Loader
 *
 * Reads test data from JSON files under src/test/resources/testdata/.
 * Uses Jackson ObjectMapper to deserialize JSON into typed POJO instances.
 *
 * Design Decisions:
 * - ObjectMapper is static and final: created once, reused across all calls.
 *   ObjectMapper is thread-safe for reading; this pattern is the Jackson best practice.
 * - Uses ClassLoader.getResourceAsStream() so paths are classpath-relative,
 *   making the framework portable across different machines and CI environments.
 * - All exceptions are wrapped in RuntimeException with descriptive messages
 *   to surface data-loading failures clearly during test setup.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
public final class TestDataLoader {

    private static final Logger       log           = LoggerFactory.getLogger(TestDataLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String       TEST_DATA_DIR = "testdata/";

    // ── Private constructor: utility class ────────────────────────────────────
    private TestDataLoader() {
        throw new UnsupportedOperationException("TestDataLoader is a utility class.");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Loads a BookingRequest from a JSON file in the testdata directory.
     *
     * @param fileName the JSON filename (e.g., "booking.json")
     * @return a fully deserialized BookingRequest POJO
     * @throws RuntimeException if the file is not found or deserialization fails
     */
    public static BookingRequest loadBookingRequest(String fileName) {
        String resourcePath = TEST_DATA_DIR + fileName;
        log.info("[TestDataLoader] Loading BookingRequest from: {}", resourcePath);

        try (InputStream inputStream = TestDataLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new RuntimeException(
                    "[TestDataLoader] Test data file not found on classpath: '" + resourcePath + "'. " +
                    "Ensure the file exists at src/test/resources/" + resourcePath
                );
            }

            BookingRequest bookingRequest = OBJECT_MAPPER.readValue(inputStream, BookingRequest.class);
            log.info("[TestDataLoader] Successfully loaded BookingRequest: {}", bookingRequest);
            return bookingRequest;

        } catch (IOException e) {
            throw new RuntimeException(
                "[TestDataLoader] Failed to deserialize '" + resourcePath + "' into BookingRequest: " + e.getMessage(), e
            );
        }
    }

    /**
     * Loads raw JSON string from a file in the testdata directory.
     * Used in negative test cases where we need to manipulate raw JSON.
     *
     * @param fileName the JSON filename
     * @return raw JSON content as String
     */
    public static String loadRawJson(String fileName) {
        String resourcePath = TEST_DATA_DIR + fileName;
        log.info("[TestDataLoader] Loading raw JSON from: {}", resourcePath);

        try (InputStream inputStream = TestDataLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new RuntimeException(
                    "[TestDataLoader] File not found: '" + resourcePath + "'"
                );
            }

            return new String(inputStream.readAllBytes());

        } catch (IOException e) {
            throw new RuntimeException(
                "[TestDataLoader] Failed to read raw JSON from '" + resourcePath + "': " + e.getMessage(), e
            );
        }
    }
}
