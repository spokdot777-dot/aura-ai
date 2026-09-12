package com.aura.ai

import com.aura.ai.network.RedactingLoggingInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Test

class OkHttpRedactionTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun logsDoNotContainApiKeyOrAuthorization() {
        val logger = TestHttpLogger()
        val interceptor = RedactingLoggingInterceptor(logger)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer "+"test-secret-key")
                    .build()
                chain.proceed(req)
            }
            // Add a response body that contains the API key to ensure body sanitizer works
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                // Return a response that includes the API key in body (simulate leak from server)
                val bodyWithKey = "server-data: ${'$'}{"test-secret-key"}"
                return@addInterceptor response.newBuilder().body(okhttp3.ResponseBody.create(response.body?.contentType(), bodyWithKey)).build()
            }
            .addInterceptor(interceptor)
            .build()

        server.enqueue(MockResponse().setBody("irrelevant"))

        val request = Request.Builder()
            .url(server.url("/test"))
            .get()
            .build()

        client.newCall(request).execute().use { resp ->
            // consume
            resp.body?.string()
        }

        val joined = logger.lines.joinToString("\n")

        // Ensure the API key is not present in logs
        assertFalse("API key leaked into logs", joined.contains("test-secret-key"))
        // Ensure Authorization header is not shown
        assertFalse("Authorization header leaked into logs", joined.contains("Authorization:"))
    }
}
