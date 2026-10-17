package com.cs152.safetycompanion.view.traveler

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs152.safetycompanion.controller.TravelerController

/** Traveler side screens. Owner: Traveler side. Flat: home + 4 screens, no nested routes. */
private enum class TravelerScreen { HOME, WHAT_NOW, READ_SIGN, TALK_TO_STAFF, I_NEED_HELP }

/** VIEW (Traveler side): navigation between the traveler screens. */
@Composable
fun TravelerRoot(controller: TravelerController) {
    val state by controller.uiState.collectAsStateWithLifecycle()

    if (!state.isPaired) {
        PairingScreen(state = state, onGetCode = controller::startPairing)
        return
    }

    var screen by rememberSaveable { mutableStateOf(TravelerScreen.HOME) }
    val goHome = { screen = TravelerScreen.HOME }
    BackHandler(enabled = screen != TravelerScreen.HOME) { goHome() }

    when (screen) {
        TravelerScreen.HOME -> TravelerHomeScreen(
            state = state,
            onWhatNow = { screen = TravelerScreen.WHAT_NOW },
            onReadSign = { screen = TravelerScreen.READ_SIGN },
            onTalkToStaff = { screen = TravelerScreen.TALK_TO_STAFF },
            onNeedHelp = { screen = TravelerScreen.I_NEED_HELP },
            onUpdate = controller::sync,
        )

        TravelerScreen.WHAT_NOW -> WhatNowScreen(
            state = state,
            onRefreshStage = controller::refreshStage,
            onBack = goHome,
        )

        TravelerScreen.READ_SIGN -> ReadSignScreen(
            state = state,
            onPhoto = controller::readSign,
            onShowStaffCard = { screen = TravelerScreen.TALK_TO_STAFF },
            onBack = {
                controller.clearSign()
                goHome()
            },
        )

        TravelerScreen.TALK_TO_STAFF -> StaffCardsScreen(state = state, onBack = goHome)

        TravelerScreen.I_NEED_HELP -> HelpScreen(state = state, onBack = goHome)
    }
}
