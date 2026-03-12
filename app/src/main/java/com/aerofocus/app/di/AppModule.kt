package com.aerofocus.app.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aerofocus.app.data.db.AeroFocusDatabase
import com.aerofocus.app.data.db.dao.AeroFocusDao
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

/**
 * Hilt module providing application-scoped singletons for the data layer.
 *
 * Build order:  Database  →  DAO  →  Repository
 *
 * The seed callback is handled via raw SQL in the Room callback to avoid
 * the circular dependency problem (DB needs DAO, DAO needs DB).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AeroFocusDatabase {
        return Room.databaseBuilder(
            context,
            AeroFocusDatabase::class.java,
            AeroFocusDatabase.DATABASE_NAME
        )
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed destinations using raw SQL to avoid circular DI.
                    // This runs once on first database creation.
                    AeroFocusDatabase.SEED_DESTINATIONS.forEach { dest ->
                        db.execSQL(
                            """
                            INSERT OR REPLACE INTO unlocked_destinations 
                            (iataCode, cityName, latitude, longitude, requiredMiles, isUnlocked) 
                            VALUES (?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf(
                                dest.iataCode,
                                dest.cityName,
                                dest.latitude,
                                dest.longitude,
                                dest.requiredMiles,
                                if (dest.isUnlocked) 1 else 0
                            )
                        )
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(database: AeroFocusDatabase): AeroFocusDao {
        return database.aeroFocusDao()
    }
}
