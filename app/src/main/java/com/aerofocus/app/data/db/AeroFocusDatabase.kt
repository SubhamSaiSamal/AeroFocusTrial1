package com.aerofocus.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aerofocus.app.data.db.dao.AeroFocusDao
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The single Room database for AeroFocus.
 *
 * On first creation, a [Callback] pre-populates the destinations table
 * with a curated set of iconic world cities, each requiring a progressive
 * number of cumulative flight miles to unlock.
 */
@Database(
    entities = [
        FocusSessionEntity::class,
        UnlockedDestinationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AeroFocusDatabase : RoomDatabase() {

    abstract fun aeroFocusDao(): AeroFocusDao

    companion object {

        const val DATABASE_NAME = "aerofocus_db"

        /**
         * Seed destinations — ordered by ascending difficulty.
         * Miles roughly correspond to cumulative focus minutes.
         *
         * Tier 1 (0–120 min): Starter cities, unlocked quickly.
         * Tier 2 (150–500 min): Mid-range, rewarding consistency.
         * Tier 3 (600–2000 min): Elite destinations for committed users.
         */
        val SEED_DESTINATIONS = listOf(
            // ── Tier 1: Starter Routes ──────────────────────────────
            UnlockedDestinationEntity(
                iataCode = "DEL",
                cityName = "New Delhi",
                latitude = 28.6139,
                longitude = 77.2090,
                requiredMiles = 0,      // Unlocked by default (home base)
                isUnlocked = true
            ),
            UnlockedDestinationEntity(
                iataCode = "BOM",
                cityName = "Mumbai",
                latitude = 19.0760,
                longitude = 72.8777,
                requiredMiles = 25,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "BKK",
                cityName = "Bangkok",
                latitude = 13.7563,
                longitude = 100.5018,
                requiredMiles = 60,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "SIN",
                cityName = "Singapore",
                latitude = 1.3521,
                longitude = 103.8198,
                requiredMiles = 100,
                isUnlocked = false
            ),

            // ── Tier 2: Mid-Range ───────────────────────────────────
            UnlockedDestinationEntity(
                iataCode = "DXB",
                cityName = "Dubai",
                latitude = 25.2048,
                longitude = 55.2708,
                requiredMiles = 150,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "HND",
                cityName = "Tokyo",
                latitude = 35.6762,
                longitude = 139.6503,
                requiredMiles = 250,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "CDG",
                cityName = "Paris",
                latitude = 48.8566,
                longitude = 2.3522,
                requiredMiles = 350,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "LHR",
                cityName = "London",
                latitude = 51.5074,
                longitude = -0.1278,
                requiredMiles = 500,
                isUnlocked = false
            ),

            // ── Tier 3: Elite Destinations ──────────────────────────
            UnlockedDestinationEntity(
                iataCode = "JFK",
                cityName = "New York",
                latitude = 40.7128,
                longitude = -74.0060,
                requiredMiles = 750,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "SFO",
                cityName = "San Francisco",
                latitude = 37.7749,
                longitude = -122.4194,
                requiredMiles = 1000,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "SYD",
                cityName = "Sydney",
                latitude = -33.8688,
                longitude = 151.2093,
                requiredMiles = 1500,
                isUnlocked = false
            ),
            UnlockedDestinationEntity(
                iataCode = "ICN",
                cityName = "Seoul",
                latitude = 37.5665,
                longitude = 126.9780,
                requiredMiles = 2000,
                isUnlocked = false
            )
        )

        /**
         * Creates a [RoomDatabase.Callback] that seeds destinations on first creation.
         * Accepts the DAO instance to perform the insert.
         */
        fun seedCallback(dao: AeroFocusDao): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insertAllDestinations(SEED_DESTINATIONS)
                    }
                }
            }
        }
    }
}
