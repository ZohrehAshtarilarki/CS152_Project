package com.cs152.safetycompanion.controller;

import com.cs152.safetycompanion.service.PairingService;
import com.cs152.safetycompanion.service.TripService;
import com.cs152.safetycompanion.view.TripViews.TravelPackageView;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CONTROLLER: the paired traveler phone downloads its whole trip here. */
@RestController
@RequestMapping("/api/traveler")
public class TravelerController {

    private final PairingService pairingService;
    private final TripService tripService;

    public TravelerController(PairingService pairingService, TripService tripService) {
        this.pairingService = pairingService;
        this.tripService = tripService;
    }

    /** Header: "Authorization: Bearer <deviceToken>". */
    @GetMapping("/package")
    public TravelPackageView getTravelPackage(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String tripId = pairingService.requireDeviceTripId(auth);
        return tripService.buildTravelPackage(tripId);
    }
}
