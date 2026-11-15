package com.cs152.safetycompanion.controller

import androidx.lifecycle.ViewModel
import com.cs152.safetycompanion.model.local.AppMode
import com.cs152.safetycompanion.model.local.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** CONTROLLER (shared): which mode this phone is in. Picked once at first launch. */
class AppController(private val session: SessionStore) : ViewModel() {

    private val _mode = MutableStateFlow(session.appMode)
    val mode: StateFlow<AppMode?> = _mode.asStateFlow()

    fun chooseMode(mode: AppMode) {
        session.appMode = mode
        _mode.value = mode
    }

    /**
     * Go back to the "who are you?" screen.
     * In Traveler Mode, requires the family PIN (default: 1234) so the traveler can't leave by mistake.
     */
    fun resetMode(pin: String? = null): Boolean {
        if (_mode.value == AppMode.TRAVELER) {
            // Keep it simple: use a hardcoded demo PIN for now.
            if (pin != "1234") {
                return false
            }
        }
        session.clear()
        _mode.value = null
        return true
    }
}
