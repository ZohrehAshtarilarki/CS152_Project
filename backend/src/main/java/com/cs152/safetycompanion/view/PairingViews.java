package com.cs152.safetycompanion.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** VIEW: JSON shapes for pairing a traveler's phone. */
public final class PairingViews {

    private PairingViews() {
    }

    /** Sent to the traveler phone. It shows "code" big and keeps "pairingId" secret. */
    public record PairingStartResponse(String pairingId, String code, long expiresAtUtc) {
    }

    /** status: WAITING, CONFIRMED or EXPIRED. deviceToken is only sent to the traveler phone. */
    public record PairingStatusResponse(String status, String deviceToken, String tripId) {
    }

    /** Sent by the family after the traveler reads the code aloud. */
    public record PairRequest(@NotBlank @Pattern(regexp = "\\d{6}") String code) {
    }
}
