package com.cs152.safetycompanion.model.logic

import com.cs152.safetycompanion.model.StaffCard

/** Fills {GATE}, {FLIGHT} and {PHONE} in a staff card's messages. */
fun StaffCard.fill(gate: String, flight: String, phone: String): StaffCard = copy(
    messageEn = messageEn.fillPlaceholders(gate, flight, phone),
    messageTr = messageTr.fillPlaceholders(gate, flight, phone),
)

private fun String.fillPlaceholders(gate: String, flight: String, phone: String): String =
    replace("{GATE}", gate).replace("{FLIGHT}", flight).replace("{PHONE}", phone)

/** The gate and flight number the traveler needs right now ("?" if unknown). */
fun JourneyStage.currentGateAndFlight(): Pair<String, String> = when (this) {
    is JourneyStage.Active -> (currentLeg.gate ?: "?") to currentLeg.flightNumber
    is JourneyStage.Upcoming -> (leg.gate ?: "?") to leg.flightNumber
    is JourneyStage.Completed -> (finalLeg.gate ?: "?") to finalLeg.flightNumber
    JourneyStage.NoTrip -> "?" to "?"
}
