package com.aerofocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a destination city on the global map.
 *
 * Each destination requires a cumulative miles threshold to unlock.
 * Pre-populated on first database creation with seed data.
 */
@Entity(tableName = "unlocked_destinations")
data class UnlockedDestinationEntity(
    /** IATA airport code, e.g. "JFK", "HND", "CDG". Serves as unique key. */
    @PrimaryKey
    val iataCode: String,

    /** Human-readable city name, e.g. "New York". */
    val cityName: String,

    /** Geographic latitude for map placement. */
    val latitude: Double,

    /** Geographic longitude for map placement. */
    val longitude: Double,

    /** Total cumulative miles the user must earn to unlock this destination. */
    val requiredMiles: Int,

    /** Whether the user has surpassed [requiredMiles] and unlocked this city. */
    val isUnlocked: Boolean = false
)
