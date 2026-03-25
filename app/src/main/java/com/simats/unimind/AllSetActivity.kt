package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AllSetActivity : AppCompatActivity() {

    private val domainTitleIds = listOf(
        R.string.domain_health,
        R.string.domain_productivity,
        R.string.domain_finance,
        R.string.domain_lifestyle
    )
    private val domainIconIds = listOf(
        R.drawable.ic_domain_health,
        R.drawable.ic_domain_productivity,
        R.drawable.ic_domain_finance,
        R.drawable.ic_domain_lifestyle
    )
    private val personalityTitleIds = listOf(
        R.string.personality_professional,
        R.string.personality_friendly,
        R.string.personality_creative
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_set)

        val domainsContainer = findViewById<LinearLayout>(R.id.all_set_domains_chips)
        val goalsContainer = findViewById<LinearLayout>(R.id.all_set_goals_container)
        val personalityName = findViewById<TextView>(R.id.all_set_personality_name)

        val domainIndices = OnboardingPrefs.getDomainIndices(this)
        if (domainIndices.isEmpty()) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_all_set_goal, domainsContainer, false)
            row.findViewById<TextView>(R.id.all_set_goal_text).text = getString(R.string.domain_health)
            domainsContainer.addView(row)
        } else {
            domainIndices.sorted().forEach { index ->
                if (index in domainTitleIds.indices) {
                    val row = LayoutInflater.from(this).inflate(R.layout.item_all_set_goal, domainsContainer, false)
                    row.findViewById<TextView>(R.id.all_set_goal_text).text = getString(domainTitleIds[index])
                    domainsContainer.addView(row)
                }
            }
        }

        val goals = OnboardingPrefs.getGoals(this)
        if (goals.isEmpty()) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_all_set_goal, goalsContainer, false)
            row.findViewById<TextView>(R.id.all_set_goal_text).text = getString(R.string.goal_sample)
            goalsContainer.addView(row)
        } else {
            goals.forEach { goal ->
                val row = LayoutInflater.from(this).inflate(R.layout.item_all_set_goal, goalsContainer, false)
                row.findViewById<TextView>(R.id.all_set_goal_text).text = goal
                goalsContainer.addView(row)
            }
        }

        val personalityIndex = OnboardingPrefs.getPersonalityIndex(this).coerceIn(0, personalityTitleIds.lastIndex)
        personalityName.text = getString(personalityTitleIds[personalityIndex])

        findViewById<Button>(R.id.all_set_continue).setOnClickListener {
            val intent = Intent(this, SubscriptionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
}
