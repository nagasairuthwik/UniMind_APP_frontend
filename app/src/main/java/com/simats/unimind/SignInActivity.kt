package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PREFILL_EMAIL = "prefill_email"
    }

    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Pre-fill email if coming from signup
        intent.getStringExtra(EXTRA_PREFILL_EMAIL)?.let { email ->
            findViewById<EditText>(R.id.signin_email).setText(email)
        }

        findViewById<ImageButton>(R.id.signin_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.signin_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_forgot_password))
                menu.add(0, 2, 1, getString(R.string.menu_create_account))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@SignInActivity, ForgotPasswordActivity::class.java))
                        2 -> startActivity(Intent(this@SignInActivity, SignupActivity::class.java))
                        3 -> startActivity(Intent(this@SignInActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val passwordEdit = findViewById<EditText>(R.id.signin_password)
        findViewById<ImageButton>(R.id.signin_password_toggle).setOnClickListener {
            passwordVisible = !passwordVisible
            passwordEdit.inputType = if (passwordVisible) {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            findViewById<ImageButton>(R.id.signin_password_toggle).setImageResource(
                if (passwordVisible) R.drawable.ic_eye_off_outline else R.drawable.ic_eye_outline
            )
        }

        findViewById<TextView>(R.id.signin_forgot_password).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        val emailEdit = findViewById<EditText>(R.id.signin_email)
        findViewById<Button>(R.id.signin_button).setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString()

            if (email.isEmpty()) {
                emailEdit.error = getString(R.string.signin_email_hint)
                emailEdit.requestFocus()
                return@setOnClickListener
            }
            if (!UniMindEmailPolicy.isAllowed(email)) {
                emailEdit.error = getString(R.string.signup_email_domain_error)
                emailEdit.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordEdit.error = getString(R.string.signin_password_hint)
                passwordEdit.requestFocus()
                return@setOnClickListener
            }

            ApiClient.service.login(LoginRequest(email, password))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            val userId = body?.user?.id
                            Toast.makeText(this@SignInActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            if (userId != null) {
                                UserPrefs.saveUserId(this@SignInActivity, userId)
                            }
                            body?.user?.full_name?.takeIf { it.isNotBlank() }?.let { name ->
                                UserPrefs.saveDisplayName(this@SignInActivity, name)
                            }
                            navigateAfterLogin(userId)
                        } else {
                            val msg = when (response.code()) {
                                400 -> "Email and password required"
                                401 -> "Invalid email or password"
                                else -> "Login failed: ${response.code()}"
                            }
                            Toast.makeText(this@SignInActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        val msg = "Cannot reach server: ${t.message}. Check: 1) Backend running (python app.py)? 2) ApiClient.BASE_URL = your PC IP?"
                        Toast.makeText(this@SignInActivity, msg, Toast.LENGTH_LONG).show()
                    }
                })
        }

        setupSignUpLink()
    }

    /**
     * After successful login:
     * - If backend already has a profile for this user, go straight to MainActivity.
     * - Otherwise, start the onboarding flow at ProfileSetupActivity.
     */
    private fun navigateAfterLogin(userId: Int?) {
        if (userId == null || userId <= 0) {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
            return
        }
        ApiClient.service.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                val profile = if (response.isSuccessful) response.body()?.profile else null
                if (profile != null) {
                    val intent = Intent(this@SignInActivity, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SignInActivity, ProfileSetupActivity::class.java)
                    intent.putExtra(ProfileSetupActivity.EXTRA_USER_ID, userId)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                val intent = Intent(this@SignInActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
        })
    }

    private fun setupSignUpLink() {
        val signUpView = findViewById<TextView>(R.id.signin_sign_up_link)
        val prefix = getString(R.string.signin_no_account)
        val signUpText = getString(R.string.signin_sign_up)
        val fullText = "$prefix $signUpText"

        val spannable = SpannableString(fullText)
        val signUpStart = fullText.indexOf(signUpText)
        if (signUpStart >= 0) {
            spannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        startActivity(Intent(this@SignInActivity, SignupActivity::class.java))
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                    }
                },
                signUpStart,
                signUpStart + signUpText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        signUpView.text = spannable
        signUpView.movementMethod = LinkMovementMethod.getInstance()
    }
}
