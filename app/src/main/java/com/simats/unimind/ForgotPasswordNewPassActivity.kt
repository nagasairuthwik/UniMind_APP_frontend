package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordNewPassActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EMAIL = "email"
        const val EXTRA_OTP = "otp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_new_pass)

        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        val otp = intent.getStringExtra(EXTRA_OTP) ?: ""
        if (email.isEmpty() || otp.isEmpty()) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.forgot_password_new_back).setOnClickListener {
            finish()
        }

        val newPassEdit = findViewById<EditText>(R.id.forgot_password_new_pass_input)
        val confirmPassEdit = findViewById<EditText>(R.id.forgot_password_confirm_pass_input)
        val resetLayout = findViewById<LinearLayout>(R.id.forgot_password_reset_btn)

        resetLayout.setOnClickListener {
            val newPass = newPassEdit.text.toString()
            val confirmPass = confirmPassEdit.text.toString()

            if (newPass.isEmpty()) {
                newPassEdit.error = getString(R.string.forgot_password_new_pass_hint)
                newPassEdit.requestFocus()
                return@setOnClickListener
            }
            if (confirmPass != newPass) {
                confirmPassEdit.error = getString(R.string.forgot_password_confirm_pass_hint)
                confirmPassEdit.requestFocus()
                return@setOnClickListener
            }

            ApiClient.service.resetForgotPassword(
                ForgotPasswordResetRequest(
                    email = email,
                    otp = otp,
                    new_password = newPass
                )
            ).enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Toast.makeText(
                            this@ForgotPasswordNewPassActivity,
                            body.message ?: "Password reset successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@ForgotPasswordNewPassActivity, SignInActivity::class.java)
                        intent.putExtra(SignInActivity.EXTRA_PREFILL_EMAIL, email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@ForgotPasswordNewPassActivity,
                            body?.message ?: "Could not reset password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ForgotPasswordNewPassActivity,
                        "Cannot reach server: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }
}

