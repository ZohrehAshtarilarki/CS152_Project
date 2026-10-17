package com.cs152.safetycompanion.service;

import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.util.Base64;

/** Random tokens and pairing codes. */
final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Tokens() {
    }

    /** Long random secret (family session token or traveler device token). */
    static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 6 random digits, for example "048213". */
    static String newPairingCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /** Reads the token from an "Authorization: Bearer <token>" header. */
    static String fromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }
}
