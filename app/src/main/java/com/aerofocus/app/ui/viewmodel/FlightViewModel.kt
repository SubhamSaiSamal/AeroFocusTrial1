package com.aerofocus.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity
import com.aerofocus.app.data.repository.AeroFocusRepository
import com.aerofocus.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Shared ViewModel holding the user's current flight configuration.
 *
 * Scoped to the navigation graph so data persists across screens
 * (Pre-Flight → In-Flight → Arrival) within a single flight booking.
 *
 * ## Responsibilities
 * - Stores selected duration, destination, and focus tag.
 * - Provides reactive total miles and destination lists from the database.
 * - Resets flight data after a session concludes.
 */
@HiltViewModel
class FlightViewModel @Inject constructor(
    private val repository: AeroFocusRepository
) : ViewModel() {

    // ── Reactive DB Data ────────────────────────────────────────────

    /** Total accumulated miles across all completed sessions. */
    val totalMiles: StateFlow<Int> = repository.getTotalMilesFlown()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** All destinations (locked + unlocked) for the map. */
    val allDestinations: StateFlow<List<UnlockedDestinationEntity>> =
        repository.getAllDestinations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Only unlocked destinations. */
    val unlockedDestinations: StateFlow<List<UnlockedDestinationEntity>> =
        repository.getUnlockedDestinations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Count of completed flights. */
    val completedFlights: StateFlow<Int> = repository.getCompletedFlightCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Total focus minutes from completed sessions. */
    val totalFocusMinutes: StateFlow<Int> = repository.getTotalFocusMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── User-Selected Flight Configuration ──────────────────────────

    private val _selectedDuration = MutableStateFlow(Constants.DEFAULT_FOCUS_MINUTES)
    val selectedDuration: StateFlow<Int> = _selectedDuration.asStateFlow()

    private val _selectedDestination = MutableStateFlow<UnlockedDestinationEntity?>(null)
    val selectedDestination: StateFlow<UnlockedDestinationEntity?> =
        _selectedDestination.asStateFlow()

    private val _selectedFocusTag = MutableStateFlow("Study")
    val selectedFocusTag: StateFlow<String> = _selectedFocusTag.asStateFlow()

    // ── Departure city (fixed as first unlocked destination) ────────

    private val _departureCity = MutableStateFlow("DEL")
    val departureCity: StateFlow<String> = _departureCity.asStateFlow()

    // ── Setters ─────────────────────────────────────────────────────

    fun setDuration(minutes: Int) {
        _selectedDuration.value = minutes.coerceIn(
            Constants.MIN_FOCUS_MINUTES,
            Constants.MAX_FOCUS_MINUTES
        )
    }

    fun setDestination(destination: UnlockedDestinationEntity) {
        _selectedDestination.value = destination
    }

    fun setFocusTag(tag: String) {
        _selectedFocusTag.value = tag
    }

    /**
     * Reset flight configuration after a session completes or is cancelled.
     * Does NOT reset total miles or destinations — those are persistent.
     */
    fun resetFlightConfig() {
        _selectedDuration.value = Constants.DEFAULT_FOCUS_MINUTES
        _selectedDestination.value = null
        _selectedFocusTag.value = "Study"
    }
}
