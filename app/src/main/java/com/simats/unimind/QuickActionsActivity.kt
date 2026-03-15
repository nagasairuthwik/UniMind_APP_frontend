package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity

class QuickActionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_actions)

        findViewById<ImageButton>(R.id.quick_actions_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.quick_actions_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_home))
                menu.add(0, 2, 1, getString(R.string.menu_settings))
                menu.add(0, 3, 2, getString(R.string.menu_notifications))
                menu.add(0, 4, 3, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> { startActivity(Intent(this@QuickActionsActivity, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }); finish() }
                        2 -> startActivity(Intent(this@QuickActionsActivity, SettingsActivity::class.java))
                        3 -> startActivity(Intent(this@QuickActionsActivity, NotificationsActivity::class.java))
                        4 -> startActivity(Intent(this@QuickActionsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.qa_ai_chat).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.qa_lifestyle).setOnClickListener {
            startActivity(Intent(this, LifestyleOptimizationActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.qa_productivity).setOnClickListener {
            startActivity(Intent(this, ProductivityActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.qa_finance).setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.qa_health).setOnClickListener {
            startActivity(Intent(this, FitnessActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.qa_nav_home).setOnClickListener {
            finish()
        }
        findViewById<LinearLayout>(R.id.qa_nav_chat).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.qa_nav_analytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.qa_nav_profile).setOnClickListener {
            startActivity(Intent(this, MyProfileActivity::class.java))
        }
    }
}
