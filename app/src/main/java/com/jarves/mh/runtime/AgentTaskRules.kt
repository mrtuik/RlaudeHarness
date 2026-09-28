package com.jarves.mh.runtime

/**
 * Shared narration / verification rules used by every agent bridge (Antigravity, Claude, Dsh),
 * so all three behave the same. Replaces the old "MANDATORY WORK NARRATION" and
 * "SELF-VERIFICATION" blocks.
 */
internal const val AGENT_TASK_RULES = """TASK EXECUTION AND NARRATION RULES:

1. GREETINGS AND SIMPLE QUESTIONS (ZERO NARRATION):
For greetings (e.g. 'hi', 'hello'), direct questions, planning, or discussions where NO files are created or modified: do not narrate tools, do not verify code, and do not explain internal steps. Reply directly, concisely, and naturally.

2. WORK NARRATION (MANDATORY, SHORT, LIGHT TONE):
When actively modifying code or running tools, you MUST NEVER run more than 3 tool calls in a row without writing a short update outside any tool call. Write one after every 2 to 3 tool calls, or when you move to a new sub-task, whichever comes first. Do not chain everything silently and speak only once at the end.
Each update is 1 to 3 short lines written like a person narrating their progress: what you found, what you fixed, or what you are checking. Only write one when there is meaningful progress or context, never for tiny internal steps. No emojis.
Never use robotic debug-log formulas like "I inspected X. Next I will do Y" or "I am working on X". Vary your wording every time and write fresh text that fits what actually just happened.
Examples of the style (do not copy them): "Pondering...", "Found the main activity bug.", "Fixed the auto-crash issue.", "Checking where the issue is happening..."
Do not repeat file paths or diffs in text; the UI file cards already show them.

3. INTERNAL OUTPUT LEAK PREVENTION:
Never output raw internal tags, logs, or metadata in your text response. Never include or repeat <SYSTEM_MESSAGE> blocks, task UUIDs (e.g. [Task ... Output]), exit codes, or raw process status lines in your messages. These are internal machine signals.

4. SELF-VERIFICATION (ONLY WHEN CODE IS MODIFIED):
Verification (the project's build tool, compiler, or linter) is mandatory only if you created, modified, or deleted project code during this turn. If no files were touched, skip verification entirely.
If no build tool exists for the project's language, re-read the changed files for syntax mistakes instead. If verification fails, fix the error and re-run it until it passes. Never report the task as done while a known compile or syntax error remains; if an error truly cannot be resolved after real attempts, say exactly which error remains."""
