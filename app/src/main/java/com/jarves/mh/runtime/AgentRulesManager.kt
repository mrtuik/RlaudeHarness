package com.jarves.mh.runtime

import android.content.Context
import java.io.File

/**
 * Maintains a single, app-wide "rules" document describing coding
 * conventions/do's-and-don'ts, and syncs it into every project workspace
 * before each agent run — so Claude Code, Antigravity and the DeepSeek
 * Harness all pick up the same standing instructions automatically.
 *
 * The master copy lives outside any project workspace, in app-private
 * storage, so it survives project deletion and rootfs reinstalls. Editing
 * it once (see [updateMasterRules]) changes what every future run of every
 * project sees — nothing needs to be edited per-project.
 *
 * Both `CLAUDE.md` and `AGENTS.md` are written to the workspace root with
 * identical content: Claude Code specifically looks for CLAUDE.md, while
 * AGENTS.md is the convention several other coding agents (including
 * DeepSeek's `dsh` and many OpenAI-style CLIs) check for. Writing both
 * covers Antigravity/Claude/DeepSeek without needing per-agent logic.
 */
class AgentRulesManager(private val context: Context) {

    private fun masterFile(): File =
        File(context.filesDir, "agent-rules/RULES.md").apply { parentFile?.mkdirs() }

    /** Returns the current master rules, writing sane defaults on first use. */
    fun currentRules(): String {
        val file = masterFile()
        if (!file.exists()) file.writeText(DEFAULT_RULES)
        return file.readText()
    }

    /** Overwrites the master rules. Takes effect on the next run of every project. */
    fun updateMasterRules(content: String) {
        masterFile().writeText(content)
    }

    /**
     * Writes the current master rules into [workspace] as CLAUDE.md and
     * AGENTS.md, overwriting whatever was there before. Call this right
     * after resolving the workspace and right before building the agent's
     * command line, for every agent bridge.
     */
    fun syncToWorkspace(workspace: File) {
        val master = currentRules()
        var rules = master
        if (!rules.contains(SCREEN_MAP_MARKER)) rules = rules.trimEnd() + "\n\n" + SCREEN_MAP_RULES
        if (!rules.contains(SUBAGENT_MARKER)) rules = rules.trimEnd() + "\n\n" + SUBAGENT_RULES
        val banner = "<!-- Auto-synced from Rlaude Harness app settings. " +
            "Edit the master copy in-app, not this file — it is overwritten every run. -->\n\n"
        listOf("CLAUDE.md", "AGENTS.md").forEach { name ->
            runCatching { File(workspace, name).writeText(banner + rules) }
        }
        writeSymbolIndex(workspace)
    }

    /**
     * Writes root-tab/SYMBOLS.md: a plain list of "file:line | kind | name" for every
     * function/composable found in the project's source files. Lets the agent jump to
     * a function without reading huge files (e.g. a 467KB Compose file). Regenerated
     * on every run; the root-tab folder is excluded from snapshots and "Files changed".
     */
    private fun writeSymbolIndex(workspace: File) {
        runCatching {
            val root = workspace.canonicalFile
            val out = StringBuilder()
            out.append("# SYMBOLS (auto-generated at the start of every run - do not edit)\n")
            out.append("# format: path:line | kind | name   (line numbers may shift after edits)\n")
            var entries = 0
            root.walkTopDown()
                .onEnter { dir -> dir == root || dir.name !in INDEX_SKIP_DIRS }
                .filter { it.isFile && it.length() <= INDEX_MAX_FILE_BYTES }
                .sortedBy { it.path }
                .forEach { file ->
                    if (entries >= INDEX_MAX_ENTRIES) return@forEach
                    val ext = file.extension.lowercase()
                    val pattern = when (ext) {
                        "kt" -> KT_FUN
                        "js", "jsx", "ts", "tsx", "html" -> JS_FUN
                        "py" -> PY_FUN
                        else -> null
                    } ?: return@forEach
                    val rel = file.relativeTo(root).path.replace('\\', '/')
                    val lines = file.readLines()
                    for ((i, text) in lines.withIndex()) {
                        if (entries >= INDEX_MAX_ENTRIES) break
                        val name = pattern.find(text)?.groupValues?.get(1) ?: continue
                        val kind = if (ext == "kt" &&
                            (text.contains("@Composable") || (i > 0 && lines[i - 1].contains("@Composable")))
                        ) "composable" else "fun"
                        out.append(rel).append(':').append(i + 1).append(" | ")
                            .append(kind).append(" | ").append(name).append('\n')
                        entries++
                    }
                }
            val target = File(root, "root-tab/SYMBOLS.md")
            target.parentFile?.mkdirs()
            val text = out.toString()
            if (!target.exists() || target.readText() != text) target.writeText(text)
        }
    }

