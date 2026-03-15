package com.simats.unimind

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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

class BudgetActivity : ComponentActivity() {

    private fun showEditBudgetDialog() {
        val salary = DomainData.getSalary(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_edit_budget, null)
        val foodEt = view.findViewById<EditText>(R.id.edit_budget_food)
        val transportEt = view.findViewById<EditText>(R.id.edit_budget_transport)
        val entertainmentEt = view.findViewById<EditText>(R.id.edit_budget_entertainment)
        val billsEt = view.findViewById<EditText>(R.id.edit_budget_bills)

        val currentFood = DomainData.getBudgetFood(this)
        val currentTransport = DomainData.getBudgetTransport(this)
        val currentEntertainment = DomainData.getBudgetEntertainment(this)
        val currentBills = DomainData.getBudgetBills(this)

        if (currentFood > 0) foodEt.setText(currentFood.toInt().toString())
        if (currentTransport > 0) transportEt.setText(currentTransport.toInt().toString())
        if (currentEntertainment > 0) entertainmentEt.setText(currentEntertainment.toInt().toString())
        if (currentBills > 0) billsEt.setText(currentBills.toInt().toString())

        AlertDialog.Builder(this)
            .setTitle("Edit budget")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val food = foodEt.text.toString().toDoubleOrNull() ?: 0.0
                val transport = transportEt.text.toString().toDoubleOrNull() ?: 0.0
                val entertainment = entertainmentEt.text.toString().toDoubleOrNull() ?: 0.0
                val bills = billsEt.text.toString().toDoubleOrNull() ?: 0.0
                val total = food + transport + entertainment + bills
                if (salary > 0 && total > salary * 1.5) {
                    Toast.makeText(this, "Total budget seems high compared to salary.", Toast.LENGTH_SHORT).show()
                }
                DomainData.setBudgets(this, food, transport, entertainment, bills)
                refreshBudgetUi()
                GoalNotificationHelper.showBudgetUpdated(this)
                val userId = UserPrefs.getUserId(this)
                if (userId > 0) {
                    val title = getString(R.string.notification_budget_updated_title)
                    val body = getString(R.string.notification_budget_updated_body)
                    ApiClient.service.createNotification(NotificationCreateRequest(user_id = userId, domain = "finance", title = title, body = body))
                        .enqueue(object : Callback<okhttp3.ResponseBody> {
                            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
                        })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.budget_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.budget_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_savings_goals))
                menu.add(0, 2, 1, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@BudgetActivity, SavingsGoalsActivity::class.java))
                        2 -> startActivity(Intent(this@BudgetActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.budget_edit_btn).setOnClickListener {
            showEditBudgetDialog()
        }

        refreshBudgetUi()
    }

    override fun onResume() {
        super.onResume()
        refreshBudgetUi()
    }

    private fun refreshBudgetUi() {
        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val remaining = (salary - spentMonth).coerceAtLeast(0.0)

        // Ensure some default budgets if user hasn't set any but salary exists.
        var foodBudget = DomainData.getBudgetFood(this)
        var transportBudget = DomainData.getBudgetTransport(this)
        var entertainmentBudget = DomainData.getBudgetEntertainment(this)
        var billsBudget = DomainData.getBudgetBills(this)
        if (salary > 0 && foodBudget == 0.0 && transportBudget == 0.0 && entertainmentBudget == 0.0 && billsBudget == 0.0) {
            foodBudget = salary * 0.3
            transportBudget = salary * 0.15
            entertainmentBudget = salary * 0.15
            billsBudget = salary * 0.4
            DomainData.setBudgets(this, foodBudget, transportBudget, entertainmentBudget, billsBudget)
        }

        val totalBudget = foodBudget + transportBudget + entertainmentBudget + billsBudget

        findViewById<TextView>(R.id.budget_monthly_total_text).text =
            if (salary > 0) "₹${String.format("%.0f", salary)}" else "--"
        findViewById<TextView>(R.id.budget_spent_value_text).text =
            "₹${String.format("%.0f", spentMonth)}"
        findViewById<TextView>(R.id.budget_remaining_value_text).text =
            if (salary > 0) "₹${String.format("%.0f", remaining)}" else "--"

        // Classify expenses by category based on note keywords
        val expenses = DomainData.getExpenses(this)
        var foodSpent = 0.0
        var transportSpent = 0.0
        var entertainmentSpent = 0.0
        var billsSpent = 0.0
        for (e in expenses) {
            val noteLower = e.note.lowercase()
            val amount = e.amount
            when {
                listOf("food", "eat", "hotel", "restaurant", "dining", "meal").any { it in noteLower } ->
                    foodSpent += amount
                listOf("bus", "cab", "uber", "auto", "ola", "train", "fuel", "petrol", "diesel", "taxi").any { it in noteLower } ->
                    transportSpent += amount
                listOf("movie", "netflix", "prime", "party", "club", "game", "entertainment").any { it in noteLower } ->
                    entertainmentSpent += amount
                listOf("rent", "bill", "electric", "electricity", "water", "wifi", "internet", "phone", "mobile", "gas").any { it in noteLower } ->
                    billsSpent += amount
                else -> {
                    // If uncategorised, add to bills by default
                    billsSpent += amount
                }
            }
        }

        // Update category labels
        findViewById<TextView>(R.id.budget_food_spent_text).text =
            "₹${String.format("%.0f", foodSpent)}"
        findViewById<TextView>(R.id.budget_food_budget_text).text =
            "₹${String.format("%.0f", foodBudget)}"

        findViewById<TextView>(R.id.budget_transport_spent_text).text =
            "₹${String.format("%.0f", transportSpent)}"
        findViewById<TextView>(R.id.budget_transport_budget_text).text =
            "₹${String.format("%.0f", transportBudget)}"

        findViewById<TextView>(R.id.budget_entertainment_spent_text).text =
            "₹${String.format("%.0f", entertainmentSpent)}"
        findViewById<TextView>(R.id.budget_entertainment_budget_text).text =
            "₹${String.format("%.0f", entertainmentBudget)}"

        findViewById<TextView>(R.id.budget_bills_spent_text).text =
            "₹${String.format("%.0f", billsSpent)}"
        findViewById<TextView>(R.id.budget_bills_budget_text).text =
            "₹${String.format("%.0f", billsBudget)}"

        // Update simple progress bars (weights) based on spent vs budget
        fun updateBar(fillId: Int, trackId: Int, spent: Double, budget: Double) {
            val fillView = findViewById<android.view.View>(fillId)
            val trackView = findViewById<android.view.View>(trackId)
            val ratio = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0) else 0.0
            val fillWeight = (ratio * 100).toFloat()
            val trackWeight = 100f - fillWeight
            (fillView.layoutParams as? LinearLayout.LayoutParams)?.let { lp ->
                lp.weight = fillWeight
                fillView.layoutParams = lp
            }
            (trackView.layoutParams as? LinearLayout.LayoutParams)?.let { lp ->
                lp.weight = trackWeight
                trackView.layoutParams = lp
            }
        }

        updateBar(R.id.budget_food_fill, R.id.budget_food_track, foodSpent, foodBudget)
        updateBar(R.id.budget_transport_fill, R.id.budget_transport_track, transportSpent, transportBudget)
        updateBar(R.id.budget_entertainment_fill, R.id.budget_entertainment_track, entertainmentSpent, entertainmentBudget)
        // Bills uses a full-width frame, so just reflect remaining vs budget by alpha
        val billsFill = findViewById<android.view.View>(R.id.budget_bills_fill)
        val billsRatio = if (billsBudget > 0) (billsSpent / billsBudget).coerceIn(0.0, 1.0) else 0.0
        billsFill.alpha = (0.4f + (0.6f * billsRatio.toFloat()))
    }
}