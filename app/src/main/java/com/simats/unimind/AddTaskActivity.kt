package com.simats.unimind

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.Calendar

class AddTaskActivity : ComponentActivity() {

    private var reminderTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        TaskNotificationHelper.ensureChannel(this)

        findViewById<ImageButton>(R.id.add_task_back).setOnClickListener { finish() }

        val titleEt = findViewById<EditText>(R.id.add_task_title)

        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, 30)
        reminderTimeMillis = cal.timeInMillis

        findViewById<Button>(R.id.add_task_pick_time).setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    cal.set(Calendar.SECOND, 0)
                    reminderTimeMillis = cal.timeInMillis
                    findViewById<Button>(R.id.add_task_pick_time).text =
                        "%02d:%02d".format(hour, minute)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
        findViewById<Button>(R.id.add_task_pick_time).text =
            "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        findViewById<Button>(R.id.add_task_save).setOnClickListener {
            val title = titleEt.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter task name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val task = DomainData.addTask(this, title, reminderTimeMillis)
            TaskNotificationHelper.scheduleTaskReminder(this, task.id, task.title, reminderTimeMillis)
            Toast.makeText(this, "Task saved. You'll be notified.", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }
}
