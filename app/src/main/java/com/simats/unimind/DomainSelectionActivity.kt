package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat

class DomainSelectionActivity : ComponentActivity() {

    private val selectedDomains = mutableSetOf<Int>()
    private val cardIds = listOf(
        R.id.domain_card_health,
        R.id.domain_card_productivity,
        R.id.domain_card_finance,
        R.id.domain_card_lifestyle
    )

    private val cardBgUnselected by lazy {
        ContextCompat.getDrawable(this, R.drawable.welcome_card_background)
    }
    private val cardBgSelected by lazy {
        ContextCompat.getDrawable(this, R.drawable.domain_card_selected)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_domain_selection)

        findViewById<ImageButton>(R.id.domain_selection_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.domain_selection_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_how_it_works))
                menu.add(0, 2, 1, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@DomainSelectionActivity, HowItWorksActivity::class.java))
                        2 -> startActivity(Intent(this@DomainSelectionActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        cardIds.forEachIndexed { index, id ->
            findViewById<LinearLayout>(id).setOnClickListener {
                if (selectedDomains.contains(index)) {
                    selectedDomains.remove(index)
                } else {
                    selectedDomains.add(index)
                }
                updateCardStyle(index, id)
                updateContinueButton()
            }
        }

        updateContinueButton()

        findViewById<Button>(R.id.domain_selection_continue).setOnClickListener {
            OnboardingPrefs.saveDomainIndices(this, selectedDomains)
            val userId = intent.getIntExtra(ProfileSetupActivity.EXTRA_USER_ID, -1)
            val next = Intent(this, GoalsActivity::class.java)
            if (userId > 0) {
                next.putExtra(ProfileSetupActivity.EXTRA_USER_ID, userId)
            }
            startActivity(next)
        }
    }

    private fun updateCardStyle(index: Int, cardId: Int) {
        findViewById<LinearLayout>(cardId).background =
            if (selectedDomains.contains(index)) cardBgSelected else cardBgUnselected
    }

    private fun updateContinueButton() {
        cardIds.forEachIndexed { index, id ->
            updateCardStyle(index, id)
        }
        val count = selectedDomains.size
        findViewById<Button>(R.id.domain_selection_continue).text =
            getString(R.string.domain_continue, count)
    }
}
