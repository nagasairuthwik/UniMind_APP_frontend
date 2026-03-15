package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class ProductivityAnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productivity_analytics)

        findViewById<ImageButton>(R.id.productivity_analytics_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.productivity_analytics_close).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.productivity_analytics_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_share))
                menu.add(0, 2, 1, getString(R.string.menu_full_insights))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "My productivity analytics from UniMind"))
                        2 -> startActivity(Intent(this@ProductivityAnalyticsActivity, InsightsActivity::class.java))
                        3 -> startActivity(Intent(this@ProductivityAnalyticsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<Button>(R.id.productivity_analytics_take_action).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        bindDomainData()
    }

    private fun bindDomainData() {
        val tasks = DomainData.getTasks(this)
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.completed }

        val productivityPercent = if (totalTasks > 0) {
            (completedTasks * 100 / totalTasks).coerceIn(0, 100)
        } else 0

        findViewById<TextView>(R.id.productivity_header_score_value).text =
            "$productivityPercent%"
        findViewById<TextView>(R.id.productivity_tasks_value).text =
            if (totalTasks > 0) "$completedTasks / $totalTasks" else "0 / 0"
    }
}
