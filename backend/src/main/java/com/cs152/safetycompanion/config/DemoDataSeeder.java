package com.cs152.safetycompanion.config;

import com.cs152.safetycompanion.model.EmergencyContact;
import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.model.JourneyLeg;
import com.cs152.safetycompanion.model.StaffCard;
import com.cs152.safetycompanion.model.Trip;
import com.cs152.safetycompanion.repository.FamilyRepository;
import com.cs152.safetycompanion.repository.TripRepository;
import com.cs152.safetycompanion.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Creates a demo family and the demo trip SFO -> IST -> ESB, so both teammates can test right away.
 * Login: demo@cs152.dev / demo1234
 *
 * Flight times are moved to "now" on every start, so the demo trip is always active.
 * Turn off with app.seed-demo-data=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    public static final String DEMO_EMAIL = "demo@cs152.dev";
    public static final String DEMO_PASSWORD = "demo1234";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final FamilyRepository families;
    private final TripRepository trips;
    private final AuthService authService;

    public DemoDataSeeder(FamilyRepository families, TripRepository trips, AuthService authService) {
        this.families = families;
        this.trips = trips;
        this.authService = authService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        Family existing = families.findByEmailIgnoreCase(DEMO_EMAIL).orElse(null);
        if (existing != null) {
            for (Trip trip : trips.findByFamilyIdOrderByCreatedAtDesc(existing.getId())) {
                List<JourneyLeg> legs = trip.getLegs();
                if (legs.size() >= 1) legs.get(0).reschedule(now.plus(Duration.ofHours(2)), now.plus(Duration.ofHours(15)));
                if (legs.size() >= 2) legs.get(1).reschedule(now.plus(Duration.ofHours(18)), now.plus(Duration.ofMinutes(19 * 60 + 15)));
                trip.touch();
            }
            log.info("Demo trip times moved to now. Login: {} / {}", DEMO_EMAIL, DEMO_PASSWORD);
            return;
        }

        Family family = families.save(new Family("Demo Family", DEMO_EMAIL, authService.hashPassword(DEMO_PASSWORD)));

        Trip trip = new Trip(family, "Ayşe Yılmaz", "tr");
        trip.replaceLegs(List.of(
                new JourneyLeg(1, "TK008", "SFO", "IST",
                        now.plus(Duration.ofHours(2)), now.plus(Duration.ofHours(15)), "I", "G101", "SCHEDULED"),
                new JourneyLeg(2, "TK2108", "IST", "ESB",
                        now.plus(Duration.ofHours(18)), now.plus(Duration.ofMinutes(19 * 60 + 15)), "D", "112", "SCHEDULED")));
        trip.replaceEmergencyContacts(List.of(
                new EmergencyContact("Demo Caregiver", "Family / Caregiver", "+1-408-555-0199")));
        trip.replaceStaffCards(List.of(
                new StaffCard("TRANSFER",
                        "Help Finding Connection Gate",
                        "Aktarma Kapısını Bulma Yardımı",
                        "I do not speak English well. Please guide me to Gate {GATE} for flight {FLIGHT}.",
                        "İngilizce iyi konuşamıyorum. Lütfen {FLIGHT} sefer sayılı uçuşum için {GATE} numaralı kapıya gitmeme yardım edin."),
                new StaffCard("HELP",
                        "I Need Help",
                        "Yardıma İhtiyacım Var",
                        "I am traveling alone and I need help. Please call my family: {PHONE}.",
                        "Yalnız seyahat ediyorum ve yardıma ihtiyacım var. Lütfen ailemi arayın: {PHONE}.")));
        trips.save(trip);

        log.info("Demo data created. Login: {} / {}", DEMO_EMAIL, DEMO_PASSWORD);
    }
}
