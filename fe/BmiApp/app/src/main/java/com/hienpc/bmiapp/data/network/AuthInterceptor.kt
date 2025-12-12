package com.hienpc.bmiapp.data.network

import com.hienpc.bmiapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor - tự động gắn header Authorization: Bearer <token>
 * cho mọi request nếu TokenManager đang có token.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val currentToken = TokenManager.token
        if (currentToken.isNullOrBlank()) {
            // Không có token → gửi request như bình thường
            return chain.proceed(original)
        }

        val newRequest = original.newBuilder()
            .addHeader("Authorization", "Bearer $currentToken")
            .build()

        return chain.proceed(newRequest)
    }
}


