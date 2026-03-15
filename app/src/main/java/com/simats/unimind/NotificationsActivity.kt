package com.simats.unimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : ComponentActivity() {

    private lateinit var listContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageButton>(R.id.notifications_back).setOnClickListener {
            finish()
        }

        listContainer = findViewById(R.id.notifications_list_container)
        emptyText = findViewById(R.id.notifications_empty_text)

        findViewById<TextView>(R.id.notifications_mark_all_read).setOnClickListener {
            markAllRead()
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            emptyText.text = "Sign in to see your notifications."
            emptyText.visibility = View.VISIBLE
            listContainer.visibility = View.VISIBLE
            return
        }

        emptyText.text = "Loading..."
        emptyText.visibility = View.VISIBLE
        listContainer.visibility = View.VISIBLE

        ApiClient.service.getNotifications(userId).enqueue(object : Callback<NotificationsListResponse> {
            override fun onResponse(
                call: Call<NotificationsListResponse>,
                response: Response<NotificationsListResponse>
            ) {
                if (!response.isSuccessful || response.body() == null || response.body()!!.success.not()) {
                    emptyText.text = "Could not load notifications. Try again."
                emptyText.visibility = View.VISIBLE
                    return
                }
                val items = response.body()!!.notifications
                if (items.isEmpty()) {
                    emptyText.text = "No notifications yet."
                emptyText.visibility = View.VISIBLE
                    return
                }
            emptyText.visibility = View.GONE
            listContainer.removeAllViews()
                val inflater = LayoutInflater.from(this@NotificationsActivity)
                for (n in items) {
                    val row = inflater.inflate(R.layout.item_notification, listContainer, false)
                    val titleTv = row.findViewById<TextView>(R.id.notification_title)
                    val bodyTv = row.findViewById<TextView>(R.id.notification_body)
                    val timeTv = row.findViewById<TextView>(R.id.notification_time)
                    val unreadDot = row.findViewById<View>(R.id.notification_unread_dot)
                    val dismissBtn = row.findViewById<ImageButton>(R.id.notification_dismiss)

                    titleTv.text = n.title
                    bodyTv.text = n.body
                    timeTv.text = n.created_at
                    unreadDot.visibility = if (n.is_read) View.GONE else View.VISIBLE

                    row.setOnClickListener {
                        if (!n.is_read) {
                            markSingleRead(n.id)
                            unreadDot.visibility = View.GONE
                        }
                    }
                    dismissBtn.setOnClickListener {
                        markSingleRead(n.id)
                        listContainer.removeView(row)
                    }

                    listContainer.addView(row)
                }
            }

            override fun onFailure(call: Call<NotificationsListResponse>, t: Throwable) {
                emptyText.text = "Could not load notifications. Check your network."
                emptyText.visibility = View.VISIBLE
            }
        })
    }

    private fun markAllRead() {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) {
            Toast.makeText(this, "Sign in to sync notifications.", Toast.LENGTH_SHORT).show()
            return
        }
        ApiClient.service.markNotificationsRead(
            NotificationMarkReadRequest(user_id = userId, all = true)
        ).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(
                call: Call<okhttp3.ResponseBody>,
                response: Response<okhttp3.ResponseBody>
            ) {
                loadNotifications()
            }

            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                Toast.makeText(this@NotificationsActivity, "Could not mark as read.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markSingleRead(notificationId: Int) {
        val userId = UserPrefs.getUserId(this)
        if (userId <= 0) return
        ApiClient.service.markNotificationsRead(
            NotificationMarkReadRequest(user_id = userId, notification_id = notificationId)
        ).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(
                call: Call<okhttp3.ResponseBody>,
                response: Response<okhttp3.ResponseBody>
            ) {
                // no-op
            }

            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                // no-op
            }
        })
    }
}
