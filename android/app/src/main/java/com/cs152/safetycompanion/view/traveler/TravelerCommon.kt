package com.cs152.safetycompanion.view.traveler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.model.Leg
import com.cs152.safetycompanion.model.StaffCard
import com.cs152.safetycompanion.view.components.BigOutlinedButton
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Small VIEW pieces used by several traveler screens. */

@Composable
fun BigText(text: String) {
    Text(text = text, fontSize = 24.sp)
}

@Composable
fun TravelerBackButton(onBack: () -> Unit) {
    BigOutlinedButton(text = stringResource(R.string.btn_back), onClick = onBack)
}

/** "Son bilinen bilgi: 16 Eki 14:30 (İnternet yok)" */
@Composable
fun LastKnownInfo(lastSyncedUtc: Long?, isOffline: Boolean) {
    if (lastSyncedUtc == null) return
    val text = stringResource(R.string.last_known, formatLocalTime(lastSyncedUtc))
    val suffix = if (isOffline) " " + stringResource(R.string.offline_suffix) else ""
    Text(
        text = text + suffix,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun LegDetails(leg: Leg) {
    Text(
        text = stringResource(R.string.flight_line, leg.flightNumber, leg.originAirport, leg.destinationAirport),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = leg.gate?.let { stringResource(R.string.gate_line, it) } ?: stringResource(R.string.gate_unknown),
        fontSize = 28.sp,
    )
}

/** Big English message for the staff, Turkish below for the traveler. */
@Composable
fun StaffCardView(card: StaffCard) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(card.titleTr, style = MaterialTheme.typography.labelLarge)
            Text(card.messageEn, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(card.messageTr, fontSize = 20.sp)
        }
    }
}

fun formatLocalTime(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("d MMM HH:mm", Locale.forLanguageTag("tr"))
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
