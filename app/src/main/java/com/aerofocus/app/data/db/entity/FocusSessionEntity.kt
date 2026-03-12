package com.aerofocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single focus session — a "flight" in AeroFocus lore.
 *
 * Every completed or failed focus session is recorded here, forming the
 * user's persistent Flight Log.
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,

    /** Unix timestamp (millis) when the session began. */
    val startTime: Long,

    /** Target duration the user committed to, in minutes. */
    val durationMinutes: Int,

    /** Free-text tag describing what the user was working on. */
    val focusTag: String,

    /**
     * `true`  → Timer reached zero naturally ("Successful Landing").
     * `false` → User cancelled early ("Emergency Landing").
     */
    val wasCompleted: Boolean,

    /**
     * Miles earned for this session.
     * Calculated as [durationMinutes] for completed sessions, 0 for failed.
     */
    val earnedMiles: Int
)
