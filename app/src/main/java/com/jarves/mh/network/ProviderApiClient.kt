package com.jarves.mh.network

import android.util.Log
import com.jarves.mh.model.ProviderProtocol
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DiscoveredModel(val id: String, val displayName: String = id, val isFree: Boolean = false)

sealed interface ModelDiscoveryResult {
    data class Success(val models: List<DiscoveredModel>, val endpoint: String) : ModelDiscoveryResult
    data class Failure(val message: String, val providerMessage: String? = null) : ModelDiscoveryResult
}

sealed interface ConnectionValidation {
    data class Success(val message: String) : ConnectionValidation
    data class Failure(
        val message: String,
        val providerMessage: String? = null,
        val label: String = "Failed",
    ) : ConnectionValidation
}

class ProviderApiClient {
    suspend fun discoverModels(
        baseUrl: String,
        apiKey: String,
        protocol: ProviderProtocol,
    ): ModelDiscoveryResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext ModelDiscoveryResult.Failure("Enter a base URL first.")
        }

        var authError = false
        var lastMessage = "This provider did not expose a model list. You can enter a custom model name."
        var lastProviderMessage: String? = null
        for (endpoint in modelEndpoints(baseUrl, protocol)) {
            // OpenRouter's complete catalog is public. Fetch it anonymously even when
            // OpenRouter is configured through Custom API so an account-scoped key does
            // not reduce discovery to the models allowed by that key's preferences.
            // The saved key is still used for validation and all inference requests.
            val discoveryKey = if (isOpenRouterCatalogEndpoint(endpoint)) "" else apiKey
            val response = request(endpoint, "GET", discoveryKey, protocol = protocol)
            when {
                response.code == 401 || response.code == 403 -> {
                    authError = true
                    lastProviderMessage = providerErrorMessage(response.body)
                }
                response.code in 200..299 -> {
                    val models = ModelResponseParser.parse(response.body)
                    if (models.isNotEmpty()) return@withContext ModelDiscoveryResult.Success(models, endpoint)
                    lastMessage = "The provider replied, but its model list was empty or unsupported."
                }
                response.code > 0 && response.code != 404 -> {
                    lastMessage = friendlyHttpError(response.code)
                    lastProviderMessage = providerErrorMessage(response.body)
                }
                response.error != null -> lastMessage = response.error
            }
        }
        ModelDiscoveryResult.Failure(
            if (authError) "Check the saved API key, then try refreshing again." else lastMessage,
            lastProviderMessage,
        )
    }

    suspend fun validate(
        baseUrl: String,
        model: String,
        apiKey: String,
        protocol: ProviderProtocol,
        discoveredModels: List<DiscoveredModel>,
    ): ConnectionValidation = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank() || model.isBlank() || apiKey.isBlank()) {
            return@withContext ConnectionValidation.Failure("Base URL, model, and API key are required.")
        }
        val endpoint = messagesEndpoint(baseUrl, protocol)
        val body = validationBody(model, protocol)
        // Gateways may need to cold-start a model before returning the first token.
        // A ten-second validation timeout produced false "network" failures even
        // though discovery and the endpoint itself were healthy.
        val response = request(endpoint, "POST", apiKey, body, protocol, connectTimeoutMs = 12_000, readTimeoutMs = 45_000)
        when {
            response.code in 200..299 -> ConnectionValidation.Success(
                if (protocol == ProviderProtocol.ANTHROPIC || protocol == ProviderProtocol.ANTHROPIC_GATEWAY || protocol == ProviderProtocol.OPENROUTER) {
                    "Anthropic Messages endpoint verified. Claude Code settings are ready."
                } else {
                    "Connection successful. Claude Code settings are ready."
                },
            )
            response.code == 401 || response.code == 403 -> ConnectionValidation.Failure(
                "Check this API key or select another saved key.",
                providerErrorMessage(response.body),
                "Rejected",
            )
            response.code == 404 -> ConnectionValidation.Failure(
                "Check the Base URL and selected gateway protocol.",
                providerErrorMessage(response.body),
                "Endpoint error",
            )
            response.code == 400 && response.body.contains("model", ignoreCase = true) ->
                ConnectionValidation.Failure(
                    "Refresh the model list or select a different model.",
                    providerErrorMessage(response.body),
                    "Model error",
                )
            response.code == 429 -> ConnectionValidation.Failure(
                "Wait a moment, then retry or use another API key.",
                providerErrorMessage(response.body),
                "Rate limited",
            )
            response.code in 500..599 -> ConnectionValidation.Failure(
                "The provider is temporarily unavailable. Try again shortly.",
                providerErrorMessage(response.body),
                "Provider error",
            )
            response.code > 0 -> ConnectionValidation.Failure(
                "Review the model, protocol, and endpoint settings.",
                providerErrorMessage(response.body),
                "Request failed",
            )
            response.error?.contains("timeout", ignoreCase = true) == true ||
                response.error?.contains("timed out", ignoreCase = true) == true ->
                ConnectionValidation.Failure("Check your connection and try again.", response.error, "Timed out")
            else -> ConnectionValidation.Failure(
                "Check your internet connection and provider settings.",
                response.error,
                "Network error",
            )
        }
    }

    private fun request(
        endpoint: String,
        method: String,
        apiKey: String,
        body: String? = null,
        protocol: ProviderProtocol,
        connectTimeoutMs: Int = 12_000,
        readTimeoutMs: Int = 20_000,
    ): HttpResult {
        return runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                if (apiKey.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $apiKey")
                }
                if (endpoint.startsWith("https://opencode.ai/zen/")) {
                    // OpenCode Zen expects requests to identify the OpenCode client and session.
                    setRequestProperty("User-Agent", "opencode/1.18.20")
                    setRequestProperty("x-session-id", "session-${UUID.randomUUID()}")
                }
                if (apiKey.isNotBlank() && protocol != ProviderProtocol.OPENROUTER && protocol != ProviderProtocol.OPENAI_CHAT && protocol != ProviderProtocol.OPENAI_RESPONSES) {
                    setRequestProperty("x-api-key", apiKey)
                    setRequestProperty("anthropic-version", "2023-06-01")
                }
                if (body != null) doOutput = true
            }
            if (body != null) connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            connection.disconnect()
            HttpResult(code, responseBody)
        }.getOrElse { HttpResult(0, "", it.message ?: "Network connection failed",) }
    }

    private fun modelEndpoints(baseUrl: String, protocol: ProviderProtocol): List<String> {
        val base = baseUrl.trim().trimEnd('/')
        val withoutAnthropic = base.removeSuffix("/anthropic")
        val candidates = when (protocol) {
            ProviderProtocol.OPENROUTER -> listOf("$base/v1/models")
            ProviderProtocol.OPENAI_CHAT, ProviderProtocol.OPENAI_RESPONSES -> listOf("$base/models")
            else -> listOf("$base/v1/models", "$base/models", "$withoutAnthropic/models", "$withoutAnthropic/v1/models")
        }
        return candidates.distinct()
    }

    private fun messagesEndpoint(baseUrl: String, protocol: ProviderProtocol): String {
        val base = baseUrl.trim().trimEnd('/')
        return when (protocol) {
            ProviderProtocol.OPENROUTER -> "$base/v1/messages"
            ProviderProtocol.OPENAI_CHAT -> "$base/chat/completions"
            ProviderProtocol.OPENAI_RESPONSES -> "$base/responses"
            else -> if (base.endsWith("/v1")) "$base/messages" else "$base/v1/messages"
        }
    }

    private fun isOpenRouterCatalogEndpoint(endpoint: String): Boolean = runCatching {
        val url = URL(endpoint)
        url.host.equals("openrouter.ai", ignoreCase = true) &&
            url.path.trimEnd('/').endsWith("/models")
    }.getOrDefault(false)

    internal fun validationBody(model: String, protocol: ProviderProtocol): String = when (protocol) {
        ProviderProtocol.OPENAI_RESPONSES -> JSONObject()
            .put("model", model)
            .put(
                "input",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "content",
                            JSONArray().put(
                                JSONObject()
                                    .put("type", "input_text")
                                    .put("text", "Hello, reply with 1 word."),
                            ),
                        ),
                ),
            )
            .toString()
        ProviderProtocol.OPENAI_CHAT -> JSONObject()
            .put("model", model)
            .put("max_tokens", 1)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", "Reply OK")))
            .toString()
        else -> JSONObject()
            .put("model", model)
            .put("max_tokens", 1)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", "Reply OK")))
            .toString()
    }

    private fun friendlyHttpError(code: Int): String = when (code) {
        429 -> "The provider rate limit was reached. Wait a moment and try again."
        in 500..599 -> "The provider is temporarily unavailable (HTTP $code)."
        else -> "The provider returned HTTP $code. Check the URL and account access."
    }

    private fun providerErrorMessage(body: String): String? {
        if (body.isBlank()) return null
        val extracted = runCatching {
            val root = JSONObject(body)
            when (val error = root.opt("error")) {
                is JSONObject -> error.optString("message").ifBlank { error.optString("detail") }
                is String -> error
                else -> root.optString("message").ifBlank { root.optString("detail") }
            }
        }.getOrNull().orEmpty()
        if (extracted.isBlank()) return null
        return extracted
            .replace(Regex("(?i)bearer\\s+\\S+"), "Bearer ••••")
            .replace(Regex("(?i)sk-[a-z0-9_-]{8,}"), "sk-••••")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(280)
    }

    private data class HttpResult(val code: Int, val body: String, val error: String? = null)
}

