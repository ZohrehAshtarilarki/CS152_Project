package com.cs152.safetycompanion.repository;

import com.cs152.safetycompanion.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, String> {

    Optional<Family> findByEmailIgnoreCase(String email);

    Optional<Family> findBySessionToken(String sessionToken);

    boolean existsByEmailIgnoreCase(String email);
}
