package com.cs152.safetycompanion.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** MODEL: one trip, created by a family, with its flights, contacts and staff cards. */
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(nullable = false, length = 100)
    private String travelerName;

    @Column(nullable = false, length = 5)
    private String targetLanguage = "tr";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    /** Changes every time the family edits the trip. The traveler phone shows it as "last known info". */
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<JourneyLeg> legs = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffCard> staffCards = new ArrayList<>();

    protected Trip() {
        // for JPA
    }

    public Trip(Family family, String travelerName, String targetLanguage) {
        this.family = family;
        this.travelerName = travelerName;
        this.targetLanguage = targetLanguage;
    }

    public void replaceLegs(List<JourneyLeg> newLegs) {
        legs.clear();
        for (JourneyLeg leg : newLegs) {
            leg.setTrip(this);
            legs.add(leg);
        }
    }

    public void replaceEmergencyContacts(List<EmergencyContact> newContacts) {
        emergencyContacts.clear();
        for (EmergencyContact contact : newContacts) {
            contact.setTrip(this);
            emergencyContacts.add(contact);
        }
    }

    public void replaceStaffCards(List<StaffCard> newCards) {
        staffCards.clear();
        for (StaffCard card : newCards) {
            card.setTrip(this);
            staffCards.add(card);
        }
    }

    /** Call after every change so the traveler phone knows the data is newer. */
    public void touch() { this.updatedAt = Instant.now(); }

    public String getId() { return id; }
    public Family getFamily() { return family; }
    public String getTravelerName() { return travelerName; }
    public String getTargetLanguage() { return targetLanguage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<JourneyLeg> getLegs() { return legs; }
    public List<EmergencyContact> getEmergencyContacts() { return emergencyContacts; }
    public List<StaffCard> getStaffCards() { return staffCards; }

    public void setTravelerName(String travelerName) { this.travelerName = travelerName; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
}
