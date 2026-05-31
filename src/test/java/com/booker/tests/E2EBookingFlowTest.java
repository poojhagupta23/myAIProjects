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
 * E2EBookingFlowTest — End-to-End Test for Create → Get Booking Flow
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * Flow Under Test:
 *
 *   [Step 1] POST /booking     — Create a new booking with valid payload
 *       ↓
 *   [Step 2] Extract bookingId — Capture the bookingid from the create response
 *       ↓
 *   [Step 3] GET /booking/{id} — Retrieve the booking using the captured ID
 *       ↓
 *   [Step 4] Validate          — Assert all GET response fields match the original request
 *
 * Test Cases:
 *   TC_E2E_001 — Full E2E: Create → Capture ID → Get → Validate data integrity
 *
 * Design Decision: This test deliberately does NOT depend on CreateBookingTest
 * or GetBookingTest to be run first. It is fully self-contained — it creates
 * its own booking data independently, ensuring E2E test isolation.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@Epic("Restful Booker API")
@Feature("End-to-End Booking Flow")
public class E2EBookingFlowTest extends BaseTest {

    private BookingApiClient bookingApiClient;
    private BookingRequest   validBookingRequest;

    /**
     * Class-level setup — initializes API client and loads test data.
     */
    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        bookingApiClient    = createApiClient();
        validBookingRequest = TestDataLoader.loadBookingRequest(
                ConfigReader.getProperty("testdata.valid.booking")
        );
        log.info("[E2EBookingFlowTest] Setup complete. Test data loaded.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  END-TO-END TEST
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC_E2E_001
     * Full end-to-end validation of the booking lifecycle:
     *   1. Create a booking → verify HTTP 200 + valid bookingId
     *   2. Retrieve the booking by ID → verify HTTP 200
     *   3. Validate ALL fields in the GET response match the original POST request
     *
     * This is the most critical test in the suite — it validates the complete
     * system integration between the Create and Get Booking APIs.
     */
    @Test(
        testName    = "TC_E2E_001",
        description = "E2E: Create a booking → capture bookingId → retrieve it → validate all fields match",
        groups      = {"e2e", "smoke", "regression"}
    )
    @Story("End-to-End Booking Flow")
    @Severity(SeverityLevel.BLOCKER)
    @Description(
        "End-to-End test that validates the complete booking lifecycle: "
        + "POST /booking → capture bookingId → GET /booking/{id} → validate all fields match the original request."
    )
    public void tc_e2e_001_createAndRetrieveBooking_fullFlowValidation() {

        // ══════════════════════════════════════════════════════════════════════
        //  STEP 1 — Create Booking
        // ══════════════════════════════════════════════════════════════════════
        log.info("[E2E TC_E2E_001] STEP 1: Creating booking via POST /booking...");

        Response createResponse = bookingApiClient.createBooking(validBookingRequest);

        // Assert: Create must succeed with HTTP 200
        Assert.assertEquals(
            createResponse.getStatusCode(),
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_E2E_001 STEP 1 FAILED: POST /booking did not return HTTP 200. Got: "
            + createResponse.getStatusCode()
        );

        // Deserialize create response
        BookingResponse createBookingResponse = createResponse.as(BookingResponse.class);
        Assert.assertNotNull(
            createBookingResponse,
            "TC_E2E_001 STEP 1 FAILED: Create response body could not be deserialized."
        );

        // ══════════════════════════════════════════════════════════════════════
        //  STEP 2 — Capture bookingId
        // ══════════════════════════════════════════════════════════════════════
        int capturedBookingId = createBookingResponse.getBookingid();
        log.info("[E2E TC_E2E_001] STEP 2: Captured bookingId = {}", capturedBookingId);

        Assert.assertTrue(
            capturedBookingId > 0,
            "TC_E2E_001 STEP 2 FAILED: bookingId must be a positive integer. Got: " + capturedBookingId
        );

        // ══════════════════════════════════════════════════════════════════════
        //  STEP 3 — Retrieve Booking by Captured ID
        // ══════════════════════════════════════════════════════════════════════
        log.info("[E2E TC_E2E_001] STEP 3: Retrieving booking via GET /booking/{}...", capturedBookingId);

        Response getResponse = bookingApiClient.getBookingById(capturedBookingId);

        // Assert: Get must succeed with HTTP 200
        Assert.assertEquals(
            getResponse.getStatusCode(),
            ConfigReader.getIntProperty("status.code.ok"),
            "TC_E2E_001 STEP 3 FAILED: GET /booking/" + capturedBookingId
            + " did not return HTTP 200. Got: " + getResponse.getStatusCode()
        );

        // ══════════════════════════════════════════════════════════════════════
        //  STEP 4 — Validate All Fields Match Original Request
        // ══════════════════════════════════════════════════════════════════════
        log.info("[E2E TC_E2E_001] STEP 4: Validating GET response fields against original POST payload...");

        // GET /booking/{id} returns just the booking object (not wrapped in bookingid)
        BookingRequest retrievedBooking = getResponse.as(BookingRequest.class);
        Assert.assertNotNull(
            retrievedBooking,
            "TC_E2E_001 STEP 4 FAILED: GET response body is null or could not be deserialized."
        );

        // ── Validate: firstname ───────────────────────────────────────────────
        Assert.assertEquals(
            retrievedBooking.getFirstname(),
            validBookingRequest.getFirstname(),
            "TC_E2E_001 STEP 4 FAILED: 'firstname' mismatch. "
            + "Expected: " + validBookingRequest.getFirstname()
            + ", Got: "    + retrievedBooking.getFirstname()
        );

        // ── Validate: lastname ────────────────────────────────────────────────
        Assert.assertEquals(
            retrievedBooking.getLastname(),
            validBookingRequest.getLastname(),
            "TC_E2E_001 STEP 4 FAILED: 'lastname' mismatch. "
            + "Expected: " + validBookingRequest.getLastname()
            + ", Got: "    + retrievedBooking.getLastname()
        );

        // ── Validate: totalprice ──────────────────────────────────────────────
        Assert.assertEquals(
            retrievedBooking.getTotalprice(),
            validBookingRequest.getTotalprice(),
            "TC_E2E_001 STEP 4 FAILED: 'totalprice' mismatch. "
            + "Expected: " + validBookingRequest.getTotalprice()
            + ", Got: "    + retrievedBooking.getTotalprice()
        );

        // ── Validate: depositpaid ─────────────────────────────────────────────
        Assert.assertEquals(
            retrievedBooking.isDepositpaid(),
            validBookingRequest.isDepositpaid(),
            "TC_E2E_001 STEP 4 FAILED: 'depositpaid' mismatch."
        );

        // ── Validate: additionalneeds ─────────────────────────────────────────
        Assert.assertEquals(
            retrievedBooking.getAdditionalneeds(),
            validBookingRequest.getAdditionalneeds(),
            "TC_E2E_001 STEP 4 FAILED: 'additionalneeds' mismatch. "
            + "Expected: " + validBookingRequest.getAdditionalneeds()
            + ", Got: "    + retrievedBooking.getAdditionalneeds()
        );

        // ── Validate: checkin date ────────────────────────────────────────────
        Assert.assertNotNull(
            retrievedBooking.getBookingdates(),
            "TC_E2E_001 STEP 4 FAILED: 'bookingdates' object is null in GET response."
        );
        Assert.assertEquals(
            retrievedBooking.getBookingdates().getCheckin(),
            validBookingRequest.getBookingdates().getCheckin(),
            "TC_E2E_001 STEP 4 FAILED: 'checkin' date mismatch. "
            + "Expected: " + validBookingRequest.getBookingdates().getCheckin()
            + ", Got: "    + retrievedBooking.getBookingdates().getCheckin()
        );

        // ── Validate: checkout date ───────────────────────────────────────────
        Assert.assertEquals(
            retrievedBooking.getBookingdates().getCheckout(),
            validBookingRequest.getBookingdates().getCheckout(),
            "TC_E2E_001 STEP 4 FAILED: 'checkout' date mismatch. "
            + "Expected: " + validBookingRequest.getBookingdates().getCheckout()
            + ", Got: "    + retrievedBooking.getBookingdates().getCheckout()
        );

        // ══════════════════════════════════════════════════════════════════════
        //  E2E FLOW COMPLETE
        // ══════════════════════════════════════════════════════════════════════
        log.info("TC_E2E_001 PASSED ✅ — Full E2E flow validated successfully!");
        log.info("  bookingId    : {}", capturedBookingId);
        log.info("  firstname    : {}", retrievedBooking.getFirstname());
        log.info("  lastname     : {}", retrievedBooking.getLastname());
        log.info("  totalprice   : {}", retrievedBooking.getTotalprice());
        log.info("  depositpaid  : {}", retrievedBooking.isDepositpaid());
        log.info("  checkin      : {}", retrievedBooking.getBookingdates().getCheckin());
        log.info("  checkout     : {}", retrievedBooking.getBookingdates().getCheckout());
    }
}
