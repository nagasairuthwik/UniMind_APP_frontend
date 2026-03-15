package com.simats.unimind

object NotificationPhrases {

    // --- Finance ---
    val financeSamples = listOf(
        "You’re close to your savings target!",
        "Spending slightly high today — adjust tomorrow.",
        "You managed money wisely this week.",
        "Dining expenses increased — review if necessary.",
        "Savings streak: 5 days in a row!",
        "Emergency fund growing steadily. Great job!",
        "You saved more than last month!",
        "Weekend spending alert — stay mindful.",
        "Budget discipline improving.",
        "Financial health score improved this week!"
    )

    fun pickFinanceMessage(salary: Double, spentToday: Double, spentMonth: Double): String? {
        if (salary <= 0.0) return null
        val dailyBudget = salary / 30.0
        val balance = (salary - spentMonth).coerceAtLeast(0.0)
        return when {
            spentToday > dailyBudget * 1.5 ->
                "Spending slightly high today — adjust tomorrow."
            balance > salary * 0.3 ->
                "Emergency fund growing steadily. Great job!"
            spentMonth < salary * 0.5 ->
                "You managed money wisely this week."
            else ->
                "Budget discipline improving."
        }
    }

    // --- Health (steps) ---
    val healthSamples = listOf(
        "Only 1,000 steps left to hit your goal!",
        "You’re more active than yesterday!",
        "Morning walk can boost today’s energy.",
        "Hydration reminder: Drink water.",
        "You burned more calories than usual!",
        "Weekend challenge: 8,000 steps today!",
        "Great improvement in activity levels.",
        "Your consistency is improving stamina.",
        "Fitness score rising steadily!"
    )

    // --- Productivity (tasks) ---
    val productivitySamples = listOf(
        "Focus time! Your next task starts soon.",
        "Have you completed all planned tasks today?",
        "Only 2 tasks left — finish strong.",
        "Deep work session suggested now.",
        "You were 15% more productive this week.",
        "Task completion streak: 4 days!",
        "Time block reminder: Stay focused.",
        "Procrastination detected — start small.",
        "Your productivity peak time is afternoon.",
        "Review tomorrow’s plan tonight."
    )

    fun pickProductivityMessage(totalTasks: Int, completedTasks: Int): String? {
        if (totalTasks <= 0) return null
        val remaining = (totalTasks - completedTasks).coerceAtLeast(0)
        return when {
            completedTasks == totalTasks ->
                "Have you completed all planned tasks today?"
            remaining in 1..2 ->
                "Only 2 tasks left — finish strong."
            completedTasks > 0 && completedTasks >= totalTasks / 2 ->
                "Time block reminder: Stay focused."
            else ->
                null
        }
    }

    // --- Lifestyle / Fitness (sleep & stress) ---
    val lifestyleSamples = listOf(
        "Sleep less than 6 hours — prioritize rest.",
        "Stress slightly high — try breathing exercises.",
        "Great sleep quality last night!",
        "Mood improving compared to yesterday.",
        "Relaxation time recommended.",
        "Your sleep consistency is improving.",
        "High stress pattern detected this week."
    )

    fun pickLifestyleMessage(sleepHours: Float, stressLevel: Int): String? {
        return when {
            sleepHours in 0f..5.99f ->
                "Sleep less than 6 hours — prioritize rest."
            stressLevel >= 8 ->
                "High stress pattern detected this week."
            stressLevel in 6..7 ->
                "Stress slightly high — try breathing exercises."
            sleepHours >= 7.5f && stressLevel <= 4 ->
                "Great sleep quality last night!"
            else ->
                null
        }
    }

    // --- Smart Weekly AI Suggestions ---
    val smartWeeklySuggestions = listOf(
        "Your finance is strong, but sleep needs attention.",
        "Health improved 20% this week — amazing!",
        "Balanced week overall. Keep consistency.",
        "Productivity rising, but stress also increased.",
        "Savings improved but spending fluctuates.",
        "You’re building strong discipline habits.",
        "Activity levels dipped mid-week.",
        "Best performance day: Wednesday!",
        "Most productive time: 3 PM – 6 PM.",
        "Overall life score improved this week."
    )

    // --- Overall Premium Feel ---
    val overallPremiumFeel = listOf(
        "Your Life Score is now 82/100. Impressive.",
        "Elite consistency unlocked.",
        "You’re outperforming last week’s version of yourself.",
        "Momentum building. Stay in control.",
        "Life Balance: Stable and improving.",
        "Performance Mode activated.",
        "You’re entering a growth phase.",
        "Next milestone within reach.",
        "Progress curve trending upward.",
        "Keep stacking wins."
    )

    // --- Gamified Premium Notifications ---
    val gamifiedPremium = listOf(
        "🔥 5-Day Discipline Streak!",
        "💎 Financial Control Level Up!",
        "🚀 Productivity Tier Upgraded!",
        "🏅 Health Consistency Badge Earned!",
        "🌙 Sleep Master Achievement!",
        "⚡ Focus Warrior Unlocked!",
        "🎯 Goal Crusher Status!",
        "📈 Growth Mode Active!",
        "🏆 Balanced Life Badge!",
        "🔥 Momentum Streak Continues!"
    )

    // --- AI Assistant Premium Tone ---
    val assistantPremiumTone = listOf(
        "I’ve analyzed your week. You’re progressing steadily.",
        "Let’s optimize tomorrow for better balance.",
        "I recommend a lighter workload today.",
        "Today is ideal for financial planning.",
        "You’re building long-term discipline.",
        "Recovery mode suggested tonight.",
        "Strong financial stability detected.",
        "Health and productivity synergy improving.",
        "You’re becoming more consistent.",
        "Your future self will thank you."
    )
}

