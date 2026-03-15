package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class AnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        findViewById<ImageButton>(R.id.analytics_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.analytics_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_view_detailed))
                menu.add(0, 2, 1, getString(R.string.menu_full_insights))
                menu.add(0, 3, 2, getString(R.string.menu_settings))
                menu.add(0, 4, 3, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> { startActivity(Intent(this@AnalyticsActivity, ProgressComparisonActivity::class.java)); true }
                        2 -> { startActivity(Intent(this@AnalyticsActivity, InsightsActivity::class.java)); true }
                        3 -> { startActivity(Intent(this@AnalyticsActivity, SettingsActivity::class.java)); true }
                        4 -> { startActivity(Intent(this@AnalyticsActivity, HelpSupportActivity::class.java)); true }
                        else -> false
                    }
                }
                show()
            }
        }

        findViewById<View>(R.id.analytics_view_detailed).setOnClickListener {
            startActivity(Intent(this, ProgressComparisonActivity::class.java))
        }

        findViewById<View>(R.id.analytics_full_insights).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        findViewById<View>(R.id.analytics_health_fitness_card).setOnClickListener {
            startActivity(Intent(this, HealthAnalyticsActivity::class.java))
        }

        findViewById<View>(R.id.analytics_productivity_card).setOnClickListener {
            startActivity(Intent(this, ProductivityAnalyticsActivity::class.java))
        }

        findViewById<View>(R.id.analytics_finance_card).setOnClickListener {
            startActivity(Intent(this, FinanceAnalyticsActivity::class.java))
        }

        findViewById<View>(R.id.analytics_lifestyle_card).setOnClickListener {
            startActivity(Intent(this, LifestyleAnalyticsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.analytics_nav_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP })
            finish()
        }
        findViewById<LinearLayout>(R.id.analytics_nav_insights).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.analytics_nav_analytics).setOnClickListener {
            // Already on Analytics
        }
        findViewById<LinearLayout>(R.id.analytics_nav_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.analytics_nav_chatbot).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
            finish()
        }

        refreshAnalyticsFromDomainData()
        loadNextWeekAiTips()
    }

    private fun refreshAnalyticsFromDomainData() {
        val stepsToday = DomainData.getStepsToday(this)
        val stepsGoal = DomainData.getStepsGoal(this)
        val fitnessPercent = if (stepsGoal > 0) {
            (stepsToday * 100 / stepsGoal).coerceIn(0, 100)
        } else 0

        val tasks = DomainData.getTasks(this)
        val completedTasks = tasks.count { it.completed }
        val totalTasks = tasks.size
        val productivityPercent = if (totalTasks > 0) {
            (completedTasks * 100 / totalTasks).coerceIn(0, 100)
        } else 0

        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        val financePercent = if (salary > 0) {
            ((balance / salary) * 100).toInt().coerceIn(0, 100)
        } else 0

        val lifestyleEntries = DomainData.getLifestyleEntries(this)
        val lifestyleDays = lifestyleEntries.size
        val lifestylePercent = if (lifestyleDays > 0) {
            (lifestyleDays.coerceAtMost(7) * 100 / 7).coerceIn(0, 100)
        } else 0

        val percents = listOf(fitnessPercent, productivityPercent, financePercent, lifestylePercent)
        val overall = if (percents.isNotEmpty()) percents.sum() / percents.size else 0

        findViewById<TextView>(R.id.analytics_overall_value_text).text = "$overall%"

        findViewById<TextView>(R.id.analytics_health_value_text).text = "$fitnessPercent%"
        findViewById<TextView>(R.id.analytics_health_change_text).text =
            if (fitnessPercent >= 80) "+ strong" else if (fitnessPercent >= 50) "+ improving" else "needs focus"

        findViewById<TextView>(R.id.analytics_productivity_value_text).text = "$productivityPercent%"
        findViewById<TextView>(R.id.analytics_productivity_change_text).text =
            if (productivityPercent >= 80) "+ consistent" else if (productivityPercent >= 50) "+ progress" else "low"

        findViewById<TextView>(R.id.analytics_finance_value_text).text = "$financePercent%"
        findViewById<TextView>(R.id.analytics_finance_change_text).text =
            if (financePercent >= 60) "+ stable" else "review budget"

        findViewById<TextView>(R.id.analytics_lifestyle_value_text).text = "$lifestylePercent%"
        findViewById<TextView>(R.id.analytics_lifestyle_change_text).text =
            if (lifestylePercent >= 60) "+ balanced" else "add more healthy days"
    }

    private fun loadNextWeekAiTips() {
        // Health
        val stepsHistory = DomainData.getStepsHistory(this)
        val stepsAvg = stepsHistory.values.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
        val stepsToday = DomainData.getStepsToday(this)
        val stepsGoal = DomainData.getStepsGoal(this)
        val healthSummary = if (stepsAvg > 0) {
            "Today $stepsToday steps, average $stepsAvg, goal $stepsGoal."
        } else {
            "No steps logged yet. Goal $stepsGoal."
        }
        GeminiApi.getDomainProgressReport(
            domainName = "Health",
            period = "next week",
            summary = healthSummary,
            metrics = mapOf("steps_today" to stepsToday, "steps_goal" to stepsGoal, "steps_avg" to stepsAvg)
        ) { text ->
            runOnUiThread {
                findViewById<TextView>(R.id.analytics_health_next_week_text).text =
                    text ?: getString(R.string.analytics_health_bullet2)
            }
        }

        // Productivity
        val tasks = DomainData.getTasks(this)
        val completedTasks = tasks.count { it.completed }
        val totalTasks = tasks.size
        val prodSummary = if (totalTasks > 0) {
            "Completed $completedTasks of $totalTasks tasks this week."
        } else {
            "No tasks created yet."
        }
        GeminiApi.getDomainProgressReport(
            domainName = "Productivity",
            period = "next week",
            summary = prodSummary,
            metrics = mapOf("tasks_total" to totalTasks, "tasks_completed" to completedTasks)
        ) { text ->
            runOnUiThread {
                findViewById<TextView>(R.id.analytics_productivity_next_week_text).text =
                    text ?: getString(R.string.analytics_productivity_bullet2)
            }
        }

        // Finance
        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        val financeSummary = "Salary %.0f, spent %.0f, balance %.0f.".format(salary, spentMonth, balance)
        GeminiApi.getDomainProgressReport(
            domainName = "Finance",
            period = "next week",
            summary = financeSummary,
            metrics = mapOf("salary" to salary, "spent_month" to spentMonth, "balance" to balance)
        ) { text ->
            runOnUiThread {
                findViewById<TextView>(R.id.analytics_finance_next_week_text).text =
                    text ?: getString(R.string.analytics_finance_bullet2)
            }
        }

        // Lifestyle
        val lifestyleEntries = DomainData.getLifestyleEntries(this)
        val avgSleep = lifestyleEntries.map { it.sleepHours }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val avgStress = lifestyleEntries.map { it.stressLevel }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val lifestyleSummary = if (lifestyleEntries.isNotEmpty()) {
            "Average sleep %.1f hours, stress %.1f/10.".format(avgSleep, avgStress)
        } else {
            "No lifestyle entries yet."
        }
        GeminiApi.getDomainProgressReport(
            domainName = "Lifestyle",
            period = "next week",
            summary = lifestyleSummary,
            metrics = mapOf("avg_sleep" to avgSleep, "avg_stress" to avgStress)
        ) { text ->
            runOnUiThread {
                findViewById<TextView>(R.id.analytics_lifestyle_next_week_text).text =
                    text ?: getString(R.string.analytics_lifestyle_bullet2)
            }
        }
    }
}
