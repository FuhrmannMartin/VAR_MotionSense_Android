// GyroscopeManager.kt
package com.example.var_websocket

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableFloatStateOf

class GyroscopeManager(context: Context) : SensorEventListener {
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var gameRotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    // Orientation in radians (using game rotation vector)
    val rotX = mutableFloatStateOf(0f) // Pitch
    val rotY = mutableFloatStateOf(0f) // Yaw
    val rotZ = mutableFloatStateOf(0f) // Roll

    // Calibration variables
    private var initialRotX = 0f
    private var initialRotY = 0f
    private var initialRotZ = 0f
    private var isCalibrated = false

    init {
        gameRotationVector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun calibrate() {
        // Set the current orientation as the calibration baseline
        initialRotX = rotX.value
        initialRotY = rotY.value
        initialRotZ = rotZ.value
        isCalibrated = true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                // Convert rotation matrix to Euler angles
                val orientations = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientations)

                // Apply calibration baseline to orientations to remove initial drift
                rotX.floatValue = orientations[1] - if (isCalibrated) initialRotX else 0f // Pitch (X-axis)
                rotY.floatValue = orientations[0] - if (isCalibrated) initialRotY else 0f // Yaw (Z-axis)
                rotZ.floatValue = orientations[2] - if (isCalibrated) initialRotZ else 0f // Roll (Y-axis)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
