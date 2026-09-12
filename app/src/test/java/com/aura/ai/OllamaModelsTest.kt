package com.aura.ai

import com.aura.ai.data.remote.model.OllamaTagsResponse
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OllamaModelsTest {
    private val moshi = Moshi.Builder().build()

    @Test
    fun parsesAvailableModels() {
        val adapter = moshi.adapter(OllamaTagsResponse::class.java)
        val response = adapter.fromJson("""{"models":[{"name":"gemma3:1b-cloud"}]}""")

        assertEquals("gemma3:1b-cloud", response?.models?.single()?.name)
    }

    @Test
    fun missingModelsIsAnEmptyList() {
        val adapter = moshi.adapter(OllamaTagsResponse::class.java)
        val response = adapter.fromJson("{}")

        assertTrue(response?.models?.isEmpty() == true)
    }
}
