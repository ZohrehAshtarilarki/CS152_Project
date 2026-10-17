package com.cs152.safetycompanion.service;

import com.cs152.safetycompanion.model.Family;
import com.cs152.safetycompanion.repository.FamilyRepository;
import com.cs152.safetycompanion.view.AuthViews.AuthResponse;
import com.cs152.safetycompanion.view.AuthViews.LoginRequest;
import com.cs152.safetycompanion.view.AuthViews.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Family sign-up, login, and checking the family's token on each request. (Family side) */
@Service
public class AuthService {

    private final FamilyRepository families;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(FamilyRepository families) {
        this.families = families;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (families.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already registered.");
        }
        Family family = new Family(request.name().trim(), request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()));
        family.setSessionToken(Tokens.newToken());
        families.save(family);
        return new AuthResponse(family.getSessionToken(), family.getId(), family.getName());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Family family = families.findByEmailIgnoreCase(request.email().trim())
                .filter(f -> passwordEncoder.matches(request.password(), f.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password."));
        family.setSessionToken(Tokens.newToken());
        return new AuthResponse(family.getSessionToken(), family.getId(), family.getName());
    }

    /** Returns the signed-in family, or answers 401 if the token is missing or wrong. */
    @Transactional(readOnly = true)
    public Family requireFamily(String authorizationHeader) {
        String token = Tokens.fromHeader(authorizationHeader);
        return families.findBySessionToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
