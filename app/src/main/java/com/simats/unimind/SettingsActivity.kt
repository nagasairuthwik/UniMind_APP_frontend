package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageButton>(R.id.settings_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.settings_sign_out).setOnClickListener {
            UserPrefs.clear(this)
            startActivity(
                Intent(this, LoginCheckActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
            finish()
        }

        findViewById<LinearLayout>(R.id.settings_nav_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        findViewById<LinearLayout>(R.id.settings_nav_insights).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.settings_nav_analytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
            finish()
        }
        // settings_nav_settings: already on Settings, no-op
        findViewById<LinearLayout>(R.id.settings_nav_chatbot).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
            finish()
        }

        // Settings rows
        findViewById<LinearLayout>(R.id.settings_row_ai_personalization).setOnClickListener {
            startActivity(Intent(this, AiPersonalizationActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.settings_row_privacy).setOnClickListener {
            startActivity(Intent(this, PrivacySecurityActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.settings_row_password).setOnClickListener {
            startActivity(Intent(this, PasswordAuthenticationActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.settings_row_language).setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.settings_row_help).setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.settings_row_about).setOnClickListener {
            Toast.makeText(this, "About UniMind", Toast.LENGTH_SHORT).show()
        }
    }
}
