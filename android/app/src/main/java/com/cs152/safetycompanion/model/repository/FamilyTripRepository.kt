package com.cs152.safetycompanion.model.repository

import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.model.local.SessionStore
import com.cs152.safetycompanion.model.remote.ApiService
import com.cs152.safetycompanion.model.remote.PairRequest
import com.cs152.safetycompanion.model.remote.PairingStatusResponse
import com.cs152.safetycompanion.model.remote.TripRequest

/** MODEL (Family side): the family's trips on the server, and pairing a traveler phone. */
class FamilyTripRepository(
    private val api: ApiService,
    private val session: SessionStore,
) {
    private val auth: String get() = "Bearer ${session.familyToken.orEmpty()}"

    suspend fun getTrips(): Result<List<TravelPackage>> = runCatching { api.getTrips(auth) }

    suspend fun createTrip(request: TripRequest): Result<TravelPackage> =
        runCatching { api.createTrip(auth, request) }

    suspend fun updateTrip(tripId: String, request: TripRequest): Result<TravelPackage> =
        runCatching { api.updateTrip(auth, tripId, request) }

    /** The family types the 6-digit code the traveler read aloud. */
    suspend fun pairTraveler(tripId: String, code: String): Result<PairingStatusResponse> =
        runCatching { api.pairTraveler(auth, tripId, PairRequest(code.filter { it.isDigit() })) }
}
