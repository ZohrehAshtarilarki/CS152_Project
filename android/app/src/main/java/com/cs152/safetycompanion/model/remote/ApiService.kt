package com.cs152.safetycompanion.model.remote

import com.cs152.safetycompanion.model.TravelPackage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Every backend endpoint the app uses. Keep in sync with docs/api.md. */
interface ApiService {

    // ---- Family side ----
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/trips")
    suspend fun getTrips(@Header("Authorization") auth: String): List<TravelPackage>

    @POST("api/trips")
    suspend fun createTrip(@Header("Authorization") auth: String, @Body body: TripRequest): TravelPackage

    @PUT("api/trips/{tripId}")
    suspend fun updateTrip(
        @Header("Authorization") auth: String,
        @Path("tripId") tripId: String,
        @Body body: TripRequest,
    ): TravelPackage

    @POST("api/trips/{tripId}/pair")
    suspend fun pairTraveler(
        @Header("Authorization") auth: String,
        @Path("tripId") tripId: String,
        @Body body: PairRequest,
    ): PairingStatusResponse

    // ---- Traveler side ----
    @POST("api/pairing/start")
    suspend fun startPairing(): PairingStartResponse

    @GET("api/pairing/{pairingId}")
    suspend fun pairingStatus(@Path("pairingId") pairingId: String): PairingStatusResponse

    @GET("api/traveler/package")
    suspend fun getTravelPackage(@Header("Authorization") auth: String): TravelPackage
}
