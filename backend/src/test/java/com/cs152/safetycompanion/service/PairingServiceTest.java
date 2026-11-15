package com.cs152.safetycompanion.service;

import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.model.PairingSession;
import com.cs152.safetycompanion.model.PairingSession.Status;
import com.cs152.safetycompanion.model.Trip;
import com.cs152.safetycompanion.repository.PairingSessionRepository;
import com.cs152.safetycompanion.view.PairingViews.PairingStartResponse;
import com.cs152.safetycompanion.view.PairingViews.PairingStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for pairing a traveler's phone (no database needed).
 * The repository and TripService are fake objects (Mockito mocks).
 * Run with: ./mvnw test
 */
@ExtendWith(MockitoExtension.class)
class PairingServiceTest {

    private static final String CODE = "482913";

    @Mock
    private PairingSessionRepository sessions;

    @Mock
    private TripService tripService;

    private PairingService pairingService;
    private Family family;
    private Trip trip;

    @BeforeEach
    void setUp() {
        pairingService = new PairingService(sessions, tripService, 10);
        family = new Family("Test Family", "test@cs152.dev", "hash");
        trip = new Trip(family, "Ayşe Yılmaz", "tr");
    }

    // ---------- Step 1: start ----------

    @Test
    void start_givesSixDigitCodeThatExpiresInTenMinutes() {
        when(sessions.existsByCodeAndStatusAndExpiresAtAfter(anyString(), eq(Status.WAITING), any()))
                .thenReturn(false);
        when(sessions.save(any(PairingSession.class))).thenAnswer(call -> call.getArgument(0));

        Instant before = Instant.now();
        PairingStartResponse response = pairingService.start();
        Instant after = Instant.now();

        assertNotNull(response.pairingId());
        assertTrue(response.code().matches("\\d{6}"), "bad code: " + response.code());
        long tenMinutes = Duration.ofMinutes(10).toMillis();
        assertTrue(response.expiresAtUtc() >= before.toEpochMilli() + tenMinutes);
        assertTrue(response.expiresAtUtc() <= after.toEpochMilli() + tenMinutes);
    }

    @Test
    void start_makesANewCodeIfTheCodeIsAlreadyInUse() {
        when(sessions.existsByCodeAndStatusAndExpiresAtAfter(anyString(), eq(Status.WAITING), any()))
                .thenReturn(true)    // first code is taken
                .thenReturn(false);  // second code is free
        when(sessions.save(any(PairingSession.class))).thenAnswer(call -> call.getArgument(0));

        pairingService.start();

        verify(sessions, times(2)).existsByCodeAndStatusAndExpiresAtAfter(anyString(), eq(Status.WAITING), any());
    }

    // ---------- Step 2: family confirms the code ----------

    @Test
    void confirm_correctCode_pairsThePhoneButDoesNotSendTheDeviceTokenToTheFamily() {
        PairingSession session = waitingSession(Instant.now().plus(Duration.ofMinutes(10)));
        when(tripService.findOwnedTrip(family, trip.getId())).thenReturn(trip);
        repositoryFindsOnlyWaitingSession(session);

        PairingStatusResponse response = pairingService.confirm(family, trip.getId(), CODE);

        // The family gets the status and trip id, but NOT the device token.
        assertEquals("CONFIRMED", response.status());
        assertNull(response.deviceToken());
        assertEquals(trip.getId(), response.tripId());

        // The session is now confirmed and linked to the trip, with a new device token.
        assertEquals(Status.CONFIRMED, session.getStatus());
        assertSame(trip, session.getTrip());
        assertNotNull(session.getDeviceToken());
    }

    @Test
    void confirm_codeWorksOnlyOnce() {
        PairingSession session = waitingSession(Instant.now().plus(Duration.ofMinutes(10)));
        when(tripService.findOwnedTrip(family, trip.getId())).thenReturn(trip);
        repositoryFindsOnlyWaitingSession(session);

        pairingService.confirm(family, trip.getId(), CODE);
        String firstToken = session.getDeviceToken();

        ApiException error = assertThrows(ApiException.class,
                () -> pairingService.confirm(family, trip.getId(), CODE));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(firstToken, session.getDeviceToken(), "the device token must not change");
    }

