package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<ImageButton>(R.id.forgot_password_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.forgot_password_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_sign_in))
                menu.add(0, 2, 1, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> { startActivity(Intent(this@ForgotPasswordActivity, SignInActivity::class.java)); finish() }
                        2 -> startActivity(Intent(this@ForgotPasswordActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val emailEdit = findViewById<EditText>(R.id.forgot_password_email)
        val sendLayout = findViewById<android.view.View>(R.id.forgot_password_send_link)
        val sendText = findViewById<TextView>(R.id.forgot_password_send_link_text)

        sendText.text = getString(R.string.forgot_password_send_otp)

        sendLayout.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            if (email.isEmpty()) {
                emailEdit.error = getString(R.string.forgot_password_email_hint)
                emailEdit.requestFocus()
                return@setOnClickListener
            }

            // Call backend to send OTP email before moving to OTP screen
            ApiClient.service.sendForgotOtp(ForgotPasswordOtpRequest(email))
                .enqueue(object : Callback<SimpleResponse> {
                    override fun onResponse(
                        call: Call<SimpleResponse>,
                        response: Response<SimpleResponse>
                    ) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                body.message ?: "OTP sent to your email.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@ForgotPasswordActivity, ForgotPasswordOtpActivity::class.java)
                            intent.putExtra(ForgotPasswordOtpActivity.EXTRA_EMAIL, email)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                body?.message ?: "Could not send OTP. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Cannot reach server: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        findViewById<TextView>(R.id.forgot_password_back_to_signin).setOnClickListener {
            finish()
        }
    }
}
