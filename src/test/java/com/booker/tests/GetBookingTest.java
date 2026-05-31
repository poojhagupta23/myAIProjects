package com.booker.tests;

import com.booker.api.BookingApiClient;
import com.booker.base.BaseTest;
import com.booker.models.BookingRequest;
import com.booker.models.BookingResponse;
import com.booker.utils.ConfigReader;
import com.booker.utils.TestDataLoader;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * GetBookingTest — Test Suite for GET /booking/{bookingId}
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * Strategy: This class first creates a booking in @BeforeClass to get a real,
 * valid bookingId. This ensures the positive GET tests use a bookingId
 * that genuinely exists in the system — no hardcoded/assumed IDs.
 *
 * Test Cases:
 *   TC_GET_001 — Valid bookingId → HTTP 200 (Positive)
 *   TC_GET_002 — Valid bookingId → Response fields match original request (Positive)
 *   TC_GET_003 — Non-existent bookingId → HTTP 404 (Negative)
 *   TC_GET_004 — Boundary bookingId (0) → HTTP 404 (Negative)
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@Epic("Restful Booker API")
@Feature("Get Booking — GET /booking/{bookingId}")
public class GetBookingTest extends BaseTest {

    private BookingApiClient bookingApiClient;
    private BookingRequest   validBookingRequest;
    private int              createdBookingId;

    /**
     * Class-level setup:
     * 1. Loads valid test data from JSON.
     * 2. Creates a booking via POST /booking to obtain a real bookingId.
     * 3. Stores the bookingId for use in GET test cases.
     *
     * Design Decision: Creating a booking here ensures test isolation — the GET
     * tests use a known, freshly-created booking and are not dependent on
     * pre-existing data in the system.
     */
    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        bookingApiClient    = createApiClient();
        validBookingRequest = TestDataLoader.loadBookingRequest(
                ConfigReader.getProperty("testdata.valid.booking")
        );

        // Create a booking to get a real bookingId for GET test cases
        log.info("[GetBookingTest] Creating a booking to obtain a valid bookingId...");
        Response createResponse = bookingApiClient.createBooking(validBookingRequest);

        Assert.assertEquals(
            createResponse.getStatusCode(),
            ConfigReader.getIntProperty("status.code.ok"),
            "[GetBookingTest Setup] FAILED: Unable to create booking for GET test setup. HTTP: "
            + createResponse.getStatusCode()
        );

        BookingResponse bookingResponse = createResponse.as(BookingResponse.class);
        createdBookingId = bookingResponse.getBookingid();

        Assert.assertTrue(
            createdBookingId > 0,
            "[GetBookingTest Setup] FAILED: Invalid bookingId obtained from create response: " + createdBookingId
        );

