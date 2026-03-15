package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoalsActivity : ComponentActivity() {

    private val addedGoals = mutableSetOf<String>()
    private lateinit var addedContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        addedContainer = findViewById(R.id.goals_added_container)

        findViewById<ImageButton>(R.id.goals_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.goals_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_edit_domains))
                menu.add(0, 2, 1, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> { startActivity(Intent(this@GoalsActivity, DomainSelectionActivity::class.java)); finish() }
                        2 -> startActivity(Intent(this@GoalsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        addGoalChip(getString(R.string.goal_sample))

        findViewById<ImageButton>(R.id.goals_add_btn).setOnClickListener {
            val input = findViewById<EditText>(R.id.goals_input)
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                addGoalChip(text)
                input.text.clear()
            }
        }

        listOf(
            R.id.goals_suggested_1 to R.string.goal_suggested_1,
            R.id.goals_suggested_2 to R.string.goal_suggested_2,
            R.id.goals_suggested_3 to R.string.goal_suggested_3,
            R.id.goals_suggested_4 to R.string.goal_suggested_4,
            R.id.goals_suggested_5 to R.string.goal_suggested_5
        ).forEach { (id, stringId) ->
            findViewById<TextView>(id).setOnClickListener {
                val goal = getString(stringId)
                if (!addedGoals.contains(goal)) {
                    addGoalChip(goal)
                }
            }
        }

        findViewById<Button>(R.id.goals_continue).setOnClickListener {
            OnboardingPrefs.saveGoals(this, addedGoals)

            val userId = intent.getIntExtra(ProfileSetupActivity.EXTRA_USER_ID, -1)
            if (userId > 0) {
                val goalsText = addedGoals.joinToString(separator = "; ")
                ApiClient.service.saveGoals(GoalsUpdateRequest(user_id = userId, goals = goalsText))
                    .enqueue(object : Callback<okhttp3.ResponseBody> {
                        override fun onResponse(
                            call: Call<okhttp3.ResponseBody>,
                            response: Response<okhttp3.ResponseBody>
                        ) {
                            if (!response.isSuccessful) {
                                Toast.makeText(
                                    this@GoalsActivity,
                                    "Failed to save goals: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            val next = Intent(this@GoalsActivity, AiPersonalityActivity::class.java)
                            next.putExtra(ProfileSetupActivity.EXTRA_USER_ID, userId)
                            startActivity(next)
                        }

                        override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                            Toast.makeText(
                                this@GoalsActivity,
                                "Network error saving goals: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            val next = Intent(this@GoalsActivity, AiPersonalityActivity::class.java)
                            next.putExtra(ProfileSetupActivity.EXTRA_USER_ID, userId)
                            startActivity(next)
                        }
                    })
            } else {
                val next = Intent(this, AiPersonalityActivity::class.java)
                startActivity(next)
            }
        }
    }

    private fun addGoalChip(goalText: String) {
        if (addedGoals.contains(goalText)) return
        addedGoals.add(goalText)

        val chip = LayoutInflater.from(this).inflate(R.layout.item_goal_chip, addedContainer, false)
        chip.findViewById<TextView>(R.id.goal_chip_text).text = goalText
        chip.findViewById<ImageButton>(R.id.goal_chip_remove).setOnClickListener {
            addedGoals.remove(goalText)
            addedContainer.removeView(chip)
        }
        addedContainer.addView(chip)
    }
}
