package com.aura.ai.data.repository

import com.aura.ai.data.database.dao.ConversationDao
import com.aura.ai.data.database.entity.ConversationEntity
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.data.remote.OllamaService
import com.aura.ai.data.remote.model.ChatRequest
import com.aura.ai.data.remote.model.MessageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val ollamaService: OllamaService
) {
    /** Stream of all stored messages, oldest first. */
    val messages: Flow<List<ChatMessage>> = conversationDao.getAllMessages().map { entities ->
        entities.map { it.toChatMessage() }
    }

    /** Persist a user or assistant message. */
    suspend fun saveMessage(message: ChatMessage): Long {
        return conversationDao.insert(
            ConversationEntity(
                role = message.role.name.lowercase(),
                content = message.content,
                timestamp = message.timestamp
            )
        )
    }

    /**
     * Send the conversation history to Ollama and return the assistant reply.
     * [systemPrompt] is prepended as the system message.
     * [history] is the list of prior messages to provide context.
     */
    suspend fun sendToOllama(
        systemPrompt: String,
        history: List<ChatMessage>
    ): String {
        val messages = mutableListOf(MessageDto(role = "system", content = systemPrompt))
        history.forEach { msg ->
            messages.add(
                MessageDto(
                    role = when (msg.role) {
                        ChatMessage.Role.USER -> "user"
                        ChatMessage.Role.ASSISTANT -> "assistant"
                        ChatMessage.Role.SYSTEM -> "system"
                    },
                    content = msg.content
                )
            )
        }
        val response = ollamaService.chatCompletion(
            ChatRequest(model = com.aura.ai.BuildConfig.OLLAMA_MODEL, messages = messages)
        )
        return response.choices.firstOrNull()?.message?.content
            ?: error("Ollama returned an empty response")
    }

    suspend fun clearHistory() = conversationDao.clearAll()

    private fun ConversationEntity.toChatMessage() = ChatMessage(
        id = id,
        role = when (role.uppercase()) {
            "USER" -> ChatMessage.Role.USER
            "ASSISTANT" -> ChatMessage.Role.ASSISTANT
            else -> ChatMessage.Role.SYSTEM
        },
        content = content,
        timestamp = timestamp
    )
}
