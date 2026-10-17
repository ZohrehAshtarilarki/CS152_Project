package com.cs152.safetycompanion.repository;

import com.cs152.safetycompanion.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, String> {

    List<Trip> findByFamilyIdOrderByCreatedAtDesc(String familyId);

    /** Finds a trip only if it belongs to this family (so families can't see each other's trips). */
    Optional<Trip> findByIdAndFamilyId(String id, String familyId);
}
