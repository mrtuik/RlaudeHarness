package com.jarves.mh.runtime

import android.content.Context
import androidx.core.content.ContextCompat
import com.jarves.mh.model.ChatMessage
import com.jarves.mh.model.ChangeItem
import com.jarves.mh.model.ProjectKind
import com.jarves.mh.model.ProviderProfile
import com.jarves.mh.model.RuntimeEvent
import com.jarves.mh.model.ToolRequest
import com.jarves.mh.model.QuestionRequest
import java.io.File
import java.io.RandomAccessFile
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

enum class AntigravityAuthStatus { SIGNED_OUT, STARTING, AWAITING_CODE, COMPLETING, SIGNED_IN, ERROR }

data class AntigravityAuthState(
    val status: AntigravityAuthStatus = AntigravityAuthStatus.SIGNED_OUT,
    val authorizationUrl: String? = null,
    val message: String? = null,
    val accountEmail: String? = null,
)

internal sealed interface AntigravityParsedEvent {
    data class Initialized(val conversationId: String) : AntigravityParsedEvent
    data class Text(val value: String) : AntigravityParsedEvent
    data class ToolStarted(val name: String, val detail: String) : AntigravityParsedEvent
    data class ToolCompleted(val name: String, val detail: String) : AntigravityParsedEvent
    data class Question(
        val questionId: String,
        val text: String,
        val options: List<String>,
        val allowFreeText: Boolean = true,
        val allowSkip: Boolean = true,
    ) : AntigravityParsedEvent
    data class Result(
        val conversationId: String?,
        val status: String,
        val response: String?,
        val error: String?,
    ) : AntigravityParsedEvent
}

internal object AntigravityEventParser {
    fun parse(line: String): AntigravityParsedEvent? {
        val root = runCatching { JSONObject(line) }.getOrNull() ?: return null
        return when (root.optString("event")) {
            "init" -> root.optString("conversation_id")
                .takeIf(String::isNotBlank)
                ?.let(AntigravityParsedEvent::Initialized)
            "step_update" -> {
                val step = root.optJSONObject("step_update") ?: return null
                val delta = step.optString("text_delta")
                if (delta.isNotEmpty()) return AntigravityParsedEvent.Text(delta)
                val type = step.optString("step_type")
                if (type.contains("tool", true) || type.contains("command", true)) {
                    val rawName = step.optString("tool_name").ifBlank {
                        step.optJSONObject("tool_info")?.optString("name").orEmpty()
                    }.ifBlank { type.ifBlank { "Tool" } }

                    if (rawName.equals("ask_question", true) || rawName.equals("AskUserQuestion", true)) {
                        val info = step.optJSONObject("tool_info")
                        val params = info?.optJSONObject("parameters")
                        val questionsArr = params?.optJSONArray("questions")
                        val firstQ = questionsArr?.optJSONObject(0)
                        val questionText = firstQ?.optString("question")?.ifBlank { null }
                            ?: params?.optString("question")?.ifBlank { null }
                            ?: step.optString("description").ifBlank { "The agent is asking a clarifying question." }
                        val optionsArr = firstQ?.optJSONArray("options") ?: params?.optJSONArray("options")
                        val options = (0 until (optionsArr?.length() ?: 0))
                            .mapNotNull { optionsArr?.optString(it)?.takeIf(String::isNotBlank) }
                        val questionId = step.optString("id").ifBlank { UUID.randomUUID().toString() }
                        return AntigravityParsedEvent.Question(
                            questionId = questionId,
                            text = questionText,
                            options = options,
                            allowFreeText = true,
                            allowSkip = true,
                        )
                    }

                    val name = antigravityToolDisplayName(rawName)
                    val detail = antigravityToolDetail(step, rawName).ifBlank { name }
                    if (step.optString("state") == "DONE") {
                        AntigravityParsedEvent.ToolCompleted(name, detail)
                    } else {
                        AntigravityParsedEvent.ToolStarted(name, detail)
                    }
                } else null
            }
            "result" -> {
                val result = root.optJSONObject("result") ?: root
                AntigravityParsedEvent.Result(
                    result.optString("conversation_id").takeIf(String::isNotBlank),
                    result.optString("status", "ERROR"),
                    result.optString("response").takeIf(String::isNotBlank),
                    result.optString("error").takeIf(String::isNotBlank),
                )
            }
            else -> null // Forward compatible with new agy event types.
        }
    }
}

