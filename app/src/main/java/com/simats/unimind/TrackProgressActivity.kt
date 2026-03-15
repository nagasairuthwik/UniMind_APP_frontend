package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class TrackProgressActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_progress)

        findViewById<ImageButton>(R.id.track_progress_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.track_progress_continue).setOnClickListener {
            startActivity(Intent(this, SuggestionsActivity::class.java))
        }
        findViewById<TextView>(R.id.track_progress_skip).setOnClickListener {
            startActivity(Intent(this, AllSetActivity::class.java))
        }
    }
}
