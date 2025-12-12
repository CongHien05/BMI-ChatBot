package com.hienpc.bmiapp.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiClient - Quản lý Retrofit instance
 *
 * Base URL: http://10.0.2.2:8080/
 * - 10.0.2.2 là địa chỉ IP đặc biệt cho Android Emulator để truy cập localhost của máy host
 * - Port 8080 là port mặc định của Spring Boot
 *
 * Từ Sprint 2: thêm AuthInterceptor để tự động gắn Bearer token cho các request cần auth.
 */
object ApiClient {
    // Base URL cho Android Emulator
    private const val BASE_URL = "http://192.168.1.6:8080/"

    // OkHttpClient với AuthInterceptor
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    // Retrofit instance (singleton)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Tạo API service instance
     * @param serviceClass: Interface của API service (ví dụ: AuthApiService::class.java)
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
