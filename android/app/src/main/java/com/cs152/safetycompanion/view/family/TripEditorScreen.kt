package com.cs152.safetycompanion.view.family

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.FamilyUiState
import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.model.remote.TripRequest
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle
import java.util.Locale

/**
 * VIEW (Family side): create a new trip (trip == null) or edit one.
 *
 * The form data lives in a TripForm (see TripForm.kt). On "Save trip" the form is checked;
 * if it is fine, onSave gets a TripRequest (times already in UTC millis) and FamilyRoot
 * calls FamilyController.saveTrip(...).
 */
@Composable
fun TripEditorScreen(
    trip: TravelPackage?,
    state: FamilyUiState,
    onSave: (TripRequest) -> Unit,
    onBack: () -> Unit,
) {
    var form by remember(trip?.tripId) { mutableStateOf(TripForm.from(trip)) }
    var problems by remember(trip?.tripId) { mutableStateOf(emptyList<FormProblem>()) }

    ScreenColumn {
        ScreenTitle(
            if (trip == null) stringResource(R.string.family_new_trip)
            else stringResource(R.string.family_edit_trip_title, trip.travelerName)
        )

        FormField(
            value = form.travelerName,
            label = R.string.family_editor_traveler_name,
            onValueChange = { form = form.copy(travelerName = it) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )

        // ---------- Flights ----------
        SectionTitle(R.string.family_editor_flights)
        Text(stringResource(R.string.family_editor_time_hint))
        form.legs.forEachIndexed { index, leg ->
            LegCard(
                number = index + 1,
                leg = leg,
                canRemove = form.legs.size > 1,
                onChange = { form = form.copy(legs = form.legs.replaceAt(index, it)) },
                onRemove = { form = form.copy(legs = form.legs.withoutAt(index)) },
            )
        }
        OutlinedButton(onClick = {
            // A new flight usually starts where the last one ended.
            val start = form.legs.lastOrNull()?.destinationAirport.orEmpty()
            form = form.copy(legs = form.legs + LegForm(originAirport = start))
        }) {
            Text(stringResource(R.string.family_editor_add_flight))
        }

        // ---------- Emergency contacts ----------
        SectionTitle(R.string.family_editor_contacts)
        form.contacts.forEachIndexed { index, contact ->
            ContactCard(
                number = index + 1,
                contact = contact,
                onChange = { form = form.copy(contacts = form.contacts.replaceAt(index, it)) },
                onRemove = { form = form.copy(contacts = form.contacts.withoutAt(index)) },
            )
        }
        OutlinedButton(onClick = { form = form.copy(contacts = form.contacts + ContactForm()) }) {
            Text(stringResource(R.string.family_editor_add_contact))
        }

        // ---------- Staff cards ----------
        SectionTitle(R.string.family_editor_cards)
        Text(stringResource(R.string.family_editor_cards_hint))
        form.staffCards.forEachIndexed { index, card ->
            StaffCardEditor(
                number = index + 1,
                card = card,
                onChange = { form = form.copy(staffCards = form.staffCards.replaceAt(index, it)) },
                onRemove = { form = form.copy(staffCards = form.staffCards.withoutAt(index)) },
            )
        }
        OutlinedButton(onClick = { form = form.copy(staffCards = form.staffCards + StaffCardForm()) }) {
            Text(stringResource(R.string.family_editor_add_card))
        }

        // ---------- Problems, save, back ----------
        if (problems.isNotEmpty()) {
            Text(
                stringResource(R.string.family_editor_fix),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
            problems.forEach { problem ->
                Text("• " + problemText(problem), color = MaterialTheme.colorScheme.error)
            }
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.isLoading) CircularProgressIndicator()

        Button(
            onClick = {
                when (val result = form.toRequest()) {
                    is FormResult.Ok -> {
                        problems = emptyList()
                        onSave(result.request)
                    }
                    is FormResult.Invalid -> {
                        problems = result.problems
                    }
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.family_editor_save))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.family_back))
        }
    }
}

// ---------------- Parts of the form ----------------

@Composable
private fun LegCard(
    number: Int,
    leg: LegForm,
    canRemove: Boolean,
    onChange: (LegForm) -> Unit,
    onRemove: () -> Unit,
) {
    val upperCase = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
    ItemCard(title = stringResource(R.string.family_editor_flight_n, number)) {
        FormField(
            value = leg.flightNumber,
            label = R.string.family_editor_flight_number,
            onValueChange = { onChange(leg.copy(flightNumber = it.take(10))) },
            keyboardOptions = upperCase,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormField(
                value = leg.originAirport,
                label = R.string.family_editor_from,
                onValueChange = { onChange(leg.copy(originAirport = it.take(3))) },
                modifier = Modifier.weight(1f),
                keyboardOptions = upperCase,
            )
            FormField(
                value = leg.destinationAirport,
                label = R.string.family_editor_to,
                onValueChange = { onChange(leg.copy(destinationAirport = it.take(3))) },
                modifier = Modifier.weight(1f),
                keyboardOptions = upperCase,
            )
        }
        TimeField(
            value = leg.departureLocal,
            label = R.string.family_editor_departure,
            airport = leg.originAirport,
            onValueChange = { onChange(leg.copy(departureLocal = it)) },
        )
        TimeField(
            value = leg.arrivalLocal,
            label = R.string.family_editor_arrival,
            airport = leg.destinationAirport,
            onValueChange = { onChange(leg.copy(arrivalLocal = it)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormField(
                value = leg.terminal,
                label = R.string.family_editor_terminal,
                onValueChange = { onChange(leg.copy(terminal = it.take(10))) },
                modifier = Modifier.weight(1f),
                keyboardOptions = upperCase,
            )
            FormField(
                value = leg.gate,
                label = R.string.family_editor_gate,
                onValueChange = { onChange(leg.copy(gate = it.take(10))) },
                modifier = Modifier.weight(1f),
                keyboardOptions = upperCase,
            )
        }
        if (canRemove) {
            TextButton(onClick = onRemove) { Text(stringResource(R.string.family_editor_remove_flight)) }
        }
    }
}

/** A date + time field. Shows which time zone is used, so the family knows what to type. */
@Composable
private fun TimeField(
    value: String,
    @StringRes label: Int,
    airport: String,
    onValueChange: (String) -> Unit,
) {
    val zoneText = if (isKnownAirport(airport)) {
        stringResource(R.string.family_editor_zone_airport, airport.trim().uppercase(Locale.ROOT), zoneFor(airport).id)
    } else {
        stringResource(R.string.family_editor_zone_phone, zoneFor(airport).id)
    }
    FormField(
        value = value,
        label = label,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(R.string.family_editor_time_placeholder)) },
        supportingText = { Text(zoneText) },
    )
}

