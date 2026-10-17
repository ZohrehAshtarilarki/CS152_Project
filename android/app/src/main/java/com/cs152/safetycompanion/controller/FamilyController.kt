package com.cs152.safetycompanion.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.model.remote.TripRequest
import com.cs152.safetycompanion.model.repository.AuthRepository
import com.cs152.safetycompanion.model.repository.FamilyTripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the Family screens need to draw themselves. */
data class FamilyUiState(
    val isLoggedIn: Boolean = false,
    val familyName: String? = null,
    val trips: List<TravelPackage> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

/**
 * CONTROLLER (Family side).
 * Screens call these functions (user actions); this class asks the Model and updates uiState;
 * the screens redraw automatically when uiState changes.
 */
class FamilyController(
    private val authRepository: AuthRepository,
    private val tripRepository: FamilyTripRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FamilyUiState(isLoggedIn = authRepository.isLoggedIn, familyName = authRepository.familyName)
    )
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        if (authRepository.isLoggedIn) loadTrips()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            authRepository.login(email, password)
                .onSuccess {
                    _uiState.update { state -> state.copy(isLoggedIn = true, familyName = authRepository.familyName) }
                    refreshTrips()
                }
                .onFailure { showMessage("Sign in failed. Check your email and password.") }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            authRepository.register(name, email, password)
                .onSuccess {
                    _uiState.update { state -> state.copy(isLoggedIn = true, familyName = authRepository.familyName) }
                    refreshTrips()
                }
                .onFailure { showMessage("Could not create the account. Password needs 6+ characters.") }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = FamilyUiState()
    }

    fun loadTrips() {
        viewModelScope.launch { refreshTrips() }
    }

    /** Create (tripId == null) or replace a trip. Call this from the trip editor. */
    fun saveTrip(tripId: String?, request: TripRequest, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val result = if (tripId == null) {
                tripRepository.createTrip(request)
            } else {
                tripRepository.updateTrip(tripId, request)
            }
            result
                .onSuccess {
                    refreshTrips()
                    onSaved()
                }
                .onFailure { showMessage("Could not save the trip.") }
        }
    }

    fun pairTraveler(tripId: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            tripRepository.pairTraveler(tripId, code)
                .onSuccess { showMessage("Paired! The traveler's phone is now downloading the trip.") }
                .onFailure { showMessage("The code is wrong or expired. Ask the traveler for a new code.") }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private suspend fun refreshTrips() {
        _uiState.update { it.copy(isLoading = true) }
        tripRepository.getTrips()
            .onSuccess { trips -> _uiState.update { it.copy(trips = trips) } }
            .onFailure { showMessage("Could not load trips. Is the backend running?") }
        _uiState.update { it.copy(isLoading = false) }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }
}
