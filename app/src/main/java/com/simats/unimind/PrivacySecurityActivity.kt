package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class PrivacySecurityActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_security)

        findViewById<ImageButton>(R.id.privacy_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.privacy_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_help))
                menu.add(0, 2, 1, getString(R.string.menu_settings))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@PrivacySecurityActivity, HelpSupportActivity::class.java))
                        2 -> startActivity(Intent(this@PrivacySecurityActivity, SettingsActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.privacy_row_change_password).setOnClickListener {
            Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.privacy_row_settings).setOnClickListener {
            Toast.makeText(this, "Privacy Settings", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.privacy_row_download).setOnClickListener {
            Toast.makeText(this, "Download My Data", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.privacy_row_delete).setOnClickListener {
            Toast.makeText(this, "Delete My Account", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.privacy_terms).setOnClickListener {
            Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.privacy_policy_link).setOnClickListener {
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show()
        }
    }
}
