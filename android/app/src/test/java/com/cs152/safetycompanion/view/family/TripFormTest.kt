package com.cs152.safetycompanion.view.family

import com.cs152.safetycompanion.model.Leg
import com.cs152.safetycompanion.model.TravelPackage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

/** Unit tests for the trip editor form (no Android needed). Run with: ./gradlew test */
class TripFormTest {

    private val phoneZone = ZoneId.of("America/Los_Angeles")

    private fun utcMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun validForm() = TripForm(
        travelerName = "  Ayşe Yılmaz ",
        legs = listOf(LegForm("tk008", "sfo", "ist", "2026-11-20 14:30", "2026-11-21 19:15", "I", "G101")),
        contacts = listOf(ContactForm("Demo Caregiver", "Daughter", "+1-408-555-0199")),
    )

    private fun problemsOf(form: TripForm): List<FormProblem> =
        (form.toRequest(phoneZone) as FormResult.Invalid).problems

    @Test
    fun validForm_becomesRequestWithUtcTimes() {
        val request = (validForm().toRequest(phoneZone) as FormResult.Ok).request

        assertEquals("Ayşe Yılmaz", request.travelerName)
        val leg = request.legs.single()
        assertEquals(1, leg.sequenceOrder)
        assertEquals("TK008", leg.flightNumber)
        assertEquals("SFO", leg.originAirport)
        assertEquals("IST", leg.destinationAirport)
        // 14:30 in San Francisco (UTC-8 in November) = 22:30 UTC
        assertEquals(utcMillis(2026, 11, 20, 22, 30), leg.departureTimeUtc)
        // 19:15 in Istanbul (UTC+3) = 16:15 UTC
        assertEquals(utcMillis(2026, 11, 21, 16, 15), leg.arrivalTimeUtc)
        assertEquals(1, request.emergencyContacts.size)
        assertEquals(4, request.staffCards.size) // the ready-made cards of a new trip
    }

    @Test
    fun emptyContactAndTerminal_areSkipped() {
        val form = validForm().copy(
            legs = listOf(LegForm("TK008", "SFO", "IST", "2026-11-20 14:30", "2026-11-21 19:15")),
            contacts = listOf(ContactForm()),
            staffCards = emptyList(),
        )

        val request = (form.toRequest(phoneZone) as FormResult.Ok).request

        assertTrue(request.emergencyContacts.isEmpty())
        assertTrue(request.staffCards.isEmpty())
        assertNull(request.legs.single().terminal)
        assertNull(request.legs.single().gate)
    }

    @Test
    fun missingName_isAProblem() {
        assertEquals(listOf(FormProblem(ProblemType.TRAVELER_NAME)), problemsOf(validForm().copy(travelerName = "  ")))
    }

    @Test
    fun noFlights_isAProblem() {
        assertEquals(listOf(FormProblem(ProblemType.NO_FLIGHTS)), problemsOf(validForm().copy(legs = emptyList())))
    }

    @Test
    fun badAirportCode_isAProblem() {
        val form = validForm().copy(legs = listOf(validForm().legs[0].copy(originAirport = "SF")))
        assertEquals(listOf(FormProblem(ProblemType.AIRPORT, 1)), problemsOf(form))
    }

    @Test
    fun wrongTimeFormat_isAProblem() {
        val form = validForm().copy(legs = listOf(validForm().legs[0].copy(departureLocal = "20/11/2026 2:30 PM")))
        assertEquals(listOf(FormProblem(ProblemType.TIME_FORMAT, 1)), problemsOf(form))
    }

    @Test
    fun arrivalBeforeDeparture_isComparedInUtc() {
        // 20:00 in Istanbul = 17:00 UTC, but 14:30 in San Francisco = 22:30 UTC -> arrival is before departure.
        val form = validForm().copy(legs = listOf(validForm().legs[0].copy(arrivalLocal = "2026-11-20 20:00")))
        assertEquals(listOf(FormProblem(ProblemType.ARRIVAL_BEFORE_DEPARTURE, 1)), problemsOf(form))
    }

    @Test
    fun halfFilledContactAndCard_areProblems() {
        val form = validForm().copy(
            contacts = listOf(ContactForm(name = "Only a name")),
            staffCards = listOf(StaffCardForm(category = "GATE", titleEn = "Only a title")),
        )
        assertEquals(
            listOf(FormProblem(ProblemType.CONTACT_INCOMPLETE, 1), FormProblem(ProblemType.CARD_INCOMPLETE, 1)),
            problemsOf(form),
        )
    }

    @Test
    fun newCard_isOtherType_andCanShareATypeWithOtherCards() {
        val extra = StaffCardForm(
            titleEn = "I Need a Wheelchair",
            titleTr = "Tekerlekli Sandalyeye İhtiyacım Var",
            messageEn = "Please bring me a wheelchair.",
            messageTr = "Lütfen bana bir tekerlekli sandalye getirin.",
        )
        val secondHelp = extra.copy(category = "HELP")
        val form = validForm().copy(staffCards = defaultStaffCards() + extra + secondHelp)

        val cards = (form.toRequest(phoneZone) as FormResult.Ok).request.staffCards

        assertEquals(6, cards.size)
        assertEquals("OTHER", cards[4].category)
        assertEquals(2, cards.count { it.category == "HELP" })
    }

    @Test
    fun airportTimeZones() {
        assertEquals(ZoneId.of("Europe/Istanbul"), zoneFor("ist", phoneZone))
        assertEquals(ZoneId.of("America/Los_Angeles"), zoneFor("SFO", ZoneOffset.UTC))
        assertEquals(phoneZone, zoneFor("XYZ", phoneZone)) // unknown airport -> phone's time zone
    }

    @Test
    fun editingATrip_showsTheSameTicketTimes() {
        val trip = TravelPackage(
            tripId = "t1",
            travelerName = "Ayşe Yılmaz",
            targetLanguage = "tr",
            lastUpdatedUtc = 0L,
            legs = listOf(
                Leg(1, "TK008", "SFO", "IST", utcMillis(2026, 11, 20, 22, 30), utcMillis(2026, 11, 21, 16, 15), "I", "G101"),
            ),
            emergencyContacts = emptyList(),
            staffCards = emptyList(),
        )

        val leg = TripForm.from(trip, phoneZone).legs.single()

        assertEquals("2026-11-20 14:30", leg.departureLocal)
        assertEquals("2026-11-21 19:15", leg.arrivalLocal)
    }

    @Test
    fun turkishPhone_stillGivesNormalAirportCodes() {
        val oldLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"))
            val request = (validForm().toRequest(phoneZone) as FormResult.Ok).request
            assertEquals("IST", request.legs.single().destinationAirport) // not "İST"
        } finally {
            Locale.setDefault(oldLocale)
        }
    }
}
