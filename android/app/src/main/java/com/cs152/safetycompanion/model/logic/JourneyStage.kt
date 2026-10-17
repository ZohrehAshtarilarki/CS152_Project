package com.cs152.safetycompanion.model.logic

import com.cs152.safetycompanion.model.Leg

/** Where the traveler is in the trip right now. */
sealed interface JourneyStage {
    data object NoTrip : JourneyStage
    data class Upcoming(val leg: Leg) : JourneyStage
    data class Active(val currentLeg: Leg, val nextLeg: Leg?) : JourneyStage
    data class Completed(val finalLeg: Leg) : JourneyStage
}

/**
 * Finds the stage by comparing the phone's clock with the flight times. Works offline.
 * A flight is "active" from 6 hours before departure until it lands.
 */
object JourneyStageResolver {

    private const val SIX_HOURS_MILLIS = 6 * 3600 * 1000L

    fun resolve(nowUtc: Long, legs: List<Leg>): JourneyStage {
        if (legs.isEmpty()) return JourneyStage.NoTrip
        val sorted = legs.sortedBy { it.sequenceOrder }

        for ((index, leg) in sorted.withIndex()) {
            val activeFrom = leg.departureTimeUtc - SIX_HOURS_MILLIS
            if (nowUtc in activeFrom..leg.arrivalTimeUtc) {
                return JourneyStage.Active(currentLeg = leg, nextLeg = sorted.getOrNull(index + 1))
            }
            if (nowUtc < activeFrom) {
                return JourneyStage.Upcoming(leg)
            }
        }
        return JourneyStage.Completed(sorted.last())
    }
}
