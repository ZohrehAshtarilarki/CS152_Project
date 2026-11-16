package com.cs152.safetycompanion.model.logic

import java.util.Locale

enum class SignAdviceType { BAGGAGE_WARNING, TRANSFER, GATE, BAGGAGE, UNKNOWN }

data class SignGuidance(
    val type: SignAdviceType,
    /** The gate number to substitute into the message, if any. */
    val gate: String? = null,
    /** true = also offer the staff card, because the app is not sure. */
    val showStaffCard: Boolean,
)

/**
 * Turns the text read from a sign + the current trip step into advice type and arguments.
 */
class SignRules {

    private val transferWords = setOf("transfer", "transfers", "transit", "connection", "aktarma")
    private val gateWords = setOf("gate", "boarding", "kapı")
    private val baggageWords = setOf("baggage", "claim", "luggage", "bagaj")

    fun evaluate(signText: String, stage: JourneyStage): SignGuidance {
        val localeTr = Locale.forLanguageTag("tr")
        val lowerText = signText.lowercase(localeTr)
        
        // Extract words safely without splitting on non-English characters like ı, ş, ğ
        val words = lowerText.split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }.toSet()

        val hasBaggage = words.intersect(baggageWords).isNotEmpty()
        val hasTransfer = words.intersect(transferWords).isNotEmpty()
        val hasGate = words.intersect(gateWords).isNotEmpty()
        
        val activeStage = stage as? JourneyStage.Active

        // 1. If connecting flight, BAGGAGE warning takes precedence.
        if (hasBaggage && activeStage?.nextLeg != null) {
            return SignGuidance(
                type = SignAdviceType.BAGGAGE_WARNING,
                showStaffCard = false
            )
        }

        // 2. Transfer / Connection
        if (hasTransfer) {
            val gateStr = activeStage?.nextLeg?.gate
            return SignGuidance(
                type = SignAdviceType.TRANSFER,
                gate = gateStr,
                showStaffCard = gateStr == null
            )
        }

        // 3. Gate
        if (hasGate) {
            val gateStr = activeStage?.currentLeg?.gate
            return SignGuidance(
                type = SignAdviceType.GATE,
                gate = gateStr,
                showStaffCard = gateStr == null
            )
        }

        // 4. Baggage (when it's safe to collect)
        if (hasBaggage) {
            return SignGuidance(
                type = SignAdviceType.BAGGAGE,
                showStaffCard = false
            )
        }

        // Safe default: never guess, send the traveler to staff.
        return SignGuidance(
            type = SignAdviceType.UNKNOWN,
            showStaffCard = true,
        )
    }
}