object ModelResponseParser {
    fun parse(json: String): List<DiscoveredModel> = runCatching {
        val trimmed = json.trim()
        val array = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> {
                val root = JSONObject(trimmed)
                root.optJSONArray("data") ?: root.optJSONArray("models") ?: JSONArray()
            }
        }
        buildList {
            for (index in 0 until array.length()) {
                when (val item = array.opt(index)) {
                    is String -> add(DiscoveredModel(item))
                    is JSONObject -> {
                        val id = item.optString("id").ifBlank { item.optString("name") }
                        if (id.isNotBlank()) {
                            val label = item.optString("display_name").ifBlank { item.optString("displayName") }.ifBlank { id }
                            val pricing = item.optJSONObject("pricing")
                            val free = id.endsWith(":free", ignoreCase = true) || pricing?.let {
                                listOf("prompt", "completion", "request").all { field ->
                                    it.optString(field, "0").toDoubleOrNull() == 0.0
                                }
                            } == true
                            add(DiscoveredModel(id, label, free))
                        }
                    }
                }
            }
        }.distinctBy { it.id }.sortedWith(compareByDescending<DiscoveredModel> { it.isFree }.thenBy { it.displayName.lowercase() })
    }.getOrDefault(emptyList())
}

data class GitHubDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Long,
    val intervalSeconds: Long,
)

