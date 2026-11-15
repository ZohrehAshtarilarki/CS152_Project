package com.cs152.safetycompanion.view.family

import com.cs152.safetycompanion.model.Contact
import com.cs152.safetycompanion.model.Leg
import com.cs152.safetycompanion.model.StaffCard
import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.model.remote.TripRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/*
 * The trip editor's form as plain Kotlin (no Compose), so it is easy to unit test.
 * TripEditorScreen shows a TripForm; toRequest() checks it and turns it into a TripRequest.
 *
 * Times: the family types the time printed on the ticket = the LOCAL time at that airport.
 * Departure uses the origin airport's time zone, arrival uses the destination's.
 * The API always gets UTC epoch milliseconds.
 */

/** One flight, as the family types it. Times look like "2026-11-20 14:30". */
data class LegForm(
    val flightNumber: String = "",
    val originAirport: String = "",
    val destinationAirport: String = "",
    val departureLocal: String = "",
    val arrivalLocal: String = "",
    val terminal: String = "",
    val gate: String = "",
    val status: String = "SCHEDULED",
) {
    companion object {
        fun from(leg: Leg, fallbackZone: ZoneId): LegForm = LegForm(
            flightNumber = leg.flightNumber,
            originAirport = leg.originAirport,
            destinationAirport = leg.destinationAirport,
            departureLocal = formatTicketTime(leg.departureTimeUtc, zoneFor(leg.originAirport, fallbackZone)),
            arrivalLocal = formatTicketTime(leg.arrivalTimeUtc, zoneFor(leg.destinationAirport, fallbackZone)),
            terminal = leg.terminal.orEmpty(),
            gate = leg.gate.orEmpty(),
            status = leg.status,
        )
    }
}

data class ContactForm(
    val name: String = "",
    val relation: String = "",
    val phoneNumber: String = "",
) {
    fun isEmpty() = name.isBlank() && relation.isBlank() && phoneNumber.isBlank()
    fun isComplete() = name.isNotBlank() && relation.isNotBlank() && phoneNumber.isNotBlank()
}

/** A card the traveler shows to airport staff. Messages may use {GATE}, {FLIGHT} and {PHONE}. */
data class StaffCardForm(
    val id: String? = null,
    val category: String = "OTHER",
    val titleEn: String = "",
    val titleTr: String = "",
    val messageEn: String = "",
    val messageTr: String = "",
) {
    private val texts get() = listOf(titleEn, titleTr, messageEn, messageTr)
    fun isEmpty() = texts.all { it.isBlank() }
    fun isComplete() = texts.all { it.isNotBlank() }
}

/** The kinds of mistakes the form can find. The screen turns each one into a message. */
enum class ProblemType {
    TRAVELER_NAME,
    NO_FLIGHTS,
    FLIGHT_NUMBER,
    AIRPORT,
    TIME_FORMAT,
    ARRIVAL_BEFORE_DEPARTURE,
    TERMINAL_GATE,
    CONTACT_INCOMPLETE,
    CARD_INCOMPLETE,
}

/** One thing the family must fix. [number] = flight / contact / card number (1, 2, 3...), or 0. */
data class FormProblem(val type: ProblemType, val number: Int = 0)

sealed interface FormResult {
    data class Ok(val request: TripRequest) : FormResult
    data class Invalid(val problems: List<FormProblem>) : FormResult
}

/**
 * Card types. The type decides WHEN the traveler's phone offers the card; many cards can share a type.
 *  - TRANSFER, GATE, BAGGAGE: offered by Read a Sign (SignRules.kt)
 *  - HELP: shown on the I Need Help screen (HelpScreen.kt)
 *  - OTHER: general card, shown only in Talk to Airport Staff
 * Every card is always shown in Talk to Airport Staff (StaffCardsScreen.kt).
 */
val STAFF_CARD_CATEGORIES = listOf("TRANSFER", "GATE", "BAGGAGE", "HELP", "OTHER")