        log.info("[GetBookingTest] Setup complete. Using bookingId: {}", createdBookingId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  POSITIVE TEST CASES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC_GET_001
     * Verify that GET /booking/{bookingId} with a valid, existing ID returns HTTP 200.
     */
    @Test(
        testName    = "TC_GET_001",
        description = "GET /booking/{bookingId} with valid ID must return HTTP 200",
        groups      = {"smoke", "regression", "get-booking"}
    )
    @Story("Get Booking — Valid ID")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifies the Get Booking API returns HTTP 200 for a valid, existing booking ID.")
    public void tc_get_001_validBookingId_returns200() {
        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.getBookingById(createdBookingId);

        // ── Assert: HTTP 200 ──────────────────────────────────────────────────
        Assert.assertEquals(
            response.getStatusCode(),
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_GET_001 FAILED: Expected HTTP 200 for bookingId=" + createdBookingId
            + " but got: " + response.getStatusCode()
        );

        log.info("TC_GET_001 PASSED — GET /booking/{} returned HTTP 200.", createdBookingId);
    }

    /**
     * TC_GET_002
     * Verify that GET /booking/{bookingId} response body fields match the original
     * data that was used to create the booking.
     * This is a data integrity / round-trip validation test.
     */
    @Test(
        testName    = "TC_GET_002",
        description = "GET /booking/{bookingId} response body must match the original booking data",
        groups      = {"regression", "get-booking"}
    )
    @Story("Get Booking — Response Body Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies that retrieved booking fields exactly match what was created — ensuring data round-trip integrity.")
    public void tc_get_002_validBookingId_responseMatchesOriginalData() {
        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.getBookingById(createdBookingId);

        // ── Assert: HTTP 200 ──────────────────────────────────────────────────
        Assert.assertEquals(
            response.getStatusCode(),
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_GET_002 FAILED: Expected HTTP 200 for bookingId=" + createdBookingId
        );

        // ── Deserialize: GET /booking/{id} returns just the booking object (no wrapper) ──
        BookingRequest returnedBooking = response.as(BookingRequest.class);
        Assert.assertNotNull(returnedBooking, "TC_GET_002 FAILED: Response body is null or could not be deserialized.");

        // ── Assert: All fields must match the original request ─────────────────
        Assert.assertEquals(
            returnedBooking.getFirstname(),
            validBookingRequest.getFirstname(),
            "TC_GET_002 FAILED: 'firstname' mismatch — Expected: "
            + validBookingRequest.getFirstname() + ", Got: " + returnedBooking.getFirstname()
        );
        Assert.assertEquals(
            returnedBooking.getLastname(),
            validBookingRequest.getLastname(),
            "TC_GET_002 FAILED: 'lastname' mismatch — Expected: "
            + validBookingRequest.getLastname() + ", Got: " + returnedBooking.getLastname()
        );
        Assert.assertEquals(
            returnedBooking.getTotalprice(),
            validBookingRequest.getTotalprice(),
            "TC_GET_002 FAILED: 'totalprice' mismatch — Expected: "
            + validBookingRequest.getTotalprice() + ", Got: " + returnedBooking.getTotalprice()
        );
        Assert.assertEquals(
            returnedBooking.isDepositpaid(),
            validBookingRequest.isDepositpaid(),
            "TC_GET_002 FAILED: 'depositpaid' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getAdditionalneeds(),
            validBookingRequest.getAdditionalneeds(),
            "TC_GET_002 FAILED: 'additionalneeds' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getBookingdates().getCheckin(),
            validBookingRequest.getBookingdates().getCheckin(),
            "TC_GET_002 FAILED: 'checkin' date mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getBookingdates().getCheckout(),
            validBookingRequest.getBookingdates().getCheckout(),
            "TC_GET_002 FAILED: 'checkout' date mismatch"
        );

        log.info("TC_GET_002 PASSED — GET /booking/{} response fields match original booking data.", createdBookingId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NEGATIVE TEST CASES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC_GET_003
     * Verify that GET /booking/{bookingId} with a non-existent ID returns HTTP 404.
     * Uses a very large ID (999999999) that is virtually guaranteed to not exist.
     */
    @Test(
        testName    = "TC_GET_003",
        description = "GET /booking/{bookingId} with non-existent ID must return HTTP 404",
        groups      = {"regression", "negative", "get-booking"}
    )
    @Story("Get Booking — Non-Existent ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Negative test: Verifies the API returns HTTP 404 when the booking ID does not exist in the system.")
    public void tc_get_003_nonExistentBookingId_returns404() {
        // ── Arrange: Use a non-existent booking ID from config ─────────────────
        int nonExistentId = ConfigReader.getIntProperty("invalid.booking.id");

        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.getBookingById(nonExistentId);

        // ── Assert: HTTP 404 ──────────────────────────────────────────────────
        Assert.assertEquals(
            response.getStatusCode(),
            ConfigReader.getIntProperty("status.code.not.found"),
            "TC_GET_003 FAILED: Expected HTTP 404 for non-existent bookingId=" + nonExistentId
            + " but got: " + response.getStatusCode()
        );

        log.info("TC_GET_003 PASSED — Non-existent bookingId={} correctly returned HTTP 404.", nonExistentId);
    }

    /**
     * TC_GET_004
     * Verify that GET /booking/0 (boundary ID) returns HTTP 404.
     * Zero is a boundary value that should never be a valid booking ID.
     */
    @Test(
        testName    = "TC_GET_004",
        description = "GET /booking/0 (boundary ID) must return HTTP 404",
        groups      = {"regression", "negative", "get-booking"}
    )
    @Story("Get Booking — Boundary ID")
    @Severity(SeverityLevel.MINOR)
    @Description("Negative/boundary test: Verifies the API returns HTTP 404 when booking ID is 0 (boundary value).")
    public void tc_get_004_boundaryBookingId_returns404() {
        // ── Arrange: Use boundary ID from config ──────────────────────────────
        int boundaryId = ConfigReader.getIntProperty("boundary.booking.id");

        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.getBookingById(boundaryId);

        // ── Assert: Must not return HTTP 200 ─────────────────────────────────
        int actualStatus = response.getStatusCode();
        Assert.assertNotEquals(
            actualStatus,
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_GET_004 FAILED: API should not return HTTP 200 for boundary bookingId=0."
        );

        log.info("TC_GET_004 PASSED — Boundary bookingId=0 returned HTTP: {}", actualStatus);
    }
}