data class GitHubAccount(val login: String, val avatarUrl: String)

data class GitHubRepository(
    val fullName: String,
    val cloneUrl: String,
    val private: Boolean,
    val defaultBranch: String,
    val description: String,
    val updatedAt: String,
)

sealed interface GitHubTokenPoll {
    data class Success(val accessToken: String) : GitHubTokenPoll
    data class Pending(val slowDown: Boolean = false) : GitHubTokenPoll
    data class Failure(val message: String) : GitHubTokenPoll
}

class GitHubClient {
    fun requestDeviceCode(clientId: String): GitHubDeviceCode {
        val json = postForm(
            "https://github.com/login/device/code",
            mapOf("client_id" to clientId),
        )
        return GitHubDeviceCode(
            deviceCode = json.getString("device_code"),
            userCode = json.getString("user_code"),
            verificationUri = json.optString("verification_uri", "https://github.com/login/device"),
            expiresInSeconds = json.optLong("expires_in", 900L),
            intervalSeconds = json.optLong("interval", 5L).coerceAtLeast(5L),
        )
    }

    fun pollDeviceToken(clientId: String, deviceCode: String): GitHubTokenPoll {
        val json = postForm(
            "https://github.com/login/oauth/access_token",
            mapOf(
                "client_id" to clientId,
                "device_code" to deviceCode,
                "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
            ),
        )
        json.optString("access_token").takeIf(String::isNotBlank)?.let { return GitHubTokenPoll.Success(it) }
        return when (val error = json.optString("error")) {
            "authorization_pending" -> GitHubTokenPoll.Pending()
            "slow_down" -> GitHubTokenPoll.Pending(slowDown = true)
            "access_denied" -> GitHubTokenPoll.Failure("GitHub authorization was cancelled")
            "expired_token" -> GitHubTokenPoll.Failure("The GitHub sign-in code expired. Try again.")
            else -> GitHubTokenPoll.Failure(json.optString("error_description").ifBlank { error.ifBlank { "GitHub sign-in failed" } })
        }
    }

