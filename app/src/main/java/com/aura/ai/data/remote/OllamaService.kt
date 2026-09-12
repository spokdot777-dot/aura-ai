package com.aura.ai.data.remote

import com.aura.ai.data.remote.model.ChatRequest
import com.aura.ai.data.remote.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: ChatRequest): ChatResponse
}
