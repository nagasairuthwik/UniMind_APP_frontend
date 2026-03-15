package com.simats.unimind

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
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

class ProductivityActivity : AppCompatActivity() {

    private val addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) refreshProductivityUi()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var nextTask: DomainData.TaskEntry? = null

    companion object {
        /** Build user_data map for POST /domain/productivity from current DomainData. */
        fun buildProductivityUserData(context: Context): Map<String, Any?> {
            val tasks = DomainData.getTasks(context)
            val completedToday = tasks.count { it.completed }
            val upcomingTitles = tasks.filter { !it.completed }
                .sortedBy { it.timeMillis }
                .map { it.title }
            val tasksPayload = tasks.map { t ->
                mapOf(
                    "id" to t.id,
                    "title" to t.title,
                    "time_millis" to t.timeMillis,
                    "completed" to t.completed
                )
            }
            return mapOf(
                "tasks" to tasksPayload,
                "completed_today" to completedToday,
                "total_tasks_today" to tasks.size,
                "upcoming_titles" to upcomingTitles
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productivity)
        TaskNotificationHelper.ensureChannel(this)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.productivity_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.productivity_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_add_task))
                menu.add(0, 2, 1, getString(R.string.menu_settings))
                menu.add(0, 3, 2, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> addTaskLauncher.launch(Intent(this@ProductivityActivity, AddTaskActivity::class.java))
                        2 -> startActivity(Intent(this@ProductivityActivity, SettingsActivity::class.java))
                        3 -> startActivity(Intent(this@ProductivityActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<LinearLayout>(R.id.productivity_plan_tasks).setOnClickListener {
            addTaskLauncher.launch(Intent(this, AddTaskActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.productivity_focus_mode).setOnClickListener {
            showFocusModePopup()
        }

        refreshProductivityUi()
    }

    override fun onResume() {
        super.onResume()
        stopTimerService()
        refreshProductivityUi()
        startTimerUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopTimerUpdates()
        val task = nextTask
        if (task != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, ProductivityTimerService::class.java).apply {
                putExtra(ProductivityTimerService.EXTRA_TASK_ID, task.id)
                putExtra(ProductivityTimerService.EXTRA_TITLE, task.title)
                putExtra(ProductivityTimerService.EXTRA_TIME_MILLIS, task.timeMillis)
            }
            startForegroundService(intent)
        } else {
            TaskNotificationHelper.cancelTimerNotification(this)
        }
    }

    private fun refreshProductivityUi() {
        val tasks = DomainData.getTasks(this)
        val completedToday = tasks.count { it.completed }
        val upcoming = tasks.filter { !it.completed }.sortedBy { it.timeMillis }.map { it.title }

        findViewById<TextView>(R.id.productivity_tasks_count).text = completedToday.toString()
        findViewById<TextView>(R.id.productivity_tasks_subtitle).text = "completed today"

        val taskList = findViewById<LinearLayout>(R.id.productivity_task_list)
        taskList.removeAllViews()
        for (task in tasks.sortedBy { it.timeMillis }) {
            val row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, taskList, false)
            val t1 = row.findViewById<TextView>(android.R.id.text1)
            val t2 = row.findViewById<TextView>(android.R.id.text2)
            t1.text = task.title
            t2.text = if (task.completed) "Done" else "Tap to mark done"
            row.setPadding(32, 24, 32, 24)
            row.setOnClickListener {
                if (!task.completed) {
                    val tasks = DomainData.getTasks(this)
                    val wasLastIncomplete = tasks.count { !it.completed } == 1
                    DomainData.setTaskCompleted(this, task.id, true)
                    refreshProductivityUi()
                    if (wasLastIncomplete) {
                        GoalNotificationHelper.showAllTasksCompleted(this)
                        val userId = UserPrefs.getUserId(this)
                        if (userId > 0) {
                            val title = getString(R.string.notification_all_tasks_done_title)
                            val body = getString(R.string.notification_all_tasks_done_body)
                            ApiClient.service.createNotification(NotificationCreateRequest(user_id = userId, domain = "productivity", title = title, body = body))
                                .enqueue(object : Callback<okhttp3.ResponseBody> {
                                    override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                                    override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
                                })
                        }
                    }
                }
            }
            taskList.addView(row)
        }

        val aiTip = findViewById<TextView>(R.id.productivity_ai_tip)
        aiTip.text = "Loading…"
        GeminiApi.getProductivitySuggestions(tasks.size, completedToday, upcoming) { tip ->
            runOnUiThread {
                val text = tip ?: "Add tasks and set reminder times to stay on track."
                aiTip.text = text
                // Also sync the latest productivity snapshot to backend (if user is signed in)
                saveProductivityToBackend(text)
            }
        }

        updateNextReminderCard(tasks)
    }

    private fun updateNextReminderCard(tasks: List<DomainData.TaskEntry>) {
        val now = System.currentTimeMillis()
        nextTask = tasks.filter { !it.completed && it.timeMillis > now }.minByOrNull { it.timeMillis }
        if (nextTask == null) {
            TaskNotificationHelper.cancelTimerNotification(this)
            stopTimerService()
        }
    }

    private fun stopTimerService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopService(Intent(this, ProductivityTimerService::class.java))
        }
    }

    private fun formatCountdownText(reminderTimeMillis: Long): String {
        val remaining = reminderTimeMillis - System.currentTimeMillis()
        return when {
            remaining <= 0 -> "Due now!"
            remaining >= 24 * 60 * 60 * 1000L -> {
                val days = (remaining / (24 * 60 * 60 * 1000)).toInt()
                val h = ((remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)).toInt()
                val m = ((remaining % (60 * 60 * 1000)) / (60 * 1000)).toInt()
                String.format("in %dd %02d:%02d", days, h, m)
            }
            else -> {
                val totalSec = (remaining / 1000).toInt()
                val h = totalSec / 3600
                val m = (totalSec % 3600) / 60
                val s = totalSec % 60
                String.format("in %02d:%02d:%02d", h, m, s)
            }
        }
    }

    private fun startTimerUpdates() {
        stopTimerUpdates()
        timerRunnable = object : Runnable {
            override fun run() {
                val task = nextTask
                if (task == null) {
                    TaskNotificationHelper.cancelTimerNotification(this@ProductivityActivity)
                    handler.postDelayed(this, 1000)
                    return
                }
                val remaining = task.timeMillis - System.currentTimeMillis()
                if (remaining <= 0) {
                    TaskNotificationHelper.cancelTimerNotification(this@ProductivityActivity)
                    TaskNotificationHelper.showNotification(this@ProductivityActivity, task.id, task.title)
                    AlertDialog.Builder(this@ProductivityActivity)
                        .setTitle("Reminder")
                        .setMessage("Time's up: ${task.title}")
                        .setPositiveButton("OK", null)
                        .show()
                    refreshProductivityUi()
                } else {
                    val countdownText = formatCountdownText(task.timeMillis)
                    TaskNotificationHelper.showTimerNotification(this@ProductivityActivity, task.title, countdownText)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimerUpdates() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }

    private fun saveProductivityToBackend(aiText: String? = null) {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            // User not signed in; skip cloud sync but keep local reminders working.
            return
        }
        val request = DomainProductivityRequest(
            user_id = userId,
            entry_date = DomainData.todayDate(),
            user_data = buildProductivityUserData(this),
            ai_text = aiText
        )
        ApiClient.service.saveDomainProductivity(request)
            .enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: Call<okhttp3.ResponseBody>,
                    response: Response<okhttp3.ResponseBody>
                ) {
                    if (!response.isSuccessful) {
                        val msg = response.errorBody()?.string() ?: "Error ${response.code()}"
                        android.util.Log.w("ProductivityActivity", "saveDomainProductivity: $msg")
                    }
                }

                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                    android.util.Log.w("ProductivityActivity", "saveDomainProductivity failed: ${t.message}")
                }
            })
    }

    private fun showFocusModePopup() {
        val message = """
            Block distractions and work on one task at a time.
            
            • Silence notifications during your focus block
            • Set a time block (e.g. 25 min) and stick to it
            • Work on your top priority task only
            • Take a short break between sessions
        """.trimIndent()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.productivity_focus_mode))
            .setMessage(message)
            .setPositiveButton("Done", null)
            .show()
    }
}
