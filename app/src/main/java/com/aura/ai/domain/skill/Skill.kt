package com.aura.ai.domain.skill

/**
 * A Skill is an executable action handler. Implementations must be safe
 * and delegate to existing use cases via adapters.
 */
interface Skill {
    val skillId: String
    val version: String
    val riskLevel: String

    /** Execute the skill with the provided context. */
    suspend fun execute(context: SkillContext): SkillResult
}
