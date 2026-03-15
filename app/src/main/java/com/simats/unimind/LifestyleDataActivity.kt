package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class LifestyleDataActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifestyle_data)

        findViewById<ImageButton>(R.id.lifestyle_data_back).setOnClickListener {
            finish()
        }

        val container = findViewById<LinearLayout>(R.id.lifestyle_data_container)
        container.removeAllViews()

        val entries = DomainData.getLifestyleEntries(this)
        if (entries.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No lifestyle logs yet. Save your first log to see data here."
                textSize = 14f
            }
            container.addView(empty)
            return
        }

        val inflater = LayoutInflater.from(this)
        for (entry in entries.sortedByDescending { it.date }) {
            val row = inflater.inflate(R.layout.item_lifestyle_data_entry, container, false)
            row.findViewById<TextView>(R.id.lifestyle_item_date).text = entry.date
            row.findViewById<TextView>(R.id.lifestyle_item_sleep).text =
                String.format("Sleep: %.1f hours", entry.sleepHours)
            row.findViewById<TextView>(R.id.lifestyle_item_stress).text =
                String.format("Stress: %d/10", entry.stressLevel)
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

