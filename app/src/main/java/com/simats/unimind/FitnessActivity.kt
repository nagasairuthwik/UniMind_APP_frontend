package com.simats.unimind

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat

class FitnessActivity : ComponentActivity() {

    private var stepCounterHelper: StepCounterHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness)

        // Ask Android system for permission to show notifications (Android 13+)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.fitness_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.fitness_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_view_progress))
                menu.add(0, 2, 1, getString(R.string.menu_set_step_goal))
                menu.add(0, 3, 2, getString(R.string.menu_reset_health))
                menu.add(0, 4, 3, getString(R.string.menu_settings))
                menu.add(0, 5, 4, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@FitnessActivity, StepsOverviewActivity::class.java))
                        2 -> showSetStepGoalDialog()
                        3 -> { DomainData.resetHealthData(this@FitnessActivity); refreshStepsUi(); Toast.makeText(this@FitnessActivity, "Health data reset.", Toast.LENGTH_LONG).show() }
                        4 -> startActivity(Intent(this@FitnessActivity, SettingsActivity::class.java))
                        5 -> startActivity(Intent(this@FitnessActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.fitness_recommendations).setOnClickListener {
            showRecommendationsPopup()
        }
        findViewById<LinearLayout>(R.id.fitness_view_progress).setOnClickListener {
            startActivity(Intent(this, StepsOverviewActivity::class.java))
        }

        refreshStepsUi()
    }

    override fun onResume() {
        super.onResume()
        refreshStepsUi()
        requestActivityRecognitionAndStartStepCounter()
    }

    override fun onPause() {
        super.onPause()
        stepCounterHelper?.stop()
    }

    private fun requestActivityRecognitionAndStartStepCounter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED -> {
                    startStepCounter()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACTIVITY_RECOGNITION) -> {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_ACTIVITY_RECOGNITION)
                }
                else -> {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_ACTIVITY_RECOGNITION)
                }
            }
        } else {
            startStepCounter()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepCounter()
        }
    }

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION = 1001

        /** Build user_data map for POST /domain/health from current DomainData. */
        fun buildHealthUserData(context: Context): Map<String, Any?> {
            val stepsToday = DomainData.getStepsToday(context)
            val stepsGoal = DomainData.getStepsGoal(context)
            val calories = DomainData.stepsToCalories(stepsToday)
            val distanceKm = DomainData.stepsToDistanceKm(stepsToday)
            val goalPercent = if (stepsGoal > 0) {
                (stepsToday * 100 / stepsGoal).coerceIn(0, 100)
            } else 0
            val last7 = DomainData.getStepsHistory(context).toList()
                .sortedBy { it.first }
                .takeLast(7)
                .map { it.second }
            val activeDaysThisWeek = DomainData.getStepsForCurrentWeekDays(context)
                .count { it > 0 }
            return mapOf(
                "steps_today" to stepsToday,
                "steps_goal" to stepsGoal,
                "calories_burned" to calories,
                "distance_km" to distanceKm,
                "goal_percent" to goalPercent,
                "last_7_days" to last7,
                "active_days_this_week" to activeDaysThisWeek
            )
        }
    }

    private fun startStepCounter() {
        if (!StepCounterForegroundService.isRunning) {
            StepCounterForegroundService.start(this)
        }
        if (stepCounterHelper == null) {
            stepCounterHelper = StepCounterHelper(this) { refreshStepsUi() }
        }
        stepCounterHelper?.start()
    }

    private fun refreshStepsUi() {
        val stepsToday = DomainData.getStepsToday(this)
        val stepsGoal = DomainData.getStepsGoal(this)
        findViewById<TextView>(R.id.fitness_steps_value).text = "%,d".format(stepsToday)
        val pct = if (stepsGoal > 0) (stepsToday * 100 / stepsGoal).coerceAtMost(100) else 0
        val fillWeight = pct.toFloat().coerceIn(0f, 100f)
        val emptyWeight = (100 - pct).toFloat().coerceIn(0f, 100f)
        findViewById<android.view.View>(R.id.fitness_progress_fill).layoutParams?.let { params ->
            if (params is LinearLayout.LayoutParams) {
                params.weight = fillWeight
                findViewById<android.view.View>(R.id.fitness_progress_fill).layoutParams = params
            }
        }
        findViewById<android.view.View>(R.id.fitness_progress_empty)?.layoutParams?.let { params ->
            if (params is LinearLayout.LayoutParams) {
                params.weight = emptyWeight
                findViewById<android.view.View>(R.id.fitness_progress_empty).layoutParams = params
            }
        }
        findViewById<TextView>(R.id.fitness_steps_goal_text).text = "%d%% of daily goal (%,d).".format(pct, stepsGoal)
        val calories = DomainData.stepsToCalories(stepsToday)
        findViewById<TextView>(R.id.fitness_calories_value).text = "%,d kcal".format(calories)

        // Keep foreground service notification in sync with latest steps
        StepCounterForegroundService.refreshNotification(this)

        // Sync latest health snapshot to backend if signed in
        saveHealthToBackend(null)

        // Trigger health goal notification once per day when goal reached
        if (stepsGoal > 0 && stepsToday >= stepsGoal) {
            val today = DomainData.todayDate()
            val lastNotified = DomainData.getHealthGoalNotifiedDate(this)
            if (lastNotified != today) {
                DomainData.setHealthGoalNotifiedToday(this, today)
                GoalNotificationHelper.showHealthGoalAchieved(this, stepsToday, stepsGoal)

                val userId = UserPrefs.getUserId(this)
                if (userId > 0) {
                    val title = getString(R.string.notification_health_goal_title)
                    val body = getString(R.string.notification_health_goal_body, stepsToday, stepsGoal)
                    val req = NotificationCreateRequest(
                        user_id = userId,
                        domain = "health",
                        title = title,
                        body = body
                    )
                    ApiClient.service.createNotification(req).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                        override fun onResponse(
                            call: retrofit2.Call<okhttp3.ResponseBody>,
                            response: retrofit2.Response<okhttp3.ResponseBody>
                        ) {
                            // no-op
                        }

                        override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                            // no-op
                        }
                    })
                }
            }
        }
    }

    private fun saveHealthToBackend(aiText: String? = null) {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            return
        }
        val request = DomainHealthRequest(
            user_id = userId,
            entry_date = DomainData.todayDate(),
            user_data = buildHealthUserData(this),
            ai_text = aiText
        )
        ApiClient.service.saveDomainHealth(request)
            .enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    response: retrofit2.Response<okhttp3.ResponseBody>
                ) {
                    if (!response.isSuccessful) {
                        val msg = response.errorBody()?.string() ?: "Error ${response.code()}"
                        android.util.Log.w("FitnessActivity", "saveDomainHealth: $msg")
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    t: Throwable
                ) {
                    android.util.Log.w("FitnessActivity", "saveDomainHealth failed: ${t.message}")
                }
            })
    }

    private fun showRecommendationsPopup() {
        val stepsToday = DomainData.getStepsToday(this)
        val stepsGoal = DomainData.getStepsGoal(this)
        val last7 = DomainData.getStepsHistory(this).toList().sortedBy { it.first }.takeLast(7).map { it.second }
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val textView = dialogView.findViewById<TextView>(android.R.id.text1)
        textView.text = "Loading recommendations..."
        textView.setPadding(48, 48, 48, 48)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Health recommendations")
            .setView(dialogView)
            .setPositiveButton("Done", null)
            .show()
        GeminiApi.getStepsInsight(stepsToday, stepsGoal, if (last7.isEmpty()) null else last7) { insight ->
            runOnUiThread {
                textView.text = insight ?: "Keep moving! Set a daily step goal and track your progress."
            }
        }
    }

    private fun showHealthMenu() {
        val options = arrayOf("Reset health data", "Set step goal")
        AlertDialog.Builder(this)
            .setTitle("Health options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        DomainData.resetHealthData(this)
                        refreshStepsUi()
                        Toast.makeText(this, "Health data reset. Step count will update from device.", Toast.LENGTH_LONG).show()
                    }
                    1 -> showSetStepGoalDialog()
                }
            }
            .show()
    }

    private fun showSetStepGoalDialog() {
        val edit = android.widget.EditText(this).apply {
            setPadding(48, 32, 48, 32)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(DomainData.getStepsGoal(this@FitnessActivity).toString())
            hint = "Daily step goal"
        }
        AlertDialog.Builder(this)
            .setTitle("Daily step goal")
            .setView(edit)
            .setPositiveButton("Save") { _, _ ->
                val v = edit.text.toString().toIntOrNull() ?: 10000
                if (v > 0) {
                    DomainData.setStepsGoal(this, v)
                    refreshStepsUi()
                    Toast.makeText(this, "Goal set to %,d steps".format(v), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
