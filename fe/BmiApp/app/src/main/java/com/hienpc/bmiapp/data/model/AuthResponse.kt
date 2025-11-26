package com.hienpc.bmiapp.data.model

/**
 * AuthResponse - Data class cho response từ API authentication
 * 
 * DTO (Data Transfer Object) để nhận dữ liệu từ server
 * Gson sẽ tự động deserialize JSON response thành object này
 * 
 * @param token: JWT token hoặc access token từ server
 * @param message: Thông báo từ server (optional)
 */
data class AuthResponse(
    val token: String,
    val message: String? = null
)

