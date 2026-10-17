package com.cs152.safetycompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** MODEL: a person the traveler can call for help. */
@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String relation;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    protected EmergencyContact() {
        // for JPA
    }

    public EmergencyContact(String name, String relation, String phoneNumber) {
        this.name = name;
        this.relation = relation;
        this.phoneNumber = phoneNumber;
    }

    void setTrip(Trip trip) { this.trip = trip; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRelation() { return relation; }
    public String getPhoneNumber() { return phoneNumber; }
}
