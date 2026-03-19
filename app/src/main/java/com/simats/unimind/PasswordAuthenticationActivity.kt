package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class PasswordAuthenticationActivity : ComponentActivity() {

    private var currentPasswordVisible = false
    private var newPasswordVisible = false
    private var confirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_authentication)

        findViewById<ImageButton>(R.id.password_auth_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.password_auth_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_help))
                menu.add(0, 2, 1, getString(R.string.menu_settings))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@PasswordAuthenticationActivity, HelpSupportActivity::class.java))
                        2 -> startActivity(Intent(this@PasswordAuthenticationActivity, SettingsActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val currentEdit = findViewById<EditText>(R.id.password_auth_current_edit)
        findViewById<ImageButton>(R.id.password_auth_current_toggle).setOnClickListener {
            currentPasswordVisible = !currentPasswordVisible
            togglePasswordVisibility(currentEdit, findViewById(R.id.password_auth_current_toggle), currentPasswordVisible)
        }
        val newEdit = findViewById<EditText>(R.id.password_auth_new_edit)
        findViewById<ImageButton>(R.id.password_auth_new_toggle).setOnClickListener {
            newPasswordVisible = !newPasswordVisible
            togglePasswordVisibility(newEdit, findViewById(R.id.password_auth_new_toggle), newPasswordVisible)
        }
        val confirmEdit = findViewById<EditText>(R.id.password_auth_confirm_edit)
        findViewById<ImageButton>(R.id.password_auth_confirm_toggle).setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            togglePasswordVisibility(confirmEdit, findViewById(R.id.password_auth_confirm_toggle), confirmPasswordVisible)
        }

        findViewById<Button>(R.id.password_auth_update_btn).setOnClickListener {
            Toast.makeText(this, "Update Password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePasswordVisibility(edit: EditText, toggle: ImageButton, visible: Boolean) {
        edit.inputType = if (visible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        toggle.setImageResource(
            if (visible) R.drawable.ic_eye_off_outline else R.drawable.ic_eye_outline
        )
    }
}
