package com.cs152.safetycompanion.model.logic

import com.cs152.safetycompanion.model.Leg
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Example unit test (plain Kotlin, no emulator). Run: ./gradlew test */
class JourneyStageResolverTest {

    private val hour = 3600 * 1000L
    private val leg1 = Leg(1, "TK008", "SFO", "IST", departureTimeUtc = 10 * hour, arrivalTimeUtc = 23 * hour,
        terminal = "I", gate = "G101")
    private val leg2 = Leg(2, "TK2108", "IST", "ESB", departureTimeUtc = 26 * hour, arrivalTimeUtc = 27 * hour,
        terminal = "D", gate = "112")

    @Test
    fun noLegsMeansNoTrip() {
        assertEquals(JourneyStage.NoTrip, JourneyStageResolver.resolve(0, emptyList()))
    }

    @Test
    fun longBeforeDepartureIsUpcoming() {
        val stage = JourneyStageResolver.resolve(1 * hour, listOf(leg1, leg2))
        assertEquals(JourneyStage.Upcoming(leg1), stage)
    }

    @Test
    fun duringConnectionTheSecondFlightIsActive() {
        val stage = JourneyStageResolver.resolve(24 * hour, listOf(leg2, leg1)) // order doesn't matter
        assertTrue(stage is JourneyStage.Active)
        assertEquals("TK2108", (stage as JourneyStage.Active).currentLeg.flightNumber)
    }

    @Test
    fun afterLastLandingIsCompleted() {
        val stage = JourneyStageResolver.resolve(30 * hour, listOf(leg1, leg2))
        assertEquals(JourneyStage.Completed(leg2), stage)
    }
}
