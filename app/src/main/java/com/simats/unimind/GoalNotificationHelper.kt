package com.simats.unimind

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object GoalNotificationHelper {

    private const val CHANNEL_ID_GOALS = "unimind_goals"
    private const val CHANNEL_NAME_GOALS = "UniMind Goals"
    private const val CHANNEL_DESC_GOALS = "Notifications when you complete goals in each domain."

    private const val NOTIF_ID_HEALTH_GOAL = 2001
    private const val NOTIF_ID_FINANCE = 2002
    private const val NOTIF_ID_PRODUCTIVITY = 2003
    private const val NOTIF_ID_LIFESTYLE = 2004

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID_GOALS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_GOALS,
                    CHANNEL_NAME_GOALS,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESC_GOALS
                    enableLights(true)
                    lightColor = Color.GREEN
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun showDomainNotification(
        context: Context,
        smallIconRes: Int,
        notificationId: Int,
        title: String,
        body: String
    ) {
        ensureChannel(context)
        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID_GOALS)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showHealthGoalAchieved(context: Context, stepsToday: Int, stepsGoal: Int) {
        val body = context.getString(R.string.notification_health_goal_body, stepsToday, stepsGoal)
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_HEALTH_GOAL,
            title = context.getString(R.string.notification_health_goal_title),
            body = body
        )
    }

    /** Shown when user saves monthly salary in Finance. */
    fun showSalaryUpdated(context: Context, amount: Double) {
        val body = context.getString(R.string.notification_salary_updated_body)
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_FINANCE,
            title = context.getString(R.string.notification_salary_updated_title),
            body = body
        )
    }

    /** Shown when user adds an expense. */
    fun showExpenseRecorded(context: Context) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_FINANCE,
            title = context.getString(R.string.notification_expense_recorded_title),
            body = context.getString(R.string.notification_expense_recorded_body)
        )
    }

    /** Shown when user saves budget (Food/Transport/Entertainment/Bills). */
    fun showBudgetUpdated(context: Context) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_FINANCE,
            title = context.getString(R.string.notification_budget_updated_title),
            body = context.getString(R.string.notification_budget_updated_body)
        )
    }

    /** Shown when user adds a savings goal. */
    fun showSavingsGoalAdded(context: Context, goalName: String) {
        val body = context.getString(R.string.notification_savings_goal_added_body, goalName)
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_FINANCE,
            title = context.getString(R.string.notification_savings_goal_added_title),
            body = body
        )
    }

    /** Shown when user completes all tasks for the day. */
    fun showAllTasksCompleted(context: Context) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_PRODUCTIVITY,
            title = context.getString(R.string.notification_all_tasks_done_title),
            body = context.getString(R.string.notification_all_tasks_done_body)
        )
    }

    /** Shown when user saves sleep/stress log in Lifestyle. */
    fun showLifestyleLogSaved(context: Context) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_LIFESTYLE,
            title = context.getString(R.string.notification_lifestyle_saved_title),
            body = context.getString(R.string.notification_lifestyle_saved_body)
        )
    }

    fun showFinanceUpdate(context: Context, title: String, body: String) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_FINANCE,
            title = title,
            body = body
        )
    }

    fun showProductivityUpdate(context: Context, title: String, body: String) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_PRODUCTIVITY,
            title = title,
            body = body
        )
    }

    fun showLifestyleUpdate(context: Context, title: String, body: String) {
        showDomainNotification(
            context = context,
            smallIconRes = R.drawable.ei_1773028736012_removebg_preview,
            notificationId = NOTIF_ID_LIFESTYLE,
            title = title,
            body = body
        )
    }
}

