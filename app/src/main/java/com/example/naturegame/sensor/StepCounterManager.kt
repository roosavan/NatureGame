package com.example.naturegame.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Sensorien hallintapalvelu askelmittarille ja gyroskoopille.
 */
class StepCounterManager(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Yritetään ensin STEP_DETECTORia, sitten STEP_COUNTERia fallbackina
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val gyroSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var stepListener: SensorEventListener? = null
    private var gyroListener: SensorEventListener? = null

    // STEP_COUNTER palauttaa askeleet laitteen käynnistyksestä asti, joten tarvitaan nollauspiste
    private var initialSteps = -1f

    /**
     * Käynnistää askelten laskemisen.
     * @param onStep Callback-funktio joka kutsutaan jokaisen askeleen kohdalla
     */
    fun startStepCounting(onStep: () -> Unit) {
        initialSteps = -1f
        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_STEP_DETECTOR -> {
                        // Detector laukeaa kerran per askel
                        onStep()
                    }
                    Sensor.TYPE_STEP_COUNTER -> {
                        // Counter on kumulatiivinen arvo
                        val currentSteps = event.values[0]
                        if (initialSteps < 0) {
                            initialSteps = currentSteps
                        } else {
                            val delta = (currentSteps - initialSteps).toInt()
                            if (delta > 0) {
                                repeat(delta) { onStep() }
                                initialSteps = currentSteps
                            }
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        stepSensor?.let {
            sensorManager.registerListener(
                stepListener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
            Log.d("StepCounterManager", "Sensor registered: ${it.name}")
        } ?: Log.e("StepCounterManager", "No step sensor found!")
    }

    /** Pysäyttää askelten laskemisen. */
    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
        initialSteps = -1f
    }

    /** Käynnistää gyroskoopin. */
    fun startGyroscope(onRotation: (Float, Float, Float) -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    onRotation(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        gyroSensor?.let {
            sensorManager.registerListener(
                gyroListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
    }

    fun isStepSensorAvailable(): Boolean = stepSensor != null

    companion object {
        const val STEP_LENGTH_METERS = 0.74f
    }
}
