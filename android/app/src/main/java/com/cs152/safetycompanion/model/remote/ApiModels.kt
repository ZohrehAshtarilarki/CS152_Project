package com.cs152.safetycompanion.model.remote

import com.cs152.safetycompanion.model.Contact
import com.cs152.safetycompanion.model.Leg
import com.cs152.safetycompanion.model.StaffCard

/** JSON shapes sent to / received from the backend (see docs/api.md). */

data class RegisterRequest(val name: String, val email: String, val password: String)

data class LoginRequest(val email: String, val password: String)

data class AuthResponse(val token: String, val familyId: String, val name: String)

data class TripRequest(
    val travelerName: String,
    val targetLanguage: String = "tr",
    val legs: List<Leg>,
    val emergencyContacts: List<Contact>,
    val staffCards: List<StaffCard>,
)

data class PairRequest(val code: String)

data class PairingStartResponse(val pairingId: String, val code: String, val expiresAtUtc: Long)

/** status is WAITING, CONFIRMED or EXPIRED. */
data class PairingStatusResponse(val status: String, val deviceToken: String?, val tripId: String?)
