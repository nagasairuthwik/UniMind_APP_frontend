package com.simats.unimind

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LifestyleOptimizationActivity : AppCompatActivity() {

    companion object {
        /** Build user_data map for POST /domain/lifestyle from current DomainData. */
        fun buildLifestyleUserData(context: Context): Map<String, Any?> {
            val entries = DomainData.getLifestyleEntries(context)
            val today = DomainData.todayDate()
            val todayEntry = entries.find { it.date == today }
            val avgSleep = entries.map { it.sleepHours }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
            val avgStress = entries.map { it.stressLevel }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
            val entriesPayload = entries.map { e ->
                mapOf(
                    "date" to e.date,
                    "sleep_hours" to e.sleepHours,
                    "stress_level" to e.stressLevel
                )
            }
            return mapOf(
                "today_date" to today,
                "today_sleep_hours" to (todayEntry?.sleepHours ?: 0f),
                "today_stress_level" to (todayEntry?.stressLevel ?: 0),
                "average_sleep_hours" to avgSleep,
                "average_stress_level" to avgStress,
                "entries" to entriesPayload
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifestyle_optimization)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.lifestyle_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.lifestyle_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_settings))
                menu.add(0, 2, 1, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@LifestyleOptimizationActivity, SettingsActivity::class.java))
                        2 -> startActivity(Intent(this@LifestyleOptimizationActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val sleepEt = findViewById<EditText>(R.id.lifestyle_sleep_hours)
        val stressEt = findViewById<EditText>(R.id.lifestyle_stress_level)
        val aiTip = findViewById<TextView>(R.id.lifestyle_ai_tip)

        val today = DomainData.getTodayLifestyle(this)
        if (today != null) {
            sleepEt.setText(today.sleepHours.toString())
            stressEt.setText(today.stressLevel.toString())
            GeminiApi.getLifestyleSuggestions(today.sleepHours, today.stressLevel) { tip ->
                runOnUiThread { aiTip.text = tip ?: "" }
            }
        }

        // Initial summary cards based on existing data
        updateSummaryCards()

        findViewById<Button>(R.id.lifestyle_open_data).setOnClickListener {
            startActivity(Intent(this, LifestyleDataActivity::class.java))
        }

        findViewById<Button>(R.id.lifestyle_save_log).setOnClickListener {
            val sleep = sleepEt.text.toString().toFloatOrNull() ?: 0f
            val stress = stressEt.text.toString().toIntOrNull() ?: 5
            if (sleep < 0f || sleep > 24f) {
                Toast.makeText(this, "Enter sleep hours 0–24", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clampedStress = stress.coerceIn(1, 10)
            DomainData.addLifestyleEntry(this, DomainData.todayDate(), sleep, clampedStress)
            GoalNotificationHelper.showLifestyleLogSaved(this)
            val userId = UserPrefs.getUserId(this)
            if (userId > 0) {
                val title = getString(R.string.notification_lifestyle_saved_title)
                val body = getString(R.string.notification_lifestyle_saved_body)
                ApiClient.service.createNotification(NotificationCreateRequest(user_id = userId, domain = "lifestyle", title = title, body = body))
                    .enqueue(object : Callback<okhttp3.ResponseBody> {
                        override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                        override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
                    })
            }
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            // Update UI and sync to backend immediately, even if AI call fails.
            updateSummaryCards()
            saveLifestyleToBackend(null)
            GeminiApi.getLifestyleSuggestions(sleep, clampedStress) { tip ->
                runOnUiThread {
                    aiTip.text = tip ?: "Aim for 7–8 hours of sleep."
                    // Optionally send AI text to backend when available
                    saveLifestyleToBackend(tip)
                }
            }
        }
    }

    /** Update lifestyle score, sleep quality, balance and stress cards from user data. */
    private fun updateSummaryCards() {
        val entries = DomainData.getLifestyleEntries(this)
        val scoreText = findViewById<TextView>(R.id.lifestyle_score_value_text)
        val scoreChange = findViewById<TextView>(R.id.lifestyle_score_change_text)
        val sleepAvgText = findViewById<TextView>(R.id.lifestyle_sleep_average_text)
        val balanceStatus = findViewById<TextView>(R.id.lifestyle_balance_status_text)
        val stressStatus = findViewById<TextView>(R.id.lifestyle_stress_status_text)
        val stressSuggestion = findViewById<TextView>(R.id.lifestyle_stress_suggestion_text)

        if (entries.isEmpty()) {
            scoreText.text = getString(R.string.lifestyle_score_value)
            scoreChange.text = getString(R.string.lifestyle_score_change)
            sleepAvgText.text = getString(R.string.lifestyle_sleep_average)
            balanceStatus.text = getString(R.string.lifestyle_balance_status)
            stressStatus.text = getString(R.string.lifestyle_stress_status)
            stressSuggestion.text = getString(R.string.lifestyle_stress_suggestion)
            return
        }

        val avgSleep = entries.map { it.sleepHours }.average()
        val avgStress = entries.map { it.stressLevel }.average()

        // 0–10 lifestyle score: 50% sleep (target 8h), 50% stress (1 best, 10 worst).
        val sleepScore = ((avgSleep / 8.0) * 10.0).coerceIn(0.0, 10.0)
        val stressScore = (((11.0 - avgStress) / 10.0) * 10.0).coerceIn(0.0, 10.0)
        val lifestyleScore = ((sleepScore + stressScore) / 2.0)

        scoreText.text = String.format("%.1f/10", lifestyleScore)
        scoreChange.text = "Based on your recent logs."

        sleepAvgText.text = String.format("Average: %.1f hours/night", avgSleep)

        // Update simple sleep progress bar (0–100% of 8 hours)
        val track = findViewById<LinearLayout>(R.id.lifestyle_sleep_progress_container)
        val fill = findViewById<View>(R.id.lifestyle_sleep_progress_fill)
        val empty = findViewById<View>(R.id.lifestyle_sleep_progress_empty)
        val percentOfTarget = (avgSleep / 8.0 * 100.0).coerceIn(0.0, 100.0)
        val fillWeight = percentOfTarget.toFloat()
        val emptyWeight = (100f - fillWeight).coerceIn(0f, 100f)
        (fill.layoutParams as? LinearLayout.LayoutParams)?.let {
            it.weight = fillWeight
            fill.layoutParams = it
        }
        (empty.layoutParams as? LinearLayout.LayoutParams)?.let {
            it.weight = emptyWeight
            empty.layoutParams = it
        }

        // Work-Life Balance status from sleep + stress
        balanceStatus.text = when {
            avgSleep in 7.0..9.0 && avgStress <= 4.0 ->
                "Well balanced"
            avgSleep in 6.0..7.0 || avgStress in 5.0..7.0 ->
                "Needs small adjustments"
            else ->
                "Out of balance"
        }

        // Stress card text
        when {
            avgStress <= 3.0 -> {
                stressStatus.text = "Low stress levels"
                stressSuggestion.text = "Keep your current routines to maintain this."
            }
            avgStress <= 6.0 -> {
                stressStatus.text = "Moderate stress levels"
                stressSuggestion.text = "Try short walks, breathing exercises, or a wind‑down routine."
            }
            else -> {
                stressStatus.text = "High stress levels"
                stressSuggestion.text = "Schedule regular breaks and relaxation time each day."
            }
        }
    }

    private fun saveLifestyleToBackend(aiText: String? = null) {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            // Not signed in; keep data local only.
            return
        }
        val request = DomainLifestyleRequest(
            user_id = userId,
            entry_date = DomainData.todayDate(),
            user_data = buildLifestyleUserData(this),
            ai_text = aiText
        )
        ApiClient.service.saveDomainLifestyle(request)
            .enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: Call<okhttp3.ResponseBody>,
                    response: Response<okhttp3.ResponseBody>
                ) {
                    if (!response.isSuccessful) {
                        val msg = response.errorBody()?.string() ?: "Error ${response.code()}"
                        android.util.Log.w("LifestyleActivity", "saveDomainLifestyle: $msg")
                    }
                }

                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                    android.util.Log.w("LifestyleActivity", "saveDomainLifestyle failed: ${t.message}")
                }
            })
    }
}
