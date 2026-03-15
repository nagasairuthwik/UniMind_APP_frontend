package com.simats.unimind

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Foreground service that tracks steps when the app is closed or in background.
 * Shows a persistent notification with current steps and daily goal.
 * Updates DomainData so Health screen and Progress Overview show correct steps when the app is opened.
 */
class StepCounterForegroundService : Service() {

    private var stepCounterHelper: StepCounterHelper? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        ensureChannel()
        val notification = buildStepsNotification(
            DomainData.getStepsToday(this),
            DomainData.getStepsGoal(this)
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIF_ID_STEPS,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                @Suppress("DEPRECATION")
                startForeground(NOTIF_ID_STEPS, notification)
            }
        } catch (_: SecurityException) {
            // Notification permission or foreground service restrictions; stop service gracefully.
            stopSelf()
            isRunning = false
            return
        }
        stepCounterHelper = StepCounterHelper(this) { stepsToday ->
            handler.post { updateNotification(stepsToday) }
        }
        stepCounterHelper?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        stepCounterHelper?.stop()
        stepCounterHelper = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID_STEPS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_STEPS,
                    CHANNEL_NAME_STEPS,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows your current step count and daily goal while step tracking is active."
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun updateNotification(stepsToday: Int) {
        val goal = DomainData.getStepsGoal(this)
        val notification = buildStepsNotification(stepsToday, goal)
        try {
            NotificationManagerCompat.from(this).notify(NOTIF_ID_STEPS, notification)
        } catch (_: SecurityException) { }
    }

    private fun buildStepsNotification(stepsToday: Int, stepsGoal: Int): Notification =
        buildStepsNotificationInternal(this, stepsToday, stepsGoal)

    companion object {
        private const val CHANNEL_ID_STEPS = "unimind_steps_tracker"
        private const val CHANNEL_NAME_STEPS = "Step tracker"
        private const val NOTIF_ID_STEPS = 3001

        @Volatile
        var isRunning = false
            private set

        fun start(context: android.content.Context) {
            val intent = Intent(context, StepCounterForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Refresh the running steps notification from current DomainData (used when UI changes steps quickly). */
        fun refreshNotification(context: android.content.Context) {
            if (!isRunning) return
            val stepsToday = DomainData.getStepsToday(context)
            val stepsGoal = DomainData.getStepsGoal(context)
            val notification = buildStepsNotificationInternal(context, stepsToday, stepsGoal)
            try {
                NotificationManagerCompat.from(context).notify(NOTIF_ID_STEPS, notification)
            } catch (_: SecurityException) { }
        }

        private fun buildStepsNotificationInternal(
            context: android.content.Context,
            stepsToday: Int,
            stepsGoal: Int
        ): Notification {
            val contentText = "%,d / %,d steps".format(stepsToday, stepsGoal)
            val openIntent = Intent(context, FitnessActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(context, CHANNEL_ID_STEPS)
                .setContentTitle("UniMind · Steps")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ei_1773028736012_removebg_preview)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
        }
    }
}
