package com.cs152.safetycompanion.service;

import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.model.Trip;
import com.cs152.safetycompanion.repository.TripRepository;
import com.cs152.safetycompanion.view.TripMapper;
import com.cs152.safetycompanion.view.TripViews.TravelPackageView;
import com.cs152.safetycompanion.view.TripViews.TripRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Creating, reading and changing trips. (Family side, plus the package the traveler downloads) */
@Service
public class TripService {

    private final TripRepository trips;

    public TripService(TripRepository trips) {
        this.trips = trips;
    }

    @Transactional(readOnly = true)
    public List<TravelPackageView> listTrips(Family family) {
        return trips.findByFamilyIdOrderByCreatedAtDesc(family.getId()).stream()
                .map(TripMapper::toPackage)
                .toList();
    }

    @Transactional
    public TravelPackageView createTrip(Family family, TripRequest request) {
        Trip trip = new Trip(family, request.travelerName().trim(), languageOrDefault(request.targetLanguage()));
        applyRequest(trip, request);
        return TripMapper.toPackage(trips.save(trip));
    }

    @Transactional(readOnly = true)
    public TravelPackageView getTrip(Family family, String tripId) {
        return TripMapper.toPackage(findOwnedTrip(family, tripId));
    }

    /** Replaces the whole trip (simple and safe: the request always has the full trip). */
    @Transactional
    public TravelPackageView updateTrip(Family family, String tripId, TripRequest request) {
        Trip trip = findOwnedTrip(family, tripId);
        trip.setTravelerName(request.travelerName().trim());
        trip.setTargetLanguage(languageOrDefault(request.targetLanguage()));
        applyRequest(trip, request);
        trip.touch();
        return TripMapper.toPackage(trip);
    }

    /** The full trip for a paired traveler phone. */
    @Transactional(readOnly = true)
    public TravelPackageView buildTravelPackage(String tripId) {
        return trips.findById(tripId)
                .map(TripMapper::toPackage)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found."));
    }

    /** Finds a trip only if it belongs to this family. */
    @Transactional(readOnly = true)
    public Trip findOwnedTrip(Family family, String tripId) {
        return trips.findByIdAndFamilyId(tripId, family.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found."));
    }

    private void applyRequest(Trip trip, TripRequest request) {
        trip.replaceLegs(request.legs().stream().map(TripMapper::toModel).toList());
        trip.replaceEmergencyContacts(orEmpty(request.emergencyContacts()).stream().map(TripMapper::toModel).toList());
        trip.replaceStaffCards(orEmpty(request.staffCards()).stream().map(TripMapper::toModel).toList());
    }

    private static String languageOrDefault(String language) {
        return (language == null || language.isBlank()) ? "tr" : language;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
