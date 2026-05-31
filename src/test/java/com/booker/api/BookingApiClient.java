package com.booker.api;

import com.booker.models.BookingRequest;
import com.booker.utils.ConfigReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * LAYER 1 — API Object Model: HTTP Client
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * Responsibility: Encapsulates ALL RestAssured HTTP communications.
 *
 * Design Rationale:
 * - Test classes NEVER call RestAssured directly — they always go through this client.
 * - This ensures a single point of change if the HTTP library needs to be swapped.
 * - The RequestSpecification is built once per client instance and reused,
 *   reducing code duplication and ensuring consistent headers across all calls.
 * - AllureRestAssured filter is applied globally to capture all request/response
 *   details in Allure reports automatically.
 * - @Step annotations on each method provide Allure step visibility in reports.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
public class BookingApiClient {

    private static final Logger log = LoggerFactory.getLogger(BookingApiClient.class);

    /**
     * Shared base request specification.
     * Configured once — base URI, headers, and Allure filter are applied to all requests.
     * Thread-safety note: RequestSpecification from given() is not shared state;
     * each .given() call creates a new specification inheriting from this base.
     */
    private final RequestSpecification baseRequestSpec;

    /**
     * Constructor — reads config from ConfigReader and builds the base RequestSpecification.
     *
     * Design Decision: RestAssured.baseURI is set globally here so all subsequent
     * given() calls automatically prepend the base URL.
     */
    public BookingApiClient() {
        // Set global base URI from config — avoids any hardcoded URL anywhere
        RestAssured.baseURI = ConfigReader.getProperty("base.url");

        log.info("BookingApiClient initialized. Base URI: {}", RestAssured.baseURI);

        // Build shared base specification
        this.baseRequestSpec = given()
                .filter(new AllureRestAssured())   // Auto-log all requests/responses to Allure
                .header("Content-Type", ConfigReader.getProperty("content.type"))
                .header("Accept",       ConfigReader.getProperty("accept.type"))
                .log().all();                       // Log full request to console/file
    }

    // ── CREATE BOOKING ────────────────────────────────────────────────────────

    /**
     * Sends a POST /booking request with a valid BookingRequest POJO.
     * Serialized to JSON automatically by RestAssured + Jackson.
     *
     * @param bookingRequest the booking payload (deserialized from test data)
     * @return full Response object — tests perform assertions on this
     */
    @Step("POST /booking — Create Booking with valid payload: {bookingRequest}")
    public Response createBooking(BookingRequest bookingRequest) {
        log.info("Executing POST /booking with payload: {}", bookingRequest);

        return given()
                .spec(baseRequestSpec)
                .body(bookingRequest)
                .when()
                .post(BookingEndpoints.CREATE_BOOKING)
                .then()
                .log().all()
                .extract()
                .response();
    }

    /**
     * Sends a POST /booking request with a raw JSON string body.
     * Used for negative test cases where we deliberately send malformed/incomplete JSON.
     *
     * Design Decision: Separate method for raw body prevents the valid-flow method
     * from being polluted with negative-test conditional logic.
     *
     * @param rawJsonBody raw JSON string (can be empty, invalid, or missing fields)
     * @return full Response object
     */
    @Step("POST /booking — Create Booking with raw body (negative scenario): {rawJsonBody}")
    public Response createBookingWithRawBody(String rawJsonBody) {
        log.info("Executing POST /booking with raw body: {}", rawJsonBody);

        return given()
                .spec(baseRequestSpec)
                .body(rawJsonBody)
                .when()
                .post(BookingEndpoints.CREATE_BOOKING)
                .then()
                .log().all()
                .extract()
                .response();
    }

    /**
     * Sends a POST /booking request with a custom Content-Type header.
     * Used for negative testing — verifies the API rejects non-JSON content types.
     *
     * Design Decision: RestAssured can only serialize POJOs to content-types it knows
     * (e.g. application/json). For non-JSON content-types like text/plain, we pre-serialize
     * the POJO to a JSON String using Jackson, then pass that String as the raw body.
     * RestAssured can always write a String body regardless of Content-Type.
     *
     * @param bookingRequest the booking payload
     * @param contentType    the incorrect/custom content-type to test with
     * @return full Response object
     */
    @Step("POST /booking — Create Booking with custom Content-Type: {contentType}")
    public Response createBookingWithContentType(BookingRequest bookingRequest, String contentType) {
        log.info("Executing POST /booking with custom Content-Type: {}", contentType);

        // Pre-serialize POJO → JSON string so RestAssured doesn't attempt to infer
        // a serializer based on the non-JSON Content-Type header (which would fail).
        String serializedBody;
        try {
            serializedBody = new ObjectMapper().writeValueAsString(bookingRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("[BookingApiClient] Failed to serialize BookingRequest to JSON string: " + e.getMessage(), e);
        }

        return given()
                .filter(new AllureRestAssured())
                .header("Content-Type", contentType)
                .header("Accept",       ConfigReader.getProperty("accept.type"))
                .baseUri(ConfigReader.getProperty("base.url"))
                .body(serializedBody)           // Pass String, not POJO — safe for any Content-Type
                .log().all()
                .when()
                .post(BookingEndpoints.CREATE_BOOKING)
                .then()
                .log().all()
                .extract()
                .response();
    }

    // ── GET BOOKING ───────────────────────────────────────────────────────────

    /**
     * Sends a GET /booking/{bookingId} request to retrieve a specific booking.
     *
     * Design Decision: Path parameter binding is handled by RestAssured's
     * pathParam() — never via string concatenation (prevents injection issues
     * and improves readability).
     *
     * @param bookingId the booking ID to retrieve (integer)
     * @return full Response object
     */
    @Step("GET /booking/{bookingId} — Retrieve booking by ID: {bookingId}")
    public Response getBookingById(int bookingId) {
        log.info("Executing GET /booking/{}", bookingId);

        return given()
                .spec(baseRequestSpec)
                .pathParam("bookingId", bookingId)
                .when()
                .get(BookingEndpoints.GET_BOOKING_BY_ID)
                .then()
                .log().all()
                .extract()
                .response();
    }

}
