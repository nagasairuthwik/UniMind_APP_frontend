package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class SuggestionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestions)

        findViewById<ImageButton>(R.id.suggestions_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.suggestions_continue).setOnClickListener {
            startActivity(Intent(this, AllSetActivity::class.java))
        }
        findViewById<TextView>(R.id.suggestions_skip).setOnClickListener {
            startActivity(Intent(this, AllSetActivity::class.java))
        }
    }
}
