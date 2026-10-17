package com.cs152.safetycompanion.model.logic

enum class SignCategory { TRANSFER, GATE, BAGGAGE, UNKNOWN }

data class SignGuidance(
    val category: SignCategory,
    /** The advice shown to the traveler, in Turkish (correct Turkish letters). */
    val messageTr: String,
    /** true = also offer the staff card, because the app is not sure. */
    val showStaffCard: Boolean,
)

/**
 * Turns the text read from a sign + the current trip step into simple Turkish advice.
 *
 * TODO (Traveler side):
 *  - TRANSFER words (transfer, transit, connection, aktarma) -> show the connection gate.
 *  - GATE words (gate, boarding, kapı) -> show the traveler's gate.
 *  - BAGGAGE words (baggage, claim, luggage, bagaj) -> if a connecting flight is next,
 *    WARN: "Dikkat! Bağlantı uçuşunuz var. Bagaj alanına gitmeyin."
 *  - Use Turkish-aware lowercase: text.lowercase(Locale.forLanguageTag("tr")),
 *    and don't split words on non-English letters (ı, ş, ğ, ...).
 *  - Add unit tests in app/src/test (see JourneyStageResolverTest for an example).
 */
class SignRules {

    fun evaluate(signText: String, stage: JourneyStage): SignGuidance {
        // Safe default until the rules are written: never guess, send the traveler to staff.
        return SignGuidance(
            category = SignCategory.UNKNOWN,
            messageTr = "Tabela tam anlaşılamadı. Lütfen görevliye danışın.",
            showStaffCard = true,
        )
    }
}
