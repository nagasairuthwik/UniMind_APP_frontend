package com.simats.unimind

import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class AiChatActivity : ComponentActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        messagesContainer = findViewById(R.id.ai_chat_messages)
        scrollView = findViewById(R.id.ai_chat_scroll)
        input = findViewById(R.id.ai_chat_input)

        findViewById<ImageButton>(R.id.ai_chat_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.ai_chat_send).setOnClickListener {
            sendMessage()
        }

        findViewById<ImageButton>(R.id.ai_chat_mic).setOnClickListener {
            Toast.makeText(this, "Voice input", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.ai_chat_nav_home).setOnClickListener {
            startActivity(android.content.Intent(this, MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
        findViewById<LinearLayout>(R.id.ai_chat_nav_insights).setOnClickListener {
            startActivity(android.content.Intent(this, InsightsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.ai_chat_nav_analytics).setOnClickListener {
            startActivity(android.content.Intent(this, AnalyticsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.ai_chat_nav_settings).setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
            finish()
        }
        // ai_chat_nav_chatbot: already on Chatbot, no-op
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return

        input.text.clear()
        addUserBubble(text)
        val aiBubble = addAiBubble("Thinking…")
        scrollToBottom()

        GeminiApi.sendChatMessage(text) { response ->
            runOnUiThread {
                aiBubble.text = response ?: "Something went wrong. Please try again."
                scrollToBottom()
            }
        }
    }

    private fun addUserBubble(message: String) {
        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundResource(R.drawable.chat_bubble_user)
            addView(TextView(this@AiChatActivity).apply {
                text = message
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 15f
                setPadding(0, 0, 0, dp(2))
            })
        }
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(12)
            gravity = Gravity.END
        }
        bubble.layoutParams = params
        messagesContainer.addView(bubble)
    }

    private fun addAiBubble(placeholder: String): TextView {
        val textView = TextView(this).apply {
            text = placeholder
            setTextColor(ContextCompat.getColor(this@AiChatActivity, R.color.on_surface))
            textSize = 15f
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.chat_bubble_ai)
            addView(textView)
        }
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(12)
            gravity = Gravity.START
        }
        bubble.layoutParams = params
        messagesContainer.addView(bubble)
        return textView
    }

    private fun scrollToBottom() {
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun dp(px: Int): Int = (px * resources.displayMetrics.density).toInt()
}
