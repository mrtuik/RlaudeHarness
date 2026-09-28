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
        val rules = if (master.contains(SCREEN_MAP_MARKER)) master else master.trimEnd() + "\n\n" + SCREEN_MAP_RULES
        val banner = "<!-- Auto-synced from Rlaude Harness app settings. " +
            "Edit the master copy in-app, not this file — it is overwritten every run. -->\n\n"
        listOf("CLAUDE.md", "AGENTS.md").forEach { name ->
            runCatching { File(workspace, name).writeText(banner + rules) }
        }
    }

    companion object {
        private const val SCREEN_MAP_MARKER = "## SCREEN MAP WORKFLOW"

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
              of the project source, so never copy it into source folders.
            - If it does not exist, create it BEFORE anything else by scanning the UI code
              (use grep/find, do not read huge files top to bottom). Cover every screen,
              page, dialog, bottom sheet, tab, menu and major section.
            - One compact entry per screen, 2-4 lines:
              Screen name | file | function/component (approx line range) | how the user
              reaches it | exact visible texts (titles, buttons, labels, hints).
            - If it exists, read it first. Treat line numbers as hints only: confirm with
              grep on the function name before editing, and fix stale entries.
            - Keep it under about 300 lines. Do not mention this file in your summaries.

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
