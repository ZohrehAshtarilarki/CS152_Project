package com.cs152.safetycompanion.view.traveler

import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.TravelerUiState
import com.cs152.safetycompanion.view.components.BigButton
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Traveler side): the 4 big buttons. Everything here works offline. */
@Composable
fun TravelerHomeScreen(
    state: TravelerUiState,
    onWhatNow: () -> Unit,
    onReadSign: () -> Unit,
    onTalkToStaff: () -> Unit,
    onNeedHelp: () -> Unit,
    onUpdate: () -> Unit,
) {
    ScreenColumn {
        ScreenTitle(stringResource(R.string.traveler_title))
        BigButton(text = stringResource(R.string.btn_what_now), onClick = onWhatNow)
        BigButton(text = stringResource(R.string.btn_read_sign), onClick = onReadSign)
        BigButton(text = stringResource(R.string.btn_talk_staff), onClick = onTalkToStaff)
        BigButton(text = stringResource(R.string.btn_need_help), onClick = onNeedHelp)
        LastKnownInfo(lastSyncedUtc = state.lastSyncedUtc, isOffline = state.isOffline)
        TextButton(onClick = onUpdate) {
            Text(stringResource(R.string.btn_update), fontSize = 18.sp)
        }
    }
}
