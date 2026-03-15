package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class HealthAnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_analytics)

        findViewById<ImageButton>(R.id.health_analytics_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.health_analytics_close).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.health_analytics_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_share))
                menu.add(0, 2, 1, getString(R.string.menu_full_insights))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "My health analytics from UniMind"))
                        2 -> startActivity(Intent(this@HealthAnalyticsActivity, InsightsActivity::class.java))
                        3 -> startActivity(Intent(this@HealthAnalyticsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<Button>(R.id.health_analytics_take_action).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        bindDomainData()
    }

    private fun bindDomainData() {
        val stepsToday = DomainData.getStepsToday(this)
        val stepsGoal = DomainData.getStepsGoal(this)
        val stepsHistory = DomainData.getStepsHistory(this)
        val avgSteps = stepsHistory.values.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0

        val fitnessPercent = if (stepsGoal > 0) {
            (stepsToday * 100 / stepsGoal).coerceIn(0, 100)
        } else 0

        val weeklyWorkouts = stepsHistory.values.count { it >= stepsGoal && stepsGoal > 0 }
        val calories = DomainData.stepsToCalories(stepsToday)

        val lifestyleEntries = DomainData.getLifestyleEntries(this)
        val avgSleep = lifestyleEntries.map { it.sleepHours }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 0.0
        val sleepQualityPercent = ((avgSleep / 8.0) * 100).toInt().coerceIn(0, 100)

        findViewById<TextView>(R.id.health_header_score_value).text = "$fitnessPercent%"
        findViewById<TextView>(R.id.health_weekly_workouts_value).text = weeklyWorkouts.toString()
        findViewById<TextView>(R.id.health_avg_steps_value).text = avgSteps.toString()
        findViewById<TextView>(R.id.health_calories_burned_value).text = calories.toString()
        findViewById<TextView>(R.id.health_sleep_quality_value).text = "$sleepQualityPercent%"
    }
}
