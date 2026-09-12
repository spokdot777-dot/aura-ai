package com.aura.ai.domain.ai

import com.aura.ai.data.model.ChatMessage

sealed interface AiFailure {
    val message: String

    data class Unreachable(override val message: String) : AiFailure
    data class Timeout(override val message: String) : AiFailure
    data class Authentication(override val message: String) : AiFailure
    data class ModelUnavailable(override val message: String) : AiFailure
    data class MalformedResponse(override val message: String) : AiFailure
    data class Generation(override val message: String) : AiFailure
}

data class AiConnectionResult(
    val success: Boolean,
    val model: String,
    val failure: AiFailure? = null
)

interface AiProvider {
    suspend fun generate(systemPrompt: String, history: List<ChatMessage>): Result<String>
    suspend fun testConnection(): AiConnectionResult
}
