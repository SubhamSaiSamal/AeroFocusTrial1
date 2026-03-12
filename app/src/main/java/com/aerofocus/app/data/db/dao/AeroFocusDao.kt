package com.aerofocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for all AeroFocus database operations.
 *
 * All read operations return [Flow] for reactive UI updates.
 * All write operations are `suspend` functions for coroutine-based async access.
 */
@Dao
interface AeroFocusDao {

    // ── Focus Sessions (Flight Log) ──────────────────────────────────

    /** Insert a new completed or failed focus session. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    /** Retrieve all sessions ordered by most recent first. */
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    /** Get the total accumulated miles across all completed sessions. */
    @Query("SELECT COALESCE(SUM(earnedMiles), 0) FROM focus_sessions")
    fun getTotalMilesFlown(): Flow<Int>

    /** Count of all completed flights. */
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE wasCompleted = 1")
    fun getCompletedFlightCount(): Flow<Int>

    /** Total focus minutes across all completed sessions. */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE wasCompleted = 1")
    fun getTotalFocusMinutes(): Flow<Int>

    // ── Destinations (Map Progress) ──────────────────────────────────

    /** Retrieve all destinations (for displaying the full map). */
    @Query("SELECT * FROM unlocked_destinations ORDER BY requiredMiles ASC")
    fun getAllDestinations(): Flow<List<UnlockedDestinationEntity>>

    /** Retrieve only unlocked destinations. */
    @Query("SELECT * FROM unlocked_destinations WHERE isUnlocked = 1 ORDER BY requiredMiles ASC")
    fun getUnlockedDestinations(): Flow<List<UnlockedDestinationEntity>>

    /** Insert or replace a destination (used for seed data population). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: UnlockedDestinationEntity)

    /** Batch insert destinations (used at DB creation). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDestinations(destinations: List<UnlockedDestinationEntity>)

    /**
     * Unlock a specific destination by IATA code.
     * Called when the user's total miles exceed the destination's threshold.
     */
    @Query("UPDATE unlocked_destinations SET isUnlocked = 1 WHERE iataCode = :iataCode")
    suspend fun unlockDestination(iataCode: String)

    /**
     * Bulk-unlock all destinations whose [requiredMiles] ≤ [totalMiles].
     * Called after every successful session to update the map.
     */
    @Query("UPDATE unlocked_destinations SET isUnlocked = 1 WHERE requiredMiles <= :totalMiles")
    suspend fun unlockEligibleDestinations(totalMiles: Int)

    /** Get the next locked destination the user is closest to unlocking. */
    @Query("SELECT * FROM unlocked_destinations WHERE isUnlocked = 0 ORDER BY requiredMiles ASC LIMIT 1")
    suspend fun getNextDestinationToUnlock(): UnlockedDestinationEntity?
}
