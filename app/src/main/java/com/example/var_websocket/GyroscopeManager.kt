package com.example.var_websocket

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.abs

class AccelerometerManager(context: Context) : SensorEventListener {
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Mutable state values for position
    val posX = mutableFloatStateOf(0f)
    val posY = mutableFloatStateOf(0f)
    val posZ = mutableFloatStateOf(0f)

    private var velX = 0f
    private var velY = 0f
    private var velZ = 0f

    // For tracking the time between sensor updates
    private var lastTimestamp: Long = 0

    // Gravity vector in the device's current orientation
    private val gravity = FloatArray(3)

    // Constants for managing drift and stability
    private val dampingFactor = 0.95f     // Friction effect to reduce drift
    private val threshold = 0.1f          // Ignore minor noise below this value
    private val filterAlpha = 0.8f        // Smoothing factor for low-pass filter

    // Last velocity values for filtering
    private var lastVelX = 0f
    private var lastVelY = 0f
    private var lastVelZ = 0f

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        rotationVector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun resetPosition() {
        posX.floatValue = 0f
        posY.floatValue = 0f
        posZ.floatValue = 0f
        velX = 0f
        velY = 0f
        velZ = 0f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    // Update gravity vector based on device orientation
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                    gravity[0] = SensorManager.GRAVITY_EARTH * rotationMatrix[6]
                    gravity[1] = SensorManager.GRAVITY_EARTH * rotationMatrix[7]
                    gravity[2] = SensorManager.GRAVITY_EARTH * rotationMatrix[8]
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    val currentTime = it.timestamp
                    val deltaTime = if (lastTimestamp != 0L) (currentTime - lastTimestamp) / 1_000_000_000f else 0f
                    lastTimestamp = currentTime

                    if (deltaTime > 0) {
                        // Remove gravity from the accelerometer data to get linear acceleration
                        val linearAccelerationX = it.values[0] - gravity[0]
                        val linearAccelerationY = it.values[1] - gravity[1]
                        val linearAccelerationZ = it.values[2] - gravity[2]

                        // Only update velocity if above threshold to reduce noise drift
                        if (abs(linearAccelerationX) > threshold) velX += linearAccelerationX * deltaTime
                        if (abs(linearAccelerationY) > threshold) velY += linearAccelerationY * deltaTime
                        if (abs(linearAccelerationZ) > threshold) velZ += linearAccelerationZ * deltaTime

                        // Apply damping to velocity
                        velX *= dampingFactor
                        velY *= dampingFactor
                        velZ *= dampingFactor

                        // Reset velocity if acceleration is below threshold (stationary)
                        if (abs(linearAccelerationX) < threshold &&
                            abs(linearAccelerationY) < threshold &&
                            abs(linearAccelerationZ) < threshold) {
                            velX = 0f
                            velY = 0f
                            velZ = 0f
                        }

                        // Apply low-pass filter to smooth velocity
                        velX = filterAlpha * velX + (1 - filterAlpha) * lastVelX
                        velY = filterAlpha * velY + (1 - filterAlpha) * lastVelY
                        velZ = filterAlpha * velZ + (1 - filterAlpha) * lastVelZ

                        // Store the filtered velocities for next iteration
                        lastVelX = velX
                        lastVelY = velY
                        lastVelZ = velZ

                        // Integrate velocity to update position
                        posX.floatValue += velX * deltaTime
                        posY.floatValue += velY * deltaTime
                        posZ.floatValue += velZ * deltaTime
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not implemented
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
