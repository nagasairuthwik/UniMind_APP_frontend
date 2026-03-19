package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
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

        // Change password → go to Password & Authentication screen
        findViewById<LinearLayout>(R.id.privacy_row_change_password).setOnClickListener {
            startActivity(Intent(this, PasswordAuthenticationActivity::class.java))
        }

        // Privacy settings → reuse Permissions screen (app permissions & privacy toggles)
        findViewById<LinearLayout>(R.id.privacy_row_settings).setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }

        // Download my data → simple confirmation and status message for now
        findViewById<LinearLayout>(R.id.privacy_row_download).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Download my data")
                .setMessage("We will prepare a copy of your UniMind data that you can save or share from your device. This may take a few moments.")
                .setPositiveButton("OK") { _, _ ->
                    Toast.makeText(this, "Preparing your data…", Toast.LENGTH_SHORT).show()
                    // Backend export endpoint can be called from here when available.
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Delete account → confirm, then clear local user session
        findViewById<LinearLayout>(R.id.privacy_row_delete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete my account")
                .setMessage("This will sign you out and clear your data from this device. If your account is synced to the cloud, it will also need to be deleted from the server.")
                .setPositiveButton("Delete") { _, _ ->
                    UserPrefs.clear(this)
                    Toast.makeText(this, "Account removed from this device.", Toast.LENGTH_LONG).show()
                    startActivity(
                        Intent(this, LoginCheckActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<TextView>(R.id.privacy_terms).setOnClickListener {
            Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.privacy_policy_link).setOnClickListener {
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show()
        }
    }
}
