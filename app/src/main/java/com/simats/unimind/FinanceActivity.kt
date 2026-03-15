package com.simats.unimind

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinanceActivity : AppCompatActivity() {

    private val addExpenseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) refreshFinanceUi()
    }

    companion object {
        /** Build user_data map for POST /domain/finance from current DomainData. */
        fun buildFinanceUserData(context: Context): Map<String, Any?> {
            val salary = DomainData.getSalary(context)
            val today = DomainData.todayDate()
            val spentToday = DomainData.getTotalSpentOnDate(context, today)
            val spentMonth = DomainData.getTotalSpentThisMonth(context)
            val balance = (salary - spentMonth).coerceAtLeast(0.0)
            val expenses = DomainData.getExpenses(context)
            val expensesToday = expenses.filter { it.date == today }.map { e ->
                mapOf("id" to e.id, "date" to e.date, "amount" to e.amount, "note" to e.note)
            }
            val recentTransactions = expenses.map { e ->
                mapOf("id" to e.id, "date" to e.date, "amount" to e.amount, "note" to e.note)
            }.takeLast(50)
            return mapOf(
                "salary_monthly" to salary,
                "total_spent_today" to spentToday,
                "total_spent_this_month" to spentMonth,
                "balance_this_month" to balance,
                "expenses_today" to expensesToday,
                "recent_transactions" to recentTransactions
            )
        }
    }

    private fun saveFinanceToBackend(aiText: String? = null) {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            Toast.makeText(this, "Sign in (email/password) to sync finance data to cloud", Toast.LENGTH_LONG).show()
            return
        }
        val request = DomainFinanceRequest(
            user_id = userId,
            entry_date = DomainData.todayDate(),
            user_data = buildFinanceUserData(this),
            ai_text = aiText
        )
        ApiClient.service.saveDomainFinance(request).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                if (!response.isSuccessful) {
                    val msg = response.errorBody()?.string() ?: "Error ${response.code()}"
                    android.util.Log.w("FinanceActivity", "saveDomainFinance: $msg")
                    Toast.makeText(this@FinanceActivity, "Sync failed: $msg", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                android.util.Log.w("FinanceActivity", "saveDomainFinance failed: ${t.message}")
                Toast.makeText(this@FinanceActivity, "Sync failed: ${t.message}. Check server & network.", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.finance_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.finance_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_set_salary))
                menu.add(0, 2, 1, getString(R.string.menu_budget))
                menu.add(0, 3, 2, getString(R.string.menu_savings_goals))
                menu.add(0, 4, 3, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> { showSetSalaryDialog(); true }
                        2 -> { startActivity(Intent(this@FinanceActivity, BudgetActivity::class.java)); true }
                        3 -> { startActivity(Intent(this@FinanceActivity, SavingsGoalsActivity::class.java)); true }
                        4 -> { startActivity(Intent(this@FinanceActivity, HelpSupportActivity::class.java)); true }
                        else -> false
                    }
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.finance_income_card).setOnClickListener {
            showSetSalaryDialog()
        }

        findViewById<LinearLayout>(R.id.finance_add_expense).setOnClickListener {
            addExpenseLauncher.launch(Intent(this, AddExpenseActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.finance_budget_plan).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.finance_savings_goals).setOnClickListener {
            startActivity(Intent(this, SavingsGoalsActivity::class.java))
        }

        findViewById<TextView>(R.id.finance_view_all).setOnClickListener {
            Toast.makeText(this, "View All", Toast.LENGTH_SHORT).show()
        }

        refreshFinanceUi()
    }

    override fun onResume() {
        super.onResume()
        refreshFinanceUi()
    }

    private fun refreshFinanceUi() {
        val salary = DomainData.getSalary(this)
        val today = DomainData.todayDate()
        val spentToday = DomainData.getTotalSpentOnDate(this, today)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)

        findViewById<TextView>(R.id.finance_income_value).text =
            if (salary > 0) "₹${String.format("%.0f", salary)}" else getString(R.string.finance_income_value)
        findViewById<TextView>(R.id.finance_expenses_value).text =
            "₹${String.format("%.2f", spentToday)} today"
        findViewById<TextView>(R.id.finance_balance_value).text =
            "₹${String.format("%.2f", balance)}"

        val insightTv = findViewById<TextView>(R.id.finance_insight_message)
        if (salary > 0) {
            GeminiApi.getFinanceSuggestions(salary, spentToday, spentMonth) { suggestion ->
                runOnUiThread {
                    insightTv.text = suggestion ?: getString(R.string.finance_insight_message)
                    saveFinanceToBackend(suggestion)
                }
            }
        } else {
            insightTv.text = "Set your monthly salary (tap income card or menu) to get AI suggestions."
        }

        // Recent transactions: show all expenses entered by user (newest first)
        val container = findViewById<LinearLayout>(R.id.finance_recent_transactions_container)
        container.removeAllViews()
        val expenses = DomainData.getExpenses(this).reversed().take(100)
        if (expenses.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No transactions yet. Tap Add Expense to log one."
                textSize = 14f
                setPadding(0, 24, 0, 24)
            }
            container.addView(empty)
        } else {
            val inflater = LayoutInflater.from(this)
            for (e in expenses) {
                val row = inflater.inflate(R.layout.item_finance_transaction, container, false)
                row.findViewById<TextView>(R.id.item_finance_tx_title).text = e.note.ifBlank { "Expense" }
                row.findViewById<TextView>(R.id.item_finance_tx_detail).text = e.date
                row.findViewById<TextView>(R.id.item_finance_tx_amount).text =
                    "-₹${String.format("%.2f", e.amount)}"
                container.addView(row)
            }
        }
    }

    private fun showSetSalaryDialog() {
        val edit = EditText(this).apply {
            hint = "Monthly salary"
            setPadding(48, 32, 48, 32)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(DomainData.getSalary(this@FinanceActivity).takeIf { it > 0 }?.toInt()?.toString() ?: "")
        }
        AlertDialog.Builder(this)
            .setTitle("Monthly salary")
            .setView(edit)
            .setPositiveButton("Save") { _, _ ->
                val v = edit.text.toString().toDoubleOrNull() ?: 0.0
                if (v > 0) {
                    DomainData.setSalary(this, v)
                    refreshFinanceUi()
                    saveFinanceToBackend(null)
                    GoalNotificationHelper.showSalaryUpdated(this, v)
                    val userId = UserPrefs.getUserId(this)
                    if (userId > 0) {
                        val title = getString(R.string.notification_salary_updated_title)
                        val body = getString(R.string.notification_salary_updated_body)
                        ApiClient.service.createNotification(NotificationCreateRequest(user_id = userId, domain = "finance", title = title, body = body))
                            .enqueue(object : Callback<okhttp3.ResponseBody> {
                                override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
                            })
                    }
                    Toast.makeText(this, "Salary saved", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
