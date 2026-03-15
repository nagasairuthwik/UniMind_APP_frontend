package com.simats.unimind

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class TasksOverviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_overview)

        findViewById<ImageButton>(R.id.tasks_overview_close).setOnClickListener { finish() }
        findViewById<Button>(R.id.tasks_overview_close_btn).setOnClickListener { finish() }

        updateFromDomainData()
    }

    private fun setWeeklyTrendBars(stepsPerDay: List<Int>) {
        val barIds = listOf(
            R.id.tasks_bar_mon, R.id.tasks_bar_tue, R.id.tasks_bar_wed,
            R.id.tasks_bar_thu, R.id.tasks_bar_fri, R.id.tasks_bar_sat, R.id.tasks_bar_sun
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

    private fun updateFromDomainData() {
        val tasks = DomainData.getTasks(this)
        val completed = tasks.count { it.completed }
        val total = tasks.size
        val remaining = (total - completed).coerceAtLeast(0)
        // For now, we treat every task as equal priority.
        val highPriority = 0
        val completionRate = if (total > 0) {
            (completed * 100 / total).coerceIn(0, 100)
        } else 0

        findViewById<TextView>(R.id.tasks_overview_value_text).text =
            if (total > 0) "$completed/$total" else "--"
        findViewById<TextView>(R.id.tasks_metric_completed_today_text).text =
            if (completed > 0) "$completed tasks" else "--"
        findViewById<TextView>(R.id.tasks_metric_remaining_text).text =
            if (remaining > 0) "$remaining tasks" else "--"
        findViewById<TextView>(R.id.tasks_metric_total_planned_text).text =
            if (total > 0) "$total tasks" else "--"
        findViewById<TextView>(R.id.tasks_metric_completion_rate_text).text =
            if (total > 0) "$completionRate%" else "--"
        findViewById<TextView>(R.id.tasks_metric_high_priority_text).text =
            if (highPriority > 0) "$highPriority pending" else "--"
        findViewById<TextView>(R.id.tasks_metric_avg_time_text).text = "--"

        // Weekly trend: steps per day (Mon–Sun) as activity indicator
        setWeeklyTrendBars(DomainData.getStepsForCurrentWeekDays(this))
    }
}
