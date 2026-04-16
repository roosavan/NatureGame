package com.example.naturegame.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Edustaa käyttäjän tallentamaa luontokohdetta (esim. tunnistettu kasvi).
 * Sisältää sijaintitiedot ja aikaleiman.
 */
@Entity(tableName = "nature_spots")
data class NatureSpot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val plantLabel: String?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val imagePath: String? = null
)
