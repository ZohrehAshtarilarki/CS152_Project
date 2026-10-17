package com.cs152.safetycompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * MODEL: a ready-made message the traveler shows to airport staff, in English and Turkish.
 * Messages may contain placeholders: {GATE}, {FLIGHT}, {PHONE}. The phone fills them in.
 */
@Entity
@Table(name = "staff_cards")
public class StaffCard {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    /** For example TRANSFER, HELP, GATE. */
    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 100)
    private String titleEn;

    @Column(nullable = false, length = 100)
    private String titleTr;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageEn;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageTr;

    protected StaffCard() {
        // for JPA
    }

    public StaffCard(String category, String titleEn, String titleTr, String messageEn, String messageTr) {
        this.category = category;
        this.titleEn = titleEn;
        this.titleTr = titleTr;
        this.messageEn = messageEn;
        this.messageTr = messageTr;
    }

    void setTrip(Trip trip) { this.trip = trip; }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getTitleEn() { return titleEn; }
    public String getTitleTr() { return titleTr; }
    public String getMessageEn() { return messageEn; }
    public String getMessageTr() { return messageTr; }
}