@Composable
private fun ContactCard(
    number: Int,
    contact: ContactForm,
    onChange: (ContactForm) -> Unit,
    onRemove: () -> Unit,
) {
    ItemCard(title = stringResource(R.string.family_editor_contact_n, number)) {
        FormField(
            value = contact.name,
            label = R.string.family_editor_contact_name,
            onValueChange = { onChange(contact.copy(name = it)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        FormField(
            value = contact.relation,
            label = R.string.family_editor_contact_relation,
            onValueChange = { onChange(contact.copy(relation = it)) },
        )
        FormField(
            value = contact.phoneNumber,
            label = R.string.family_editor_contact_phone,
            onValueChange = { onChange(contact.copy(phoneNumber = it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        TextButton(onClick = onRemove) { Text(stringResource(R.string.family_editor_remove_contact)) }
    }
}

@Composable
private fun StaffCardEditor(
    number: Int,
    card: StaffCardForm,
    onChange: (StaffCardForm) -> Unit,
    onRemove: () -> Unit,
) {
    ItemCard(title = stringResource(R.string.family_editor_card_n, number)) {
        Text(stringResource(R.string.family_editor_card_category), style = MaterialTheme.typography.labelLarge)
        // 2 buttons per row, so every type is always visible (no hidden buttons on the right).
        STAFF_CARD_CATEGORIES.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCategories.forEach { category ->
                    val text = stringResource(categoryLabel(category))
                    val select = { onChange(card.copy(category = category)) }
                    if (category == card.category) {
                        Button(onClick = select, modifier = Modifier.weight(1f)) { Text(text) }
                    } else {
                        OutlinedButton(onClick = select, modifier = Modifier.weight(1f)) { Text(text) }
                    }
                }
                if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text(
            stringResource(categoryInfo(card.category)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FormField(
            value = card.titleEn,
            label = R.string.family_editor_card_title_en,
            onValueChange = { onChange(card.copy(titleEn = it)) },
        )
        FormField(
            value = card.titleTr,
            label = R.string.family_editor_card_title_tr,
            onValueChange = { onChange(card.copy(titleTr = it)) },
        )
        FormField(
            value = card.messageEn,
            label = R.string.family_editor_card_message_en,
            onValueChange = { onChange(card.copy(messageEn = it)) },
            singleLine = false,
        )
        FormField(
            value = card.messageTr,
            label = R.string.family_editor_card_message_tr,
            onValueChange = { onChange(card.copy(messageTr = it)) },
            singleLine = false,
        )
        TextButton(onClick = onRemove) { Text(stringResource(R.string.family_editor_remove_card)) }
    }
}

// ---------------- Small building blocks ----------------

@Composable
private fun SectionTitle(@StringRes text: Int) {
    Text(stringResource(text), style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun ItemCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun FormField(
    value: String,
    @StringRes label: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        placeholder = placeholder,
        supportingText = supportingText,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
    )
}

@StringRes
private fun categoryLabel(category: String): Int = when (category) {
    "TRANSFER" -> R.string.family_editor_category_transfer
    "GATE" -> R.string.family_editor_category_gate
    "BAGGAGE" -> R.string.family_editor_category_baggage
    "HELP" -> R.string.family_editor_category_help
    else -> R.string.family_editor_category_other
}

/** Where the traveler's phone shows a card of this type. */
@StringRes
private fun categoryInfo(category: String): Int = when (category) {
    "TRANSFER" -> R.string.family_editor_type_info_transfer
    "GATE" -> R.string.family_editor_type_info_gate
    "BAGGAGE" -> R.string.family_editor_type_info_baggage
    "HELP" -> R.string.family_editor_type_info_help
    else -> R.string.family_editor_type_info_other
}

@Composable
private fun problemText(problem: FormProblem): String = when (problem.type) {
    ProblemType.TRAVELER_NAME -> stringResource(R.string.family_problem_name)
    ProblemType.NO_FLIGHTS -> stringResource(R.string.family_problem_no_flights)
    ProblemType.FLIGHT_NUMBER -> stringResource(R.string.family_problem_flight_number, problem.number)
    ProblemType.AIRPORT -> stringResource(R.string.family_problem_airport, problem.number)
    ProblemType.TIME_FORMAT -> stringResource(R.string.family_problem_time, problem.number)
    ProblemType.ARRIVAL_BEFORE_DEPARTURE -> stringResource(R.string.family_problem_arrival, problem.number)
    ProblemType.TERMINAL_GATE -> stringResource(R.string.family_problem_terminal_gate, problem.number)
    ProblemType.CONTACT_INCOMPLETE -> stringResource(R.string.family_problem_contact, problem.number)
    ProblemType.CARD_INCOMPLETE -> stringResource(R.string.family_problem_card, problem.number)
}
