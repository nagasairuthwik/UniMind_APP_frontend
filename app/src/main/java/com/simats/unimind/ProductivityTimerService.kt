package com.simats.unimind

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationManagerCompat

/**
 * Foreground service that keeps the task countdown timer updating in the notification bar
 * when the app is in the background.
 */
class ProductivityTimerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskId = intent?.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return START_NOT_STICKY
        val timeMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, 0L)
        if (timeMillis <= 0) return START_NOT_STICKY

        TaskNotificationHelper.ensureChannel(this)
        val initialNotification = TaskNotificationHelper.buildTimerNotification(
            this, title, formatCountdown(timeMillis - System.currentTimeMillis())
        ) ?: return START_NOT_STICKY
        startForeground(TaskNotificationHelper.NOTIFICATION_ID_TIMER, initialNotification)
        startUpdateLoop(taskId, title, timeMillis)
        return START_NOT_STICKY
    }

    private fun startUpdateLoop(taskId: String, title: String, timeMillis: Long) {
        updateRunnable?.let { handler.removeCallbacks(it) }
        val runnable = object : Runnable {
            override fun run() {
                val remaining = timeMillis - System.currentTimeMillis()
                if (remaining <= 0) {
                    TaskNotificationHelper.showNotification(this@ProductivityTimerService, taskId, title)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf()
                    return
                }
                val countdownText = formatCountdown(remaining)
                val notification = TaskNotificationHelper.buildTimerNotification(
                    this@ProductivityTimerService, title, countdownText
                )
                if (notification != null) {
                    try {
                        NotificationManagerCompat.from(this@ProductivityTimerService)
                            .notify(TaskNotificationHelper.NOTIFICATION_ID_TIMER, notification)
                    } catch (_: SecurityException) { }
                }
                handler.postDelayed(this, 1000)
            }
        }
        updateRunnable = runnable
        runnable.run()
    }

    private fun formatCountdown(remainingMs: Long): String {
        return when {
            remainingMs >= 24 * 60 * 60 * 1000L -> {
                val days = (remainingMs / (24 * 60 * 60 * 1000)).toInt()
                val h = ((remainingMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)).toInt()
                val m = ((remainingMs % (60 * 60 * 1000)) / (60 * 1000)).toInt()
                String.format("in %dd %02d:%02d", days, h, m)
            }
            else -> {
                val totalSec = (remainingMs / 1000).toInt()
                val h = totalSec / 3600
                val m = (totalSec % 3600) / 60
                val s = totalSec % 60
                String.format("in %02d:%02d:%02d", h, m, s)
            }
        }
    }

    override fun onDestroy() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        TaskNotificationHelper.cancelTimerNotification(this)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TIME_MILLIS = "time_millis"
    }
}
