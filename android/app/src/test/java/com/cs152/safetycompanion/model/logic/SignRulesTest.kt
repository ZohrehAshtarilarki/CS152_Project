package com.cs152.safetycompanion.model.logic

import com.cs152.safetycompanion.model.Leg
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignRulesTest {

    private val rules = SignRules()

    private val dummyLeg = Leg(
        sequenceOrder = 1,
        flightNumber = "TK101",
        originAirport = "SFO",
        destinationAirport = "IST",
        departureTimeUtc = 0L,
        arrivalTimeUtc = 1000L,
        terminal = "T1",
        gate = "G5"
    )
    
    private val nextLeg = dummyLeg.copy(sequenceOrder = 2, flightNumber = "TK102", originAirport = "IST", destinationAirport = "ESB", gate = "G12")

    @Test
    fun `when TRANSFER words detected, shows transfer guidance and connection gate`() {
        // active stage with a connection next
        val stage = JourneyStage.Active(currentLeg = dummyLeg, nextLeg = nextLeg)
        
        val guidanceEn = rules.evaluate("Follow the signs for International Transfers", stage)
        assertEquals(SignCategory.TRANSFER, guidanceEn.category)
        assertTrue(guidanceEn.messageTr.contains("G12"))
        
        val guidanceTr = rules.evaluate("Transit ve aktarma yolcuları", stage)
        assertEquals(SignCategory.TRANSFER, guidanceTr.category)
    }

    @Test
    fun `when GATE words detected, shows gate guidance`() {
        val stage = JourneyStage.Active(currentLeg = dummyLeg, nextLeg = null)
        
        val guidanceEn = rules.evaluate("Gate G5 Boarding", stage)
        assertEquals(SignCategory.GATE, guidanceEn.category)
        assertTrue(guidanceEn.messageTr.contains("G5"))
        
        val guidanceTr = rules.evaluate("Kapı boarding", stage)
        assertEquals(SignCategory.GATE, guidanceTr.category)
    }

    @Test
    fun `when BAGGAGE words detected AND next leg exists, WARNS traveler`() {
        val stage = JourneyStage.Active(currentLeg = dummyLeg, nextLeg = nextLeg)
        
        val guidance = rules.evaluate("Baggage Claim", stage)
        assertEquals(SignCategory.BAGGAGE, guidance.category)
        assertEquals("Dikkat! Bağlantı uçuşunuz var. Bagaj alanına gitmeyin.", guidance.messageTr)
        assertFalse(guidance.showStaffCard)
    }

    @Test
    fun `when BAGGAGE words detected AND no next leg, tells traveler to collect baggage`() {
        val stage = JourneyStage.Active(currentLeg = dummyLeg, nextLeg = null)
        
        val guidance = rules.evaluate("Baggage claim", stage)
        assertEquals(SignCategory.BAGGAGE, guidance.category)
        assertTrue(guidance.messageTr.contains("Bagaj")) // something affirmative
    }

    @Test
    fun `when words from multiple categories are present, prioritizes BAGGAGE warning if connecting`() {
        val stage = JourneyStage.Active(currentLeg = dummyLeg, nextLeg = nextLeg)
        val guidance = rules.evaluate("Baggage Claim and Transfers", stage)
        assertEquals(SignCategory.BAGGAGE, guidance.category) // Warning is most critical
    }
    
    @Test
    fun `handles Turkish letters correctly without splitting words`() {
        val stage = JourneyStage.Active(currentLeg = dummyLeg, nextLeg = null)
        val guidance = rules.evaluate("KAPI 5", stage) // Uppercase Turkish
        assertEquals(SignCategory.GATE, guidance.category)
    }
}
