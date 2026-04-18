package com.example.naturegame.data.remote.firebase

import com.example.naturegame.data.local.entity.NatureSpot

/**
 * Firestore-hallinta (stub).
 * Tallentaa löytöjen metatiedot (nimi, sijainti, kasvilaji) Firestoreen.
 * Offline-tilassa palauttaa onnistumisen tekemättä mitään.
 */
class FirestoreManager {

    /**
     * Tallentaa luontolöydön metatiedot Firestoreen.
     */
    suspend fun saveSpot(spot: NatureSpot): Result<Unit> {
        // No-op toteutus offline-versiota varten
        return Result.success(Unit)
    }
}