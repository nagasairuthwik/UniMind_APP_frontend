package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu

class FinanceAnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_analytics)

        findViewById<ImageButton>(R.id.finance_analytics_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.finance_analytics_close).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.finance_analytics_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_share))
                menu.add(0, 2, 1, getString(R.string.menu_budget))
                menu.add(0, 3, 2, getString(R.string.menu_full_insights))
                menu.add(0, 4, 3, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "My finance analytics from UniMind"))
                        2 -> startActivity(Intent(this@FinanceAnalyticsActivity, BudgetActivity::class.java))
                        3 -> startActivity(Intent(this@FinanceAnalyticsActivity, InsightsActivity::class.java))
                        4 -> startActivity(Intent(this@FinanceAnalyticsActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<Button>(R.id.finance_analytics_take_action).setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        bindDomainData()
    }

    private fun bindDomainData() {
        val salary = DomainData.getSalary(this)
        val spentMonth = DomainData.getTotalSpentThisMonth(this)
        val balance = (salary - spentMonth).coerceAtLeast(0.0)

        val financePercent = if (salary > 0) {
            ((balance / salary) * 100).toInt().coerceIn(0, 100)
        } else 0

        findViewById<TextView>(R.id.finance_header_score_value).text = "$financePercent%"
        findViewById<TextView>(R.id.finance_budget_value).text = "₹%.0f".format(salary)
        findViewById<TextView>(R.id.finance_savings_value).text = "₹%.0f".format(balance)
        findViewById<TextView>(R.id.finance_spending_value).text = "₹%.0f".format(spentMonth)
    }
}
