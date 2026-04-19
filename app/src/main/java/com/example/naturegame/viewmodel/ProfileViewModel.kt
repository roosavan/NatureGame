package com.example.naturegame.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturegame.data.local.AppDatabase
import com.example.naturegame.data.remote.firebase.AuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager = AuthManager()
    private val db = AppDatabase.getDatabase(application)
    private val sharedPrefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    // Nimi ladataan SharedPreferencesista
    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Luontoseikkailija") ?: "Luontoseikkailija")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Profiilikuvan polku ladataan SharedPreferencesista
    private val _profileImageUri = MutableStateFlow(sharedPrefs.getString("profile_image_uri", null))
    val profileImageUri: StateFlow<String?> = _profileImageUri.asStateFlow()

    private val allSessions = db.walkSessionDao().getAllSessions()

    val totalSteps = allSessions.map { sessions -> sessions.sumOf { it.stepCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDistance = allSessions.map { sessions -> sessions.sumOf { it.distanceMeters.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalCalories = allSessions.map { sessions -> sessions.sumOf { it.caloriesBurned } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSpots = db.natureSpotDao().getAllSpots().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Kirjaudutaan anonyymisti taustalla, jotta Firebase-tallennus toimii ilman erillistä nappia
        if (FirebaseAuth.getInstance().currentUser == null) {
            viewModelScope.launch {
                authManager.signInAnonymously()
            }
        }
    }

    fun updateName(newName: String) {
        _userName.value = newName
        sharedPrefs.edit().putString("user_name", newName).apply()
    }

    fun updateProfileImage(uri: String) {
        _profileImageUri.value = uri
        sharedPrefs.edit().putString("profile_image_uri", uri).apply()
    }
}
