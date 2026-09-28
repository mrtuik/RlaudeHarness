package com.jarves.mh.runtime

import java.io.File

/**
 * Agent-agnostic, code-level "evidence first" step.
 *
 * When the user's message contains build errors, stack traces or log lines that
 * point at `file:line`, the app itself opens those files, reads the surrounding
 * code and attaches it to the prompt BEFORE it reaches Claude Code, Antigravity
 * or DeepSeek. Every agent therefore starts from real file contents instead of
 * guessing from the error text — and this does not depend on the model choosing
 * to follow a prompt rule.
 */
object ErrorContextEnricher {
    private const val MAX_LOCATIONS = 4
    private const val CONTEXT_LINES = 12
    private const val MAX_TOTAL_CHARS = 7000
    private const val MAX_FILE_BYTES = 2_000_000L
    private const val MAX_SCAN_FILES = 20_000

    private val SKIP_DIRS = setOf(".git", "node_modules", "build", ".gradle", ".cxx", "dist", ".idea", "third_party")

    private val LOCATION = Regex(
        """([A-Za-z0-9_./\\:\-]+\.(?:kt|kts|java|xml|gradle|js|jsx|ts|tsx|py|html|css|json|rs|go|c|cc|cpp|h|sh|md|yml|yaml|toml)):(\d{1,6})(?::(\d{1,4}))?"""
    )
    private val UNRESOLVED = Regex("""Unresolved reference[:']?\s*'?([A-Za-z_][A-Za-z0-9_]*)'?""")

    fun enrich(prompt: String, workspace: File): String {
        return runCatching { build(prompt, workspace) }.getOrDefault(prompt)
    }

    private fun build(prompt: String, workspace: File): String {
        if (prompt.length < 12) return prompt
        val root = workspace.canonicalFile
        val seen = LinkedHashSet<Pair<String, Int>>()
        val hits = ArrayList<Triple<String, Int, String>>() // rawPath, line, lineText
        for (m in LOCATION.findAll(prompt)) {
            val raw = m.groupValues[1]
            val line = m.groupValues[2].toIntOrNull() ?: continue
            if (line <= 0 || !seen.add(raw to line)) continue
            hits += Triple(raw, line, prompt.lineContaining(m.range.first))
            if (hits.size >= MAX_LOCATIONS) break
        }
        if (hits.isEmpty()) return prompt

        val out = StringBuilder()
        var attached = 0
        val missing = ArrayList<String>()
        var index: Map<String, List<File>>? = null
        for ((raw, line, promptLine) in hits) {
            val file = resolve(root, raw) ?: run {
                val name = raw.substringAfterLast('/').substringAfterLast('\\')
                if (index == null) index = indexByName(root)
                index!![name]?.singleOrNull()
            }
            if (file == null || !file.isFile || file.length() > MAX_FILE_BYTES) {
                missing += "$raw:$line"
                continue
            }
            val lines = file.readLines()
            if (line > lines.size) {
                missing += "$raw:$line (file has only ${lines.size} lines — it may have changed since the error)"
                continue
            }
            val rel = file.canonicalFile.relativeTo(root).path
            val from = maxOf(1, line - CONTEXT_LINES)
            val to = minOf(lines.size, line + CONTEXT_LINES)
            val block = StringBuilder()
            block.append("### ").append(rel).append(':').append(line).append("  (").append(lines.size).append(" lines total)\n```\n")
            for (n in from..to) {
                block.append(if (n == line) "> " else "  ")
                    .append(n.toString().padStart(5)).append(" | ").append(lines[n - 1]).append('\n')
            }
            block.append("```\n")

            UNRESOLVED.find(promptLine)?.groupValues?.get(1)?.let { sym ->
                val imports = lines.filter { it.trimStart().startsWith("import ") && it.contains(sym) }
                val uses = lines.count { it.contains(sym) }
                block.append("Check for `").append(sym).append("`: ")
                    .append(if (imports.isEmpty()) "NO import mentioning it in this file" else "imports: " + imports.joinToString("; ") { it.trim() })
                    .append("; appears on ").append(uses).append(" line(s) in this file.\n")
            }
            if (out.length + block.length > MAX_TOTAL_CHARS) break
            out.append(block).append('\n')
            attached++
        }
        if (attached == 0 && missing.isEmpty()) return prompt

        val note = StringBuilder(prompt.trimEnd())
        note.append("\n\n---\n[Rlaude Harness auto-attached evidence — read directly from the project files, not guessed. ")
        note.append("The file may have changed since the error was produced, so re-read before editing.]\n\n")
        note.append(out)
        if (missing.isNotEmpty()) {
            note.append("Could not open in this project: ").append(missing.joinToString(", "))
                .append(". Locate them with grep/find instead of assuming.\n")
        }
        return note.toString()
    }

    /** Tries the path as-is, then progressively strips leading segments (CI runners use other roots). */
    private fun resolve(root: File, raw: String): File? {
        val parts = raw.replace('\\', '/').removePrefix("file://").split('/').filter { it.isNotEmpty() && !it.endsWith(":") }
        for (start in parts.indices) {
            val candidate = File(root, parts.drop(start).joinToString("/"))
            if (candidate.isFile && candidate.canonicalPath.startsWith(root.path)) return candidate
        }
        return null
    }

    private fun indexByName(root: File): Map<String, List<File>> {
        val map = HashMap<String, MutableList<File>>()
        var count = 0
        val stack = ArrayDeque<File>().apply { add(root) }
        while (stack.isNotEmpty() && count < MAX_SCAN_FILES) {
            val dir = stack.removeLast()
            val children = dir.listFiles() ?: continue
            for (f in children) {
                if (f.isDirectory) {
                    if (f.name !in SKIP_DIRS) stack.add(f)
                } else {
                    count++
                    map.getOrPut(f.name) { ArrayList() }.add(f)
                }
            }
        }
        return map
    }

    private fun String.lineContaining(index: Int): String {
        val start = lastIndexOf('\n', index).let { if (it < 0) 0 else it + 1 }
        val end = indexOf('\n', index).let { if (it < 0) length else it }
        return substring(start, end)
    }
}
