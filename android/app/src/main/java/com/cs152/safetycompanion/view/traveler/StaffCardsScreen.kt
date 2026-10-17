package com.cs152.safetycompanion.view.traveler

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.TravelerUiState
import com.cs152.safetycompanion.model.logic.currentGateAndFlight
import com.cs152.safetycompanion.model.logic.fill
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Traveler side): ready-made cards to show airport staff, with gate and flight filled in. */
@Composable
fun StaffCardsScreen(state: TravelerUiState, onBack: () -> Unit) {
    val (gate, flight) = state.stage.currentGateAndFlight()
    val phone = state.trip?.emergencyContacts?.firstOrNull()?.phoneNumber.orEmpty()

    ScreenColumn {
        ScreenTitle(stringResource(R.string.btn_talk_staff))
        BigText(stringResource(R.string.staff_show_hint))
        state.trip?.staffCards.orEmpty().forEach { card ->
            StaffCardView(card.fill(gate = gate, flight = flight, phone = phone))
        }
        TravelerBackButton(onBack)
    }
}
