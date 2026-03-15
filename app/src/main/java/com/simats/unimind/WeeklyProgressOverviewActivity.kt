package com.simats.unimind

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class WeeklyProgressOverviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_progress_overview)

        findViewById<ImageButton>(R.id.progress_overview_close).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.progress_overview_close_btn).setOnClickListener {
            finish()
        }

        val reportTv = findViewById<TextView>(R.id.progress_ai_report)
        reportTv.text = "Loading AI progress…"

        buildWeekSummary()
    }

    private fun setWeeklyTrendBars(stepsPerDay: List<Int>) {
        val barIds = listOf(
            R.id.progress_bar_mon, R.id.progress_bar_tue, R.id.progress_bar_wed,
            R.id.progress_bar_thu, R.id.progress_bar_fri, R.id.progress_bar_sat, R.id.progress_bar_sun
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

    private fun buildWeekSummary() {
        val salary = DomainData.getSalary(this)
        val expenses = DomainData.getExpenses(this)
        val stepsHistory = DomainData.getStepsHistory(this)
        val tasks = DomainData.getTasks(this)
        val lifestyle = DomainData.getLifestyleEntries(this)

        // --- Compute live metrics for UI cards ---
        val stepsToday = DomainData.getStepsToday(this)
        val stepsGoal = DomainData.getStepsGoal(this)
        val fitnessPercent = if (stepsGoal > 0) {
            (stepsToday * 100 / stepsGoal).coerceIn(0, 100)
        } else 0

        val completedTasks = tasks.count { it.completed }
        val totalTasks = tasks.size
        val productivityPercent = if (totalTasks > 0) {
            (completedTasks * 100 / totalTasks).coerceIn(0, 100)
        } else 0

        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        val financePercent = if (salary > 0) {
            ((balance / salary) * 100).toInt().coerceIn(0, 100)
        } else 0

        val learningPercent = if (lifestyle.isNotEmpty()) {
            (lifestyle.size.coerceAtMost(7) * 100 / 7).coerceIn(0, 100)
        } else 0

        val percentList = listOf(fitnessPercent, productivityPercent, financePercent, learningPercent)
            .filter { it > 0 }
        val overallPercent = if (percentList.isNotEmpty()) {
            percentList.sum() / percentList.size
        } else 0

        // Simple "streak": number of days with any steps logged
        val streakDays = stepsHistory.size

        // --- Update card texts (reset from static values to live data) ---
        findViewById<TextView>(R.id.progress_overview_value_text).text =
            if (overallPercent > 0) "$overallPercent%" else "--"

        findViewById<TextView>(R.id.progress_metric_overall_value_text).text =
            if (overallPercent > 0) "$overallPercent%" else "--"

        findViewById<TextView>(R.id.progress_metric_fitness_value_text).text =
            if (stepsToday > 0) "${stepsToday} steps" else "--"

        findViewById<TextView>(R.id.progress_metric_productivity_value_text).text =
            if (totalTasks > 0) "$completedTasks/$totalTasks tasks" else "--"

        findViewById<TextView>(R.id.progress_metric_finance_value_text).text =
            if (salary > 0) "₹${String.format("%.0f", balance)} saved" else "--"

        findViewById<TextView>(R.id.progress_metric_learning_value_text).text =
            if (lifestyle.isNotEmpty()) "${lifestyle.size} days" else "--"

        findViewById<TextView>(R.id.progress_metric_streak_value_text).text =
            if (streakDays > 0) "$streakDays days" else "--"

        // --- Weekly trend: steps per day (Mon–Sun) from domain data ---
        setWeeklyTrendBars(DomainData.getStepsForCurrentWeekDays(this))

        // --- Build AI summary string for Gemini ---
        val financeSummary = if (salary > 0) "Salary $salary, ${expenses.size} expenses this month, balance $balance." else "No salary set."
        val stepsValues = stepsHistory.values
        val healthSummary = if (stepsValues.isNotEmpty()) "Steps: today $stepsToday, avg ${stepsValues.average().toInt()}, max ${stepsValues.maxOrNull() ?: 0}." else "No steps logged."
        val productivitySummary = "Tasks: $completedTasks completed, $totalTasks total."
        val lifestyleSummary = if (lifestyle.isNotEmpty()) "Sleep/stress logged ${lifestyle.size} days." else "No sleep/stress logged."

        val fullSummary = "Finance: $financeSummary Health: $healthSummary Productivity: $productivitySummary Lifestyle: $lifestyleSummary"
        val metrics = mapOf(
            "salary" to salary,
            "expenses_count" to expenses.size,
            "steps_avg" to (stepsValues.average().takeIf { !it.isNaN() } ?: 0),
            "tasks_completed" to tasks.count { it.completed },
            "lifestyle_days" to lifestyle.size
        )

        val reportTv = findViewById<TextView>(R.id.progress_ai_report)
        GeminiApi.getDomainProgressReport(
            "All domains",
            "this week",
            fullSummary,
            metrics
        ) { report ->
            runOnUiThread {
                reportTv.text = report ?: "Keep tracking daily for better insights."
            }
        }
    }
}
