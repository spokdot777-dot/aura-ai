package com.aura.ai.domain.skill

/**
 * Result of a Skill execution.
 */
data class SkillResult(
    val success: Boolean,
    val message: String? = null,
    val data: Map<String, Any?> = emptyMap(),
    val latencyMs: Long? = null,
    val confidence: Double? = null
)
