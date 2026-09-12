package com.aura.ai.network

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * Interceptor that sanitizes plain-text request and response bodies before logging.
 */
class BodySanitizingInterceptor(
    private val sensitiveValues: Set<String> = emptySet()
) : Interceptor {
    private val redaction = "[REDACTED_SENSITIVE_VALUE]"

    private fun sanitize(text: String): String = sensitiveValues
        .filter { it.isNotEmpty() }
        .fold(text) { sanitized, sensitiveValue ->
            sanitized.replace(sensitiveValue, redaction)
        }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val sanitizedRequest = sanitizeRequestBody(originalRequest)

        val response = chain.proceed(sanitizedRequest)
        return sanitizeResponseBody(response)
    }

    private fun sanitizeRequestBody(request: Request): Request {
        val body = request.body ?: return request
        try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val originalBody = buffer.readUtf8()
            val bodyString = sanitize(originalBody)
            if (bodyString != originalBody) {
                val mediaType: MediaType? = body.contentType()
                val newBody = bodyString.toRequestBody(mediaType)
                // Build new request with sanitized body
                return request.newBuilder().method(request.method, newBody).build()
            }
        } catch (e: Exception) {
            // On error, return original request; do not fail the network call due to logging concerns
            return request
        }
        return request
    }

    private fun sanitizeResponseBody(response: Response): Response {
        val body = response.body ?: return response
        try {
            val source = body.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer.clone()
            val originalBody = buffer.readUtf8()
            val bodyString = sanitize(originalBody)
            if (bodyString != originalBody) {
                val contentType = body.contentType()
                val newBody = bodyString.toResponseBody(contentType)
                return response.newBuilder().body(newBody).build()
            }
        } catch (e: Exception) {
            return response
        }
        return response
    }
}
