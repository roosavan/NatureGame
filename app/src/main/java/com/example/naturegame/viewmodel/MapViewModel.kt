package com.example.naturegame.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturegame.data.local.AppDatabase
import com.example.naturegame.data.local.entity.NatureSpot
import com.example.naturegame.data.repository.NatureSpotRepository
import com.example.naturegame.data.remote.firebase.AuthManager
import com.example.naturegame.data.remote.firebase.FirestoreManager
import com.example.naturegame.data.remote.firebase.StorageManager
import com.example.naturegame.location.LocationManager
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel karttanäkymälle (MapScreen).
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)
    private val db = AppDatabase.getDatabase(application)
    
    // Alustetaan repository (käytetään samaa kuin CameraViewModelissa)
    private val repository = NatureSpotRepository(
        dao = db.natureSpotDao(),
        firestoreManager = FirestoreManager(),
        storageManager = StorageManager(),
        authManager = AuthManager()
    )

    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    /** 
     * Kartalla näytettävät luontolöydöt.
     * Käytetään repositoryn allSpots-virtaa, joka päivittyy automaattisesti 
     * kun tietokantaan lisätään uusi kohde.
     */
    val natureSpots: StateFlow<List<NatureSpot>> = repository.allSpots
        .map { list -> 
            // Suodatetaan vain ne joilla on koordinaatit (ei tasan 0,0)
            list.filter { it.latitude != 0.0 || it.longitude != 0.0 }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startTracking() = locationManager.startTracking()
    fun stopTracking() = locationManager.stopTracking()
    fun resetRoute() = locationManager.resetRoute()

    override fun onCleared() {
        super.onCleared()
        locationManager.stopTracking()
    }
}

fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}
