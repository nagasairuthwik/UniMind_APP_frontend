package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.welcome_get_started).setOnClickListener {
            startActivity(Intent(this, LoginCheckActivity::class.java))
            finish()
        }
    }
}
