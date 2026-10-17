package com.cs152.safetycompanion.service;

import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.model.PairingSession;
import com.cs152.safetycompanion.model.PairingSession.Status;
import com.cs152.safetycompanion.model.Trip;
import com.cs152.safetycompanion.repository.PairingSessionRepository;
import com.cs152.safetycompanion.view.PairingViews.PairingStartResponse;
import com.cs152.safetycompanion.view.PairingViews.PairingStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Pairing a traveler's phone without a login (see docs/architecture.md, "Pairing flow").
 * Security in this version: random code, expires after 10 minutes, works only once,
 * and each device token can read only its own trip.
 */
@Service
public class PairingService {

    private final PairingSessionRepository sessions;
    private final TripService tripService;
    private final Duration codeLifetime;

    public PairingService(PairingSessionRepository sessions,
                          TripService tripService,
                          @Value("${app.pairing.code-minutes:10}") long codeMinutes) {
        this.sessions = sessions;
        this.tripService = tripService;
        this.codeLifetime = Duration.ofMinutes(codeMinutes);
    }

    /** Step 1 (traveler phone): make a new 6-digit code. */
    @Transactional
    public PairingStartResponse start() {
        Instant now = Instant.now();
        String code;
        do {
            code = Tokens.newPairingCode();
        } while (sessions.existsByCodeAndStatusAndExpiresAtAfter(code, Status.WAITING, now));

        PairingSession session = sessions.save(new PairingSession(code, now.plus(codeLifetime)));
        return new PairingStartResponse(session.getId(), session.getCode(), session.getExpiresAt().toEpochMilli());
    }

    /** Step 2 (family): the family types the code the traveler read aloud. */
    @Transactional
    public PairingStatusResponse confirm(Family family, String tripId, String code) {
        Trip trip = tripService.findOwnedTrip(family, tripId);
        PairingSession session = sessions
                .findFirstByCodeAndStatusAndExpiresAtAfter(code, Status.WAITING, Instant.now())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "The code is wrong or expired. Ask the traveler for a new code."));
        session.confirm(trip, Tokens.newToken());
        // The device token is NOT sent to the family, only to the traveler phone (step 3).
        return new PairingStatusResponse(Status.CONFIRMED.name(), null, trip.getId());
    }

    /** Step 3 (traveler phone): ask every few seconds until the family has confirmed. */
    @Transactional(readOnly = true)
    public PairingStatusResponse status(String pairingId) {
        PairingSession session = sessions.findById(pairingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pairing not found."));
        if (session.getStatus() == Status.CONFIRMED) {
            return new PairingStatusResponse(Status.CONFIRMED.name(), session.getDeviceToken(), session.getTrip().getId());
        }
        if (session.isExpired(Instant.now())) {
            return new PairingStatusResponse("EXPIRED", null, null);
        }
        return new PairingStatusResponse(Status.WAITING.name(), null, null);
    }

    /** Returns the trip id this traveler phone may read, or answers 401. */
    @Transactional(readOnly = true)
    public String requireDeviceTripId(String authorizationHeader) {
        String token = Tokens.fromHeader(authorizationHeader);
        return sessions.findByDeviceTokenAndStatus(token, Status.CONFIRMED)
                .map(session -> session.getTrip().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "This phone is not paired."));
    }
}
