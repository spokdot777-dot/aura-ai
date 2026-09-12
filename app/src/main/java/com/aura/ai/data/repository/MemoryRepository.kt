package com.aura.ai.data.repository

import com.aura.ai.data.database.dao.MemoryDao
import com.aura.ai.data.database.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao
) {
    /** All stored key-value memory facts as a stream. */
    val memories: Flow<Map<String, String>> = memoryDao.getAll().map { list ->
        list.associate { it.key to it.value }
    }

    suspend fun remember(key: String, value: String) {
        memoryDao.upsert(MemoryEntity(key = key, value = value))
    }

    suspend fun recall(key: String): String? = memoryDao.getByKey(key)?.value

    suspend fun forget(key: String) = memoryDao.deleteByKey(key)

    suspend fun clearAll() = memoryDao.clearAll()

    /** Build a bounded memory context string to inject into the assistant system prompt. */
    suspend fun buildMemoryContext(): String {
        val facts = memoryDao.getAllOnce()
        if (facts.isEmpty()) return ""
        return facts.joinToString(separator = "\n") { "- ${it.key}: ${it.value}" }
    }
}
