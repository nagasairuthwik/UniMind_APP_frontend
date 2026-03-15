package com.simats.unimind

import android.app.Notification
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object TaskNotificationHelper {

    private const val CHANNEL_ID = "unimind_tasks"
    private const val CHANNEL_NAME = "Task reminders"
    /** Notification ID for the ongoing "next reminder" countdown in the notification bar. */
    const val NOTIFICATION_ID_TIMER = 9999

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun scheduleTaskReminder(context: Context, taskId: String, title: String, timeMillis: Long) {
        ensureChannel(context)
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("title", title)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarm.canScheduleExactAlarms()) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pending)
        } else {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pending)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: String) {
        val intent = Intent(context, TaskNotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pending)
    }

    fun showNotification(context: Context, taskId: String, title: String) {
        ensureChannel(context)
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ei_1773028736012_removebg_preview)
            .setContentTitle("Task: $title")
            .setContentText("Time to get it done!")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
        } catch (_: SecurityException) { }
    }

    /**
     * Show or update the countdown timer in the notification bar.
     * Title: "Next: [taskTitle]"; text: countdown e.g. "in 00:15:30".
     */
    fun showTimerNotification(context: Context, taskTitle: String, countdownText: String) {
        val notification = buildTimerNotification(context, taskTitle, countdownText) ?: return
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TIMER, notification)
        } catch (_: SecurityException) { }
    }

    /** Build the timer notification for use with foreground service or notify. */
    fun buildTimerNotification(context: Context, taskTitle: String, countdownText: String): Notification? {
        ensureChannel(context)
        // When the user taps the timer in the notification bar,
        // open the Productivity screen (same as Quick Actions → Productivity).
        val intent = Intent(context, ProductivityActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TIMER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return try {
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ei_1773028736012_removebg_preview)
                .setContentTitle("Next: $taskTitle")
                .setContentText(countdownText)
                .setContentIntent(pending)
                .setOngoing(true)
                .setSilent(true)
                .build()
        } catch (_: Exception) { null }
    }

    /** Remove the timer countdown from the notification bar. */
    fun cancelTimerNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_TIMER)
        } catch (_: SecurityException) { }
    }
}
