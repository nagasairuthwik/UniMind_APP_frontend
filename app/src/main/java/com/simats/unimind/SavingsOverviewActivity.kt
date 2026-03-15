package com.simats.unimind

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class SavingsOverviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_overview)

        findViewById<ImageButton>(R.id.savings_overview_close).setOnClickListener { finish() }
        findViewById<Button>(R.id.savings_overview_close_btn).setOnClickListener { finish() }

        updateFromDomainData()
    }

    private fun setWeeklyTrendBars() {
        val salary = DomainData.getSalary(this)
        if (salary <= 0) return
        val weeklyTarget = salary / 4.0
        val spentPerWeek = DomainData.getSpentPerWeekOfMonth(this)
        val fillTrackIds = listOf(
            R.id.savings_week1_fill to R.id.savings_week1_track,
            R.id.savings_week2_fill to R.id.savings_week2_track,
            R.id.savings_week3_fill to R.id.savings_week3_track,
            R.id.savings_week4_fill to R.id.savings_week4_track
        )
        fillTrackIds.forEachIndexed { i, (fillId, trackId) ->
            val spent = spentPerWeek.getOrElse(i) { 0.0 }
            val saved = (weeklyTarget - spent).coerceAtLeast(0.0)
            val fillRatio = (saved / weeklyTarget).toFloat().coerceIn(0f, 1f)
            val trackRatio = 1f - fillRatio
            val fillView = findViewById<View>(fillId)
            val trackView = findViewById<View>(trackId)
            (fillView.layoutParams as? LinearLayout.LayoutParams)?.let { lp -> lp.weight = fillRatio; fillView.layoutParams = lp }
            (trackView.layoutParams as? LinearLayout.LayoutParams)?.let { lp -> lp.weight = trackRatio; trackView.layoutParams = lp }
        }
    }

    private fun updateFromDomainData() {
        val salary = DomainData.getSalary(this)
        val today = DomainData.todayDate()
        val spentToday = DomainData.getTotalSpentOnDate(this, today)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)

        val daysInMonth = java.util.Calendar.getInstance().getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val cal = java.util.Calendar.getInstance()
        val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val daysLeft = (daysInMonth - dayOfMonth).coerceAtLeast(0)

        val monthlyTarget = salary.takeIf { it > 0 } ?: 0.0
        val totalSavings = balance // simple model: what’s left this month
        val savingsRate = if (salary > 0) {
            ((balance / salary) * 100).toInt().coerceIn(0, 100)
        } else 0
        val dailyTarget = if (daysInMonth > 0 && monthlyTarget > 0) {
            monthlyTarget / daysInMonth
        } else 0.0

        findViewById<TextView>(R.id.savings_overview_value_text).text =
            if (balance > 0) "₹${String.format("%.0f", balance)}" else "--"

        findViewById<TextView>(R.id.savings_metric_saved_month_text).text =
            if (balance > 0) "₹${String.format("%.2f", balance)}" else "--"

        findViewById<TextView>(R.id.savings_metric_monthly_target_text).text =
            if (monthlyTarget > 0) "₹${String.format("%.0f", monthlyTarget)}" else "--"

        findViewById<TextView>(R.id.savings_metric_total_text).text =
            if (totalSavings > 0) "₹${String.format("%.2f", totalSavings)}" else "--"

        findViewById<TextView>(R.id.savings_metric_rate_text).text =
            if (salary > 0) "$savingsRate%" else "--"

        findViewById<TextView>(R.id.savings_metric_days_left_text).text =
            if (daysLeft > 0) "$daysLeft days" else "--"

        findViewById<TextView>(R.id.savings_metric_daily_target_text).text =
            if (dailyTarget > 0) "₹${String.format("%.2f", dailyTarget)}" else "--"

        // Weekly trend: savings per week (Week 1–4) from domain data
        setWeeklyTrendBars()
    }
}
