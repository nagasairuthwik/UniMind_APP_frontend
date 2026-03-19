package com.simats.unimind

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Gemini AI client for UniMind – runs on the device (no backend AI).
 * Uses Gemini REST API for step insights, domain recommendations, and progress tips.
 *
 * You can use up to 6 API keys for fallback when one hits quota/rate limit:
 * - In gradle.properties set: GEMINI_API_KEYS=key1,key2,... (comma-separated, up to 6)
 * - Or set a single key: GEMINI_API_KEY=your_key
 * Get keys at https://aistudio.google.com/apikey
 */
object GeminiApi {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val MODEL = "gemini-2.5-flash"

    /** Built-in 6 keys so the app always has fallbacks even if gradle.properties is not loaded. */
    private val DEFAULT_API_KEYS = listOf(
        "AIzaSyDlATX6DeDnz-TwECP53ONssOSOTWDROis",
        "AIzaSyDHXMTYn7TCqMoygrQMerZpwePnjenr6eM",
        "AIzaSyAv7LpCGBee4IRAwr_k5OUkdJ1jQSCOdgg",
        "AIzaSyCUZAW5Ln2RKjpuEo_tgD7Dv54HI6wxJ6I",
        "AIzaSyCG7VM6Nk7h6Zc304NMplFubVIucv4GOv0",
        "AIzaSyDjJ7DP-Gx_rKqTkRZ8du4DF5LFXFNySmY"
    )

