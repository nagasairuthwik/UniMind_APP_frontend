package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PermissionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        findViewById<ImageButton>(R.id.permissions_back).setOnClickListener {
            finish()
        }
        findViewById<ImageButton>(R.id.permissions_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_help))
                menu.add(0, 2, 1, getString(R.string.menu_skip))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@PermissionsActivity, HelpSupportActivity::class.java))
                        2 -> { startActivity(Intent(this@PermissionsActivity, AllSetActivity::class.java)); finish() }
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val notificationsSwitch = findViewById<Switch>(R.id.permission_notifications)
        val locationSwitch = findViewById<Switch>(R.id.permission_location)
        val calendarSwitch = findViewById<Switch>(R.id.permission_calendar)
        val healthSwitch = findViewById<Switch>(R.id.permission_health)

        // Toggles are for UI; actual Android runtime permission requests can be added when toggled
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Request notification permission (POST_NOTIFICATIONS on Android 13+)
            }
        }
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Request location permission
            }
        }
        calendarSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Request calendar permission
            }
        }
        healthSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Request health data permission (e.g. Health Connect)
            }
        }

        val continueButton = findViewById<Button>(R.id.permissions_continue)
        continueButton.setOnClickListener {
            val userId = intent.getIntExtra(ProfileSetupActivity.EXTRA_USER_ID, -1)
            // If we don't have a backend user id yet, just continue the flow.
            if (userId <= 0) {
                startActivity(Intent(this, ChatIntroActivity::class.java))
                return@setOnClickListener
            }

            val request = PermissionsUpdateRequest(
                user_id = userId,
                allow_notifications = notificationsSwitch.isChecked,
                allow_location = locationSwitch.isChecked,
                allow_calendar = calendarSwitch.isChecked,
                allow_health = healthSwitch.isChecked
            )

            continueButton.isEnabled = false
            ApiClient.service.savePermissions(request)
                .enqueue(object : Callback<okhttp3.ResponseBody> {
                    override fun onResponse(
                        call: Call<okhttp3.ResponseBody>,
                        response: Response<okhttp3.ResponseBody>
                    ) {
                        continueButton.isEnabled = true
                        if (!response.isSuccessful) {
                            Toast.makeText(
                                this@PermissionsActivity,
                                "Failed to save permissions: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        startActivity(Intent(this@PermissionsActivity, ChatIntroActivity::class.java))
                    }

                    override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                        continueButton.isEnabled = true
                        Toast.makeText(
                            this@PermissionsActivity,
                            "Network error saving permissions: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@PermissionsActivity, ChatIntroActivity::class.java))
                    }
                })
        }

        findViewById<TextView>(R.id.permissions_skip).setOnClickListener {
            startActivity(Intent(this, AllSetActivity::class.java))
        }
    }
}
