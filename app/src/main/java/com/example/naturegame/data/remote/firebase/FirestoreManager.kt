package com.example.naturegame.data.remote.firebase

import com.example.naturegame.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Offline-tilassa toimiva Firestore-hallinta (no-op).
 * Kaikki data tallennetaan vain paikalliseen Room-tietokantaan.
 */
class FirestoreManager {
    suspend fun saveSpot(spot: NatureSpot): Result<Unit> = Result.success(Unit)
    fun getUserSpots(userId: String): Flow<List<NatureSpot>> = flowOf(emptyList())
}