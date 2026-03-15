package com.simats.unimind

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists onboarding choices so the "All Set" summary screen can display them.
 */
object OnboardingPrefs {

    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_DOMAIN_INDICES = "domain_indices"
    private const val KEY_GOALS = "goals_set"
    private const val KEY_PERSONALITY_INDEX = "personality_index"
    private const val DELIMITER = "||"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveDomainIndices(context: Context, indices: Set<Int>) {
        prefs(context).edit()
            .putString(KEY_DOMAIN_INDICES, indices.sorted().joinToString(","))
            .apply()
    }

    fun getDomainIndices(context: Context): Set<Int> {
        val s = prefs(context).getString(KEY_DOMAIN_INDICES, "") ?: return emptySet()
        if (s.isEmpty()) return emptySet()
        return s.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun saveGoals(context: Context, goals: Set<String>) {
        prefs(context).edit()
            .putString(KEY_GOALS, goals.joinToString(DELIMITER))
            .apply()
    }

    fun getGoals(context: Context): Set<String> {
        val s = prefs(context).getString(KEY_GOALS, "") ?: return emptySet()
        if (s.isEmpty()) return emptySet()
        return s.split(DELIMITER).filter { it.isNotBlank() }.toSet()
    }

    fun savePersonalityIndex(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_PERSONALITY_INDEX, index).apply()
    }

    fun getPersonalityIndex(context: Context): Int =
        prefs(context).getInt(KEY_PERSONALITY_INDEX, 0)
}
