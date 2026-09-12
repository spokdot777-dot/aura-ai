package com.aura.ai.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OllamaTagsResponse(
    @Json(name = "models") val models: List<OllamaModel> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OllamaModel(
    @Json(name = "name") val name: String
)
