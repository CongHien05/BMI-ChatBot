package com.hienpc.bmiapp.data.local

/**
 * TokenManager - giữ JWT token hiện tại trong memory
 *
 * DataStore dùng để lưu token bền vững (đã có trong AuthPreferences),
 * còn TokenManager cung cấp cách truy cập nhanh cho OkHttp interceptor
 * (do interceptor không gọi được suspend function).
 */
object TokenManager {
    @Volatile
    var token: String? = null
}


