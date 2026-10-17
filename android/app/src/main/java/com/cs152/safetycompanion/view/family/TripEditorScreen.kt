package com.cs152.safetycompanion.view.family

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/**
 * VIEW (Family side): create or edit a trip.
 *
 * TODO (Family side):
 *  - Form fields: traveler name; flights (number, from, to, departure, arrival, terminal, gate);
 *    emergency contacts; staff cards (English + Turkish).
 *  - On "Save", build a TripRequest and call FamilyController.saveTrip(trip?.tripId, request) { onBack() }.
 *  - Convert the date/time the family picks into UTC epoch milliseconds.
 */
@Composable
fun TripEditorScreen(trip: TravelPackage?, onBack: () -> Unit) {
    ScreenColumn {
        ScreenTitle(trip?.travelerName ?: stringResource(R.string.family_new_trip))
        Text(stringResource(R.string.family_editor_todo))
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.family_back)) }
    }
}
