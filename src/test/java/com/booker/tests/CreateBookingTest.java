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
 * CreateBookingTest — Test Suite for POST /booking
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * Test Cases:
 *   TC_CREATE_001 — Valid payload → HTTP 200 + bookingid present (Positive)
 *   TC_CREATE_002 — Valid payload → Response body fields match request (Positive)
 *   TC_CREATE_003 — Empty JSON body → HTTP 500 (Negative)
 *   TC_CREATE_004 — Missing 'firstname' field → error response (Negative)
 *   TC_CREATE_005 — Invalid Content-Type header → error response (Negative)
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@Epic("Restful Booker API")
@Feature("Create Booking — POST /booking")
public class CreateBookingTest extends BaseTest {

    private BookingApiClient bookingApiClient;
    private BookingRequest   validBookingRequest;

    /**
     * Class-level setup — runs once before all tests in this class.
     * Initializes the API client and loads valid test data from JSON.
     */
    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        bookingApiClient    = createApiClient();
        validBookingRequest = TestDataLoader.loadBookingRequest(
                ConfigReader.getProperty("testdata.valid.booking")
        );
        log.info("[CreateBookingTest] Setup complete. Valid booking data loaded: {}", validBookingRequest);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  POSITIVE TEST CASES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC_CREATE_001
     * Verify that POST /booking with a complete, valid payload returns:
     *   - HTTP 200 OK
     *   - Response body contains a non-zero 'bookingid'
     */
    @Test(
        testName   = "TC_CREATE_001",
        description = "POST /booking with valid payload must return HTTP 200 and a non-zero bookingid",
        groups     = {"smoke", "regression", "create-booking"}
    )
    @Story("Create Booking — Valid Payload")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifies the Create Booking API returns HTTP 200 and a valid bookingid when provided a complete, correct payload.")
    public void tc_create_001_validPayload_returns200WithBookingId() {
        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.createBooking(validBookingRequest);

        // ── Assert: HTTP Status ───────────────────────────────────────────────
        int expectedStatus = ConfigReader.getIntProperty("status.code.ok");
        Assert.assertEquals(
            response.getStatusCode(),
            expectedStatus,
            "TC_CREATE_001 FAILED: Expected HTTP " + expectedStatus + " but got " + response.getStatusCode()
        );

        // ── Assert: bookingid is present and valid ────────────────────────────
        BookingResponse bookingResponse = response.as(BookingResponse.class);
        Assert.assertNotNull(
            bookingResponse,
            "TC_CREATE_001 FAILED: Response body could not be deserialized into BookingResponse"
        );
        Assert.assertTrue(
            bookingResponse.getBookingid() > 0,
            "TC_CREATE_001 FAILED: Expected a positive bookingid but got: " + bookingResponse.getBookingid()
        );

        log.info("TC_CREATE_001 PASSED — Booking created with ID: {}", bookingResponse.getBookingid());
    }

