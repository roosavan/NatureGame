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
 * Sovelluksen pääasiallinen Room-tietokanta.
 * Sisältää taulut kävelykertojen (walk_sessions) ja luontokohteiden (nature_spots) tallentamiseen.
 *
 * Käyttää Singleton-mallia varmistaakseen, että vain yksi tietokantainstanssi
 * on kerrallaan auki.
 */
@Database(entities = [WalkSession::class, NatureSpot::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Palauttaa DAO:n kävelykertojen käsittelyyn.
     */
    abstract fun walkSessionDao(): WalkSessionDao

    /**
     * Palauttaa DAO:n luontokohteiden käsittelyyn.
     */
    abstract fun natureSpotDao(): NatureSpotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Palauttaa tai luo tietokantainstanssin (Singleton).
         * @param context Sovelluksen konteksti
         * @return AppDatabase-instanssi
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nature_game_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