private fun antigravityToolDisplayName(name: String): String = when (name.lowercase()) {
    "run_command" -> "Bash"
    "write_to_file", "replace_file_content", "multi_replace_file_content" -> "Write"
    "view_file" -> "Read"
    "list_dir" -> "List files"
    "grep_search", "search_files" -> "Search"
    "schedule" -> "Wait"
    "manage_task" -> "Manage task"
    else -> name.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

private fun antigravityToolDetail(step: JSONObject, rawName: String): String {
    step.optString("description").takeIf(String::isNotBlank)?.let { return redactToolDetail(it) }
    val info = step.optJSONObject("tool_info")
    val parameters = info?.optJSONObject("parameters")
    if (rawName.equals("invoke_subagent", true)) {
        antigravitySubagentSummary(parameters)?.let { return redactToolDetail(it) }
    }
    val preferredKeys = when (rawName.lowercase()) {
        "run_command" -> listOf("CommandLine", "command", "cmd")
        "write_to_file", "replace_file_content", "multi_replace_file_content" ->
            listOf("TargetFile", "AbsolutePath", "path", "file")
        "view_file" -> listOf("AbsolutePath", "TargetFile", "path", "file")
        "list_dir" -> listOf("DirectoryPath", "AbsolutePath", "path")
        "grep_search", "search_files" -> listOf("Query", "Pattern", "query", "pattern")
        "schedule" -> listOf("Prompt", "DurationSeconds")
        "manage_task" -> listOf("Action", "TaskId")
        else -> emptyList()
    }
    preferredKeys.forEach { key ->
        parameters?.opt(key)?.toString()?.takeIf { it.isNotBlank() && it != "null" }?.let {
            val prefix = when {
                rawName.equals("schedule", true) && key == "DurationSeconds" -> "Wait "
                rawName.equals("manage_task", true) -> "$key: "
                else -> ""
            }
            val suffix = if (rawName.equals("schedule", true) && key == "DurationSeconds") "s" else ""
            return redactToolDetail(prefix + it + suffix)
        }
    }
    return parameters?.toString()?.takeUnless { it == "{}" }?.let(::redactToolDetail)
        ?: step.optJSONObject("tool")?.toString()?.let(::redactToolDetail)
        .orEmpty()
}

/** "READER: X + WRITER: X" instead of the raw {"Subagents":[...]} JSON on the Invoke subagent card. */
private fun antigravitySubagentSummary(parameters: JSONObject?): String? {
    val list = parameters?.optJSONArray("Subagents") ?: return null
    val labels = (0 until list.length()).mapNotNull { i ->
        val item = list.optJSONObject(i) ?: return@mapNotNull null
        val explicit = listOf("Name", "Title", "Description", "Type")
            .firstNotNullOfOrNull { key -> item.optString(key).takeIf(String::isNotBlank) }
        val firstLine = item.optString("Prompt").lineSequence().map(String::trim).firstOrNull(String::isNotEmpty)
        val named = firstLine?.takeIf { it.startsWith("READER", true) || it.startsWith("WRITER", true) }
        (named ?: explicit ?: firstLine)?.take(60)
    }
    return labels.takeIf { it.isNotEmpty() }?.joinToString(" + ")
}

private fun redactToolDetail(value: String): String = value
    .replace(Regex("(?i)(api[_-]?key|token|secret|password)(\\s*[=:]\\s*)([^\\s'\"]+)"), "$1$2••••")
    .replace(Regex("(?i)(authorization:\\s*bearer\\s+)[^\\s'\"]+"), "$1••••")
    .replace(Regex("\\s+"), " ")
    .trim()
    .take(500)

/** Official Antigravity CLI bridge. OAuth and credentials remain owned by agy. */
private const val HELLO_TIMEOUT_MILLIS = 90_000L
class AntigravityRuntimeBridge(
    private val context: Context,
    private val model: () -> String,
    private val effort: () -> String,
    private val conversationId: (String) -> String?,
    private val saveConversationId: (String, String) -> Unit,
) : RuntimeBridge {
    private val installer = RuntimeInstaller(context)
    private val checkpoints = WorkspaceCheckpoints(context.filesDir)
    private val lessons = AgentLessonsStore(context)
    private val agentRules = AgentRulesManager(context)
    private val eventBus = MutableSharedFlow<RuntimeEvent>(extraBufferCapacity = 64)
    override val events: Flow<RuntimeEvent> = eventBus
    private val finished = ConcurrentHashMap.newKeySet<String>()
    @Volatile private var activeProcess: Process? = null
    @Volatile private var activeSessionId: String? = null
    @Volatile private var userStopRequested = false
    @Volatile private var foregroundResultPosted = false

    fun configureProjectRoot(projectId: String, rootPath: String) = checkpoints.configureProjectRoot(projectId, rootPath)

    /**
     * Waits (max ~4s) until no file in the workspace changes between two checks, so a write
     * that lands right as agy exits (e.g. from a subagent) is still part of "Files changed".
     */
    private suspend fun awaitWorkspaceSettled(ws: File) {
        var last = checkpoints.quickSignature(ws)
        repeat(4) {
            delay(1_000)
            val now = checkpoints.quickSignature(ws)
            if (now == last) return
            last = now
        }
    }

    /**
     * Lightweight connectivity probe: sends a tiny hello to agy and returns its
     * reply text. No foreground service, no checkpoints, no saved conversation.
     * The timeout is intentionally internal — callers only see success/failure.
     */
    suspend fun hello(timeoutMillis: Long = HELLO_TIMEOUT_MILLIS): String = withContext(Dispatchers.IO) {
        if (!installer.isAgentInstalled(com.jarves.mh.model.AgentKind.ANTIGRAVITY)) {
            throw IllegalStateException("Antigravity CLI is not installed.")
        }
        try {
            withTimeout(timeoutMillis) {
                val installed = installer.installedRuntime()
                val probeDir = File(context.cacheDir, "agy-hello").apply { mkdirs() }
                val command = buildList {
                    add(RuntimeInstaller.AGY_GUEST_PATH)
                    addAll(listOf("--input-format", "stream-json"))
                    addAll(listOf("--output-format", "stream-json"))
                    addAll(listOf("--print-timeout", "2m"))
                    add("--dangerously-skip-permissions")
                    addAntigravitySelection(model(), effort())
                    add("--new-project")
                }
                val outputFile = File(context.cacheDir, "agy-hello-${System.nanoTime()}.log")
                val process = installer.process(
                    installed.proot,
                    installed.rootfs,
                    probeDir,
                    emptyMap(),
                    command,
                    guestWorkspacePath = "/workspace/hello",
                    emulateHardLinks = false,
                    outputFile = outputFile,
                )
                try {
                    val request = JSONObject()
                        .put("event", "user")
                        .put("message", JSONObject().put("content", "Reply with exactly: ok"))
                        .toString() + "\n"
                    process.outputStream.write(request.toByteArray())
                    process.outputStream.flush()
                    process.outputStream.close()
                    var offset = 0L
                    val pending = StringBuilder()
                    var reply: String? = null
                    fun handleLine(line: String): Boolean {
                        when (val event = AntigravityEventParser.parse(line)) {
                            is AntigravityParsedEvent.Text -> if (reply == null && event.value.isNotBlank()) reply = event.value
                            is AntigravityParsedEvent.Result -> {
                                if (event.status.equals("SUCCESS", ignoreCase = true)) {
                                    if (reply == null && !event.response.isNullOrBlank()) reply = event.response
                                    return true
                                }
                                throw AntigravitySessionException(friendlyError(event.error ?: event.status))
                            }
                            else -> Unit
                        }
                        return false
                    }
                    var done = false
                    while (!done && (process.isAlive || outputFile.length() > offset)) {
                        val available = outputFile.length() - offset
                        if (available <= 0) {
                            delay(100)
                            continue
                        }
                        val bytes = ByteArray(minOf(available, 16L * 1024).toInt())
                        val count = RandomAccessFile(outputFile, "r").use { file ->
                            file.seek(offset)
                            file.read(bytes)
                        }
                        if (count <= 0) continue
                        offset += count
                        pending.append(bytes.decodeToString(0, count))
                        var newline = pending.indexOf("\n")
                        while (newline >= 0) {
                            val line = pending.substring(0, newline).trimEnd('\r')
                            pending.delete(0, newline + 1)
                            if (handleLine(line)) {
                                done = true
                                break
                            }
                            newline = pending.indexOf("\n")
                        }
                    }
                    pending.toString().trim().takeIf(String::isNotEmpty)?.let { if (!done) done = handleLine(it) }
                    // Drain process exit without hanging past the timeout.
                    withContext(NonCancellable) {
                        runCatching { process.waitFor() }
                    }
                    check(done) { friendlyError(pending.toString().takeLast(500).ifBlank { "Antigravity exited without answering" }) }
                    reply?.trim().takeUnless { it.isNullOrEmpty() } ?: "ok"
                } finally {
                    runCatching { process.destroy() }
                    runCatching {
                        if (process.isAlive) process.destroyForcibly()
                    }
                    runCatching { outputFile.delete() }
                    runCatching { probeDir.deleteRecursively() }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw AntigravitySessionException("Antigravity did not answer. Try again.")
        }
    }

    override suspend fun startSession(
        projectId: String,
        projectSlug: String,
        projectKind: ProjectKind,
        prompt: String,
        conversationHistory: List<ChatMessage>,
        provider: ProviderProfile,
        projectName: String,
    ): String = withContext(Dispatchers.IO + NonCancellable) {
        val sessionId = UUID.randomUUID().toString()
        activeSessionId = sessionId
        userStopRequested = false
        foregroundResultPosted = false
        finished.remove(sessionId)
        eventBus.emit(RuntimeEvent.SessionStarted(sessionId))
        if (!installer.isAgentInstalled(com.jarves.mh.model.AgentKind.ANTIGRAVITY)) {
            emitFailure(sessionId, "Antigravity CLI is not installed. Open Settings → Coding agent to install it.")
            return@withContext sessionId
        }

        var workspace: File? = null
        var before: Map<String, String>? = null
        runCatching {
            RuntimeTaskController.stopAction = {
                userStopRequested = true
                activeProcess?.destroy()
            }
            startForegroundRuntime(projectName)
            val installed = installer.installedRuntime()
            val ws = checkpoints.ensureWorkspace(projectId)
            workspace = ws
            agentRules.syncToWorkspace(ws)
            checkpoints.createCheckpoint(projectId, ws)
            val snapshotBefore = checkpoints.snapshot(ws)
            before = snapshotBefore
            val command = antigravityCommand(model(), effort(), conversationId(projectId))
            val process = installer.process(
                installed.proot,
                installed.rootfs,
                ws,
                emptyMap(),
                command,
                guestWorkspacePath = "/workspace/$projectSlug",
                emulateHardLinks = false,
            )
            activeProcess = process
            if (userStopRequested) process.destroy()
            val request = JSONObject()
                .put("event", "user")
                .put("message", JSONObject().put("content", antigravityWorkspacePrompt(projectSlug, ErrorContextEnricher.enrich(prompt, ws), lessons.buildLessonsPromptSection())))
                .toString() + "\n"
            process.outputStream.write(request.toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val native = process as? NativeSpawnProcess ?: error("Unsupported Antigravity process")
            var offset = 0L
            val pending = StringBuilder()
            var resultSeen = false
            var assistantTextSeen = false
            // Strips <SYSTEM_MESSAGE> blocks and [Task ... Output] markers even when split across deltas.
            val textFilter = AntigravityOutputFilter()
            suspend fun emitCleanText(text: String) {
                if (text.isBlank()) return
                assistantTextSeen = true
                eventBus.emit(RuntimeEvent.AssistantDelta(sessionId, text))
            }
            suspend fun handleLine(line: String) {
                when (val event = AntigravityEventParser.parse(line)) {
                    is AntigravityParsedEvent.Initialized -> saveConversationId(projectId, event.conversationId)
                    is AntigravityParsedEvent.Text -> emitCleanText(textFilter.feed(event.value))
                    is AntigravityParsedEvent.ToolStarted -> eventBus.emit(RuntimeEvent.ToolStarted(sessionId, event.name, event.detail))
                    is AntigravityParsedEvent.ToolCompleted -> eventBus.emit(RuntimeEvent.ToolCompleted(sessionId, event.name, event.detail))
                    is AntigravityParsedEvent.Question -> {
                        eventBus.emit(
                            RuntimeEvent.QuestionAsked(
                                sessionId,
                                QuestionRequest(
                                    questionId = event.questionId,
                                    sessionId = sessionId,
                                    text = event.text,
                                    options = event.options,
                                    allowFreeText = event.allowFreeText,
                                    allowSkip = event.allowSkip,
                                ),
                            ),
                        )
                    }
                    is AntigravityParsedEvent.Result -> {
                        event.conversationId?.let { saveConversationId(projectId, it) }
                        if (!assistantTextSeen && !event.response.isNullOrBlank()) {
                            val cleaned = AntigravityOutputFilter.stripAll(event.response)
                            if (cleaned.isNotBlank()) {
                                assistantTextSeen = true
                                eventBus.emit(RuntimeEvent.AssistantDelta(sessionId, lessons.extractAndStore(cleaned)))
                            }
                        }
                        if (event.status.equals("SUCCESS", ignoreCase = true)) {
                            resultSeen = true
                        } else throw AntigravitySessionException(friendlyError(event.error ?: event.status))
                    }
                    null -> Unit
                }
            }
            while (process.isAlive || native.outputFile.length() > offset) {
                val available = native.outputFile.length() - offset
                if (available <= 0) {
                    delay(50)
                    continue
                }
                val bytes = ByteArray(minOf(available, 16L * 1024).toInt())
                val count = RandomAccessFile(native.outputFile, "r").use { file ->
                    file.seek(offset)
                    file.read(bytes)
                }
                if (count <= 0) continue
                offset += count
                pending.append(bytes.decodeToString(0, count))
                var newline = pending.indexOf("\n")
                while (newline >= 0) {
                    val line = pending.substring(0, newline).trimEnd('\r')
                    pending.delete(0, newline + 1)
                    handleLine(line)
                    newline = pending.indexOf("\n")
                }
            }
            pending.toString().trim().takeIf(String::isNotEmpty)?.let { handleLine(it) }
            emitCleanText(textFilter.flush())
            val exit = process.waitFor()
            awaitWorkspaceSettled(ws)
            val paths = checkpoints.changedFiles(ws, snapshotBefore)
            // agy sometimes tears down its network stream right after finishing real work, before
            // it manages to print the final Result JSON line. If the process still exited cleanly
            // and files were actually written, trust that evidence over the missing Result event
            // instead of reporting a false "Task stopped" for a task that truly completed.
            check(exit == 0 && (resultSeen || paths.isNotEmpty())) {
                friendlyError(pending.toString().takeLast(1_000).ifBlank { "Antigravity exited with code $exit" })
            }
            checkpoints.saveChangedPaths(projectId, paths)
            if (paths.isNotEmpty()) {
                eventBus.emit(RuntimeEvent.FilesChanged(sessionId, checkpoints.buildChangeDetails(projectId, ws, paths)))
            }
            emitCompleted(sessionId)
            finishForegroundRuntime(true, projectName, "Antigravity CLI finished working in $projectName.")
        }.onFailure {
            val message = if (userStopRequested) "Stopped by user" else friendlyError(it.message.orEmpty())
            val ws = workspace
            val snapshotBefore = before
            if (ws != null && snapshotBefore != null) {
                runCatching {
                    val paths = checkpoints.changedFiles(ws, snapshotBefore)
                    checkpoints.saveChangedPaths(projectId, paths)
                    if (paths.isNotEmpty()) {
                        eventBus.emit(RuntimeEvent.FilesChanged(sessionId, checkpoints.buildChangeDetails(projectId, ws, paths)))
                    }
                }
            }
            emitFailure(sessionId, message)
            if (userStopRequested) cancelForegroundRuntime()
            else finishForegroundRuntime(false, projectName, message)
        }
        activeProcess = null
        activeSessionId = null
        RuntimeTaskController.stopAction = null
        sessionId
    }

    override suspend fun respondToApproval(request: ToolRequest, approved: Boolean) {
        // agy headless streaming rejects control_response messages. This driver is
        // intentionally launched with --dangerously-skip-permissions by explicit
        // product choice, so no Antigravity approval can be pending here.
    }

    override suspend fun respondToQuestion(request: QuestionRequest, answer: String?, skipped: Boolean) {
        val userResponse = if (skipped) {
            "User skipped, proceed with your best judgment."
        } else {
            answer?.trim().takeUnless { it.isNullOrBlank() } ?: "User provided no additional details."
        }
        eventBus.emit(RuntimeEvent.QuestionAnswered(request.sessionId, request.questionId, userResponse, skipped))
        activeProcess?.let { proc ->
            runCatching {
                proc.outputStream.write((userResponse + "\n").toByteArray())
                proc.outputStream.flush()
            }
        }
    }

    override suspend fun stopSession(sessionId: String) {
        if (activeSessionId == sessionId) {
            userStopRequested = true
            activeProcess?.destroy()
            emitFailure(sessionId, "Stopped by user")
        }
    }

    override suspend fun stopActiveSession() = activeSessionId?.let { stopSession(it) } ?: Unit

    override suspend fun undoLastChanges(projectId: String): Boolean = withContext(Dispatchers.IO) {
        val checkpoint = checkpoints.checkpointDir(projectId)
        val backup = File(checkpoint, "project")
        val paths = checkpoints.readChangedPaths(projectId)
        if (!backup.isDirectory || paths.isEmpty()) return@withContext false
        val workspace = checkpoints.ensureWorkspace(projectId)
        paths.forEach { restore(workspace, backup, it) }
        checkpoint.deleteRecursively()
        true
    }

    override suspend fun acceptLastChanges(projectId: String): Unit = withContext(Dispatchers.IO) {
        checkpoints.checkpointDir(projectId).deleteRecursively()
        Unit
    }

    override suspend fun loadPendingChanges(projectId: String): List<ChangeItem> = withContext(Dispatchers.IO) {
        val paths = checkpoints.readChangedPaths(projectId)
        if (paths.isEmpty()) emptyList() else checkpoints.buildChangeDetails(projectId, checkpoints.ensureWorkspace(projectId), paths)
    }

    override suspend fun undoFileChange(projectId: String, path: String): Boolean = withContext(Dispatchers.IO) {
        if (path !in checkpoints.readChangedPaths(projectId)) return@withContext false
        restore(checkpoints.ensureWorkspace(projectId), File(checkpoints.checkpointDir(projectId), "project"), path)
        checkpoints.removeChangedPath(projectId, path)
        true
    }

    override suspend fun acceptFileChange(projectId: String, path: String): Boolean = withContext(Dispatchers.IO) {
        if (path !in checkpoints.readChangedPaths(projectId)) return@withContext false
        val workspace = checkpoints.ensureWorkspace(projectId)
        val backup = File(checkpoints.checkpointDir(projectId), "project")
        val current = checkpoints.safeWorkspaceFile(workspace, path)
        val baseline = checkpoints.safeWorkspaceFile(backup, path)
        if (current.isFile) {
            baseline.parentFile?.mkdirs()
            current.copyTo(baseline, overwrite = true)
        } else baseline.delete()
        checkpoints.removeChangedPath(projectId, path)
        true
    }

    private fun restore(workspace: File, backup: File, path: String) {
        val target = checkpoints.safeWorkspaceFile(workspace, path)
        val original = checkpoints.safeWorkspaceFile(backup, path)
        if (original.isFile) {
            target.parentFile?.mkdirs()
            original.copyTo(target, overwrite = true)
        } else target.delete()
    }

    private suspend fun emitCompleted(sessionId: String) {
        if (finished.add(sessionId)) eventBus.emit(RuntimeEvent.SessionCompleted(sessionId))
    }

    private suspend fun emitFailure(sessionId: String, reason: String) {
        if (finished.add(sessionId)) eventBus.emit(RuntimeEvent.SessionFailed(sessionId, reason))
    }

    private fun startForegroundRuntime(projectName: String) {
        ContextCompat.startForegroundService(
            context,
            android.content.Intent(context, RuntimeExecutionService::class.java)
                .setAction(RuntimeExecutionService.ACTION_START)
                .putExtra(RuntimeExecutionService.EXTRA_PROJECT_NAME, projectName)
                .putExtra(RuntimeExecutionService.EXTRA_TITLE, "Antigravity CLI is working")
                .putExtra(RuntimeExecutionService.EXTRA_DETAIL, "Antigravity CLI is working in $projectName"),
        )
    }

    private fun finishForegroundRuntime(completed: Boolean, projectName: String, detail: String) {
        if (foregroundResultPosted) return
        foregroundResultPosted = true
        runCatching {
            context.startService(
                android.content.Intent(context, RuntimeExecutionService::class.java)
                    .setAction(if (completed) RuntimeExecutionService.ACTION_COMPLETE else RuntimeExecutionService.ACTION_FAILED)
                    .putExtra(RuntimeExecutionService.EXTRA_PROJECT_NAME, projectName)
                    .putExtra(RuntimeExecutionService.EXTRA_TITLE, "Antigravity CLI")
                    .putExtra(RuntimeExecutionService.EXTRA_DETAIL, detail),
            )
        }.onFailure { context.stopService(android.content.Intent(context, RuntimeExecutionService::class.java)) }
    }

    private fun cancelForegroundRuntime() {
        if (foregroundResultPosted) return
        foregroundResultPosted = true
        runCatching {
            context.startService(
                android.content.Intent(context, RuntimeExecutionService::class.java)
                    .setAction(RuntimeExecutionService.ACTION_CANCELLED),
            )
        }.onFailure { context.stopService(android.content.Intent(context, RuntimeExecutionService::class.java)) }
    }

    private fun friendlyError(raw: String): String {
        val value = raw.replace(Regex("\\s+"), " ").trim()
        return when {
            value.contains("authentication required", true) ||
                value.contains("authentication failed", true) ||
                value.contains("not signed in", true) ->
                "Antigravity needs Google sign-in. Open Settings → Coding agent."
            value.contains("out of credits", true) || value.contains("quota", true) ->
                "Your Antigravity account is out of credits. Check the account plan or wait for credits to reset."
            value.contains("timed out", true) || value.contains("timeout", true) ->
                "Antigravity reached the 60-minute task limit. Your files were kept."
            value.contains("model", true) && (value.contains("invalid", true) || value.contains("unknown", true)) ->
                "The selected Antigravity model is unavailable. Refresh models in Settings."
            value.isBlank() -> "Antigravity could not complete the task."
            else -> value.take(500)
        }
    }
}

private class AntigravitySessionException(message: String) : IllegalStateException(message)

internal fun antigravityCommand(model: String, effort: String, conversationId: String?): List<String> = buildList {
    add(RuntimeInstaller.AGY_GUEST_PATH)
    addAll(listOf("--input-format", "stream-json"))
    addAll(listOf("--output-format", "stream-json"))
    addAll(listOf("--print-timeout", "60m"))
    // This is intentionally explicit and covered by tests. Antigravity tool calls
    // do not pass through PocketDev approval dialogs while this mode is enabled.
    add("--dangerously-skip-permissions")
    addAntigravitySelection(model, effort)
    conversationId?.takeIf(String::isNotBlank)?.let {
        addAll(listOf("--conversation", it))
    } ?: add("--new-project")
}

/**
 * `agy models` returns complete configuration IDs such as
 * `gemini-3.8-flash-medium`. Supplying a second, different --effort makes the
 * official CLI reject an otherwise valid model as conflicting. An explicit
 * model ID therefore owns its effort; --effort is used only with agy's default
 * model selection.
 */
private fun MutableList<String>.addAntigravitySelection(model: String, effort: String) {
    if (model.isNotBlank()) {
        addAll(listOf("--model", model))
    } else if (effort in setOf("low", "medium", "high")) {
        addAll(listOf("--effort", effort))
    }
}

internal fun antigravityWorkspacePrompt(projectSlug: String, prompt: String, pastMistakesSection: String): String = """
    <pocketdev_workspace>
    The active project workspace is /workspace/$projectSlug. Create, edit, read, run, and build project files only inside this directory. Do not create project output under ~/.gemini/antigravity-cli/scratch or any other scratch directory.
    </pocketdev_workspace>

    CRITICAL REQUIREMENT: Strictly avoid all emoji characters anywhere in your responses or generated text. Do NOT use any emojis under any circumstances.

    $AGENT_TASK_RULES

    The FINAL summary, written once when all work is done, keeps the fuller form: clear, specific sentences of measured length that briefly connect what was needed to what was done, rephrased naturally each time; never use the literal phrase "you asked to".
    Never include code, code blocks, or diffs inside this narration text — code belongs only in the tool-call/file-change cards, never in plain prose.
    In the FINAL summary only, when listing multiple distinct items use a numbered list (1., 2., 3. ...) instead of bullet points, and bold the key word or file name at the start of each item.
    Do not list the changed file paths as text in your narration or final summary — the app already shows a separate "Files changed" card with that information automatically, so repeating it in prose is redundant. Just describe in plain language what you changed and why.

    QUESTION AND TASK PRIORITIZATION:
    When parsing the user's message, identify every distinct question or instruction.
    Direct status or verification questions about existing work (e.g. "is this done?", "any issues left?", "did that bug get fixed?") always take priority over a new feature request or new task in the same message.
    Status questions must be answered before new work begins, regardless of where in the message they appear.
    If multiple questions exist, answer each one explicitly and separately in the response.
    Only after all questions in the message have been fully and directly answered should you proceed to any new task included in the same message.

    FILE EDITING STRATEGY (MANDATORY, NOT OPTIONAL):
    When modifying an existing file, first read only the relevant section with view_file, then change only that specific section using replace_file_content or multi_replace_file_content.
    Do not use write_to_file on a file that already exists unless the user explicitly asks for a full rewrite of that file. write_to_file is only for creating brand-new files.
    Rewriting an entire existing file from memory risks silently dropping or corrupting unrelated code elsewhere in the file and introduces avoidable syntax errors. A precise, minimal edit is always safer and faster than a full-file rewrite.

    ASKING CLARIFYING QUESTIONS (ALLOWED AND ENCOURAGED):
    If the user's request is genuinely ambiguous, missing a decision only they can make (e.g. which of two designs, which library, which behavior when requirements conflict), or could be done multiple valid ways with very different results, call the ask_question tool with a short question and 2-4 concrete options instead of silently guessing.
    Do not overuse this — only ask when a wrong guess would mean real rework, not for things you can reasonably infer or where any reasonable choice is fine.
    The user's answer will be sent back to you as the next message in this same task; continue directly from it without restarting your work.

    $AGENT_LESSON_REPORTING_INSTRUCTION

    ${if (pastMistakesSection.isNotBlank()) "$pastMistakesSection\n" else ""}
    $prompt
""".trimIndent()


/**
 * Removes internal machine output the CLI can echo into assistant text: <SYSTEM_MESSAGE>...</SYSTEM_MESSAGE>
 * blocks and "[Task <id>/<name> Output|Finished]" markers. Stateful so tags split across streamed
 * deltas are still caught. Ordinary prose (including the words "exit code") is left untouched.
 */
internal class AntigravityOutputFilter {
    private var inBlock = false
    private val buf = StringBuilder()

    fun feed(chunk: String): String {
        buf.append(chunk)
        return drain(final = false)
    }

    fun flush(): String = drain(final = true)

    private fun drain(final: Boolean): String {
        val out = StringBuilder()
        while (true) {
            if (inBlock) {
                val end = buf.indexOf(CLOSE)
                if (end < 0) {
                    if (final) buf.setLength(0)
                    else if (buf.length > CLOSE.length - 1) buf.delete(0, buf.length - (CLOSE.length - 1))
                    break
                }
                buf.delete(0, end + CLOSE.length)
                inBlock = false
                continue
            }
            val start = buf.indexOf(OPEN)
            if (start >= 0) {
                out.append(buf, 0, start)
                buf.delete(0, start + OPEN.length)
                inBlock = true
                continue
            }
            val hold = if (final) 0 else heldSuffixLength()
            out.append(buf, 0, buf.length - hold)
            buf.delete(0, buf.length - hold)
            break
        }
        return TASK_MARKER.replace(out.toString(), "")
    }

    /** How many trailing chars might be the start of a tag/marker that the next chunk completes. */
    private fun heldSuffixLength(): Int {
        var hold = 0
        for (k in minOf(OPEN.length - 1, buf.length) downTo 1) {
            if (bufEndsWith(OPEN.substring(0, k))) { hold = k; break }
        }
        val task = buf.lastIndexOf("[Task ")
        if (task >= 0 && buf.indexOf("]", task) < 0 && buf.length - task < 160) hold = maxOf(hold, buf.length - task)
        else for (k in minOf(5, buf.length) downTo 1) {
            if (bufEndsWith("[Task ".substring(0, k))) { hold = maxOf(hold, k); break }
        }
        return hold
    }

    private fun bufEndsWith(suffix: String): Boolean =
        buf.length >= suffix.length && buf.substring(buf.length - suffix.length) == suffix

    companion object {
        private const val OPEN = "<SYSTEM_MESSAGE>"
        private const val CLOSE = "</SYSTEM_MESSAGE>"
        private val TASK_MARKER = Regex("\\[Task [a-f0-9\\-]+/[\\w\\-]+ (Output|Finished)\\]")

        fun stripAll(text: String): String = AntigravityOutputFilter().let { it.feed(text) + it.flush() }
    }
}
