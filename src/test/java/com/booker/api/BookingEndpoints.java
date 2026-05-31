package com.booker.api;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * LAYER 2 — API Object Model: Endpoint Definitions
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * Responsibility: Centralizes ALL endpoint path constants for the Restful Booker API.
 *
 * Design Rationale:
 * - Separating endpoint paths from HTTP call logic (Layer 1) ensures that
 *   if any API path changes, only this file needs updating — not every test.
 * - This layer has NO RestAssured imports, NO HTTP logic, NO assertions.
 *   It is purely a configuration/definition layer.
 * - Path parameters are expressed using RestAssured's {placeholder} notation.
 *
 * Usage: BookingApiClient (Layer 1) references these constants for all HTTP calls.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
public final class BookingEndpoints {

    // ── Private constructor: utility class — must not be instantiated ─────────
    private BookingEndpoints() {
        throw new UnsupportedOperationException("BookingEndpoints is a utility class and cannot be instantiated.");
    }

    // ── Booking Resource Endpoints ────────────────────────────────────────────

    /**
     * POST /booking — Creates a new booking.
     * Accepts JSON body. Returns bookingid + full booking object.
     */
    public static final String CREATE_BOOKING = "/booking";

    /**
     * GET /booking/{bookingId} — Retrieves a single booking by its ID.
     * Path parameter {bookingId} is resolved at runtime by RestAssured.
     */
    public static final String GET_BOOKING_BY_ID = "/booking/{bookingId}";

    /**
     * GET /booking — Retrieves a list of all booking IDs.
     * Optional query params: firstname, lastname, checkin, checkout.
     */
    public static final String GET_ALL_BOOKINGS = "/booking";

    /**
     * PUT /booking/{bookingId} — Fully updates an existing booking.
     * Requires authentication (Basic Auth or Token).
     */
    public static final String UPDATE_BOOKING = "/booking/{bookingId}";

    /**
     * DELETE /booking/{bookingId} — Deletes a booking by ID.
     * Requires authentication (Basic Auth or Token).
     */
    public static final String DELETE_BOOKING = "/booking/{bookingId}";

    /**
     * POST /auth — Generates an authentication token.
     * Required for PUT/PATCH/DELETE operations.
     */
    public static final String AUTH_TOKEN = "/auth";

}
