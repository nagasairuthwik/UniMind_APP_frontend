package com.simats.unimind

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class InsightsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        findViewById<ImageButton>(R.id.insights_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.insights_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_refresh))
                menu.add(0, 2, 1, getString(R.string.menu_share))
                menu.add(0, 3, 2, getString(R.string.menu_settings))
                menu.add(0, 4, 3, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> { fetchAiRecommendations(); updateFromDomainData(); true }
                        2 -> { startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "Check out my UniMind insights!")); true }
                        3 -> { startActivity(Intent(this@InsightsActivity, SettingsActivity::class.java)); true }
                        4 -> { startActivity(Intent(this@InsightsActivity, HelpSupportActivity::class.java)); true }
                        else -> false
                    }
                }
                show()
            }
        }

        updateFromDomainData()
        fetchAiRecommendations()
    }

    private fun updateFromDomainData() {
        val stepsHistory = DomainData.getStepsHistory(this)
        val stepsDays = stepsHistory.size
        val avgSteps = stepsHistory.values.takeIf { it.isNotEmpty() }
            ?.average()?.toInt() ?: 0
        val maxSteps = stepsHistory.values.maxOrNull() ?: 0

        val tasks = DomainData.getTasks(this)
        val completedTasks = tasks.count { it.completed }
        val totalTasks = tasks.size

        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)

        val lifestyle = DomainData.getLifestyleEntries(this)
        val avgSleep = lifestyle.takeIf { it.isNotEmpty() }
            ?.map { it.sleepHours }?.average() ?: 0.0
        val avgStress = lifestyle.takeIf { it.isNotEmpty() }
            ?.map { it.stressLevel }?.average() ?: 0.0

        val daysOfActivity = listOf(
            stepsDays,
            lifestyle.size
        ).filter { it > 0 }.maxOrNull() ?: 0

        // Header: based on N days of activity
        findViewById<TextView>(R.id.insights_header_days).text =
            getString(R.string.insights_header_days, if (daysOfActivity > 0) daysOfActivity else 1)

        // Patterns: show concrete numbers from all domains
        val pattern1 = if (stepsDays > 0) {
            "Health: you logged steps on $stepsDays days. Average $avgSteps steps, best day $maxSteps steps."
        } else {
            "Health: start tracking your daily steps to unlock detailed insights."
        }
        val pattern2 = if (salary > 0) {
            val spentPct = if (salary > 0) ((spentMonth / salary) * 100).toInt().coerceIn(0, 100) else 0
            "Finance: monthly salary $salary, spent $spentMonth so far (${spentPct}%), estimated balance $balance."
        } else {
            "Finance: set your monthly salary and log expenses to see savings patterns."
        }
        val pattern3 = if (totalTasks > 0 || lifestyle.isNotEmpty()) {
            val prodPart = if (totalTasks > 0) "Productivity: $completedTasks of $totalTasks tasks completed." else ""
            val lifePart = if (lifestyle.isNotEmpty())
                " Lifestyle: average sleep ${"%.1f".format(avgSleep)} hours, average stress ${"%.1f".format(avgStress)}/10."
            else ""
            (prodPart + lifePart).trim()
        } else {
            "Productivity & lifestyle: add tasks and log sleep/stress to reveal more patterns."
        }

        findViewById<TextView>(R.id.insights_pattern_1_text).text = pattern1
        findViewById<TextView>(R.id.insights_pattern_2_text).text = pattern2
        findViewById<TextView>(R.id.insights_pattern_3_text).text = pattern3

        // Simple confidence score based on how many domains have any data
        var domainsWithData = 0
        if (stepsDays > 0) domainsWithData++
        if (totalTasks > 0) domainsWithData++
        if (salary > 0 || spentMonth > 0) domainsWithData++
        if (lifestyle.isNotEmpty()) domainsWithData++
        val confidence = (domainsWithData * 25).coerceIn(0, 100)
        findViewById<TextView>(R.id.insights_confidence_value).text = "$confidence%"
    }

    private fun fetchAiRecommendations() {
        // Ask Gemini for cross-domain recommendations using all four domains.
        GeminiApi.getDomainRecommendations(
            domainIndices = listOf(0, 1, 2, 3),
            goalsText = "",
            fullName = ""
        ) { tips ->
            runOnUiThread {
                if (tips.isEmpty()) {
                    findViewById<TextView>(R.id.insights_rec_1_text).text =
                        "Focus on small, consistent actions across health, productivity, finance, and lifestyle this week."
                    return@runOnUiThread
                }
                val values = tips.values.toList()
                findViewById<TextView>(R.id.insights_rec_1_text).text = values.getOrNull(0)
                    ?: "Take one small step in each domain today."
                findViewById<TextView>(R.id.insights_rec_2_text).text = values.getOrNull(1) ?: ""
                findViewById<TextView>(R.id.insights_rec_3_text).text = values.getOrNull(2) ?: ""
            }
        }
    }
}
