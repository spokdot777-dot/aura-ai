package com.aura.ai.data.remote

import com.aura.ai.BuildConfig

object OllamaConfiguration {
    const val connectionTimeoutSeconds = 15L
    const val readTimeoutSeconds = 90L
    const val writeTimeoutSeconds = 30L

    val baseUrl: String get() = BuildConfig.OLLAMA_BASE_URL.trimEnd('/') + "/"
    val model: String get() = BuildConfig.OLLAMA_MODEL
}
