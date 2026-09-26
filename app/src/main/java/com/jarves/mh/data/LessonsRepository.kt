package com.jarves.mh.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LessonsRepository(private val preferences: AppPreferences) {

    fun getCachedLessons(): List<String> {
        val raw = preferences.cachedLessonsJson
        return parseLessonsJson(raw)
    }

    suspend fun refreshLessons(repo: String = preferences.githubLessonsRepo): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val targetRepo = repo.trim().ifBlank { "mrtuik/RlaudeHarnessAgent" }
                val url = "https://raw.githubusercontent.com/$targetRepo/main/lessons.json"
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "PocketDev-Android")
                }

                val status = connection.responseCode
                if (status !in 200..299) {
                    error("Failed to fetch lessons.json: HTTP $status")
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val parsed = parseLessonsJson(body)
                preferences.cachedLessonsJson = body
                preferences.lastLessonsFetchMillis = System.currentTimeMillis()
                parsed
            }
        }

    fun formatLessonsForPrompt(max: Int = 30): String {
        val lessons = getCachedLessons()
        if (lessons.isEmpty()) return ""

        val limited = lessons.take(max)
        return buildString {
            appendLine("=== SHARED AGENT LESSONS & GUIDELINES ===")
            appendLine("Apply the following learned lessons from previous runs and user corrections:")
            limited.forEachIndexed { index, lesson ->
                appendLine("${index + 1}. $lesson")
            }
            appendLine("=========================================")
        }
    }

    private fun parseLessonsJson(rawJson: String): List<String> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            val trimmed = rawJson.trim()
            if (!trimmed.startsWith("[")) return emptyList()
            val array = JSONArray(trimmed)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val item = array.get(i)
                when (item) {
                    is String -> {
                        val text = item.trim()
                        if (text.isNotBlank()) list.add(text)
                    }
                    is JSONObject -> {
                        val lessonText = item.optString("rule").takeIf(String::isNotBlank)
                            ?: item.optString("lesson").takeIf(String::isNotBlank)
                            ?: item.optString("summary").takeIf(String::isNotBlank)
                            ?: item.optString("text").takeIf(String::isNotBlank)
                            ?: item.optString("description").takeIf(String::isNotBlank)
                            ?: item.optString("title").takeIf(String::isNotBlank)
                        if (!lessonText.isNullOrBlank()) {
                            list.add(lessonText.trim())
                        }
                    }
                }
            }
            list
        }.getOrDefault(emptyList())
    }
}