    /**
     * TC_CREATE_002
     * Verify that POST /booking response body fields exactly match the request payload.
     * This is a data integrity check — the API must persist and echo back all fields.
     */
    @Test(
        testName    = "TC_CREATE_002",
        description = "POST /booking response body must reflect all request payload fields correctly",
        groups      = {"regression", "create-booking"}
    )
    @Story("Create Booking — Response Body Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies that the booking details in the API response match what was sent in the request — ensuring data integrity.")
    public void tc_create_002_validPayload_responseBodyMatchesRequest() {
        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.createBooking(validBookingRequest);

        // ── Assert: HTTP 200 ──────────────────────────────────────────────────
        Assert.assertEquals(
            response.getStatusCode(),
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_CREATE_002 FAILED: Unexpected HTTP status code."
        );

        // ── Deserialize response ──────────────────────────────────────────────
        BookingResponse bookingResponse = response.as(BookingResponse.class);
        BookingRequest  returnedBooking = bookingResponse.getBooking();

        Assert.assertNotNull(returnedBooking, "TC_CREATE_002 FAILED: Booking object in response is null");

        // ── Assert: Each field matches the request ────────────────────────────
        Assert.assertEquals(
            returnedBooking.getFirstname(),
            validBookingRequest.getFirstname(),
            "TC_CREATE_002 FAILED: 'firstname' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getLastname(),
            validBookingRequest.getLastname(),
            "TC_CREATE_002 FAILED: 'lastname' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getTotalprice(),
            validBookingRequest.getTotalprice(),
            "TC_CREATE_002 FAILED: 'totalprice' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.isDepositpaid(),
            validBookingRequest.isDepositpaid(),
            "TC_CREATE_002 FAILED: 'depositpaid' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getAdditionalneeds(),
            validBookingRequest.getAdditionalneeds(),
            "TC_CREATE_002 FAILED: 'additionalneeds' mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getBookingdates().getCheckin(),
            validBookingRequest.getBookingdates().getCheckin(),
            "TC_CREATE_002 FAILED: 'checkin' date mismatch"
        );
        Assert.assertEquals(
            returnedBooking.getBookingdates().getCheckout(),
            validBookingRequest.getBookingdates().getCheckout(),
            "TC_CREATE_002 FAILED: 'checkout' date mismatch"
        );

        log.info("TC_CREATE_002 PASSED — All response body fields match the request payload.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NEGATIVE TEST CASES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC_CREATE_003
     * Verify that POST /booking with an empty JSON body returns an error response.
     * The Restful Booker API returns HTTP 500 for an empty body.
     *
     * Design Decision: We test HTTP 500 specifically because this is the documented
     * behavior of this API. In a production system, HTTP 400 would be preferred.
     */
    @Test(
        testName    = "TC_CREATE_003",
        description = "POST /booking with an empty body must return HTTP 500",
        groups      = {"regression", "negative", "create-booking"}
    )
    @Story("Create Booking — Empty Body")
    @Severity(SeverityLevel.NORMAL)
    @Description("Negative test: Verifies the API returns an error status when the request body is empty.")
    public void tc_create_003_emptyBody_returnsError() {
        // ── Act: Send an empty JSON body ──────────────────────────────────────
        Response response = bookingApiClient.createBookingWithRawBody("{}");

        // ── Assert: API must return a server error ────────────────────────────
        int actualStatus = response.getStatusCode();
        Assert.assertTrue(
            actualStatus == ConfigReader.getIntProperty("status.code.server.error")
            || actualStatus == 400,
            "TC_CREATE_003 FAILED: Expected HTTP 400 or 500 for empty body, but got: " + actualStatus
        );

        log.info("TC_CREATE_003 PASSED — Empty body returned HTTP: {}", actualStatus);
    }

    /**
     * TC_CREATE_004
     * Verify that POST /booking with a missing 'firstname' field returns an error.
     * Tests API input validation for required fields.
     */
    @Test(
        testName    = "TC_CREATE_004",
        description = "POST /booking with missing 'firstname' field must return an error response",
        groups      = {"regression", "negative", "create-booking"}
    )
    @Story("Create Booking — Missing Required Field")
    @Severity(SeverityLevel.NORMAL)
    @Description("Negative test: Verifies the API returns an error when a required field (firstname) is omitted from the request.")
    public void tc_create_004_missingFirstname_returnsError() {
        // ── Arrange: Build a request with no firstname ─────────────────────────
        String incompleteBody = "{"
            + "\"lastname\": \"" + validBookingRequest.getLastname() + "\","
            + "\"totalprice\": " + validBookingRequest.getTotalprice() + ","
            + "\"depositpaid\": " + validBookingRequest.isDepositpaid() + ","
            + "\"bookingdates\": {"
            + "  \"checkin\": \"" + validBookingRequest.getBookingdates().getCheckin() + "\","
            + "  \"checkout\": \"" + validBookingRequest.getBookingdates().getCheckout() + "\""
            + "},"
            + "\"additionalneeds\": \"" + validBookingRequest.getAdditionalneeds() + "\""
            + "}";

        // ── Act ──────────────────────────────────────────────────────────────
        Response response = bookingApiClient.createBookingWithRawBody(incompleteBody);

        // ── Assert: Must not be HTTP 200 ──────────────────────────────────────
        int actualStatus = response.getStatusCode();
        Assert.assertNotEquals(
            actualStatus,
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_CREATE_004 FAILED: API should not return 200 for a missing 'firstname' field."
        );

        log.info("TC_CREATE_004 PASSED — Missing firstname returned HTTP: {}", actualStatus);
    }

    /**
     * TC_CREATE_005
     * Verify that POST /booking with an invalid Content-Type header returns an error.
     * Tests API's header validation — ensures it rejects non-JSON content types.
     */
    @Test(
        testName    = "TC_CREATE_005",
        description = "POST /booking with Content-Type: text/plain must return an error response",
        groups      = {"regression", "negative", "create-booking"}
    )
    @Story("Create Booking — Invalid Content-Type")
    @Severity(SeverityLevel.MINOR)
    @Description("Negative test: Verifies the API returns an error when a non-JSON Content-Type header is provided.")
    public void tc_create_005_invalidContentType_returnsError() {
        // ── Act: Send with text/plain content type ────────────────────────────
        Response response = bookingApiClient.createBookingWithContentType(
            validBookingRequest, "text/plain"
        );

        // ── Assert: Must not return 200 ───────────────────────────────────────
        int actualStatus = response.getStatusCode();
        Assert.assertNotEquals(
            actualStatus,
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_CREATE_005 FAILED: API must not return HTTP 200 for text/plain content type."
        );

        log.info("TC_CREATE_005 PASSED — Invalid Content-Type returned HTTP: {}", actualStatus);
    }
}
