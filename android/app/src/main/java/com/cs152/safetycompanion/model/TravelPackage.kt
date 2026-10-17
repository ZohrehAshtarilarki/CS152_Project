package com.cs152.safetycompanion.model

/**
 * MODEL: the whole trip in one object. Same shape as the backend's TravelPackageView.
 * This is the shared contract between both teammates (see docs/api.md).
 * All times are UTC epoch milliseconds.
 */
data class TravelPackage(
    val tripId: String,
    val travelerName: String,
    val targetLanguage: String,
    val lastUpdatedUtc: Long,
    val legs: List<Leg>,
    val emergencyContacts: List<Contact>,
    val staffCards: List<StaffCard>,
)

data class Leg(
    val sequenceOrder: Int,
    val flightNumber: String,
    val originAirport: String,
    val destinationAirport: String,
    val departureTimeUtc: Long,
    val arrivalTimeUtc: Long,
    val terminal: String?,
    val gate: String?,
    val status: String = "SCHEDULED",
)

data class Contact(
    val name: String,
    val relation: String,
    val phoneNumber: String,
)

/** Messages may contain {GATE}, {FLIGHT} and {PHONE}. See StaffCardText.kt. */
data class StaffCard(
    val id: String?,
    val category: String,
    val titleEn: String,
    val titleTr: String,
    val messageEn: String,
    val messageTr: String,
)
