package com.cs152.safetycompanion.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Example unit test. Run with: ./mvnw test */
class TokensTest {

    @Test
    void pairingCodeIsAlwaysSixDigits() {
        for (int i = 0; i < 1000; i++) {
            String code = Tokens.newPairingCode();
            assertTrue(code.matches("\\d{6}"), "bad code: " + code);
        }
    }

    @Test
    void tokensAreRandom() {
        assertNotEquals(Tokens.newToken(), Tokens.newToken());
    }

    @Test
    void readsBearerHeader() {
        assertEquals("abc123", Tokens.fromHeader("Bearer abc123"));
        assertThrows(ApiException.class, () -> Tokens.fromHeader(null));
        assertThrows(ApiException.class, () -> Tokens.fromHeader("abc123"));
    }
}
