package com.simats.unimind

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class SearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById<ImageButton>(R.id.search_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.search_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_clear_search))
                menu.add(0, 2, 1, getString(R.string.menu_settings))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> clearRecentSearches()
                        2 -> startActivity(Intent(this@SearchActivity, SettingsActivity::class.java))
                        3 -> startActivity(Intent(this@SearchActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        // When user presses search on keyboard, ask UniMind AI in a popup.
        findViewById<EditText>(R.id.search_input).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = findViewById<EditText>(R.id.search_input).text.toString()
                handleSearchQuery(query)
                true
            } else false
        }

        // Quick command: previously opened full AI chat. Now reuse the same search popup with a helpful preset question.
        findViewById<LinearLayout>(R.id.search_cmd_ask_ai).setOnClickListener {
            val defaultQuestion = "Give me a quick summary of my UniMind data and how to improve."
            findViewById<EditText>(R.id.search_input).setText(defaultQuestion)
            handleSearchQuery(defaultQuestion)
        }

        // Recent searches: tap to fill input and ask UniMind AI.
        listOf(R.id.search_recent_1, R.id.search_recent_2, R.id.search_recent_3, R.id.search_recent_4).forEach { id ->
            findViewById<TextView>(id).setOnClickListener {
                val query = (findViewById<TextView>(id).text).toString()
                findViewById<EditText>(R.id.search_input).setText(query)
                handleSearchQuery(query)
            }
        }

        // Trending topics: tap to fill input and ask UniMind AI.
        listOf(R.id.search_trend_1, R.id.search_trend_2, R.id.search_trend_3, R.id.search_trend_4, R.id.search_trend_5).forEach { id ->
            findViewById<TextView>(id).setOnClickListener {
                val topic = (findViewById<TextView>(id).text).toString()
                findViewById<EditText>(R.id.search_input).setText(topic)
                handleSearchQuery(topic)
            }
        }
    }

    private fun clearRecentSearches() {
        listOf(R.id.search_recent_1, R.id.search_recent_2, R.id.search_recent_3, R.id.search_recent_4).forEach { id ->
            findViewById<TextView>(id).text = ""
        }
        Toast.makeText(this, "Recent searches cleared", Toast.LENGTH_SHORT).show()
    }

    private fun handleSearchQuery(raw: String) {
        val query = raw.trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Type something to ask UniMind.", Toast.LENGTH_SHORT).show()
            return
        }
        showAiAnswerPopup(query)
    }

    private fun showAiAnswerPopup(query: String) {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val textView = dialogView.findViewById<TextView>(android.R.id.text1)
        textView.text = "Asking UniMind AI about:\n\n\"$query\"\n\nPlease wait..."
        textView.setPadding(48, 48, 48, 48)

        val dialog = AlertDialog.Builder(this)
            .setTitle("UniMind AI answer")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()

        GeminiApi.sendChatMessage(query) { answer ->
            runOnUiThread {
                val safe = answer ?: "I couldn't get a response right now. Please check your connection and try again."
                textView.text = safe
            }
        }
    }
}
