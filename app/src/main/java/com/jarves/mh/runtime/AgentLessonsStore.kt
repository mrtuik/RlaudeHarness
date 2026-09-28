package com.jarves.mh.runtime

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject

/**
 * Offline, on-device log of mistakes the coding agents (Claude, DeepSeek, Antigravity)
 * have caught and fixed in their own generated code during self-verification, AND
 * mistakes the user explicitly pointed out that the agent then corrected.
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
    val source: String = AgentLessonsStore.SOURCE_SELF_VERIFICATION,
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
                $COL_SOURCE TEXT NOT NULL DEFAULT '$SOURCE_SELF_VERIFICATION',
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
    fun record(
        pattern: String,
        fix: String,
        language: String,
        source: String = SOURCE_SELF_VERIFICATION,
    ) {
        val normalizedPattern = pattern.trim().take(200)
        val normalizedFix = fix.trim().take(200)
        val normalizedLanguage = language.trim().lowercase().ifBlank { "general" }
        val normalizedSource = source.trim().lowercase().ifBlank { SOURCE_SELF_VERIFICATION }
        if (normalizedPattern.isBlank() || normalizedFix.isBlank()) return
        if (isBogusLesson(normalizedPattern, normalizedFix)) return
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
                        "UPDATE $TABLE SET $COL_OCCURRENCES = ?, $COL_LAST_SEEN_AT = ?, $COL_FIX = ?, $COL_SOURCE = ? WHERE $COL_ID = ?",
                        arrayOf(count + 1, now, normalizedFix, normalizedSource, id),
                    )
                } else {
                    val values = ContentValues().apply {
                        put(COL_PATTERN, normalizedPattern)
                        put(COL_FIX, normalizedFix)
                        put(COL_LANGUAGE, normalizedLanguage)
                        put(COL_OCCURRENCES, 1)
                        put(COL_SOURCE, normalizedSource)
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
                "SELECT $COL_PATTERN, $COL_FIX, $COL_LANGUAGE, $COL_OCCURRENCES, $COL_SOURCE FROM $TABLE " +
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
                        source = cursor.getString(4),
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
                        source = json.optString("source").ifBlank { SOURCE_SELF_VERIFICATION },
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
        purgeBogusLessons()
        val lessons = topLessons(limit)
        if (lessons.isEmpty()) return ""
        val sb = StringBuilder()
        sb.appendLine("KNOWN PAST MISTAKES — AVOID REPEATING THESE:")
        sb.appendLine("These are real mistakes this project's coding agents have made and fixed before, across all coding agents used in this app (not only the one running now). Do not repeat them.")
        lessons.forEach { lesson ->
            val tag = if (lesson.source == SOURCE_USER_CORRECTION) "user-corrected" else "self-caught"
            sb.appendLine("- [${lesson.language} · $tag] ${lesson.pattern} -> ${lesson.fix}")
        }
        return sb.toString().trimEnd('\n')
    }

    /**
     * The app compares the whole workspace before/after a run, so subagent edits ARE tracked.
     * An earlier agent wrongly recorded the opposite as a lesson; never store or replay it.
     */
    private fun isBogusLesson(pattern: String, fix: String): Boolean {
        val t = "$pattern $fix".lowercase()
        return t.contains("subagent") &&
            (t.contains("not registered") || t.contains("host app") || t.contains("tracking") || t.contains("not tracked"))
    }

    private fun purgeBogusLessons() {
        runCatching {
            writableDatabase.use { db ->
                db.execSQL(
                    "DELETE FROM $TABLE WHERE lower($COL_PATTERN || ' ' || $COL_FIX) LIKE '%subagent%' AND (" +
                        "lower($COL_PATTERN || ' ' || $COL_FIX) LIKE '%not registered%' OR " +
                        "lower($COL_PATTERN || ' ' || $COL_FIX) LIKE '%host app%' OR " +
                        "lower($COL_PATTERN || ' ' || $COL_FIX) LIKE '%tracking%' OR " +
                        "lower($COL_PATTERN || ' ' || $COL_FIX) LIKE '%not tracked%')",
                )
            }
        }
    }

    companion object {
        private const val DB_NAME = "agent_lessons.db"
        private const val DB_VERSION = 2
        private const val TABLE = "lessons"
        private const val COL_ID = "id"
        private const val COL_PATTERN = "pattern"
        private const val COL_FIX = "fix"
        private const val COL_LANGUAGE = "language"
        private const val COL_OCCURRENCES = "occurrences"
        private const val COL_SOURCE = "source"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_LAST_SEEN_AT = "last_seen_at"
        const val MARKER = "##LESSON##"
        const val SOURCE_SELF_VERIFICATION = "self_verification"
        const val SOURCE_USER_CORRECTION = "user_correction"
    }
}

/** Shared instruction text telling an agent how to report a lesson, appended to every bridge's prompt. */
internal const val AGENT_LESSON_REPORTING_INSTRUCTION = """LESSON REPORTING (for continuous improvement across future tasks and other coding agents):
Emit exactly one line with no other text on that line, in this exact format, in EITHER of these two cases:
##LESSON##{"pattern":"<short, general description of the mistake>","fix":"<short, general description of the correct fix>","language":"<file language, e.g. kotlin, javascript, css, html>","source":"<self_verification or user_correction>"}

Case 1 (source: self_verification) — your self-verification step catches and fixes a real syntax or compile error you made yourself.
Case 2 (source: user_correction) — the user explicitly points out that you made a mistake (wrong asset/icon used, an unwanted rewrite, an ignored instruction, etc.) and you correct it in this task.

Only emit this for genuine mistakes you actually made — never for issues that were already present before you started, and never more than once per distinct mistake in this task. Keep pattern/fix under 20 words each, written generally enough to apply to similar future situations, not tied to this specific file, variable, or project name. This line will be removed from what the user sees; it is only for the app's own records."""