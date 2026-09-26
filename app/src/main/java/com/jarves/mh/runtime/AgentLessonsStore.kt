package com.jarves.mh.runtime

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject

/**
 * Offline, on-device log of mistakes the coding agents (Claude, DeepSeek, Antigravity)
 * have caught and fixed in their own generated code during self-verification.
 *
 * Every bridge shares this same SQLite file (by DB name) so a lesson learned by one
 * agent is available to all of them on the next task, regardless of which agent runs it.
 * There is no server involved; this never leaves the device.
 */
data class AgentLesson(
    val pattern: String,
    val fix: String,
    val language: String,
    val occurrences: Int = 1,
)

class AgentLessonsStore(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PATTERN TEXT NOT NULL,
                $COL_FIX TEXT NOT NULL,
                $COL_LANGUAGE TEXT NOT NULL,
                $COL_OCCURRENCES INTEGER NOT NULL DEFAULT 1,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_LAST_SEEN_AT INTEGER NOT NULL,
                UNIQUE($COL_PATTERN, $COL_LANGUAGE)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    /** Insert a new lesson, or bump the occurrence count if this exact mistake was seen before. */
    fun record(pattern: String, fix: String, language: String) {
        val normalizedPattern = pattern.trim().take(200)
        val normalizedFix = fix.trim().take(200)
        val normalizedLanguage = language.trim().lowercase().ifBlank { "general" }
        if (normalizedPattern.isBlank() || normalizedFix.isBlank()) return
        val now = System.currentTimeMillis()
        runCatching {
            writableDatabase.use { db ->
                val existing = db.rawQuery(
                    "SELECT $COL_ID, $COL_OCCURRENCES FROM $TABLE WHERE $COL_PATTERN = ? AND $COL_LANGUAGE = ?",
                    arrayOf(normalizedPattern, normalizedLanguage),
                )
                val found = existing.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getLong(0) to cursor.getInt(1) else null
                }
                if (found != null) {
                    val (id, count) = found
                    db.execSQL(
                        "UPDATE $TABLE SET $COL_OCCURRENCES = ?, $COL_LAST_SEEN_AT = ?, $COL_FIX = ? WHERE $COL_ID = ?",
                        arrayOf(count + 1, now, normalizedFix, id),
                    )
                } else {
                    val values = ContentValues().apply {
                        put(COL_PATTERN, normalizedPattern)
                        put(COL_FIX, normalizedFix)
                        put(COL_LANGUAGE, normalizedLanguage)
                        put(COL_OCCURRENCES, 1)
                        put(COL_CREATED_AT, now)
                        put(COL_LAST_SEEN_AT, now)
                    }
                    db.insert(TABLE, null, values)
                }
            }
        }
    }

    fun topLessons(limit: Int = 8): List<AgentLesson> = runCatching {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT $COL_PATTERN, $COL_FIX, $COL_LANGUAGE, $COL_OCCURRENCES FROM $TABLE " +
                    "ORDER BY $COL_OCCURRENCES DESC, $COL_LAST_SEEN_AT DESC LIMIT ?",
                arrayOf(limit.toString()),
            ).use { cursor ->
                val out = mutableListOf<AgentLesson>()
                while (cursor.moveToNext()) {
                    out += AgentLesson(
                        pattern = cursor.getString(0),
                        fix = cursor.getString(1),
                        language = cursor.getString(2),
                        occurrences = cursor.getInt(3),
                    )
                }
                out
            }
        }
    }.getOrDefault(emptyList())

    /**
     * Scans assistant-visible text for one-line ##LESSON##{...} markers, records each as a
     * lesson, and returns the text with those marker lines removed so the raw JSON is never
     * shown to the user in chat.
     */
    fun extractAndStore(text: String): String {
        if (!text.contains(MARKER)) return text
        val kept = StringBuilder()
        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith(MARKER)) {
                runCatching {
                    val json = JSONObject(trimmed.removePrefix(MARKER).trim())
                    record(
                        pattern = json.optString("pattern"),
                        fix = json.optString("fix"),
                        language = json.optString("language"),
                    )
                }
            } else {
                kept.appendLine(line)
            }
        }
        return kept.toString().trimEnd('\n')
    }

    /**
     * Formatted prompt section listing the most common past mistakes across all agents,
     * or an empty string if nothing has been learned yet. Callers should only append this
     * to a prompt when it is non-blank.
     */
    fun buildLessonsPromptSection(limit: Int = 8): String {
        val lessons = topLessons(limit)
        if (lessons.isEmpty()) return ""
        val sb = StringBuilder()
        sb.appendLine("KNOWN PAST MISTAKES — AVOID REPEATING THESE:")
        sb.appendLine("These are real mistakes this project's coding agents have made and fixed before, across all coding agents used in this app (not only the one running now). Do not repeat them.")
        lessons.forEach { lesson ->
            sb.appendLine("- [${lesson.language}] ${lesson.pattern} -> ${lesson.fix}")
        }
        return sb.toString().trimEnd('\n')
    }

    companion object {
        private const val DB_NAME = "agent_lessons.db"
        private const val DB_VERSION = 1
        private const val TABLE = "lessons"
        private const val COL_ID = "id"
        private const val COL_PATTERN = "pattern"
        private const val COL_FIX = "fix"
        private const val COL_LANGUAGE = "language"
        private const val COL_OCCURRENCES = "occurrences"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_LAST_SEEN_AT = "last_seen_at"
        const val MARKER = "##LESSON##"
    }
}

/** Shared instruction text telling an agent how to report a lesson, appended to every bridge's prompt. */
internal const val AGENT_LESSON_REPORTING_INSTRUCTION = """LESSON REPORTING (for continuous improvement across future tasks and other coding agents):
Whenever your self-verification step catches and fixes a real syntax or compile error, immediately after fixing it, emit exactly one line with no other text on that line, in this exact format:
##LESSON##{"pattern":"<short, general description of the mistake>","fix":"<short, general description of the correct fix>","language":"<file language, e.g. kotlin, javascript, css, html>"}
Only emit this for genuine mistakes you actually made and fixed yourself — never for issues that were already present before you started, and never more than once per distinct mistake in this task. Keep both fields under 20 words, written generally enough to apply to similar future code, not tied to this specific file, variable, or project name. This line will be removed from what the user sees; it is only for the app's own records."""
