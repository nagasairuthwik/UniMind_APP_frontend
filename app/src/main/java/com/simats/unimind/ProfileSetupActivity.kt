package com.simats.unimind

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileSetupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
    }

    private var selectedGender: String = "Male"
    private var selectedPhotoUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                selectedPhotoUri = it
                findViewById<ImageView>(R.id.profile_setup_avatar).setImageURI(it)
            } catch (e: Exception) {
                e.printStackTrace()
                selectedPhotoUri = null
                Toast.makeText(this, "Could not load selected photo. Please try a different image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) pickImage.launch("image/*") else
            Toast.makeText(this, "Gallery access is needed to set profile photo", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        findViewById<ImageButton>(R.id.profile_setup_back).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.profile_setup_menu).setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menu.add(0, 1, 0, getString(R.string.menu_help))
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> startActivity(Intent(this@ProfileSetupActivity, HelpSupportActivity::class.java))
                        else -> { }
                    }
                    true
                }
                show()
            }
        }

        findViewById<ImageButton>(R.id.profile_setup_add_photo).setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES))
            } else {
                pickImage.launch("image/*")
            }
        }

        setupGenderSelection()
        updateGenderButtonStyles()

        val nameEdit = findViewById<EditText>(R.id.profile_setup_name)
        val ageEdit = findViewById<EditText>(R.id.profile_setup_age)

        findViewById<Button>(R.id.profile_setup_continue).setOnClickListener {
            val fullName = nameEdit.text.toString().trim()
            val ageText = ageEdit.text.toString().trim()

            var hasError = false
            if (fullName.isEmpty()) {
                nameEdit.error = getString(R.string.profile_setup_name_hint)
                nameEdit.requestFocus()
                hasError = true
            } else {
                nameEdit.error = null
            }
            if (ageText.isEmpty()) {
                ageEdit.error = getString(R.string.profile_setup_age_hint)
                if (!hasError) ageEdit.requestFocus()
                hasError = true
            } else {
                val age = ageText.toIntOrNull()
                if (age == null || age < 1 || age > 150) {
                    ageEdit.error = "Enter a valid age (1–150)"
                    if (!hasError) ageEdit.requestFocus()
                    hasError = true
                } else {
                    ageEdit.error = null
                }
            }
            if (!hasError) {
                val userId = intent.getIntExtra(EXTRA_USER_ID, -1)
                if (userId <= 0) {
                    Toast.makeText(this, "Please sign in to save your profile", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val continueBtn = findViewById<Button>(R.id.profile_setup_continue)
                continueBtn.isEnabled = false
                Toast.makeText(this, "Saving profile…", Toast.LENGTH_SHORT).show()
                uploadPhotoAndSaveProfile(userId, fullName, ageText.toInt(), continueBtn)
            } else {
                Toast.makeText(this, "Please enter your name and age to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPhotoAndSaveProfile(userId: Int, fullName: String, age: Int, continueBtn: Button) {
        fun doSaveProfile(avatarUrl: String?, localPhotoPath: String?) {
            ApiClient.service.saveProfile(
                ProfileSaveRequest(user_id = userId, full_name = fullName, age = age, gender = selectedGender, avatar_url = avatarUrl)
            ).enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                    continueBtn.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileSetupActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                        UserPrefs.saveProfilePhotoUri(this@ProfileSetupActivity, localPhotoPath)
                        UserPrefs.saveDisplayName(this@ProfileSetupActivity, fullName)
                        val next = Intent(this@ProfileSetupActivity, DomainSelectionActivity::class.java)
                        next.putExtra(EXTRA_USER_ID, userId)
                        startActivity(next)
                        finish()
                    } else {
                        Toast.makeText(this@ProfileSetupActivity, "Failed to save profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                    continueBtn.isEnabled = true
                    Toast.makeText(this@ProfileSetupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        val uri = selectedPhotoUri
        if (uri != null) {
            try {
                val file = uriToFile(uri) ?: run {
                    // If we can't read the file, just save profile without photo
                    doSaveProfile(null, null)
                    return
                }
                val localPath = file.absolutePath
                val body = RequestBody.create(null, file)
                val part = MultipartBody.Part.createFormData("photo", file.name, body)
                ApiClient.service.uploadProfilePhoto(part).enqueue(object : Callback<UploadPhotoResponse> {
                    override fun onResponse(call: Call<UploadPhotoResponse>, response: Response<UploadPhotoResponse>) {
                        val url = response.body()?.takeIf { it.success }?.avatar_url
                        doSaveProfile(url, localPath)
                    }
                    override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                        continueBtn.isEnabled = true
                        Toast.makeText(this@ProfileSetupActivity, "Photo upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                // Any unexpected error while handling the image: fall back to saving profile without photo
                doSaveProfile(null, null)
            }
        } else {
            doSaveProfile(null, null)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val stream = contentResolver.openInputStream(uri) ?: return null
            val ext = contentResolver.getType(uri)?.substringAfter("/") ?: "jpg"
            val file = File(cacheDir, "profile_photo_${System.currentTimeMillis()}.$ext")
            FileOutputStream(file).use { out ->
                stream.use { it.copyTo(out) }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupGenderSelection() {
        val male = findViewById<TextView>(R.id.profile_setup_gender_male)
        val female = findViewById<TextView>(R.id.profile_setup_gender_female)
        val other = findViewById<TextView>(R.id.profile_setup_gender_other)

        male.setOnClickListener {
            selectedGender = "Male"
            updateGenderButtonStyles()
        }
        female.setOnClickListener {
            selectedGender = "Female"
            updateGenderButtonStyles()
        }
        other.setOnClickListener {
            selectedGender = "Other"
            updateGenderButtonStyles()
        }
    }

    private fun updateGenderButtonStyles() {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.gender_button_selected)
        val unselectedBg = ContextCompat.getDrawable(this, R.drawable.gender_button_unselected)
        val selectedTextColor = ContextCompat.getColor(this, R.color.text_on_light_bg)
        val unselectedTextColor = ContextCompat.getColor(this, R.color.on_surface)

        findViewById<TextView>(R.id.profile_setup_gender_male).apply {
            background = if (selectedGender == "Male") selectedBg else unselectedBg
            setTextColor(if (selectedGender == "Male") selectedTextColor else unselectedTextColor)
        }
        findViewById<TextView>(R.id.profile_setup_gender_female).apply {
            background = if (selectedGender == "Female") selectedBg else unselectedBg
            setTextColor(if (selectedGender == "Female") selectedTextColor else unselectedTextColor)
        }
        findViewById<TextView>(R.id.profile_setup_gender_other).apply {
            background = if (selectedGender == "Other") selectedBg else unselectedBg
            setTextColor(if (selectedGender == "Other") selectedTextColor else unselectedTextColor)
        }
    }
}
