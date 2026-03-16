package com.aerofocus.app.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.data.repository.AeroFocusRepository
import com.aerofocus.app.service.AmbientAudioPlayer
import com.aerofocus.app.service.FlightTimerService
import com.aerofocus.app.service.TimerState
import com.aerofocus.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel controlling the active timer session and ambient audio.
 *
 * ## Responsibilities
 * - Starts / pauses / resumes / stops the [FlightTimerService].
 * - Observes the service's companion [StateFlow]s for reactive UI.
 * - Manages the [AmbientAudioPlayer] for focus audio.
 * - Records the session to Room upon completion or cancellation.
 * - Triggers destination unlocks when new mile thresholds are crossed.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    application: Application,
    private val repository: AeroFocusRepository,
    val audioPlayer: AmbientAudioPlayer
) : AndroidViewModel(application) {

    // ── Timer State (delegated to service companions) ───────────────

    /** Current state of the timer: IDLE, RUNNING, PAUSED, COMPLETED, STOPPED. */
    val timerState: StateFlow<TimerState> = FlightTimerService.timerStateFlow

    /** Remaining time in milliseconds. */
    val remainingTimeMillis: StateFlow<Long> = FlightTimerService.remainingTimeFlow

    /** Total duration in milliseconds. */
    val totalDurationMillis: StateFlow<Long> = FlightTimerService.totalDurationFlow

    /** Destination name for the current flight. */
    val destinationName: StateFlow<String> = FlightTimerService.destinationNameFlow

    // ── Audio State ─────────────────────────────────────────────────

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _audioVolume = MutableStateFlow(0.8f)
    val audioVolume: StateFlow<Float> = _audioVolume.asStateFlow()

    private val _activeTrackName = MutableStateFlow<String?>(null)
    val activeTrackName: StateFlow<String?> = _activeTrackName.asStateFlow()

    // ── Flight metadata (set before starting) ───────────────────────

    private var currentDurationMinutes: Int = 0
    private var currentDestinationName: String = ""
    private var currentFocusTag: String = ""
    private var sessionStartTime: Long = 0L

    // ── Timer Control ───────────────────────────────────────────────

    /**
     * Begin a focus flight. Launches the foreground service.
     */
    fun startFlight(
        durationMinutes: Int,
        destinationName: String,
        focusTag: String,
        isStrictMode: Boolean
    ) {
        currentDurationMinutes = durationMinutes
        currentDestinationName = destinationName
        currentFocusTag = focusTag
        sessionStartTime = System.currentTimeMillis()

        val intent = Intent(getApplication(), FlightTimerService::class.java).apply {
            action = Constants.ACTION_START
            putExtra(Constants.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(Constants.EXTRA_DESTINATION_NAME, destinationName)
            putExtra(Constants.EXTRA_FOCUS_TAG, focusTag)
            putExtra(Constants.EXTRA_STRICT_MODE, isStrictMode)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    /** Pause the active flight. */
    fun pauseFlight() {
        sendServiceAction(Constants.ACTION_PAUSE)
    }

    /** Resume a paused flight. */
    fun resumeFlight() {
        sendServiceAction(Constants.ACTION_RESUME)
    }

    /**
     * Emergency landing — cancel the flight early.
     * Records the session as failed (no miles earned).
     */
    fun emergencyLand() {
        sendServiceAction(Constants.ACTION_STOP)
        audioPlayer.stop()
        _isAudioPlaying.value = false
        _activeTrackName.value = null

        // Record failed session
        viewModelScope.launch {
            repository.insertSession(
                FocusSessionEntity(
                    startTime = sessionStartTime,
                    durationMinutes = currentDurationMinutes,
                    focusTag = currentFocusTag,
                    wasCompleted = false,
                    earnedMiles = 0
                )
            )
        }
    }

    /**
     * Called when the timer reaches zero (COMPLETED state).
     * Records the session as successful and triggers destination unlocks.
     *
     * @return The earned miles for the Arrival screen.
     */
    fun recordSuccessfulLanding(): Int {
        val earnedMiles = currentDurationMinutes * Constants.MILES_PER_MINUTE

        viewModelScope.launch {
            // 1. Record the successful session
            repository.insertSession(
                FocusSessionEntity(
                    startTime = sessionStartTime,
                    durationMinutes = currentDurationMinutes,
                    focusTag = currentFocusTag,
                    wasCompleted = true,
                    earnedMiles = earnedMiles
                )
            )

            // 2. Collect the updated total miles (now includes this session)
            //    and unlock all destinations whose threshold has been crossed.
            repository.getTotalMilesFlown().collect { updatedTotal ->
                repository.unlockEligibleDestinations(updatedTotal)
                return@collect  // Only need the first emission after insert
            }
        }

        // Stop the service
        sendServiceAction(Constants.ACTION_STOP)
        audioPlayer.stop()
        _isAudioPlaying.value = false
        _activeTrackName.value = null

        return earnedMiles
    }

    // ── Audio Control ───────────────────────────────────────────────

    /**
     * Toggle an ambient audio track on/off.
     *
     * @param rawResId The raw resource ID (e.g., `R.raw.cabin_noise`).
     * @param trackName Display name for the UI (e.g., "Cabin").
     */
    fun toggleAudioTrack(rawResId: Int, trackName: String) {
        if (audioPlayer.isPlaying() && audioPlayer.getCurrentTrackId() == rawResId) {
            // Same track playing → stop
            audioPlayer.stop()
            _isAudioPlaying.value = false
            _activeTrackName.value = null
        } else {
            // Different track or nothing playing → switch
            audioPlayer.play(rawResId)
            _isAudioPlaying.value = true
            _activeTrackName.value = trackName
        }
    }

    /** Adjust the ambient audio volume. */
    fun setAudioVolume(volume: Float) {
        _audioVolume.value = volume.coerceIn(0f, 1f)
        audioPlayer.setVolume(_audioVolume.value)
    }

    // ── Utility ─────────────────────────────────────────────────────

    /** Get the progress fraction (0.0 to 1.0) for UI progress indicators. */
    fun getProgressFraction(): Float = FlightTimerService.getProgressFraction()

    /** Format the remaining time as a display string. */
    fun getFormattedRemainingTime(): String =
        FlightTimerService.formatMillisToHHMMSS(remainingTimeMillis.value)

    private fun sendServiceAction(action: String) {
        val intent = Intent(getApplication(), FlightTimerService::class.java).apply {
            this.action = action
        }
        getApplication<Application>().startService(intent)
    }

    override fun onCleared() {
        // Don't stop the service on ViewModel clear — the foreground service
        // is intentionally independent of the ViewModel lifecycle.
        super.onCleared()
    }
}
