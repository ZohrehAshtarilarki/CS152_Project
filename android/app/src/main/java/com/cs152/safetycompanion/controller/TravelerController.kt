package com.cs152.safetycompanion.controller

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs152.safetycompanion.model.TravelPackage
import com.cs152.safetycompanion.model.logic.JourneyStage
import com.cs152.safetycompanion.model.logic.JourneyStageResolver
import com.cs152.safetycompanion.model.logic.SignGuidance
import com.cs152.safetycompanion.model.logic.SignReader
import com.cs152.safetycompanion.model.logic.SignRules
import com.cs152.safetycompanion.model.repository.PairingRepository
import com.cs152.safetycompanion.model.repository.SyncResult
import com.cs152.safetycompanion.model.repository.TravelPackageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PairingStatus { NOT_STARTED, WAITING, EXPIRED, ERROR }

/** Everything the Traveler screens need to draw themselves. */
data class TravelerUiState(
    val isPaired: Boolean = false,
    val pairingCode: String? = null,
    val pairingStatus: PairingStatus = PairingStatus.NOT_STARTED,
    val trip: TravelPackage? = null,
    val lastSyncedUtc: Long? = null,
    val isOffline: Boolean = false,
    val stage: JourneyStage = JourneyStage.NoTrip,
    val isReadingSign: Boolean = false,
    val signGuidance: SignGuidance? = null,
)

/**
 * CONTROLLER (Traveler side).
 * The trip always comes from the phone database (Room), so every screen works offline.
 */
class TravelerController(
    private val pairingRepository: PairingRepository,
    private val travelPackageRepository: TravelPackageRepository,
    private val signReader: SignReader = SignReader(),
    private val signRules: SignRules = SignRules(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelerUiState(isPaired = pairingRepository.isPaired))
    val uiState: StateFlow<TravelerUiState> = _uiState.asStateFlow()

    private var pairingJob: Job? = null

    init {
        // Offline-first: always show what is saved on the phone.
        viewModelScope.launch {
            travelPackageRepository.savedTrip.collect { saved ->
                _uiState.update { state ->
                    state.copy(
                        trip = saved?.travelPackage,
                        lastSyncedUtc = saved?.lastSyncedUtc,
                        stage = JourneyStageResolver.resolve(
                            System.currentTimeMillis(),
                            saved?.travelPackage?.legs.orEmpty(),
                        ),
                    )
                }
            }
        }
        // Then try to get newer data (fails quietly with no internet).
        if (pairingRepository.isPaired) sync()
    }

    // ---------------- Pairing ----------------

    /** Get a 6-digit code, then check every 3 seconds until the family types it in. */
    fun startPairing() {
        pairingJob?.cancel()
        pairingJob = viewModelScope.launch {
            pairingRepository.start()
                .onSuccess { start ->
                    _uiState.update { it.copy(pairingCode = start.code, pairingStatus = PairingStatus.WAITING) }
                }
                .onFailure {
                    _uiState.update { it.copy(pairingStatus = PairingStatus.ERROR) }
                    return@launch
                }

            while (isActive) {
                delay(3_000)
                val status = pairingRepository.checkStatus().getOrNull() ?: continue
                when (status.status) {
                    "CONFIRMED" -> {
                        _uiState.update { it.copy(isPaired = true) }
                        sync()
                        break
                    }
                    "EXPIRED" -> {
                        _uiState.update { it.copy(pairingStatus = PairingStatus.EXPIRED) }
                        break
                    }
                }
            }
        }
    }

    // ---------------- Trip data ----------------

    /** Download the newest trip. With no internet, the saved trip stays on screen. */
    fun sync() {
        viewModelScope.launch {
            val result = travelPackageRepository.sync()
            _uiState.update { it.copy(isOffline = result !is SyncResult.Success) }
        }
    }

    /** The clock moves on, so recompute the trip step when a screen opens. */
    fun refreshStage() {
        _uiState.update {
            it.copy(stage = JourneyStageResolver.resolve(System.currentTimeMillis(), it.trip?.legs.orEmpty()))
        }
    }

    // ---------------- Read a Sign ----------------

    fun readSign(photo: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReadingSign = true, signGuidance = null) }
            val text = signReader.readText(photo)
            val guidance = signRules.evaluate(text, _uiState.value.stage)
            _uiState.update { it.copy(isReadingSign = false, signGuidance = guidance) }
        }
    }

    fun clearSign() {
        _uiState.update { it.copy(signGuidance = null) }
    }
}
