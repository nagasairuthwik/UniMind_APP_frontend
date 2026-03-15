package com.simats.unimind

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddExpenseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)
        NotificationPermissionHelper.ensureNotificationPermission(this)

        findViewById<ImageButton>(R.id.add_expense_back).setOnClickListener { finish() }

        val amountEt = findViewById<EditText>(R.id.add_expense_amount)
        val noteEt = findViewById<EditText>(R.id.add_expense_note)

        findViewById<Button>(R.id.add_expense_save).setOnClickListener {
            val amountStr = amountEt.text.toString().trim()
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val note = noteEt.text.toString().trim()
            DomainData.addExpense(this, DomainData.todayDate(), amount, note)
            saveFinanceToBackend()
            GoalNotificationHelper.showExpenseRecorded(this)
            val userId = UserPrefs.getUserId(this)
            if (userId > 0) {
                val title = getString(R.string.notification_expense_recorded_title)
                val body = getString(R.string.notification_expense_recorded_body)
                ApiClient.service.createNotification(NotificationCreateRequest(user_id = userId, domain = "finance", title = title, body = body))
                    .enqueue(object : Callback<okhttp3.ResponseBody> {
                        override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                        override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
                    })
            }
            Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun saveFinanceToBackend() {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            Toast.makeText(this, "Sign in to sync finance data to cloud", Toast.LENGTH_SHORT).show()
            return
        }
        val request = DomainFinanceRequest(
            user_id = userId,
            entry_date = DomainData.todayDate(),
            user_data = FinanceActivity.buildFinanceUserData(this),
            ai_text = null
        )
        ApiClient.service.saveDomainFinance(request).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                if (!response.isSuccessful) {
                    val msg = response.errorBody()?.string() ?: "Error ${response.code()}"
                    android.util.Log.w("AddExpenseActivity", "saveDomainFinance: $msg")
                    Toast.makeText(this@AddExpenseActivity, "Sync failed: $msg", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                android.util.Log.w("AddExpenseActivity", "saveDomainFinance failed: ${t.message}")
                Toast.makeText(this@AddExpenseActivity, "Sync failed: ${t.message}. Check server & network.", Toast.LENGTH_LONG).show()
            }
        })
    }
}
