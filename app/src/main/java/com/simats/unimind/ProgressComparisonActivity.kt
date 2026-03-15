package com.simats.unimind

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.PopupMenu
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgressComparisonActivity : ComponentActivity() {

    private lateinit var beforeDate: String
    private lateinit var afterDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_comparison)

        findViewById<ImageButton>(R.id.comparison_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.comparison_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_share))
                menu.add(0, 2, 1, getString(R.string.menu_full_insights))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "My UniMind progress comparison"))
                        2 -> startActivity(Intent(this@ProgressComparisonActivity, InsightsActivity::class.java))
                        3 -> startActivity(Intent(this@ProgressComparisonActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        val beforeDateText = findViewById<TextView>(R.id.comparison_before_date_text)
        val afterDateText = findViewById<TextView>(R.id.comparison_after_date_text)

        // Default dates: one week ago vs today
        val today = DomainData.todayDate()
        val oneWeekAgo = getDateNDaysAgo(7)
        beforeDate = oneWeekAgo
        afterDate = today

        beforeDateText.text = formatDisplayDate(beforeDate)
        afterDateText.text = formatDisplayDate(afterDate)

        beforeDateText.setOnClickListener {
            showDatePicker(beforeDate) { selected ->
                beforeDate = selected
                beforeDateText.text = formatDisplayDate(beforeDate)
                updateComparisonUi()
            }
        }

        afterDateText.setOnClickListener {
            showDatePicker(afterDate) { selected ->
                afterDate = selected
                afterDateText.text = formatDisplayDate(afterDate)
                updateComparisonUi()
            }
        }

        // Initial load from user's domain data
        updateComparisonUi()
    }

    private fun updateComparisonUi() {
        val stepsBefore = getStepsOn(beforeDate)
        val stepsAfter = getStepsOn(afterDate)
        findViewById<TextView>(R.id.comparison_steps_before_value).text = stepsBefore.toString()
        findViewById<TextView>(R.id.comparison_steps_after_value).text = stepsAfter.toString()

        val tasksBefore = getTasksCompletedOn(beforeDate)
        val tasksAfter = getTasksCompletedOn(afterDate)
        findViewById<TextView>(R.id.comparison_tasks_before_value).text = tasksBefore.toString()
        findViewById<TextView>(R.id.comparison_tasks_after_value).text = tasksAfter.toString()

        val savingsBefore = getSavingsForMonthOf(beforeDate)
        val savingsAfter = getSavingsForMonthOf(afterDate)
        findViewById<TextView>(R.id.comparison_savings_before_value).text =
            "₹%.0f".format(savingsBefore)
        findViewById<TextView>(R.id.comparison_savings_after_value).text =
            "₹%.0f".format(savingsAfter)

        val sleepBefore = getSleepHoursOn(beforeDate)
        val sleepAfter = getSleepHoursOn(afterDate)
        findViewById<TextView>(R.id.comparison_sleep_before_value).text =
            "%.1f hrs".format(sleepBefore)
        findViewById<TextView>(R.id.comparison_sleep_after_value).text =
            "%.1f hrs".format(sleepAfter)
    }

    private fun showDatePicker(
        currentDate: String,
        onDateSelected: (String) -> Unit
    ) {
        val cal = Calendar.getInstance()
        parseDomainDate(currentDate)?.let { cal.time = it }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, y, m, d ->
                val selected = formatDomainDate(y, m, d)
                onDateSelected(selected)
            },
            year,
            month,
            day
        ).show()
    }

    private fun getStepsOn(date: String): Int {
        val history = DomainData.getStepsHistory(this)
        return history[date] ?: 0
    }

    private fun getTasksCompletedOn(date: String): Int {
        val tasks = DomainData.getTasks(this)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return tasks.count { task ->
            task.completed && sdf.format(Date(task.timeMillis)) == date
        }
    }

    private fun getSavingsForMonthOf(date: String): Double {
        val yearMonth = date.take(7) // "yyyy-MM"
        val expenses = DomainData.getExpenses(this)
            .filter { it.date.startsWith(yearMonth) }
            .sumOf { it.amount }
        val salary = DomainData.getSalary(this)
        return (salary - expenses).coerceAtLeast(0.0)
    }

    private fun getSleepHoursOn(date: String): Float {
        val entries = DomainData.getLifestyleEntries(this)
        return entries.find { it.date == date }?.sleepHours ?: 0f
    }

    private fun getDateNDaysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return formatDomainDate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun formatDomainDate(year: Int, monthZeroBased: Int, day: Int): String {
        val m = (monthZeroBased + 1).toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        return "$year-$m-$d"
    }

    private fun parseDomainDate(date: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDisplayDate(date: String): String {
        val parsed = parseDomainDate(date) ?: return date
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(parsed)
    }
}
