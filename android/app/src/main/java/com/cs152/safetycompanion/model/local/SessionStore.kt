package com.cs152.safetycompanion.model.local

import android.content.Context

enum class AppMode { FAMILY, TRAVELER }

/**
 * MODEL: small saved settings (mode, tokens).
 * Stored in plain SharedPreferences for this version.
 */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    /** Picked once at first launch. null = not picked yet. */
    var appMode: AppMode?
        get() = prefs.getString(KEY_MODE, null)?.let { AppMode.valueOf(it) }
        set(value) { prefs.edit().putString(KEY_MODE, value?.name).apply() }

    // Family side
    var familyToken: String?
        get() = prefs.getString(KEY_FAMILY_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_FAMILY_TOKEN, value).apply() }

    var familyName: String?
        get() = prefs.getString(KEY_FAMILY_NAME, null)
        set(value) { prefs.edit().putString(KEY_FAMILY_NAME, value).apply() }

    // Traveler side
    var pairingId: String?
        get() = prefs.getString(KEY_PAIRING_ID, null)
        set(value) { prefs.edit().putString(KEY_PAIRING_ID, value).apply() }

    var deviceToken: String?
        get() = prefs.getString(KEY_DEVICE_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_DEVICE_TOKEN, value).apply() }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_MODE = "app_mode"
        const val KEY_FAMILY_TOKEN = "family_token"
        const val KEY_FAMILY_NAME = "family_name"
        const val KEY_PAIRING_ID = "pairing_id"
        const val KEY_DEVICE_TOKEN = "device_token"
    }
}
