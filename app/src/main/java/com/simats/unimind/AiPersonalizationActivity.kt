package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
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

        // Toggles: persist state in SharedPreferences if needed; for now they just work in UI
        findViewById<Switch>(R.id.ai_personalization_proactive).setOnCheckedChangeListener { _, isChecked ->
            // Optionally save: getSharedPreferences(...).edit().putBoolean("ai_proactive", isChecked).apply()
        }
        findViewById<Switch>(R.id.ai_personalization_learning).setOnCheckedChangeListener { _, _ -> }
        findViewById<Switch>(R.id.ai_personalization_explanations).setOnCheckedChangeListener { _, _ -> }
        findViewById<Switch>(R.id.ai_personalization_scheduling).setOnCheckedChangeListener { _, _ -> }
    }
}
