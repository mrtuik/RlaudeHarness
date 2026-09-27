package com.jarves.mh.model

import org.json.JSONArray
import org.json.JSONObject

enum class PipelineStage {
    IDLE,
    ARCHITECT,
    ANALYSIS,
    BREAKDOWN,
    AWAITING_APPROVAL,
    IMPLEMENTING,
    DONE,
}

enum class TaskStatus { QUEUED, ACTIVE, DONE }

data class PipelineTask(
    val id: String,
    val title: String,
    val files: List<String> = emptyList(),
    val description: String = "",
    val subagent: String = "",
    val status: TaskStatus = TaskStatus.QUEUED,
)

data class PipelinePlan(
    val architectPlan: String = "",
    val analysis: String = "",
    val tasks: List<PipelineTask> = emptyList(),
    val revisionCount: Int = 0,
    val rejectionReason: String? = null,
)

/** Display names used so the implementation phase reads like an office of subagents. */
val SUBAGENT_NAMES = listOf("Tuik", "Harmes", "Rthan")

fun assignSubagents(tasks: List<PipelineTask>): List<PipelineTask> =
    tasks.mapIndexed { index, task -> task.copy(subagent = SUBAGENT_NAMES[index % SUBAGENT_NAMES.size]) }

/**
 * Pulls a JSON array of tasks out of raw LLM text (which may include prose or a ```json fence
 * around the array). Falls back to a single manual-review task if nothing parseable is found,
 * so the pipeline never silently produces zero tasks.
 */
fun parsePipelineTasks(raw: String): List<PipelineTask> {
    val start = raw.indexOf('[')
    val end = raw.lastIndexOf(']')
    if (start == -1 || end == -1 || end < start) return fallbackTask(raw)
    return runCatching {
        val array = JSONArray(raw.substring(start, end + 1))
        (0 until array.length()).map { index ->
            val obj: JSONObject = array.getJSONObject(index)
            val files = obj.optJSONArray("files")
            PipelineTask(
                id = obj.optString("id", (index + 1).toString()),
                title = obj.optString("title", "Task ${index + 1}"),
                files = files?.let { arr -> (0 until arr.length()).map { arr.getString(it) } } ?: emptyList(),
                description = obj.optString("description", ""),
            )
        }
    }.getOrElse { fallbackTask(raw) }
}

private fun fallbackTask(raw: String): List<PipelineTask> = listOf(
    PipelineTask(id = "1", title = "Review plan manually", description = raw.take(500)),
)
