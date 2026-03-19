package com.simats.unimind

import android.content.Context
import android.content.SharedPreferences

object UserPrefs {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_AI_PROACTIVE = "ai_proactive"
    private const val KEY_AI_LEARNING = "ai_learning"
    private const val KEY_AI_EXPLANATIONS = "ai_explanations"
    private const val KEY_AI_SCHEDULING = "ai_scheduling"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserId(context: Context, id: Int) {
        prefs(context).edit().putInt(KEY_USER_ID, id).apply()
    }

    fun getUserId(context: Context): Int =
        prefs(context).getInt(KEY_USER_ID, -1)

    fun saveProfilePhotoUri(context: Context, uri: String?) {
        val editor = prefs(context).edit()
        if (uri.isNullOrEmpty()) {
            editor.remove(KEY_PROFILE_PHOTO_URI)
        } else {
            editor.putString(KEY_PROFILE_PHOTO_URI, uri)
        }
        editor.apply()
    }

    fun getProfilePhotoUri(context: Context): String? =
        prefs(context).getString(KEY_PROFILE_PHOTO_URI, null)

    fun saveDisplayName(context: Context, name: String?) {
        val editor = prefs(context).edit()
        if (name.isNullOrBlank()) {
            editor.remove(KEY_DISPLAY_NAME)
        } else {
            editor.putString(KEY_DISPLAY_NAME, name.trim())
        }
        editor.apply()
    }

    fun getDisplayName(context: Context): String? =
        prefs(context).getString(KEY_DISPLAY_NAME, null)?.takeIf { it.isNotBlank() }

    fun setAiProactive(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AI_PROACTIVE, enabled).apply()
    }

    fun isAiProactive(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AI_PROACTIVE, false)

    fun setAiLearning(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AI_LEARNING, enabled).apply()
    }

    fun isAiLearning(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AI_LEARNING, false)

    fun setAiExplanations(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AI_EXPLANATIONS, enabled).apply()
    }

    fun isAiExplanations(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AI_EXPLANATIONS, false)

    fun setAiScheduling(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AI_SCHEDULING, enabled).apply()
    }

    fun isAiScheduling(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AI_SCHEDULING, false)

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun getAiStyleInstructions(context: Context): String {
        val parts = mutableListOf<String>()
        if (isAiProactive(context)) {
            parts += "Be gently proactive and suggest useful ideas even if the user does not ask directly."
        }
        if (isAiLearning(context)) {
            parts += "Try to remember the user's preferences across answers and keep a consistent coaching style."
        }
        if (isAiExplanations(context)) {
            parts += "Give slightly more detailed explanations when it will help the user understand why a suggestion matters."
        }
        if (isAiScheduling(context)) {
            parts += "When relevant, propose simple time-based suggestions such as morning, afternoon, or evening routines rather than exact clock times."
        }
        if (parts.isEmpty()) {
            return ""
        }
        return "AI personalization preferences: " + parts.joinToString(" ")
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

