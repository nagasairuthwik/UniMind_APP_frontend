package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class AiPersonalizationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_personalization)

        findViewById<ImageButton>(R.id.ai_personalization_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.ai_personalization_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_settings))
                menu.add(0, 2, 1, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@AiPersonalizationActivity, SettingsActivity::class.java))
                        2 -> startActivity(Intent(this@AiPersonalizationActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val proactive = findViewById<Switch>(R.id.ai_personalization_proactive)
        val learning = findViewById<Switch>(R.id.ai_personalization_learning)
        val explanations = findViewById<Switch>(R.id.ai_personalization_explanations)
        val scheduling = findViewById<Switch>(R.id.ai_personalization_scheduling)

        proactive.isChecked = UserPrefs.isAiProactive(this)
        learning.isChecked = UserPrefs.isAiLearning(this)
        explanations.isChecked = UserPrefs.isAiExplanations(this)
        scheduling.isChecked = UserPrefs.isAiScheduling(this)

        proactive.setOnCheckedChangeListener { _, isChecked ->
            UserPrefs.setAiProactive(this, isChecked)
        }
        learning.setOnCheckedChangeListener { _, isChecked ->
            UserPrefs.setAiLearning(this, isChecked)
        }
        explanations.setOnCheckedChangeListener { _, isChecked ->
            UserPrefs.setAiExplanations(this, isChecked)
        }
        scheduling.setOnCheckedChangeListener { _, isChecked ->
            UserPrefs.setAiScheduling(this, isChecked)
        }
    }
}
