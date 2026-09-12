package com.aura.ai.data.remote.core

import com.aura.ai.data.auth.CoreTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CoreAuthInterceptor @Inject constructor(
    private val tokenStore: CoreTokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply { tokenStore.read()?.let { addHeader("Authorization", "Bearer $it") } }
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
