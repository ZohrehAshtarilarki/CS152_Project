package com.cs152.safetycompanion.view.family

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.FamilyUiState
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Family side): type the code the traveler reads aloud on the phone call. */
@Composable
fun PairTravelerScreen(
    state: FamilyUiState,
    onPair: (code: String) -> Unit,
    onBack: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }

    ScreenColumn {
        ScreenTitle(stringResource(R.string.family_pair_title))
        Text(stringResource(R.string.family_pair_instructions))
        OutlinedTextField(
            value = code,
            onValueChange = { input -> code = input.filter { it.isDigit() }.take(6) },
            label = { Text(stringResource(R.string.family_pair_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        state.message?.let { Text(it) }
        Button(
            onClick = { onPair(code) },
            enabled = code.length == 6 && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.family_pair_button))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.family_back))
        }
    }
}
