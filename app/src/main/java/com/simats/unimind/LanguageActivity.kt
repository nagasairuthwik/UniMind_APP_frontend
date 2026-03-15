package com.simats.unimind

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity

class LanguageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        findViewById<ImageButton>(R.id.language_back).setOnClickListener {
            finish()
        }
    }
}
