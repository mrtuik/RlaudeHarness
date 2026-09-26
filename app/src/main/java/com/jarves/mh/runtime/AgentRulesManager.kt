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
        val rules = currentRules()
        val banner = "<!-- Auto-synced from Rlaude Harness app settings. " +
            "Edit the master copy in-app, not this file — it is overwritten every run. -->\n\n"
        listOf("CLAUDE.md", "AGENTS.md").forEach { name ->
            runCatching { File(workspace, name).writeText(banner + rules) }
        }
    }

    companion object {
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
