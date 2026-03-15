package com.simats.unimind

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

/**
 * Local storage for all 4 domains: Finance, Health, Productivity, Lifestyle.
 * Used by activities and GeminiApi for AI suggestions and progress.
 */
object DomainData {

    private const val PREFS_BASE = "domain_data"
    private val gson = Gson()

    // --- Finance ---
    private const val KEY_SALARY = "salary_monthly"
    private const val KEY_EXPENSES = "daily_expenses" // JSON list of ExpenseEntry
    private const val KEY_SAVINGS_GOALS = "savings_goals" // JSON list of SavingsGoalEntry
    private const val KEY_BUDGET_FOOD = "budget_food"
    private const val KEY_BUDGET_TRANSPORT = "budget_transport"
    private const val KEY_BUDGET_ENTERTAINMENT = "budget_entertainment"
    private const val KEY_BUDGET_BILLS = "budget_bills"
    private const val KEY_STEPS_TODAY = "steps_today"
    private const val KEY_STEPS_GOAL = "steps_goal"
    private const val KEY_STEPS_HISTORY = "steps_history" // JSON: date -> steps
    private const val KEY_STEPS_SENSOR_BASELINE = "steps_sensor_baseline"
    private const val KEY_STEPS_BASELINE_DATE = "steps_baseline_date"
    private const val KEY_STEPS_LAST_DETECTOR_DATE = "steps_last_detector_date"
    private const val KEY_HEALTH_GOAL_NOTIFIED_DATE = "health_goal_notified_date"
    private const val KEY_FINANCE_NOTIFIED_DATE = "finance_notified_date"
    private const val KEY_PRODUCTIVITY_NOTIFIED_DATE = "productivity_notified_date"
    private const val KEY_LIFESTYLE_NOTIFIED_DATE = "lifestyle_notified_date"
    private const val KEY_TASKS = "productivity_tasks" // JSON list of TaskEntry
    private const val KEY_SLEEP_STRESS = "lifestyle_entries" // JSON list of SleepStressEntry

