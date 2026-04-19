package com.example.naturegame.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.units.Energy
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Hallinnoi Health Connect -integraatiota.
 */
class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    /** Tarkistaa onko Health Connect saatavilla laitteessa. */
    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    /** Tarkistaa onko tarvittavat luvat myönnetty. */
    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    /**
     * Tallentaa kävelysession ja poltetut kalorit Health Connectiin.
     */
    suspend fun writeWalkData(startTime: Long, endTime: Long, calories: Double) {
        if (!hasAllPermissions()) return

        val start = Instant.ofEpochMilli(startTime)
        val end = Instant.ofEpochMilli(endTime)

        val exerciseRecord = ExerciseSessionRecord(
            startTime = start,
            startZoneOffset = ZonedDateTime.now().offset,
            endTime = end,
            endZoneOffset = ZonedDateTime.now().offset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_WALKING,
            title = "Luontopeli kävely"
        )

        val caloriesRecord = TotalCaloriesBurnedRecord(
            startTime = start,
            startZoneOffset = ZonedDateTime.now().offset,
            endTime = end,
            endZoneOffset = ZonedDateTime.now().offset,
            energy = Energy.kilocalories(calories)
        )

        try {
            healthConnectClient.insertRecords(listOf(exerciseRecord, caloriesRecord))
        } catch (e: Exception) {
            // Logaus tai virheen käsittely
        }
    }
}
