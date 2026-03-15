package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat

class AiPersonalityActivity : ComponentActivity() {

    private var selectedIndex = 0
    private val cardIds = listOf(
        R.id.ai_personality_card_0,
        R.id.ai_personality_card_1,
        R.id.ai_personality_card_2
    )
    private val titleIds = listOf(
        R.string.personality_professional,
        R.string.personality_friendly,
        R.string.personality_creative
    )
    private val descIds = listOf(
        R.string.personality_professional_desc,
        R.string.personality_friendly_desc,
        R.string.personality_creative_desc
    )

    private val cardBgSelected by lazy {
        ContextCompat.getDrawable(this, R.drawable.ai_personality_card_selected)
    }
    private val cardBgUnselected by lazy {
        ContextCompat.getDrawable(this, R.drawable.ai_personality_card_unselected)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_personality)

        findViewById<ImageButton>(R.id.ai_personality_back).setOnClickListener {
            finish()
        }
        findViewById<ImageButton>(R.id.ai_personality_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@AiPersonalityActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        cardIds.forEachIndexed { index, id ->
            val cardRoot = findViewById<ViewGroup>(id)
            val innerLayout = cardRoot.getChildAt(1) as LinearLayout
            (innerLayout.getChildAt(0) as TextView).text = getString(titleIds[index])
            (innerLayout.getChildAt(1) as TextView).text = getString(descIds[index])
            cardRoot.setOnClickListener {
                selectedIndex = index
                updateSelection()
            }
        }
        updateSelection()

        findViewById<Button>(R.id.ai_personality_continue).setOnClickListener {
            OnboardingPrefs.savePersonalityIndex(this, selectedIndex)
            val next = Intent(this, PermissionsActivity::class.java)
            val userId = intent.getIntExtra(ProfileSetupActivity.EXTRA_USER_ID, -1)
            if (userId > 0) {
                next.putExtra(ProfileSetupActivity.EXTRA_USER_ID, userId)
            }
            startActivity(next)
        }
    }

    private fun updateSelection() {
        cardIds.forEachIndexed { index, id ->
            val cardRoot = findViewById<ViewGroup>(id)
            cardRoot.background = if (index == selectedIndex) cardBgSelected else cardBgUnselected
            val checkView = cardRoot.getChildAt(2) as ImageView
            checkView.visibility = if (index == selectedIndex) View.VISIBLE else View.GONE
        }
    }
}
