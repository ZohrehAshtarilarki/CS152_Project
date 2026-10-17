package com.cs152.safetycompanion.model.repository

import com.cs152.safetycompanion.model.local.SessionStore
import com.cs152.safetycompanion.model.remote.ApiService
import com.cs152.safetycompanion.model.remote.PairingStartResponse
import com.cs152.safetycompanion.model.remote.PairingStatusResponse

/** MODEL (Traveler side): get a pairing code and wait for the family to type it. */
class PairingRepository(
    private val api: ApiService,
    private val session: SessionStore,
) {
    val isPaired: Boolean get() = session.deviceToken != null

    suspend fun start(): Result<PairingStartResponse> = runCatching {
        api.startPairing().also { session.pairingId = it.pairingId }
    }

    /** When CONFIRMED, the device token is saved and the phone is paired. */
    suspend fun checkStatus(): Result<PairingStatusResponse> = runCatching {
        val pairingId = session.pairingId ?: error("Pairing was not started")
        api.pairingStatus(pairingId).also { status ->
            if (status.status == "CONFIRMED" && status.deviceToken != null) {
                session.deviceToken = status.deviceToken
                session.pairingId = null
            }
        }
    }
}
