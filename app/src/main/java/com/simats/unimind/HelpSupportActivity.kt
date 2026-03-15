package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class HelpSupportActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        findViewById<ImageButton>(R.id.help_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.help_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_how_it_works))
                menu.add(0, 2, 1, getString(R.string.menu_settings))
                menu.add(0, 3, 2, getString(R.string.menu_about))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@HelpSupportActivity, HowItWorksActivity::class.java))
                        2 -> startActivity(Intent(this@HelpSupportActivity, SettingsActivity::class.java))
                        3 -> Toast.makeText(this@HelpSupportActivity, getString(R.string.help_app_version), Toast.LENGTH_SHORT).show()
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.help_how_it_works).setOnClickListener {
            startActivity(Intent(this, HowItWorksActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.help_chat_support).setOnClickListener {
            Toast.makeText(this, "Chat Support", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.help_email_us).setOnClickListener {
            Toast.makeText(this, "Email Us", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.help_faq_1).setOnClickListener {
            Toast.makeText(this, getString(R.string.help_faq_1_q), Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.help_faq_2).setOnClickListener {
            Toast.makeText(this, getString(R.string.help_faq_2_q), Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.help_faq_3).setOnClickListener {
            Toast.makeText(this, getString(R.string.help_faq_3_q), Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.help_faq_4).setOnClickListener {
            Toast.makeText(this, getString(R.string.help_faq_4_q), Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.help_user_guide).setOnClickListener {
            Toast.makeText(this, "User Guide", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.help_video_tutorials).setOnClickListener {
            Toast.makeText(this, "Video Tutorials", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.help_community_forum).setOnClickListener {
            Toast.makeText(this, "Community Forum", Toast.LENGTH_SHORT).show()
        }
    }
}
