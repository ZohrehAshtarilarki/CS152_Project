package com.cs152.safetycompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * MODEL: one pairing attempt of a traveler's phone.
 *
 * 1. The traveler phone starts it and gets a 6-digit code + a secret pairing id.
 * 2. The family types the code -> status becomes CONFIRMED and a device token is created.
 * 3. The traveler phone asks for the status with its secret id and receives the device token.
 */
@Entity
@Table(name = "pairing_sessions")
public class PairingSession {

    public enum Status { WAITING, CONFIRMED }

    /** Secret: only the traveler phone knows this id. */
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.WAITING;

    @Column(nullable = false)
    private Instant expiresAt;

    /** Set when the family confirms the code. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    /** The traveler phone's long-term "password". Set when confirmed. */
    @Column(unique = true)
    private String deviceToken;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected PairingSession() {
        // for JPA
    }

    public PairingSession(String code, Instant expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return status == Status.WAITING && now.isAfter(expiresAt);
    }

    public void confirm(Trip trip, String deviceToken) {
        this.trip = trip;
        this.deviceToken = deviceToken;
        this.status = Status.CONFIRMED;
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public Status getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Trip getTrip() { return trip; }
    public String getDeviceToken() { return deviceToken; }
    public Instant getCreatedAt() { return createdAt; }
}
