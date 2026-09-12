package com.aura.ai.data.repository

import com.aura.ai.data.database.dao.ConversationDao
import com.aura.ai.data.database.entity.ConversationEntity
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.domain.ai.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val aiProvider: AiProvider
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
        return aiProvider.generate(systemPrompt, history).getOrThrow()
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