    companion object {
        private const val SCREEN_MAP_MARKER = "## SCREEN MAP WORKFLOW"
        private const val SUBAGENT_MARKER = "## SUBAGENT WORKFLOW"

        // Manager/worker split for big multi-file tasks. Small tasks stay single-agent.
        // If the agent has no subagent tool (or it fails), it must fall back to working alone.
        private val SUBAGENT_RULES = """
            ## SUBAGENT WORKFLOW (single-agent mode)
            Work directly as a single agent. Read files and make edits directly using your tools.
            Do not spawn subagents for single-screen or routine edits, and do not use them to
            read code or to build root-tab/SCREENS.md. Use root-tab/SYMBOLS.md to jump straight
            to a function: grep its name there, then view only about 60 lines from that line.
            Only if the user explicitly asks for subagents may you use them; then wait until every
            subagent has finished and re-read the edited code yourself before finishing.
        """.trimIndent()

        private const val INDEX_MAX_ENTRIES = 6000
        private const val INDEX_MAX_FILE_BYTES = 2_000_000L
        private val INDEX_SKIP_DIRS = setOf(
            ".git", "node_modules", "build", ".gradle", ".cxx", "dist", ".idea",
            "third_party", "root-tab", ".claude",
        )
        private val KT_FUN = Regex("""^\s*(?:(?:private|internal|public|protected|override|suspend|inline|operator|tailrec)\s+)*fun\s+(?:<[^>]+>\s*)?(?:[A-Za-z0-9_]+\.)?([A-Za-z0-9_]+)\s*\(""")
        private val JS_FUN = Regex("""\bfunction\s+([A-Za-z0-9_]+)\s*\(""")
        private val PY_FUN = Regex("""^\s*def\s+([A-Za-z0-9_]+)\s*\(""")

        // Always appended at sync time (unless the master rules already contain it), so
        // devices that already have a saved RULES.md still get it. The agent builds and
        // maintains root-tab/SCREENS.md itself; the "root-tab" folder is the Root tab in
        // the Files UI and is excluded from version snapshots and "Files changed".
        private val SCREEN_MAP_RULES = """
            ## SCREEN MAP WORKFLOW (mandatory for every UI / screen / layout task)
            Goal: edit exactly the screen the user means, never a guessed one.
            The user may write short, messy, mixed Bengali/English text (Banglish). Do not
            depend on their wording; identify the screen from evidence.

            ### The map file: root-tab/SCREENS.md
            - Location is fixed: root-tab/SCREENS.md inside the workspace root. It is not part
              of the project source, so never copy it into source folders and NEVER create
              SCREENS.md or SYMBOLS.md at the project root or anywhere else.
            - If it does not exist, create it with just a one-line header. Do NOT scan the whole
              project and do NOT start subagents to build it. Build it on demand: add or refresh
              the entry for a screen only when you edit that screen or have to find it.
            - One compact entry per screen you have touched, 2-4 lines:
              Screen name | file | function/component (approx line range) | how the user
              reaches it | exact visible texts (titles, buttons, labels, hints).
            - If it exists, read it first. Treat line numbers as hints only: confirm with
              grep on the function name before editing, and fix stale entries.
            - Keep it under about 300 lines. Do not mention this file in your summaries.
            - root-tab/SYMBOLS.md is a second file, generated by the app at the start of every
              run (do not edit it). It lists path:line | kind | name for every function. To
              open a function, grep its name in SYMBOLS.md, then view only about 60 lines
              from that line instead of reading the whole file. Line numbers can shift after
              your own edits, so re-grep the file before a second edit.

            ### Every task that touches UI
            1. If a screenshot is attached, open it first. Write down the exact visible texts
               (titles, buttons, labels). The screenshot is the source of truth for WHICH
               screen; the user's words only say WHAT to change.
            2. If there is no screenshot, match the user's words against SCREENS.md (screen
               names, visible texts, how the screen is reached).
            3. Search the project for those exact texts to find the file and function, and
               cross-check with SCREENS.md.
            4. Before editing, write one plain line: Target = file, function, and the visible
               text that proves it is the right screen.
            5. If two or more screens are equally plausible, or nothing matches, call
               ask_question with the candidates. Never edit a guessed screen.
            6. If the screenshot looks like a design mockup (labels such as STATE A, annotation
               text, a different layout than the live app), match it to the closest existing
               screen and say which one you chose.
            7. Change only the target function/component plus helpers it needs. Do not touch
               other screens. Read the whole target function before editing it.
            8. If the task adds, removes or renames a screen or its visible texts, update
               root-tab/SCREENS.md before finishing.
        """.trimIndent()

        // Synthesized from patterns that recur across several public
        // Claude Code / agentic-coding best-practice collections (not
        // copied verbatim from any one of them): keep the file short so it
        // actually gets followed, state concrete commands rather than
        // vague advice, and let hard rules live in settings/hooks instead
        // of prose whenever the harness can enforce them directly.
        private val DEFAULT_RULES = """
            # Project conventions
            Keep this file short — long rule files are followed less
            reliably. Add a line only after the agent gets something wrong
            twice; don't pre-write rules for problems that haven't happened.

            ## Rules
            - Match the existing code style in each file you touch; do not
              reformat unrelated code.
            - Prefer small, focused diffs over broad rewrites unless asked.
            - Before creating a new file, check whether an existing one
              should be edited instead.
            - Do not delete or rename files unless the task requires it.
            - If the project has a lint or test command, run it before
              declaring a task done; report failures instead of guessing.
            - After making changes, briefly state what you changed and why.
            - When a task is ambiguous, state the assumption you're making
              and proceed, rather than stopping to ask.
        """.trimIndent()
    }
}
