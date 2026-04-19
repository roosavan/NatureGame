package com.example.naturegame.ui.map

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naturegame.viewmodel.MapViewModel
import com.example.naturegame.viewmodel.WalkViewModel
import com.example.naturegame.viewmodel.formatDuration
import com.example.naturegame.viewmodel.formatDistance
import com.example.naturegame.viewmodel.toFormattedDate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    walkViewModel: WalkViewModel = viewModel()
) {
    val context = LocalContext.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            mapViewModel.startTracking()
        }
    }

    if (!permissionState.allPermissionsGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sijaintilupa tarvitaan karttaa varten")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Myönnä lupa")
            }
        }
        return
    }

    val isWalking by walkViewModel.isWalking.collectAsState()
    val routePoints by mapViewModel.routePoints.collectAsState()
    val natureSpots by mapViewModel.natureSpots.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val mapViewState = remember { MapView(context) }
            
            // Luodaan overlay kerran
            val myLocationOverlay = remember {
                MyLocationNewOverlay(GpsMyLocationProvider(context), mapViewState).apply {
                    enableMyLocation()
                    enableFollowLocation()
                }
            }

            // Keskitytään viimeisimpään sijaintiin heti kun se on saatavilla (vain kerran alussa)
            var hasInitiallyCentered by remember { mutableStateOf(false) }
            LaunchedEffect(currentLocation) {
                if (!hasInitiallyCentered && currentLocation != null) {
                    mapViewState.controller.setCenter(GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude))
                    hasInitiallyCentered = true
                }
            }

            DisposableEffect(Unit) {
                mapViewState.setTileSource(TileSourceFactory.MAPNIK)
                mapViewState.setMultiTouchControls(true)
                mapViewState.controller.setZoom(17.5)
                
                if (!mapViewState.overlays.contains(myLocationOverlay)) {
                    mapViewState.overlays.add(myLocationOverlay)
                }

                onDispose {
                    myLocationOverlay.disableMyLocation()
                    mapViewState.onDetach()
                }
            }

            AndroidView(
                factory = { mapViewState },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    // Tyhjennetään vain tarpeelliset (ei MyLocationOverlayta)
                    val currentOverlays = mapView.overlays.toList()
                    mapView.overlays.removeAll { it !is MyLocationNewOverlay }

                    // 1. Reittiviiva
                    if (isWalking && routePoints.size >= 2) {
                        val polyline = Polyline().apply {
                            setPoints(routePoints)
                            outlinePaint.color = 0xFF2E7D32.toInt()
                            outlinePaint.strokeWidth = 10f
                        }
                        mapView.overlays.add(polyline)
                    }

                    // 2. Luontokohteet
                    natureSpots.forEach { spot ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(spot.latitude, spot.longitude)
                            title = spot.plantLabel ?: spot.name
                            snippet = spot.timestamp.toFormattedDate()
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                    }

                    // Huom: Poistettu manuaalinen animateTo, jotta MyLocationOverlay hoitaa seuraamisen sulavasti
                    mapView.invalidate()
                }
            )
        }

        WalkStatsCard(walkViewModel)
    }
}

@Composable
fun WalkStatsCard(viewModel: WalkViewModel) {
    val session by viewModel.currentSession.collectAsState()
    val isWalking by viewModel.isWalking.collectAsState()
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isWalking) {
        if (isWalking) {
            while (true) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isWalking) "Kävely käynnissä" else "Kävely pysäytetty",
                style = MaterialTheme.typography.titleSmall
            )

            session?.let { s ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("${s.stepCount}", "askelta")
                    StatItem(formatDistance(s.distanceMeters), "matka")
                    StatItem(formatDuration(s.startTime, if (isWalking) currentTime else (s.endTime ?: currentTime)), "aika")
                    StatItem("${s.caloriesBurned.toInt()}", "kcal")
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                if (!isWalking) {
                    Button(onClick = { viewModel.startWalk() }, modifier = Modifier.weight(1f)) {
                        Text("Aloita kävely")
                    }
                } else {
                    OutlinedButton(onClick = { viewModel.stopWalk() }, modifier = Modifier.weight(1f)) {
                        Text("Lopeta")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
