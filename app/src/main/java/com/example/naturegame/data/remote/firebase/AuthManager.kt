package com.example.naturegame.data.remote.firebase

import java.util.UUID

/**
 * Offline-tilassa toimiva käyttäjähallinta.
 * Generoi paikallisen UUID:n käyttäjätunnisteeksi.
 */
class AuthManager {
    private val localUserId: String = UUID.randomUUID().toString()
    val currentUserId: String get() = localUserId
    val isSignedIn: Boolean get() = true

    suspend fun signInAnonymously(): Result<String> = Result.success(localUserId)
    fun signOut() { /* Ei tarvita offline-tilassa */ }
}