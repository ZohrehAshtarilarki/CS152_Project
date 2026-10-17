package com.cs152.safetycompanion.view;

import com.cs152.safetycompanion.model.EmergencyContact;
import com.cs152.safetycompanion.model.JourneyLeg;
import com.cs152.safetycompanion.model.StaffCard;
import com.cs152.safetycompanion.model.Trip;
import com.cs152.safetycompanion.view.TripViews.ContactView;
import com.cs152.safetycompanion.view.TripViews.LegView;
import com.cs152.safetycompanion.view.TripViews.StaffCardView;
import com.cs152.safetycompanion.view.TripViews.TravelPackageView;

import java.time.Instant;

/** Converts between Model (database objects) and View (JSON shapes). */
public final class TripMapper {

    private TripMapper() {
    }

    public static TravelPackageView toPackage(Trip trip) {
        return new TravelPackageView(
                trip.getId(),
                trip.getTravelerName(),
                trip.getTargetLanguage(),
                trip.getUpdatedAt().toEpochMilli(),
                trip.getLegs().stream().map(TripMapper::toView).toList(),
                trip.getEmergencyContacts().stream().map(TripMapper::toView).toList(),
                trip.getStaffCards().stream().map(TripMapper::toView).toList());
    }

    public static LegView toView(JourneyLeg leg) {
        return new LegView(
                leg.getSequenceOrder(),
                leg.getFlightNumber(),
                leg.getOriginAirport(),
                leg.getDestinationAirport(),
                leg.getDepartureTime().toEpochMilli(),
                leg.getArrivalTime().toEpochMilli(),
                leg.getTerminal(),
                leg.getGate(),
                leg.getStatus());
    }

    public static ContactView toView(EmergencyContact contact) {
        return new ContactView(contact.getName(), contact.getRelation(), contact.getPhoneNumber());
    }

    public static StaffCardView toView(StaffCard card) {
        return new StaffCardView(card.getId(), card.getCategory(), card.getTitleEn(), card.getTitleTr(),
                card.getMessageEn(), card.getMessageTr());
    }

    public static JourneyLeg toModel(LegView view) {
        return new JourneyLeg(
                view.sequenceOrder(),
                view.flightNumber().trim().toUpperCase(),
                view.originAirport().trim().toUpperCase(),
                view.destinationAirport().trim().toUpperCase(),
                Instant.ofEpochMilli(view.departureTimeUtc()),
                Instant.ofEpochMilli(view.arrivalTimeUtc()),
                view.terminal(),
                view.gate(),
                view.status() == null ? "SCHEDULED" : view.status());
    }

    public static EmergencyContact toModel(ContactView view) {
        return new EmergencyContact(view.name(), view.relation(), view.phoneNumber());
    }

    public static StaffCard toModel(StaffCardView view) {
        return new StaffCard(view.category(), view.titleEn(), view.titleTr(), view.messageEn(), view.messageTr());
    }
}
