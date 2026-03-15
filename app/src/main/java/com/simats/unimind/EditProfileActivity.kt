package com.simats.unimind

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class EditProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViewById<ImageButton>(R.id.edit_profile_back).setOnClickListener {
            finish()
        }

        val fullNameEdit = findViewById<EditText>(R.id.edit_profile_full_name)
        val emailEdit = findViewById<EditText>(R.id.edit_profile_email)
        val ageEdit = findViewById<EditText>(R.id.edit_profile_age)
        val dobEdit = findViewById<EditText>(R.id.edit_profile_dob)
        val phoneEdit = findViewById<EditText>(R.id.edit_profile_phone)

        dobEdit.setOnClickListener {
            showDatePicker(dobEdit)
        }

        val userId = UserPrefs.getUserId(this)
        if (userId > 0) {
            // Load existing profile from backend
            ApiClient.service.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val profile = response.body()?.profile
                        if (profile != null) {
                            fullNameEdit.setText(profile.full_name ?: "")
                            emailEdit.setText(profile.email ?: "")
                            ageEdit.setText(profile.age?.toString() ?: "")
                            // Backend stores dob as YYYY-MM-DD; show as-is
                            dobEdit.setText(profile.dob ?: "")
                            phoneEdit.setText(profile.phone ?: "")
                        }
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Failed to load profile: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Network error loading profile: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        val saveButton = findViewById<Button>(R.id.edit_profile_save)
        saveButton.setOnClickListener {
            if (userId <= 0) {
                Toast.makeText(this, "Please sign in again to edit your profile", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = fullNameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val ageText = ageEdit.text.toString().trim()
            val dobText = dobEdit.text.toString().trim()
            val phoneText = phoneEdit.text.toString().trim()

            val ageValue = ageText.toIntOrNull() ?: 0

            val request = ProfileSaveRequest(
                user_id = userId,
                full_name = fullName,
                age = ageValue,
                email = email.ifBlank { null },
                dob = dobText.ifBlank { null },
                phone = phoneText.ifBlank { null }
            )

            saveButton.isEnabled = false

            ApiClient.service.saveProfile(request).enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: Call<okhttp3.ResponseBody>,
                    response: Response<okhttp3.ResponseBody>
                ) {
                    saveButton.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                        UserPrefs.saveDisplayName(this@EditProfileActivity, fullName)
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Failed to update profile: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                    saveButton.isEnabled = true
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Network error updating profile: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                target.setText(formatted)
            },
            year,
            month,
            day
        )

        // Do not allow future dates for DOB
        datePicker.datePicker.maxDate = System.currentTimeMillis()

        datePicker.show()
    }
}

