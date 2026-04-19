package com.example.naturegame.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.naturegame.ui.map.MapScreen
import com.example.naturegame.camera.CameraScreen
import com.example.naturegame.ui.discover.DiscoverScreen
import com.example.naturegame.ui.stats.StatsScreen
import com.example.naturegame.ui.profile.ProfileScreen
import com.example.naturegame.viewmodel.MapViewModel
import com.example.naturegame.viewmodel.WalkViewModel

@Composable
fun LuontopeliNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val mapViewModel: MapViewModel = viewModel()
    val walkViewModel: WalkViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier
    ) {
        composable(Screen.Map.route) {
            MapScreen(mapViewModel = mapViewModel, walkViewModel = walkViewModel)
        }
        composable(Screen.Camera.route) {
            CameraScreen(mapViewModel = mapViewModel)
        }
        composable(Screen.Discover.route) {
            DiscoverScreen()
        }
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
