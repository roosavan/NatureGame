package com.example.naturegame.data.repository

import com.example.naturegame.data.local.dao.NatureSpotDao
import com.example.naturegame.data.local.entity.NatureSpot
import com.example.naturegame.data.remote.firebase.AuthManager
import com.example.naturegame.data.remote.firebase.FirestoreManager
import com.example.naturegame.data.remote.firebase.StorageManager
import kotlinx.coroutines.flow.Flow

/**
 * Repository-luokka luontolöytöjen hallintaan (Repository-suunnittelumalli).
 *
 * Toimii välittäjänä tietolähteiden (Room-tietokanta) ja ViewModelien välillä.
 * Offline-tilassa kaikki data tallennetaan ja haetaan paikallisesta Room-tietokannasta.
 * Firebase-managerit ovat no-op -toteutuksia, jotka eivät tee verkko-operaatioita.
 */
class NatureSpotRepository(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {
    /** Flow-virta kaikista luontolöydöistä aikajärjestyksessä (uusin ensin) */
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()

    /** Flow-virta löydöistä joilla on validi GPS-sijainti (kartalla näytettävät) */
    val spotsWithLocation: Flow<List<NatureSpot>> = dao.getSpotsWithLocation()

    /**
     * Tallentaa uuden luontolöydön paikalliseen tietokantaan.
     * Lisää automaattisesti käyttäjätunnisteen ja merkitsee synkronoiduksi.
     */
    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId, synced = true)
        dao.insert(spotWithUser)
    }

    /** Poistaa luontolöydön paikallisesta tietokannasta. */
    suspend fun deleteSpot(spot: NatureSpot) {
        dao.delete(spot)
    }

    /**
     * Hakee löydöt jotka eivät ole synkronoituja.
     * Offline-tilassa tämä lista on yleensä tyhjä.
     */
    suspend fun getUnsyncedSpots(): List<NatureSpot> {
        return dao.getUnsyncedSpots()
    }
}