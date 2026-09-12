package com.aura.ai.data.remote

import com.aura.ai.BuildConfig
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.data.remote.model.ChatRequest
import com.aura.ai.data.remote.model.MessageDto
import com.aura.ai.domain.ai.AiConnectionResult
import com.aura.ai.domain.ai.AiFailure
import com.aura.ai.domain.ai.AiProvider
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class OllamaAiProvider @Inject constructor(
    private val service: OllamaService
) : AiProvider {
    override suspend fun generate(systemPrompt: String, history: List<ChatMessage>): Result<String> = runCatching {
        val requestMessages = buildList {
            add(MessageDto("system", systemPrompt))
            addAll(history.map { MessageDto(it.role.name.lowercase(), it.content) })
        }
        val response = service.chatCompletion(
            ChatRequest(model = BuildConfig.OLLAMA_MODEL, messages = requestMessages)
        )
        response.choices.firstOrNull()?.message?.content?.takeIf { it.isNotBlank() }
            ?: error("Ollama returned an empty response")
    }.onFailure { if (it is CancellationException) throw it }

    override suspend fun testConnection(): AiConnectionResult {
        return try {
            val models = service.listModels().models
            if (models.none { it.name == BuildConfig.OLLAMA_MODEL || it.name.startsWith(BuildConfig.OLLAMA_MODEL) }) {
                AiConnectionResult(false, BuildConfig.OLLAMA_MODEL, AiFailure.ModelUnavailable("Model ${BuildConfig.OLLAMA_MODEL} is not available"))
            } else {
                val result = generate("Reply with exactly: connection-ok", emptyList())
                if (result.isSuccess) AiConnectionResult(true, BuildConfig.OLLAMA_MODEL)
                else AiConnectionResult(false, BuildConfig.OLLAMA_MODEL, result.exceptionOrNull().toFailure())
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            AiConnectionResult(false, BuildConfig.OLLAMA_MODEL, error.toFailure())
        }
    }

    private fun Throwable?.toFailure(): AiFailure = when (this) {
        is SocketTimeoutException -> AiFailure.Timeout("Ollama timed out")
        is HttpException -> when (code()) {
            401, 403 -> AiFailure.Authentication("Ollama authentication failed")
            404 -> AiFailure.ModelUnavailable("Ollama model or endpoint was not found")
            else -> AiFailure.Generation("Ollama returned HTTP ${code()}")
        }
        is IOException -> AiFailure.Unreachable("Ollama server is unreachable")
        else -> AiFailure.MalformedResponse("Ollama returned an invalid response")
    }
}
