package com.booker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing the response from POST /booking.
 *
 * The API returns:
 * {
 *   "bookingid": 12345,
 *   "booking": { ...full booking object... }
 * }
 *
 * Design Decision: The bookingid field is the critical link between
 * CreateBooking (POST) and GetBooking (GET) in the E2E flow.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {

    @JsonProperty("bookingid")
    private int bookingid;

    @JsonProperty("booking")
    private BookingRequest booking;

    // ── Required by Jackson ───────────────────────────────────────────────────
    public BookingResponse() {}

    // ── Getters ───────────────────────────────────────────────────────────────
    public int            getBookingid() { return bookingid; }
    public BookingRequest getBooking()   { return booking; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setBookingid(int bookingid)        { this.bookingid = bookingid; }
    public void setBooking(BookingRequest booking) { this.booking   = booking; }

    @Override
    public String toString() {
        return "BookingResponse{bookingid=" + bookingid + ", booking=" + booking + "}";
    }
}
