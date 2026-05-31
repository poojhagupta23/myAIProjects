package com.booker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing the full booking request payload.
 * Maps directly to the POST /booking request body.
 * Uses Builder pattern for enterprise-grade, readable object construction.
 *
 * Design Decision: @JsonIgnoreProperties(ignoreUnknown = true) is used so that
 * if the API adds new fields in future, our deserialization never breaks — forward compatibility.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingRequest {

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("totalprice")
    private int totalprice;

    @JsonProperty("depositpaid")
    private boolean depositpaid;

    @JsonProperty("bookingdates")
    private BookingDates bookingdates;

    @JsonProperty("additionalneeds")
    private String additionalneeds;

    // ── Required by Jackson ───────────────────────────────────────────────────
    public BookingRequest() {}

    // ── Private constructor via Builder ───────────────────────────────────────
    private BookingRequest(Builder builder) {
        this.firstname       = builder.firstname;
        this.lastname        = builder.lastname;
        this.totalprice      = builder.totalprice;
        this.depositpaid     = builder.depositpaid;
        this.bookingdates    = builder.bookingdates;
        this.additionalneeds = builder.additionalneeds;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String       getFirstname()       { return firstname; }
    public String       getLastname()        { return lastname; }
    public int          getTotalprice()      { return totalprice; }
    public boolean      isDepositpaid()      { return depositpaid; }
    public BookingDates getBookingdates()    { return bookingdates; }
    public String       getAdditionalneeds() { return additionalneeds; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setFirstname(String firstname)             { this.firstname       = firstname; }
    public void setLastname(String lastname)               { this.lastname        = lastname; }
    public void setTotalprice(int totalprice)              { this.totalprice      = totalprice; }
    public void setDepositpaid(boolean depositpaid)        { this.depositpaid     = depositpaid; }
    public void setBookingdates(BookingDates bookingdates) { this.bookingdates    = bookingdates; }
    public void setAdditionalneeds(String additionalneeds) { this.additionalneeds = additionalneeds; }

    // ── Builder ───────────────────────────────────────────────────────────────
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String       firstname;
        private String       lastname;
        private int          totalprice;
        private boolean      depositpaid;
        private BookingDates bookingdates;
        private String       additionalneeds;

        public Builder firstname(String firstname)             { this.firstname       = firstname;       return this; }
        public Builder lastname(String lastname)               { this.lastname        = lastname;        return this; }
        public Builder totalprice(int totalprice)              { this.totalprice      = totalprice;      return this; }
        public Builder depositpaid(boolean depositpaid)        { this.depositpaid     = depositpaid;     return this; }
        public Builder bookingdates(BookingDates bookingdates) { this.bookingdates    = bookingdates;    return this; }
        public Builder additionalneeds(String additionalneeds) { this.additionalneeds = additionalneeds; return this; }

        public BookingRequest build() {
            return new BookingRequest(this);
        }
    }

    @Override
    public String toString() {
        return "BookingRequest{"
            + "firstname='" + firstname + "'"
            + ", lastname='" + lastname + "'"
            + ", totalprice=" + totalprice
            + ", depositpaid=" + depositpaid
            + ", bookingdates=" + bookingdates
            + ", additionalneeds='" + additionalneeds + "'"
            + "}";
    }
}
