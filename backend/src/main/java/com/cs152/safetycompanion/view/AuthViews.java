package com.cs152.safetycompanion.view;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** VIEW: JSON shapes for family sign-up and login. */
public final class AuthViews {

    private AuthViews() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password) {
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password) {
    }

    /** "token" must be sent back as "Authorization: Bearer <token>" on family requests. */
    public record AuthResponse(String token, String familyId, String name) {
    }
}
