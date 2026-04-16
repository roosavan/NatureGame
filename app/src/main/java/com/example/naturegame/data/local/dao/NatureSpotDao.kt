package com.example.naturegame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.naturegame.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow

@Dao
interface NatureSpotDao {
    @Query("SELECT * FROM nature_spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<NatureSpot>>

    @Query("SELECT * FROM nature_spots WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    fun getSpotsWithLocation(): Flow<List<NatureSpot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: NatureSpot)

    @Query("DELETE FROM nature_spots WHERE id = :id")
    suspend fun deleteSpot(id: Long)
}