data class TripForm(
    val travelerName: String = "",
    val targetLanguage: String = "tr",
    val legs: List<LegForm> = listOf(LegForm()),
    val contacts: List<ContactForm> = listOf(ContactForm()),
    val staffCards: List<StaffCardForm> = defaultStaffCards(),
) {

    /** Checks the form. Rows that are completely empty (contacts, cards) are skipped. */
    fun toRequest(fallbackZone: ZoneId = ZoneId.systemDefault()): FormResult {
        val problems = mutableListOf<FormProblem>()

        val name = travelerName.trim()
        if (name.isEmpty() || name.length > 100) problems += FormProblem(ProblemType.TRAVELER_NAME)
        if (legs.isEmpty()) problems += FormProblem(ProblemType.NO_FLIGHTS)

        val requestLegs = legs.mapIndexedNotNull { index, leg -> leg.toLeg(index + 1, fallbackZone, problems) }

        val requestContacts = contacts.mapIndexedNotNull { index, contact ->
            when {
                contact.isEmpty() -> null
                contact.isComplete() -> Contact(contact.name.trim(), contact.relation.trim(), contact.phoneNumber.trim())
                else -> {
                    problems += FormProblem(ProblemType.CONTACT_INCOMPLETE, index + 1)
                    null
                }
            }
        }

        val requestCards = staffCards.mapIndexedNotNull { index, card ->
            when {
                card.isEmpty() -> null
                card.isComplete() -> StaffCard(
                    id = card.id,
                    category = card.category,
                    titleEn = card.titleEn.trim(),
                    titleTr = card.titleTr.trim(),
                    messageEn = card.messageEn.trim(),
                    messageTr = card.messageTr.trim(),
                )
                else -> {
                    problems += FormProblem(ProblemType.CARD_INCOMPLETE, index + 1)
                    null
                }
            }
        }

        return if (problems.isEmpty()) {
            FormResult.Ok(TripRequest(name, targetLanguage, requestLegs, requestContacts, requestCards))
        } else {
            FormResult.Invalid(problems)
        }
    }

    companion object {
        /** An empty form for a new trip, or the trip's data for editing. */
        fun from(trip: TravelPackage?, fallbackZone: ZoneId = ZoneId.systemDefault()): TripForm {
            if (trip == null) return TripForm()
            return TripForm(
                travelerName = trip.travelerName,
                targetLanguage = trip.targetLanguage,
                legs = trip.legs.sortedBy { it.sequenceOrder }.map { LegForm.from(it, fallbackZone) },
                contacts = trip.emergencyContacts.map { ContactForm(it.name, it.relation, it.phoneNumber) },
                staffCards = trip.staffCards.map {
                    StaffCardForm(it.id, it.category, it.titleEn, it.titleTr, it.messageEn, it.messageTr)
                },
            )
        }
    }
}

private val AIRPORT_CODE = Regex("[A-Z]{3}")

private fun LegForm.toLeg(number: Int, fallbackZone: ZoneId, problems: MutableList<FormProblem>): Leg? {
    val problemsBefore = problems.size
    // Locale.ROOT: on a Turkish phone, "ist".uppercase() would give "İST" (dotted İ).
    val flight = flightNumber.trim().uppercase(Locale.ROOT)
    val from = originAirport.trim().uppercase(Locale.ROOT)
    val to = destinationAirport.trim().uppercase(Locale.ROOT)

    if (flight.isEmpty() || flight.length > 10) problems += FormProblem(ProblemType.FLIGHT_NUMBER, number)
    if (!AIRPORT_CODE.matches(from) || !AIRPORT_CODE.matches(to)) problems += FormProblem(ProblemType.AIRPORT, number)

    val departure = parseTicketTime(departureLocal, zoneFor(from, fallbackZone))
    val arrival = parseTicketTime(arrivalLocal, zoneFor(to, fallbackZone))
    if (departure == null || arrival == null) {
        problems += FormProblem(ProblemType.TIME_FORMAT, number)
    } else if (arrival <= departure) {
        problems += FormProblem(ProblemType.ARRIVAL_BEFORE_DEPARTURE, number)
    }

    if (terminal.trim().length > 10 || gate.trim().length > 10) problems += FormProblem(ProblemType.TERMINAL_GATE, number)

    if (problems.size > problemsBefore || departure == null || arrival == null) return null
    return Leg(
        sequenceOrder = number,
        flightNumber = flight,
        originAirport = from,
        destinationAirport = to,
        departureTimeUtc = departure,
        arrivalTimeUtc = arrival,
        terminal = terminal.trim().ifEmpty { null },
        gate = gate.trim().ifEmpty { null },
        status = status,
    )
}

// ---------------- Times ----------------