    /** List of API keys to try in order; on 429/quota we try the next key (up to 6).
     * Uses BuildConfig if set, else DEFAULT_API_KEYS so all 6 are always available. */
    private val apiKeys: List<String>
        get() = try {
            val keysStr = BuildConfig.GEMINI_API_KEYS.takeIf { it.isNotBlank() }
            val fromConfig = if (!keysStr.isNullOrBlank()) {
                keysStr.split(",").map { part ->
                    val trimmed = part.trim()
                    if (trimmed.contains(":")) trimmed.substringAfter(":").trim() else trimmed
                }.filter { it.isNotBlank() }.take(6)
            } else {
                emptyList()
            }
            fromConfig.ifEmpty {
                listOfNotNull(BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() })
            }.ifEmpty {
                DEFAULT_API_KEYS
            }
        } catch (e: Exception) {
            DEFAULT_API_KEYS
        }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val domainNames = listOf("Health", "Productivity", "Finance", "Lifestyle")

    // --- Request/Response DTOs for Gemini REST API ---
    private data class GenerateRequest(
        val contents: List<Content>
    ) {
        data class Content(val parts: List<Part>)
        data class Part(val text: String)
    }

    private data class GenerateResponse(
        val candidates: List<Candidate>?
    ) {
        data class Candidate(val content: Content?)
        data class Content(val parts: List<Part>?)
        data class Part(val text: String?)
    }

    private data class GeminiError(
        val error: ErrorDetail?
    ) {
        data class ErrorDetail(val message: String?, val code: Int?)
    }

    private fun quotaLimitMessage(): String =
        "Your AI limit has been completed for now. You can still use UniMind without AI tips. Please try the AI assistant again tomorrow."

    private fun buildPromptRequest(prompt: String): String {
        val req = GenerateRequest(
            contents = listOf(
                GenerateRequest.Content(
                    parts = listOf(GenerateRequest.Part(text = prompt))
                )
            )
        )
        return gson.toJson(req)
    }

    private fun callGemini(prompt: String, keyIndex: Int = 0, onResult: (String?) -> Unit) {
        val keys = apiKeys
        if (keys.isEmpty() || keyIndex >= keys.size) {
            onResult("Set GEMINI_API_KEY or GEMINI_API_KEYS in gradle.properties.")
            return
        }
        val key = keys[keyIndex]
        val url = "$BASE_URL/$MODEL:generateContent?key=$key"
        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(mediaType, buildPromptRequest(prompt))
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("Connection failed: ${e.message}. Check internet and try again.")
            }
            override fun onResponse(call: Call, response: Response) {
                val text = response.body()?.string() ?: ""
                if (!response.isSuccessful) {
                    try {
                        val err = gson.fromJson(text, GeminiError::class.java)
                        val msg = err.error?.message ?: "API error ${response.code()}"
                        val lower = msg.lowercase()
                        val isQuotaOrRateLimit = response.code() == 429 ||
                            "quota" in lower ||
                            "rate limit" in lower ||
                            "exceeded" in lower
                        if (isQuotaOrRateLimit && keyIndex + 1 < keys.size) {
                            // Try next API key
                            callGemini(prompt, keyIndex + 1, onResult)
                            return
                        }
                        if (isQuotaOrRateLimit) {
                            onResult(quotaLimitMessage())
                            return
                        }
                        onResult("Error: $msg")
                    } catch (e: Exception) {
                        if (response.code() == 429 && keyIndex + 1 < keys.size) {
                            callGemini(prompt, keyIndex + 1, onResult)
                        } else if (response.code() == 429) {
                            onResult(quotaLimitMessage())
                        } else {
                            onResult("API error: ${response.code()}. Check your API key at aistudio.google.com/apikey")
                        }
                    }
                    return
                }
                try {
                    val parsed = gson.fromJson(text, GenerateResponse::class.java)
                    val firstText = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    onResult(firstText ?: "I'm not sure how to respond. Try rephrasing.")
                } catch (e: Exception) {
                    onResult("Could not read response. Try again.")
                }
            }
        })
    }

    /**
     * Chatbot: send user message and get AI reply. Use for AI Chat screen.
     */
    fun sendChatMessage(
        userMessage: String,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are the in-app UniMind assistant. You must ONLY answer questions related to:
- The UniMind app itself (features, navigation, how to use screens).
- The four UniMind domains: Health, Productivity, Finance, Lifestyle (goals, habits, routines, motivation, and how the app can help).

If the user asks anything that is clearly outside these topics (for example: general world knowledge, politics, celebrities, programming, random trivia, or anything not connected to UniMind or these four domains), you MUST reply with EXACTLY this sentence and nothing else:
\"This question is not related to this app. Please ask questions related to UniMind and its Health, Productivity, Finance, or Lifestyle features.\"

${if (style.isNotEmpty()) "The user has chosen these AI personalization preferences in the app. Follow them as much as possible:\n$style\n" else ""}

Keep valid answers helpful, concise (2-4 sentences unless they ask for more), and encouraging. Reply in plain text, no markdown or bullets unless the user asks for a list.

User: $userMessage
Assistant:"""
        callGemini(prompt) { result ->
            onResult(result ?: "I couldn't get a response right now. Please check your connection and try again.")
        }
    }

    /**
     * Get a short AI tip for step count. Call on background; pass result to UI.
     */
    fun getStepsInsight(
        stepsToday: Int,
        stepsGoal: Int,
        last7Days: List<Int>? = null,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val week = if (!last7Days.isNullOrEmpty()) " Last 7 days steps: $last7Days." else ""
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a friendly health coach for the UniMind app. Give one short, encouraging tip (1-2 sentences) for the user's step count.
Today: $stepsToday steps. Daily goal: $stepsGoal.$week
If they are behind goal, suggest one simple action (e.g. short walk, stairs). If they met or exceeded goal, congratulate briefly. No bullet points, no hashtags. Plain text only.
${if (style.isNotEmpty()) "AI personalization preferences: $style" else ""}"""
        callGemini(prompt) { result ->
            onResult(result ?: "Keep moving! Every step counts.")
        }
    }

    /**
     * Get AI recommendations for each selected domain. domainIndices: 0=Health, 1=Productivity, 2=Finance, 3=Lifestyle.
     */
    fun getDomainRecommendations(
        domainIndices: List<Int>,
        goalsText: String = "",
        fullName: String = "",
        extraStyleInstructions: String = "",
        onResult: (Map<String, String>) -> Unit
    ) {
        val names = domainIndices.mapNotNull { i -> domainNames.getOrNull(i) }
        if (names.isEmpty()) {
            onResult(emptyMap())
            return
        }
        val goals = goalsText.ifBlank { "Not specified" }
        val name = fullName.ifBlank { "User" }
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a friendly coach for the UniMind app. The user "$name" has selected these life domains: ${names.joinToString(", ")}. Their goals: $goals.
For each domain in the list below, give exactly one short, actionable recommendation (1-2 sentences). Be specific and practical.
${if (style.isNotEmpty()) "Follow these personalization preferences when writing tips: $style" else ""}
Respond in valid JSON only, no other text. Format: { "Health": "tip...", "Productivity": "tip...", "Finance": "tip...", "Lifestyle": "tip..." }
Only include keys for: ${names.joinToString(", ")}."""
        callGemini(prompt) { raw ->
            if (raw.isNullOrBlank()) {
                onResult(names.associateWith { "Focus on small steps in $it this week." })
                return@callGemini
            }
            try {
                val cleaned = raw.replace(Regex("```\\w*"), "").trim()
                val start = cleaned.indexOf('{')
                val end = cleaned.lastIndexOf('}') + 1
                if (start >= 0 && end > start) {
                    val json = cleaned.substring(start, end)
                    @Suppress("UNCHECKED_CAST")
                    val map = gson.fromJson(json, Map::class.java) as Map<String, String>
                    onResult(map.filterKeys { it in names })
                    return@callGemini
                }
            } catch (e: Exception) { }
            onResult(names.associateWith { "Focus on small steps in $it this week." })
        }
    }

    /**
     * Get a short progress insight for one domain.
     */
    fun getProgressInsight(
        domainIndex: Int,
        summary: String,
        recentMetrics: Map<String, Any>? = null,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val domainName = domainNames.getOrElse(domainIndex) { "General" }
        val metrics = if (!recentMetrics.isNullOrEmpty()) " Recent metrics: $recentMetrics." else ""
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a friendly coach for the UniMind app. Give one short, encouraging progress insight (1-2 sentences) for the $domainName domain.
Progress summary: $summary.$metrics
Acknowledge progress and suggest one next step if relevant. Plain text only, no bullets.
${if (style.isNotEmpty()) "Personalize your tone using: $style" else ""}"""
        callGemini(prompt) { result ->
            onResult(result ?: "You're making progress. Keep it up!")
        }
    }

    /**
     * Finance: AI suggestions based on salary and daily/monthly spending.
     */
    fun getFinanceSuggestions(
        monthlySalary: Double,
        totalSpentToday: Double,
        totalSpentThisMonth: Double,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a friendly financial coach for the UniMind app. The user's monthly salary is $monthlySalary. Today they spent $totalSpentToday. So far this month they have spent $totalSpentThisMonth.
Give 2-3 short, actionable suggestions (1 sentence each): budgeting tip, saving tip, or spending awareness. Be specific. Plain text, no bullets or numbers.
${if (style.isNotEmpty()) "Adjust your advice based on: $style" else ""}"""
        callGemini(prompt) { result ->
            onResult(result ?: "Track your daily expenses to stay within your budget.")
        }
    }

    /**
     * Productivity: AI suggestions based on tasks summary.
     */
    fun getProductivitySuggestions(
        totalTasks: Int,
        completedToday: Int,
        upcomingTaskTitles: List<String>,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val upcoming = if (upcomingTaskTitles.isEmpty()) "No upcoming tasks." else "Upcoming: ${upcomingTaskTitles.take(5).joinToString(", ")}."
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a productivity coach for the UniMind app. User has $totalTasks tasks total, completed $completedToday today. $upcoming
Give one short, practical tip to stay focused or prioritize better. Plain text only.
${if (style.isNotEmpty()) "Follow these personalization preferences when coaching: $style" else ""}"""
        callGemini(prompt) { result ->
            onResult(result ?: "Tackle the most important task first.")
        }
    }

    /**
     * Lifestyle: AI tips based on sleep and stress (tracked by user).
     */
    fun getLifestyleSuggestions(
        sleepHours: Float,
        stressLevel: Int,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a wellness coach for the UniMind app. User slept $sleepHours hours last night. Self-reported stress level (1-10): $stressLevel.
Give one short, kind suggestion to improve sleep or reduce stress. Plain text only.
${if (style.isNotEmpty()) "Match your tone to these personalization preferences: $style" else ""}"""
        callGemini(prompt) { result ->
            onResult(result ?: "Aim for 7-8 hours of sleep and short breaks during the day.")
        }
    }

    /**
     * Day/week progress report for a domain. period = "today" or "this week".
     */
    fun getDomainProgressReport(
        domainName: String,
        period: String,
        summary: String,
        metrics: Map<String, Any>? = null,
        extraStyleInstructions: String = "",
        onResult: (String?) -> Unit
    ) {
        val m = if (metrics.isNullOrEmpty()) "" else " Metrics: $metrics."
        val style = extraStyleInstructions.takeIf { it.isNotBlank() } ?: ""
        val prompt = """You are a friendly coach for the UniMind app. Give a short progress report (2-3 sentences) for the $domainName domain for $period.
Summary: $summary.$m
Acknowledge what went well and suggest one improvement for next period. Plain text only.
${if (style.isNotEmpty()) "Use these personalization preferences when writing: $style" else ""}"""
        callGemini(prompt) { result ->
            onResult(result ?: "Keep tracking for better insights.")
        }
    }
}
