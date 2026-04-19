package com.example.naturegame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.naturegame.data.local.dao.NatureSpotDao
import com.example.naturegame.data.local.dao.WalkSessionDao
import com.example.naturegame.data.local.entity.NatureSpot
import com.example.naturegame.data.local.entity.WalkSession

/**
 * Room-tietokannan pääluokka.
 * Nostettu versioon 4 Health Connect -integraation (kalorit) myötä.
 */
@Database(
    entities = [
        NatureSpot::class,
        WalkSession::class
    ],
    version = 4,             // Nostettu 3 -> 4
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun natureSpotDao(): NatureSpotDao
    abstract fun walkSessionDao(): WalkSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luontopeli_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
