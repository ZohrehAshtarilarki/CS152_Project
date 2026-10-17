package com.cs152.safetycompanion.model.repository

import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.model.local.SessionStore
import com.cs152.safetycompanion.model.local.TripDocument
import com.cs152.safetycompanion.model.local.TripDocumentDao
import com.cs152.safetycompanion.model.remote.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

/** The trip saved on the phone + when it was downloaded. */
data class SavedTrip(val travelPackage: TravelPackage, val lastSyncedUtc: Long)

sealed interface SyncResult {
    data class Success(val lastUpdatedUtc: Long) : SyncResult
    data object NoInternet : SyncResult
    data object NotPaired : SyncResult
    data class ServerError(val code: Int) : SyncResult
}

/**
 * MODEL (Traveler side): the offline-first heart of the app.
 *
 * - sync(): download the trip from the server and save it in Room (needs internet).
 * - savedTrip: the trip read from Room (works with NO internet).
 *
 * Rule: traveler screens read ONLY savedTrip, never the server directly.
 */
class TravelPackageRepository(
    private val api: ApiService,
    private val dao: TripDocumentDao,
    private val session: SessionStore,
) {
    private val gson = Gson()

    val savedTrip: Flow<SavedTrip?> = dao.observe()
        .map { document ->
            document?.let { SavedTrip(gson.fromJson(it.rawJson, TravelPackage::class.java), it.lastSyncedUtc) }
        }
        .flowOn(Dispatchers.Default)

    suspend fun sync(): SyncResult {
        val token = session.deviceToken ?: return SyncResult.NotPaired
        return try {
            val travelPackage = api.getTravelPackage("Bearer $token")
            dao.save(
                TripDocument(
                    tripId = travelPackage.tripId,
                    rawJson = gson.toJson(travelPackage),
                    lastSyncedUtc = System.currentTimeMillis(),
                )
            )
            SyncResult.Success(travelPackage.lastUpdatedUtc)
        } catch (e: IOException) {
            SyncResult.NoInternet
        } catch (e: HttpException) {
            SyncResult.ServerError(e.code())
        }
    }
}
