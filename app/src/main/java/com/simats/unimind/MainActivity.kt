package com.simats.unimind

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File
import java.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageView>(R.id.home_profile).setOnClickListener {
            startActivity(Intent(this, MyProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_steps_card).setOnClickListener {
            startActivity(Intent(this, StepsOverviewActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_tasks_card).setOnClickListener {
            startActivity(Intent(this, TasksOverviewActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_saved_card).setOnClickListener {
            startActivity(Intent(this, SavingsOverviewActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_progress_card).setOnClickListener {
            startActivity(Intent(this, WeeklyProgressOverviewActivity::class.java))
        }

        findViewById<ImageButton>(R.id.home_search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<ImageButton>(R.id.home_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_ai_summary_card).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        findViewById<TextView>(R.id.home_see_all).setOnClickListener {
            startActivity(Intent(this, QuickActionsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.home_recent_insights_card).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_quick_ai_chat).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.home_quick_lifestyle).setOnClickListener {
            startActivity(Intent(this, LifestyleOptimizationActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.home_quick_productivity).setOnClickListener {
            startActivity(Intent(this, ProductivityActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.home_quick_finance).setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.home_nav_insights).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.home_nav_analytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.home_nav_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.home_nav_chatbot).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
        }

        updateHomeCardsFromDomainData()
        updateGreeting()
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        updateProfileAvatar()
        updateHomeCardsFromDomainData()
        NotificationPermissionHelper.ensureNotificationPermission(this)
        requestActivityRecognitionAndStartStepCounter()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepTracking()
        }
    }

    private fun requestActivityRecognitionAndStartStepCounter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED ->
                    startStepTracking()
                else ->
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_ACTIVITY_RECOGNITION)
            }
        } else {
            startStepTracking()
        }
    }

    /** Start step-tracking foreground service so steps are counted even when app is closed. */
    private fun startStepTracking() {
        if (!StepCounterForegroundService.isRunning) {
            StepCounterForegroundService.start(this)
        }
    }

    private fun updateHomeCardsFromDomainData() {
        // Steps today from Health domain
        val stepsToday = DomainData.getStepsToday(this)
        findViewById<TextView>(R.id.home_steps_value_text).text =
            if (stepsToday > 0) "%,d".format(stepsToday) else "--"

        // Tasks completed vs total from Productivity domain
        val tasks = DomainData.getTasks(this)
        val completedTasks = tasks.count { it.completed }
        val totalTasks = tasks.size
        findViewById<TextView>(R.id.home_tasks_value_text).text =
            if (totalTasks > 0) "$completedTasks/$totalTasks" else "--"

        // Savings this month from Finance domain
        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        findViewById<TextView>(R.id.home_saved_value_text).text =
            if (balance > 0) "₹${String.format("%.0f", balance)}" else "--"

        // Weekly progress overall from same logic as WeeklyProgressOverviewActivity
        val stepsGoal = DomainData.getStepsGoal(this)
        val fitnessPercent = if (stepsGoal > 0) {
            (stepsToday * 100 / stepsGoal).coerceIn(0, 100)
        } else 0
        val productivityPercent = if (totalTasks > 0) {
            (completedTasks * 100 / totalTasks).coerceIn(0, 100)
        } else 0
        val lifestyleDays = DomainData.getLifestyleEntries(this).size
        val learningPercent = if (lifestyleDays > 0) {
            (lifestyleDays.coerceAtMost(7) * 100 / 7).coerceIn(0, 100)
        } else 0
        val financePercent = if (salary > 0) {
            ((balance / salary) * 100).toInt().coerceIn(0, 100)
        } else 0
        val percents = listOf(fitnessPercent, productivityPercent, financePercent, learningPercent)
            .filter { it > 0 }
        val overall = if (percents.isNotEmpty()) percents.sum() / percents.size else 0

        findViewById<TextView>(R.id.home_progress_value_text).text =
            if (overall > 0) "$overall%" else "--"
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> getString(R.string.home_greeting_morning)
            in 12..14 -> getString(R.string.home_greeting_afternoon)
            in 15..17 -> getString(R.string.home_greeting_evening)
            else -> getString(R.string.home_greeting_night)
        }
        findViewById<TextView>(R.id.home_greeting).text = greeting
        findViewById<TextView>(R.id.home_greeting_name).text =
            UserPrefs.getDisplayName(this) ?: getString(R.string.home_greeting_name)
    }

    private fun updateProfileAvatar() {
        val path = UserPrefs.getProfilePhotoUri(this)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                try {
                    val imageView = findViewById<ImageView>(R.id.home_profile)
                    imageView.setImageURI(android.net.Uri.fromFile(file))
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                } catch (e: Exception) {
                    e.printStackTrace()
                    // If anything goes wrong loading the saved photo, clear it so we don't keep crashing.
                    UserPrefs.saveProfilePhotoUri(this, null)
                }
            } else {
                // File no longer exists; clear stale reference.
                UserPrefs.saveProfilePhotoUri(this, null)
            }
        }
    }

    companion object {
        const val REQUEST_ACTIVITY_RECOGNITION = 1002
    }
}
