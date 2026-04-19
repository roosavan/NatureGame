package com.example.naturegame.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naturegame.viewmodel.StatsViewModel
import com.example.naturegame.viewmodel.formatDistance
import com.example.naturegame.viewmodel.formatDuration
import com.example.naturegame.viewmodel.toFormattedDate

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val sessions by viewModel.allSessions.collectAsState()
    val totalSpots by viewModel.totalSpots.collectAsState()

    val totalSteps = sessions.sumOf { it.stepCount }
    val totalDistance = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat()
    val totalCalories = sessions.sumOf { it.caloriesBurned }.toInt()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Tilastot",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Rivi 1: Askeleet + Kalorit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryCard(
                    value = "$totalSteps",
                    label = "Askelta",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryCard(
                    value = "$totalCalories kcal",
                    label = "Poltetut kalorit",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Rivi 2: Matka + Löydöt
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryCard(
                    value = formatDistance(totalDistance),
                    label = "Matka yhteensä",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryCard(
                    value = "$totalSpots",
                    label = "Löytöjä",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (sessions.isNotEmpty()) {
            item {
                Text(
                    "Kävelyhistoria",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsWalk, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${session.stepCount} askelta • ${session.caloriesBurned.toInt()} kcal",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                session.startTime.toFormattedDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            session.endTime?.let { end ->
                                Text(
                                    "Kesto: ${formatDuration(session.startTime, end)} • ${formatDistance(session.distanceMeters)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        // Lisätään kalori-ikoni oikeaan reunaan
                        Icon(
                            Icons.Default.LocalFireDepartment, null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BarChart, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("Ei kävelylenkkejä vielä", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatSummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
