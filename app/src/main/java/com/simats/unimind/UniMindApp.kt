package com.simats.unimind

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class UniMindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Follow system dark/light mode (phone settings). No in-app theme toggle.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
