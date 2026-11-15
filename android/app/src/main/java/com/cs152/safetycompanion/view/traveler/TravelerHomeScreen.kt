package com.cs152.safetycompanion.view.traveler

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
    onLeave: (String) -> Boolean,
) {
    var showPinDialog by remember { mutableStateOf(false) }

    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showPinDialog = true }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Leave Traveler Mode"
                )
            }
        }

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

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Leave Traveler Mode") },
            text = {
                ScreenColumn {
                    Text("Enter Family PIN to exit.")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { 
                            pinInput = it
                            isError = false
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = isError,
                        supportingText = if (isError) { { Text("Incorrect PIN") } } else null
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val success = onLeave(pinInput)
                    if (success) {
                        showPinDialog = false
                    } else {
                        isError = true
                    }
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
