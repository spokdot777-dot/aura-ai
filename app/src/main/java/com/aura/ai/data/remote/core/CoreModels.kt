package com.aura.ai.data.remote.core

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoreMasterResponse(
    val apiVersion: String,
    val subject: String,
    val modules: List<CoreModule>,
    val tasks: List<CoreTask> = emptyList(),
    val approvals: List<CoreApproval> = emptyList(),
    val safeMode: Boolean,
    val capabilities: List<String> = emptyList(),
    val boundaries: CoreBoundaries? = null
)

@JsonClass(generateAdapter = true)
data class CoreModule(
    val id: String,
    val name: String,
    val category: String,
    val version: String,
    val apiBase: String? = null,
    val description: String? = null,
    val capabilities: List<String> = emptyList(),
    val status: String,
    val health: String,
    val detail: String? = null
)

@JsonClass(generateAdapter = true)
data class CoreTask(
    val id: String? = null,
    val status: String? = null,
    val name: String? = null,
    val detail: String? = null
)

@JsonClass(generateAdapter = true)
data class CoreApproval(
    val id: String? = null,
    val status: String? = null,
    val action: String? = null,
    val detail: String? = null
)

@JsonClass(generateAdapter = true)
data class CoreBoundaries(
    val android: String? = null,
    val filesystem: String? = null,
    val providers: String? = null
)

@JsonClass(generateAdapter = true)
data class SafeModeRequest(val action: String = "safe_mode", val enabled: Boolean)

@JsonClass(generateAdapter = true)
data class SafeModeResponse(val safeMode: Boolean)

@JsonClass(generateAdapter = true)
data class CreateTaskRequest(
    val user: String,
    val project: String,
    val request: String,
    val priority: String? = null,
    val metadata: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class TaskStepRequest(val stepId: String? = null)

@JsonClass(generateAdapter = true)
data class CoreTaskPlan(
    val id: String? = null,
    val taskId: String? = null,
    val status: String? = null,
    val steps: List<CoreTaskStep> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CoreTaskStep(
    val id: String? = null,
    val taskId: String? = null,
    val status: String? = null,
    val name: String? = null,
    val detail: String? = null,
    val approvalRequired: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class CoreTaskEvent(
    val id: String? = null,
    val taskId: String? = null,
    val type: String? = null,
    val status: String? = null,
    val detail: String? = null,
    val createdAt: String? = null
)

enum class CoreModuleStatus {
    HEALTHY, UNCONFIGURED, UNAVAILABLE, DISCONNECTED, AUTHENTICATION_REQUIRED, ERROR, UNKNOWN;

    companion object {
        fun from(raw: String?): CoreModuleStatus = when (raw?.lowercase()) {
            "healthy" -> HEALTHY
            "unconfigured" -> UNCONFIGURED
            "unavailable" -> UNAVAILABLE
            "disconnected" -> DISCONNECTED
            "authentication-required", "authentication_required" -> AUTHENTICATION_REQUIRED
            "error" -> ERROR
            else -> UNKNOWN
        }
    }
}

sealed interface CoreResult<out T> {
    data class Success<T>(val value: T) : CoreResult<T>
    data object Unauthenticated : CoreResult<Nothing>
    data object Forbidden : CoreResult<Nothing>
    data object NetworkUnavailable : CoreResult<Nothing>
    data object Timeout : CoreResult<Nothing>
    data class ServerError(val code: Int, val message: String? = null) : CoreResult<Nothing>
    data class Unexpected(val cause: Throwable) : CoreResult<Nothing>
}
