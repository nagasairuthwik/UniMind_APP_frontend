package com.simats.unimind

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

class SavingsGoalsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_goals)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.savings_goals_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.savings_goals_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_add_goal))
                menu.add(0, 2, 1, getString(R.string.menu_budget))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> showAddGoalDialog()
                        2 -> startActivity(Intent(this@SavingsGoalsActivity, BudgetActivity::class.java))
                        3 -> startActivity(Intent(this@SavingsGoalsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.savings_goals_add_btn).setOnClickListener {
            showAddGoalDialog()
        }

        refreshGoalsList()
    }

    override fun onResume() {
        super.onResume()
        refreshGoalsList()
    }

    private fun refreshGoalsList() {
        val container = findViewById<LinearLayout>(R.id.savings_goals_list_container)
        val emptyText = findViewById<TextView>(R.id.savings_goals_empty_text)
        container.removeAllViews()

        val goals = DomainData.getSavingsGoals(this)
        if (goals.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            return
        }
        emptyText.visibility = View.GONE

        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        val fmt = "%.0f"
        val totalTarget = goals.sumOf { it.target }
        val inflater = LayoutInflater.from(this)

        for (goal in goals) {
            val row = inflater.inflate(R.layout.item_savings_goal_card, container, false)
            val target = goal.target
            val saved = if (totalTarget > 0) {
                (balance * (target / totalTarget)).coerceAtMost(target)
            } else {
                0.0
            }
            val pct = if (target > 0) (saved * 100 / target).toInt().coerceIn(0, 100) else 0

            row.findViewById<TextView>(R.id.card_goal_title).text = goal.name
            row.findViewById<TextView>(R.id.card_goal_saved_text).text = "₹${String.format(fmt, saved)}"
            row.findViewById<TextView>(R.id.card_goal_target_text).text = "₹${String.format(fmt, target)}"
            row.findViewById<TextView>(R.id.card_goal_pct_text).text = "${pct}% of goal reached"

            val fill = row.findViewById<View>(R.id.card_goal_progress_fill)
            val track = row.findViewById<View>(R.id.card_goal_progress_track)
            fill.layoutParams = (fill.layoutParams as LinearLayout.LayoutParams).apply { weight = pct.toFloat() }
            track.layoutParams = (track.layoutParams as LinearLayout.LayoutParams).apply { weight = (100 - pct).toFloat() }

            row.findViewById<ImageButton>(R.id.card_goal_view).setOnClickListener {
                showGoalDetailsDialog(title = goal.name, saved = saved, target = target)
            }

            container.addView(row)
        }
    }

    private fun showGoalDetailsDialog(title: String, saved: Double, target: Double) {
        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        val remaining = (target - saved).coerceAtLeast(0.0)
        val pct = if (target > 0) (saved * 100 / target).toInt() else 0
        val fmt = "%.0f"

        val message = buildString {
            append("Goal: $title\n")
            append("Target: ₹${String.format(fmt, target)}\n")
            append("Saved (based on current balance): ₹${String.format(fmt, saved)} ($pct%)\n")
            append("Still to save: ₹${String.format(fmt, remaining)}\n\n")
            append("Current finance snapshot:\n")
            append("Salary this month: ₹${String.format(fmt, salary)}\n")
            append("Spent this month: ₹${String.format(fmt, spentMonth)}\n")
            append("Available balance: ₹${String.format(fmt, balance)}")
        }

        AlertDialog.Builder(this)
            .setTitle("Savings goal details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAddGoalDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_add_savings_goal, null)
        val nameEt = view.findViewById<EditText>(R.id.edit_goal_name)
        val targetEt = view.findViewById<EditText>(R.id.edit_goal_target)

        AlertDialog.Builder(this)
            .setTitle("Add new goal")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEt.text.toString().trim().ifBlank { "New goal" }
                val target = targetEt.text.toString().toDoubleOrNull() ?: 0.0
                if (target <= 0.0) {
                    Toast.makeText(this, "Enter a valid target amount.", Toast.LENGTH_SHORT).show()
                } else {
                    DomainData.addSavingsGoal(this, name, target)
                    refreshGoalsList()
                    GoalNotificationHelper.showSavingsGoalAdded(this, name)
                    val userId = UserPrefs.getUserId(this)
                    if (userId > 0) {
                        val title = getString(R.string.notification_savings_goal_added_title)
                        val body = getString(R.string.notification_savings_goal_added_body, name)
                        ApiClient.service.createNotification(NotificationCreateRequest(user_id = userId, domain = "finance", title = title, body = body))
                            .enqueue(object : Callback<okhttp3.ResponseBody> {
                                override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
                            })
                    }
                    val fmtNum = "%.0f"
                    Toast.makeText(this, "Goal \"$name\" with target ₹${String.format(fmtNum, target)} saved.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
