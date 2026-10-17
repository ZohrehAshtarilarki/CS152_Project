package com.cs152.safetycompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** MODEL: a family member (caregiver) account. */
@Entity
@Table(name = "families")
public class Family {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** Simple login token (one active session per family). */
    @Column(unique = true)
    private String sessionToken;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Family() {
        // for JPA
    }

    public Family(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getSessionToken() { return sessionToken; }
    public Instant getCreatedAt() { return createdAt; }

    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
}
