package com.cs152.safetycompanion.view.traveler

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.TravelerUiState
import com.cs152.safetycompanion.model.logic.currentGateAndFlight
import com.cs152.safetycompanion.model.logic.fill
import com.cs152.safetycompanion.view.components.BigButton
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Traveler side): help card for staff + emergency contacts with a Call button. */
@Composable
fun HelpScreen(state: TravelerUiState, onBack: () -> Unit) {
    val context = LocalContext.current
    val (gate, flight) = state.stage.currentGateAndFlight()
    val contacts = state.trip?.emergencyContacts.orEmpty()
    val phone = contacts.firstOrNull()?.phoneNumber.orEmpty()

    ScreenColumn {
        ScreenTitle(stringResource(R.string.btn_need_help))

        state.trip?.staffCards.orEmpty()
            .filter { it.category == "HELP" }
            .forEach { card -> StaffCardView(card.fill(gate = gate, flight = flight, phone = phone)) }

        Text(stringResource(R.string.help_contacts), style = MaterialTheme.typography.titleLarge)
        contacts.forEach { contact ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(contact.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(contact.relation, fontSize = 18.sp)
                    Text(contact.phoneNumber, fontSize = 22.sp)
                    BigButton(
                        text = stringResource(R.string.help_call),
                        onClick = {
                            // Opens the phone app with the number filled in (no permission needed).
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}")))
                        },
                    )
                }
            }
        }

        TravelerBackButton(onBack)
    }
}
