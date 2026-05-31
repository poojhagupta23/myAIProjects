package com.booker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing the booking dates sub-object.
 * Uses Jackson annotations for JSON serialization/deserialization.
 * Implements Builder pattern for fluent, readable construction.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDates {

    @JsonProperty("checkin")
    private String checkin;

    @JsonProperty("checkout")
    private String checkout;

    // ── Required by Jackson for deserialization ───────────────────────────────
    public BookingDates() {}

    // ── Private constructor for Builder pattern ───────────────────────────────
    private BookingDates(Builder builder) {
        this.checkin  = builder.checkin;
        this.checkout = builder.checkout;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getCheckin()  { return checkin; }
    public String getCheckout() { return checkout; }

    // ── Setters (required for Jackson deserialization) ────────────────────────
    public void setCheckin(String checkin)   { this.checkin  = checkin; }
    public void setCheckout(String checkout) { this.checkout = checkout; }

    // ── Builder ───────────────────────────────────────────────────────────────
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String checkin;
        private String checkout;

        public Builder checkin(String checkin)   { this.checkin  = checkin;  return this; }
        public Builder checkout(String checkout) { this.checkout = checkout; return this; }

        public BookingDates build() {
            return new BookingDates(this);
        }
    }

    @Override
    public String toString() {
        return "BookingDates{checkin='" + checkin + "', checkout='" + checkout + "'}";
    }
}