    fun account(token: String): GitHubAccount {
        val json = getJson("https://api.github.com/user", token) as JSONObject
        return GitHubAccount(json.getString("login"), json.optString("avatar_url"))
    }

    fun repositories(token: String): List<GitHubRepository> {
        val result = LinkedHashMap<String, GitHubRepository>()
        var page = 1
        while (page <= 10) {
            val array = getJson(
                "https://api.github.com/user/repos?visibility=all&affiliation=owner,collaborator,organization_member&sort=updated&per_page=100&page=$page",
                token,
            ) as JSONArray
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val repo = GitHubRepository(
                    fullName = item.getString("full_name"),
                    cloneUrl = item.getString("clone_url"),
                    private = item.optBoolean("private"),
                    defaultBranch = item.optString("default_branch", "main"),
                    description = item.optString("description"),
                    updatedAt = item.optString("updated_at"),
                )
                result[repo.fullName] = repo
            }
            if (array.length() < 100) break
            page++
        }
        return result.values.toList()
    }

    private fun postForm(endpoint: String, values: Map<String, String>): JSONObject {
        val body = values.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}" 
        }.toByteArray()
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", "PocketDev-Android")
        }
        connection.outputStream.use { it.write(body) }
        return readResponse(connection) as JSONObject
    }

    private fun getJson(endpoint: String, token: String): Any {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("X-GitHub-Api-Version", "2026-03-10")
            setRequestProperty("User-Agent", "PocketDev-Android")
        }
        return readResponse(connection)
    }

    private fun readResponse(connection: HttpURLConnection): Any {
        val status = connection.responseCode
        val text = (if (status in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (status !in 200..299) {
            val message = runCatching { JSONObject(text).optString("message") }.getOrNull()
            error(message?.takeIf(String::isNotBlank) ?: "GitHub returned HTTP $status")
        }
        return if (text.trimStart().startsWith("[")) JSONArray(text) else JSONObject(text)
    }
}

data class MistakeDetectionResult(
    val isCorrection: Boolean,
    val summary: String,
)

class GitHubLessonReporter {

    companion object {
        private const val TAG = "GitHubLessonReporter"
        private val DEFAULT_GROQ_KEY: String
            get() = com.jarves.mh.BuildConfig.GROQ_API_KEY
    }

    suspend fun detectMistake(
        previousAssistantText: String,
        userMessage: String,
        apiKey: String? = null,
    ): MistakeDetectionResult = withContext(Dispatchers.IO) {
        if (previousAssistantText.isBlank() || userMessage.isBlank()) {
            return@withContext MistakeDetectionResult(false, "")
        }

        val effectiveKey = apiKey?.takeIf(String::isNotBlank)
            ?: DEFAULT_GROQ_KEY.takeIf(String::isNotBlank)
            ?: ""
        if (effectiveKey.isBlank()) {
            return@withContext MistakeDetectionResult(false, "")
        }

        runCatching {
            val truncatedAssistant = previousAssistantText.take(1500)
            val truncatedUser = userMessage.take(1000)

            val prompt = """
                You are a supervisor evaluating whether a human user is correcting or complaining about an AI coding agent's previous response.
                
                Previous Assistant Response:
                \"\"\"$truncatedAssistant\"\"\"
                
                User Message:
                \"\"\"$truncatedUser\"\"\"
                
                Analyze if the user is pointing out a mistake, error, bug, failure to follow instructions, or problem with the assistant's previous response.
                Respond with ONLY a JSON object:
                {"is_correction": true, "summary": "Concise summary of the mistake"}
                or
                {"is_correction": false, "summary": ""}
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("model", "llama-3.1-8b-instant")
                put("temperature", 0.0)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are a JSON-only evaluator. Output valid JSON only, without any markdown formatting.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            val connection = (URL("https://api.groq.com/openai/v1/chat/completions").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 12_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $effectiveKey")
                setRequestProperty("User-Agent", "PocketDev-Android")
            }

            connection.outputStream.use { it.write(requestJson.toString().toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            if (status !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.w(TAG, "Groq mistake detection HTTP $status: $errorBody")
                return@withContext MistakeDetectionResult(false, "")
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val responseJson = JSONObject(responseBody)
            val choices = responseJson.optJSONArray("choices") ?: return@withContext MistakeDetectionResult(false, "")
            if (choices.length() == 0) return@withContext MistakeDetectionResult(false, "")
            val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content").orEmpty().trim()

            parseDetectionJson(content)
        }.getOrElse { error ->
            Log.w(TAG, "Mistake detection failed", error)
            MistakeDetectionResult(false, "")
        }
    }

    suspend fun reportCandidate(
        summary: String,
        userMessage: String,
        assistantContext: String,
        patToken: String,
        repo: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val targetRepo = repo.trim().ifBlank { "mrtuik/RlaudeHarnessAgent" }
            val endpoint = "https://api.github.com/repos/$targetRepo/issues"

            val issueTitle = "Candidate Lesson: ${summary.take(100)}"
            val issueBody = buildString {
                appendLine("### Problem / Mistake Summary")
                appendLine(summary)
                appendLine()
                appendLine("### User Correction")
                appendLine(userMessage)
                appendLine()
                appendLine("### Assistant Output Context")
                appendLine("```")
                appendLine(assistantContext.take(1500))
                appendLine("```")
                appendLine()
                appendLine("_Reported automatically by RlaudeHarness Agent Memory._")
            }

            fun postIssue(includeLabels: Boolean): Pair<Int, String> {
                val payload = JSONObject().apply {
                    put("title", issueTitle)
                    put("body", issueBody)
                    if (includeLabels) {
                        put("labels", JSONArray().apply { put("candidate") })
                    }
                }

                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 12_000
                    readTimeout = 20_000
                    doOutput = true
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("Authorization", "Bearer ${patToken.trim()}")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("User-Agent", "PocketDev-Android")
                }

                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                return code to responseText
            }

            var (status, body) = postIssue(includeLabels = true)
            if (status == 422 || status == 403) {
                // If fine-grained PAT or repo rejects the labels field, retry without labels
                val retry = postIssue(includeLabels = false)
                status = retry.first
                body = retry.second
            }

            if (status !in 200..299) {
                error("GitHub issue creation failed (HTTP $status): $body")
            }

            val resultJson = JSONObject(body)
            resultJson.optString("html_url", "https://github.com/$targetRepo/issues")
        }
    }

    suspend fun checkAndReportCandidate(
        previousAssistantText: String,
        userMessage: String,
        patToken: String?,
        repo: String,
        customGroqKey: String? = null,
    ) {
        if (patToken.isNullOrBlank()) {
            return
        }
        val detection = detectMistake(previousAssistantText, userMessage, customGroqKey)
        if (detection.isCorrection && detection.summary.isNotBlank()) {
            reportCandidate(
                summary = detection.summary,
                userMessage = userMessage,
                assistantContext = previousAssistantText,
                patToken = patToken,
                repo = repo,
            ).onSuccess { issueUrl ->
                Log.i(TAG, "Candidate lesson reported successfully to GitHub: $issueUrl")
            }.onFailure { error ->
                Log.w(TAG, "Failed to report candidate lesson to GitHub", error)
            }
        }
    }

    private fun parseDetectionJson(raw: String): MistakeDetectionResult {
        val cleaned = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val startIdx = cleaned.indexOf('{')
        val endIdx = cleaned.lastIndexOf('}')
        if (startIdx != -1 && endIdx > startIdx) {
            val jsonSubstring = cleaned.substring(startIdx, endIdx + 1)
            val json = JSONObject(jsonSubstring)
            val isCorrection = json.optBoolean("is_correction", false)
            val summary = json.optString("summary", "").trim()
            return MistakeDetectionResult(isCorrection, summary)
        }
        return MistakeDetectionResult(false, "")
    }
}

