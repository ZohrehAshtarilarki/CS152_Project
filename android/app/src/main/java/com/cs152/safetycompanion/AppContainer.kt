package com.cs152.safetycompanion

import android.content.Context
import com.cs152.safetycompanion.model.local.AppDatabase
import com.cs152.safetycompanion.model.local.SessionStore
import com.cs152.safetycompanion.model.remote.ApiClient
import com.cs152.safetycompanion.model.repository.AuthRepository
import com.cs152.safetycompanion.model.repository.FamilyTripRepository
import com.cs152.safetycompanion.model.repository.PairingRepository
import com.cs152.safetycompanion.model.repository.TravelPackageRepository

/** Creates the MODEL objects once, so every CONTROLLER uses the same ones. */
class AppContainer(context: Context) {

    val sessionStore = SessionStore(context)
    private val api = ApiClient.create()
    private val database = AppDatabase.getInstance(context)

    // Family side
    val authRepository = AuthRepository(api, sessionStore)
    val familyTripRepository = FamilyTripRepository(api, sessionStore)

    // Traveler side
    val pairingRepository = PairingRepository(api, sessionStore)
    val travelPackageRepository = TravelPackageRepository(api, database.tripDocumentDao(), sessionStore)
}
