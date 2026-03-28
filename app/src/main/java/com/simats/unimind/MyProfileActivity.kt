package com.simats.unimind

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyProfileActivity : AppCompatActivity() {

    private val domainTitleIds = listOf(
        R.string.domain_health,
        R.string.domain_productivity,
        R.string.domain_finance,
        R.string.domain_lifestyle
    )

    private val personalityTitleIds = listOf(
        R.string.personality_professional,
        R.string.personality_friendly,
        R.string.personality_creative
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        findViewById<ImageButton>(R.id.profile_back).setOnClickListener {
            finish()
        }

        val editIntent = Intent(this, EditProfileActivity::class.java)
        findViewById<LinearLayout>(R.id.profile_edit_btn_top).setOnClickListener {
            startActivity(editIntent)
        }

        findViewById<LinearLayout>(R.id.profile_edit_info_btn).setOnClickListener {
            startActivity(editIntent)
        }

        updateProfileAvatar()

        // Active domains from DomainSelection (OnboardingPrefs)
        val domainIndices = OnboardingPrefs.getDomainIndices(this).sorted()
        val domainNames = domainIndices.mapNotNull { index ->
            domainTitleIds.getOrNull(index)?.let { getString(it) }
        }
        val activeDomainsTextView = findViewById<TextView>(R.id.profile_active_domains_value_text)
        if (domainNames.isNotEmpty()) {
            activeDomainsTextView.text = domainNames.joinToString(", ")
        }

        // AI personality from AiPersonality selection
        val personalityIndex = OnboardingPrefs
            .getPersonalityIndex(this)
            .coerceIn(0, personalityTitleIds.lastIndex)
        val personalityTextView = findViewById<TextView>(R.id.profile_ai_personality_value_text)
        personalityTextView.text = getString(personalityTitleIds[personalityIndex])

        // Active goals from Goals selection
        val goals = OnboardingPrefs.getGoals(this)
        val activeGoalsTextView = findViewById<TextView>(R.id.profile_active_goals_value_text)
        val detailsGoalsTextView = findViewById<TextView>(R.id.profile_details_goals_value_text)

        if (goals.isNotEmpty()) {
            // Show count of goals in the stats card
            activeGoalsTextView.text = goals.size.toString()
            // Show all goals inside Profile Details row
            detailsGoalsTextView.text = goals.joinToString(", ")
        } else {
            // No saved goals: keep default stat value and show a friendly placeholder goal
            detailsGoalsTextView.text = getString(R.string.goal_sample)
        }

        // Load profile details (name, email, age, goals) from backend if we have a user id
        val userId = UserPrefs.getUserId(this)
        if (userId > 0) {
            ApiClient.service.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (!response.isSuccessful) return
                    val profile = response.body()?.profile ?: return

                    // Header name and username (email)
                    findViewById<TextView>(R.id.profile_name).text =
                        profile.full_name ?: getString(R.string.profile_user_name)
                    findViewById<TextView>(R.id.profile_username).text =
                        profile.email ?: getString(R.string.profile_username)

                    // Details card: full name and email
                    findViewById<TextView>(R.id.profile_full_name_value_text).text =
                        profile.full_name ?: getString(R.string.profile_full_name_value)
                    findViewById<TextView>(R.id.profile_email_value_text).text =
                        profile.email ?: getString(R.string.profile_email_value)

                    // Age stat
                    profile.age?.let {
                        findViewById<TextView>(R.id.profile_age_value_text).text = it.toString()
                    }

                    // If goals in backend, prefer them for details (falls back to OnboardingPrefs above)
                    val backendGoals = profile.goals?.takeIf { it.isNotBlank() }
                    if (backendGoals != null) {
                        detailsGoalsTextView.text = backendGoals
                        // Update count if we can split; otherwise keep previous value
                        val parts = backendGoals.split(";", ",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (parts.isNotEmpty()) {
                            activeGoalsTextView.text = parts.size.toString()
                        }
                    }

                    profile.member_since?.takeIf { it.isNotBlank() }?.let { since ->
                        findViewById<TextView>(R.id.profile_member_since_value).text = since
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MyProfileActivity,
                        "Could not load profile: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun updateProfileAvatar() {
        val path = UserPrefs.getProfilePhotoUri(this)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                try {
                    val imageView = findViewById<ImageView>(R.id.profile_avatar_image)
                    imageView.setImageURI(android.net.Uri.fromFile(file))
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                } catch (e: Exception) {
                    e.printStackTrace()
                    UserPrefs.saveProfilePhotoUri(this, null)
                }
            } else {
                UserPrefs.saveProfilePhotoUri(this, null)
            }
        }
    }
}
