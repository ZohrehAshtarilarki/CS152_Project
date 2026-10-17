package com.cs152.safetycompanion.view;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * VIEW: JSON shapes for trips.
 *
 * TravelPackageView is THE shared contract between backend and Android:
 * the family app shows it, and the traveler phone downloads and saves it.
 * Change it only after both teammates agree (see docs/api.md).
 */
public final class TripViews {

    private TripViews() {
    }

    /** Times are UTC epoch milliseconds. */
    public record LegView(
            @Min(1) int sequenceOrder,
            @NotBlank @Size(max = 10) String flightNumber,
            @NotBlank @Size(min = 3, max = 3) String originAirport,
            @NotBlank @Size(min = 3, max = 3) String destinationAirport,
            long departureTimeUtc,
            long arrivalTimeUtc,
            @Size(max = 10) String terminal,
            @Size(max = 10) String gate,
            String status) {
    }

    public record ContactView(
            @NotBlank String name,
            @NotBlank String relation,
            @NotBlank String phoneNumber) {
    }

    /** Messages may contain {GATE}, {FLIGHT} and {PHONE}; the phone fills them in. */
    public record StaffCardView(
            String id,
            @NotBlank String category,
            @NotBlank String titleEn,
            @NotBlank String titleTr,
            @NotBlank String messageEn,
            @NotBlank String messageTr) {
    }

    /** What the family sends to create or replace a trip. */
    public record TripRequest(
            @NotBlank @Size(max = 100) String travelerName,
            String targetLanguage,
            @NotNull List<@Valid LegView> legs,
            List<@Valid ContactView> emergencyContacts,
            List<@Valid StaffCardView> staffCards) {
    }

    /** The whole trip in one JSON document (what the traveler phone saves for offline use). */
    public record TravelPackageView(
            String tripId,
            String travelerName,
            String targetLanguage,
            long lastUpdatedUtc,
            List<LegView> legs,
            List<ContactView> emergencyContacts,
            List<StaffCardView> staffCards) {
    }
}
