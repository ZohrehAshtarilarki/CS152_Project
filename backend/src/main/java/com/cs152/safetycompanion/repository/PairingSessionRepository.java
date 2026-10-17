package com.cs152.safetycompanion.repository;

import com.cs152.safetycompanion.model.PairingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PairingSessionRepository extends JpaRepository<PairingSession, String> {

    Optional<PairingSession> findFirstByCodeAndStatusAndExpiresAtAfter(
            String code, PairingSession.Status status, Instant now);

    boolean existsByCodeAndStatusAndExpiresAtAfter(String code, PairingSession.Status status, Instant now);

    Optional<PairingSession> findByDeviceTokenAndStatus(String deviceToken, PairingSession.Status status);
}
