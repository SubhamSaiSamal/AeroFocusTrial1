package com.aerofocus.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.data.repository.AeroFocusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel for the Logbook / Flight History screen.
 *
 * Exposes a reactive list of all past focus sessions from the database.
 * The UI observes this as a [Flow] — no manual refresh needed.
 */
@HiltViewModel
class LogbookViewModel @Inject constructor(
    repository: AeroFocusRepository
) : ViewModel() {

    /** All focus sessions, ordered by most recent first. */
    val allSessions: Flow<List<FocusSessionEntity>> = repository.getAllSessions()

    /** Total number of completed flights. */
    val completedFlightCount: Flow<Int> = repository.getCompletedFlightCount()

    /** Total accumulated miles. */
    val totalMiles: Flow<Int> = repository.getTotalMilesFlown()

    /** Total focus minutes from completed sessions. */
    val totalFocusMinutes: Flow<Int> = repository.getTotalFocusMinutes()
}
