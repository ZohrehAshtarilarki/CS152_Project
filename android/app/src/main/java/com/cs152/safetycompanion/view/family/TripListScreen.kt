package com.cs152.safetycompanion.view.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.FamilyUiState
import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Family side): the family's trips. */
@Composable
fun TripListScreen(
    state: FamilyUiState,
    onNewTrip: () -> Unit,
    onEditTrip: (tripId: String) -> Unit,
    onPairPhone: (tripId: String) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
) {
    ScreenColumn {
        ScreenTitle(stringResource(R.string.family_hello, state.familyName.orEmpty()))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewTrip) { Text(stringResource(R.string.family_new_trip)) }
            OutlinedButton(onClick = onRefresh) { Text(stringResource(R.string.family_refresh)) }
        }

        if (state.isLoading) CircularProgressIndicator()
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.trips.isEmpty() && !state.isLoading) Text(stringResource(R.string.family_no_trips))

        state.trips.forEach { trip ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(trip.travelerName, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.family_trip_route, routeOf(trip)))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onEditTrip(trip.tripId) }) {
                            Text(stringResource(R.string.family_edit_trip))
                        }
                        Button(onClick = { onPairPhone(trip.tripId) }) {
                            Text(stringResource(R.string.family_pair_phone))
                        }
                    }
                }
            }
        }

        TextButton(onClick = onLogout) { Text(stringResource(R.string.family_logout)) }
    }
}

/** "SFO → IST → ESB" */
private fun routeOf(trip: TravelPackage): String {
    val legs = trip.legs.sortedBy { it.sequenceOrder }
    if (legs.isEmpty()) return "—"
    return (listOf(legs.first().originAirport) + legs.map { it.destinationAirport }).joinToString(" → ")
}
