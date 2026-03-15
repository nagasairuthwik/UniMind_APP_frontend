package com.simats.unimind

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Listens to the device step sensor and updates DomainData with steps for today.
 * Uses TYPE_STEP_COUNTER (total since boot; baseline for today) as the single source of truth.
 * TYPE_STEP_DETECTOR is no longer used because some devices emit noisy detector events.
 * Steps are tracked whenever the listener is registered (e.g. app in foreground).
 */
class StepCounterHelper(
    private val context: Context,
    private val onStepsUpdated: (stepsToday: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val handler = Handler(Looper.getMainLooper())
    private var isRegistered = false

    // Simple noise filter for the cumulative counter:
    // - Ignore counter jumps that imply unrealistically high step rate (likely phone shaking).
    private var lastCounterTimestampNs: Long = 0L
    private var lastCounterTotalSinceBoot: Int = -1

    fun start() {
        if (isRegistered) return

        if (stepCounterSensor == null) {
            Log.w(TAG, "No TYPE_STEP_COUNTER sensor available on this device")
            return
        }

        val ok = sensorManager.registerListener(
            this,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        if (ok) {
            isRegistered = true
            Log.d(TAG, "Step counter (TYPE_STEP_COUNTER) registered")
        } else {
            Log.w(TAG, "Failed to register step counter (check ACTIVITY_RECOGNITION permission)")
        }
    }

    fun stop() {
        if (!isRegistered) return
        sensorManager.unregisterListener(this)
        isRegistered = false
        Log.d(TAG, "Step listener unregistered")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == null || event.values.isEmpty()) return

        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val stepsToday = handleStepCounter(event.values[0].toInt(), event.timestamp)
        handler.post { onStepsUpdated(stepsToday) }
        // Keep steps foreground notification in sync with latest sensor value
        StepCounterForegroundService.refreshNotification(context)
    }

    private fun handleStepCounter(totalSinceBoot: Int, timestampNs: Long): Int {
        // First reading: just establish baseline logic without filtering.
        if (lastCounterTotalSinceBoot < 0) {
            lastCounterTotalSinceBoot = totalSinceBoot
            lastCounterTimestampNs = timestampNs
        } else {
            val deltaSteps = totalSinceBoot - lastCounterTotalSinceBoot
            val deltaTimeSec = (timestampNs - lastCounterTimestampNs) / 1_000_000_000.0
            if (deltaSteps > 0 && deltaTimeSec > 0) {
                val stepsPerSecond = deltaSteps / deltaTimeSec
                // Filter out unrealistically high burst rates (likely from phone shaking).
                if (stepsPerSecond > 4.0) {
                    Log.d(TAG, "Ignoring counter jump: $deltaSteps steps in ${"%.2f".format(deltaTimeSec)}s ($stepsPerSecond s/s)")
                    // Do not update baseline timestamps here so real walking can still be picked up later.
                    return DomainData.getStepsToday(context)
                }
            }
            lastCounterTotalSinceBoot = totalSinceBoot
            lastCounterTimestampNs = timestampNs
        }

        val today = DomainData.todayDate()
        val baseline = DomainData.getStepsSensorBaseline(context)
        val baselineDate = DomainData.getStepsBaselineDate(context)

        return when {
            // First time ever, or device rebooted (counter reset)
            baselineDate.isEmpty() || baseline > totalSinceBoot -> {
                DomainData.setStepsSensorBaseline(context, totalSinceBoot, today)
                DomainData.setStepsToday(context, 0)
                0
            }
            baselineDate != today -> {
                val previousSteps = DomainData.getStepsToday(context)
                DomainData.setStepsHistoryForDate(context, baselineDate, previousSteps)
                DomainData.setStepsSensorBaseline(context, totalSinceBoot, today)
                DomainData.setStepsToday(context, 0)
                0
            }
            else -> {
                val steps = (totalSinceBoot - baseline).coerceAtLeast(0)
                DomainData.setStepsToday(context, steps)
                steps
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "StepCounterHelper"
    }
}
