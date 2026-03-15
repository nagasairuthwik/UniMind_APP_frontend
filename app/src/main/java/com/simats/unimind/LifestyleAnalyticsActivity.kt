package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class LifestyleAnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifestyle_analytics)

        findViewById<ImageButton>(R.id.lifestyle_analytics_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.lifestyle_analytics_close).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.lifestyle_analytics_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_share))
                menu.add(0, 2, 1, getString(R.string.menu_full_insights))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "My lifestyle analytics from UniMind"))
                        2 -> startActivity(Intent(this@LifestyleAnalyticsActivity, InsightsActivity::class.java))
                        3 -> startActivity(Intent(this@LifestyleAnalyticsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<Button>(R.id.lifestyle_analytics_take_action).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        bindDomainData()
    }

    private fun bindDomainData() {
        val entries = DomainData.getLifestyleEntries(this)
        val avgSleep = entries.map { it.sleepHours }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 0.0
        val daysWithSleep = entries.count { it.sleepHours > 0f }
        val lifestylePercent = if (daysWithSleep > 0) {
            (daysWithSleep.coerceAtMost(7) * 100 / 7)
        } else 0

        findViewById<TextView>(R.id.lifestyle_header_score_value).text = "$lifestylePercent%"
        findViewById<TextView>(R.id.lifestyle_sleep_value).text = "%.1f h/night".format(avgSleep)

        // Populate list of individual lifestyle logs (one row per entry)
        val container = findViewById<LinearLayout>(R.id.lifestyle_entries_container)
        container.removeAllViews()
        if (entries.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.lifestyle_sleep_average)
                textSize = 14f
                setTextColor(resources.getColor(R.color.on_surface, theme))
            }
            container.addView(empty)
        } else {
            val inflater = LayoutInflater.from(this)
            for (entry in entries.sortedBy { it.date }) {
                val row = inflater.inflate(android.R.layout.simple_list_item_2, container, false)
                row.findViewById<TextView>(android.R.id.text1).text = entry.date
                row.findViewById<TextView>(android.R.id.text2).text =
                    "%.1f h, stress %d/10".format(entry.sleepHours, entry.stressLevel)
                row.setOnClickListener {
                    val intent = Intent(this, LifestyleEntryDetailActivity::class.java).apply {
                        putExtra(LifestyleEntryDetailActivity.EXTRA_DATE, entry.date)
                        putExtra(LifestyleEntryDetailActivity.EXTRA_SLEEP_HOURS, entry.sleepHours)
                        putExtra(LifestyleEntryDetailActivity.EXTRA_STRESS_LEVEL, entry.stressLevel)
                    }
                    startActivity(intent)
                }
                container.addView(row)
            }
        }
    }
}
