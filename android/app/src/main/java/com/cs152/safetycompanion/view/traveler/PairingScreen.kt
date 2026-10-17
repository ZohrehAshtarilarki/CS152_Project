package com.cs152.safetycompanion.view.traveler

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.PairingStatus
import com.cs152.safetycompanion.controller.TravelerUiState
import com.cs152.safetycompanion.view.components.BigButton
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Traveler side): shows a big 6-digit code. The traveler only reads it aloud. */
@Composable
fun PairingScreen(state: TravelerUiState, onGetCode: () -> Unit) {
    // Get a code automatically, so the traveler doesn't have to tap anything.
    LaunchedEffect(Unit) {
        if (state.pairingStatus == PairingStatus.NOT_STARTED) onGetCode()
    }

    ScreenColumn {
        ScreenTitle(stringResource(R.string.pairing_title))

        when (state.pairingStatus) {
            PairingStatus.NOT_STARTED -> CircularProgressIndicator()

            PairingStatus.WAITING -> {
                BigText(stringResource(R.string.pairing_instructions))
                Text(
                    text = state.pairingCode.orEmpty().chunked(3).joinToString(" "),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.pairing_waiting), fontSize = 20.sp)
            }

            PairingStatus.EXPIRED, PairingStatus.ERROR -> {
                BigText(
                    stringResource(
                        if (state.pairingStatus == PairingStatus.EXPIRED) R.string.pairing_expired
                        else R.string.pairing_error
                    )
                )
                BigButton(text = stringResource(R.string.pairing_new_code), onClick = onGetCode)
            }
        }
    }
}
