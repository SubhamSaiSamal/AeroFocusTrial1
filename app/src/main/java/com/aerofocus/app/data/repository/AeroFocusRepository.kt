package com.aerofocus.app.data.repository

import com.aerofocus.app.data.db.dao.AeroFocusDao
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for all AeroFocus data.
 *
 * Thin wrapper around [AeroFocusDao] — exists to keep the ViewModel layer
 * decoupled from Room internals and to make future additions (caching,
 * remote sync) straightforward.
 */
@Singleton
class AeroFocusRepository @Inject constructor(
    private val dao: AeroFocusDao
) {

    // ── Focus Sessions ───────────────────────────────────────────────

    /** Record a completed or aborted flight. */
    suspend fun insertSession(session: FocusSessionEntity) {
        dao.insertSession(session)
    }

    /** Observable list of all past sessions, newest-first. */
    fun getAllSessions(): Flow<List<FocusSessionEntity>> =
        dao.getAllSessions()

    /** Observable total miles across all completed sessions. */
    fun getTotalMilesFlown(): Flow<Int> =
        dao.getTotalMilesFlown()

    /** Observable count of successfully completed flights. */
    fun getCompletedFlightCount(): Flow<Int> =
        dao.getCompletedFlightCount()

    /** Observable total focus minutes from completed sessions. */
    fun getTotalFocusMinutes(): Flow<Int> =
        dao.getTotalFocusMinutes()

    // ── Destinations ─────────────────────────────────────────────────

    /** Observable list of all destinations (locked and unlocked). */
    fun getAllDestinations(): Flow<List<UnlockedDestinationEntity>> =
        dao.getAllDestinations()

    /** Observable list of unlocked destinations only. */
    fun getUnlockedDestinations(): Flow<List<UnlockedDestinationEntity>> =
        dao.getUnlockedDestinations()

    /** Unlock a single destination by its IATA code. */
    suspend fun unlockDestination(iataCode: String) {
        dao.unlockDestination(iataCode)
    }

    /**
     * After a successful session, call this with the user's updated total
     * miles to bulk-unlock all eligible destinations at once.
     */
    suspend fun unlockEligibleDestinations(totalMiles: Int) {
        dao.unlockEligibleDestinations(totalMiles)
    }

    /** Get the next destination the user is working toward. */
    suspend fun getNextDestinationToUnlock(): UnlockedDestinationEntity? =
        dao.getNextDestinationToUnlock()
}
