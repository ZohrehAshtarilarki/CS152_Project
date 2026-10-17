package com.cs152.safetycompanion.controller;

import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.service.AuthService;
import com.cs152.safetycompanion.service.PairingService;
import com.cs152.safetycompanion.service.TripService;
import com.cs152.safetycompanion.view.PairingViews.PairRequest;
import com.cs152.safetycompanion.view.PairingViews.PairingStatusResponse;
import com.cs152.safetycompanion.view.TripViews.TravelPackageView;
import com.cs152.safetycompanion.view.TripViews.TripRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CONTROLLER: the family's trips, and pairing a traveler phone to a trip. (Family side) */
@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final AuthService authService;
    private final TripService tripService;
    private final PairingService pairingService;

    public TripController(AuthService authService, TripService tripService, PairingService pairingService) {
        this.authService = authService;
        this.tripService = tripService;
        this.pairingService = pairingService;
    }

    @GetMapping
    public List<TravelPackageView> listTrips(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        return tripService.listTrips(authService.requireFamily(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TravelPackageView createTrip(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @Valid @RequestBody TripRequest request) {
        return tripService.createTrip(authService.requireFamily(auth), request);
    }

    @GetMapping("/{tripId}")
    public TravelPackageView getTrip(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable String tripId) {
        return tripService.getTrip(authService.requireFamily(auth), tripId);
    }

    @PutMapping("/{tripId}")
    public TravelPackageView updateTrip(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable String tripId,
            @Valid @RequestBody TripRequest request) {
        return tripService.updateTrip(authService.requireFamily(auth), tripId, request);
    }

    /** The family types the 6-digit code the traveler read aloud. */
    @PostMapping("/{tripId}/pair")
    public PairingStatusResponse pairTraveler(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable String tripId,
            @Valid @RequestBody PairRequest request) {
        Family family = authService.requireFamily(auth);
        return pairingService.confirm(family, tripId, request.code());
    }
}
