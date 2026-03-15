package com.simats.unimind

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class StepsOverviewActivity : ComponentActivity() {

    private var stepCounterHelper: StepCounterHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_overview)

        findViewById<ImageButton>(R.id.steps_overview_close).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.steps_overview_close_btn).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.steps_reset_health).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset health data")
                .setMessage("Clear all steps history and start fresh with accurate device step count?")
                .setPositiveButton("Reset") { _, _ ->
                    DomainData.resetHealthData(this)
                    refreshStepsUi()
                    Toast.makeText(this, "Health data reset. Steps will update from device.", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        refreshStepsUi()
    }

    override fun onResume() {
        super.onResume()
        refreshStepsUi()
        NotificationPermissionHelper.ensureNotificationPermission(this)
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

        findViewById<TextView>(R.id.steps_overview_value).text = "%,d".format(stepsToday)
        findViewById<TextView>(R.id.steps_metric_target_value).text = "%,d".format(stepsGoal)
        findViewById<TextView>(R.id.steps_metric_current_value).text = "%,d".format(stepsToday)
        val remaining = (stepsGoal - stepsToday).coerceAtLeast(0)
        findViewById<TextView>(R.id.steps_metric_remaining_value).text = "%,d".format(remaining)
        findViewById<TextView>(R.id.steps_metric_calories_value).text = "%,d kcal".format(DomainData.stepsToCalories(stepsToday))
        findViewById<TextView>(R.id.steps_metric_distance_value).text = "%.2f km".format(DomainData.stepsToDistanceKm(stepsToday))

        // Keep steps notification in sync when user views/updates overview
        StepCounterForegroundService.refreshNotification(this)

        val last7 = DomainData.getStepsHistory(this).toList().sortedBy { it.first }.takeLast(7).map { it.second }
        val aiTipText = findViewById<TextView>(R.id.steps_ai_tip_text)
        GeminiApi.getStepsInsight(stepsToday, stepsGoal, if (last7.isEmpty()) null else last7) { insight ->
            runOnUiThread { aiTipText.text = insight }
        }

        // Weekly trend: steps per day (Mon–Sun) from domain data
        setWeeklyTrendBars(DomainData.getStepsForCurrentWeekDays(this))

        findViewById<TextView>(R.id.steps_overview_value).setOnClickListener {
            showUpdateStepsDialog(stepsToday, stepsGoal)
        }
    }

    private fun setWeeklyTrendBars(stepsPerDay: List<Int>) {
        val barIds = listOf(
            R.id.steps_bar_mon, R.id.steps_bar_tue, R.id.steps_bar_wed,
            R.id.steps_bar_thu, R.id.steps_bar_fri, R.id.steps_bar_sat, R.id.steps_bar_sun
        )
        val maxSteps = stepsPerDay.maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxBarDp = 36f
        val minBarDp = 4f
        val density = resources.displayMetrics.density
        barIds.forEachIndexed { i, id ->
            val bar = findViewById<View>(id)
            val steps = stepsPerDay.getOrElse(i) { 0 }
            val heightDp = if (maxSteps > 0) (steps.toFloat() / maxSteps * maxBarDp).coerceIn(minBarDp, maxBarDp) else minBarDp
            (bar.layoutParams as? LinearLayout.LayoutParams)?.let { lp ->
                lp.height = (heightDp * density).toInt()
                bar.layoutParams = lp
            }
        }
    }

    private fun showUpdateStepsDialog(current: Int, goal: Int) {
        val edit = EditText(this).apply {
            setPadding(48, 32, 48, 32)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(if (current > 0) current.toString() else "")
            hint = "Steps today"
        }
        AlertDialog.Builder(this)
            .setTitle("Update steps")
            .setView(edit)
            .setPositiveButton("Save") { _, _ ->
                val v = edit.text.toString().toIntOrNull() ?: 0
                if (v >= 0) {
                    DomainData.setStepsToday(this, v)
                    refreshStepsUi()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        const val EXTRA_STEPS_TODAY = "steps_today"
        const val EXTRA_STEPS_GOAL = "steps_goal"
        private const val REQUEST_ACTIVITY_RECOGNITION = 1002
    }
}
