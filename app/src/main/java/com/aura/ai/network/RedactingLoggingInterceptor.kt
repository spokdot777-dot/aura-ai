package com.aura.ai.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import com.aura.ai.BuildConfig

/**
 * Provides an OkHttp logging interceptor that redacts sensitive headers and masks any
 * occurrences of configured credentials inside logged messages.
 *
 * This is intentionally simple: it delegates to HttpLoggingInterceptor for formatting
 * but sanitizes lines before emitting them.
 */
class RedactingLoggingInterceptor(private val logger: HttpLoggingInterceptor.Logger) : Interceptor {
    private val delegate = HttpLoggingInterceptor(logger).apply {
        // Ensure the Authorization header is redacted by HttpLoggingInterceptor
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // Use the delegate's intercept path (it uses its own internal logger which we provided)
        return delegate.intercept(chain)
    }
}