private val TICKET_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT)

/** "2026-11-20 14:30" at [zone] -> UTC epoch millis, or null if the text is not in that format. */
fun parseTicketTime(text: String, zone: ZoneId): Long? = try {
    LocalDateTime.parse(text.trim(), TICKET_TIME).atZone(zone).toInstant().toEpochMilli()
} catch (e: DateTimeParseException) {
    null
}

/** UTC epoch millis -> "2026-11-20 14:30" at [zone]. */
fun formatTicketTime(epochMillis: Long, zone: ZoneId): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime().format(TICKET_TIME)

/** Time zones of airports this project uses. Other airports use the phone's time zone. */
private val AIRPORT_TIME_ZONES = mapOf(
    // USA
    "SFO" to "America/Los_Angeles", "SJC" to "America/Los_Angeles", "OAK" to "America/Los_Angeles",
    "LAX" to "America/Los_Angeles", "SEA" to "America/Los_Angeles", "ORD" to "America/Chicago",
    "JFK" to "America/New_York", "EWR" to "America/New_York", "IAD" to "America/New_York", "BOS" to "America/New_York",
    // Türkiye
    "IST" to "Europe/Istanbul", "SAW" to "Europe/Istanbul", "ESB" to "Europe/Istanbul",
    "ADB" to "Europe/Istanbul", "AYT" to "Europe/Istanbul",
    // Europe hubs
    "FRA" to "Europe/Berlin", "MUC" to "Europe/Berlin", "AMS" to "Europe/Amsterdam",
    "CDG" to "Europe/Paris", "LHR" to "Europe/London",
)

fun isKnownAirport(code: String): Boolean = code.trim().uppercase(Locale.ROOT) in AIRPORT_TIME_ZONES

fun zoneFor(airport: String, fallback: ZoneId = ZoneId.systemDefault()): ZoneId =
    AIRPORT_TIME_ZONES[airport.trim().uppercase(Locale.ROOT)]?.let { ZoneId.of(it) } ?: fallback

// ---------------- Helpers ----------------

/** Ready-made cards for a new trip (same texts as the backend demo data). The family can change them. */
fun defaultStaffCards(): List<StaffCardForm> = listOf(
    StaffCardForm(
        category = "TRANSFER",
        titleEn = "Help Finding Connection Gate",
        titleTr = "Aktarma Kapısını Bulma Yardımı",
        messageEn = "I do not speak English well. Please guide me to Gate {GATE} for flight {FLIGHT}.",
        messageTr = "İngilizce iyi konuşamıyorum. Lütfen {FLIGHT} sefer sayılı uçuşum için {GATE} numaralı kapıya gitmeme yardım edin.",
    ),
    StaffCardForm(
        category = "GATE",
        titleEn = "Help Finding My Gate",
        titleTr = "Kapımı Bulma Yardımı",
        messageEn = "I do not speak English well. I am looking for Gate {GATE} for flight {FLIGHT}. Please show me the way.",
        messageTr = "İngilizce iyi konuşamıyorum. {FLIGHT} sefer sayılı uçuşum için {GATE} numaralı kapıyı arıyorum. Lütfen bana yolu gösterin.",
    ),
    StaffCardForm(
        category = "BAGGAGE",
        titleEn = "Question About My Baggage",
        titleTr = "Bagajımla İlgili Soru",
        messageEn = "I do not speak English well. Is my checked baggage sent to my final destination, or do I need to pick it up here? My next flight: {FLIGHT}.",
        messageTr = "İngilizce iyi konuşamıyorum. Bagajım son varış noktama mı gönderiliyor, yoksa burada mı almam gerekiyor? Sonraki uçuşum: {FLIGHT}.",
    ),
    StaffCardForm(
        category = "HELP",
        titleEn = "I Need Help",
        titleTr = "Yardıma İhtiyacım Var",
        messageEn = "I am traveling alone and I need help. Please call my family: {PHONE}.",
        messageTr = "Yalnız seyahat ediyorum ve yardıma ihtiyacım var. Lütfen ailemi arayın: {PHONE}.",
    ),
)

fun <T> List<T>.replaceAt(index: Int, item: T): List<T> = mapIndexed { i, old -> if (i == index) item else old }

fun <T> List<T>.withoutAt(index: Int): List<T> = filterIndexed { i, _ -> i != index }
