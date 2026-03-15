package com.simats.unimind

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class LifestyleEntryDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifestyle_entry_detail)

        val date = intent.getStringExtra(EXTRA_DATE) ?: ""
        val sleep = intent.getFloatExtra(EXTRA_SLEEP_HOURS, 0f)
        val stress = intent.getIntExtra(EXTRA_STRESS_LEVEL, 0)

        findViewById<TextView>(R.id.lifestyle_detail_date).text = date
        findViewById<TextView>(R.id.lifestyle_detail_sleep).text =
            "%.1f hours".format(sleep)
        findViewById<TextView>(R.id.lifestyle_detail_stress).text =
            stress.toString()
    }

    companion object {
        const val EXTRA_DATE = "date"
        const val EXTRA_SLEEP_HOURS = "sleep_hours"
        const val EXTRA_STRESS_LEVEL = "stress_level"
    }
}

