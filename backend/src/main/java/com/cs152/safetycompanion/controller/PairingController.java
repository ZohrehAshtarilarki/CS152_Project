package com.cs152.safetycompanion.controller;

import com.cs152.safetycompanion.service.PairingService;
import com.cs152.safetycompanion.view.PairingViews.PairingStartResponse;
import com.cs152.safetycompanion.view.PairingViews.PairingStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CONTROLLER: the traveler phone's side of pairing. No login needed. (Family side owns this file) */
@RestController
@RequestMapping("/api/pairing")
public class PairingController {

    private final PairingService pairingService;

    public PairingController(PairingService pairingService) {
        this.pairingService = pairingService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public PairingStartResponse start() {
        return pairingService.start();
    }

    @GetMapping("/{pairingId}")
    public PairingStatusResponse status(@PathVariable String pairingId) {
        return pairingService.status(pairingId);
    }
}
