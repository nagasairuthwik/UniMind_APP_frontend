package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordOtpActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EMAIL = "email"
        const val EXTRA_OTP = "otp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_otp)

        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        if (email.isEmpty()) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.forgot_password_otp_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.forgot_password_otp_email_text).text = email

        val otpEdit = findViewById<EditText>(R.id.forgot_password_otp_input)
        val verifyLayout = findViewById<LinearLayout>(R.id.forgot_password_verify_btn)

        verifyLayout.setOnClickListener {
            val otp = otpEdit.text.toString().trim()
            if (otp.isEmpty()) {
                otpEdit.error = getString(R.string.forgot_password_otp_hint)
                otpEdit.requestFocus()
                return@setOnClickListener
            }

            ApiClient.service.verifyForgotOtp(ForgotPasswordVerifyRequest(email, otp))
                .enqueue(object : Callback<SimpleResponse> {
                    override fun onResponse(
                        call: Call<SimpleResponse>,
                        response: Response<SimpleResponse>
                    ) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            Toast.makeText(
                                this@ForgotPasswordOtpActivity,
                                body.message ?: "OTP verified.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@ForgotPasswordOtpActivity, ForgotPasswordNewPassActivity::class.java)
                            intent.putExtra(ForgotPasswordNewPassActivity.EXTRA_EMAIL, email)
                            intent.putExtra(ForgotPasswordNewPassActivity.EXTRA_OTP, otp)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@ForgotPasswordOtpActivity,
                                body?.message ?: "Invalid or expired OTP.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                        Toast.makeText(
                            this@ForgotPasswordOtpActivity,
                            "Cannot reach server: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}

