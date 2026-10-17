package com.cs152.safetycompanion.view.family

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs152.safetycompanion.controller.FamilyController

/** Family side screens. Owner: Family side. */
private enum class FamilyScreen { TRIPS, EDIT_TRIP, PAIR }

/** VIEW (Family side): navigation between the family screens. */
@Composable
fun FamilyRoot(controller: FamilyController, onLeaveFamilyMode: () -> Unit) {
    val state by controller.uiState.collectAsStateWithLifecycle()

    if (!state.isLoggedIn) {
        FamilyLoginScreen(
            state = state,
            onLogin = controller::login,
            onRegister = controller::register,
            onBack = onLeaveFamilyMode,
        )
        return
    }

    var screen by rememberSaveable { mutableStateOf(FamilyScreen.TRIPS) }
    var selectedTripId by rememberSaveable { mutableStateOf<String?>(null) }
    val goToTrips = {
        controller.clearMessage()
        screen = FamilyScreen.TRIPS
    }
    BackHandler(enabled = screen != FamilyScreen.TRIPS) { goToTrips() }

    when (screen) {
        FamilyScreen.TRIPS -> TripListScreen(
            state = state,
            onNewTrip = {
                selectedTripId = null
                screen = FamilyScreen.EDIT_TRIP
            },
            onEditTrip = { tripId ->
                selectedTripId = tripId
                screen = FamilyScreen.EDIT_TRIP
            },
            onPairPhone = { tripId ->
                controller.clearMessage()
                selectedTripId = tripId
                screen = FamilyScreen.PAIR
            },
            onRefresh = controller::loadTrips,
            onLogout = controller::logout,
        )

        FamilyScreen.EDIT_TRIP -> TripEditorScreen(
            trip = state.trips.find { it.tripId == selectedTripId },
            onBack = goToTrips,
        )

        FamilyScreen.PAIR -> PairTravelerScreen(
            state = state,
            onPair = { code -> selectedTripId?.let { controller.pairTraveler(it, code) } },
            onBack = goToTrips,
        )
    }
}
