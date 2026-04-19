package com.example.naturegame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturegame.data.local.AppDatabase
import com.example.naturegame.data.local.entity.WalkSession
import com.example.naturegame.sensor.StepCounterManager
import com.example.naturegame.health.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalkViewModel(application: Application) : AndroidViewModel(application) {

    private val stepManager = StepCounterManager(application)
    private val healthManager = HealthConnectManager(application)
    private val db = AppDatabase.getDatabase(application)

    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    fun startWalk() {
        if (_isWalking.value) return
        val session = WalkSession()
        _currentSession.value = session
        _isWalking.value = true

        stepManager.startStepCounting {
            _currentSession.update { current ->
                val newSteps = (current?.stepCount ?: 0) + 1
                val newDistance = (current?.distanceMeters ?: 0f) + StepCounterManager.STEP_LENGTH_METERS
                // Arvioidaan kalorit: ~0.044 kcal per askel
                val newCalories = newSteps * 0.044 

                current?.copy(
                    stepCount = newSteps,
                    distanceMeters = newDistance,
                    caloriesBurned = newCalories
                )
            }
        }
    }

    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false

        _currentSession.update { it?.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )}

        viewModelScope.launch {
            _currentSession.value?.let { session ->
                db.walkSessionDao().insert(session)
                if (healthManager.isAvailable() && healthManager.hasAllPermissions()) {
                    healthManager.writeWalkData(
                        session.startTime,
                        session.endTime ?: System.currentTimeMillis(),
                        session.caloriesBurned
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}

fun formatDistance(meters: Float): String {
    return if (meters < 1000f) "${meters.toInt()} m" else "${"%.1f".format(meters / 1000f)} km"
}

fun formatDuration(startTime: Long, endTime: Long = System.currentTimeMillis()): String {
    val seconds = (endTime - startTime) / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}min"
        minutes > 0 -> "${minutes}min ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