    private fun prefs(context: Context): SharedPreferences {
        val userId = try {
            UserPrefs.getUserId(context)
        } catch (e: Exception) {
            -1
        }
        val name = if (userId > 0) {
            "${PREFS_BASE}_user_$userId"
        } else {
            "${PREFS_BASE}_guest"
        }
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    // ---------- Finance ----------
    data class ExpenseEntry(val id: String, val date: String, val amount: Double, val note: String = "")

    data class SavingsGoalEntry(val name: String, val target: Double)

    fun getSalary(context: Context): Double =
        prefs(context).getString(KEY_SALARY, null)?.toDoubleOrNull() ?: 0.0

    fun setSalary(context: Context, monthlySalary: Double) {
        prefs(context).edit().putString(KEY_SALARY, monthlySalary.toString()).apply()
    }

    fun getExpenses(context: Context): List<ExpenseEntry> {
        val json = prefs(context).getString(KEY_EXPENSES, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<ExpenseEntry>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addExpense(context: Context, date: String, amount: Double, note: String = "") {
        val list = getExpenses(context).toMutableList()
        list.add(ExpenseEntry(id = System.currentTimeMillis().toString(), date = date, amount = amount, note = note))
        prefs(context).edit().putString(KEY_EXPENSES, gson.toJson(list)).apply()
    }

    fun getSavingsGoals(context: Context): List<SavingsGoalEntry> {
        val json = prefs(context).getString(KEY_SAVINGS_GOALS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<SavingsGoalEntry>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addSavingsGoal(context: Context, name: String, target: Double) {
        val list = getSavingsGoals(context).toMutableList()
        list.add(SavingsGoalEntry(name = name, target = target))
        prefs(context).edit().putString(KEY_SAVINGS_GOALS, gson.toJson(list)).apply()
    }

    fun getTotalSpentOnDate(context: Context, date: String): Double =
        getExpenses(context).filter { it.date == date }.sumOf { it.amount }

    fun getTotalSpentThisMonth(context: Context): Double {
        val cal = Calendar.getInstance()
        val yearMonth = "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}"
        return getExpenses(context).filter { it.date.startsWith(yearMonth) }.sumOf { it.amount }
    }

    fun getBudgetFood(context: Context): Double =
        prefs(context).getString(KEY_BUDGET_FOOD, null)?.toDoubleOrNull() ?: 0.0

    fun getBudgetTransport(context: Context): Double =
        prefs(context).getString(KEY_BUDGET_TRANSPORT, null)?.toDoubleOrNull() ?: 0.0

    fun getBudgetEntertainment(context: Context): Double =
        prefs(context).getString(KEY_BUDGET_ENTERTAINMENT, null)?.toDoubleOrNull() ?: 0.0

    fun getBudgetBills(context: Context): Double =
        prefs(context).getString(KEY_BUDGET_BILLS, null)?.toDoubleOrNull() ?: 0.0

    fun setBudgets(
        context: Context,
        food: Double,
        transport: Double,
        entertainment: Double,
        bills: Double
    ) {
        prefs(context).edit()
            .putString(KEY_BUDGET_FOOD, food.toString())
            .putString(KEY_BUDGET_TRANSPORT, transport.toString())
            .putString(KEY_BUDGET_ENTERTAINMENT, entertainment.toString())
            .putString(KEY_BUDGET_BILLS, bills.toString())
            .apply()
    }

    // ---------- Health (steps, derived: calories, distance) ----------
    fun getStepsToday(context: Context): Int = prefs(context).getInt(KEY_STEPS_TODAY, 0)
    fun getStepsGoal(context: Context): Int = prefs(context).getInt(KEY_STEPS_GOAL, 10000)
    fun setStepsToday(context: Context, steps: Int) {
        prefs(context).edit().putInt(KEY_STEPS_TODAY, steps).apply()
        val date = todayDate()
        val history = getStepsHistory(context).toMutableMap()
        history[date] = steps
        prefs(context).edit().putString(KEY_STEPS_HISTORY, gson.toJson(history)).apply()
    }
    fun setStepsGoal(context: Context, goal: Int) {
        prefs(context).edit().putInt(KEY_STEPS_GOAL, goal).apply()
    }

    /** Step counter sensor baseline (total steps at start of day) for accurate counting. */
    fun getStepsSensorBaseline(context: Context): Int = prefs(context).getInt(KEY_STEPS_SENSOR_BASELINE, 0)
    fun getStepsBaselineDate(context: Context): String = prefs(context).getString(KEY_STEPS_BASELINE_DATE, "") ?: ""
    fun setStepsSensorBaseline(context: Context, baseline: Int, date: String) {
        prefs(context).edit()
            .putInt(KEY_STEPS_SENSOR_BASELINE, baseline)
            .putString(KEY_STEPS_BASELINE_DATE, date)
            .apply()
    }

    fun getStepsLastDetectorDate(context: Context): String = prefs(context).getString(KEY_STEPS_LAST_DETECTOR_DATE, "") ?: ""
    fun setStepsLastDetectorDate(context: Context, date: String) {
        prefs(context).edit().putString(KEY_STEPS_LAST_DETECTOR_DATE, date).apply()
    }

    /** Clears all health (steps) data so user can start fresh with accurate step count. */
    fun resetHealthData(context: Context) {
        prefs(context).edit()
            .remove(KEY_STEPS_TODAY)
            .remove(KEY_STEPS_GOAL)
            .remove(KEY_STEPS_HISTORY)
            .remove(KEY_STEPS_SENSOR_BASELINE)
            .remove(KEY_STEPS_BASELINE_DATE)
            .remove(KEY_STEPS_LAST_DETECTOR_DATE)
            .remove(KEY_HEALTH_GOAL_NOTIFIED_DATE)
            .apply()
    }

    fun getStepsHistory(context: Context): Map<String, Int> {
        val json = prefs(context).getString(KEY_STEPS_HISTORY, null) ?: return emptyMap()
        return try {
            gson.fromJson(json, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** Sets steps for a specific date in history (e.g. when flushing previous day). */
    fun setStepsHistoryForDate(context: Context, date: String, steps: Int) {
        val history = getStepsHistory(context).toMutableMap()
        history[date] = steps
        prefs(context).edit().putString(KEY_STEPS_HISTORY, gson.toJson(history)).apply()
    }

    /** Approx calories (kcal) from steps: ~0.04 per step. */
    fun stepsToCalories(steps: Int): Int = (steps * 0.04).toInt()
    /** Approx distance in km: ~0.0008 km per step. */
    fun stepsToDistanceKm(steps: Int): Double = steps * 0.0008

    fun getHealthGoalNotifiedDate(context: Context): String =
        prefs(context).getString(KEY_HEALTH_GOAL_NOTIFIED_DATE, "") ?: ""

    fun setHealthGoalNotifiedToday(context: Context, date: String) {
        prefs(context).edit()
            .putString(KEY_HEALTH_GOAL_NOTIFIED_DATE, date)
            .apply()
    }

    fun getFinanceNotifiedDate(context: Context): String =
        prefs(context).getString(KEY_FINANCE_NOTIFIED_DATE, "") ?: ""

    fun setFinanceNotifiedToday(context: Context, date: String) {
        prefs(context).edit()
            .putString(KEY_FINANCE_NOTIFIED_DATE, date)
            .apply()
    }

    fun getProductivityNotifiedDate(context: Context): String =
        prefs(context).getString(KEY_PRODUCTIVITY_NOTIFIED_DATE, "") ?: ""

    fun setProductivityNotifiedToday(context: Context, date: String) {
        prefs(context).edit()
            .putString(KEY_PRODUCTIVITY_NOTIFIED_DATE, date)
            .apply()
    }

    fun getLifestyleNotifiedDate(context: Context): String =
        prefs(context).getString(KEY_LIFESTYLE_NOTIFIED_DATE, "") ?: ""

    fun setLifestyleNotifiedToday(context: Context, date: String) {
        prefs(context).edit()
            .putString(KEY_LIFESTYLE_NOTIFIED_DATE, date)
            .apply()
    }

    // ---------- Productivity (tasks with time for notifications) ----------
    data class TaskEntry(
        val id: String,
        val title: String,
        val timeMillis: Long, // reminder time
        val completed: Boolean = false
    )

    fun getTasks(context: Context): List<TaskEntry> {
        val json = prefs(context).getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<TaskEntry>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addTask(context: Context, title: String, timeMillis: Long): TaskEntry {
        val t = TaskEntry(id = System.currentTimeMillis().toString(), title = title, timeMillis = timeMillis)
        val list = getTasks(context).toMutableList()
        list.add(t)
        prefs(context).edit().putString(KEY_TASKS, gson.toJson(list)).apply()
        return t
    }

    fun setTaskCompleted(context: Context, taskId: String, completed: Boolean) {
        val list = getTasks(context).map {
            if (it.id == taskId) it.copy(completed = completed) else it
        }
        prefs(context).edit().putString(KEY_TASKS, gson.toJson(list)).apply()
    }

    fun deleteTask(context: Context, taskId: String) {
        val list = getTasks(context).filter { it.id != taskId }
        prefs(context).edit().putString(KEY_TASKS, gson.toJson(list)).apply()
    }

    // ---------- Lifestyle (sleep, stress) ----------
    data class SleepStressEntry(val date: String, val sleepHours: Float, val stressLevel: Int) // stress 1-10

    fun getLifestyleEntries(context: Context): List<SleepStressEntry> {
        val json = prefs(context).getString(KEY_SLEEP_STRESS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<SleepStressEntry>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addLifestyleEntry(context: Context, date: String, sleepHours: Float, stressLevel: Int) {
        val list = getLifestyleEntries(context).toMutableList()
        list.add(SleepStressEntry(date = date, sleepHours = sleepHours, stressLevel = stressLevel.coerceIn(1, 10)))
        prefs(context).edit().putString(KEY_SLEEP_STRESS, gson.toJson(list)).apply()
    }

    fun getTodayLifestyle(context: Context): SleepStressEntry? =
        getLifestyleEntries(context).find { it.date == todayDate() }

    fun todayDate(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
    }

    /** Formats Calendar to YYYY-MM-DD. */
    private fun formatDate(c: Calendar): String =
        "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"

    /**
     * Steps for current week Mon..Sun (7 values). Uses steps history; missing days are 0.
     */
    fun getStepsForCurrentWeekDays(context: Context): List<Int> {
        val cal = Calendar.getInstance()
        cal.setFirstDayOfWeek(Calendar.MONDAY)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val history = getStepsHistory(context)
        return (0 until 7).map { i ->
            val d = cal.clone() as Calendar
            d.add(Calendar.DAY_OF_MONTH, i)
            history[formatDate(d)] ?: 0
        }
    }

    /**
     * Spent amount per week of current month: [week1, week2, week3, week4].
     * Week 1 = days 1-7, week 2 = 8-14, week 3 = 15-21, week 4 = 22-end.
     */
    fun getSpentPerWeekOfMonth(context: Context): List<Double> {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val yearMonth = "${year}-${(month + 1).toString().padStart(2, '0')}"
        val expenses = getExpenses(context).filter { it.date.startsWith(yearMonth) }
        val spentByDay = (1..31).associateWith { d ->
            val date = "$yearMonth-${d.toString().padStart(2, '0')}"
            expenses.filter { it.date == date }.sumOf { it.amount }
        }
        fun weekSum(startDay: Int, endDay: Int): Double =
            (startDay..endDay.coerceAtMost(daysInMonth)).sumOf { spentByDay[it] ?: 0.0 }
        return listOf(
            weekSum(1, 7),
            weekSum(8, 14),
            weekSum(15, 21),
            weekSum(22, daysInMonth)
        )
    }
}
