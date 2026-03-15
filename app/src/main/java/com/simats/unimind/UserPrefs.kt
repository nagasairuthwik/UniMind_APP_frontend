package com.simats.unimind

import android.content.Context
import android.content.SharedPreferences

object UserPrefs {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"
    private const val KEY_DISPLAY_NAME = "display_name"

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

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

