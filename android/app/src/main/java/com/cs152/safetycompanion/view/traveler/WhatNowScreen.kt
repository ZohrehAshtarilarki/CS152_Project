package com.cs152.safetycompanion.view.traveler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.TravelerUiState
import com.cs152.safetycompanion.model.logic.JourneyStage
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Traveler side): "What should I do now?" based on the saved trip and the clock. */
@Composable
fun WhatNowScreen(state: TravelerUiState, onRefreshStage: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onRefreshStage() }

    ScreenColumn {
        ScreenTitle(stringResource(R.string.btn_what_now))

        when (val stage = state.stage) {
            JourneyStage.NoTrip -> BigText(stringResource(R.string.what_now_no_trip))

            is JourneyStage.Upcoming -> {
                BigText(stringResource(R.string.what_now_upcoming))
                LegDetails(stage.leg)
            }

            is JourneyStage.Active -> {
                BigText(stringResource(R.string.what_now_current))
                LegDetails(stage.currentLeg)
                stage.nextLeg?.let { next ->
                    BigText(stringResource(R.string.what_now_next))
                    LegDetails(next)
                }
            }

            is JourneyStage.Completed -> BigText(stringResource(R.string.what_now_done))
        }

        LastKnownInfo(lastSyncedUtc = state.lastSyncedUtc, isOffline = state.isOffline)
        TravelerBackButton(onBack)
    }
}
