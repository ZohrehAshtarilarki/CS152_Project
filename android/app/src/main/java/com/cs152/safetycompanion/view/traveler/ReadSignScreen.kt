package com.cs152.safetycompanion.view.traveler

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.TravelerUiState
import com.cs152.safetycompanion.model.logic.SignAdviceType
import com.cs152.safetycompanion.model.logic.SignGuidance
import com.cs152.safetycompanion.view.components.BigButton
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/**
 * VIEW (Traveler side): take ONE photo of a sign (no live video).
 * Flow: photo -> TravelerController.readSign -> SignReader (ML Kit) -> SignRules -> advice here.
 */
@Composable
fun ReadSignScreen(
    state: TravelerUiState,
    onPhoto: (Bitmap) -> Unit,
    onShowStaffCard: () -> Unit,
    onBack: () -> Unit,
) {
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onPhoto(bitmap)
    }

    ScreenColumn {
        ScreenTitle(stringResource(R.string.btn_read_sign))
        BigButton(text = stringResource(R.string.read_sign_take_photo), onClick = { takePhoto.launch(null) })

        val guidance = state.signGuidance
        when {
            state.isReadingSign -> CircularProgressIndicator()
            guidance != null -> {
                BigText(getLocalizedMessage(guidance))
                if (guidance.showStaffCard) {
                    BigButton(text = stringResource(R.string.read_sign_show_staff), onClick = onShowStaffCard)
                }
            }
            else -> Text(stringResource(R.string.read_sign_hint), fontSize = 20.sp)
        }

        TravelerBackButton(onBack)
    }
}

@Composable
private fun getLocalizedMessage(guidance: SignGuidance): String {
    val defaultGateStr = stringResource(R.string.gate_unknown)
    return when (guidance.type) {
        SignAdviceType.BAGGAGE_WARNING -> stringResource(R.string.sign_advice_baggage_warning)
        SignAdviceType.TRANSFER -> stringResource(R.string.sign_advice_transfer, guidance.gate ?: defaultGateStr)
        SignAdviceType.GATE -> stringResource(R.string.sign_advice_gate, guidance.gate ?: defaultGateStr)
        SignAdviceType.BAGGAGE -> stringResource(R.string.sign_advice_baggage)
        SignAdviceType.UNKNOWN -> stringResource(R.string.sign_advice_unknown)
    }
}
