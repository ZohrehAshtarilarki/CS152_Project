package com.cs152.safetycompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** MODEL: one flight of a trip (for example SFO -> IST). All times are UTC. */
@Entity
@Table(name = "journey_legs")
public class JourneyLeg {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false)
    private int sequenceOrder;

    @Column(nullable = false, length = 10)
    private String flightNumber;

    @Column(nullable = false, length = 3)
    private String originAirport;

    @Column(nullable = false, length = 3)
    private String destinationAirport;

    @Column(nullable = false)
    private Instant departureTime;

    @Column(nullable = false)
    private Instant arrivalTime;

    @Column(length = 10)
    private String terminal;

    @Column(length = 10)
    private String gate;

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    protected JourneyLeg() {
        // for JPA
    }

    public JourneyLeg(int sequenceOrder, String flightNumber, String originAirport, String destinationAirport,
                      Instant departureTime, Instant arrivalTime, String terminal, String gate, String status) {
        this.sequenceOrder = sequenceOrder;
        this.flightNumber = flightNumber;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.terminal = terminal;
        this.gate = gate;
        this.status = status;
    }

    void setTrip(Trip trip) { this.trip = trip; }

    public void reschedule(Instant departureTime, Instant arrivalTime) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getId() { return id; }
    public Trip getTrip() { return trip; }
    public int getSequenceOrder() { return sequenceOrder; }
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirport() { return originAirport; }
    public String getDestinationAirport() { return destinationAirport; }
    public Instant getDepartureTime() { return departureTime; }
    public Instant getArrivalTime() { return arrivalTime; }
    public String getTerminal() { return terminal; }
    public String getGate() { return gate; }
    public String getStatus() { return status; }
}
