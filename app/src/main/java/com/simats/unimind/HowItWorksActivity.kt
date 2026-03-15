package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class HowItWorksActivity : ComponentActivity() {

    private var healthExpanded = true
    private var productivityExpanded = false
    private var financeExpanded = false
    private var lifestyleExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_it_works)

        findViewById<ImageButton>(R.id.howitworks_back).setOnClickListener {
            finish()
        }

        // Health & Fitness: toggle expand/collapse
        findViewById<LinearLayout>(R.id.howitworks_health_header).setOnClickListener {
            healthExpanded = !healthExpanded
            findViewById<LinearLayout>(R.id.howitworks_health_content).visibility =
                if (healthExpanded) View.VISIBLE else View.GONE
            (findViewById<ImageView>(R.id.howitworks_health_chevron)).setImageResource(
                if (healthExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            )
        }

        findViewById<Button>(R.id.howitworks_try_health).setOnClickListener {
            startActivity(Intent(this, FitnessActivity::class.java))
            finish()
        }

        // Productivity: toggle expand/collapse
        findViewById<LinearLayout>(R.id.howitworks_productivity).setOnClickListener {
            productivityExpanded = !productivityExpanded
            findViewById<LinearLayout>(R.id.howitworks_productivity_content).visibility =
                if (productivityExpanded) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.howitworks_productivity_chevron).setImageResource(
                if (productivityExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            )
        }
        findViewById<Button>(R.id.howitworks_try_productivity).setOnClickListener {
            startActivity(Intent(this, ProductivityActivity::class.java))
            finish()
        }

        // Finance: toggle expand/collapse
        findViewById<LinearLayout>(R.id.howitworks_finance).setOnClickListener {
            financeExpanded = !financeExpanded
            findViewById<LinearLayout>(R.id.howitworks_finance_content).visibility =
                if (financeExpanded) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.howitworks_finance_chevron).setImageResource(
                if (financeExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            )
        }
        findViewById<Button>(R.id.howitworks_try_finance).setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
            finish()
        }

        // Lifestyle: toggle expand/collapse
        findViewById<LinearLayout>(R.id.howitworks_lifestyle).setOnClickListener {
            lifestyleExpanded = !lifestyleExpanded
            findViewById<LinearLayout>(R.id.howitworks_lifestyle_content).visibility =
                if (lifestyleExpanded) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.howitworks_lifestyle_chevron).setImageResource(
                if (lifestyleExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            )
        }
        findViewById<Button>(R.id.howitworks_try_lifestyle).setOnClickListener {
            startActivity(Intent(this, LifestyleOptimizationActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.howitworks_go_dashboard).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
    }
}
