package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class LoginCheckActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_check)

        findViewById<View>(R.id.login_check_back).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.login_check_create_account).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.login_check_sign_in).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}
