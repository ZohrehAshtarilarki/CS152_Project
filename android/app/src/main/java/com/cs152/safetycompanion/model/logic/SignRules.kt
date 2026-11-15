package com.cs152.safetycompanion.model.logic

import java.util.Locale

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
                category = SignCategory.BAGGAGE,
                messageTr = "Dikkat! Bağlantı uçuşunuz var. Bagaj alanına gitmeyin.",
                showStaffCard = false
            )
        }

        // 2. Transfer / Connection
        if (hasTransfer) {
            val gateStr = activeStage?.nextLeg?.gate ?: "henüz belli değil"
            return SignGuidance(
                category = SignCategory.TRANSFER,
                messageTr = "Aktarma alanına doğru gidiyorsunuz. Bağlantı uçuşunuzun kapısı: $gateStr.",
                showStaffCard = activeStage?.nextLeg?.gate == null
            )
        }

        // 3. Gate
        if (hasGate) {
            val gateStr = activeStage?.currentLeg?.gate ?: "henüz belli değil"
            return SignGuidance(
                category = SignCategory.GATE,
                messageTr = "Uçuş kapılarına yönlendiriliyorsunuz. Sizin kapınız: $gateStr.",
                showStaffCard = activeStage?.currentLeg?.gate == null
            )
        }

        // 4. Baggage (when it's safe to collect)
        if (hasBaggage) {
            return SignGuidance(
                category = SignCategory.BAGGAGE,
                messageTr = "Bagaj alım alanına doğru gidiyorsunuz. Lütfen bagajınızı alın.",
                showStaffCard = false
            )
        }

        // Safe default: never guess, send the traveler to staff.
        return SignGuidance(
            category = SignCategory.UNKNOWN,
            messageTr = "Tabela tam anlaşılamadı. Lütfen görevliye danışın.",
            showStaffCard = true,
        )
    }
}
