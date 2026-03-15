package com.simats.unimind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val title = intent.getStringExtra("title") ?: "Task"
        TaskNotificationHelper.showNotification(context, taskId, title)
    }
}
