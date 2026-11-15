package com.cs152.safetycompanion.service;

import com.cs152.safetycompanion.model.EmergencyContact;
import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.model.JourneyLeg;
import com.cs152.safetycompanion.model.Trip;
import com.cs152.safetycompanion.repository.TripRepository;
import com.cs152.safetycompanion.view.TripViews.ContactView;
import com.cs152.safetycompanion.view.TripViews.LegView;
import com.cs152.safetycompanion.view.TripViews.StaffCardView;
import com.cs152.safetycompanion.view.TripViews.TravelPackageView;
import com.cs152.safetycompanion.view.TripViews.TripRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for creating, reading and changing trips (no database needed).
 * The repository is a fake object (Mockito mock).
 * Run with: ./mvnw test
 */
@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    private static final long DEPARTURE = 1_793_556_600_000L;
    private static final long ARRIVAL = 1_793_625_900_000L;

    @Mock
    private TripRepository trips;

    private TripService tripService;
    private Family family;

    @BeforeEach
    void setUp() {
        tripService = new TripService(trips);
        family = new Family("Test Family", "test@cs152.dev", "hash");
    }

    // ---------- Create ----------

    @Test
    void createTrip_savesTheWholeTrip() {
        when(trips.save(any(Trip.class))).thenAnswer(call -> call.getArgument(0));

        TravelPackageView result = tripService.createTrip(family, fullRequest("  Ayşe Yılmaz  "));

        assertEquals("Ayşe Yılmaz", result.travelerName(), "the name is trimmed, Turkish letters kept");
        assertEquals("tr", result.targetLanguage());

        assertEquals(1, result.legs().size());
        LegView leg = result.legs().get(0);
        assertEquals("TK008", leg.flightNumber(), "flight number is upper case");
        assertEquals("SFO", leg.originAirport());
        assertEquals("IST", leg.destinationAirport());
        assertEquals(DEPARTURE, leg.departureTimeUtc(), "UTC millis stay the same");
        assertEquals(ARRIVAL, leg.arrivalTimeUtc());
        assertEquals("SCHEDULED", leg.status(), "missing status becomes SCHEDULED");

        assertEquals(1, result.emergencyContacts().size());
        assertEquals("+1-408-555-0199", result.emergencyContacts().get(0).phoneNumber());

        assertEquals(1, result.staffCards().size());
        assertEquals("Aktarma Kapısını Bulma Yardımı", result.staffCards().get(0).titleTr());
    }

    @Test
    void createTrip_withoutContactsOrStaffCards_givesEmptyLists() {
        when(trips.save(any(Trip.class))).thenAnswer(call -> call.getArgument(0));
        TripRequest request = new TripRequest("Ayşe Yılmaz", null, List.of(leg(1, "TK008")), null, null);

        TravelPackageView result = tripService.createTrip(family, request);

        assertTrue(result.emergencyContacts().isEmpty());
        assertTrue(result.staffCards().isEmpty());
    }

    @Test
    void createTrip_withoutLanguage_usesTurkish() {
        when(trips.save(any(Trip.class))).thenAnswer(call -> call.getArgument(0));
        TripRequest request = new TripRequest("Ayşe Yılmaz", " ", List.of(leg(1, "TK008")), null, null);

        TravelPackageView result = tripService.createTrip(family, request);

        assertEquals("tr", result.targetLanguage());
    }

    @Test
    void createTrip_onAServerSetToTurkish_keepsNormalAirportCodes() {
        when(trips.save(any(Trip.class))).thenAnswer(call -> call.getArgument(0));
        Locale oldLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));

            TravelPackageView result = tripService.createTrip(family, fullRequest("Ayşe Yılmaz"));

            assertEquals("IST", result.legs().get(0).destinationAirport(), "must not become İST (dotted İ)");
        } finally {
            Locale.setDefault(oldLocale);
        }
    }

    // ---------- Read ----------

    @Test
    void listTrips_returnsOnlyThisFamilysTrips() {
        Trip trip = new Trip(family, "Ayşe Yılmaz", "tr");
        when(trips.findByFamilyIdOrderByCreatedAtDesc(family.getId())).thenReturn(List.of(trip));

        List<TravelPackageView> result = tripService.listTrips(family);

        assertEquals(1, result.size());
        assertEquals(trip.getId(), result.get(0).tripId());
    }

    @Test
    void getTrip_ownTrip_isReturned() {
        Trip trip = new Trip(family, "Ayşe Yılmaz", "tr");
        when(trips.findByIdAndFamilyId(trip.getId(), family.getId())).thenReturn(Optional.of(trip));

        TravelPackageView result = tripService.getTrip(family, trip.getId());

        assertEquals(trip.getId(), result.tripId());
    }

    @Test
    void getTrip_ofAnotherFamily_is404() {
        // The database only finds a trip when BOTH the trip id and this family's id match.
        when(trips.findByIdAndFamilyId("other-trip", family.getId())).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> tripService.getTrip(family, "other-trip"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void buildTravelPackage_unknownTrip_is404() {
        when(trips.findById("unknown")).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> tripService.buildTravelPackage("unknown"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    // ---------- Update ----------

    @Test
    void updateTrip_replacesTheWholeTrip() {
        Trip trip = new Trip(family, "Old Name", "tr");
        trip.replaceLegs(List.of(
                legModel(1, "TK001"),
                legModel(2, "TK002")));
        trip.replaceEmergencyContacts(List.of(new EmergencyContact("Old Contact", "Friend", "+1-000")));
        long oldUpdatedUtc = trip.getUpdatedAt().toEpochMilli();
        when(trips.findByIdAndFamilyId(trip.getId(), family.getId())).thenReturn(Optional.of(trip));

        TravelPackageView result = tripService.updateTrip(family, trip.getId(), fullRequest("New Name"));

        assertEquals("New Name", result.travelerName());
        // The 2 old legs are gone; only the 1 new leg is left.
        assertEquals(1, result.legs().size());
        assertEquals("TK008", result.legs().get(0).flightNumber());
        assertEquals(1, result.emergencyContacts().size());
        assertEquals("Demo Caregiver", result.emergencyContacts().get(0).name());
        assertEquals(1, result.staffCards().size());
        // New children point to this trip (needed for the database).
        assertSame(trip, trip.getLegs().get(0).getTrip());
        // The phone uses this time to show "last known info".
        assertTrue(result.lastUpdatedUtc() >= oldUpdatedUtc);
    }

    @Test
    void updateTrip_ofAnotherFamily_is404AndChangesNothing() {
        when(trips.findByIdAndFamilyId("other-trip", family.getId())).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class,
                () -> tripService.updateTrip(family, "other-trip", fullRequest("New Name")));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verify(trips, never()).save(any());
    }

    // ---------- Helpers ----------

    private static TripRequest fullRequest(String travelerName) {
        return new TripRequest(
                travelerName,
                "tr",
                List.of(leg(1, "tk008")),
                List.of(new ContactView("Demo Caregiver", "Family / Caregiver", "+1-408-555-0199")),
                List.of(new StaffCardView(null, "TRANSFER",
                        "Help Finding Connection Gate",
                        "Aktarma Kapısını Bulma Yardımı",
                        "Please guide me to Gate {GATE} for flight {FLIGHT}.",
                        "Lütfen {FLIGHT} sefer sayılı uçuşum için {GATE} numaralı kapıya gitmeme yardım edin.")));
    }

    private static LegView leg(int order, String flightNumber) {
        return new LegView(order, flightNumber, "sfo", "ist", DEPARTURE, ARRIVAL, "I", "G101", null);
    }

    private static JourneyLeg legModel(int order, String flightNumber) {
        return new JourneyLeg(order, flightNumber, "SFO", "IST",
                Instant.ofEpochMilli(DEPARTURE), Instant.ofEpochMilli(ARRIVAL), "I", "G101", "SCHEDULED");
    }
}
