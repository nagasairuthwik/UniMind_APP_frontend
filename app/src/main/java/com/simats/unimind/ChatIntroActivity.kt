package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class ChatIntroActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_intro)

        findViewById<ImageButton>(R.id.chat_intro_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.chat_intro_continue).setOnClickListener {
            startActivity(Intent(this, TrackProgressActivity::class.java))
        }
        findViewById<TextView>(R.id.chat_intro_skip).setOnClickListener {
            startActivity(Intent(this, AllSetActivity::class.java))
        }
    }
}