    @Test
    void confirm_wrongOrExpiredCode_isRejected() {
        when(tripService.findOwnedTrip(family, trip.getId())).thenReturn(trip);
        when(sessions.findFirstByCodeAndStatusAndExpiresAtAfter(eq("000000"), eq(Status.WAITING), any()))
                .thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class,
                () -> pairingService.confirm(family, trip.getId(), "000000"));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void confirm_onlyLooksForCodesThatHaveNotExpiredYet() {
        when(tripService.findOwnedTrip(family, trip.getId())).thenReturn(trip);
        ArgumentCaptor<Instant> nowCaptor = ArgumentCaptor.forClass(Instant.class);
        when(sessions.findFirstByCodeAndStatusAndExpiresAtAfter(eq(CODE), eq(Status.WAITING), nowCaptor.capture()))
                .thenReturn(Optional.empty());

        Instant before = Instant.now();
        assertThrows(ApiException.class, () -> pairingService.confirm(family, trip.getId(), CODE));
        Instant after = Instant.now();

        // The service must ask for codes that expire after "now" (not some old time).
        Instant usedNow = nowCaptor.getValue();
        assertFalse(usedNow.isBefore(before));
        assertFalse(usedNow.isAfter(after));
    }

    @Test
    void confirm_tripOfAnotherFamily_isRejectedBeforeTheCodeIsUsed() {
        when(tripService.findOwnedTrip(family, "someone-elses-trip"))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Trip not found."));

        ApiException error = assertThrows(ApiException.class,
                () -> pairingService.confirm(family, "someone-elses-trip", CODE));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verifyNoInteractions(sessions); // the code was not touched, so it still works for the right trip
    }

    // ---------- Step 3: traveler phone asks for the status ----------

    @Test
    void status_beforeTheFamilyConfirms_isWaiting() {
        PairingSession session = waitingSession(Instant.now().plus(Duration.ofMinutes(10)));
        when(sessions.findById(session.getId())).thenReturn(Optional.of(session));

        PairingStatusResponse response = pairingService.status(session.getId());

        assertEquals("WAITING", response.status());
        assertNull(response.deviceToken());
        assertNull(response.tripId());
    }

    @Test
    void status_afterTheFamilyConfirms_givesTheDeviceTokenToThePhone() {
        PairingSession session = waitingSession(Instant.now().plus(Duration.ofMinutes(10)));
        session.confirm(trip, "device-token-123");
        when(sessions.findById(session.getId())).thenReturn(Optional.of(session));

        PairingStatusResponse response = pairingService.status(session.getId());

        assertEquals("CONFIRMED", response.status());
        assertEquals("device-token-123", response.deviceToken());
        assertEquals(trip.getId(), response.tripId());
    }

    @Test
    void status_afterTenMinutesWithoutConfirm_isExpired() {
        PairingSession session = waitingSession(Instant.now().minusSeconds(1));
        when(sessions.findById(session.getId())).thenReturn(Optional.of(session));

        PairingStatusResponse response = pairingService.status(session.getId());

        assertEquals("EXPIRED", response.status());
        assertNull(response.deviceToken());
    }

    @Test
    void status_unknownPairingId_is404() {
        when(sessions.findById("unknown")).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> pairingService.status("unknown"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    // ---------- Device token: the paired phone downloads its trip ----------

    @Test
    void requireDeviceTripId_pairedPhone_getsOnlyItsOwnTripId() {
        PairingSession session = waitingSession(Instant.now().plus(Duration.ofMinutes(10)));
        session.confirm(trip, "device-token-123");
        when(sessions.findByDeviceTokenAndStatus("device-token-123", Status.CONFIRMED))
                .thenReturn(Optional.of(session));

        String tripId = pairingService.requireDeviceTripId("Bearer device-token-123");

        assertEquals(trip.getId(), tripId);
    }

    @Test
    void requireDeviceTripId_unpairedPhone_is401() {
        when(sessions.findByDeviceTokenAndStatus("wrong-token", Status.CONFIRMED)).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class,
                () -> pairingService.requireDeviceTripId("Bearer wrong-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
    }

    @Test
    void requireDeviceTripId_missingHeader_is401() {
        ApiException error = assertThrows(ApiException.class, () -> pairingService.requireDeviceTripId(null));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
        verifyNoInteractions(sessions);
    }

    // ---------- Helpers ----------

    private static PairingSession waitingSession(Instant expiresAt) {
        return new PairingSession(CODE, expiresAt);
    }

    /** Acts like the real database query: finds the session only while it is still WAITING. */
    private void repositoryFindsOnlyWaitingSession(PairingSession session) {
        when(sessions.findFirstByCodeAndStatusAndExpiresAtAfter(eq(CODE), eq(Status.WAITING), any()))
                .thenAnswer(call -> session.getStatus() == Status.WAITING
                        ? Optional.of(session)
                        : Optional.empty());
    }
}
