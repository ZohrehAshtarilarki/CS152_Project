package com.cs152.safetycompanion.view;

/** VIEW: every error is sent as {"error": "..."}. */
public record ErrorView(String error) {
}
